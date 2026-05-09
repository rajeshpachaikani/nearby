package `in`.unartech.nearbydevs.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import `in`.unartech.nearbydevs.data.model.UiDevice
import `in`.unartech.nearbydevs.ui.theme.MonoStyle
import `in`.unartech.nearbydevs.ui.theme.Success
import `in`.unartech.nearbydevs.ui.theme.SuccessDark

@Composable
fun DeviceCard(
    device: UiDevice,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isDark = isDarkTheme()
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        if (device.fresh) {
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(40.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(if (isDark) SuccessDark else Success)
            )
        }
        ProtoIcon(protocol = device.protocol)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = device.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(top = 2.dp),
            ) {
                Text(
                    text = device.protocol.label(),
                    style = MonoStyle.copy(fontSize = MaterialTheme.typography.labelSmall.fontSize),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Dot()
                Text(
                    text = device.mac.take(8) + "…",
                    style = MonoStyle.copy(fontSize = MaterialTheme.typography.labelSmall.fontSize),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                )
                Dot()
                Text(
                    text = device.vendor,
                    style = MonoStyle.copy(fontSize = MaterialTheme.typography.labelSmall.fontSize),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            IntentTag(text = device.intent)
        }
        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
            RssiBars(rssi = device.rssi)
            Text(
                text = if (device.rssi != null) "${device.rssi} dBm" else ":${device.port ?: "—"}",
                style = MonoStyle.copy(fontSize = MaterialTheme.typography.labelSmall.fontSize),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
fun IntentTag(text: String) {
    Box(
        modifier = Modifier
            .padding(top = 4.dp)
            .clip(RoundedCornerShape(999.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .padding(horizontal = 8.dp, vertical = 2.dp),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun Dot() {
    Box(
        modifier = Modifier
            .size(3.dp)
            .clip(RoundedCornerShape(50))
            .background(MaterialTheme.colorScheme.outline)
    )
}

@Composable
private fun isDarkTheme(): Boolean {
    val bg = MaterialTheme.colorScheme.background
    return 0.2126f * bg.red + 0.7152f * bg.green + 0.0722f * bg.blue < 0.4f
}

private fun `in`.unartech.nearbydevs.data.model.DeviceType.label(): String = when (this) {
    `in`.unartech.nearbydevs.data.model.DeviceType.BLE -> "BLE"
    `in`.unartech.nearbydevs.data.model.DeviceType.MDNS -> "mDNS"
    `in`.unartech.nearbydevs.data.model.DeviceType.SSDP -> "SSDP"
}
