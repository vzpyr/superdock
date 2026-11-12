package com.vzpyr.superdock

import android.content.Context

enum class IconKey { Search, Mic, Globe, Camera, Settings, Apps }

enum class Side { Left, Center, Right }

enum class ColorMode { MaterialYou, Custom }

data class Slot(
    val icon: IconKey,
    val target: String?,
    val colorMode: ColorMode,
    val paletteKey: PaletteKey,
    val color: Int,
    val colorAlpha: Int,
)

data class Config(
    val colorMode: ColorMode = ColorMode.MaterialYou,
    val paletteKey: PaletteKey = PaletteKey.SurfaceContainerHigh,
    val color: Int = 0xFF1B1B1F.toInt(),
    val colorAlpha: Int = 255,
    val hint: String = "",
    val hintColor: Int? = null,
    val left: List<Slot> = emptyList(),
    val right: List<Slot> = emptyList(),
    val center: Slot = Slot(
        icon = IconKey.Search,
        target = null,
        colorMode = ColorMode.MaterialYou,
        paletteKey = PaletteKey.Surface,
        color = 0xFFFFFFFF.toInt(),
        colorAlpha = 255,
    ),
)

class ConfigStore(context: Context) {
    private val prefs = context.applicationContext
        .getSharedPreferences("searchbox", Context.MODE_PRIVATE)

    fun load(): Config {
        val defaults = Config()
        return Config(
            colorMode = runCatching {
                ColorMode.valueOf(prefs.getString(KEY_MODE, null).orEmpty())
            }.getOrDefault(defaults.colorMode),
            colorAlpha = prefs.getInt(KEY_ALPHA, defaults.colorAlpha),
            paletteKey = runCatching {
                PaletteKey.valueOf(prefs.getString(KEY_PALETTE, null).orEmpty())
            }.getOrDefault(defaults.paletteKey),
            color = prefs.getInt(KEY_COLOR, defaults.color),
            hint = prefs.getString(KEY_HINT, defaults.hint).orEmpty(),
            hintColor = prefs.getInt(KEY_HINT_COLOR, 0).takeIf { it != 0 },
            left = decodeList(prefs.getString(KEY_LEFT, null)),
            right = decodeList(prefs.getString(KEY_RIGHT, null)),
            center = decodeSlot(prefs.getString(KEY_CENTER, null)) ?: defaults.center,
        )
    }

    fun save(config: Config) {
        prefs.edit()
            .putString(KEY_MODE, config.colorMode.name)
            .putString(KEY_PALETTE, config.paletteKey.name)
            .putInt(KEY_COLOR, config.color)
            .putInt(KEY_ALPHA, config.colorAlpha)
            .putString(KEY_HINT, config.hint)
            .putInt(KEY_HINT_COLOR, config.hintColor ?: 0)
            .putString(KEY_LEFT, encodeList(config.left))
            .putString(KEY_RIGHT, encodeList(config.right))
            .putString(KEY_CENTER, encodeSlot(config.center))
            .apply()
    }

    private fun encodeList(slots: List<Slot>): String = slots.joinToString(";") { encodeSlot(it) }

    private fun encodeSlot(slot: Slot?): String = listOf(
        slot?.icon?.name.orEmpty(),
        slot?.target.orEmpty(),
        slot?.colorMode?.name.orEmpty(),
        slot?.paletteKey?.name.orEmpty(),
        slot?.color?.toString().orEmpty(),
        slot?.colorAlpha?.toString().orEmpty(),
    ).joinToString("|")

    private fun decodeList(raw: String?): List<Slot> =
        raw.orEmpty().split(";").mapNotNull { decodeSlot(it) }

    private fun decodeSlot(raw: String?): Slot? {
        if (raw.isNullOrBlank()) return null
        val parts = raw.split("|")
        val icon = IconKey.entries.firstOrNull { it.name == parts[0] } ?: return null
        return Slot(
            icon = icon,
            target = parts.getOrNull(1)?.takeIf { it.isNotEmpty() },
            colorMode = runCatching {
                ColorMode.valueOf(parts.getOrNull(2).orEmpty())
            }.getOrDefault(ColorMode.MaterialYou),
            paletteKey = runCatching {
                PaletteKey.valueOf(parts.getOrNull(3).orEmpty())
            }.getOrDefault(PaletteKey.Surface),
            color = parts.getOrNull(4)?.toIntOrNull() ?: 0xFFFFFFFF.toInt(),
            colorAlpha = parts.getOrNull(5)?.toIntOrNull() ?: 255,
        )
    }

    private companion object {
        const val KEY_MODE = "mode"
        const val KEY_PALETTE = "palette"
        const val KEY_COLOR = "color"
        const val KEY_ALPHA = "color_alpha"
        const val KEY_HINT = "hint"
        const val KEY_HINT_COLOR = "hint_color"
        const val KEY_LEFT = "left"
        const val KEY_RIGHT = "right"
        const val KEY_CENTER = "center"
    }
}

fun String.toComponentName(): android.content.ComponentName? =
    runCatching { android.content.ComponentName.unflattenFromString(this) }.getOrNull()
