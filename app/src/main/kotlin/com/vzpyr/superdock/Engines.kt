package com.vzpyr.superdock

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager

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
        .mapNotNull { packageName ->
            val label = runCatching {
                pm.getApplicationLabel(pm.getApplicationInfo(packageName, 0)).toString()
            }.getOrNull() ?: return@mapNotNull null
            Engine(packageName, label)
        }
        .sortedBy { it.label.lowercase() }
}

fun findTargets(context: Context): List<Target> {
    val pm = context.packageManager

    return pm.getInstalledPackages(PackageManager.GET_ACTIVITIES)
        .flatMap { packageInfo ->
            val appLabel = runCatching {
                packageInfo.applicationInfo?.let { pm.getApplicationLabel(it).toString() }
            }.getOrNull() ?: packageInfo.packageName

            packageInfo.activities.orEmpty()
                .filter { it.exported }
                .map { activity ->
                    val name = activity.name.substringAfterLast('.')
                    Target(
                        label = appLabel,
                        detail = name.takeUnless { it == appLabel || it.isEmpty() },
                        component = ComponentName(activity.packageName, activity.name),
                    )
                }
        }
        .distinctBy { it.component.flattenToString() }
        .sortedWith(compareBy({ it.label.lowercase() }, { it.detail.orEmpty() }))
}
