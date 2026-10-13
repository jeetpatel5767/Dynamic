package com.jeet.island.ui.states

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jeet.island.state.IslandState
import kotlinx.coroutines.delay
import kotlin.math.max

@Composable
fun TimerContent(state: IslandState.ExpandedTimer) {
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(state.endTimeMillis) {
        while (!state.finished && now < state.endTimeMillis) {
            now = System.currentTimeMillis()
            delay(200)
        }
    }
    val remaining = max(0L, state.endTimeMillis - now)
    val progress =
        if (state.totalDurationMs > 0L) (remaining.toFloat() / state.totalDurationMs.toFloat()).coerceIn(0f, 1f)
        else 0f

    val infinite = rememberInfiniteTransition(label = "pulse")
    val pulse by infinite.animateFloat(
        initialValue = 1f,
        targetValue = if (state.finished) 1.08f else 1f,
        animationSpec = infiniteRepeatable(tween(260), RepeatMode.Reverse),
        label = "pulse"
    )
    val finishScale = pulse

    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(12.dp).scale(finishScale)) {
        Box(modifier = Modifier.size(28.dp)) {
            Canvas(modifier = Modifier.size(28.dp)) {
                drawArc(
                    color = Color.White.copy(alpha = 0.25f),
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = Offset(0f, 0f),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3f)
                )
                drawArc(
                    color = if (state.finished) Color(0xFFFFCC00) else Color.White,
                    startAngle = -90f,
                    sweepAngle = 360f * progress,
                    useCenter = false,
                    topLeft = Offset(0f, 0f),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3f)
                )
            }
        }
        Spacer(Modifier.width(10.dp))
        Text(
            text = formatMmSs(remaining),
            color = Color.White,
            fontSize = 16.sp
        )
    }
}

private fun formatMmSs(ms: Long): String {
    val seconds = (ms / 1000).toInt()
    return "%02d:%02d".format(seconds / 60, seconds % 60)
}
