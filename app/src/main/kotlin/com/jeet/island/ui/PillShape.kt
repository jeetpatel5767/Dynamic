package com.jeet.island.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.hsv
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.jeet.island.settings.GlassSettings
import kotlin.random.Random

@Composable
fun PillShape(
    width: Dp,
    height: Dp,
    settings: GlassSettings,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit = {}
) {
    val cornerRadius = height / 2
    val shape = RoundedCornerShape(cornerRadius)
    val noise = remember { generateNoise(32) }

    val bgColor =
        if (!settings.glassEnabled) Color.Black
        else if (settings.tintHue > 0f) Color.hsv(settings.tintHue, 0.3f, 0.1f, settings.glassOpacity)
        else Color.Black.copy(alpha = settings.glassOpacity)

    val borderColor = if (settings.glassEnabled) Color.White.copy(alpha = settings.borderAlpha) else Color.Transparent

    Box(
        modifier = modifier
            .widthIn(min = width)
            .heightIn(min = height)
            .clip(shape)
            .graphicsLayer { }
            .drawBehind {
                drawRect(bgColor)
                if (settings.glassEnabled && settings.highlightStrength > 0f) {
                    drawRect(
                        brush = Brush.verticalGradient(
                            0f to Color.White.copy(alpha = settings.highlightStrength * 0.3f),
                            0.5f to Color.Transparent
                        )
                    )
                }
                if (settings.glassEnabled && settings.noiseAmount > 0f) {
                    drawImage(
                        image = noise,
                        srcOffset = IntOffset.Zero,
                        srcSize = IntSize(noise.width, noise.height),
                        dstOffset = IntOffset.Zero,
                        dstSize = IntSize(size.width.toInt(), size.height.toInt()),
                        alpha = settings.noiseAmount
                    )
                }
            }
            .border(1.dp, borderColor, shape),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

private fun generateNoise(size: Int): ImageBitmap {
    val bmp = android.graphics.Bitmap.createBitmap(size, size, android.graphics.Bitmap.Config.ARGB_8888)
    val rng = Random(0xC0FFEE)
    val px = IntArray(size * size)
    for (i in px.indices) {
        val v = rng.nextInt(0, 256)
        px[i] = (0xFF shl 24) or (v shl 16) or (v shl 8) or v
    }
    bmp.setPixels(px, 0, size, 0, 0, size, size)
    return bmp.asImageBitmap()
}
