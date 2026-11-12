package com.vzpyr.superdock

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.glance.appwidget.updateAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(store: ConfigStore) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val (shizuku, refreshShizuku) = rememberShizukuState()

    var config by remember { mutableStateOf(store.load()) }
    var engine by remember { mutableStateOf(readEngine(context)) }
    var target by remember { mutableStateOf<SlotTarget?>(null) }
    val engines by produceState(initialValue = emptyList<Engine>(), key1 = context) {
        value = withContext(Dispatchers.IO) { findEngines(context) }
    }

    val ready = shizuku is ShizukuState.Ready

    fun persist(next: Config) {
        config = next
        store.save(next)
        scope.launch { SearchBox().updateAll(context) }
    }

    target?.let { pending ->
        TargetSheet(
            onDismiss = { target = null },
            onPick = { component ->
                persist(pending.bind(config, component))
                target = null
            },
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
                actions = {
                    IconAction(
                        icon = Icons.Default.Refresh,
                        label = stringResource(R.string.action_refresh_status),
                        onClick = refreshShizuku,
                    )
                    IconAction(
                        icon = Icons.Default.Warning,
                        label = stringResource(R.string.action_restart_launcher),
                        onClick = { scope.launch(Dispatchers.IO) { restartLauncher() } },
                        enabled = ready,
                    )
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 32.dp),
        ) {
            ShizukuStatusRow(shizuku)
            SectionDivider()
            SearchEngineSection(
                engines = engines,
                current = engine,
                shizuku = shizuku,
                onPick = { packageName ->
                    scope.launch {
                        withContext(Dispatchers.IO) { writeEngine(context, packageName) }
                        engine = readEngine(context)
                        SearchBox().updateAll(context)
                    }
                },
            )
            SectionDivider()
            BackgroundSection(
                colors = config.colors,
                onChange = { persist(config.copy(colors = it)) },
            )
            SectionDivider()
            ActionsSection(
                config = config,
                onChange = ::persist,
                onEditTarget = { target = it },
            )
        }
    }
}

@Composable
private fun ShizukuStatusRow(state: ShizukuState) {
    val ready = state is ShizukuState.Ready

    ListItem(
        headlineContent = { Text(stringResource(R.string.shizuku)) },
        supportingContent = { Text(stringResource(state.labelRes)) },
        leadingContent = {
            Icon(
                imageVector = if (ready) Icons.Default.Check else Icons.Default.Warning,
                contentDescription = null,
                tint = if (ready) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.error
                },
            )
        },
        trailingContent = {
            if (state is ShizukuState.PermissionRequired) {
                TextButton(onClick = { requestShizukuPermission() }) {
                    Text(stringResource(R.string.action_grant))
                }
            }
        },
    )
}

@Composable
private fun SearchEngineSection(
    engines: List<Engine>,
    current: String?,
    shizuku: ShizukuState,
    onPick: (String) -> Unit,
) {
    SectionHeader(stringResource(R.string.section_search_engine))
    shizuku.noticeRes?.let { Notice(stringResource(it)) }
    EngineDropdown(
        engines = engines,
        current = current,
        enabled = shizuku is ShizukuState.Ready,
        onPick = onPick,
        modifier = Modifier.padding(horizontal = 16.dp),
    )
}

@Composable
private fun BackgroundSection(colors: ColorSelection, onChange: (ColorSelection) -> Unit) {
    SectionHeader(stringResource(R.string.section_background))
    ColorControls(colors = colors, onChange = onChange)
}

@Composable
private fun ActionsSection(
    config: Config,
    onChange: (Config) -> Unit,
    onEditTarget: (SlotTarget) -> Unit,
) {
    SectionHeader(stringResource(R.string.section_actions))

    SubHeader(stringResource(R.string.side_left))
    SlotList(config.left, Side.Left, config, onChange, onEditTarget)

    SubHeader(stringResource(R.string.side_center))
    OutlinedTextField(
        value = config.hint,
        onValueChange = { onChange(config.copy(hint = it)) },
        label = { Text(stringResource(R.string.label_hint)) },
        placeholder = { Text(stringResource(R.string.placeholder_search)) },
        singleLine = true,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
    )
    SlotRow(
        slot = config.center,
        showArrows = false,
        onBind = { onEditTarget(SlotTarget(Side.Center, 0)) },
        onRemove = null,
        onChange = { onChange(config.copy(center = it)) },
        onMoveUp = null,
        onMoveDown = null,
    )

    SubHeader(stringResource(R.string.side_right))
    SlotList(config.right, Side.Right, config, onChange, onEditTarget)
}

@Composable
private fun SlotList(
    slots: List<Slot>,
    side: Side,
    config: Config,
    onChange: (Config) -> Unit,
    onEditTarget: (SlotTarget) -> Unit,
) {
    slots.forEachIndexed { index, slot ->
        SlotRow(
            slot = slot,
            showArrows = slots.size > 1,
            onBind = { onEditTarget(SlotTarget(side, index)) },
            onRemove = { onChange(config.withSlots(side, slots.without(index))) },
            onChange = { next -> onChange(config.updateSlot(side, index) { next }) },
            onMoveUp = if (index > 0) {
                { onChange(config.withSlots(side, slots.swapped(index, index - 1))) }
            } else {
                null
            },
            onMoveDown = if (index < slots.lastIndex) {
                { onChange(config.withSlots(side, slots.swapped(index, index + 1))) }
            } else {
                null
            },
        )
    }
    if (slots.size < 2) {
        AddSlotRow { onChange(config.withSlots(side, slots + Slot())) }
    }
}

@Composable
private fun AddSlotRow(onClick: () -> Unit) {
    ListItem(
        headlineContent = { Text(stringResource(R.string.action_add_button)) },
        leadingContent = { Icon(Icons.Default.Add, contentDescription = null) },
        modifier = Modifier.clickable(onClick = onClick),
    )
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 8.dp),
    )
}

@Composable
private fun SubHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 4.dp),
    )
}

@Composable
private fun SectionDivider() {
    HorizontalDivider(modifier = Modifier.padding(top = 8.dp))
}

@Composable
private fun Notice(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 8.dp),
    )
}

private fun <T> List<T>.without(index: Int): List<T> =
    filterIndexed { i, _ -> i != index }

private fun <T> List<T>.swapped(a: Int, b: Int): List<T> =
    toMutableList().apply {
        val item = this[a]
        this[a] = this[b]
        this[b] = item
    }
