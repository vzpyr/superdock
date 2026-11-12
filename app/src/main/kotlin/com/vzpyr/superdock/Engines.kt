package com.vzpyr.superdock

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings

data class Engine(
    val packageName: String,
    val label: String,
)

data class Target(
    val label: String,
    val detail: String?,
    val component: ComponentName,
)

fun findEngines(context: Context): List<Engine> {
    val pm = context.packageManager
    val webSearch = pm.queryIntentActivities(Intent(Intent.ACTION_WEB_SEARCH), 0)
        .mapNotNull { it.activityInfo?.packageName }

    return (webSearch + context.packageName)
        .distinct()
        .mapNotNull { pkg ->
            val label = runCatching {
                pm.getApplicationLabel(pm.getApplicationInfo(pkg, 0)).toString()
            }.getOrNull() ?: return@mapNotNull null
            Engine(pkg, label)
        }
        .sortedBy { it.label.lowercase() }
}

fun findTargets(context: Context): List<Target> {
    val pm = context.packageManager

    val activities = pm.getInstalledApplications(PackageManager.GET_META_DATA).flatMap { app ->
        runCatching {
            pm.getPackageInfo(app.packageName, PackageManager.GET_ACTIVITIES)
                .activities
                .orEmpty()
                .toList()
        }.getOrDefault(emptyList())
    }

    return activities
        .distinctBy { "${it.packageName}/${it.name}" }
        .mapNotNull { activity ->
            if (!activity.exported) return@mapNotNull null
            val appLabel = runCatching {
                pm.getApplicationLabel(pm.getApplicationInfo(activity.packageName, 0)).toString()
            }.getOrDefault(activity.packageName)
            val name = activity.name.substringAfterLast('.')
            Target(
                label = appLabel,
                detail = if (name == appLabel) null else name,
                component = ComponentName(activity.packageName, activity.name),
            )
        }
        .sortedWith(compareBy({ it.label.lowercase() }, { it.detail.orEmpty() }))
}

fun currentEngine(context: Context): String? =
    Settings.Secure.getString(context.contentResolver, ENGINE_KEY)?.takeIf { it.isNotBlank() }
