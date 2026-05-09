package `in`.unartech.nearbydevs.data.model

// Unified display model. Folds BleDevice / MdnsDevice / SsdpDevice into a single
// shape consumed by every screen. Real scanners map into this; mock data builds
// it directly.
data class UiDevice(
    val id: String,
    val protocol: DeviceType,
    val name: String,
    val mac: String,
    val vendor: String,
    val intent: String,
    val rssi: Int? = null,
    val advIntervalMs: Int? = null,
    val txPowerDbm: Int? = null,
    val connectable: Boolean = false,
    val services: List<String> = emptyList(),
    val port: Int? = null,
    val ipv4: String? = null,
    val txt: Map<String, String> = emptyMap(),
    val location: String? = null,
    val server: String? = null,
    val seenAt: String = "",
    val lastSeenMs: Long = 0L,
    val fresh: Boolean = false,
    val favorite: Boolean = false,
    val rawAdv: ByteArray? = null,
    val gattServices: List<ServiceDef> = emptyList(),
    val rawXml: String? = null,
)

data class CharFlag(val r: Boolean = false, val w: Boolean = false, val n: Boolean = false) {
    companion object {
        fun of(vararg s: String): CharFlag {
            return CharFlag(
                r = s.any { it.equals("R", true) || it.equals("READ", true) },
                w = s.any { it.equals("W", true) || it.equals("WRITE", true) },
                n = s.any { it.equals("N", true) || it.equals("NOTIFY", true) },
            )
        }
    }
}

data class CharDef(
    val name: String,
    val uuid: String,
    val flags: CharFlag,
)

enum class ServiceTag { STD, VENDOR }

data class ServiceDef(
    val name: String,
    val uuid: String,
    val tag: ServiceTag,
    val chars: List<CharDef>,
)

enum class LogTag { ADV, GATT, MDNS, SSDP }

data class LogEvent(
    val time: String,
    val tag: LogTag,
    val msg: String,
)

data class ScanConfig(
    val windowMs: Int = 512,
    val intervalMs: Int = 1024,
    val activeScan: Boolean = true,
    val allowDuplicates: Boolean = false,
    val mdnsEnabled: Boolean = true,
    val ssdpEnabled: Boolean = true,
    val mxSeconds: Int = 2,
)

enum class ThemeMode { System, Light, Dark }
