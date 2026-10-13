package com.jeet.island.notif

import android.content.pm.PackageManager
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.jeet.island.state.IslandSignals
import com.jeet.island.state.NotifSignal

class NotifListener : NotificationListenerService() {

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        sbn ?: return
        val extras = sbn.notification.extras
        val title = extras.getCharSequence(android.app.Notification.EXTRA_TITLE)?.toString()
            ?: sbn.packageName
        val text = extras.getCharSequence(android.app.Notification.EXTRA_TEXT)?.toString() ?: ""
        val icon = runCatching { packageManager.getApplicationIcon(sbn.packageName) }.getOrNull()
        val signal = NotifSignal(
            packageName = sbn.packageName,
            title = title,
            text = text,
            icon = icon,
            postedAt = System.currentTimeMillis()
        )
        Log.d(TAG, "post ${sbn.packageName}: $title / $text")
        IslandSignals.notif.value = signal
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) = Unit

    companion object {
        private const val TAG = "NotifListener"
    }
}
