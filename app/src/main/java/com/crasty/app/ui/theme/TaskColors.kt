package com.crasty.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp
import com.crasty.app.domain.model.Task

/** Color de tarjeta con su versión para tema claro y oscuro. */
data class CardColor(val light: Color, val dark: Color) {
    @Composable
    fun resolve(): Color = if (MaterialTheme.colorScheme.background.luminance() < 0.5f) dark else light
}

/** Colores suaves (poco saturados) para no cansar la vista. */
object TaskColors {
    /** Tareas sin proyecto: gris claro, algo más claro que el fondo. */
    val plain = CardColor(Color(0xFFF8F7F5), Color(0xFF2B2927))

    /** Tareas recurrentes. */
    val recurring = CardColor(Color(0xFFF1E6D2), Color(0xFF423C2E))

    /** Paleta a elegir para cada proyecto; el proyecto guarda la posición en esta lista. */
    val projects = listOf(
        CardColor(Color(0xFFD7E4CF), Color(0xFF34402F)), // salvia
        CardColor(Color(0xFFCFDDEC), Color(0xFF2F3B49)), // azul cielo
        CardColor(Color(0xFFDDD5EE), Color(0xFF3C364A)), // lavanda
        CardColor(Color(0xFFF0D3D9), Color(0xFF4A3439)), // rosa
        CardColor(Color(0xFFF4D8C4), Color(0xFF50362A)), // albaricoque
        CardColor(Color(0xFFCBE4DF), Color(0xFF2C4340)), // verde agua
    )

    fun project(colorIndex: Int): CardColor = projects[colorIndex.mod(projects.size)]
}

val CardShape = RoundedCornerShape(16.dp)

/** Color de cada proyecto (id → posición en la paleta), disponible en toda la UI. */
val LocalProjectColors = compositionLocalOf<Map<Long, Int>> { emptyMap() }

@Composable
fun taskCardColor(task: Task): Color {
    val projectColor = task.projectId?.let { LocalProjectColors.current[it] }
    return when {
        task.isRecurring -> TaskColors.recurring
        projectColor != null -> TaskColors.project(projectColor)
        else -> TaskColors.plain
    }.resolve()
}
