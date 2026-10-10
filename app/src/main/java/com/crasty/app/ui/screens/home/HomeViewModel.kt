package com.crasty.app.ui.screens.home

import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.crasty.app.data.repository.TaskRepository
import com.crasty.app.di.appContainer
import com.crasty.app.domain.model.Task
import com.crasty.app.ui.components.TaskListViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate

class HomeViewModel(repository: TaskRepository) : TaskListViewModel(repository) {

    val todayDate: LocalDate = LocalDate.now()
    val tomorrowDate: LocalDate = todayDate.plusDays(1)

    // Solo pendientes: las completadas se ven en Completadas (y las recurrentes, en el Calendario).
    val today: StateFlow<List<Task>> = pendingOn(todayDate)

    val tomorrow: StateFlow<List<Task>> = pendingOn(tomorrowDate)

    private fun pendingOn(date: LocalDate): StateFlow<List<Task>> = repository.observeDay(date)
        .map { tasks -> tasks.filterNot { it.isCompleted } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    companion object {
        val Factory = viewModelFactory {
            initializer { HomeViewModel(appContainer().taskRepository) }
        }
    }
}
