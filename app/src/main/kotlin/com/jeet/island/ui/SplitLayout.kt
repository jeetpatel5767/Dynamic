package com.jeet.island.ui

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.jeet.island.settings.GlassSettings
import com.jeet.island.state.IslandState
import com.jeet.island.ui.states.MediaContent
import com.jeet.island.ui.states.NotifContent
import com.jeet.island.ui.states.TimerContent

@Composable
fun SplitLayout(state: IslandState.Split, settings: GlassSettings, modifier: Modifier = Modifier) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier) {
        PillShape(width = 200.dp, height = 60.dp, settings = settings) {
            RenderInner(state.leading)
        }
        Spacer(Modifier.width(8.dp))
        PillShape(width = 80.dp, height = 60.dp, settings = settings) {
            RenderInner(state.trailing)
        }
    }
}

@Composable
private fun RenderInner(s: IslandState) {
    when (s) {
        is IslandState.ExpandedMedia -> MediaContent(s)
        is IslandState.ExpandedTimer -> TimerContent(s)
        is IslandState.ExpandedNotif -> NotifContent(s)
        else -> Unit
    }
}
