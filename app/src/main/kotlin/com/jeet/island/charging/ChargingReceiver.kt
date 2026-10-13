package com.jeet.island.charging

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import com.jeet.island.state.ChargingSignal
import com.jeet.island.state.IslandSignals

class ChargingReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_POWER_CONNECTED -> {
                val bm = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
                val level = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
                IslandSignals.charging.value =
                    ChargingSignal(batteryPercent = level, startedAt = System.currentTimeMillis())
            }
            Intent.ACTION_POWER_DISCONNECTED -> {
                IslandSignals.charging.value = null
            }
        }
    }
}

/** Registered dynamically for sticky battery broadcast at service start. */
fun chargingIntentFilter() = IntentFilter().apply {
    addAction(Intent.ACTION_POWER_CONNECTED)
    addAction(Intent.ACTION_POWER_DISCONNECTED)
}
