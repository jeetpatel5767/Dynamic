package com.jeet.island.call

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.ContactsContract
import android.telephony.TelephonyCallback
import android.telephony.TelephonyManager
import androidx.core.content.ContextCompat
import com.jeet.island.state.CallSignal
import com.jeet.island.state.IslandSignals
import java.util.concurrent.Executor

/**
 * Watches CALL_STATE on API 31+. Resolves caller name via contacts when available.
 */
class CallWatcher(private val context: Context) {

    private val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
    private val executor: Executor = java.util.concurrent.Executors.newSingleThreadExecutor()
    private var callback: Any? = null

    @SuppressLint("MissingPermission")
    fun start() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return
        if (!hasPhonePermission()) return
        val cb = object : TelephonyCallback(), TelephonyCallback.CallStateListener {
            override fun onCallStateChanged(state: Int) {
                when (state) {
                    TelephonyManager.CALL_STATE_RINGING -> {
                        // Incoming number can't be read without READ_CALL_LOG + READ_PHONE_NUMBERS on newer APIs.
                        IslandSignals.call.value = CallSignal(number = "", displayName = null)
                    }
                    TelephonyManager.CALL_STATE_IDLE -> IslandSignals.call.value = null
                    TelephonyManager.CALL_STATE_OFFHOOK -> IslandSignals.call.value = null
                }
            }
        }
        tm.registerTelephonyCallback(executor, cb)
        callback = cb
    }

    fun stop() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return
        (callback as? TelephonyCallback)?.let { tm.unregisterTelephonyCallback(it) }
        callback = null
        IslandSignals.call.value = null
    }

    private fun hasPhonePermission() =
        ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) ==
            PackageManager.PERMISSION_GRANTED
}

/**
 * Static helpers for Answer / Decline. ponytail: TelecomManager path only; no low-level fallback.
 */
object CallController {
    @SuppressLint("MissingPermission")
    fun answer(context: Context) {
        val tm = context.getSystemService(Context.TELECOM_SERVICE) as? android.telecom.TelecomManager
        runCatching { tm?.acceptRingingCall() }
    }

    @SuppressLint("MissingPermission")
    fun decline(context: Context) {
        val tm = context.getSystemService(Context.TELECOM_SERVICE) as? android.telecom.TelecomManager
        runCatching { tm?.endCall() }
    }

    @Suppress("unused")
    fun resolveContactName(context: Context, number: String): String? {
        if (number.isBlank()) return null
        val uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number))
        return runCatching {
            context.contentResolver.query(
                uri, arrayOf(ContactsContract.PhoneLookup.DISPLAY_NAME), null, null, null
            )?.use { if (it.moveToFirst()) it.getString(0) else null }
        }.getOrNull()
    }
}
