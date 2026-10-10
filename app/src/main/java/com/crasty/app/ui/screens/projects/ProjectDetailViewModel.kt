package com.crasty.app.ui.screens.projects

import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.crasty.app.data.repository.TaskRepository
import com.crasty.app.di.appContainer
import com.crasty.app.domain.model.Project
import com.crasty.app.domain.model.Task
import com.crasty.app.ui.components.TaskListViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProjectDetailViewModel(
    projectId: Long,
    repository: TaskRepository,
) : TaskListViewModel(repository) {

    val project: StateFlow<Project?> = repository.observeProject(projectId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val tasks: StateFlow<List<Task>> = repository.observeProjectTasks(projectId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun toggleProjectCompleted() {
        val current = project.value ?: return
        viewModelScope.launch { repository.setProjectCompleted(current, !current.isCompleted) }
    }

    fun deleteProject(onDone: () -> Unit) {
        val current = project.value ?: return
        viewModelScope.launch {
            repository.deleteProject(current)
            onDone()
        }
    }

    companion object {
        fun factory(projectId: Long) = viewModelFactory {
            initializer { ProjectDetailViewModel(projectId, appContainer().taskRepository) }
        }
    }
}
