package `in`.unartech.nearbydevs.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.foundation.clickable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import `in`.unartech.nearbydevs.data.model.ScanConfig
import `in`.unartech.nearbydevs.data.model.ThemeMode
import `in`.unartech.nearbydevs.ui.component.GhPill
import `in`.unartech.nearbydevs.ui.component.NEARBY_REPO_URL
import `in`.unartech.nearbydevs.ui.component.NbdAppBar
import `in`.unartech.nearbydevs.ui.component.SectionHeader
import `in`.unartech.nearbydevs.ui.theme.MonoStyle
import `in`.unartech.nearbydevs.ui.viewmodel.DiscoveryViewModel

private val RepoBg = Color(0xFF1F1A2C)
private val RepoFg = Color(0xFFE5E0F0)
private val RepoButtonBg = Color(0xFFE5E0F0)
private val RepoButtonFg = Color(0xFF1F1A2C)

@Composable
fun SettingsScreen(
    vm: DiscoveryViewModel,
    modifier: Modifier = Modifier,
) {
    val cfg by vm.scanCfg.collectAsState()
    val themeMode by vm.themeMode.collectAsState()
    val uriHandler = LocalUriHandler.current
    val openRepo: () -> Unit = { uriHandler.openUri(NEARBY_REPO_URL) }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp),
    ) {
        item { NbdAppBar(title = "Settings") { GhPill(onClick = openRepo) } }
        item { RepoCard(onOpenRepo = openRepo) }
        item { SectionHeader(title = "Appearance") }
        item {
            SettingGroup {
                SwitchRow(
                    title = "Dark theme",
                    description = "Follows system by default",
                    checked = themeMode == ThemeMode.Dark,
                    onChange = { vm.setThemeMode(if (it) ThemeMode.Dark else ThemeMode.Light) },
                )
            }
        }
        item { SectionHeader(title = "BLE scan") }
        item {
            SettingGroup {
                SliderRow(
                    title = "Scan window",
                    description = "Active scan window (LE 1M PHY)",
                    value = cfg.windowMs.toFloat(),
                    range = 100f..4096f,
                    suffix = "ms",
                    onChange = { vm.setScanConfig(cfg.copy(windowMs = it.toInt())) },
                )
                Divider()
                SliderRow(
                    title = "Scan interval",
                    description = "Time between scan windows",
                    value = cfg.intervalMs.toFloat(),
                    range = 100f..10240f,
                    suffix = "ms",
                    onChange = { vm.setScanConfig(cfg.copy(intervalMs = it.toInt())) },
                )
                Divider()
                SwitchRow(
                    title = "Active scan (SCAN_REQ)",
                    description = "Solicits scan response packets — drains battery",
                    checked = cfg.activeScan,
                    onChange = { vm.setScanConfig(cfg.copy(activeScan = it)) },
                )
                Divider()
                SwitchRow(
                    title = "Allow duplicates",
                    description = "Report every adv even if MAC seen before",
                    checked = cfg.allowDuplicates,
                    onChange = { vm.setScanConfig(cfg.copy(allowDuplicates = it)) },
                )
            }
        }
        item { SectionHeader(title = "Network discovery") }
        item {
            SettingGroup {
                SwitchRow(
                    title = "mDNS (RFC 6762)",
                    description = "Multicast on 224.0.0.251:5353",
                    checked = cfg.mdnsEnabled,
                    onChange = { vm.setScanConfig(cfg.copy(mdnsEnabled = it)) },
                )
                Divider()
                SwitchRow(
                    title = "SSDP (UPnP)",
                    description = "M-SEARCH on 239.255.255.250:1900",
                    checked = cfg.ssdpEnabled,
                    onChange = { vm.setScanConfig(cfg.copy(ssdpEnabled = it)) },
                )
                Divider()
                SliderRow(
                    title = "M-SEARCH MX",
                    description = "Max wait time for SSDP responses",
                    value = cfg.mxSeconds.toFloat(),
                    range = 1f..5f,
                    steps = 3,
                    suffix = "s",
                    onChange = { vm.setScanConfig(cfg.copy(mxSeconds = it.toInt())) },
                )
            }
        }
        item { Spacer(Modifier.height(40.dp)) }
    }
}

@Composable
private fun RepoCard(onOpenRepo: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp)
            .clip(MaterialTheme.shapes.extraLarge)
            .background(RepoBg)
            .clickable(onClick = onOpenRepo)
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Icon(Icons.Filled.Code, contentDescription = null, tint = RepoFg, modifier = Modifier.size(28.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "rajeshpachaikani/nearby",
                    style = MonoStyle.copy(fontSize = MaterialTheme.typography.titleSmall.fontSize),
                    fontWeight = FontWeight.SemiBold,
                    color = RepoFg,
                )
                Text(
                    text = "v0.4.2 · MIT · Kotlin · minSdk 23",
                    style = MonoStyle.copy(fontSize = MaterialTheme.typography.labelSmall.fontSize),
                    color = RepoFg.copy(alpha = 0.7f),
                )
            }
            Text(
                text = "★ 1.2k",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFE5A471),
            )
        }
        Text(
            text = "Reference Android app for SSDP / mDNS / BLE discovery. Built as teaching material for embedded firmware & Android engineers — fully open source, every screen here links to its source file.",
            style = MaterialTheme.typography.bodySmall,
            color = RepoFg.copy(alpha = 0.75f),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            RepoMeta("★ 1,243"); RepoMeta("⑂ 87"); RepoMeta("● Kotlin 92%")
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            RepoButton("View on GitHub", filled = true, icon = Icons.Filled.Code, onClick = onOpenRepo)
            RepoButton("Clone", filled = false, icon = Icons.Filled.Code, onClick = onOpenRepo)
            RepoButton("Docs", filled = false, icon = Icons.Filled.Info, onClick = onOpenRepo)
        }
    }
}

@Composable
private fun RepoMeta(text: String) {
    Text(
        text = text,
        style = MonoStyle.copy(fontSize = MaterialTheme.typography.labelSmall.fontSize),
        color = RepoFg.copy(alpha = 0.7f),
    )
}

@Composable
private fun RepoButton(label: String, filled: Boolean, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .clip(CircleShape)
            .background(if (filled) RepoButtonBg else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Icon(icon, contentDescription = null, tint = if (filled) RepoButtonFg else RepoFg, modifier = Modifier.size(14.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = if (filled) RepoButtonFg else RepoFg,
        )
    }
}

@Composable
private fun SettingGroup(content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clip(MaterialTheme.shapes.large)
            .background(MaterialTheme.colorScheme.surfaceContainerLow),
    ) { content() }
}

@Composable
private fun SwitchRow(title: String, description: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            Text(text = description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(
            checked = checked,
            onCheckedChange = onChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                checkedTrackColor = MaterialTheme.colorScheme.primary,
            ),
        )
    }
}

@Composable
private fun SliderRow(
    title: String,
    description: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    suffix: String,
    steps: Int = 0,
    onChange: (Float) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            Text(
                text = "${value.toInt()}$suffix",
                style = MonoStyle.copy(fontSize = MaterialTheme.typography.titleSmall.fontSize),
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
            )
        }
        Slider(
            value = value,
            onValueChange = onChange,
            valueRange = range,
            steps = steps,
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary,
                inactiveTrackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
            ),
        )
        Text(text = description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun Divider() {
    androidx.compose.foundation.layout.Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(MaterialTheme.colorScheme.outlineVariant)
    )
}
