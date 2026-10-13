package com.jeet.island.state

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlin.math.max

/**
 * Reduces all signals into a single IslandState. Priority:
 *   Call > TimerFinished > Charging > Notif > Split(Timer+Media) > Media > Timer > Idle
 *
 * Notifications auto-collapse after NOTIF_TIMEOUT_MS unless a new one arrives.
 */
class IslandStateMachine(private val scope: CoroutineScope = CoroutineScope(SupervisorJob())) {

    private val _state = MutableStateFlow<IslandState>(IslandState.Idle)
    val state: StateFlow<IslandState> = _state.asStateFlow()

    private val activeNotif = MutableStateFlow<NotifSignal?>(null)
    private var notifTimeoutJob: Job? = null

    fun start() {
        IslandSignals.notif
            .onEach { notif ->
                if (notif == null) return@onEach
                activeNotif.value = notif
                notifTimeoutJob?.cancel()
                notifTimeoutJob = scope.launch {
                    delay(NOTIF_TIMEOUT_MS)
                    activeNotif.value = null
                }
            }
            .launchIn(scope)

        combine(
            activeNotif,
            IslandSignals.media,
            IslandSignals.timer,
            IslandSignals.call,
            IslandSignals.charging
        ) { notif, media, timer, call, charging ->
            reduce(notif, media, timer, call, charging)
        }.onEach { _state.value = it }.launchIn(scope)
    }

    fun stop() {
        scope.cancel()
    }

    private fun reduce(
        notif: NotifSignal?,
        media: MediaSignal?,
        timer: TimerSignal,
        call: CallSignal?,
        charging: ChargingSignal?
    ): IslandState {
        if (call != null) return IslandState.ExpandedCall(call.number, call.displayName)
        if (timer is TimerSignal.Finished) return timerFinishedState(timer)
        if (charging != null && isFreshCharging(charging)) {
            return IslandState.ChargingPulse(charging.batteryPercent)
        }
        if (notif != null) return notif.toState()

        val timerState = (timer as? TimerSignal.Running)?.toExpandedTimer()
        val mediaState = media?.toExpandedMedia()
        if (timerState != null && mediaState != null) {
            return IslandState.Split(leading = mediaState, trailing = timerState)
        }
        if (mediaState != null) return mediaState
        if (timerState != null) return timerState
        return IslandState.Idle
    }

    private fun timerFinishedState(signal: TimerSignal.Finished): IslandState =
        IslandState.ExpandedTimer(
            endTimeMillis = signal.firedAt,
            totalDurationMs = 0L,
            remainingMs = 0L,
            finished = true
        )

    private fun isFreshCharging(signal: ChargingSignal): Boolean =
        System.currentTimeMillis() - signal.startedAt < CHARGING_PULSE_MS

    private fun NotifSignal.toState() =
        IslandState.ExpandedNotif(packageName, title, text, icon)

    private fun MediaSignal.toExpandedMedia() =
        IslandState.ExpandedMedia(
            packageName = packageName,
            title = title,
            artist = artist,
            albumArt = albumArt,
            isPlaying = isPlaying,
            positionMs = positionMs,
            durationMs = durationMs
        )

    private fun TimerSignal.Running.toExpandedTimer(): IslandState.ExpandedTimer {
        val now = System.currentTimeMillis()
        return IslandState.ExpandedTimer(
            endTimeMillis = endTimeMillis,
            totalDurationMs = totalDurationMs,
            remainingMs = max(0L, endTimeMillis - now)
        )
    }

    companion object {
        const val NOTIF_TIMEOUT_MS = 3_000L
        const val CHARGING_PULSE_MS = 2_000L
    }
}
