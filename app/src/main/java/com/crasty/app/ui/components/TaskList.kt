package com.crasty.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.crasty.app.R
import com.crasty.app.domain.model.Recurrence
import com.crasty.app.domain.model.RepeatUnit
import com.crasty.app.domain.model.Task
import com.crasty.app.ui.theme.CardShape
import com.crasty.app.ui.theme.taskCardColor

private const val FADE_OUT_MILLIS = 400

/**
 * Lista de tareas. Si se pasa [onSwipeRight]/[onSwipeLeft], las tareas normales pendientes
 * se pueden arrastrar en esa dirección (las recurrentes nunca).
 *
 * Con [fadeOutOnComplete], al marcar una tarea pendiente se ve marcada y se desvanece,
 * y solo entonces se llama a [onToggleCompleted] (para listas de las que desaparece al completarse).
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
    fadeOutOnComplete: Boolean = false,
    emptyText: String = stringResource(R.string.empty_day),
) {
    if (tasks.isEmpty()) {
        Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(emptyText, style = MaterialTheme.typography.bodyLarge)
        }
        return
    }
    // Hueco al final para que el botón flotante no tape la última tarea.
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 88.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(tasks, key = { it.id }) { task ->
            var fading by remember { mutableStateOf(false) }
            var committed by remember { mutableStateOf(false) }
            val commit = {
                if (!committed) {
                    committed = true
                    onToggleCompleted(task)
                }
            }
            val alpha by animateFloatAsState(
                targetValue = if (fading) 0f else 1f,
                animationSpec = tween(FADE_OUT_MILLIS),
                finishedListener = { if (fading) commit() },
                label = "fadeOut",
            )
            // Si la fila desaparece a mitad del desvanecimiento (p. ej. al cambiar de pestaña), se completa igualmente.
            DisposableEffect(Unit) {
                onDispose { if (fading) commit() }
            }

            // animateItem: las demás tareas se recolocan suavemente cuando una sale o entra de la lista.
            Box(Modifier.animateItem(fadeOutSpec = null).alpha(alpha)) {
                SwipeableCard(
                    startToEnd = onSwipeRight?.takeIf { task.isMovable && !fading }?.let { onSwipe ->
                        SwipeAction(stringResource(R.string.tab_tomorrow), Icons.AutoMirrored.Filled.ArrowForward) { onSwipe(task) }
                    },
                    endToStart = onSwipeLeft?.takeIf { task.isMovable && !fading }?.let { onSwipe ->
                        SwipeAction(stringResource(R.string.tab_today), Icons.AutoMirrored.Filled.ArrowBack) { onSwipe(task) }
                    },
                ) {
                    TaskRow(
                        task = if (fading) task.copy(isCompleted = true) else task,
                        onToggleCompleted = {
                            when {
                                fading -> Unit
                                fadeOutOnComplete && !task.isCompleted -> fading = true
                                else -> onToggleCompleted(task)
                            }
                        },
                        onClick = { onClick(task) },
                        showDate = showDate,
                    )
                }
            }
        }
    }
}

/** Tarjeta de una tarea: gris si es normal, del color de su proyecto o beige si es recurrente. */
@Composable
fun TaskRow(
    task: Task,
    onToggleCompleted: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    showDate: Boolean = false,
) {
    val details = listOfNotNull(
        task.date.format(mediumDateFormatter).takeIf { showDate },
        task.reminderTime?.toString(),
        task.recurrence?.let { recurrenceLabel(it) },
    )
    Surface(
        onClick = onClick,
        shape = CardShape,
        color = taskCardColor(task),
        modifier = modifier
            .fillMaxWidth()
            .alpha(if (task.isCompleted) 0.6f else 1f),
    ) {
        TaskRowContent(task, details, onToggleCompleted)
    }
}

@Composable
private fun TaskRowContent(task: Task, details: List<String>, onToggleCompleted: () -> Unit) {
    ListItem(
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
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
