package `in`.unartech.nearbydevs.data.model

import java.util.UUID

/**
 * Base interface for all discovered devices
 */
interface Device {
    val id: String
    val name: String
    val type: DeviceType
    val discoveryTime: Long
    val lastSeen: Long
}

/**
 * SSDP/UPnP Device
 */
data class SsdpDevice(
    override val id: String,
    override val name: String,
    override val type: DeviceType = DeviceType.SSDP,
    override val discoveryTime: Long = System.currentTimeMillis(),
    override var lastSeen: Long = System.currentTimeMillis(),
    val ipAddress: String,
    val port: Int,
    val location: String,
    val server: String,
    val usn: String,
    val deviceType: String,
    val manufacturer: String = "",
    val modelName: String = "",
    val modelNumber: String = "",
    val friendlyName: String = "",
    val services: List<SsdpService> = emptyList(),
    val rawXml: String = "",
    val presentationUrl: String = ""
) : Device

/**
 * SSDP Service information
 */
data class SsdpService(
    val serviceType: String,
    val serviceId: String,
    val controlUrl: String = "",
    val eventSubUrl: String = "",
    val scpdUrl: String = ""
)

/**
 * mDNS/Bonjour Device
 */
data class MdnsDevice(
    override val id: String,
    override val name: String,
    override val type: DeviceType = DeviceType.MDNS,
    override val discoveryTime: Long = System.currentTimeMillis(),
    override var lastSeen: Long = System.currentTimeMillis(),
    val hostname: String,
    val ipAddresses: List<String>,
    val port: Int,
    val serviceType: String,
    val instanceName: String = "",
    val txtRecords: Map<String, String> = emptyMap()
) : Device

/**
 * Bluetooth Low Energy Device
 */
data class BleDevice(
    override val id: String,
    override val name: String,
    override val type: DeviceType = DeviceType.BLE,
    override val discoveryTime: Long = System.currentTimeMillis(),
    override var lastSeen: Long = System.currentTimeMillis(),
    val macAddress: String,
    val rssi: Int,
    val txPowerLevel: Int? = null,
    val isConnectable: Boolean = true,
    val advertisedServices: List<UUID> = emptyList(),
    val manufacturerData: Map<Int, ByteArray> = emptyMap(),
    val serviceData: Map<UUID, ByteArray> = emptyMap(),
    val rawScanRecord: ByteArray? = null,
    val addressType: Int = 0
) : Device {

    /**
     * Get signal strength description
     */
    fun getSignalStrength(): SignalStrength {
        return when {
            rssi >= -50 -> SignalStrength.EXCELLENT
            rssi >= -60 -> SignalStrength.GOOD
            rssi >= -70 -> SignalStrength.FAIR
            else -> SignalStrength.WEAK
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as BleDevice
        return macAddress == other.macAddress
    }

    override fun hashCode(): Int {
        return macAddress.hashCode()
    }
}

enum class SignalStrength {
    EXCELLENT, GOOD, FAIR, WEAK
}

/**
 * Common BLE Service UUIDs with human-readable names
 */
object BleServiceNames {
    private val knownServices = mapOf(
        UUID.fromString("00001800-0000-1000-8000-00805f9b34fb") to "Generic Access",
        UUID.fromString("00001801-0000-1000-8000-00805f9b34fb") to "Generic Attribute",
        UUID.fromString("0000180a-0000-1000-8000-00805f9b34fb") to "Device Information",
        UUID.fromString("0000180d-0000-1000-8000-00805f9b34fb") to "Heart Rate",
        UUID.fromString("0000180f-0000-1000-8000-00805f9b34fb") to "Battery Service",
        UUID.fromString("00001810-0000-1000-8000-00805f9b34fb") to "Blood Pressure",
        UUID.fromString("00001816-0000-1000-8000-00805f9b34fb") to "Cycling Speed and Cadence",
        UUID.fromString("0000181a-0000-1000-8000-00805f9b34fb") to "Environmental Sensing",
        UUID.fromString("0000181c-0000-1000-8000-00805f9b34fb") to "User Data",
        UUID.fromString("0000fee0-0000-1000-8000-00805f9b34fb") to "Xiaomi/Mi Band",
        UUID.fromString("0000feaa-0000-1000-8000-00805f9b34fb") to "Eddystone",
    )

    fun getName(uuid: UUID): String {
        return knownServices[uuid] ?: uuid.toString()
    }
}
