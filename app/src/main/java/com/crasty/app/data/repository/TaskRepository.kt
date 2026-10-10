package com.crasty.app.data.repository

import com.crasty.app.data.local.TaskDao
import com.crasty.app.data.local.toEntity
import com.crasty.app.data.local.toTask
import com.crasty.app.domain.model.Task
import com.crasty.app.reminders.ReminderScheduler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

/** Única puerta de acceso a las tareas. Mantiene los recordatorios sincronizados con la BD. */
class TaskRepository(
    private val dao: TaskDao,
    private val reminders: ReminderScheduler,
) {
    fun observeDay(date: LocalDate): Flow<List<Task>> =
        dao.observeByDay(date.toEpochDay()).map { list -> list.map { it.toTask() } }

    suspend fun get(id: Long): Task? = dao.getById(id)?.toTask()

    suspend fun getAll(): List<Task> = dao.getAll().map { it.toTask() }

    suspend fun save(task: Task): Long {
        val insertedId = dao.upsert(task.toEntity())
        val id = if (task.id == 0L) insertedId else task.id
        reminders.sync(task.copy(id = id))
        return id
    }

    suspend fun setCompleted(task: Task, completed: Boolean) {
        save(task.copy(isCompleted = completed))
    }

    /** Pasa la tarea al día siguiente (el recordatorio, si lo hay, se mueve con ella). */
    suspend fun procrastinate(task: Task) {
        save(task.copy(date = task.date.plusDays(1), postponedCount = task.postponedCount + 1))
    }

    suspend fun delete(task: Task) {
        dao.deleteById(task.id)
        reminders.cancel(task.id)
    }

    /** Sustituye todas las tareas (usado al importar una copia de seguridad). */
    suspend fun replaceAll(tasks: List<Task>) {
        dao.getAll().forEach { reminders.cancel(it.id) }
        dao.replaceAll(tasks.map { it.toEntity().copy(id = 0) })
        rescheduleAllReminders()
    }

    suspend fun rescheduleAllReminders() {
        dao.getPendingWithReminder(LocalDate.now().toEpochDay())
            .forEach { reminders.sync(it.toTask()) }
    }
}
