package `in`.unartech.nearbydevs.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import `in`.unartech.nearbydevs.data.model.DeviceType

// BLE = violet, mDNS = cyan, SSDP = peach.
data class ProtocolColors(
    val ble: Color,
    val bleContainer: Color,
    val mdns: Color,
    val mdnsContainer: Color,
    val ssdp: Color,
    val ssdpContainer: Color,
)

val ProtocolColorsLight = ProtocolColors(
    ble = Color(0xFF7656E8),
    bleContainer = Color(0xFFE7E0FF),
    mdns = Color(0xFF1FA8C2),
    mdnsContainer = Color(0xFFD7F2F8),
    ssdp = Color(0xFFE89766),
    ssdpContainer = Color(0xFFFCE5CF),
)

val ProtocolColorsDark = ProtocolColors(
    ble = Color(0xFFBAA8F2),
    bleContainer = Color(0xFF4D38A4),
    mdns = Color(0xFF7BD1E0),
    mdnsContainer = Color(0xFF1F5560),
    ssdp = Color(0xFFEFC1A0),
    ssdpContainer = Color(0xFF6E4318),
)

@Composable
@ReadOnlyComposable
fun ProtocolColors.color(type: DeviceType): Color = when (type) {
    DeviceType.BLE -> ble
    DeviceType.MDNS -> mdns
    DeviceType.SSDP -> ssdp
}

@Composable
@ReadOnlyComposable
fun ProtocolColors.container(type: DeviceType): Color = when (type) {
    DeviceType.BLE -> bleContainer
    DeviceType.MDNS -> mdnsContainer
    DeviceType.SSDP -> ssdpContainer
}
