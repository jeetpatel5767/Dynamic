package com.jeet.island.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jeet.island.timer.TimerService

@Composable
fun TimerScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    fun start(ms: Long) {
        context.startForegroundService(TimerService.startIntent(context, ms))
    }
    Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
        Text("Timer test", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { start(10_000L) }) { Text("10 s") }
            Button(onClick = { start(60_000L) }) { Text("1 min") }
            Button(onClick = { start(2 * 60_000L) }) { Text("2 min") }
        }
        Spacer(Modifier.height(12.dp))
        OutlinedButton(onClick = {
            context.startService(TimerService.cancelIntent(context))
        }) { Text("Cancel timer") }
        Spacer(Modifier.height(16.dp))
        Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("Back") }
    }
}
