package `in`.unartech.nearbydevs.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import `in`.unartech.nearbydevs.data.model.UiDevice
import `in`.unartech.nearbydevs.ui.component.GhPill
import `in`.unartech.nearbydevs.ui.component.NbdAppBar
import `in`.unartech.nearbydevs.ui.component.ProtoIcon
import `in`.unartech.nearbydevs.ui.component.SectionHeader
import `in`.unartech.nearbydevs.ui.theme.LocalProtocolColors
import `in`.unartech.nearbydevs.ui.theme.MonoStyle
import `in`.unartech.nearbydevs.ui.theme.color
import `in`.unartech.nearbydevs.ui.theme.container
import `in`.unartech.nearbydevs.ui.viewmodel.DiscoveryViewModel

@Composable
fun SavedScreen(
    vm: DiscoveryViewModel,
    onOpenDevice: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val devices by vm.devices.collectAsState()
    val faves = devices.filter { it.favorite }

    Column(modifier = modifier.fillMaxSize()) {
        NbdAppBar(title = "Saved") { GhPill() }
        SectionHeader(
            title = "${faves.size} pinned devices",
            aside = "re-resolve on tap",
        )
        if (faves.isEmpty()) {
            Empty()
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(faves) { d -> SavedTile(d, onClick = { onOpenDevice(d.id) }) }
            }
        }
    }
}

@Composable
private fun Empty() {
    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 32.dp, vertical = 60.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(
            imageVector = Icons.Outlined.StarOutline,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(40.dp),
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "No pinned devices yet",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = "Star a device from the Discover tab to access it quickly between scans.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )
    }
}

@Composable
private fun SavedTile(device: UiDevice, onClick: () -> Unit) {
    val proto = LocalProtocolColors.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        ProtoIcon(protocol = device.protocol)
        Text(
            text = device.name,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = device.mac,
            style = MonoStyle.copy(fontSize = MaterialTheme.typography.labelSmall.fontSize),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Box(
            modifier = Modifier
                .clip(CircleShape)
                .background(proto.container(device.protocol))
                .padding(horizontal = 8.dp, vertical = 3.dp),
        ) {
            val label = when (device.protocol) {
                `in`.unartech.nearbydevs.data.model.DeviceType.BLE -> "BLE"
                `in`.unartech.nearbydevs.data.model.DeviceType.MDNS -> "mDNS"
                `in`.unartech.nearbydevs.data.model.DeviceType.SSDP -> "SSDP"
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = proto.color(device.protocol),
            )
        }
    }
}
