package com.crasty.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.crasty.app.ui.theme.CardShape
import kotlinx.coroutines.launch

/** Acción al deslizar una tarjeta: lo que se ve debajo mientras se arrastra y qué hace al soltar. */
class SwipeAction(
    val label: String,
    val icon: ImageVector,
    val onSwipe: () -> Unit,
)

/**
 * Tarjeta que se puede deslizar a la derecha ([startToEnd]) y/o a la izquierda ([endToStart]).
 * Con ambas a null es una tarjeta normal.
 *
 * Ojo: si el elemento pasa a otra lista con la misma clave (p. ej. otra pestaña), esa lista debe
 * componerse aparte (`key(tab)`), o reutilizaría el estado de deslizamiento a medias y Compose fallaría.
 */
@Composable
fun SwipeableCard(
    startToEnd: SwipeAction?,
    endToStart: SwipeAction?,
    content: @Composable () -> Unit,
) {
    if (startToEnd == null && endToStart == null) {
        content()
        return
    }
    // Estado nuevo si cambian las direcciones permitidas: uno antiguo podría quedarse en una posición que ya no existe.
    key(startToEnd != null, endToStart != null) {
        SwipeToDismissCard(startToEnd, endToStart, content)
    }
}

@Composable
private fun SwipeToDismissCard(
    startToEnd: SwipeAction?,
    endToStart: SwipeAction?,
    content: @Composable () -> Unit,
) {
    val state = rememberSwipeToDismissBoxState()
    val scope = rememberCoroutineScope()
    SwipeToDismissBox(
        state = state,
        modifier = Modifier.clip(CardShape),
        enableDismissFromStartToEnd = startToEnd != null,
        enableDismissFromEndToStart = endToStart != null,
        onDismiss = { direction ->
            when (direction) {
                SwipeToDismissBoxValue.StartToEnd -> startToEnd?.onSwipe?.invoke()
                SwipeToDismissBoxValue.EndToStart -> endToStart?.onSwipe?.invoke()
                SwipeToDismissBoxValue.Settled -> Unit
            }
            // Si el elemento cambia de lista, desaparece; si la acción no tuvo efecto, vuelve a su sitio.
            scope.launch { state.reset() }
        },
        backgroundContent = {
            when (state.dismissDirection) {
                SwipeToDismissBoxValue.StartToEnd -> startToEnd?.let { SwipeBackground(it, Alignment.CenterStart) }
                SwipeToDismissBoxValue.EndToStart -> endToStart?.let { SwipeBackground(it, Alignment.CenterEnd) }
                SwipeToDismissBoxValue.Settled -> Unit
            }
        },
    ) {
        content()
    }
}

@Composable
private fun SwipeBackground(action: SwipeAction, alignment: Alignment) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .padding(horizontal = 24.dp),
        contentAlignment = alignment,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(action.icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer)
            Text(action.label, color = MaterialTheme.colorScheme.onSecondaryContainer)
        }
    }
}
