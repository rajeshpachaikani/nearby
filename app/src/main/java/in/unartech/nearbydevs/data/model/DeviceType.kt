package `in`.unartech.nearbydevs.data.model

/**
 * Enum representing the type of device discovery protocol
 */
enum class DeviceType {
    SSDP,   // Simple Service Discovery Protocol (UPnP)
    MDNS,   // mDNS/Bonjour/DNS-SD
    BLE     // Bluetooth Low Energy
}
