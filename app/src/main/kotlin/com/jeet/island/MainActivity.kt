package com.jeet.island

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.jeet.island.permissions.PermissionChecker
import com.jeet.island.permissions.PermissionsScreen
import com.jeet.island.service.IslandService
import com.jeet.island.service.OverlayController
import com.jeet.island.settings.GlassSettingsStore
import com.jeet.island.ui.settings.SettingsScreen
import com.jeet.island.ui.settings.TimerScreen
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Scaffold { padding ->
                    AppRoot(modifier = Modifier.fillMaxSize().padding(padding))
                }
            }
        }
    }
}

@Composable
private fun AppRoot(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var screen by remember { mutableStateOf(Screen.Permissions) }
    var refreshToken by remember { mutableIntStateOf(0) }
    val battDismissed by GlassSettingsStore.battDialogDismissedFlow(context).collectAsState(initial = true)
    var showBattDialog by remember { mutableStateOf(false) }

    androidx.compose.runtime.DisposableEffect(Unit) {
        val activity = context as ComponentActivity
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) refreshToken++
        }
        activity.lifecycle.addObserver(observer)
        onDispose { activity.lifecycle.removeObserver(observer) }
    }

    androidx.compose.runtime.LaunchedEffect(battDismissed) {
        if (!battDismissed) {
            val pm = context.getSystemService(android.content.Context.POWER_SERVICE) as android.os.PowerManager
            if (!pm.isIgnoringBatteryOptimizations(context.packageName)) {
                showBattDialog = true
            }
        }
    }

    if (showBattDialog) {
        AlertDialog(
            onDismissRequest = { showBattDialog = false },
            title = { Text("Keep running in background?") },
            text = { Text("Dynamic Island needs to stay alive to show the pill. Grant battery optimization exemption?") },
            confirmButton = {
                TextButton(onClick = {
                    showBattDialog = false
                    scope.launch { GlassSettingsStore.setBattDialogDismissed(context) }
                    val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
                        .apply { data = Uri.parse("package:${context.packageName}") }
                    runCatching { context.startActivity(intent) }
                }) { Text("Open settings") }
            },
            dismissButton = {
                TextButton(onClick = {
                    showBattDialog = false
                    scope.launch { GlassSettingsStore.setBattDialogDismissed(context) }
                }) { Text("Later") }
            }
        )
    }

    when (screen) {
        Screen.Permissions -> PermissionsScreen(
            refreshToken = refreshToken,
            onOpenSettings = { screen = Screen.Settings },
            onStartOverlay = {
                // Android 15 rule: overlay BEFORE foreground service start.
                if (PermissionChecker.criticalGranted(context)) {
                    OverlayController.show(context)
                    context.startForegroundService(IslandService.intent(context))
                    screen = Screen.Running
                }
            }
        )
        Screen.Settings -> SettingsScreen(onBack = { screen = Screen.Permissions })
        Screen.Running -> RunningScreen(
            onSettings = { screen = Screen.Settings },
            onTimer = { screen = Screen.Timer },
            onStop = {
                context.stopService(IslandService.intent(context))
                OverlayController.hide(context)
                screen = Screen.Permissions
            }
        )
        Screen.Timer -> TimerScreen(onBack = { screen = Screen.Running })
    }
}

private enum class Screen { Permissions, Settings, Running, Timer }

@Composable
private fun RunningScreen(onSettings: () -> Unit, onTimer: () -> Unit, onStop: () -> Unit) {
    androidx.compose.foundation.layout.Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp)
    ) {
        Text("Dynamic Island is running.", style = MaterialTheme.typography.titleMedium)
        androidx.compose.material3.OutlinedButton(onClick = onSettings) {
            Text("Glass settings")
        }
        androidx.compose.material3.OutlinedButton(onClick = onTimer) {
            Text("Timer test")
        }
        androidx.compose.material3.OutlinedButton(onClick = onStop) {
            Text("Stop overlay")
        }
    }
}

