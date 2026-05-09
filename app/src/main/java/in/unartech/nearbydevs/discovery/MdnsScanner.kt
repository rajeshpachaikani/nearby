package `in`.unartech.nearbydevs.discovery

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.os.Build
import android.util.Log
import `in`.unartech.nearbydevs.data.model.MdnsDevice
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.util.concurrent.ConcurrentHashMap

class MdnsScanner(private val context: Context) {

    companion object {
        private const val TAG = "MdnsScanner"

        private val SERVICE_TYPES = listOf(
            "_http._tcp.",
            "_printer._tcp.",
            "_airplay._tcp.",
            "_googlecast._tcp.",
            "_homekit._tcp.",
            "_ipp._tcp.",
            "_smb._tcp.",
            "_ssh._tcp.",
            "_ftp._tcp.",
            "_daap._tcp.",
            "_raop._tcp.",
            "_hap._tcp."
        )
    }

    private val nsdManager: NsdManager by lazy {
        context.getSystemService(Context.NSD_SERVICE) as NsdManager
    }

    fun startScan(): Flow<MdnsDevice> = callbackFlow {
        val resolvedDevices = ConcurrentHashMap<String, MdnsDevice>()
        val discoveryListeners = mutableListOf<NsdManager.DiscoveryListener>()

        val resolveListener = object : NsdManager.ResolveListener {
            override fun onResolveFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
                Log.w(TAG, "Failed to resolve ${serviceInfo.serviceName}: error $errorCode")
            }

            override fun onServiceResolved(serviceInfo: NsdServiceInfo) {
                try {
                    val device = buildMdnsDevice(serviceInfo)
                    val previous = resolvedDevices.put(device.id, device)
                    if (previous == null) {
                        trySend(device)
                        Log.d(TAG, "Discovered mDNS device: ${device.name} (${device.hostname})")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error building mDNS device", e)
                }
            }
        }

        for (serviceType in SERVICE_TYPES) {
            val listener = object : NsdManager.DiscoveryListener {
                override fun onDiscoveryStarted(regType: String) {
                    Log.d(TAG, "Discovery started for $regType")
                }

                override fun onServiceFound(serviceInfo: NsdServiceInfo) {
                    Log.d(TAG, "Service found: ${serviceInfo.serviceName} ($serviceType)")
                    try {
                        nsdManager.resolveService(serviceInfo, resolveListener)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error resolving service", e)
                    }
                }

                override fun onServiceLost(serviceInfo: NsdServiceInfo) {
                    Log.d(TAG, "Service lost: ${serviceInfo.serviceName}")
                }

                override fun onDiscoveryStopped(regType: String) {
                    Log.d(TAG, "Discovery stopped for $regType")
                }

                override fun onStartDiscoveryFailed(regType: String, errorCode: Int) {
                    Log.e(TAG, "Discovery start failed for $regType: error $errorCode")
                }

                override fun onStopDiscoveryFailed(regType: String, errorCode: Int) {
                    Log.e(TAG, "Discovery stop failed for $regType: error $errorCode")
                }
            }
            discoveryListeners.add(listener)

            try {
                nsdManager.discoverServices(serviceType, NsdManager.PROTOCOL_DNS_SD, listener)
            } catch (e: Exception) {
                Log.e(TAG, "Error starting discovery for $serviceType", e)
            }
        }

        awaitClose {
            Log.d(TAG, "Stopping all mDNS discoveries...")
            for (listener in discoveryListeners) {
                try {
                    nsdManager.stopServiceDiscovery(listener)
                } catch (_: Exception) {
                }
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun buildMdnsDevice(serviceInfo: NsdServiceInfo): MdnsDevice {
        val host = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            serviceInfo.hostAddresses?.firstOrNull()
        } else {
            serviceInfo.host
        }
        val hostname = host?.hostName ?: host?.hostAddress ?: "unknown.local"
        val ipList = mutableListOf<String>()

        host?.let { h ->
            val address = h.hostAddress
            if (address != null && address.isNotEmpty()) {
                ipList.add(address)
            }
        }

        val serviceName = serviceInfo.serviceName ?: "Unknown Service"
        val serviceType = serviceInfo.serviceType ?: "_unknown._tcp."
        val port = serviceInfo.port
        val instanceName = serviceInfo.serviceName ?: ""

        val txtRecords = mutableMapOf<String, String>()
        serviceInfo.attributes?.let { attributes ->
            for ((key, value) in attributes) {
                txtRecords[key] = String(value, Charsets.UTF_8)
            }
        }

        val deviceId = "$serviceName:$serviceType:$hostname"

        return MdnsDevice(
            id = deviceId,
            name = serviceName,
            hostname = hostname,
            ipAddresses = ipList,
            port = port,
            serviceType = serviceType,
            instanceName = instanceName,
            txtRecords = txtRecords
        )
    }
}
