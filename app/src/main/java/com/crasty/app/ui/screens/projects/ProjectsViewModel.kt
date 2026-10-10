package com.crasty.app.ui.screens.projects

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.crasty.app.data.repository.TaskRepository
import com.crasty.app.di.appContainer
import com.crasty.app.domain.model.ProjectSummary
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class ProjectsViewModel(repository: TaskRepository) : ViewModel() {

    val inProgress: StateFlow<List<ProjectSummary>> = repository.observeProjects(completed = false)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val completed: StateFlow<List<ProjectSummary>> = repository.observeProjects(completed = true)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    companion object {
        val Factory = viewModelFactory {
            initializer { ProjectsViewModel(appContainer().taskRepository) }
        }
    }
}
