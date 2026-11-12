package com.vzpyr.superdock

private val iconRes = mapOf(
    IconKey.Search to R.drawable.ic_search,
    IconKey.Mic to R.drawable.ic_mic,
    IconKey.Globe to R.drawable.ic_globe,
    IconKey.Camera to R.drawable.ic_camera,
    IconKey.Settings to R.drawable.ic_settings,
    IconKey.Apps to R.drawable.ic_apps,
)

private val iconLabel = mapOf(
    IconKey.Search to R.string.icon_search,
    IconKey.Mic to R.string.icon_mic,
    IconKey.Globe to R.string.icon_globe,
    IconKey.Camera to R.string.icon_camera,
    IconKey.Settings to R.string.icon_settings,
    IconKey.Apps to R.string.icon_apps,
)

fun IconKey.drawable(): Int = iconRes.getValue(this)

fun IconKey.label(): Int = iconLabel.getValue(this)
