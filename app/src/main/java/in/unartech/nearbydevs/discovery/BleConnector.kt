package `in`.unartech.nearbydevs.discovery

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.os.Build
import android.util.Log
import `in`.unartech.nearbydevs.data.model.CharDef
import `in`.unartech.nearbydevs.data.model.CharFlag
import `in`.unartech.nearbydevs.data.model.ServiceDef
import `in`.unartech.nearbydevs.data.model.ServiceTag
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.util.UUID

private const val TAG = "BleConnector"
private val CCCD_UUID: UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")

// 0x180D Heart Rate Measurement / 0x180F Battery Level / 0x2A37, 0x2A19
private val BATTERY_CHAR: UUID = UUID.fromString("00002a19-0000-1000-8000-00805f9b34fb")
private val HEART_RATE_CHAR: UUID = UUID.fromString("00002a37-0000-1000-8000-00805f9b34fb")

sealed interface GattEvent {
    object Connecting : GattEvent
    data class Connected(val mtu: Int) : GattEvent
    data class Services(val services: List<ServiceDef>) : GattEvent
    data class Value(val charUuid: UUID, val value: ByteArray) : GattEvent
    data class Disconnected(val status: Int) : GattEvent
    data class Error(val message: String) : GattEvent
}

class BleConnector(private val context: Context) {

    private val bluetoothManager: BluetoothManager? by lazy {
        context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
    }
    private val adapter: BluetoothAdapter? by lazy { bluetoothManager?.adapter }

    private var gatt: BluetoothGatt? = null

    @SuppressLint("MissingPermission")
    fun connect(macAddress: String): Flow<GattEvent> = callbackFlow {
        val a = adapter ?: run {
            trySend(GattEvent.Error("BluetoothAdapter unavailable"))
            close(); return@callbackFlow
        }
        val device = try {
            a.getRemoteDevice(macAddress)
        } catch (e: Exception) {
            trySend(GattEvent.Error("invalid MAC: $macAddress"))
            close(); return@callbackFlow
        }

        trySend(GattEvent.Connecting)

        val callback = object : BluetoothGattCallback() {
            override fun onConnectionStateChange(g: BluetoothGatt, status: Int, newState: Int) {
                when (newState) {
                    BluetoothProfile.STATE_CONNECTED -> {
                        Log.d(TAG, "connected status=$status")
                        trySend(GattEvent.Connected(mtu = 23))
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            g.requestMtu(247)
                        } else {
                            g.discoverServices()
                        }
                    }
                    BluetoothProfile.STATE_DISCONNECTED -> {
                        Log.d(TAG, "disconnected status=$status")
                        trySend(GattEvent.Disconnected(status))
                    }
                }
            }

            override fun onMtuChanged(g: BluetoothGatt, mtu: Int, status: Int) {
                trySend(GattEvent.Connected(mtu = mtu))
                g.discoverServices()
            }

            override fun onServicesDiscovered(g: BluetoothGatt, status: Int) {
                if (status != BluetoothGatt.GATT_SUCCESS) {
                    trySend(GattEvent.Error("discoverServices status=$status"))
                    return
                }
                val services = g.services.map { svc ->
                    val tag = if (isStandardService(svc.uuid)) ServiceTag.STD else ServiceTag.VENDOR
                    val chars = svc.characteristics.map { ch ->
                        CharDef(
                            name = friendlyName(ch.uuid),
                            uuid = ch.uuid.toString().uppercase(),
                            flags = CharFlag(
                                r = (ch.properties and BluetoothGattCharacteristic.PROPERTY_READ) != 0,
                                w = (ch.properties and (BluetoothGattCharacteristic.PROPERTY_WRITE or BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE)) != 0,
                                n = (ch.properties and (BluetoothGattCharacteristic.PROPERTY_NOTIFY or BluetoothGattCharacteristic.PROPERTY_INDICATE)) != 0,
                            ),
                        )
                    }
                    ServiceDef(
                        name = friendlyName(svc.uuid),
                        uuid = svc.uuid.toString().uppercase(),
                        tag = tag,
                        chars = chars,
                    )
                }
                trySend(GattEvent.Services(services))
            }

            override fun onCharacteristicChanged(g: BluetoothGatt, ch: BluetoothGattCharacteristic) {
                val v = ch.value ?: return
                trySend(GattEvent.Value(ch.uuid, v))
            }

            @Suppress("DEPRECATION")
            override fun onCharacteristicRead(g: BluetoothGatt, ch: BluetoothGattCharacteristic, status: Int) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    val v = ch.value ?: return
                    trySend(GattEvent.Value(ch.uuid, v))
                }
            }
        }

        gatt = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            device.connectGatt(context, false, callback, BluetoothDevice.TRANSPORT_LE)
        } else {
            @Suppress("DEPRECATION")
            device.connectGatt(context, false, callback)
        }

        awaitClose {
            try {
                gatt?.disconnect()
                gatt?.close()
            } catch (e: Exception) {
                Log.e(TAG, "close error", e)
            } finally {
                gatt = null
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun readBattery() {
        val g = gatt ?: return
        val svc = g.services.firstOrNull { it.characteristics.any { c -> c.uuid == BATTERY_CHAR } } ?: return
        val ch = svc.getCharacteristic(BATTERY_CHAR) ?: return
        g.readCharacteristic(ch)
    }

    @SuppressLint("MissingPermission")
    fun subscribeHeartRate(enable: Boolean): Boolean {
        val g = gatt ?: return false
        val svc = g.services.firstOrNull { it.characteristics.any { c -> c.uuid == HEART_RATE_CHAR } } ?: return false
        val ch = svc.getCharacteristic(HEART_RATE_CHAR) ?: return false
        if (!g.setCharacteristicNotification(ch, enable)) return false
        val cccd = ch.getDescriptor(CCCD_UUID) ?: return false
        @Suppress("DEPRECATION")
        cccd.value = if (enable) BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                     else BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE
        @Suppress("DEPRECATION")
        return g.writeDescriptor(cccd)
    }

    @SuppressLint("MissingPermission")
    fun writeHex(serviceUuid: UUID, charUuid: UUID, bytes: ByteArray): Boolean {
        val g = gatt ?: return false
        val svc = g.getService(serviceUuid) ?: return false
        val ch = svc.getCharacteristic(charUuid) ?: return false
        @Suppress("DEPRECATION")
        ch.value = bytes
        @Suppress("DEPRECATION")
        return g.writeCharacteristic(ch)
    }

    @SuppressLint("MissingPermission")
    fun close() {
        try {
            gatt?.disconnect()
            gatt?.close()
        } catch (_: Exception) { } finally {
            gatt = null
        }
    }

    companion object {
        private val STD_BASE = "-0000-1000-8000-00805f9b34fb"

        private fun isStandardService(uuid: UUID): Boolean =
            uuid.toString().endsWith(STD_BASE)

        private val FRIENDLY = mapOf(
            "00001800" to "Generic Access",
            "00001801" to "Generic Attribute",
            "0000180a" to "Device Information",
            "0000180d" to "Heart Rate",
            "0000180f" to "Battery Service",
            "00001810" to "Blood Pressure",
            "0000181a" to "Environmental Sensing",
            "00002a00" to "Device Name",
            "00002a01" to "Appearance",
            "00002a19" to "Battery Level",
            "00002a24" to "Model Number",
            "00002a26" to "Firmware Revision",
            "00002a29" to "Manufacturer Name",
            "00002a37" to "Heart Rate Measurement",
            "00002a38" to "Body Sensor Location",
            "00002a6d" to "Pressure",
            "00002a6e" to "Temperature",
            "00002a6f" to "Humidity",
            "6e400001" to "Nordic UART Service",
            "6e400002" to "RX (write)",
            "6e400003" to "TX (notify)",
        )

        fun friendlyName(uuid: UUID): String {
            val short = uuid.toString().substring(0, 8).lowercase()
            return FRIENDLY[short] ?: "0x${short.uppercase()} (vendor)"
        }
    }
}
