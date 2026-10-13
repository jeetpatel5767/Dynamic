package com.jeet.island.ui.states

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jeet.island.call.CallController
import com.jeet.island.state.IslandState

@Composable
fun CallContent(state: IslandState.ExpandedCall) {
    val context = LocalContext.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp).fillMaxWidth()
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                state.displayName ?: state.number,
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                "Incoming call",
                color = Color.White.copy(alpha = 0.75f),
                fontSize = 11.sp
            )
        }
        IconButton(
            onClick = { CallController.decline(context) },
            modifier = Modifier.size(36.dp).clip(CircleShape).background(Color(0xFFD32F2F))
        ) {
            Icon(Icons.Filled.CallEnd, contentDescription = "Decline", tint = Color.White)
        }
        Spacer(Modifier.width(8.dp))
        IconButton(
            onClick = { CallController.answer(context) },
            modifier = Modifier.size(36.dp).clip(CircleShape).background(Color(0xFF2E7D32))
        ) {
            Icon(Icons.Filled.Call, contentDescription = "Answer", tint = Color.White)
        }
    }
}
