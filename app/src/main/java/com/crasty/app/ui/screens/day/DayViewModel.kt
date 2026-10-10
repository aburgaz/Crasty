package com.crasty.app.ui.screens.day

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.crasty.app.data.repository.TaskRepository
import com.crasty.app.di.appContainer
import com.crasty.app.domain.model.Task
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

class DayViewModel(
    date: LocalDate,
    private val repository: TaskRepository,
) : ViewModel() {

    val tasks: StateFlow<List<Task>> = repository.observeDay(date)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun toggleCompleted(task: Task) {
        viewModelScope.launch { repository.setCompleted(task, !task.isCompleted) }
    }

    fun procrastinate(task: Task) {
        viewModelScope.launch { repository.procrastinate(task) }
    }

    companion object {
        fun factory(date: LocalDate) = viewModelFactory {
            initializer { DayViewModel(date, appContainer().taskRepository) }
        }
    }
}
