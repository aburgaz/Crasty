package com.crasty.app.ui.screens.calendar

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.crasty.app.R
import com.crasty.app.domain.model.Task
import com.crasty.app.ui.components.DeadlineErrorEffect
import com.crasty.app.ui.components.TaskList
import com.crasty.app.ui.components.fullDateFormatter
import com.crasty.app.ui.components.toUtcMillis
import com.crasty.app.ui.components.utcMillisToLocalDate
import java.time.LocalDate

/** Calendario: eliges un día y ves/añades/editas sus tareas. Versión provisional con el DatePicker de Material. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    onBack: () -> Unit,
    onOpenTask: (Task) -> Unit,
    onNewTask: (LocalDate) -> Unit,
    viewModel: CalendarViewModel = viewModel(factory = CalendarViewModel.Factory),
) {
    val selectedDate by viewModel.selectedDate.collectAsStateWithLifecycle()
    val tasks by viewModel.tasks.collectAsStateWithLifecycle()
    val pickerState = rememberDatePickerState(initialSelectedDateMillis = selectedDate.toUtcMillis())
    val snackbarHostState = remember { SnackbarHostState() }
    DeadlineErrorEffect(viewModel, snackbarHostState)

    LaunchedEffect(pickerState.selectedDateMillis) {
        pickerState.selectedDateMillis?.let { viewModel.select(it.utcMillisToLocalDate()) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.calendar)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(onClick = { onNewTask(selectedDate) }) {
                Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.new_task))
            }
        },
    ) { padding ->
        Column(Modifier.padding(padding)) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                shadowElevation = 2.dp,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            ) {
                DatePicker(
                    state = pickerState,
                    title = null,
                    headline = null,
                    showModeToggle = false,
                    colors = DatePickerDefaults.colors(containerColor = Color.Transparent),
                )
            }
            Text(
                text = stringResource(R.string.tasks_of_day, selectedDate.format(fullDateFormatter)),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 12.dp),
            )
            TaskList(
                tasks = tasks,
                onToggleCompleted = viewModel::toggleCompleted,
                onClick = onOpenTask,
                modifier = Modifier.weight(1f),
            )
        }
    }
}
