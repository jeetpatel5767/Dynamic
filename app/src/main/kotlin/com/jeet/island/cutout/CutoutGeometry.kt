package com.jeet.island.cutout

import android.graphics.Path

data class CutoutGeometry(
    val centerX: Int,
    val topY: Int,
    val width: Int,
    val height: Int,
    val path: Path?
)
