package com.jeet.island.permissions

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

enum class RequiredPermission(val label: String) {
    OVERLAY("Draw over other apps"),
    NOTIFICATION_LISTENER("Notification access"),
    POST_NOTIFICATIONS("Post notifications"),
    PHONE_STATE("Phone state"),
    EXACT_ALARM("Exact alarm"),
    BATTERY_OPT("Ignore battery optimization");
}

object PermissionChecker {

    fun check(context: Context, p: RequiredPermission): Boolean = when (p) {
        RequiredPermission.OVERLAY -> Settings.canDrawOverlays(context)
        RequiredPermission.NOTIFICATION_LISTENER -> {
            val enabled = NotificationManagerCompat.getEnabledListenerPackages(context)
            enabled.contains(context.packageName)
        }
        RequiredPermission.POST_NOTIFICATIONS ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
                    PackageManager.PERMISSION_GRANTED
            else true
        RequiredPermission.PHONE_STATE ->
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) ==
                PackageManager.PERMISSION_GRANTED
        RequiredPermission.EXACT_ALARM -> {
            val am = context.getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) am.canScheduleExactAlarms() else true
        }
        RequiredPermission.BATTERY_OPT -> {
            val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
            pm.isIgnoringBatteryOptimizations(context.packageName)
        }
    }

    fun allGranted(context: Context): Boolean =
        RequiredPermission.entries.all { check(context, it) }

    fun criticalGranted(context: Context): Boolean =
        check(context, RequiredPermission.OVERLAY)
}
