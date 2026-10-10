package com.crasty.app.ui.screens.projects

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.crasty.app.data.repository.TaskRepository
import com.crasty.app.di.appContainer
import com.crasty.app.domain.model.Project
import com.crasty.app.domain.model.ProjectSummary
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProjectsViewModel(private val repository: TaskRepository) : ViewModel() {

    val inProgress: StateFlow<List<ProjectSummary>> = repository.observeProjects(completed = false)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val completed: StateFlow<List<ProjectSummary>> = repository.observeProjects(completed = true)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun setCompleted(project: Project, completed: Boolean) {
        viewModelScope.launch { repository.setProjectCompleted(project, completed) }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer { ProjectsViewModel(appContainer().taskRepository) }
        }
    }
}

/** Todas sus tareas están hechas (o no tiene ninguna). */
val ProjectSummary.canComplete: Boolean get() = doneTasks == totalTasks
