package com.crasty.app.ui.screens.editor

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.crasty.app.data.repository.TaskRepository
import com.crasty.app.di.appContainer
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
)

/** Crear (taskId = 0) o editar una tarea. */
class TaskEditorViewModel(
    taskId: Long,
    initialDate: LocalDate,
    private val repository: TaskRepository,
) : ViewModel() {

    private var original: Task? = null

    var state by mutableStateOf(EditorState(date = initialDate))
        private set

    init {
        if (taskId != 0L) {
            viewModelScope.launch {
                repository.get(taskId)?.let {
                    original = it
                    state = EditorState(it.title, it.notes, it.date, it.reminderTime, isNew = false)
                }
            }
        }
    }

    fun onTitleChange(value: String) { state = state.copy(title = value) }
    fun onNotesChange(value: String) { state = state.copy(notes = value) }
    fun onDateChange(value: LocalDate) { state = state.copy(date = value) }
    fun onReminderChange(value: LocalTime?) { state = state.copy(reminderTime = value) }

    fun save(onDone: () -> Unit) {
        if (state.title.isBlank()) return
        viewModelScope.launch {
            val base = original ?: Task(title = "", date = state.date)
            repository.save(
                base.copy(
                    title = state.title.trim(),
                    notes = state.notes.trim(),
                    date = state.date,
                    reminderTime = state.reminderTime,
                ),
            )
            onDone()
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
        fun factory(taskId: Long, initialDate: LocalDate) = viewModelFactory {
            initializer { TaskEditorViewModel(taskId, initialDate, appContainer().taskRepository) }
        }
    }
}
