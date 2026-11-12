package com.vzpyr.superdock

import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight

@OptIn(ExperimentalTextApi::class)
val GoogleSansFlex = FontFamily(
    Font(
        R.font.google_sans_flex,
        weight = FontWeight.Normal,
        variationSettings = FontVariation.Settings(
            FontVariation.Setting("ROND", 100f),
            FontVariation.weight(FontWeight.Normal.weight),
        ),
    ),
    Font(
        R.font.google_sans_flex,
        weight = FontWeight.Medium,
        variationSettings = FontVariation.Settings(
            FontVariation.Setting("ROND", 100f),
            FontVariation.weight(FontWeight.Medium.weight),
        ),
    ),
    Font(
        R.font.google_sans_flex,
        weight = FontWeight.SemiBold,
        variationSettings = FontVariation.Settings(
            FontVariation.Setting("ROND", 100f),
            FontVariation.weight(FontWeight.SemiBold.weight),
        ),
    ),
)

val SuperdockTypography = Typography()
    .withFamily(GoogleSansFlex)

private fun Typography.withFamily(family: FontFamily): Typography = copy(
    displayLarge = displayLarge.copy(fontFamily = family),
    displayMedium = displayMedium.copy(fontFamily = family),
    displaySmall = displaySmall.copy(fontFamily = family),
    headlineLarge = headlineLarge.copy(fontFamily = family),
    headlineMedium = headlineMedium.copy(fontFamily = family),
    headlineSmall = headlineSmall.copy(fontFamily = family),
    titleLarge = titleLarge.copy(fontFamily = family),
    titleMedium = titleMedium.copy(fontFamily = family),
    titleSmall = titleSmall.copy(fontFamily = family),
    bodyLarge = bodyLarge.copy(fontFamily = family),
    bodyMedium = bodyMedium.copy(fontFamily = family),
    bodySmall = bodySmall.copy(fontFamily = family),
    labelLarge = labelLarge.copy(fontFamily = family),
    labelMedium = labelMedium.copy(fontFamily = family),
    labelSmall = labelSmall.copy(fontFamily = family),
)
