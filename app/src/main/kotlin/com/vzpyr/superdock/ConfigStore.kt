package com.vzpyr.superdock

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

class ConfigStore(context: Context) {
    private val prefs = context.applicationContext
        .getSharedPreferences("searchbox", Context.MODE_PRIVATE)

    fun load(): Config {
        val raw = prefs.getString(KEY_CONFIG, null) ?: return Config()
        return runCatching { JSONObject(raw).toConfig() }.getOrDefault(Config())
    }

    fun save(config: Config) {
        prefs.edit().putString(KEY_CONFIG, config.toJson().toString()).apply()
    }

    private companion object {
        const val KEY_CONFIG = "config"
    }
}

private fun Config.toJson(): JSONObject = JSONObject()
    .put("colors", colors.toJson())
    .put("hint", hint)
    .putOpt("hintColor", hintColor)
    .put("left", left.toJson())
    .put("right", right.toJson())
    .put("center", center.toJson())

private fun List<Slot>.toJson(): JSONArray = JSONArray().apply {
    forEach { put(it.toJson()) }
}

private fun Slot.toJson(): JSONObject = JSONObject()
    .put("icon", icon.name)
    .put("target", target)
    .put("colors", colors.toJson())

private fun ColorSelection.toJson(): JSONObject = JSONObject()
    .put("mode", mode.name)
    .put("palette", paletteKey.name)
    .put("custom", custom)
    .put("alpha", alpha)

private fun JSONObject.toConfig(): Config = Config(
    colors = optJSONObject("colors")?.toColorSelection() ?: ColorSelection(),
    hint = optString("hint"),
    hintColor = optInt("hintColor", 0).takeIf { it != 0 },
    left = optJSONArray("left").toSlots(),
    right = optJSONArray("right").toSlots(),
    center = optJSONObject("center")?.toSlot() ?: Slot(),
)

private fun JSONArray?.toSlots(): List<Slot> =
    if (this == null) {
        emptyList()
    } else {
        (0 until length()).mapNotNull { index -> optJSONObject(index)?.toSlot() }
    }

private fun JSONObject.toSlot(): Slot = Slot(
    icon = enum("icon", IconKey.Search),
    target = optString("target").takeIf { it.isNotEmpty() },
    colors = optJSONObject("colors")?.toColorSelection() ?: Slot().colors,
)

private fun JSONObject.toColorSelection(): ColorSelection = ColorSelection(
    mode = enum("mode", ColorMode.MaterialYou),
    paletteKey = enum("palette", PaletteKey.SurfaceContainerHigh),
    custom = optInt("custom", ColorSelection().custom),
    alpha = optInt("alpha", ColorSelection().alpha),
)

private inline fun <reified T : Enum<T>> JSONObject.enum(name: String, fallback: T): T =
    enumValues<T>().firstOrNull { it.name == optString(name) } ?: fallback
