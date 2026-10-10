package com.crasty.app.ui.screens.projects

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.crasty.app.R
import com.crasty.app.domain.model.Task
import com.crasty.app.ui.components.DeadlineErrorEffect
import com.crasty.app.ui.components.TaskList
import com.crasty.app.ui.components.dateTimeFormatter
import com.crasty.app.ui.theme.TaskColors

/** Un proyecto: sus tareas, y editar / borrar / completar el proyecto. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectDetailScreen(
    projectId: Long,
    onBack: () -> Unit,
    onEditProject: () -> Unit,
    onOpenTask: (Task) -> Unit,
    onNewTask: () -> Unit,
    viewModel: ProjectDetailViewModel = viewModel(factory = ProjectDetailViewModel.factory(projectId)),
) {
    val project by viewModel.project.collectAsStateWithLifecycle()
    val tasks by viewModel.tasks.collectAsStateWithLifecycle()
    var confirmDelete by rememberSaveable { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    DeadlineErrorEffect(viewModel, snackbarHostState)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(project?.name.orEmpty()) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                actions = {
                    IconButton(onClick = onEditProject) {
                        Icon(Icons.Outlined.Edit, contentDescription = stringResource(R.string.edit_project))
                    }
                    IconButton(onClick = { confirmDelete = true }) {
                        Icon(Icons.Outlined.Delete, contentDescription = stringResource(R.string.delete))
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(onClick = onNewTask) {
                Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.new_task))
            }
        },
    ) { padding ->
        val current = project ?: return@Scaffold
        val done = tasks.count { it.isCompleted }
        Column(Modifier.padding(padding)) {
            // Cabecera del proyecto en una tarjeta de su color.
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = TaskColors.project(current.colorIndex).resolve(),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        stringResource(R.string.project_deadline, current.deadline.format(dateTimeFormatter)),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Text(
                        stringResource(R.string.project_progress, done, tasks.size),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    LinearProgressIndicator(
                        progress = { if (tasks.isEmpty()) 0f else done.toFloat() / tasks.size },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    // Solo se puede completar con todas sus tareas hechas.
                    val canToggle = current.isCompleted || done == tasks.size
                    OutlinedButton(
                        onClick = viewModel::toggleProjectCompleted,
                        enabled = canToggle,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            stringResource(
                                when {
                                    current.isCompleted -> R.string.reopen_project
                                    canToggle -> R.string.complete_project
                                    else -> R.string.complete_project_pending
                                },
                            ),
                        )
                    }
                }
            }
            TaskList(
                tasks = tasks,
                onToggleCompleted = viewModel::toggleCompleted,
                onClick = onOpenTask,
                showDate = true,
                emptyText = stringResource(R.string.empty_project),
                modifier = Modifier.weight(1f),
            )
        }
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text(stringResource(R.string.delete_project)) },
            text = { Text(pluralStringResource(R.plurals.delete_project_confirm, tasks.size, tasks.size)) },
            confirmButton = {
                TextButton(onClick = {
                    confirmDelete = false
                    viewModel.deleteProject(onBack)
                }) { Text(stringResource(R.string.delete)) }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }) { Text(stringResource(android.R.string.cancel)) }
            },
        )
    }
}
