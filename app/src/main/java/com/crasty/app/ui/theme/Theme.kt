package com.crasty.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

/** Tema propio (sin colores dinámicos) para controlar el contraste entre fondo y tarjetas. */
@Composable
fun CrastyTheme(
    darkTheme: Boolean,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = CrastyTypography,
        content = content,
    )
}
