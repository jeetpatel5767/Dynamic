package com.jeet.island.cutout

import android.content.Context
import android.graphics.Rect
import android.os.Build
import android.util.Log
import android.view.WindowManager

object CutoutResolver {

    private const val TAG = "CutoutResolver"

    fun resolve(context: Context): CutoutGeometry? {
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager ?: return null
        val metrics = wm.currentWindowMetrics
        val cutout = metrics.windowInsets.displayCutout

        val geometry = when {
            cutout == null -> null
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && cutout.cutoutPath != null ->
                fromPath(cutout.cutoutPath!!, metrics.bounds)
            cutout.boundingRects.isNotEmpty() -> fromRect(cutout.boundingRects[0])
            else -> null
        }
        Log.d(TAG, "Resolved cutout geometry: $geometry")
        return geometry
    }

    private fun fromPath(path: android.graphics.Path, screenBounds: Rect): CutoutGeometry {
        val rf = android.graphics.RectF()
        path.computeBounds(rf, true)
        return CutoutGeometry(
            centerX = ((rf.left + rf.right) / 2f).toInt(),
            topY = rf.top.toInt(),
            width = rf.width().toInt(),
            height = rf.height().toInt(),
            path = path
        )
    }

    private fun fromRect(rect: Rect): CutoutGeometry = CutoutGeometry(
        centerX = rect.centerX(),
        topY = rect.top,
        width = rect.width(),
        height = rect.height(),
        path = null
    )
}
