package com.crasty.app.ui.screens.calendar

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.DatePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.crasty.app.R
import com.crasty.app.ui.components.TaskList
import com.crasty.app.ui.components.toUtcMillis
import com.crasty.app.ui.components.utcMillisToLocalDate
import java.time.LocalDate

/** Calendario: eliges un día y ves/añades/editas sus tareas. Versión provisional con el DatePicker de Material. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    onEditTask: (taskId: Long, date: LocalDate) -> Unit,
    viewModel: CalendarViewModel = viewModel(factory = CalendarViewModel.Factory),
) {
    val selectedDate by viewModel.selectedDate.collectAsStateWithLifecycle()
    val tasks by viewModel.tasks.collectAsStateWithLifecycle()
    val pickerState = rememberDatePickerState(initialSelectedDateMillis = selectedDate.toUtcMillis())

    LaunchedEffect(pickerState.selectedDateMillis) {
        pickerState.selectedDateMillis?.let { viewModel.select(it.utcMillisToLocalDate()) }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { onEditTask(0L, selectedDate) }) {
                Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.new_task))
            }
        },
    ) { padding ->
        Column(Modifier.padding(padding)) {
            DatePicker(state = pickerState, title = null, headline = null, showModeToggle = false)
            TaskList(
                tasks = tasks,
                onToggleCompleted = viewModel::toggleCompleted,
                onProcrastinate = viewModel::procrastinate,
                onClick = { onEditTask(it.id, it.date) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}
