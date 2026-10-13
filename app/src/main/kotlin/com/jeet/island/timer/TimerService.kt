package com.jeet.island.timer

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import com.jeet.island.state.IslandSignals
import com.jeet.island.state.TimerSignal

class TimerService : LifecycleService() {

    override fun onCreate() {
        super.onCreate()
        ensureChannel()
        val notif = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Timer")
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setOngoing(true)
            .setSilent(true)
            .build()
        startForeground(NOTIF_ID, notif)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        when (intent?.action) {
            ACTION_START -> startTimer(intent.getLongExtra(EXTRA_DURATION, 0L))
            ACTION_CANCEL -> cancelTimer()
            ACTION_FIRE -> fireTimer()
        }
        return START_STICKY
    }

    private fun startTimer(durationMs: Long) {
        if (durationMs <= 0) return
        val endTime = System.currentTimeMillis() + durationMs
        IslandSignals.timer.value = TimerSignal.Running(endTimeMillis = endTime, totalDurationMs = durationMs)

        val am = getSystemService(ALARM_SERVICE) as AlarmManager
        val fireIntent = Intent(this, TimerAlarmReceiver::class.java).apply { action = ACTION_FIRE }
        val pi = PendingIntent.getBroadcast(
            this, 0, fireIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !am.canScheduleExactAlarms()) {
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, endTime, pi)
        } else {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, endTime, pi)
        }
    }

    private fun cancelTimer() {
        val am = getSystemService(ALARM_SERVICE) as AlarmManager
        val fireIntent = Intent(this, TimerAlarmReceiver::class.java).apply { action = ACTION_FIRE }
        val pi = PendingIntent.getBroadcast(
            this, 0, fireIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        am.cancel(pi)
        IslandSignals.timer.value = TimerSignal.Idle
    }

    private fun fireTimer() {
        IslandSignals.timer.value = TimerSignal.Finished(System.currentTimeMillis())
        playBell()
        vibrate()
    }

    private fun playBell() {
        runCatching {
            val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val ringtone = RingtoneManager.getRingtone(this, uri)
            ringtone.audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
            ringtone.play()
        }
    }

    private fun vibrate() {
        val effect = VibrationEffect.createOneShot(300, VibrationEffect.DEFAULT_AMPLITUDE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vm = getSystemService(VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vm.defaultVibrator.vibrate(effect)
        } else {
            @Suppress("DEPRECATION")
            (getSystemService(VIBRATOR_SERVICE) as Vibrator).vibrate(effect)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    private fun ensureChannel() {
        val mgr = getSystemService(NotificationManager::class.java)
        if (mgr.getNotificationChannel(CHANNEL_ID) == null) {
            mgr.createNotificationChannel(
                NotificationChannel(CHANNEL_ID, "Timer", NotificationManager.IMPORTANCE_MIN)
            )
        }
    }

    companion object {
        const val CHANNEL_ID = "timer_service"
        const val NOTIF_ID = 43
        const val ACTION_START = "com.jeet.island.timer.START"
        const val ACTION_CANCEL = "com.jeet.island.timer.CANCEL"
        const val ACTION_FIRE = "com.jeet.island.timer.FIRE"
        const val EXTRA_DURATION = "duration_ms"

        fun startIntent(context: Context, durationMs: Long) =
            Intent(context, TimerService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_DURATION, durationMs)
            }

        fun cancelIntent(context: Context) =
            Intent(context, TimerService::class.java).apply { action = ACTION_CANCEL }
    }
}
