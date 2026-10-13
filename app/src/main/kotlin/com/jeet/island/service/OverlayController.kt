package com.jeet.island.service

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PixelFormat
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.jeet.island.cutout.CutoutGeometry
import com.jeet.island.cutout.CutoutResolver
import com.jeet.island.state.IslandStateMachine
import com.jeet.island.ui.overlay.IslandOverlay

/**
 * Owns the single WindowManager overlay view. Idempotent show/hide.
 * ponytail: process-global singleton, carries the overlay across activity death.
 */
object OverlayController {

    private const val TAG = "OverlayController"
    private var view: View? = null
    private var lifecycleOwner: OverlayLifecycleOwner? = null
    private val stateMachine = IslandStateMachine()
    private var started = false

    val LocalStateMachine = compositionLocalOf<IslandStateMachine> {
        error("No IslandStateMachine in composition")
    }

    @SuppressLint("InflateParams")
    fun show(context: Context) {
        if (view != null) return
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val geometry = CutoutResolver.resolve(context)
        val params = buildLayoutParams(context, geometry)

        if (!started) {
            stateMachine.start()
            started = true
        }

        val owner = OverlayLifecycleOwner().also { it.handleStart() }
        val composeView = ComposeView(context).apply {
            setViewTreeLifecycleOwner(owner)
            setViewTreeViewModelStoreOwner(owner)
            setViewTreeSavedStateRegistryOwner(owner)
            setContent {
                CompositionLocalProvider(LocalStateMachine provides stateMachine) {
                    IslandOverlay(geometry = geometry)
                }
            }
        }
        try {
            wm.addView(composeView, params)
            Log.d(TAG, "Overlay attached at geometry=$geometry")
        } catch (t: Throwable) {
            Log.e(TAG, "Failed to attach overlay", t)
            owner.handleStop()
            return
        }
        view = composeView
        lifecycleOwner = owner
    }

    fun hide(context: Context) {
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager
        val v = view ?: return
        try {
            wm?.removeView(v)
        } catch (t: Throwable) {
            Log.e(TAG, "removeView failed", t)
        }
        lifecycleOwner?.handleStop()
        view = null
        lifecycleOwner = null
    }

    fun reposition(context: Context) {
        val v = view ?: return
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val geometry = CutoutResolver.resolve(context)
        val params = buildLayoutParams(context, geometry)
        try {
            wm.updateViewLayout(v, params)
        } catch (t: Throwable) {
            Log.e(TAG, "updateViewLayout failed; hiding", t)
            hide(context)
        }
    }

    private fun buildLayoutParams(
        context: Context,
        geometry: CutoutGeometry?
    ): WindowManager.LayoutParams {
        val density = context.resources.displayMetrics.density
        val fallbackY = (24 * density).toInt()
        val y = geometry?.topY?.coerceAtLeast(0) ?: fallbackY
        return WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                or WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
            this.y = y
        }
    }
}
