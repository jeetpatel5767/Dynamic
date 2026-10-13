package com.jeet.island.permissions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PermissionsScreen(
    refreshToken: Int,
    onOpenSettings: () -> Unit,
    onStartOverlay: () -> Unit
) {
    val context = LocalContext.current
    val perms = RequiredPermission.entries
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text("Dynamic Island", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(4.dp))
        Text(
            "Grant each permission below, then start the overlay.",
            fontSize = 14.sp,
            color = Color.DarkGray
        )
        Spacer(Modifier.height(16.dp))

        // refreshToken is read so recomposition picks up newly granted permissions.
        @Suppress("UNUSED_EXPRESSION") refreshToken

        perms.forEach { p ->
            val granted = PermissionChecker.check(context, p)
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (granted) Color(0xFFDCEDC8) else Color(0xFFFFF3E0)
                )
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(p.label, fontWeight = FontWeight.SemiBold)
                        Text(if (granted) "Granted" else "Not granted", fontSize = 12.sp)
                    }
                    OutlinedButton(onClick = { PermissionActions.open(context, p) }) {
                        Text(if (granted) "Reopen" else "Grant")
                    }
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        val allGranted = PermissionChecker.allGranted(context)
        Button(
            onClick = onStartOverlay,
            enabled = PermissionChecker.criticalGranted(context),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (allGranted) "Start Dynamic Island" else "Start (with granted perms)")
        }
        Spacer(Modifier.height(8.dp))
        OutlinedButton(onClick = onOpenSettings, modifier = Modifier.fillMaxWidth()) {
            Text("Open Glass Settings")
        }
    }
}
