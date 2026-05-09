package `in`.unartech.nearbydevs.discovery

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.Build
import android.os.ParcelUuid
import android.util.Log
import `in`.unartech.nearbydevs.data.model.BleDevice
import `in`.unartech.nearbydevs.data.model.ScanConfig
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.util.UUID

/**
 * BLE Scanner for discovering Bluetooth Low Energy devices
 */
class BleScanner(private val context: Context) {

    companion object {
        private const val TAG = "BleScanner"
    }

    private val bluetoothManager: BluetoothManager? by lazy {
        context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
    }

    private val bluetoothAdapter: BluetoothAdapter? by lazy {
        bluetoothManager?.adapter
    }

    private val bleScanner: BluetoothLeScanner? by lazy {
        bluetoothAdapter?.bluetoothLeScanner
    }

    /**
     * Check if BLE is supported on this device
     */
    fun isBleSupported(): Boolean {
        return context.packageManager.hasSystemFeature(android.content.pm.PackageManager.FEATURE_BLUETOOTH_LE)
    }

    /**
     * Check if Bluetooth is enabled
     */
    fun isBluetoothEnabled(): Boolean {
        return bluetoothAdapter?.isEnabled == true
    }

    /**
     * Start BLE scanning and emit discovered devices as a Flow
     */
    @SuppressLint("MissingPermission")
    fun startScan(config: ScanConfig = ScanConfig()): Flow<BleDevice> = callbackFlow {
        if (!isBleSupported()) {
            Log.w(TAG, "BLE not supported on this device")
            close()
            return@callbackFlow
        }

        if (!isBluetoothEnabled()) {
            Log.w(TAG, "Bluetooth is not enabled")
            close()
            return@callbackFlow
        }

        val scanner = bleScanner
        if (scanner == null) {
            Log.e(TAG, "Unable to get BluetoothLeScanner")
            close()
            return@callbackFlow
        }

        val scanCallback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                val device = parseScanResult(result)
                if (device != null) {
                    trySend(device)
                }
            }

            override fun onBatchScanResults(results: MutableList<ScanResult>) {
                results.forEach { result ->
                    val device = parseScanResult(result)
                    if (device != null) {
                        trySend(device)
                    }
                }
            }

            override fun onScanFailed(errorCode: Int) {
                Log.e(TAG, "BLE scan failed with error code: $errorCode")
                close(Exception("BLE scan failed: $errorCode"))
            }
        }

        val scanMode = when {
            config.intervalMs <= 512 -> ScanSettings.SCAN_MODE_LOW_LATENCY
            config.intervalMs <= 4096 -> ScanSettings.SCAN_MODE_BALANCED
            else -> ScanSettings.SCAN_MODE_LOW_POWER
        }
        val callbackType = if (config.allowDuplicates) {
            ScanSettings.CALLBACK_TYPE_ALL_MATCHES
        } else {
            ScanSettings.CALLBACK_TYPE_FIRST_MATCH or ScanSettings.CALLBACK_TYPE_MATCH_LOST
        }
        val builder = ScanSettings.Builder()
            .setScanMode(scanMode)
            .setReportDelay(0)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            builder.setCallbackType(callbackType)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setLegacy(false)
            builder.setPhy(android.bluetooth.le.ScanSettings.PHY_LE_ALL_SUPPORTED)
        }
        val scanSettings = builder.build()

        try {
            Log.d(TAG, "Starting BLE scan...")
            scanner.startScan(null, scanSettings, scanCallback)
        } catch (e: Exception) {
            Log.e(TAG, "Error starting BLE scan", e)
            close(e)
            return@callbackFlow
        }

        awaitClose {
            Log.d(TAG, "Stopping BLE scan...")
            try {
                scanner.stopScan(scanCallback)
            } catch (e: Exception) {
                Log.e(TAG, "Error stopping BLE scan", e)
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun parseScanResult(result: ScanResult): BleDevice? {
        val bluetoothDevice = result.device
        val scanRecord = result.scanRecord

        val macAddress = bluetoothDevice.address ?: return null
        val deviceName = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            bluetoothDevice.alias ?: bluetoothDevice.name ?: scanRecord?.deviceName ?: "Unknown Device"
        } else {
            bluetoothDevice.name ?: scanRecord?.deviceName ?: "Unknown Device"
        }

        val advertisedServices = mutableListOf<UUID>()
        scanRecord?.serviceUuids?.forEach { parcelUuid: ParcelUuid ->
            advertisedServices.add(parcelUuid.uuid)
        }

        val manufacturerData = mutableMapOf<Int, ByteArray>()
        scanRecord?.manufacturerSpecificData?.let { sparseArray ->
            for (i in 0 until sparseArray.size()) {
                val key = sparseArray.keyAt(i)
                val value = sparseArray.valueAt(i)
                manufacturerData[key] = value
            }
        }

        val serviceData = mutableMapOf<UUID, ByteArray>()
        scanRecord?.serviceData?.forEach { (parcelUuid, data) ->
            serviceData[parcelUuid.uuid] = data
        }

        val txPowerLevel = scanRecord?.txPowerLevel?.takeIf { it != Int.MIN_VALUE }

        val isConnectable = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            result.isConnectable
        } else {
            true
        }

        return BleDevice(
            id = macAddress,
            name = deviceName,
            macAddress = macAddress,
            rssi = result.rssi,
            txPowerLevel = txPowerLevel,
            isConnectable = isConnectable,
            advertisedServices = advertisedServices,
            manufacturerData = manufacturerData,
            serviceData = serviceData,
            rawScanRecord = scanRecord?.bytes,
            addressType = bluetoothDevice.type
        )
    }
}
