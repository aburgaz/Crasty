package com.crasty.app.ui.screens.projects

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.crasty.app.data.repository.ProjectDeadlineConflictException
import com.crasty.app.data.repository.TaskRepository
import com.crasty.app.di.appContainer
import com.crasty.app.domain.model.Project
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

data class ProjectEditorState(
    val name: String = "",
    val deadline: LocalDateTime = LocalDate.now().plusWeeks(1).atTime(18, 0),
    val isNew: Boolean = true,
    val colorIndex: Int = 0,
    /** Tareas pendientes que quedarían después de la nueva fecha límite (0 = sin problema). */
    val conflictingTasks: Int = 0,
)

/** Crear (projectId = 0) o editar un proyecto. */
class ProjectEditorViewModel(
    projectId: Long,
    private val repository: TaskRepository,
) : ViewModel() {

    private var original: Project? = null

    var state by mutableStateOf(ProjectEditorState())
        private set

    init {
        if (projectId != 0L) {
            viewModelScope.launch {
                repository.getProject(projectId)?.let {
                    original = it
                    state = ProjectEditorState(it.name, it.deadline, isNew = false, colorIndex = it.colorIndex)
                }
            }
        }
    }

    fun onNameChange(value: String) {
        state = state.copy(name = value)
    }

    fun onColorChange(value: Int) {
        state = state.copy(colorIndex = value)
    }

    fun onDateChange(value: LocalDate) {
        state = state.copy(deadline = value.atTime(state.deadline.toLocalTime()), conflictingTasks = 0)
    }

    fun onTimeChange(value: LocalTime) {
        state = state.copy(deadline = state.deadline.toLocalDate().atTime(value), conflictingTasks = 0)
    }

    /** [onSaved] recibe el id del proyecto y si es nuevo. */
    fun save(onSaved: (id: Long, isNew: Boolean) -> Unit) {
        if (state.name.isBlank()) return
        viewModelScope.launch {
            val base = original ?: Project(name = "", deadline = state.deadline)
            try {
                val id = repository.saveProject(
                    base.copy(name = state.name.trim(), deadline = state.deadline, colorIndex = state.colorIndex),
                )
                onSaved(id, original == null)
            } catch (e: ProjectDeadlineConflictException) {
                state = state.copy(conflictingTasks = e.conflictingTasks)
            }
        }
    }

    companion object {
        fun factory(projectId: Long) = viewModelFactory {
            initializer { ProjectEditorViewModel(projectId, appContainer().taskRepository) }
        }
    }
}
