package com.vzpyr.superdock

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconToggleButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SlotSheet(
    slot: Slot,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onDismiss: () -> Unit,
    onChange: (Slot) -> Unit,
    onBind: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onRemove: () -> Unit,
) {
    val context = LocalContext.current

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
            SheetTitle(stringResource(R.string.sheet_button))
            SheetTitle(stringResource(R.string.sheet_icon))
            IconPicker(
                selected = slot.icon,
                onSelect = { onChange(slot.copy(icon = it)) },
            )
            SheetTitle(stringResource(R.string.sheet_color))
            ColorControls(
                colors = slot.colors,
                onChange = { onChange(slot.copy(colors = it)) },
                inlinePicker = true,
            )
            HorizontalDivider(modifier = Modifier.padding(top = 8.dp))
            ActionRow(
                target = slot.target?.toComponentName()?.let { componentLabel(context, it) },
                onClick = onBind,
            )
            if (canMoveUp) {
                MenuRow(
                    icon = Icons.Default.KeyboardArrowUp,
                    label = stringResource(R.string.action_move_up),
                    onClick = onMoveUp,
                )
            }
            if (canMoveDown) {
                MenuRow(
                    icon = Icons.Default.KeyboardArrowDown,
                    label = stringResource(R.string.action_move_down),
                    onClick = onMoveDown,
                )
            }
            MenuRow(
                icon = Icons.Default.Close,
                label = stringResource(R.string.action_remove),
                onClick = onRemove,
                destructive = true,
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

@Composable
private fun MenuRow(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    destructive: Boolean = false,
) {
    ListItem(
        headlineContent = { Text(label) },
        leadingContent = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (destructive) {
                    MaterialTheme.colorScheme.error
                } else {
                    LocalContentColor.current
                },
            )
        },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        modifier = Modifier.clickable(onClick = onClick),
    )
}
