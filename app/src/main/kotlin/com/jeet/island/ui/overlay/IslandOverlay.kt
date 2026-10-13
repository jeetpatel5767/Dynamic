package com.jeet.island.ui.overlay

import android.view.HapticFeedbackConstants
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.jeet.island.cutout.CutoutGeometry
import com.jeet.island.service.OverlayController
import com.jeet.island.settings.GlassSettings
import com.jeet.island.settings.GlassSettingsStore
import com.jeet.island.state.IslandState
import com.jeet.island.ui.PillShape
import com.jeet.island.ui.SplitLayout
import com.jeet.island.ui.states.CallContent
import com.jeet.island.ui.states.ChargingContent
import com.jeet.island.ui.states.MediaContent
import com.jeet.island.ui.states.NotifContent
import com.jeet.island.ui.states.TimerContent

private val RESTING_W = 126.dp
private val RESTING_H = 37.dp
private val EXPANDED_W = 320.dp
private val EXPANDED_H = 84.dp
private val LARGE_W = 360.dp
private val LARGE_H = 110.dp

@Composable
fun IslandOverlay(geometry: CutoutGeometry?) {
    val context = LocalContext.current
    val view = LocalView.current
    val stateMachine = OverlayController.LocalStateMachine.current
    val islandState by stateMachine.state.collectAsState()
    val settings by GlassSettingsStore.flow(context).collectAsState(initial = GlassSettings())

    var tapExpanded by remember { mutableStateOf(false) }
    val targetW: Dp
    val targetH: Dp
    when {
        islandState is IslandState.Idle && !tapExpanded -> {
            targetW = RESTING_W; targetH = RESTING_H
        }
        islandState is IslandState.ExpandedCall -> {
            targetW = LARGE_W; targetH = LARGE_H
        }
        islandState is IslandState.ExpandedTimer && (islandState as IslandState.ExpandedTimer).finished -> {
            targetW = EXPANDED_W; targetH = EXPANDED_H
        }
        islandState is IslandState.Split -> {
            targetW = LARGE_W; targetH = EXPANDED_H
        }
        else -> {
            targetW = EXPANDED_W; targetH = EXPANDED_H
        }
    }
    val animW by animateDpAsState(targetW, spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMediumLow), label = "w")
    val animH by animateDpAsState(targetH, spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMediumLow), label = "h")

    LaunchedEffect(islandState) {
        if (islandState !is IslandState.Idle) tapExpanded = false
    }

    if (islandState is IslandState.Split) {
        SplitLayout(state = islandState as IslandState.Split, settings = settings, modifier = Modifier.padding(top = 2.dp))
        return
    }

    PillShape(
        width = animW,
        height = animH,
        settings = settings,
        modifier = Modifier
            .padding(top = 2.dp)
            .clickable {
                tapExpanded = !tapExpanded
                view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
            }
    ) {
        AnimatedContent(
            targetState = islandState to tapExpanded,
            transitionSpec = {
                (fadeIn() + scaleIn(initialScale = 0.95f)) togetherWith (fadeOut() + scaleOut(targetScale = 0.95f))
            },
            label = "island_content"
        ) { (state, forced) ->
            when (state) {
                is IslandState.Idle -> if (forced) Box(Modifier.padding(8.dp)) {} else Unit
                is IslandState.ExpandedNotif -> NotifContent(state)
                is IslandState.ExpandedMedia -> MediaContent(state)
                is IslandState.ExpandedTimer -> TimerContent(state)
                is IslandState.ExpandedCall -> CallContent(state)
                is IslandState.ChargingPulse -> ChargingContent(state)
                is IslandState.Split -> Unit
            }
        }
    }
}
