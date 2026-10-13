package com.jeet.island.media

import android.content.ComponentName
import android.content.Context
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.util.Log
import com.jeet.island.notif.NotifListener
import com.jeet.island.state.IslandSignals
import com.jeet.island.state.MediaSignal
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Reads active media sessions. Requires BIND_NOTIFICATION_LISTENER_SERVICE grant.
 * Polls position every 500 ms while playing (feature 4.2-2).
 */
class MediaSessionWatcher(private val context: Context) {

    private val mgr = context.getSystemService(Context.MEDIA_SESSION_SERVICE) as MediaSessionManager
    private val component = ComponentName(context, NotifListener::class.java)
    private val scope = CoroutineScope(Dispatchers.Main)
    private var pollJob: Job? = null
    private var currentController: MediaController? = null

    private val callback = object : MediaController.Callback() {
        override fun onMetadataChanged(metadata: MediaMetadata?) = push()
        override fun onPlaybackStateChanged(state: PlaybackState?) {
            push()
            restartPolling()
        }
    }

    private val listener = MediaSessionManager.OnActiveSessionsChangedListener { controllers ->
        updateController(controllers?.firstOrNull())
    }

    fun start() {
        try {
            mgr.addOnActiveSessionsChangedListener(listener, component)
            updateController(mgr.getActiveSessions(component).firstOrNull())
        } catch (t: SecurityException) {
            Log.w(TAG, "Media session access denied; notif listener not granted?", t)
        }
    }

    fun stop() {
        pollJob?.cancel()
        currentController?.unregisterCallback(callback)
        currentController = null
        try {
            mgr.removeOnActiveSessionsChangedListener(listener)
        } catch (_: Throwable) {}
        IslandSignals.media.value = null
        scope.cancel()
    }

    private fun updateController(next: MediaController?) {
        currentController?.unregisterCallback(callback)
        currentController = next
        if (next != null) {
            next.registerCallback(callback)
            push()
            restartPolling()
        } else {
            pollJob?.cancel()
            IslandSignals.media.value = null
        }
    }

    private fun restartPolling() {
        pollJob?.cancel()
        pollJob = scope.launch {
            while (true) {
                val c = currentController ?: break
                val ps = c.playbackState
                if (ps?.state == PlaybackState.STATE_PLAYING) {
                    push()
                }
                delay(500)
            }
        }
    }

    private fun push() {
        val c = currentController ?: run {
            IslandSignals.media.value = null
            return
        }
        val m = c.metadata
        val ps = c.playbackState
        val title = m?.getString(MediaMetadata.METADATA_KEY_TITLE) ?: "Unknown"
        val artist = m?.getString(MediaMetadata.METADATA_KEY_ARTIST)
            ?: m?.getString(MediaMetadata.METADATA_KEY_ALBUM_ARTIST) ?: ""
        val art = m?.getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART)
            ?: m?.getBitmap(MediaMetadata.METADATA_KEY_ART)
        val duration = m?.getLong(MediaMetadata.METADATA_KEY_DURATION) ?: 0L
        val position = ps?.position ?: 0L
        val playing = ps?.state == PlaybackState.STATE_PLAYING

        IslandSignals.media.value = MediaSignal(
            packageName = c.packageName,
            title = title,
            artist = artist,
            albumArt = art,
            isPlaying = playing,
            positionMs = position,
            durationMs = duration,
            onPlayPause = {
                if (playing) c.transportControls.pause() else c.transportControls.play()
            }
        )
    }

    companion object {
        private const val TAG = "MediaSessionWatcher"
    }
}
