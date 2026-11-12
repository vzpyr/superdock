package com.vzpyr.superdock

import android.content.ComponentName

enum class IconKey(val drawable: Int, val label: Int) {
    Search(R.drawable.ic_search, R.string.icon_search),
    Mic(R.drawable.ic_mic, R.string.icon_mic),
    Globe(R.drawable.ic_globe, R.string.icon_globe),
    Camera(R.drawable.ic_camera, R.string.icon_camera),
    Settings(R.drawable.ic_settings, R.string.icon_settings),
    Apps(R.drawable.ic_apps, R.string.icon_apps),
}

enum class Side { Left, Center, Right }

enum class ColorMode { MaterialYou, Custom }

data class ColorSelection(
    val mode: ColorMode = ColorMode.MaterialYou,
    val paletteKey: PaletteKey = PaletteKey.SurfaceContainerHigh,
    val custom: Int = 0xFF1B1B1F.toInt(),
    val alpha: Int = 255,
)

data class Slot(
    val icon: IconKey = IconKey.Search,
    val target: String? = null,
    val colors: ColorSelection = ColorSelection(
        paletteKey = PaletteKey.Surface,
        custom = 0xFFFFFFFF.toInt(),
    ),
)

data class Config(
    val colors: ColorSelection = ColorSelection(),
    val hint: String = "",
    val hintColor: Int? = null,
    val left: List<Slot> = emptyList(),
    val right: List<Slot> = emptyList(),
    val center: Slot = Slot(),
)

data class SlotTarget(val side: Side, val index: Int) {
    fun bind(config: Config, component: ComponentName): Config =
        config.updateSlot(side, index) { it.copy(target = component.flattenToString()) }
}

fun Config.slots(side: Side): List<Slot> = when (side) {
    Side.Left -> left
    Side.Right -> right
    Side.Center -> listOf(center)
}

fun Config.withSlots(side: Side, slots: List<Slot>): Config = when (side) {
    Side.Left -> copy(left = slots)
    Side.Right -> copy(right = slots)
    Side.Center -> slots.firstOrNull()?.let { copy(center = it) } ?: this
}

fun Config.updateSlot(side: Side, index: Int, transform: (Slot) -> Slot): Config =
    withSlots(
        side,
        slots(side).mapIndexed { i, slot ->
            if (i == index) transform(slot) else slot
        },
    )

fun String.toComponentName(): ComponentName? =
    runCatching { ComponentName.unflattenFromString(this) }.getOrNull()
