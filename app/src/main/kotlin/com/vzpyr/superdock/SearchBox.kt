package com.vzpyr.superdock

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.glance.ColorFilter
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
import androidx.glance.layout.RowScope
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
            val scheme = colorScheme(context, context.isNight())
            val background = ColorProvider(config.colors.background(scheme))
            val content = ColorProvider(config.colors.foreground(scheme))

            GlanceTheme(
                colors = ColorProviders(
                    colorScheme(context, false),
                    colorScheme(context, true),
                ),
            ) {
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
                                color = config.hintColor?.let { ColorProvider(Color(it)) } ?: content,
                            ),
                        )
                    }
                    config.right.forEach { SlotView(it) }
                }
            }
        }
    }
}

@Composable
private fun RowScope.SlotView(slot: Slot) {
    val context = LocalContext.current
    val scheme = colorScheme(context, context.isNight())

    Row(
        modifier = GlanceModifier
            .size(48.dp)
            .clickable(slot.action(context)),
        verticalAlignment = Alignment.CenterVertically,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            provider = ImageProvider(slot.icon.drawable),
            contentDescription = context.getString(slot.icon.label),
            modifier = GlanceModifier.size(24.dp),
            colorFilter = ColorFilter.tint(ColorProvider(slot.colors.iconColor(scheme))),
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
        ?: Intent().setComponent(ComponentName(this, MainActivity::class.java))

class SearchBoxReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = SearchBox()
}
