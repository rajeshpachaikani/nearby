package `in`.unartech.nearbydevs.ui.screen

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import java.io.File
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.ExtendedFloatingActionButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import `in`.unartech.nearbydevs.data.model.CharDef
import `in`.unartech.nearbydevs.data.model.DeviceType
import `in`.unartech.nearbydevs.data.model.ServiceDef
import `in`.unartech.nearbydevs.data.model.ServiceTag
import `in`.unartech.nearbydevs.data.model.UiDevice
import `in`.unartech.nearbydevs.ui.component.HexBlock
import `in`.unartech.nearbydevs.ui.component.MonoLines
import `in`.unartech.nearbydevs.ui.component.ProtoIcon
import `in`.unartech.nearbydevs.ui.component.SectionHeader
import `in`.unartech.nearbydevs.ui.component.SourceTip
import `in`.unartech.nearbydevs.ui.viewmodel.DiscoveryViewModel
import `in`.unartech.nearbydevs.ui.theme.MonoStyle

private enum class DetailTab(val label: String) { Services("Services"), Info("Info"), Raw("Raw") }

@Composable
fun DeviceDetailScreen(
    vm: DiscoveryViewModel,
    deviceId: String,
    onBack: () -> Unit,
    onConnect: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val devices by vm.devices.collectAsState()
    val device = remember(devices, deviceId) { devices.find { it.id == deviceId } }
    if (device == null) {
        Text("Device not found")
        return
    }

    val tabs = remember(device.protocol) {
        if (device.protocol == DeviceType.BLE) listOf(DetailTab.Services, DetailTab.Raw)
        else listOf(DetailTab.Info, DetailTab.Raw)
    }
    var tab by remember(device.protocol) { mutableStateOf(tabs[0]) }
    val ctx = LocalContext.current

    Box(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface)) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
            contentPadding = PaddingValues(bottom = 120.dp),
        ) {
            item {
                Topbar(
                    title = device.name,
                    isFave = device.favorite,
                    onBack = onBack,
                    onToggleFave = { vm.toggleFavorite(device.id) },
                    onShare = { shareDeviceInfo(ctx, device) },
                )
            }
            item { Hero(device) }
            item { Tabs(tabs = tabs, selected = tab, onSelect = { tab = it }) }
            when (tab) {
                DetailTab.Services -> servicesContent(device)
                DetailTab.Info -> infoContent(device)
                DetailTab.Raw -> rawContent(device)
            }
        }
        if (device.protocol == DeviceType.BLE && device.connectable) {
            ExtendedFloatingActionButton(
                onClick = onConnect,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .navigationBarsPadding()
                    .padding(end = 18.dp, bottom = 24.dp),
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("Connect & inspect", fontWeight = FontWeight.SemiBold) },
            )
        }
    }
}

@Composable
private fun Topbar(title: String, isFave: Boolean, onBack: () -> Unit, onToggleFave: () -> Unit, onShare: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 8.dp, end = 8.dp, top = 8.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        IconButton(onClick = onToggleFave) {
            Icon(
                imageVector = if (isFave) Icons.Filled.Star else Icons.Outlined.StarOutline,
                contentDescription = "Pin",
                tint = if (isFave) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        IconButton(onClick = onShare) {
            Icon(Icons.Filled.Share, contentDescription = "Share")
        }
    }
}

private fun buildDeviceInfoText(d: UiDevice): String = buildString {
    appendLine("Nearby — device info")
    appendLine("====================")
    appendLine("Name      : ${d.name}")
    appendLine("Protocol  : ${d.protocol}")
    appendLine("ID        : ${d.id}")
    appendLine("MAC/Host  : ${d.mac}")
    appendLine("Vendor    : ${d.vendor}")
    appendLine("Intent    : ${d.intent}")
    d.rssi?.let { appendLine("RSSI      : $it dBm") }
    d.txPowerDbm?.let { appendLine("TX power  : $it dBm") }
    d.advIntervalMs?.let { appendLine("Adv int   : ${it} ms") }
    appendLine("Connectable: ${d.connectable}")
    if (d.services.isNotEmpty()) {
        appendLine("Services  :")
        d.services.forEach { appendLine("  - $it") }
    }
    d.port?.let { appendLine("Port      : $it") }
    d.ipv4?.let { appendLine("IPv4      : $it") }
    if (d.txt.isNotEmpty()) {
        appendLine("TXT       :")
        d.txt.forEach { (k, v) -> appendLine("  $k=$v") }
    }
    d.location?.let { appendLine("LOCATION  : $it") }
    d.server?.let { appendLine("SERVER    : $it") }
    appendLine("Seen at   : ${d.seenAt}")
    if (d.gattServices.isNotEmpty()) {
        appendLine()
        appendLine("GATT services")
        appendLine("-------------")
        d.gattServices.forEach { svc ->
            appendLine("• ${svc.name}  (${svc.uuid})  [${svc.tag}]")
            svc.chars.forEach { c ->
                val flags = listOfNotNull(
                    if (c.flags.r) "R" else null,
                    if (c.flags.w) "W" else null,
                    if (c.flags.n) "N" else null,
                ).joinToString("")
                appendLine("    - ${c.name}  ${c.uuid}  [$flags]")
            }
        }
    }
    d.rawAdv?.let { raw ->
        appendLine()
        appendLine("Raw adv (${raw.size} B)")
        appendLine(raw.joinToString(" ") { (it.toInt() and 0xFF).toString(16).uppercase().padStart(2, '0') })
    }
    d.rawXml?.let {
        appendLine()
        appendLine("SSDP description XML")
        appendLine(it)
    }
}

private fun shareDeviceInfo(ctx: Context, device: UiDevice) {
    val dir = File(ctx.cacheDir, "shared").apply { mkdirs() }
    val file = File(dir, "device_info.txt")
    file.writeText(buildDeviceInfoText(device))
    val uri = FileProvider.getUriForFile(ctx, "${ctx.packageName}.fileprovider", file)
    val send = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_STREAM, uri)
        putExtra(Intent.EXTRA_SUBJECT, "Nearby device: ${device.name}")
        putExtra(Intent.EXTRA_TEXT, "Device info attached.")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    val chooser = Intent.createChooser(send, "Share device info").apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    ctx.startActivity(chooser)
}

@Composable
private fun Hero(device: UiDevice) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    listOf(MaterialTheme.colorScheme.surfaceContainerLow, MaterialTheme.colorScheme.surface)
                )
            )
            .padding(horizontal = 16.dp, vertical = 18.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            ProtoIcon(protocol = device.protocol)
            Column {
                Text(
                    text = device.name,
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold,
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(top = 4.dp),
                ) {
                    Icon(
                        Icons.Filled.Memory,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(12.dp),
                    )
                    Text(
                        text = device.mac,
                        style = MonoStyle,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
        Spacer(Modifier.height(14.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            StatCard(
                label = "RSSI",
                value = device.rssi?.toString() ?: "—",
                modifier = Modifier.weight(1f),
            )
            StatCard(
                label = if (device.protocol == DeviceType.BLE) "Adv int" else "Port",
                value = if (device.protocol == DeviceType.BLE)
                    device.advIntervalMs?.let { "${it}ms" } ?: "—"
                else device.port?.toString() ?: "—",
                modifier = Modifier.weight(1f),
            )
            StatCard(
                label = if (device.protocol == DeviceType.BLE) "TX pwr" else "IPv4",
                value = if (device.protocol == DeviceType.BLE)
                    device.txPowerDbm?.let { "$it dBm" } ?: "—"
                else device.ipv4 ?: "—",
                modifier = Modifier.weight(1f),
                small = device.protocol != DeviceType.BLE,
            )
        }
    }
}

@Composable
private fun StatCard(label: String, value: String, modifier: Modifier = Modifier, small: Boolean = false) {
    Column(
        modifier = modifier
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(horizontal = 10.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = value,
            style = if (small) MonoStyle else MonoStyle.copy(fontSize = MaterialTheme.typography.titleMedium.fontSize),
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun Tabs(tabs: List<DetailTab>, selected: DetailTab, onSelect: (DetailTab) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(top = 6.dp)) {
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp)) {
            tabs.forEach { t ->
                val active = t == selected
                Column(
                    modifier = Modifier
                        .clickable { onSelect(t) }
                        .padding(horizontal = 14.dp),
                ) {
                    Text(
                        text = t.label,
                        modifier = Modifier.padding(top = 12.dp, bottom = 12.dp),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Box(
                        modifier = Modifier
                            .height(3.dp)
                            .fillMaxWidth()
                            .background(
                                if (active) MaterialTheme.colorScheme.primary
                                else androidx.compose.ui.graphics.Color.Transparent
                            )
                    )
                }
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(MaterialTheme.colorScheme.outlineVariant)
        )
    }
}

private fun LazyListScope.servicesContent(device: UiDevice) {
    val services = device.gattServices
    if (services.isEmpty()) {
        item { ServicesEmptyState(device) }
    } else {
        items(services) { svc -> ServiceCard(svc) }
    }
    item { Spacer(Modifier.height(8.dp)) }
    item {
        SourceTip(
            file = "app/src/main/java/.../BleScanner.kt",
            line = 87,
        )
    }
}

@Composable
private fun ServicesEmptyState(device: UiDevice) {
    androidx.compose.foundation.layout.Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp)
            .clip(MaterialTheme.shapes.large)
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .padding(16.dp),
    ) {
        if (device.services.isNotEmpty()) {
            Text(
                text = "Advertised UUIDs",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(6.dp))
            device.services.forEach {
                Text(
                    text = it.uppercase(),
                    style = MonoStyle,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(vertical = 2.dp),
                )
            }
            Spacer(Modifier.height(10.dp))
        }
        Text(
            text = if (device.connectable)
                "Tap Connect & inspect to discover GATT services and characteristics."
            else "Device advertises non-connectable. No GATT discovery possible.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

private fun LazyListScope.infoContent(device: UiDevice) {
    item { InfoCard(device) }
    item { Spacer(Modifier.height(8.dp)) }
    item {
        val file = if (device.protocol == DeviceType.MDNS)
            "app/src/main/java/.../MdnsScanner.kt"
        else "app/src/main/java/.../SsdpScanner.kt"
        SourceTip(file = file)
    }
}

private fun LazyListScope.rawContent(device: UiDevice) {
    item {
        SectionHeader(
            title = when (device.protocol) {
                DeviceType.BLE -> "Adv payload (${device.rawAdv?.size ?: 0} B)"
                DeviceType.MDNS -> "DNS-SD record"
                DeviceType.SSDP -> "SSDP NOTIFY"
            },
            aside = "captured",
        )
    }
    item {
        when (device.protocol) {
            DeviceType.BLE -> {
                val raw = device.rawAdv
                if (raw == null || raw.isEmpty()) {
                    MonoLines(lines = listOf("(no scan record captured)"))
                } else {
                    HexBlock(rows = formatHexRows(raw))
                }
            }
            DeviceType.MDNS -> MonoLines(
                lines = buildList {
                    add("PTR  ${device.intent}.local → ${device.name}")
                    add("SRV  ${device.name} 0 0 ${device.port ?: 0} ${device.mac}")
                    if (device.txt.isNotEmpty()) {
                        add("TXT  ${device.txt.entries.joinToString("  ") { "${it.key}=${it.value}" }}")
                    }
                    add("A    ${device.name} A ${device.ipv4 ?: "?"}")
                }
            )
            DeviceType.SSDP -> MonoLines(
                lines = listOf(
                    "NOTIFY * HTTP/1.1",
                    "HOST: 239.255.255.250:1900",
                    "LOCATION: ${device.location ?: "?"}",
                    "SERVER: ${device.server ?: "?"}",
                    "NT: ${device.intent}",
                    "NTS: ssdp:alive",
                    "USN: ${device.id.removePrefix("ssdp:")}",
                )
            )
        }
    }
    item { SectionHeader(title = "Decoded") }
    item {
        Column(modifier = Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            when (device.protocol) {
                DeviceType.BLE -> {
                    decodeAdvPayload(device.rawAdv).forEach { (bytes, desc) -> DecodedLine(bytes, desc) }
                }
                DeviceType.MDNS -> DecodedLine("—", "Resolved by DNS-SD on 224.0.0.251:5353")
                DeviceType.SSDP -> {
                    DecodedLine("—", "Discovered via SSDP M-SEARCH on 239.255.255.250:1900")
                    if (device.rawXml != null) DecodedLine("XML", "Description fetched from LOCATION (${device.rawXml.length} bytes)")
                }
            }
        }
    }
}

private fun formatHexRows(bytes: ByteArray): List<String> {
    val rows = mutableListOf<String>()
    var i = 0
    while (i < bytes.size) {
        val end = (i + 8).coerceAtMost(bytes.size)
        rows.add(
            (i until end).joinToString(" ") { idx ->
                (bytes[idx].toInt() and 0xFF).toString(16).uppercase().padStart(2, '0')
            }
        )
        i = end
    }
    return rows
}

// Decode common BLE advertising AD structures (length-type-value triples).
private fun decodeAdvPayload(bytes: ByteArray?): List<Pair<String, String>> {
    if (bytes == null || bytes.isEmpty()) return listOf("—" to "(no scan record)")
    val out = mutableListOf<Pair<String, String>>()
    var i = 0
    while (i < bytes.size) {
        val len = bytes[i].toInt() and 0xFF
        if (len == 0 || i + len >= bytes.size) break
        val type = bytes[i + 1].toInt() and 0xFF
        val payloadStart = i + 2
        val payloadEnd = i + 1 + len
        val payload = bytes.copyOfRange(payloadStart, payloadEnd)
        val hex = (i..payloadEnd - 1).joinToString(" ") { idx ->
            (bytes[idx].toInt() and 0xFF).toString(16).uppercase().padStart(2, '0')
        }
        val desc = describeAdType(type, payload)
        out.add(hex to desc)
        i = payloadEnd
    }
    if (out.isEmpty()) out.add("—" to "(unparseable scan record)")
    return out
}

private fun describeAdType(type: Int, payload: ByteArray): String = when (type) {
    0x01 -> "Flags · 0x${(payload.firstOrNull()?.toInt()?.and(0xFF) ?: 0).toString(16).uppercase()}"
    0x02, 0x03 -> "Incomplete/complete list of 16-bit UUIDs · " +
        payload.toList().chunked(2).joinToString(",") { (lo, hi) ->
            "0x" + ((hi.toInt() and 0xFF) shl 8 or (lo.toInt() and 0xFF))
                .toString(16).uppercase().padStart(4, '0')
        }
    0x06, 0x07 -> "List of 128-bit UUIDs (${payload.size / 16} entries)"
    0x08 -> "Shortened local name · \"${payload.toString(Charsets.UTF_8)}\""
    0x09 -> "Complete local name · \"${payload.toString(Charsets.UTF_8)}\""
    0x0A -> "TX Power Level · ${payload.firstOrNull()?.toInt()} dBm"
    0x12 -> "Slave conn interval range"
    0x16 -> "Service Data (16-bit UUID)"
    0x19 -> "Appearance"
    0xFF -> "Manufacturer Specific Data (${payload.size} bytes)"
    else -> "AD type 0x${type.toString(16).uppercase().padStart(2, '0')} (${payload.size} bytes)"
}

@Composable
private fun ServiceCard(s: ServiceDef) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clip(MaterialTheme.shapes.large)
            .background(MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = s.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = s.uuid,
                    style = MonoStyle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            ServiceTagPill(s.tag)
        }
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .padding(bottom = 12.dp),
        ) {
            s.chars.forEachIndexed { i, c ->
                if (i > 0) Spacer(Modifier.height(6.dp))
                CharRow(c)
            }
        }
    }
}

@Composable
private fun ServiceTagPill(tag: ServiceTag) {
    val (bg, fg) = if (tag == ServiceTag.STD)
        MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
    else MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.onTertiaryContainer
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(bg)
            .padding(horizontal = 8.dp, vertical = 3.dp),
    ) {
        Text(
            text = tag.name,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = fg,
        )
    }
}

@Composable
private fun CharRow(c: CharDef) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Text(text = c.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
        Text(text = c.uuid, style = MonoStyle, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(
            modifier = Modifier.padding(top = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            if (c.flags.r) FlagPill("READ", MaterialTheme.colorScheme.secondaryContainer, MaterialTheme.colorScheme.onSecondaryContainer)
            if (c.flags.w) FlagPill("WRITE", MaterialTheme.colorScheme.tertiaryContainer, MaterialTheme.colorScheme.onTertiaryContainer)
            if (c.flags.n) FlagPill("NOTIFY", MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.onPrimaryContainer)
        }
    }
}

@Composable
private fun FlagPill(label: String, bg: androidx.compose.ui.graphics.Color, fg: androidx.compose.ui.graphics.Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(5.dp))
            .background(bg)
            .padding(horizontal = 6.dp, vertical = 2.dp),
    ) {
        Text(text = label, style = MonoStyle.copy(fontSize = MaterialTheme.typography.labelSmall.fontSize), fontWeight = FontWeight.Bold, color = fg)
    }
}

@Composable
private fun InfoCard(device: UiDevice) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(MaterialTheme.shapes.large)
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = if (device.protocol == DeviceType.MDNS) "Service record" else "Device description",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
        if (device.protocol == DeviceType.MDNS) {
            KvRow("Type", device.intent)
            KvRow("Host", device.name)
            KvRow("Port", device.port?.toString() ?: "—")
            KvRow("IPv4", device.ipv4 ?: "—")
            device.txt.forEach { (k, v) -> KvRow("TXT $k", v) }
        } else {
            KvRow("ST", device.intent)
            KvRow("LOCATION", device.location ?: "—", small = true)
            KvRow("SERVER", device.server ?: "—", small = true)
            KvRow("USN", "uuid:${device.id}::urn:upnp-org", small = true)
        }
    }
}

@Composable
private fun KvRow(k: String, v: String, small: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = k.uppercase(),
            modifier = Modifier
                .width(84.dp)
                .padding(top = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = v,
            style = if (small) MonoStyle.copy(fontSize = MaterialTheme.typography.labelSmall.fontSize) else MonoStyle,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun DecodedLine(bytes: String, desc: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
    ) {
        Text(text = bytes, style = MonoStyle, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.secondary)
        Text(text = desc, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
    }
}

