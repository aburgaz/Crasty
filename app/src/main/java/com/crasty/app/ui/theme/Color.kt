package com.crasty.app.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// Paleta propia de tonos cálidos y apagados (terracota, como el cangrejo del logo).
// Los "surfaceContainer" son más claros que el fondo en ambos temas: los usan las tarjetas y diálogos.

val LightColors = lightColorScheme(
    primary = Color(0xFFB0573F),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFF5D8CD),
    onPrimaryContainer = Color(0xFF3F150A),
    secondary = Color(0xFF6E5D55),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFE8DDD5),
    onSecondaryContainer = Color(0xFF261A14),
    tertiary = Color(0xFF5B6E52),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFDCE8D2),
    onTertiaryContainer = Color(0xFF192514),
    background = Color(0xFFEDEAE5),
    onBackground = Color(0xFF1F1B19),
    surface = Color(0xFFEDEAE5),
    onSurface = Color(0xFF1F1B19),
    surfaceVariant = Color(0xFFE2DCD6),
    onSurfaceVariant = Color(0xFF4F4540),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF2EFEB),
    surfaceContainer = Color(0xFFF5F3F0),
    surfaceContainerHigh = Color(0xFFF8F7F5),
    surfaceContainerHighest = Color(0xFFFBFAF9),
    outline = Color(0xFF857A74),
    outlineVariant = Color(0xFFD5CCC6),
)

val DarkColors = darkColorScheme(
    primary = Color(0xFFF2A58D),
    onPrimary = Color(0xFF5A1F0F),
    primaryContainer = Color(0xFF7A3826),
    onPrimaryContainer = Color(0xFFFFDBD0),
    secondary = Color(0xFFD8C3B9),
    onSecondary = Color(0xFF3C2E27),
    secondaryContainer = Color(0xFF54443C),
    onSecondaryContainer = Color(0xFFF5DED4),
    tertiary = Color(0xFFBFD0B4),
    onTertiary = Color(0xFF2B3A24),
    tertiaryContainer = Color(0xFF41523A),
    onTertiaryContainer = Color(0xFFDCECD0),
    background = Color(0xFF1A1817),
    onBackground = Color(0xFFEBE3DF),
    surface = Color(0xFF1A1817),
    onSurface = Color(0xFFEBE3DF),
    surfaceVariant = Color(0xFF3A3431),
    onSurfaceVariant = Color(0xFFD3C6C0),
    surfaceContainerLowest = Color(0xFF151312),
    surfaceContainerLow = Color(0xFF211F1D),
    surfaceContainer = Color(0xFF262322),
    surfaceContainerHigh = Color(0xFF2D2A28),
    surfaceContainerHighest = Color(0xFF353130),
    outline = Color(0xFF9C8F89),
    outlineVariant = Color(0xFF4F4540),
)
