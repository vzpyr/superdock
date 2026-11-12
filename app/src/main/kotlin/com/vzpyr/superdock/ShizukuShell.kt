package com.vzpyr.superdock

import android.content.Context
import android.content.pm.PackageManager
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import moe.shizuku.server.IShizukuService
import rikka.shizuku.Shizuku

const val ENGINE_KEY = "selected_search_engine"
const val SHIZUKU_PACKAGE = "moe.shizuku.privileged.api"

val LAUNCHER_PACKAGES = listOf(
    "com.google.android.apps.nexuslauncher",
    "com.android.launcher3",
)

sealed interface ShizukuState {
    data object NotInstalled : ShizukuState
    data object NotRunning : ShizukuState
    data object PermissionRequired : ShizukuState
    data object Ready : ShizukuState
}

private fun service(): IShizukuService? = runCatching {
    IShizukuService.Stub.asInterface(Shizuku.getBinder())
}.getOrNull()

fun hasPermission(): Boolean =
    runCatching { service()?.checkSelfPermission() == true }.getOrDefault(false)

fun readEngine(context: Context): String? =
    Settings.Secure.getString(context.contentResolver, ENGINE_KEY)?.takeIf { it.isNotBlank() }

fun writeEngine(context: Context, packageName: String?): Boolean {
    val args = if (packageName == null) {
        arrayOf("settings", "delete", "secure", ENGINE_KEY)
    } else {
        arrayOf("settings", "put", "secure", ENGINE_KEY, packageName)
    }
    runShell(*args)
    return readEngine(context) == packageName
}

fun restartLauncher() {
    LAUNCHER_PACKAGES.forEach { pkg -> runShell("am", "force-stop", pkg) }
}

private fun runShell(vararg args: String): String = runCatching {
    val process = service()?.newProcess(arrayOf(*args), null, null) ?: return ""
    val output = java.io.FileInputStream(process.inputStream.fileDescriptor)
        .bufferedReader()
        .readText()
    process.waitFor()
    output
}.getOrDefault("")

fun requestShizukuPermission() {
    if (Shizuku.pingBinder() && !hasPermission()) {
        Shizuku.requestPermission(0)
    }
}

fun isInstalled(context: Context): Boolean = runCatching {
    context.packageManager.getPackageInfo(SHIZUKU_PACKAGE, 0)
}.isSuccess

@Composable
fun rememberShizukuState(): Pair<ShizukuState, () -> Unit> {
    val context = LocalContext.current
    var state by remember { mutableStateOf(currentState(context)) }
    var tick by remember { mutableIntStateOf(0) }

    val sync = { state = currentState(context) }
    val received = remember { Shizuku.OnBinderReceivedListener { sync() } }
    val dead = remember { Shizuku.OnBinderDeadListener { sync() } }
    val granted = remember { Shizuku.OnRequestPermissionResultListener { _, _ -> sync() } }

    LifecycleEventEffect(Lifecycle.Event.ON_START) {
        Shizuku.addBinderReceivedListenerSticky(received)
        Shizuku.addBinderDeadListener(dead)
        Shizuku.addRequestPermissionResultListener(granted)
        sync()
    }
    LifecycleEventEffect(Lifecycle.Event.ON_STOP) {
        Shizuku.removeBinderReceivedListener(received)
        Shizuku.removeBinderDeadListener(dead)
        Shizuku.removeRequestPermissionResultListener(granted)
    }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { sync() }

    tick
    return state to {
        tick++
        sync()
    }
}

private fun currentState(context: Context): ShizukuState = when {
    !isInstalled(context) -> ShizukuState.NotInstalled
    !Shizuku.pingBinder() -> ShizukuState.NotRunning
    !hasPermission() -> ShizukuState.PermissionRequired
    else -> ShizukuState.Ready
}

val ShizukuState.labelRes: Int
    get() = when (this) {
        ShizukuState.NotInstalled -> R.string.shizuku_not_installed
        ShizukuState.NotRunning -> R.string.shizuku_not_running
        ShizukuState.PermissionRequired -> R.string.shizuku_permission_required
        ShizukuState.Ready -> R.string.shizuku_connected
    }

@Suppress("unused")
private val unusedPermission = PackageManager.PERMISSION_GRANTED
