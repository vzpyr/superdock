package com.vzpyr.superdock

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColorModeRow(
    mode: ColorMode,
    onChange: (ColorMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    val options = listOf(
        ColorMode.MaterialYou to stringResource(R.string.color_material_you),
        ColorMode.Custom to stringResource(R.string.color_custom),
    )

    SingleChoiceSegmentedButtonRow(modifier = modifier.fillMaxWidth()) {
        options.forEachIndexed { index, (value, label) ->
            SegmentedButton(
                selected = mode == value,
                onClick = { onChange(value) },
                shape = SegmentedButtonDefaults.itemShape(index, options.size),
            ) {
                Text(label)
            }
        }
    }
}
