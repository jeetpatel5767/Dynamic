package com.jeet.island.state

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Process-wide signal buses the services write to and the state machine reads.
 * ponytail: process-global singleton, swap for Hilt-scoped bus if testing demands.
 */
object IslandSignals {
    val notif = MutableStateFlow<NotifSignal?>(null)
    val media = MutableStateFlow<MediaSignal?>(null)
    val timer = MutableStateFlow<TimerSignal>(TimerSignal.Idle)
    val call = MutableStateFlow<CallSignal?>(null)
    val charging = MutableStateFlow<ChargingSignal?>(null)
}

data class NotifSignal(
    val packageName: String,
    val title: String,
    val text: String,
    val icon: Drawable?,
    val postedAt: Long
)

data class MediaSignal(
    val packageName: String,
    val title: String,
    val artist: String,
    val albumArt: Bitmap?,
    val isPlaying: Boolean,
    val positionMs: Long,
    val durationMs: Long,
    val onPlayPause: () -> Unit
)

sealed interface TimerSignal {
    data object Idle : TimerSignal
    data class Running(val endTimeMillis: Long, val totalDurationMs: Long) : TimerSignal
    data class Finished(val firedAt: Long) : TimerSignal
}

data class CallSignal(val number: String, val displayName: String?)

data class ChargingSignal(val batteryPercent: Int, val startedAt: Long)
