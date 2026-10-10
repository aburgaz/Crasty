package com.crasty.app.ui.screens.editor

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.crasty.app.data.repository.TaskAfterDeadlineException
import com.crasty.app.data.repository.TaskRepository
import com.crasty.app.di.appContainer
import com.crasty.app.domain.model.Project
import com.crasty.app.domain.model.Recurrence
import com.crasty.app.domain.model.RepeatUnit
import com.crasty.app.domain.model.Task
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime

data class EditorState(
    val title: String = "",
    val notes: String = "",
    val date: LocalDate,
    val reminderTime: LocalTime? = null,
    val isNew: Boolean = true,
    val project: Project? = null,
    val repeat: Boolean = false,
    val repeatInterval: Int = 1,
    val repeatUnit: RepeatUnit = RepeatUnit.DAYS,
) {
    /** Las tareas de un proyecto no pueden ser recurrentes. */
    val canRepeat: Boolean get() = project == null

    val recurrence: Recurrence? get() = if (repeat && canRepeat) Recurrence(repeatInterval, repeatUnit) else null

    /** La fecha/hora elegida cae después de la fecha límite del proyecto. */
    val violatesDeadline: Boolean
        get() = project != null && !Task(title = title, date = date, reminderTime = reminderTime).fitsBefore(project.deadline)
}

/** Crear (taskId = 0) o editar una tarea. projectId = 0: sin proyecto. */
class TaskEditorViewModel(
    taskId: Long,
    initialDate: LocalDate,
    projectId: Long,
    private val repository: TaskRepository,
) : ViewModel() {

    private var original: Task? = null

    var state by mutableStateOf(EditorState(date = initialDate))
        private set

    init {
        viewModelScope.launch {
            val task = if (taskId != 0L) repository.get(taskId) else null
            original = task
            val project = (task?.projectId ?: projectId.takeIf { it != 0L })?.let { repository.getProject(it) }
            state = if (task == null) {
                state.copy(project = project)
            } else {
                EditorState(
                    title = task.title,
                    notes = task.notes,
                    date = task.date,
                    reminderTime = task.reminderTime,
                    isNew = false,
                    project = project,
                    repeat = task.isRecurring,
                    repeatInterval = task.recurrence?.interval ?: 1,
                    repeatUnit = task.recurrence?.unit ?: RepeatUnit.DAYS,
                )
            }
        }
    }

    fun onTitleChange(value: String) { state = state.copy(title = value) }
    fun onNotesChange(value: String) { state = state.copy(notes = value) }
    fun onDateChange(value: LocalDate) { state = state.copy(date = value) }
    fun onReminderChange(value: LocalTime?) { state = state.copy(reminderTime = value) }
    fun onRepeatChange(value: Boolean) { state = state.copy(repeat = value) }
    fun onRepeatIntervalChange(value: Int) { state = state.copy(repeatInterval = value.coerceIn(1, 99)) }
    fun onRepeatUnitChange(value: RepeatUnit) { state = state.copy(repeatUnit = value) }

    fun save(onDone: () -> Unit) {
        if (state.title.isBlank() || state.violatesDeadline) return
        viewModelScope.launch {
            val base = original ?: Task(title = "", date = state.date)
            val recurrence = state.recurrence
            try {
                repository.save(
                    base.copy(
                        title = state.title.trim(),
                        notes = state.notes.trim(),
                        date = state.date,
                        reminderTime = state.reminderTime,
                        projectId = state.project?.id,
                        recurrence = recurrence,
                        // Una recurrente se completa por días, no como un todo.
                        isCompleted = base.isCompleted && recurrence == null,
                    ),
                )
                onDone()
            } catch (_: TaskAfterDeadlineException) {
                // violatesDeadline ya lo muestra en pantalla.
            }
        }
    }

    fun delete(onDone: () -> Unit) {
        val task = original ?: return
        viewModelScope.launch {
            repository.delete(task)
            onDone()
        }
    }

    companion object {
        fun factory(taskId: Long, initialDate: LocalDate, projectId: Long) = viewModelFactory {
            initializer { TaskEditorViewModel(taskId, initialDate, projectId, appContainer().taskRepository) }
        }
    }
}
