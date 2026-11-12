package com.vzpyr.superdock

import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector

@Composable
fun IconAction(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
) {
    IconTooltip(label) {
        IconButton(onClick = onClick, enabled = enabled) {
            Icon(icon, contentDescription = label)
        }
    }
}
