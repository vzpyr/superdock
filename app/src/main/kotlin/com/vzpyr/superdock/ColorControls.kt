package com.vzpyr.superdock

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ColorControls(
    colors: ColorSelection,
    onChange: (ColorSelection) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        ColorModeRow(
            mode = colors.mode,
            onChange = { onChange(colors.copy(mode = it)) },
        )
        when (colors.mode) {
            ColorMode.MaterialYou -> PaletteDropdown(
                selected = colors.paletteKey,
                onPick = { onChange(colors.copy(paletteKey = it)) },
            )

            ColorMode.Custom -> ColorPicker(
                color = colors.custom,
                alpha = colors.alpha,
                onColorChange = { onChange(colors.copy(custom = it)) },
                onAlphaChange = { onChange(colors.copy(alpha = it)) },
            )
        }
    }
}
