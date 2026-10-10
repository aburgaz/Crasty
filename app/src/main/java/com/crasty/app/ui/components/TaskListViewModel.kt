package com.crasty.app.ui.components

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.crasty.app.R
import com.crasty.app.data.repository.TaskAfterDeadlineException
import com.crasty.app.data.repository.TaskRepository
import com.crasty.app.domain.model.Task
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime

/** Acciones comunes de las pantallas que muestran listas de tareas. */
abstract class TaskListViewModel(protected val repository: TaskRepository) : ViewModel() {

    /** Fecha límite del proyecto que impidió la última acción; la UI la muestra y llama a [errorShown]. */
    var deadlineError by mutableStateOf<LocalDateTime?>(null)
        private set

    fun toggleCompleted(task: Task) = launchChecked { repository.setCompleted(task, !task.isCompleted) }

    fun moveTo(task: Task, date: LocalDate) = launchChecked { repository.moveTo(task, date) }

    fun errorShown() {
        deadlineError = null
    }

    protected fun launchChecked(block: suspend () -> Unit) {
        viewModelScope.launch {
            try {
                block()
            } catch (e: TaskAfterDeadlineException) {
                deadlineError = e.deadline
            }
        }
    }
}

/** Muestra en un snackbar los errores de fecha límite de [viewModel]. */
@Composable
fun DeadlineErrorEffect(viewModel: TaskListViewModel, snackbarHostState: SnackbarHostState) {
    val context = LocalContext.current
    val deadline = viewModel.deadlineError
    LaunchedEffect(deadline) {
        if (deadline != null) {
            snackbarHostState.showSnackbar(context.getString(R.string.error_after_deadline, deadline.format(dateTimeFormatter)))
            viewModel.errorShown()
        }
    }
}
