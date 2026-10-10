package com.crasty.app.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Snooze
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import com.crasty.app.R
import com.crasty.app.domain.model.Task

/** Lista de tareas de un día. Compartida por Hoy, Mañana y Calendario. */
@Composable
fun TaskList(
    tasks: List<Task>,
    onToggleCompleted: (Task) -> Unit,
    onProcrastinate: (Task) -> Unit,
    onClick: (Task) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (tasks.isEmpty()) {
        Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(stringResource(R.string.empty_day), style = MaterialTheme.typography.bodyLarge)
        }
        return
    }
    LazyColumn(modifier.fillMaxSize()) {
        items(tasks, key = { it.id }) { task ->
            TaskRow(
                task = task,
                onToggleCompleted = { onToggleCompleted(task) },
                onProcrastinate = { onProcrastinate(task) },
                onClick = { onClick(task) },
            )
        }
    }
}

@Composable
private fun TaskRow(
    task: Task,
    onToggleCompleted: () -> Unit,
    onProcrastinate: () -> Unit,
    onClick: () -> Unit,
) {
    ListItem(
        modifier = Modifier.clickable(onClick = onClick),
        leadingContent = {
            Checkbox(checked = task.isCompleted, onCheckedChange = { onToggleCompleted() })
        },
        headlineContent = {
            Text(
                text = task.title,
                textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null,
            )
        },
        supportingContent = task.reminderTime?.let { time -> { Text(time.toString()) } },
        trailingContent = if (task.isCompleted) null else {
            {
                IconButton(onClick = onProcrastinate) {
                    Icon(Icons.Outlined.Snooze, contentDescription = stringResource(R.string.procrastinate))
                }
            }
        },
    )
}
