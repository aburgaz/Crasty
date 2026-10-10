package com.crasty.app.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CreateNewFolder
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.crasty.app.R
import com.crasty.app.domain.model.Task
import com.crasty.app.ui.components.DeadlineErrorEffect
import com.crasty.app.ui.components.TaskList
import java.time.LocalDate

private const val TAB_TODAY = 0
private const val TAB_TOMORROW = 1

/** Pantalla principal: pestañas Hoy / Mañana. Se arrastra a la derecha para pasar a mañana y a la izquierda para volver a hoy. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onOpenTask: (Task) -> Unit,
    onNewTask: (LocalDate) -> Unit,
    onNewProject: () -> Unit,
    onOpenProjects: () -> Unit,
    onOpenCompleted: () -> Unit,
    onOpenCalendar: () -> Unit,
    onOpenSettings: () -> Unit,
    viewModel: HomeViewModel = viewModel(factory = HomeViewModel.Factory),
) {
    var tab by rememberSaveable { mutableIntStateOf(TAB_TODAY) }
    val today by viewModel.today.collectAsStateWithLifecycle()
    val tomorrow by viewModel.tomorrow.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    DeadlineErrorEffect(viewModel, snackbarHostState)

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text(stringResource(R.string.app_name)) },
                    actions = {
                        IconButton(onClick = onOpenProjects) {
                            Icon(Icons.Outlined.Folder, contentDescription = stringResource(R.string.projects))
                        }
                        IconButton(onClick = onOpenCompleted) {
                            Icon(Icons.Outlined.TaskAlt, contentDescription = stringResource(R.string.completed_tasks))
                        }
                        IconButton(onClick = onOpenCalendar) {
                            Icon(Icons.Outlined.CalendarMonth, contentDescription = stringResource(R.string.calendar))
                        }
                        IconButton(onClick = onOpenSettings) {
                            Icon(Icons.Outlined.Settings, contentDescription = stringResource(R.string.settings))
                        }
                    },
                )
                PrimaryTabRow(selectedTabIndex = tab) {
                    Tab(selected = tab == TAB_TODAY, onClick = { tab = TAB_TODAY }, text = { Text(stringResource(R.string.tab_today)) })
                    Tab(selected = tab == TAB_TOMORROW, onClick = { tab = TAB_TOMORROW }, text = { Text(stringResource(R.string.tab_tomorrow)) })
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButtonPosition = FabPosition.Center,
        floatingActionButton = {
            // Proyecto a la izquierda, tarea a la derecha.
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                FloatingActionButton(onClick = onNewProject) {
                    Icon(Icons.Outlined.CreateNewFolder, contentDescription = stringResource(R.string.new_project))
                }
                FloatingActionButton(onClick = {
                    onNewTask(if (tab == TAB_TODAY) viewModel.todayDate else viewModel.tomorrowDate)
                }) {
                    Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.new_task))
                }
            }
        },
    ) { padding ->
        TaskList(
            tasks = if (tab == TAB_TODAY) today else tomorrow,
            onToggleCompleted = viewModel::toggleCompleted,
            onClick = onOpenTask,
            onSwipeRight = if (tab == TAB_TODAY) { task -> viewModel.moveTo(task, viewModel.tomorrowDate) } else null,
            onSwipeLeft = if (tab == TAB_TOMORROW) { task -> viewModel.moveTo(task, viewModel.todayDate) } else null,
            modifier = Modifier.padding(padding),
        )
    }
}
