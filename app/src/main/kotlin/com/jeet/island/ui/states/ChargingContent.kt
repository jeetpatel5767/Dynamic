package com.jeet.island.ui.states

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jeet.island.state.IslandState

@Composable
fun ChargingContent(state: IslandState.ChargingPulse) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Icon(
            imageVector = Icons.Filled.BatteryChargingFull,
            contentDescription = null,
            tint = Color(0xFF4CAF50)
        )
        Spacer(Modifier.width(8.dp))
        Text("${state.batteryPercent}%", color = Color.White, fontSize = 14.sp)
    }
}
