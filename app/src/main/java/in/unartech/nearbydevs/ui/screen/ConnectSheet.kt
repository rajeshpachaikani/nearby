package `in`.unartech.nearbydevs.ui.screen

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import `in`.unartech.nearbydevs.data.model.UiDevice
import `in`.unartech.nearbydevs.ui.component.ProtoIcon
import `in`.unartech.nearbydevs.ui.component.SectionHeader
import `in`.unartech.nearbydevs.ui.component.SourceTip
import `in`.unartech.nearbydevs.ui.theme.MonoStyle
import `in`.unartech.nearbydevs.ui.theme.Success
import `in`.unartech.nearbydevs.ui.theme.SuccessDark
import `in`.unartech.nearbydevs.ui.viewmodel.DiscoveryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConnectSheet(
    vm: DiscoveryViewModel,
    device: UiDevice,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val state by vm.gattState.collectAsState()

    LaunchedEffect(device.id) { vm.connectGatt(device) }
    DisposableEffect(Unit) { onDispose { vm.disconnectGatt() } }

    val phaseLabel = when {
        state.error != null -> state.error ?: "error"
        state.connected -> "Connected · MTU ${state.mtu} · ATT"
        state.connecting -> "Connecting…"
        else -> "Idle"
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(bottom = 22.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                ProtoIcon(protocol = device.protocol)
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = device.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    StatusLine(connected = state.connected, connecting = state.connecting, label = phaseLabel)
                }
            }

            if (state.connected) {
                SectionHeader(title = "GATT operations", aside = "live")
                OpCard(
                    name = "Battery Level",
                    uuid = "00002A19 · 0x180F",
                    actions = {
                        OpButton("Read", MaterialTheme.colorScheme.secondaryContainer, MaterialTheme.colorScheme.onSecondaryContainer) {
                            vm.readBattery()
                        }
                    },
                    value = state.batteryPercent?.let { v ->
                        "0x${v.toString(16).uppercase().padStart(2, '0')} · $v%"
                    },
                )
                OpCard(
                    name = "Heart Rate Measurement",
                    uuid = "00002A37 · 0x180D",
                    actions = {
                        OpButton(
                            label = if (state.notifying) "Stop" else "Notify",
                            bg = MaterialTheme.colorScheme.primaryContainer,
                            fg = MaterialTheme.colorScheme.onPrimaryContainer,
                            onClick = { vm.toggleHeartRateNotify() },
                        )
                    },
                    value = if (state.notifying) {
                        val h = state.heartRateBpm
                        val hex = h?.toString(16)?.uppercase()?.padStart(2, '0') ?: "—"
                        "0x$hex · ${h ?: "—"} bpm"
                    } else null,
                    live = state.notifying,
                )
                OpCard(
                    name = "RX (write)",
                    uuid = "6E400002 · Nordic UART",
                    actions = {
                        OpButton("Write hex…", MaterialTheme.colorScheme.tertiaryContainer, MaterialTheme.colorScheme.onTertiaryContainer)
                    },
                )
                Spacer(Modifier.height(8.dp))
                SourceTip(file = "app/src/main/java/.../BleConnector.kt", line = 60)
            } else if (state.connecting) {
                Spacer(Modifier.height(40.dp))
            } else if (state.error != null) {
                Text(
                    text = state.error ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
                )
            }
        }
    }
}

@Composable
private fun StatusLine(connected: Boolean, connecting: Boolean, label: String) {
    val isDark = MaterialTheme.colorScheme.background.let {
        0.2126f * it.red + 0.7152f * it.green + 0.0722f * it.blue < 0.4f
    }
    val successColor = if (isDark) SuccessDark else Success
    val color = if (connected) successColor else MaterialTheme.colorScheme.onSurfaceVariant
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.padding(top = 2.dp),
    ) {
        if (connecting && !connected) {
            val infinite = rememberInfiniteTransition(label = "dot")
            val a by infinite.animateFloat(
                initialValue = 0.3f, targetValue = 1f,
                animationSpec = infiniteRepeatable(tween(700, easing = LinearEasing), RepeatMode.Reverse),
                label = "alpha",
            )
            Dot(MaterialTheme.colorScheme.tertiary.copy(alpha = a))
        } else if (connected) {
            Dot(successColor)
        } else {
            Dot(MaterialTheme.colorScheme.outline)
        }
        Text(text = label, style = MonoStyle, color = color)
    }
}

@Composable
private fun Dot(color: Color) {
    Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(color))
}

@Composable
private fun OpCard(
    name: String,
    uuid: String,
    actions: @Composable () -> Unit,
    value: String? = null,
    live: Boolean = false,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text(text = uuid, style = MonoStyle, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            actions()
        }
        if (value != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.small)
                    .background(MaterialTheme.colorScheme.surfaceContainer)
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(text = value, style = MonoStyle, color = MaterialTheme.colorScheme.onSurface)
                if (live) {
                    Text(
                        text = "● LIVE",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Success,
                    )
                } else {
                    Icon(
                        imageVector = Icons.Filled.ContentCopy,
                        contentDescription = "Copy",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun OpButton(label: String, bg: Color, fg: Color, onClick: () -> Unit = {}) {
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(bg)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp),
    ) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = fg,
        )
    }
}
