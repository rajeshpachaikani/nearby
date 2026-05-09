package `in`.unartech.nearbydevs.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import `in`.unartech.nearbydevs.data.FavoritesStore
import `in`.unartech.nearbydevs.data.toSavedDevice
import `in`.unartech.nearbydevs.data.toUiDevice
import `in`.unartech.nearbydevs.data.model.BleDevice
import `in`.unartech.nearbydevs.data.model.DeviceType
import `in`.unartech.nearbydevs.data.model.LogEvent
import `in`.unartech.nearbydevs.data.model.LogTag
import `in`.unartech.nearbydevs.data.model.MdnsDevice
import `in`.unartech.nearbydevs.data.model.ScanConfig
import `in`.unartech.nearbydevs.data.model.ServiceDef
import `in`.unartech.nearbydevs.data.model.SsdpDevice
import `in`.unartech.nearbydevs.data.model.ThemeMode
import `in`.unartech.nearbydevs.data.model.UiDevice
import `in`.unartech.nearbydevs.discovery.BleConnector
import `in`.unartech.nearbydevs.discovery.BleScanner
import `in`.unartech.nearbydevs.discovery.GattEvent
import `in`.unartech.nearbydevs.discovery.MdnsScanner
import `in`.unartech.nearbydevs.discovery.SsdpScanner
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

enum class ProtocolFilter(val label: String, val type: DeviceType?) {
    All("All", null),
    BLE("BLE", DeviceType.BLE),
    MDNS("mDNS", DeviceType.MDNS),
    SSDP("SSDP", DeviceType.SSDP),
}

data class GattConnState(
    val connecting: Boolean = false,
    val connected: Boolean = false,
    val mtu: Int = 0,
    val batteryPercent: Int? = null,
    val heartRateBpm: Int? = null,
    val notifying: Boolean = false,
    val error: String? = null,
)

class DiscoveryViewModel(application: Application) : AndroidViewModel(application) {

    private val ssdpScanner = SsdpScanner(application)
    private val mdnsScanner = MdnsScanner(application)
    private val bleScanner = BleScanner(application)
    private val bleConnector = BleConnector(application)
    private val favoritesStore = FavoritesStore(application)
    private val timeFmt = SimpleDateFormat("HH:mm:ss.SSS", Locale.US)

    private val _scanning = MutableStateFlow(false)
    val scanning: StateFlow<Boolean> = _scanning.asStateFlow()

    private val _devices = MutableStateFlow<List<UiDevice>>(emptyList())
    val devices: StateFlow<List<UiDevice>> = _devices.asStateFlow()

    private val _filter = MutableStateFlow(ProtocolFilter.All)
    val filter: StateFlow<ProtocolFilter> = _filter.asStateFlow()

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _logs = MutableStateFlow<List<LogEvent>>(emptyList())
    val logs: StateFlow<List<LogEvent>> = _logs.asStateFlow()

    private val _logsPaused = MutableStateFlow(false)
    val logsPaused: StateFlow<Boolean> = _logsPaused.asStateFlow()

    private val _packetsPerSec = MutableStateFlow(0)
    val packetsPerSec: StateFlow<Int> = _packetsPerSec.asStateFlow()

    private val _scanCfg = MutableStateFlow(ScanConfig())
    val scanCfg: StateFlow<ScanConfig> = _scanCfg.asStateFlow()

    private val _themeMode = MutableStateFlow(ThemeMode.System)
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    private val _selectedDeviceId = MutableStateFlow<String?>(null)
    val selectedDeviceId: StateFlow<String?> = _selectedDeviceId.asStateFlow()

    private val _hasBlePermission = MutableStateFlow(false)
    val hasBlePermission: StateFlow<Boolean> = _hasBlePermission.asStateFlow()

    private val _gattState = MutableStateFlow(GattConnState())
    val gattState: StateFlow<GattConnState> = _gattState.asStateFlow()

    private var bleJob: Job? = null
    private var mdnsJob: Job? = null
    private var ssdpJob: Job? = null
    private var ppsJob: Job? = null
    private var gattJob: Job? = null

    private var packetCounter = 0

    init {
        viewModelScope.launch {
            val saved = favoritesStore.load().map { it.toUiDevice() }
            if (saved.isNotEmpty()) {
                _devices.update { current ->
                    val existingIds = current.map { it.id }.toSet()
                    current + saved.filter { it.id !in existingIds }
                }
            }
        }
    }

    private fun persistFavorites() {
        val snapshot = _devices.value.filter { it.favorite }.map { it.toSavedDevice() }
        viewModelScope.launch { favoritesStore.save(snapshot) }
    }

    fun setFilter(f: ProtocolFilter) { _filter.value = f }
    fun setQuery(q: String) { _query.value = q }
    fun toggleLogsPaused() { _logsPaused.update { !it } }
    fun clearLogs() { _logs.value = emptyList() }
    fun setThemeMode(mode: ThemeMode) { _themeMode.value = mode }
    fun openDevice(id: String?) { _selectedDeviceId.value = id }

    fun setScanConfig(cfg: ScanConfig) {
        val prev = _scanCfg.value
        _scanCfg.value = cfg
        if (_scanning.value) {
            if (prev.windowMs != cfg.windowMs ||
                prev.intervalMs != cfg.intervalMs ||
                prev.activeScan != cfg.activeScan ||
                prev.allowDuplicates != cfg.allowDuplicates
            ) restartBle()
            if (prev.mxSeconds != cfg.mxSeconds || prev.ssdpEnabled != cfg.ssdpEnabled) restartSsdp()
            if (prev.mdnsEnabled != cfg.mdnsEnabled) restartMdns()
        }
    }

    fun setBlePermission(granted: Boolean) {
        val was = _hasBlePermission.value
        _hasBlePermission.value = granted
        if (!was && granted && _scanning.value) restartBle()
    }

    fun toggleScan() {
        if (_scanning.value) stopAll() else startAll()
    }

    fun startAll() {
        if (_scanning.value) return
        _scanning.value = true
        startBle()
        startMdns()
        startSsdp()
        startPpsTicker()
    }

    fun stopAll() {
        _scanning.value = false
        bleJob?.cancel(); bleJob = null
        mdnsJob?.cancel(); mdnsJob = null
        ssdpJob?.cancel(); ssdpJob = null
        ppsJob?.cancel(); ppsJob = null
        _packetsPerSec.value = 0
    }

    fun toggleFavorite(id: String) {
        _devices.update { list ->
            list.map { if (it.id == id) it.copy(favorite = !it.favorite) else it }
        }
        persistFavorites()
    }

    fun deviceById(id: String?): UiDevice? = id?.let { x -> _devices.value.find { it.id == x } }

    private fun startBle() {
        if (!bleScanner.isBleSupported() || !bleScanner.isBluetoothEnabled() || !_hasBlePermission.value) return
        bleJob?.cancel()
        bleJob = viewModelScope.launch {
            try {
                bleScanner.startScan(_scanCfg.value).collect { d -> mergeBle(d) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                appendLog(LogTag.GATT, "BLE scan error: ${e.message}")
            }
        }
    }

    private fun restartBle() {
        bleJob?.cancel(); bleJob = null
        startBle()
    }

    private fun startMdns() {
        if (!_scanCfg.value.mdnsEnabled) return
        mdnsJob?.cancel()
        mdnsJob = viewModelScope.launch {
            try {
                mdnsScanner.startScan().collect { d -> mergeMdns(d) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                appendLog(LogTag.MDNS, "mDNS error: ${e.message}")
            }
        }
    }

    private fun restartMdns() {
        mdnsJob?.cancel(); mdnsJob = null
        if (_scanning.value) startMdns()
    }

    private fun startSsdp() {
        if (!_scanCfg.value.ssdpEnabled || !ssdpScanner.isWifiEnabled()) return
        ssdpJob?.cancel()
        ssdpJob = viewModelScope.launch {
            try {
                ssdpScanner.startScan(mxSeconds = _scanCfg.value.mxSeconds).collect { d -> mergeSsdp(d) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                appendLog(LogTag.SSDP, "SSDP error: ${e.message}")
            }
        }
    }

    private fun restartSsdp() {
        ssdpJob?.cancel(); ssdpJob = null
        if (_scanning.value) startSsdp()
    }

    private fun startPpsTicker() {
        ppsJob?.cancel()
        ppsJob = viewModelScope.launch {
            while (_scanning.value) {
                kotlinx.coroutines.delay(1000)
                _packetsPerSec.value = packetCounter
                packetCounter = 0
            }
        }
    }

    private fun mergeBle(d: BleDevice) {
        val now = timeFmt.format(Date())
        packetCounter++
        val id = "ble:${d.macAddress}"
        val existing = _devices.value.find { it.id == id }
        val name = if (d.name.isBlank() || d.name == "Unknown Device")
            "(unnamed) ${d.macAddress.take(8)}.." else d.name
        val intent = if (d.advertisedServices.isNotEmpty())
            "GATT services: ${d.advertisedServices.size}"
        else if (d.manufacturerData.isNotEmpty()) {
            val k = d.manufacturerData.keys.first()
            "Manuf 0x${k.toString(16).uppercase().padStart(4, '0')}"
        } else "BLE advertiser"

        val ui = UiDevice(
            id = id,
            protocol = DeviceType.BLE,
            name = name,
            mac = d.macAddress,
            vendor = ouiToVendor(d.macAddress),
            intent = intent,
            rssi = d.rssi,
            txPowerDbm = d.txPowerLevel,
            connectable = d.isConnectable,
            services = d.advertisedServices.map { it.toString() },
            seenAt = now,
            lastSeenMs = System.currentTimeMillis(),
            fresh = existing == null,
            favorite = existing?.favorite ?: false,
            rawAdv = d.rawScanRecord,
            gattServices = existing?.gattServices ?: emptyList(),
        )
        _devices.update { list ->
            if (existing == null) {
                appendLog(LogTag.ADV, "BLE adv  ${d.macAddress}  rssi=${d.rssi}")
                listOf(ui) + list
            } else {
                list.map { if (it.id == id) ui.copy(fresh = false) else it }
            }
        }
    }

    private fun mergeMdns(d: MdnsDevice) {
        val now = timeFmt.format(Date())
        packetCounter++
        val id = "mdns:${d.id}"
        val existing = _devices.value.find { it.id == id }
        val ui = UiDevice(
            id = id,
            protocol = DeviceType.MDNS,
            name = if (d.name.endsWith(".local")) d.name else "${d.name}.local",
            mac = d.hostname,
            vendor = ouiToVendor(d.hostname),
            intent = d.serviceType.trim('.'),
            port = d.port.takeIf { it > 0 },
            ipv4 = d.ipAddresses.firstOrNull(),
            txt = d.txtRecords,
            seenAt = now,
            lastSeenMs = System.currentTimeMillis(),
            fresh = existing == null,
            favorite = existing?.favorite ?: false,
        )
        _devices.update { list ->
            if (existing == null) {
                appendLog(LogTag.MDNS, "PTR ${d.serviceType} → ${d.name}")
                listOf(ui) + list
            } else list.map { if (it.id == id) ui.copy(fresh = false) else it }
        }
    }

    private fun mergeSsdp(d: SsdpDevice) {
        val now = timeFmt.format(Date())
        packetCounter++
        val id = "ssdp:${d.usn}"
        val existing = _devices.value.find { it.id == id }
        val displayName = d.friendlyName.ifBlank { d.modelName.ifBlank { d.name } }.ifBlank { d.deviceType }
        val ui = UiDevice(
            id = id,
            protocol = DeviceType.SSDP,
            name = displayName,
            mac = d.ipAddress,
            vendor = d.manufacturer.ifBlank { "UPnP" },
            intent = d.deviceType,
            location = d.location,
            server = d.server,
            ipv4 = d.ipAddress,
            port = d.port.takeIf { it > 0 },
            seenAt = now,
            lastSeenMs = System.currentTimeMillis(),
            fresh = existing == null,
            favorite = existing?.favorite ?: false,
            rawXml = d.rawXml.takeIf { it.isNotBlank() },
        )
        _devices.update { list ->
            if (existing == null) {
                appendLog(LogTag.SSDP, "NOTIFY USN=${d.usn} ST=${d.deviceType}")
                listOf(ui) + list
            } else list.map { if (it.id == id) ui.copy(fresh = false) else it }
        }
    }

    private fun appendLog(tag: LogTag, msg: String) {
        if (_logsPaused.value) return
        val ev = LogEvent(timeFmt.format(Date()), tag, msg)
        _logs.update { listOf(ev) + it.take(199) }
    }

    private fun ouiToVendor(mac: String): String {
        val prefix = mac.take(8).uppercase()
        return OUI[prefix] ?: "Unknown"
    }

    // --- GATT connection lifecycle for Connect sheet -----------------------

    fun connectGatt(device: UiDevice) {
        if (device.protocol != DeviceType.BLE) return
        gattJob?.cancel()
        _gattState.value = GattConnState(connecting = true)
        gattJob = viewModelScope.launch {
            try {
                bleConnector.connect(device.mac).collect { ev -> handleGattEvent(device, ev) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _gattState.update { it.copy(error = e.message) }
            }
        }
    }

    fun disconnectGatt() {
        gattJob?.cancel(); gattJob = null
        bleConnector.close()
        _gattState.value = GattConnState()
    }

    fun readBattery() {
        bleConnector.readBattery()
        appendLog(LogTag.GATT, "readCharacteristic 0x2A19")
    }

    fun toggleHeartRateNotify() {
        val state = _gattState.value
        val next = !state.notifying
        if (bleConnector.subscribeHeartRate(next)) {
            _gattState.update { it.copy(notifying = next, heartRateBpm = if (!next) null else it.heartRateBpm) }
            appendLog(LogTag.GATT, "${if (next) "subscribe" else "unsubscribe"} 0x2A37")
        }
    }

    private fun handleGattEvent(device: UiDevice, ev: GattEvent) {
        when (ev) {
            GattEvent.Connecting ->
                _gattState.update { it.copy(connecting = true) }
            is GattEvent.Connected ->
                _gattState.update { it.copy(connecting = false, connected = true, mtu = ev.mtu) }
            is GattEvent.Services -> {
                _devices.update { list ->
                    list.map { if (it.id == device.id) it.copy(gattServices = ev.services) else it }
                }
                appendLog(LogTag.GATT, "primaryServices() → ${ev.services.size} services")
            }
            is GattEvent.Value -> {
                val v = ev.value
                when (ev.charUuid) {
                    UUID.fromString("00002a19-0000-1000-8000-00805f9b34fb") -> {
                        if (v.isNotEmpty()) {
                            val pct = v[0].toInt() and 0xFF
                            _gattState.update { it.copy(batteryPercent = pct) }
                            appendLog(LogTag.GATT, "Battery=${pct}%")
                        }
                    }
                    UUID.fromString("00002a37-0000-1000-8000-00805f9b34fb") -> {
                        // Heart Rate Measurement: byte0=flags, byte1=hr (UINT8) or bytes1-2 (UINT16)
                        if (v.isNotEmpty()) {
                            val flags = v[0].toInt() and 0xFF
                            val bpm = if ((flags and 0x01) == 0 && v.size >= 2)
                                v[1].toInt() and 0xFF
                            else if (v.size >= 3)
                                ((v[2].toInt() and 0xFF) shl 8) or (v[1].toInt() and 0xFF)
                            else 0
                            _gattState.update { it.copy(heartRateBpm = bpm) }
                        }
                    }
                }
            }
            is GattEvent.Disconnected -> {
                _gattState.update { GattConnState(error = if (ev.status != 0) "disconnected (status ${ev.status})" else null) }
                appendLog(LogTag.GATT, "disconnected status=${ev.status}")
            }
            is GattEvent.Error -> {
                _gattState.update { it.copy(error = ev.message, connecting = false) }
                appendLog(LogTag.GATT, "error: ${ev.message}")
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopAll()
        bleConnector.close()
    }

    companion object {
        private val OUI = mapOf(
            "AC:23:3F" to "Espressif Inc.",
            "E8:DB:84" to "Nordic Semiconductor",
            "DC:A6:32" to "Raspberry Pi Trading",
            "F4:6B:8C" to "Google LLC",
            "B8:27:EB" to "Raspberry Pi Foundation",
            "94:B0:1F" to "Samsung Electronics",
            "00:1A:7D" to "Cisco Systems",
            "A4:C1:38" to "Telink Semi",
            "5C:CF:7F" to "Espressif Inc.",
            "D8:A0:1D" to "Sonos, Inc.",
            "B0:7D:64" to "Philips Hue",
            "C8:69:CD" to "Apple, Inc.",
        )
    }
}
