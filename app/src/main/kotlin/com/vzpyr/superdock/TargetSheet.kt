package com.vzpyr.superdock

import android.content.ComponentName
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private data class TargetApp(val label: String, val activities: List<Target>)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TargetSheet(onDismiss: () -> Unit, onPick: (ComponentName) -> Unit) {
    val context = LocalContext.current
    var query by remember { mutableStateOf("") }
    var opened by remember { mutableStateOf<TargetApp?>(null) }
    val targets by produceState<List<Target>?>(initialValue = null, key1 = context) {
        value = withContext(Dispatchers.IO) { findTargets(context) }
    }
    val apps = remember(targets) {
        targets.orEmpty()
            .groupBy { it.component.packageName }
            .map { (_, activities) -> TargetApp(activities.first().label, activities) }
    }
    val filtered = remember(apps, query) {
        apps.filter { app ->
            query.isBlank() ||
                app.label.contains(query, ignoreCase = true) ||
                app.activities.any { it.detail.orEmpty().contains(query, ignoreCase = true) }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            val selected = opened
            if (selected == null) {
                SheetTitle(stringResource(R.string.sheet_bind_to))
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text(stringResource(R.string.placeholder_search)) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                )
                if (targets == null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator()
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false),
                    ) {
                        items(
                            items = filtered,
                            key = { it.activities.first().component.packageName },
                        ) { app ->
                            ListItem(
                                headlineContent = { Text(app.label) },
                                supportingContent = if (app.activities.size > 1) {
                                    {
                                        Text(
                                            pluralStringResource(
                                                R.plurals.target_activities,
                                                app.activities.size,
                                                app.activities.size,
                                            ),
                                        )
                                    }
                                } else {
                                    null
                                },
                                modifier = Modifier.clickable {
                                    if (app.activities.size == 1) {
                                        onPick(app.activities.first().component)
                                    } else {
                                        opened = app
                                    }
                                },
                                colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                            )
                        }
                    }
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconAction(
                        icon = Icons.AutoMirrored.Filled.ArrowBack,
                        label = stringResource(R.string.action_back),
                        onClick = { opened = null },
                    )
                    Text(
                        text = selected.label,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                    )
                }
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false),
                ) {
                    items(
                        items = selected.activities,
                        key = { it.component.flattenToString() },
                    ) { target ->
                        ListItem(
                            headlineContent = { Text(target.detail ?: selected.label) },
                            modifier = Modifier.clickable { onPick(target.component) },
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                        )
                    }
                }
            }
        }
    }
}
