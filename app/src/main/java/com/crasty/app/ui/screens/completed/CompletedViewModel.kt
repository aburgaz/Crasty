package com.crasty.app.ui.screens.completed

import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.crasty.app.data.repository.TaskRepository
import com.crasty.app.di.appContainer
import com.crasty.app.domain.model.Task
import com.crasty.app.ui.components.TaskListViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import java.time.YearMonth

/** Tareas completadas de un mes, agrupadas por el día en que se completaron (más reciente primero). */
@OptIn(ExperimentalCoroutinesApi::class)
class CompletedViewModel(repository: TaskRepository) : TaskListViewModel(repository) {

    private val _month = MutableStateFlow(YearMonth.now())
    val month: StateFlow<YearMonth> = _month.asStateFlow()

    val days: StateFlow<List<Pair<LocalDate, List<Task>>>> = _month
        .flatMapLatest { repository.observeCompleted(it) }
        .map { tasks ->
            tasks.groupBy { it.completedOn ?: it.date }
                .toSortedMap(compareByDescending { it })
                .toList()
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun selectMonth(month: YearMonth) {
        _month.value = month
    }

    /** Desmarca la tarea y la devuelve a hoy. */
    fun restore(task: Task) = launchChecked { repository.restore(task) }

    companion object {
        val Factory = viewModelFactory {
            initializer { CompletedViewModel(appContainer().taskRepository) }
        }
    }
}
