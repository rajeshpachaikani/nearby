package `in`.unartech.nearbydevs.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import `in`.unartech.nearbydevs.ui.theme.MonoStyle
import `in`.unartech.nearbydevs.ui.theme.Success
import `in`.unartech.nearbydevs.ui.theme.SuccessDark

@Composable
fun RssiBars(
    rssi: Int?,
    modifier: Modifier = Modifier,
) {
    if (rssi == null) {
        Text(
            text = "—",
            style = MonoStyle,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = modifier,
        )
        return
    }
    val onCount = when {
        rssi >= -50 -> 5
        rssi >= -60 -> 4
        rssi >= -70 -> 3
        rssi >= -80 -> 2
        else -> 1
    }
    val onColor = when {
        rssi >= -55 -> if (MaterialTheme.colorScheme.onSurface == Color.White || isDark()) SuccessDark else Success
        rssi <= -78 -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    val offColor = MaterialTheme.colorScheme.outlineVariant
    val heights = listOf(4.dp, 7.dp, 10.dp, 13.dp, 16.dp)
    Row(
        modifier = modifier.height(16.dp),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        heights.forEachIndexed { i, h ->
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(h)
                    .clip(RoundedCornerShape(1.dp))
                    .background(if (i < onCount) onColor else offColor)
            )
        }
    }
}

@Composable
private fun isDark(): Boolean = MaterialTheme.colorScheme.background.luminanceLow()

private fun Color.luminanceLow(): Boolean {
    val r = red; val g = green; val b = blue
    return 0.2126f * r + 0.7152f * g + 0.0722f * b < 0.4f
}
