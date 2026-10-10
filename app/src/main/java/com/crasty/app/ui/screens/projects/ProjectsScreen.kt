package com.crasty.app.ui.screens.projects

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.CreateNewFolder
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.crasty.app.R
import com.crasty.app.domain.model.ProjectSummary
import com.crasty.app.ui.components.dateTimeFormatter

/** Lista de proyectos con dos pestañas: en curso y completados. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectsScreen(
    onBack: () -> Unit,
    onOpenProject: (Long) -> Unit,
    onNewProject: () -> Unit,
    viewModel: ProjectsViewModel = viewModel(factory = ProjectsViewModel.Factory),
) {
    var tab by rememberSaveable { mutableIntStateOf(0) }
    val inProgress by viewModel.inProgress.collectAsStateWithLifecycle()
    val completed by viewModel.completed.collectAsStateWithLifecycle()
    val projects = if (tab == 0) inProgress else completed

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text(stringResource(R.string.projects)) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                        }
                    },
                )
                PrimaryTabRow(selectedTabIndex = tab) {
                    Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text(stringResource(R.string.projects_in_progress)) })
                    Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text(stringResource(R.string.projects_completed)) })
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNewProject) {
                Icon(Icons.Outlined.CreateNewFolder, contentDescription = stringResource(R.string.new_project))
            }
        },
    ) { padding ->
        if (projects.isEmpty()) {
            Box(Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(stringResource(R.string.empty_projects), style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            LazyColumn(Modifier.padding(padding).fillMaxSize()) {
                items(projects, key = { it.project.id }) { summary ->
                    ProjectRow(summary, onClick = { onOpenProject(summary.project.id) })
                }
            }
        }
    }
}

@Composable
private fun ProjectRow(summary: ProjectSummary, onClick: () -> Unit) {
    ListItem(
        modifier = Modifier.clickable(onClick = onClick),
        leadingContent = { Icon(Icons.Outlined.Folder, contentDescription = null) },
        headlineContent = { Text(summary.project.name) },
        supportingContent = {
            Text(
                stringResource(
                    R.string.project_summary,
                    summary.project.deadline.format(dateTimeFormatter),
                    summary.doneTasks,
                    summary.totalTasks,
                ),
            )
        },
    )
}
