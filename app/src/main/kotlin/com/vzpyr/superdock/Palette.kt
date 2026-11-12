package com.vzpyr.superdock

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

enum class PaletteKey(val labelRes: Int) {
    Surface(R.string.palette_surface),
    SurfaceDim(R.string.palette_surface_dim),
    SurfaceBright(R.string.palette_surface_bright),
    SurfaceContainerLowest(R.string.palette_surface_container_lowest),
    SurfaceContainerLow(R.string.palette_surface_container_low),
    SurfaceContainer(R.string.palette_surface_container),
    SurfaceContainerHigh(R.string.palette_surface_container_high),
    SurfaceContainerHighest(R.string.palette_surface_container_highest),
    Primary(R.string.palette_primary),
    PrimaryContainer(R.string.palette_primary_container),
    Secondary(R.string.palette_secondary),
    SecondaryContainer(R.string.palette_secondary_container),
    Tertiary(R.string.palette_tertiary),
    TertiaryContainer(R.string.palette_tertiary_container),
    InverseSurface(R.string.palette_inverse_surface),
}

fun ColorScheme.role(key: PaletteKey): Color = when (key) {
    PaletteKey.Surface -> surface
    PaletteKey.SurfaceDim -> surfaceDim
    PaletteKey.SurfaceBright -> surfaceBright
    PaletteKey.SurfaceContainerLowest -> surfaceContainerLowest
    PaletteKey.SurfaceContainerLow -> surfaceContainerLow
    PaletteKey.SurfaceContainer -> surfaceContainer
    PaletteKey.SurfaceContainerHigh -> surfaceContainerHigh
    PaletteKey.SurfaceContainerHighest -> surfaceContainerHighest
    PaletteKey.Primary -> primary
    PaletteKey.PrimaryContainer -> primaryContainer
    PaletteKey.Secondary -> secondary
    PaletteKey.SecondaryContainer -> secondaryContainer
    PaletteKey.Tertiary -> tertiary
    PaletteKey.TertiaryContainer -> tertiaryContainer
    PaletteKey.InverseSurface -> inverseSurface
}

fun ColorScheme.onRole(key: PaletteKey): Color = when (key) {
    PaletteKey.Surface,
    PaletteKey.SurfaceDim,
    PaletteKey.SurfaceBright,
    PaletteKey.SurfaceContainerLowest,
    PaletteKey.SurfaceContainerLow,
    PaletteKey.SurfaceContainer,
    PaletteKey.SurfaceContainerHigh,
    PaletteKey.SurfaceContainerHighest -> onSurface

    PaletteKey.Primary -> onPrimary
    PaletteKey.PrimaryContainer -> onPrimaryContainer
    PaletteKey.Secondary -> onSecondary
    PaletteKey.SecondaryContainer -> onSecondaryContainer
    PaletteKey.Tertiary -> onTertiary
    PaletteKey.TertiaryContainer -> onTertiaryContainer
    PaletteKey.InverseSurface -> inverseOnSurface
}

fun ColorSelection.background(scheme: ColorScheme): Color = when (mode) {
    ColorMode.MaterialYou -> scheme.role(paletteKey)
    ColorMode.Custom -> Color(custom.withAlpha(alpha))
}

fun ColorSelection.foreground(scheme: ColorScheme): Color = when (mode) {
    ColorMode.MaterialYou -> scheme.onRole(paletteKey)
    ColorMode.Custom -> custom.contrastColor()
}

fun ColorSelection.iconColor(scheme: ColorScheme): Color = when (mode) {
    ColorMode.MaterialYou -> scheme.onRole(paletteKey)
    ColorMode.Custom -> Color(custom.withAlpha(alpha))
}

fun Int.withAlpha(alpha: Int): Int =
    (alpha.coerceIn(0, 255) shl 24) or (this and 0x00FFFFFF)

private fun Int.contrastColor(): Color =
    if (Color(this).luminance() > 0.5f) Color(0xFF1B1B1F) else Color(0xFFE3E3E3)

fun colorScheme(context: Context, dark: Boolean): ColorScheme {
    val dynamic = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    return when {
        dynamic && dark -> dynamicDarkColorScheme(context)
        dynamic -> dynamicLightColorScheme(context)
        dark -> darkColorScheme()
        else -> lightColorScheme()
    }
}

fun Context.isNight(): Boolean =
    resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK ==
        Configuration.UI_MODE_NIGHT_YES
