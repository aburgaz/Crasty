package com.crasty.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.crasty.app.R
import com.crasty.app.domain.model.Recurrence
import com.crasty.app.domain.model.RepeatUnit
import com.crasty.app.domain.model.Task
import kotlinx.coroutines.launch

/**
 * Lista de tareas. Si se pasa [onSwipeRight]/[onSwipeLeft], las tareas normales pendientes
 * se pueden arrastrar en esa dirección (las recurrentes nunca).
 */
@Composable
fun TaskList(
    tasks: List<Task>,
    onToggleCompleted: (Task) -> Unit,
    onClick: (Task) -> Unit,
    modifier: Modifier = Modifier,
    onSwipeRight: ((Task) -> Unit)? = null,
    onSwipeLeft: ((Task) -> Unit)? = null,
    showDate: Boolean = false,
    emptyText: String = stringResource(R.string.empty_day),
) {
    if (tasks.isEmpty()) {
        Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(emptyText, style = MaterialTheme.typography.bodyLarge)
        }
        return
    }
    // Hueco al final para que el botón flotante no tape la última tarea.
    LazyColumn(modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 88.dp)) {
        items(tasks, key = { it.id }) { task ->
            SwipeableTask(
                task = task,
                onSwipeRight = onSwipeRight?.takeIf { task.isMovable },
                onSwipeLeft = onSwipeLeft?.takeIf { task.isMovable },
            ) {
                TaskRow(
                    task = task,
                    onToggleCompleted = { onToggleCompleted(task) },
                    onClick = { onClick(task) },
                    showDate = showDate,
                )
            }
        }
    }
}

@Composable
private fun SwipeableTask(
    task: Task,
    onSwipeRight: ((Task) -> Unit)?,
    onSwipeLeft: ((Task) -> Unit)?,
    content: @Composable () -> Unit,
) {
    if (onSwipeRight == null && onSwipeLeft == null) {
        content()
        return
    }
    val state = rememberSwipeToDismissBoxState()
    val scope = rememberCoroutineScope()
    SwipeToDismissBox(
        state = state,
        enableDismissFromStartToEnd = onSwipeRight != null,
        enableDismissFromEndToStart = onSwipeLeft != null,
        onDismiss = { direction ->
            when (direction) {
                SwipeToDismissBoxValue.StartToEnd -> onSwipeRight?.invoke(task)
                SwipeToDismissBoxValue.EndToStart -> onSwipeLeft?.invoke(task)
                SwipeToDismissBoxValue.Settled -> Unit
            }
            // Si la tarea se mueve, desaparece de la lista; si no se pudo mover, vuelve a su sitio.
            scope.launch { state.reset() }
        },
        backgroundContent = { SwipeBackground(state.dismissDirection) },
    ) {
        content()
    }
}

@Composable
private fun SwipeBackground(direction: SwipeToDismissBoxValue) {
    val (label, alignment, icon) = when (direction) {
        SwipeToDismissBoxValue.StartToEnd ->
            Triple(R.string.tab_tomorrow, Alignment.CenterStart, Icons.AutoMirrored.Filled.ArrowForward)
        SwipeToDismissBoxValue.EndToStart ->
            Triple(R.string.tab_today, Alignment.CenterEnd, Icons.AutoMirrored.Filled.ArrowBack)
        SwipeToDismissBoxValue.Settled -> return
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .padding(horizontal = 24.dp),
        contentAlignment = alignment,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer)
            Text(stringResource(label), color = MaterialTheme.colorScheme.onSecondaryContainer)
        }
    }
}

@Composable
fun TaskRow(
    task: Task,
    onToggleCompleted: () -> Unit,
    onClick: () -> Unit,
    showDate: Boolean = false,
) {
    val details = listOfNotNull(
        task.date.format(mediumDateFormatter).takeIf { showDate },
        task.reminderTime?.toString(),
        task.recurrence?.let { recurrenceLabel(it) },
    )
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
        supportingContent = if (details.isEmpty()) null else {
            {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (task.isRecurring) Icon(Icons.Outlined.Repeat, contentDescription = null, modifier = Modifier.size(16.dp))
                    Text(details.joinToString(" · "))
                }
            }
        },
    )
}

/** Ej.: "Cada día", "Cada 2 semanas". */
@Composable
fun recurrenceLabel(recurrence: Recurrence): String {
    val res = when (recurrence.unit) {
        RepeatUnit.DAYS -> R.plurals.every_n_days
        RepeatUnit.WEEKS -> R.plurals.every_n_weeks
        RepeatUnit.MONTHS -> R.plurals.every_n_months
    }
    return pluralStringResource(res, recurrence.interval, recurrence.interval)
}
