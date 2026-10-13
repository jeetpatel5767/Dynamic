package com.jeet.island.timer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class TimerAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val fire = Intent(context, TimerService::class.java).apply {
            action = TimerService.ACTION_FIRE
        }
        context.startForegroundService(fire)
    }
}
