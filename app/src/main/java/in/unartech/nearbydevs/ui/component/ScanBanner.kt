package `in`.unartech.nearbydevs.ui.component

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SettingsInputAntenna
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import `in`.unartech.nearbydevs.ui.theme.MonoStyle

@Composable
fun ScanBanner(
    scanning: Boolean,
    totalSeen: Int,
    packetsPerSec: Int,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val grad = Brush.linearGradient(
        listOf(
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.tertiaryContainer,
        )
    )
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(MaterialTheme.shapes.large)
            .background(grad)
            .padding(18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        PulseMark(scanning = scanning)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = if (scanning) "Scanning all radios" else "Scan paused",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = "$totalSeen seen · $packetsPerSec pkt/s · BLE + mDNS + SSDP",
                style = MonoStyle.copy(fontSize = MaterialTheme.typography.labelMedium.fontSize),
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f),
            )
        }
        Row(
            modifier = Modifier
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface)
                .clickable { onToggle() }
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            Icon(
                imageVector = if (scanning) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(14.dp),
            )
            Text(
                text = if (scanning) "Pause" else "Resume",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
private fun PulseMark(scanning: Boolean) {
    Box(modifier = Modifier.size(64.dp), contentAlignment = Alignment.Center) {
        if (scanning) {
            val transition = rememberInfiniteTransition(label = "pulse")
            val s1 by transition.animateFloat(
                initialValue = 0.95f, targetValue = 1.7f,
                animationSpec = infiniteRepeatable(
                    animation = tween(2400, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart,
                ),
                label = "s1",
            )
            val a1 by transition.animateFloat(
                initialValue = 0.5f, targetValue = 0f,
                animationSpec = infiniteRepeatable(
                    animation = tween(2400, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart,
                ),
                label = "a1",
            )
            val s2 by transition.animateFloat(
                initialValue = 0.95f, targetValue = 1.7f,
                animationSpec = infiniteRepeatable(
                    animation = tween(2400, easing = LinearEasing, delayMillis = 1200),
                    repeatMode = RepeatMode.Restart,
                ),
                label = "s2",
            )
            val a2 by transition.animateFloat(
                initialValue = 0.5f, targetValue = 0f,
                animationSpec = infiniteRepeatable(
                    animation = tween(2400, easing = LinearEasing, delayMillis = 1200),
                    repeatMode = RepeatMode.Restart,
                ),
                label = "a2",
            )
            Ring(scale = s1, alpha = a1)
            Ring(scale = s2, alpha = a2)
        }
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.SettingsInputAntenna,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp),
            )
        }
    }
}

@Composable
private fun Ring(scale: Float, alpha: Float) {
    val ringColor = MaterialTheme.colorScheme.primary.copy(alpha = alpha)
    Canvas(modifier = Modifier.size(60.dp).scale(scale)) {
        drawCircle(color = ringColor, style = Stroke(width = 4f))
    }
}
