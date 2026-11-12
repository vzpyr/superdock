package com.vzpyr.superdock

import android.content.ComponentName
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.glance.appwidget.updateAll
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val store = ConfigStore(this)
        setContent {
            SuperdockTheme {
                Screen(store)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Screen(store: ConfigStore) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val (shizuku, resync) = rememberShizukuState()

    var config by remember { mutableStateOf(store.load()) }
    var engine by remember { mutableStateOf(readEngine(context)) }
    var picker by remember { mutableStateOf<SlotTarget?>(null) }

    val ready = shizuku is ShizukuState.Ready
    val engines = remember { findEngines(context) }

    fun persist(next: Config) {
        config = next
        store.save(next)
        scope.launch { SearchBox().updateAll(context) }
    }

    picker?.let { target ->
        TargetSheet(
            onDismiss = { picker = null },
            onPick = { component ->
                persist(target.applyTo(config, component))
                picker = null
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
                        onClick = { resync() },
                    )
                    IconAction(
                        icon = Icons.Default.Warning,
                        label = stringResource(R.string.action_restart_launcher),
                        onClick = { restartLauncher() },
                        enabled = ready,
                    )
                },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 32.dp),
        ) {
            item { ShizukuRow(shizuku) }

            item { SectionDivider() }
            item { SectionHeader(stringResource(R.string.section_search_engine)) }
            item {
                if (!ready) {
                    val notice = when (shizuku) {
                        ShizukuState.NotInstalled -> R.string.notice_shizuku_not_installed
                        ShizukuState.NotRunning -> R.string.notice_shizuku_not_running
                        else -> R.string.notice_shizuku_permission
                    }
                    Notice(stringResource(notice))
                }
                EngineDropdown(
                    engines = engines,
                    current = engine,
                    enabled = ready,
                    onPick = { pkg ->
                        writeEngine(context, pkg)
                        engine = readEngine(context)
                        scope.launch { SearchBox().updateAll(context) }
                    },
                )
            }

            item { SectionDivider() }
            item { SectionHeader(stringResource(R.string.section_background)) }
            item {
                ColorModeRow(config.colorMode) { persist(config.copy(colorMode = it)) }
            }
            when (config.colorMode) {
                ColorMode.MaterialYou -> item {
                    PaletteDropdown(
                        selected = config.paletteKey,
                        onPick = { persist(config.copy(paletteKey = it)) },
                    )
                }

                ColorMode.Custom -> item {
                    ColorPicker(
                        color = config.color,
                        alpha = config.colorAlpha,
                        onColorChange = { persist(config.copy(color = it)) },
                        onAlphaChange = { persist(config.copy(colorAlpha = it)) },
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )
                }
            }
            item { SectionDivider() }
            item { SectionHeader(stringResource(R.string.section_actions)) }
            item {
                SubHeader(stringResource(R.string.side_left))
            }
            slotItems(config.left, Side.Left, config, ::persist) { picker = it }
            item {
                SubHeader(stringResource(R.string.side_center))
            }
            item {
                OutlinedTextField(
                    value = config.hint,
                    onValueChange = { persist(config.copy(hint = it)) },
                    label = { Text(stringResource(R.string.label_hint)) },
                    placeholder = { Text(stringResource(R.string.placeholder_search)) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                )
            }
            item {
                SlotRow(
                    slot = config.center,
                    showArrows = false,
                    onBind = { picker = SlotTarget(Side.Center, 0) },
                    onRemove = null,
                    onChange = { persist(config.copy(center = it)) },
                    onMoveUp = null,
                    onMoveDown = null,
                )
            }
            item {
                SubHeader(stringResource(R.string.side_right))
            }
            slotItems(config.right, Side.Right, config, ::persist) { picker = it }
        }
    }
}

private fun LazyListScope.slotItems(
    slots: List<Slot>,
    side: Side,
    config: Config,
    persist: (Config) -> Unit,
    onTarget: (SlotTarget) -> Unit,
) {
    items(slots.size) { index ->
        SlotRow(
            slot = slots[index],
            showArrows = slots.size > 1,
            onBind = { onTarget(SlotTarget(side, index)) },
            onRemove = { persist(config.with(side, slots.without(index))) },
            onChange = { next -> persist(config.with(side, slots.replaced(index) { next })) },
            onMoveUp = if (index > 0) ({ persist(config.with(side, slots.swapped(index, index - 1))) }) else null,
            onMoveDown = if (index < slots.lastIndex) ({ persist(config.with(side, slots.swapped(index, index + 1))) }) else null,
        )
    }
    if (slots.size < 2) {
        item {
            ListItem(
                headlineContent = { Text(stringResource(R.string.action_add_button)) },
                leadingContent = { Icon(Icons.Default.Add, contentDescription = null) },
                modifier = Modifier.clickable {
                    persist(
                        config.with(
                            side,
                            slots + Slot(
                                icon = IconKey.Search,
                                target = null,
                                colorMode = ColorMode.MaterialYou,
                                paletteKey = PaletteKey.Surface,
                                color = 0xFFFFFFFF.toInt(),
                                colorAlpha = 255,
                            ),
                        ),
                    )
                },
            )
        }
    }
}

private fun Config.with(side: Side, slots: List<Slot>): Config = when (side) {
    Side.Left -> copy(left = slots)
    Side.Right -> copy(right = slots)
    Side.Center -> this
}

private data class SlotTarget(val side: Side, val index: Int) {
    fun applyTo(config: Config, component: ComponentName): Config {
        val flat = component.flattenToString()
        return when (side) {
            Side.Left -> config.copy(left = config.left.replaced(index) { it.copy(target = flat) })
            Side.Right -> config.copy(right = config.right.replaced(index) { it.copy(target = flat) })
            Side.Center -> config.copy(center = config.center.copy(target = flat))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Tooltip(label: String, content: @Composable () -> Unit) {
    TooltipBox(
        positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Above),
        tooltip = { PlainTooltip { Text(label) } },
        state = rememberTooltipState(),
        content = content,
    )
}

@Composable
private fun IconAction(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
) {
    Tooltip(label) {
        IconButton(onClick = onClick, enabled = enabled) {
            Icon(icon, contentDescription = label)
        }
    }
}

@Composable
private fun ShizukuRow(state: ShizukuState) {
    ListItem(
        headlineContent = { Text(stringResource(R.string.shizuku)) },
        supportingContent = { Text(stringResource(state.labelRes)) },
        leadingContent = {
            Icon(
                imageVector = if (state is ShizukuState.Ready) Icons.Default.Check else Icons.Default.Warning,
                contentDescription = null,
                tint = if (state is ShizukuState.Ready) {
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ColorModeRow(mode: ColorMode, onChange: (ColorMode) -> Unit) {
    val options = listOf(
        ColorMode.MaterialYou to stringResource(R.string.color_material_you),
        ColorMode.Custom to stringResource(R.string.color_custom),
    )
    SingleChoiceSegmentedButtonRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
    ) {
        options.forEachIndexed { index, (value, label) ->
            SegmentedButton(
                selected = mode == value,
                onClick = { onChange(value) },
                shape = SegmentedButtonDefaults.itemShape(index, options.size),
            ) { Text(label) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PaletteDropdown(selected: PaletteKey, onPick: (PaletteKey) -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = Modifier.padding(horizontal = 16.dp),
    ) {
        OutlinedTextField(
            value = stringResource(selected.labelRes),
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(R.string.palette_color)) },
            leadingIcon = {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(selected.container(), CircleShape),
                )
            },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            PaletteKey.entries.forEach { key ->
                DropdownMenuItem(
                    text = { Text(stringResource(key.labelRes)) },
                    leadingIcon = {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .background(key.container(), CircleShape),
                        )
                    },
                    trailingIcon = {
                        if (key == selected) Icon(Icons.Default.Check, contentDescription = null)
                    },
                    onClick = {
                        onPick(key)
                        expanded = false
                    },
                )
            }
        }
    }
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
private fun SectionDivider() {
    HorizontalDivider(modifier = Modifier.padding(top = 8.dp))
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
private fun SlotRow(
    slot: Slot,
    showArrows: Boolean,
    onBind: () -> Unit,
    onRemove: (() -> Unit)?,
    onChange: (Slot) -> Unit,
    onMoveUp: (() -> Unit)?,
    onMoveDown: (() -> Unit)?,
) {
    val context = LocalContext.current
    var editing by remember { mutableStateOf(false) }

    ListItem(
        headlineContent = {
            val label = slot.target?.toComponentName()?.let { componentLabel(context, it) }
                ?: stringResource(R.string.slot_not_bound)
            Text(label)
        },
        supportingContent = { Text(stringResource(slot.icon.label())) },
        leadingContent = {
            Tooltip(stringResource(R.string.action_edit_appearance)) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .clickable { editing = true },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painter = painterResource(slot.icon.drawable()),
                        contentDescription = stringResource(R.string.action_edit_appearance),
                        tint = when (slot.colorMode) {
                            ColorMode.MaterialYou -> slot.paletteKey.on()
                            ColorMode.Custom -> Color(slot.color.withAlpha(slot.colorAlpha))
                        },
                    )
                }
            }
        },
        trailingContent = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (showArrows) {
                    onMoveUp?.let { up ->
                        IconAction(
                            icon = Icons.Default.KeyboardArrowUp,
                            label = stringResource(R.string.action_move_up),
                            onClick = up,
                        )
                    }
                    onMoveDown?.let { down ->
                        IconAction(
                            icon = Icons.Default.KeyboardArrowDown,
                            label = stringResource(R.string.action_move_down),
                            onClick = down,
                        )
                    }
                }
                if (onRemove != null) {
                    IconAction(
                        icon = Icons.Default.Close,
                        label = stringResource(R.string.action_remove),
                        onClick = onRemove,
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SlotAppearanceSheet(
    slot: Slot,
    onDismiss: () -> Unit,
    onChange: (Slot) -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    ) {
        LazyColumn(modifier = Modifier.fillMaxWidth()) {
            item {
                Text(
                    text = stringResource(R.string.sheet_icon),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                )
            }
            item {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    IconKey.entries.forEach { key ->
                        val selected = slot.icon == key
                        Tooltip(stringResource(key.label())) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (selected) {
                                            MaterialTheme.colorScheme.secondaryContainer
                                        } else {
                                            Color.Transparent
                                        },
                                    )
                                    .clickable { onChange(slot.copy(icon = key)) },
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    painter = painterResource(key.drawable()),
                                    contentDescription = stringResource(key.label()),
                                    tint = if (selected) {
                                        MaterialTheme.colorScheme.onSecondaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    },
                                )
                            }
                        }
                    }
                }
            }
            item {
                Text(
                    text = stringResource(R.string.sheet_color),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                )
            }
            item {
                ColorModeRow(slot.colorMode) { onChange(slot.copy(colorMode = it)) }
            }
            when (slot.colorMode) {
                ColorMode.MaterialYou -> item {
                    PaletteDropdown(
                        selected = slot.paletteKey,
                        onPick = { onChange(slot.copy(paletteKey = it)) },
                    )
                }

                ColorMode.Custom -> item {
                    ColorPicker(
                        color = slot.color,
                        alpha = slot.colorAlpha,
                        onColorChange = { onChange(slot.copy(color = it)) },
                        onAlphaChange = { onChange(slot.copy(colorAlpha = it)) },
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )
                }
            }
            item {
                Spacer(modifier = Modifier.size(32.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TargetSheet(onDismiss: () -> Unit, onPick: (ComponentName) -> Unit) {
    val context = LocalContext.current
    var query by remember { mutableStateOf("") }
    val targets = remember { findTargets(context) }
    val filtered = remember(targets, query) {
        targets.filter {
            query.isBlank() ||
                it.label.contains(query, true) ||
                it.detail.orEmpty().contains(query, true)
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = stringResource(R.string.sheet_bind_to),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
            )
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text(stringResource(R.string.placeholder_search)) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
            )
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false),
            ) {
                items(filtered) { target ->
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EngineDropdown(
    engines: List<Engine>,
    current: String?,
    enabled: Boolean,
    onPick: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val selected = engines.firstOrNull { it.packageName == current }
    val notSet = stringResource(R.string.engine_not_set)

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { if (enabled) expanded = it },
        modifier = Modifier.padding(horizontal = 16.dp),
    ) {
        OutlinedTextField(
            value = selected?.label ?: current ?: notSet,
            onValueChange = {},
            readOnly = true,
            enabled = enabled,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            engines.forEach { candidate ->
                DropdownMenuItem(
                    text = { Text(candidate.label) },
                    trailingIcon = {
                        if (candidate.packageName == current) {
                            Icon(Icons.Default.Check, contentDescription = null)
                        }
                    },
                    onClick = {
                        onPick(candidate.packageName)
                        expanded = false
                    },
                )
            }
        }
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

private fun List<Slot>.replaced(index: Int, transform: (Slot) -> Slot): List<Slot> =
    toMutableList().also { if (index in indices) it[index] = transform(it[index]) }

private fun List<Slot>.without(index: Int): List<Slot> =
    filterIndexed { i, _ -> i != index }

private fun List<Slot>.swapped(a: Int, b: Int): List<Slot> =
    toMutableList().also {
        val tmp = it[a]
        it[a] = it[b]
        it[b] = tmp
    }
