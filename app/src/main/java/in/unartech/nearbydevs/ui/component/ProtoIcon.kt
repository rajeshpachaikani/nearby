package `in`.unartech.nearbydevs.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import `in`.unartech.nearbydevs.data.model.DeviceType
import `in`.unartech.nearbydevs.ui.theme.LocalProtocolColors
import `in`.unartech.nearbydevs.ui.theme.color
import `in`.unartech.nearbydevs.ui.theme.container

@Composable
fun ProtoIcon(
    protocol: DeviceType,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    iconSize: Dp = 22.dp,
) {
    val proto = LocalProtocolColors.current
    val bg = proto.container(protocol)
    val fg = proto.color(protocol)
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(14.dp))
            .background(bg),
        contentAlignment = Alignment.Center,
    ) {
        val icon = when (protocol) {
            DeviceType.BLE -> Icons.Filled.Bluetooth
            DeviceType.MDNS -> Icons.Filled.Wifi
            DeviceType.SSDP -> Icons.Filled.Public
        }
        Icon(
            imageVector = icon,
            contentDescription = protocol.name,
            tint = fg,
            modifier = Modifier.size(iconSize),
        )
    }
}
