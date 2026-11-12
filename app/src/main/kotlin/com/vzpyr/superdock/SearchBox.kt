package com.vzpyr.superdock

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.action.Action
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.material3.ColorProviders
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider

class SearchBox : GlanceAppWidget() {
    override val sizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val config = ConfigStore(context).load()

        provideContent {
            val dynamic = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
            GlanceTheme(
                colors = ColorProviders(
                    if (dynamic) dynamicLightColorScheme(context) else lightColorScheme(),
                    if (dynamic) dynamicDarkColorScheme(context) else darkColorScheme(),
                ),
            ) {
                val scheme = colorScheme(context, context.isNight())
                val background = when (config.colorMode) {
                    ColorMode.MaterialYou -> ColorProvider(scheme.role(config.paletteKey))
                    ColorMode.Custom -> ColorProvider(Color(config.color.withAlpha(config.colorAlpha)))
                }
                val content = when (config.colorMode) {
                    ColorMode.MaterialYou -> ColorProvider(scheme.onRole(config.paletteKey))
                    ColorMode.Custom -> ColorProvider(
                        if (Color(config.color).luminance() > 0.5f) Color(0xFF1B1B1F) else Color(0xFFE3E3E3),
                    )
                }

                Row(
                    modifier = GlanceModifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                        .background(background)
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    config.left.forEach { SlotView(it) }
                    Box(
                        modifier = GlanceModifier
                            .defaultWeight()
                            .fillMaxHeight()
                            .clickable(config.center.action(context)),
                        contentAlignment = Alignment.CenterStart,
                    ) {
                        Text(
                            text = config.hint,
                            style = TextStyle(
                                color = config.hintColor?.let { ColorProvider(Color(it)) }
                                    ?: content,
                            ),
                        )
                    }
                    config.right.forEach { SlotView(it) }
                }
            }
        }
    }
}

@androidx.compose.runtime.Composable
private fun androidx.glance.layout.RowScope.SlotView(slot: Slot) {
    val context = LocalContext.current
    Row(
        modifier = GlanceModifier
            .size(48.dp)
            .clickable(slot.action(context)),
        verticalAlignment = Alignment.CenterVertically,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        val scheme = colorScheme(context, context.isNight())
        Image(
            provider = ImageProvider(slot.icon.drawable()),
            contentDescription = slot.icon.name,
            modifier = GlanceModifier.size(24.dp),
            colorFilter = androidx.glance.ColorFilter.tint(
                when (slot.colorMode) {
                    ColorMode.MaterialYou -> ColorProvider(scheme.role(slot.paletteKey))
                    ColorMode.Custom -> ColorProvider(Color(slot.color.withAlpha(slot.colorAlpha)))
                },
            ),
        )
    }
}

private fun Slot.action(context: Context): Action {
    val intent = target?.toComponentName()?.let { Intent().setComponent(it) }
        ?: context.launchIntent()
    return actionStartActivity(intent)
}

private fun Context.launchIntent(): Intent =
    packageManager.getLaunchIntentForPackage(packageName)
        ?: Intent().setComponent(android.content.ComponentName(this, MainActivity::class.java))

class SearchBoxReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = SearchBox()
}
