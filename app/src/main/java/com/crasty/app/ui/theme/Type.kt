package com.crasty.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import com.crasty.app.R

// Nunito (fuente variable, licencia OFL: third_party/Nunito-OFL.txt). Va dentro de la app: no necesita internet.
private fun nunito(weight: FontWeight) = Font(
    resId = R.font.nunito,
    weight = weight,
    variationSettings = FontVariation.Settings(FontVariation.weight(weight.weight)),
)

val Nunito = FontFamily(
    nunito(FontWeight.Normal),
    nunito(FontWeight.Medium),
    nunito(FontWeight.SemiBold),
    nunito(FontWeight.Bold),
)

private fun TextStyle.nunito(weight: FontWeight? = null) =
    copy(fontFamily = Nunito, fontWeight = weight ?: fontWeight)

private val base = Typography()

val CrastyTypography = Typography(
    displayLarge = base.displayLarge.nunito(),
    displayMedium = base.displayMedium.nunito(),
    displaySmall = base.displaySmall.nunito(),
    headlineLarge = base.headlineLarge.nunito(FontWeight.SemiBold),
    headlineMedium = base.headlineMedium.nunito(FontWeight.SemiBold),
    headlineSmall = base.headlineSmall.nunito(FontWeight.SemiBold),
    titleLarge = base.titleLarge.nunito(FontWeight.Bold),
    titleMedium = base.titleMedium.nunito(FontWeight.SemiBold),
    titleSmall = base.titleSmall.nunito(FontWeight.Bold),
    bodyLarge = base.bodyLarge.nunito(FontWeight.Medium),
    bodyMedium = base.bodyMedium.nunito(),
    bodySmall = base.bodySmall.nunito(),
    labelLarge = base.labelLarge.nunito(FontWeight.Bold),
    labelMedium = base.labelMedium.nunito(FontWeight.SemiBold),
    labelSmall = base.labelSmall.nunito(FontWeight.SemiBold),
)
