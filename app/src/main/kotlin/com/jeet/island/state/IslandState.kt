package com.jeet.island.state

import android.graphics.Bitmap
import android.graphics.drawable.Drawable

sealed interface IslandState {
    data object Idle : IslandState

    data class ExpandedNotif(
        val packageName: String,
        val title: String,
        val text: String,
        val icon: Drawable?
    ) : IslandState

    data class ExpandedMedia(
        val packageName: String,
        val title: String,
        val artist: String,
        val albumArt: Bitmap?,
        val isPlaying: Boolean,
        val positionMs: Long,
        val durationMs: Long
    ) : IslandState

    data class ExpandedTimer(
        val endTimeMillis: Long,
        val totalDurationMs: Long,
        val remainingMs: Long,
        val finished: Boolean = false
    ) : IslandState

    data class ExpandedCall(
        val number: String,
        val displayName: String?
    ) : IslandState

    data class ChargingPulse(
        val batteryPercent: Int
    ) : IslandState

    data class Split(
        val leading: IslandState,
        val trailing: IslandState
    ) : IslandState
}
