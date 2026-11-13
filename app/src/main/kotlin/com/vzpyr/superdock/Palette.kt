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

enum class PaletteKey(val labelRes: Int) {
    Primary(R.string.palette_primary),
    OnPrimary(R.string.palette_on_primary),
    PrimaryContainer(R.string.palette_primary_container),
    OnPrimaryContainer(R.string.palette_on_primary_container),
    InversePrimary(R.string.palette_inverse_primary),
    Secondary(R.string.palette_secondary),
    OnSecondary(R.string.palette_on_secondary),
    SecondaryContainer(R.string.palette_secondary_container),
    OnSecondaryContainer(R.string.palette_on_secondary_container),
    Tertiary(R.string.palette_tertiary),
    OnTertiary(R.string.palette_on_tertiary),
    TertiaryContainer(R.string.palette_tertiary_container),
    OnTertiaryContainer(R.string.palette_on_tertiary_container),
    Background(R.string.palette_background),
    OnBackground(R.string.palette_on_background),
    Surface(R.string.palette_surface),
    OnSurface(R.string.palette_on_surface),
    SurfaceVariant(R.string.palette_surface_variant),
    OnSurfaceVariant(R.string.palette_on_surface_variant),
    SurfaceTint(R.string.palette_surface_tint),
    InverseSurface(R.string.palette_inverse_surface),
    InverseOnSurface(R.string.palette_inverse_on_surface),
    Error(R.string.palette_error),
    OnError(R.string.palette_on_error),
    ErrorContainer(R.string.palette_error_container),
    OnErrorContainer(R.string.palette_on_error_container),
    Outline(R.string.palette_outline),
    OutlineVariant(R.string.palette_outline_variant),
    SurfaceBright(R.string.palette_surface_bright),
    SurfaceDim(R.string.palette_surface_dim),
    SurfaceContainer(R.string.palette_surface_container),
    SurfaceContainerHigh(R.string.palette_surface_container_high),
    SurfaceContainerHighest(R.string.palette_surface_container_highest),
    SurfaceContainerLow(R.string.palette_surface_container_low),
    SurfaceContainerLowest(R.string.palette_surface_container_lowest),
    PrimaryFixed(R.string.palette_primary_fixed),
    PrimaryFixedDim(R.string.palette_primary_fixed_dim),
    OnPrimaryFixed(R.string.palette_on_primary_fixed),
    OnPrimaryFixedVariant(R.string.palette_on_primary_fixed_variant),
    SecondaryFixed(R.string.palette_secondary_fixed),
    SecondaryFixedDim(R.string.palette_secondary_fixed_dim),
    OnSecondaryFixed(R.string.palette_on_secondary_fixed),
    OnSecondaryFixedVariant(R.string.palette_on_secondary_fixed_variant),
    TertiaryFixed(R.string.palette_tertiary_fixed),
    TertiaryFixedDim(R.string.palette_tertiary_fixed_dim),
    OnTertiaryFixed(R.string.palette_on_tertiary_fixed),
    OnTertiaryFixedVariant(R.string.palette_on_tertiary_fixed_variant),
}

fun ColorScheme.role(key: PaletteKey): Color = when (key) {
    PaletteKey.Primary -> primary
    PaletteKey.OnPrimary -> onPrimary
    PaletteKey.PrimaryContainer -> primaryContainer
    PaletteKey.OnPrimaryContainer -> onPrimaryContainer
    PaletteKey.InversePrimary -> inversePrimary
    PaletteKey.Secondary -> secondary
    PaletteKey.OnSecondary -> onSecondary
    PaletteKey.SecondaryContainer -> secondaryContainer
    PaletteKey.OnSecondaryContainer -> onSecondaryContainer
    PaletteKey.Tertiary -> tertiary
    PaletteKey.OnTertiary -> onTertiary
    PaletteKey.TertiaryContainer -> tertiaryContainer
    PaletteKey.OnTertiaryContainer -> onTertiaryContainer
    PaletteKey.Background -> background
    PaletteKey.OnBackground -> onBackground
    PaletteKey.Surface -> surface
    PaletteKey.OnSurface -> onSurface
    PaletteKey.SurfaceVariant -> surfaceVariant
    PaletteKey.OnSurfaceVariant -> onSurfaceVariant
    PaletteKey.SurfaceTint -> surfaceTint
    PaletteKey.InverseSurface -> inverseSurface
    PaletteKey.InverseOnSurface -> inverseOnSurface
    PaletteKey.Error -> error
    PaletteKey.OnError -> onError
    PaletteKey.ErrorContainer -> errorContainer
    PaletteKey.OnErrorContainer -> onErrorContainer
    PaletteKey.Outline -> outline
    PaletteKey.OutlineVariant -> outlineVariant
    PaletteKey.SurfaceBright -> surfaceBright
    PaletteKey.SurfaceDim -> surfaceDim
    PaletteKey.SurfaceContainer -> surfaceContainer
    PaletteKey.SurfaceContainerHigh -> surfaceContainerHigh
    PaletteKey.SurfaceContainerHighest -> surfaceContainerHighest
    PaletteKey.SurfaceContainerLow -> surfaceContainerLow
    PaletteKey.SurfaceContainerLowest -> surfaceContainerLowest
    PaletteKey.PrimaryFixed -> primaryFixed
    PaletteKey.PrimaryFixedDim -> primaryFixedDim
    PaletteKey.OnPrimaryFixed -> onPrimaryFixed
    PaletteKey.OnPrimaryFixedVariant -> onPrimaryFixedVariant
    PaletteKey.SecondaryFixed -> secondaryFixed
    PaletteKey.SecondaryFixedDim -> secondaryFixedDim
    PaletteKey.OnSecondaryFixed -> onSecondaryFixed
    PaletteKey.OnSecondaryFixedVariant -> onSecondaryFixedVariant
    PaletteKey.TertiaryFixed -> tertiaryFixed
    PaletteKey.TertiaryFixedDim -> tertiaryFixedDim
    PaletteKey.OnTertiaryFixed -> onTertiaryFixed
    PaletteKey.OnTertiaryFixedVariant -> onTertiaryFixedVariant
}

fun ColorScheme.onRole(key: PaletteKey): Color = when (key) {
    PaletteKey.Primary -> onPrimary
    PaletteKey.OnPrimary -> primary
    PaletteKey.PrimaryContainer -> onPrimaryContainer
    PaletteKey.OnPrimaryContainer -> primaryContainer
    PaletteKey.InversePrimary -> inverseSurface
    PaletteKey.Secondary -> onSecondary
    PaletteKey.OnSecondary -> secondary
    PaletteKey.SecondaryContainer -> onSecondaryContainer
    PaletteKey.OnSecondaryContainer -> secondaryContainer
    PaletteKey.Tertiary -> onTertiary
    PaletteKey.OnTertiary -> tertiary
    PaletteKey.TertiaryContainer -> onTertiaryContainer
    PaletteKey.OnTertiaryContainer -> tertiaryContainer
    PaletteKey.Background -> onBackground
    PaletteKey.OnBackground -> background
    PaletteKey.Surface,
    PaletteKey.SurfaceDim,
    PaletteKey.SurfaceBright,
    PaletteKey.SurfaceContainerLowest,
    PaletteKey.SurfaceContainerLow,
    PaletteKey.SurfaceContainer,
    PaletteKey.SurfaceContainerHigh,
    PaletteKey.SurfaceContainerHighest -> onSurface

    PaletteKey.OnSurface -> surface
    PaletteKey.SurfaceVariant -> onSurfaceVariant
    PaletteKey.OnSurfaceVariant -> surfaceVariant
    PaletteKey.SurfaceTint -> onPrimary
    PaletteKey.InverseSurface -> inverseOnSurface
    PaletteKey.InverseOnSurface -> inverseSurface
    PaletteKey.Error -> onError
    PaletteKey.OnError -> error
    PaletteKey.ErrorContainer -> onErrorContainer
    PaletteKey.OnErrorContainer -> errorContainer
    PaletteKey.Outline,
    PaletteKey.OutlineVariant -> inverseOnSurface

    PaletteKey.PrimaryFixed -> onPrimaryFixed
    PaletteKey.PrimaryFixedDim -> onPrimaryFixedVariant
    PaletteKey.OnPrimaryFixed -> primaryFixed
    PaletteKey.OnPrimaryFixedVariant -> primaryFixedDim
    PaletteKey.SecondaryFixed -> onSecondaryFixed
    PaletteKey.SecondaryFixedDim -> onSecondaryFixedVariant
    PaletteKey.OnSecondaryFixed -> secondaryFixed
    PaletteKey.OnSecondaryFixedVariant -> secondaryFixedDim
    PaletteKey.TertiaryFixed -> onTertiaryFixed
    PaletteKey.TertiaryFixedDim -> onTertiaryFixedVariant
    PaletteKey.OnTertiaryFixed -> tertiaryFixed
    PaletteKey.OnTertiaryFixedVariant -> tertiaryFixedDim
}

fun ColorSelection.color(scheme: ColorScheme): Color = when (mode) {
    ColorMode.MaterialYou -> scheme.role(paletteKey)
    ColorMode.Custom -> Color(custom.withAlpha(alpha))
}

fun Int.withAlpha(alpha: Int): Int =
    (alpha.coerceIn(0, 255) shl 24) or (this and 0x00FFFFFF)

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
