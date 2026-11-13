package com.vzpyr.superdock

import android.content.ComponentName
import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource

data class TargetName(val app: String, val detail: String?)

@Composable
fun SlotRow(slot: Slot, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val scheme = MaterialTheme.colorScheme
    val name = slot.target?.toComponentName()?.let { targetName(context, it) }
    val detail = name?.detail

    ListItem(
        headlineContent = {
            Text(name?.app ?: stringResource(R.string.slot_not_bound))
        },
        supportingContent = if (detail != null) {
            { Text(detail) }
        } else {
            null
        },
        leadingContent = {
            Icon(
                painter = painterResource(slot.icon.drawable),
                contentDescription = null,
                tint = slot.colors.color(scheme),
            )
        },
        trailingContent = { Chevron() },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        modifier = modifier.clickable(onClick = onClick),
    )
}

@Composable
fun ActionRow(target: String?, onClick: () -> Unit, modifier: Modifier = Modifier) {
    ListItem(
        headlineContent = { Text(stringResource(R.string.label_action)) },
        supportingContent = {
            Text(target ?: stringResource(R.string.slot_not_bound))
        },
        trailingContent = { Chevron() },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        modifier = modifier.clickable(onClick = onClick),
    )
}

@Composable
private fun Chevron() {
    Icon(
        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
        contentDescription = null,
    )
}

fun targetName(context: Context, component: ComponentName): TargetName {
    val app = runCatching {
        val pm = context.packageManager
        pm.getApplicationLabel(pm.getApplicationInfo(component.packageName, 0)).toString()
    }.getOrNull() ?: return TargetName(component.packageName, null)

    val activity = component.className.substringAfterLast('.')
    return TargetName(app, activity.takeIf { it.isNotEmpty() && it != app })
}

fun componentLabel(context: Context, component: ComponentName): String {
    val name = targetName(context, component)
    return name.detail?.let { context.getString(R.string.component_label, name.app, it) }
        ?: name.app
}
