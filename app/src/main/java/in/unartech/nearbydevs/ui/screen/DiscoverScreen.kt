package `in`.unartech.nearbydevs.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import `in`.unartech.nearbydevs.data.model.DeviceType
import `in`.unartech.nearbydevs.data.model.UiDevice
import `in`.unartech.nearbydevs.ui.component.DeviceCard
import `in`.unartech.nearbydevs.ui.component.FilterChip
import `in`.unartech.nearbydevs.ui.component.GhPill
import `in`.unartech.nearbydevs.ui.component.NbdAppBar
import `in`.unartech.nearbydevs.ui.component.ScanBanner
import `in`.unartech.nearbydevs.ui.component.SearchField
import `in`.unartech.nearbydevs.ui.component.SectionHeader
import `in`.unartech.nearbydevs.ui.viewmodel.DiscoveryViewModel
import `in`.unartech.nearbydevs.ui.viewmodel.ProtocolFilter

@Composable
fun DiscoverScreen(
    vm: DiscoveryViewModel,
    onOpenDevice: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val devices by vm.devices.collectAsState()
    val scanning by vm.scanning.collectAsState()
    val filter by vm.filter.collectAsState()
    val query by vm.query.collectAsState()
    val pps by vm.packetsPerSec.collectAsState()

    val counts = remember(devices) { computeCounts(devices) }
    val filtered = remember(devices, filter, query) {
        devices.asSequence().filter { d ->
            val matchProto = filter.type == null || d.protocol == filter.type
            val q = query.lowercase()
            val matchQ = q.isBlank() ||
                d.name.lowercase().contains(q) ||
                d.mac.lowercase().contains(q) ||
                d.vendor.lowercase().contains(q)
            matchProto && matchQ
        }.sortedByDescending { it.rssi ?: -100 }.toList()
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp),
    ) {
        item { NbdAppBar(title = "Nearby") { GhPill() } }
        item {
            ScanBanner(
                scanning = scanning,
                totalSeen = devices.size,
                packetsPerSec = pps,
                onToggle = { vm.toggleScan() },
                modifier = Modifier.padding(top = 4.dp, bottom = 14.dp),
            )
        }
        item {
            SearchField(
                value = query,
                onValueChange = vm::setQuery,
                placeholder = "Filter by name, MAC, vendor…",
                modifier = Modifier.padding(bottom = 8.dp),
            )
        }
        item { ChipsRow(filter, counts) { vm.setFilter(it) } }
        item {
            SectionHeader(
                title = "Live · ${filtered.size} devices",
                aside = "sorted: rssi ↓",
            )
        }
        items(filtered, key = { it.id }) { d ->
            DeviceCard(
                device = d,
                onClick = { onOpenDevice(d.id) },
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 3.dp),
            )
        }
        item { Spacer(Modifier.height(24.dp)) }
    }
}

@Composable
private fun ChipsRow(
    selected: ProtocolFilter,
    counts: Map<ProtocolFilter, Int>,
    onSelect: (ProtocolFilter) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        ProtocolFilter.values().forEach { p ->
            FilterChip(
                label = p.label,
                count = counts[p],
                active = p == selected,
                onClick = { onSelect(p) },
            )
        }
        Row(
            modifier = Modifier
                .clip(CircleShape)
                .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outline), CircleShape)
                .clickable { /* future advanced filter */ }
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Icon(
                Icons.Filled.FilterList,
                contentDescription = "Filters",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = "Filters",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private fun computeCounts(devices: List<UiDevice>): Map<ProtocolFilter, Int> = mapOf(
    ProtocolFilter.All to devices.size,
    ProtocolFilter.BLE to devices.count { it.protocol == DeviceType.BLE },
    ProtocolFilter.MDNS to devices.count { it.protocol == DeviceType.MDNS },
    ProtocolFilter.SSDP to devices.count { it.protocol == DeviceType.SSDP },
)
