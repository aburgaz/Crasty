package com.crasty.app.data.repository

import androidx.room.withTransaction
import com.crasty.app.data.local.CrastyDatabase
import com.crasty.app.data.local.RecurringCompletionEntity
import com.crasty.app.data.local.toEntity
import com.crasty.app.data.local.toProject
import com.crasty.app.data.local.toSummary
import com.crasty.app.data.local.toTask
import com.crasty.app.domain.model.Project
import com.crasty.app.domain.model.ProjectSummary
import com.crasty.app.domain.model.Task
import com.crasty.app.reminders.ReminderScheduler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.YearMonth

/** La tarea quedaría después de la fecha límite de su proyecto. */
class TaskAfterDeadlineException(val deadline: LocalDateTime) : Exception()

/** La nueva fecha límite dejaría fuera [conflictingTasks] tareas pendientes del proyecto. */
class ProjectDeadlineConflictException(val conflictingTasks: Int) : Exception()

/** Todo el contenido de la app, para exportar/importar. En [recurringCompletions], (id de tarea, día). */
data class Snapshot(
    val projects: List<Project>,
    val tasks: List<Task>,
    val recurringCompletions: List<Pair<Long, LocalDate>>,
)

/**
 * Única puerta de acceso a tareas y proyectos. Aplica las reglas (fechas límite de proyecto)
 * y mantiene los recordatorios sincronizados con la BD.
 */
class TaskRepository(
    private val database: CrastyDatabase,
    private val reminders: ReminderScheduler,
) {
    private val taskDao = database.taskDao()
    private val projectDao = database.projectDao()

    // --- Tareas ---

    /** Tareas de un día: las normales de ese día más las repeticiones de recurrentes que caen en él. */
    fun observeDay(date: LocalDate): Flow<List<Task>> {
        val day = date.toEpochDay()
        return combine(
            taskDao.observeOneOffByDay(day),
            taskDao.observeRecurringStartedBy(day),
            taskDao.observeCompletedRecurringIds(day),
        ) { oneOff, recurring, completedIds ->
            val occurrences = recurring.map { it.toTask() }
                .filter { it.recurrence!!.occursOn(it.date, date) }
                .map { it.copy(date = date, isCompleted = it.id in completedIds) }
            (oneOff.map { it.toTask() } + occurrences).sortedWith(
                compareBy<Task>({ it.isCompleted }, { it.reminderTime ?: LocalTime.MAX }, { it.createdAt }),
            )
        }
    }

    /** Tareas normales completadas en [month] (las recurrentes no se incluyen). */
    fun observeCompleted(month: YearMonth): Flow<List<Task>> =
        taskDao.observeCompleted(month.atDay(1).toEpochDay(), month.atEndOfMonth().toEpochDay())
            .map { list -> list.map { it.toTask() } }

    fun observeProjectTasks(projectId: Long): Flow<List<Task>> =
        taskDao.observeByProject(projectId).map { list -> list.map { it.toTask() } }

    suspend fun get(id: Long): Task? = taskDao.getById(id)?.toTask()

    /** Crea o actualiza una tarea. Lanza [TaskAfterDeadlineException] si no cabe en su proyecto. */
    suspend fun save(task: Task): Long {
        task.projectId?.let { projectDao.getById(it) }?.toProject()?.let { project ->
            if (!task.fitsBefore(project.deadline)) throw TaskAfterDeadlineException(project.deadline)
        }
        return upsert(task)
    }

    /** En las recurrentes solo marca la repetición de [Task.date]. */
    suspend fun setCompleted(task: Task, completed: Boolean) {
        if (task.isRecurring) {
            val day = task.date.toEpochDay()
            if (completed) {
                taskDao.insertCompletion(RecurringCompletionEntity(task.id, day))
            } else {
                taskDao.deleteCompletion(task.id, day)
            }
            get(task.id)?.let { syncReminder(it) }
        } else {
            upsert(task.copy(isCompleted = completed, completedOn = if (completed) LocalDate.now() else null))
        }
    }

    /** Mueve una tarea normal a otro día (procrastinar = moverla a un día posterior). */
    suspend fun moveTo(task: Task, date: LocalDate) {
        require(!task.isRecurring)
        val postponed = if (date.isAfter(task.date)) task.postponedCount + 1 else task.postponedCount
        save(task.copy(date = date, postponedCount = postponed))
    }

    /** Desmarca una tarea completada y la devuelve a hoy. */
    suspend fun restore(task: Task) {
        save(task.copy(isCompleted = false, completedOn = null, date = LocalDate.now()))
    }

    suspend fun delete(task: Task) {
        taskDao.delete(task.id)
        reminders.cancel(task.id)
    }

    private suspend fun upsert(task: Task): Long {
        val insertedId = taskDao.upsert(task.toEntity())
        val id = if (task.id == 0L) insertedId else task.id
        syncReminder(task.copy(id = id))
        return id
    }

    // --- Proyectos ---

    fun observeProjects(completed: Boolean): Flow<List<ProjectSummary>> =
        projectDao.observeSummaries(completed).map { list -> list.map { it.toSummary() } }

    fun observeProject(id: Long): Flow<Project?> = projectDao.observeById(id).map { it?.toProject() }

    suspend fun getProject(id: Long): Project? = projectDao.getById(id)?.toProject()

    /** Lanza [ProjectDeadlineConflictException] si la fecha límite deja fuera tareas pendientes. */
    suspend fun saveProject(project: Project): Long {
        if (project.id != 0L) {
            val conflicts = taskDao.getByProject(project.id)
                .map { it.toTask() }
                .count { !it.isCompleted && !it.fitsBefore(project.deadline) }
            if (conflicts > 0) throw ProjectDeadlineConflictException(conflicts)
        }
        val insertedId = projectDao.upsert(project.toEntity())
        return if (project.id == 0L) insertedId else project.id
    }

    suspend fun setProjectCompleted(project: Project, completed: Boolean) {
        projectDao.upsert(project.copy(isCompleted = completed).toEntity())
    }

    /** Borra el proyecto y todas sus tareas. */
    suspend fun deleteProject(project: Project) {
        val tasks = taskDao.getByProject(project.id)
        database.withTransaction {
            taskDao.deleteByProject(project.id)
            projectDao.deleteById(project.id)
        }
        tasks.forEach { reminders.cancel(it.id) }
    }

    // --- Copia de seguridad ---

    suspend fun snapshot() = Snapshot(
        projects = projectDao.getAll().map { it.toProject() },
        tasks = taskDao.getAll().map { it.toTask() },
        recurringCompletions = taskDao.getAllCompletions().map { it.taskId to LocalDate.ofEpochDay(it.dateEpochDay) },
    )

    /** Sustituye todo por [snapshot]. Sus ids solo se usan para enlazar tareas con proyectos y repeticiones. */
    suspend fun replaceAll(snapshot: Snapshot) {
        taskDao.getAll().forEach { reminders.cancel(it.id) }
        database.withTransaction {
            taskDao.deleteAllCompletions()
            taskDao.deleteAll()
            projectDao.deleteAll()
            val projectIds = snapshot.projects.associate { it.id to projectDao.insert(it.copy(id = 0).toEntity()) }
            val taskIds = snapshot.tasks.associate {
                it.id to taskDao.insert(it.copy(id = 0, projectId = it.projectId?.let(projectIds::get)).toEntity())
            }
            snapshot.recurringCompletions.forEach { (taskKey, day) ->
                taskIds[taskKey]?.let { taskDao.insertCompletion(RecurringCompletionEntity(it, day.toEpochDay())) }
            }
        }
        rescheduleAllReminders()
    }

    // --- Recordatorios ---

    /** Tras saltar un recordatorio: programa la siguiente repetición (si es recurrente). */
    suspend fun refreshReminder(taskId: Long) {
        get(taskId)?.let { syncReminder(it) }
    }

    suspend fun rescheduleAllReminders() {
        taskDao.getWithUpcomingReminder(LocalDate.now().toEpochDay())
            .forEach { syncReminder(it.toTask()) }
    }

    private suspend fun syncReminder(task: Task) {
        reminders.schedule(task.id, task.title, nextReminder(task))
    }

    private suspend fun nextReminder(task: Task): LocalDateTime? {
        val time = task.reminderTime ?: return null
        val now = LocalDateTime.now()
        val recurrence = task.recurrence
            ?: return task.date.atTime(time).takeIf { !task.isCompleted && it.isAfter(now) }
        // Siguiente repetición futura que no esté ya completada.
        var day = recurrence.nextOnOrAfter(task.date, now.toLocalDate())
        repeat(MAX_OCCURRENCES_TO_CHECK) {
            val at = day.atTime(time)
            if (at.isAfter(now) && !taskDao.isRecurringCompleted(task.id, day.toEpochDay())) return at
            day = recurrence.nextOnOrAfter(task.date, day.plusDays(1))
        }
        return null
    }

    private companion object {
        const val MAX_OCCURRENCES_TO_CHECK = 60
    }
}
