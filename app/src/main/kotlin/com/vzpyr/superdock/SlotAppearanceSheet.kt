package com.vzpyr.superdock

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconToggleButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SlotAppearanceSheet(
    slot: Slot,
    onDismiss: () -> Unit,
    onChange: (Slot) -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 32.dp),
        ) {
            SheetTitle(stringResource(R.string.sheet_icon))
            IconPicker(
                selected = slot.icon,
                onSelect = { onChange(slot.copy(icon = it)) },
            )
            SheetTitle(stringResource(R.string.sheet_color))
            ColorControls(
                colors = slot.colors,
                onChange = { onChange(slot.copy(colors = it)) },
            )
        }
    }
}

@Composable
private fun IconPicker(selected: IconKey, onSelect: (IconKey) -> Unit) {
    Row(
        modifier = Modifier.padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        IconKey.entries.forEach { key ->
            IconTooltip(stringResource(key.label)) {
                FilledIconToggleButton(
                    checked = key == selected,
                    onCheckedChange = { onSelect(key) },
                ) {
                    Icon(
                        painter = painterResource(key.drawable),
                        contentDescription = stringResource(key.label),
                    )
                }
            }
        }
    }
}
