package `in`.unartech.nearbydevs.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import `in`.unartech.nearbydevs.ui.theme.NearbydevsTheme
import `in`.unartech.nearbydevs.ui.component.BottomNav
import `in`.unartech.nearbydevs.ui.component.NavTab
import `in`.unartech.nearbydevs.ui.screen.ConnectSheet
import `in`.unartech.nearbydevs.ui.screen.DeviceDetailScreen
import `in`.unartech.nearbydevs.ui.screen.DiscoverScreen
import `in`.unartech.nearbydevs.ui.screen.LogsScreen
import `in`.unartech.nearbydevs.ui.screen.SavedScreen
import `in`.unartech.nearbydevs.ui.screen.SettingsScreen
import `in`.unartech.nearbydevs.ui.viewmodel.DiscoveryViewModel

@Composable
fun NearbyDevsNavGraph(
    onRequestBlePermissions: () -> Unit = {},
    onOpenBluetoothSettings: () -> Unit = {},
    onOpenAppSettings: () -> Unit = {},
    hasBlePermission: Boolean = false,
) {
    val vm: DiscoveryViewModel = viewModel()
    val themeMode by vm.themeMode.collectAsState()

    LaunchedEffect(hasBlePermission) {
        vm.setBlePermission(hasBlePermission)
        if (!hasBlePermission) onRequestBlePermissions()
        vm.startAll()
    }

    NearbydevsTheme(mode = themeMode) {
        NbdAppContent(vm = vm)
    }
}

@Composable
private fun NbdAppContent(vm: DiscoveryViewModel) {
    var tab by remember { mutableStateOf(NavTab.Discover) }
    var showConnect by remember { mutableStateOf(false) }
    val devices by vm.devices.collectAsState()
    val openId by vm.selectedDeviceId.collectAsState()
    val savedCount = devices.count { it.favorite }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surface,
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surface)
                        .statusBarsPadding(),
                ) {
                    when (tab) {
                        NavTab.Discover -> DiscoverScreen(
                            vm = vm,
                            onOpenDevice = { vm.openDevice(it) },
                        )
                        NavTab.Saved -> SavedScreen(
                            vm = vm,
                            onOpenDevice = { vm.openDevice(it) },
                        )
                        NavTab.Logs -> LogsScreen(vm = vm)
                        NavTab.Settings -> SettingsScreen(vm = vm)
                    }
                }
                BottomNav(
                    selected = tab,
                    savedCount = savedCount,
                    onSelect = { tab = it },
                    modifier = Modifier.navigationBarsPadding(),
                )
            }

            if (openId != null) {
                DeviceDetailScreen(
                    vm = vm,
                    deviceId = openId!!,
                    onBack = { vm.openDevice(null); showConnect = false },
                    onConnect = { showConnect = true },
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surface),
                )
                BackHandler(enabled = !showConnect) {
                    vm.openDevice(null)
                }
            } else if (tab != NavTab.Discover) {
                BackHandler { tab = NavTab.Discover }
            }
        }
    }

    if (showConnect) {
        val device = vm.deviceById(openId)
        if (device != null) {
            BackHandler { showConnect = false }
            ConnectSheet(vm = vm, device = device, onDismiss = { showConnect = false })
        }
    }
}
