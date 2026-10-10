package com.crasty.app.ui.screens.day

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.crasty.app.R
import com.crasty.app.ui.components.TaskList
import java.time.LocalDate

/** Pantalla de un día concreto (se usa para Hoy y Mañana). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DayScreen(
    date: LocalDate,
    title: String,
    onEditTask: (taskId: Long, date: LocalDate) -> Unit,
    onOpenSettings: () -> Unit,
) {
    val viewModel: DayViewModel = viewModel(key = "day-$date", factory = DayViewModel.factory(date))
    val tasks by viewModel.tasks.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                actions = {
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Outlined.Settings, contentDescription = stringResource(R.string.settings))
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { onEditTask(0L, date) }) {
                Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.new_task))
            }
        },
    ) { padding ->
        TaskList(
            tasks = tasks,
            onToggleCompleted = viewModel::toggleCompleted,
            onProcrastinate = viewModel::procrastinate,
            onClick = { onEditTask(it.id, it.date) },
            modifier = Modifier.padding(padding),
        )
    }
}
