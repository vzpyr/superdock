package com.vzpyr.superdock

import android.content.ComponentName
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TargetSheet(onDismiss: () -> Unit, onPick: (ComponentName) -> Unit) {
    val context = LocalContext.current
    var query by remember { mutableStateOf("") }
    val targets by produceState<List<Target>?>(initialValue = null, key1 = context) {
        value = withContext(Dispatchers.IO) { findTargets(context) }
    }
    val filtered = remember(targets, query) {
        targets.orEmpty().filter { target ->
            query.isBlank() ||
                target.label.contains(query, ignoreCase = true) ||
                target.detail.orEmpty().contains(query, ignoreCase = true)
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
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
                    items(filtered, key = { it.component.flattenToString() }) { target ->
                        ListItem(
                            headlineContent = { Text(target.label) },
                            supportingContent = target.detail?.let { { Text(it) } },
                            modifier = Modifier.clickable { onPick(target.component) },
                        )
                    }
                }
            }
        }
    }
}
