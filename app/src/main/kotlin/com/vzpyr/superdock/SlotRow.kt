package com.vzpyr.superdock

import android.content.ComponentName
import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource

@Composable
fun SlotRow(
    slot: Slot,
    showArrows: Boolean,
    onBind: () -> Unit,
    onRemove: (() -> Unit)?,
    onChange: (Slot) -> Unit,
    onMoveUp: (() -> Unit)?,
    onMoveDown: (() -> Unit)?,
) {
    val context = LocalContext.current
    val scheme = MaterialTheme.colorScheme
    var editing by remember { mutableStateOf(false) }

    ListItem(
        headlineContent = {
            Text(
                slot.target?.toComponentName()?.let { componentLabel(context, it) }
                    ?: stringResource(R.string.slot_not_bound),
            )
        },
        supportingContent = { Text(stringResource(slot.icon.label)) },
        leadingContent = {
            IconTooltip(stringResource(R.string.action_edit_appearance)) {
                IconButton(onClick = { editing = true }) {
                    Icon(
                        painter = painterResource(slot.icon.drawable),
                        contentDescription = stringResource(R.string.action_edit_appearance),
                        tint = slot.colors.iconColor(scheme),
                    )
                }
            }
        },
        trailingContent = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (showArrows) {
                    onMoveUp?.let {
                        IconAction(
                            icon = Icons.Default.KeyboardArrowUp,
                            label = stringResource(R.string.action_move_up),
                            onClick = it,
                        )
                    }
                    onMoveDown?.let {
                        IconAction(
                            icon = Icons.Default.KeyboardArrowDown,
                            label = stringResource(R.string.action_move_down),
                            onClick = it,
                        )
                    }
                }
                onRemove?.let {
                    IconAction(
                        icon = Icons.Default.Close,
                        label = stringResource(R.string.action_remove),
                        onClick = it,
                    )
                }
            }
        },
        modifier = Modifier.clickable(onClick = onBind),
    )

    if (editing) {
        SlotAppearanceSheet(
            slot = slot,
            onDismiss = { editing = false },
            onChange = onChange,
        )
    }
}

@Composable
private fun componentLabel(context: Context, component: ComponentName): String {
    val (app, activity) = runCatching {
        val pm = context.packageManager
        val label = pm.getApplicationLabel(pm.getApplicationInfo(component.packageName, 0)).toString()
        label to component.className.substringAfterLast('.')
    }.getOrNull() ?: return component.packageName

    return if (activity == app || activity.isEmpty()) {
        app
    } else {
        stringResource(R.string.component_label, app, activity)
    }
}
