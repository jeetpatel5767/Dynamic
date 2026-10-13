package com.jeet.island.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.content.res.Configuration
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.jeet.island.call.CallWatcher
import com.jeet.island.media.MediaSessionWatcher

/**
 * Foreground service that keeps the process alive while the overlay is attached.
 * Overlay is attached by MainActivity BEFORE this service starts (Android 15 rule).
 */
class IslandService : Service() {

    private var mediaWatcher: MediaSessionWatcher? = null
    private var callWatcher: CallWatcher? = null

    override fun onCreate() {
        super.onCreate()
        ensureChannel()
        val notif = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Dynamic Island")
            .setContentText("Overlay active")
            .setSmallIcon(android.R.drawable.stat_notify_more)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setOngoing(true)
            .setSilent(true)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                NOTIF_ID,
                notif,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )
        } else {
            startForeground(NOTIF_ID, notif)
        }

        mediaWatcher = MediaSessionWatcher(this).also { it.start() }
        callWatcher = CallWatcher(this).also { it.start() }
        Log.d(TAG, "IslandService started as foreground")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int = START_STICKY

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        OverlayController.reposition(this)
    }

    override fun onDestroy() {
        mediaWatcher?.stop()
        callWatcher?.stop()
        OverlayController.hide(this)
        super.onDestroy()
    }

    private fun ensureChannel() {
        val mgr = getSystemService(NotificationManager::class.java)
        if (mgr.getNotificationChannel(CHANNEL_ID) == null) {
            mgr.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID,
                    "Island Service",
                    NotificationManager.IMPORTANCE_MIN
                )
            )
        }
    }

    companion object {
        const val CHANNEL_ID = "island_service"
        const val NOTIF_ID = 42
        private const val TAG = "IslandService"

        fun intent(context: Context) = Intent(context, IslandService::class.java)
    }
}
