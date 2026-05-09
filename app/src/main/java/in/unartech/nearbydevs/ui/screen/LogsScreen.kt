package `in`.unartech.nearbydevs.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import `in`.unartech.nearbydevs.data.model.LogEvent
import `in`.unartech.nearbydevs.data.model.LogTag
import `in`.unartech.nearbydevs.ui.component.FilterChip
import `in`.unartech.nearbydevs.ui.component.GhPill
import `in`.unartech.nearbydevs.ui.component.NbdAppBar
import `in`.unartech.nearbydevs.ui.component.SectionHeader
import `in`.unartech.nearbydevs.ui.theme.LocalProtocolColors
import `in`.unartech.nearbydevs.ui.theme.MonoStyle
import `in`.unartech.nearbydevs.ui.viewmodel.DiscoveryViewModel

private enum class LogFilter(val label: String, val tag: LogTag?) {
    All("All", null),
    BLE("BLE adv", LogTag.ADV),
    GATT("GATT", LogTag.GATT),
    MDNS("mDNS", LogTag.MDNS),
    SSDP("SSDP", LogTag.SSDP),
}

@Composable
fun LogsScreen(
    vm: DiscoveryViewModel,
    modifier: Modifier = Modifier,
) {
    val events by vm.logs.collectAsState()
    val paused by vm.logsPaused.collectAsState()
    var selected by remember { mutableStateOf(LogFilter.All) }

    val visible = remember(events, selected) {
        if (selected.tag == null) events else events.filter { it.tag == selected.tag }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp),
    ) {
        item {
            NbdAppBar(title = "Live log") {
                IconButton(
                    onClick = { vm.toggleLogsPaused() },
                    modifier = Modifier
                        .clip(MaterialTheme.shapes.extraLarge)
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                ) {
                    Icon(
                        imageVector = if (paused) Icons.Filled.PlayArrow else Icons.Filled.Pause,
                        contentDescription = if (paused) "Resume" else "Pause",
                    )
                }
                Spacer(Modifier.width(4.dp))
                IconButton(
                    onClick = { vm.clearLogs() },
                    modifier = Modifier
                        .clip(MaterialTheme.shapes.extraLarge)
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                ) {
                    Icon(Icons.Filled.DeleteOutline, contentDescription = "Clear")
                }
                Spacer(Modifier.width(4.dp))
                GhPill()
            }
        }
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                LogFilter.values().forEach { f ->
                    FilterChip(
                        label = f.label,
                        active = f == selected,
                        onClick = { selected = f },
                    )
                }
            }
        }
        item {
            SectionHeader(
                title = "Captured packets",
                aside = "${visible.size} events · ${if (paused) "paused" else "live"}",
            )
        }
        items(visible) { ev -> LogRow(ev) }
    }
}

@Composable
private fun LogRow(ev: LogEvent) {
    val proto = LocalProtocolColors.current
    val (bg, fg) = when (ev.tag) {
        LogTag.ADV -> proto.bleContainer to proto.ble
        LogTag.MDNS -> proto.mdnsContainer to proto.mdns
        LogTag.SSDP -> proto.ssdpContainer to proto.ssdp
        LogTag.GATT -> Color(0xFFD8F0DD) to Color(0xFF2C7A4E)
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Text(
            text = ev.time.removePrefix("12:"),
            style = MonoStyle.copy(fontSize = MaterialTheme.typography.labelSmall.fontSize),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(60.dp),
        )
        Box(
            modifier = Modifier
                .width(56.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(bg)
                .padding(vertical = 2.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = ev.tag.name,
                style = MonoStyle.copy(fontSize = MaterialTheme.typography.labelSmall.fontSize),
                color = fg,
                fontWeight = FontWeight.Bold,
            )
        }
        Text(
            text = ev.msg,
            style = MonoStyle.copy(fontSize = MaterialTheme.typography.labelSmall.fontSize),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(MaterialTheme.colorScheme.outlineVariant)
    )
}
