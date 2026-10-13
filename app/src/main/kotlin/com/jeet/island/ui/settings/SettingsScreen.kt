package com.jeet.island.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jeet.island.settings.GlassPreset
import com.jeet.island.settings.GlassSettings
import com.jeet.island.settings.GlassSettingsStore
import com.jeet.island.ui.PillShape
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val settings by GlassSettingsStore.flow(context).collectAsState(initial = GlassSettings())

    fun write(update: GlassSettings) {
        scope.launch { GlassSettingsStore.write(context, update) }
    }

    Column(
        modifier = Modifier.verticalScroll(rememberScrollState()).padding(16.dp).fillMaxWidth()
    ) {
        Text("Glass Settings", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))

        Box(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            PillShape(width = 220.dp, height = 60.dp, settings = settings) {
                Text("preview", color = Color.White, fontSize = 12.sp)
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Glass enabled", modifier = Modifier.weight(1f))
            Switch(
                checked = settings.glassEnabled,
                onCheckedChange = { write(settings.copy(glassEnabled = it)) }
            )
        }

        Spacer(Modifier.height(12.dp))
        Text("Presets", fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            GlassPreset.entries.forEach { preset ->
                AssistChip(
                    onClick = { write(preset.settings) },
                    label = { Text(preset.label, fontSize = 11.sp) }
                )
            }
        }

        Spacer(Modifier.height(16.dp))
        LabeledSlider("Opacity", settings.glassOpacity, 0.30f..0.85f) {
            write(settings.copy(glassOpacity = it))
        }
        LabeledSlider("Border", settings.borderAlpha, 0f..0.20f) {
            write(settings.copy(borderAlpha = it))
        }
        LabeledSlider("Highlight", settings.highlightStrength, 0f..1f) {
            write(settings.copy(highlightStrength = it))
        }
        LabeledSlider("Noise", settings.noiseAmount, 0f..0.10f) {
            write(settings.copy(noiseAmount = it))
        }
        LabeledSlider("Tint hue", settings.tintHue, 0f..360f) {
            write(settings.copy(tintHue = it))
        }
        LabeledSlider("Corner smoothing", settings.cornerSmoothing, 0f..1f) {
            write(settings.copy(cornerSmoothing = it))
        }

        Spacer(Modifier.height(16.dp))
        Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text("Back")
        }
    }
}

@Composable
private fun LabeledSlider(
    label: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    onChange: (Float) -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 4.dp).fillMaxWidth()) {
        Row {
            Text(label, modifier = Modifier.weight(1f))
            Text("%.2f".format(value))
        }
        Slider(value = value, onValueChange = onChange, valueRange = range)
    }
}
