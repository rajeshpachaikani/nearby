package `in`.unartech.nearbydevs.discovery

import android.content.Context
import android.net.wifi.WifiManager
import android.util.Log
import `in`.unartech.nearbydevs.data.model.SsdpDevice
import `in`.unartech.nearbydevs.data.model.SsdpService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.StringReader
import java.net.DatagramPacket
import java.net.InetAddress
import java.net.MulticastSocket
import java.net.URL
import java.util.concurrent.ConcurrentHashMap

/**
 * SSDP Scanner for discovering UPnP devices on the network
 */
class SsdpScanner(private val context: Context) {

    companion object {
        private const val TAG = "SsdpScanner"
        private const val SSDP_ADDRESS = "239.255.255.250"
        private const val SSDP_PORT = 1900
        private const val RECEIVE_TIMEOUT = 3000

        private fun buildSearchMessage(mx: Int, st: String) =
            "M-SEARCH * HTTP/1.1\r\n" +
            "HOST: 239.255.255.250:1900\r\n" +
            "MAN: \"ssdp:discover\"\r\n" +
            "MX: $mx\r\n" +
            "ST: $st\r\n" +
            "\r\n"
    }

    private val wifiManager: WifiManager by lazy {
        context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
    }

    private val discoveredDevices = ConcurrentHashMap<String, SsdpDevice>()

    /**
     * Check if WiFi is enabled
     */
    fun isWifiEnabled(): Boolean {
        return wifiManager.isWifiEnabled
    }

    /**
     * Start SSDP discovery and emit discovered devices as a Flow
     */
    fun startScan(mxSeconds: Int = 2, searchTarget: String = "ssdp:all"): Flow<SsdpDevice> = callbackFlow {
        discoveredDevices.clear()
        val mSearch = buildSearchMessage(mxSeconds.coerceIn(1, 5), searchTarget)

        var multicastLock: WifiManager.MulticastLock? = null
        var socket: MulticastSocket? = null

        try {
            // Acquire multicast lock
            multicastLock = wifiManager.createMulticastLock("SsdpScanner")
            multicastLock.setReferenceCounted(true)
            multicastLock.acquire()

            val group = InetAddress.getByName(SSDP_ADDRESS)
            socket = MulticastSocket(SSDP_PORT)
            socket.reuseAddress = true
            socket.joinGroup(group)
            socket.soTimeout = RECEIVE_TIMEOUT

            // Send M-SEARCH request
            val searchBytes = mSearch.toByteArray()
            val searchPacket = DatagramPacket(searchBytes, searchBytes.size, group, SSDP_PORT)

            withContext(Dispatchers.IO) {
                socket.send(searchPacket)
                Log.d(TAG, "Sent M-SEARCH request")

                val buffer = ByteArray(4096)
                val receivePacket = DatagramPacket(buffer, buffer.size)

                // Keep receiving responses
                while (isActive) {
                    try {
                        socket.receive(receivePacket)
                        val response = String(receivePacket.data, 0, receivePacket.length)

                        val device = parseSsdpResponse(response, receivePacket.address.hostAddress ?: "")
                        if (device != null && !discoveredDevices.containsKey(device.usn)) {
                            discoveredDevices[device.usn] = device

                            // Try to fetch device description XML
                            val enrichedDevice = fetchDeviceDescription(device)
                            trySend(enrichedDevice)
                        }
                    } catch (e: java.net.SocketTimeoutException) {
                        // Timeout is expected, resend search request
                        socket.send(searchPacket)
                    } catch (e: Exception) {
                        if (isActive) {
                            Log.e(TAG, "Error receiving SSDP response", e)
                        }
                        break
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "SSDP scan error", e)
        }

        awaitClose {
            Log.d(TAG, "Stopping SSDP scan...")
            try {
                socket?.close()
                multicastLock?.release()
            } catch (e: Exception) {
                Log.e(TAG, "Error cleaning up SSDP scan", e)
            }
        }
    }

    private fun parseSsdpResponse(response: String, senderIp: String): SsdpDevice? {
        try {
            val headers = mutableMapOf<String, String>()
            response.lines().forEach { line ->
                val colonIndex = line.indexOf(':')
                if (colonIndex > 0) {
                    val key = line.substring(0, colonIndex).trim().uppercase()
                    val value = line.substring(colonIndex + 1).trim()
                    headers[key] = value
                }
            }

            val location = headers["LOCATION"] ?: return null
            val usn = headers["USN"] ?: return null
            val server = headers["SERVER"] ?: ""
            val st = headers["ST"] ?: ""

            // Parse the location URL
            val url = URL(location)
            val ipAddress = url.host
            val port = if (url.port == -1) 80 else url.port

            return SsdpDevice(
                id = usn,
                name = st.substringAfterLast(":"),
                ipAddress = ipAddress,
                port = port,
                location = location,
                server = server,
                usn = usn,
                deviceType = st
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing SSDP response", e)
            return null
        }
    }

    private suspend fun fetchDeviceDescription(device: SsdpDevice): SsdpDevice {
        return withContext(Dispatchers.IO) {
            try {
                val url = URL(device.location)
                val connection = url.openConnection()
                connection.connectTimeout = 5000
                connection.readTimeout = 5000

                val xml = connection.getInputStream().bufferedReader().use { it.readText() }
                parseDeviceXml(device, xml)
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching device description from ${device.location}", e)
                device
            }
        }
    }

    private fun parseDeviceXml(device: SsdpDevice, xml: String): SsdpDevice {
        try {
            val factory = XmlPullParserFactory.newInstance()
            val parser = factory.newPullParser()
            parser.setInput(StringReader(xml))

            var friendlyName = ""
            var manufacturer = ""
            var modelName = ""
            var modelNumber = ""
            var presentationUrl = ""
            val services = mutableListOf<SsdpService>()

            var currentTag = ""
            var inService = false
            var serviceType = ""
            var serviceId = ""
            var controlUrl = ""
            var eventSubUrl = ""
            var scpdUrl = ""

            var eventType = parser.eventType
            while (eventType != XmlPullParser.END_DOCUMENT) {
                when (eventType) {
                    XmlPullParser.START_TAG -> {
                        currentTag = parser.name
                        if (currentTag == "service") {
                            inService = true
                        }
                    }
                    XmlPullParser.TEXT -> {
                        val text = parser.text?.trim() ?: ""
                        if (text.isNotEmpty()) {
                            when (currentTag) {
                                "friendlyName" -> friendlyName = text
                                "manufacturer" -> manufacturer = text
                                "modelName" -> modelName = text
                                "modelNumber" -> modelNumber = text
                                "presentationURL" -> presentationUrl = text
                                "serviceType" -> if (inService) serviceType = text
                                "serviceId" -> if (inService) serviceId = text
                                "controlURL" -> if (inService) controlUrl = text
                                "eventSubURL" -> if (inService) eventSubUrl = text
                                "SCPDURL" -> if (inService) scpdUrl = text
                            }
                        }
                    }
                    XmlPullParser.END_TAG -> {
                        if (parser.name == "service" && inService) {
                            services.add(SsdpService(
                                serviceType = serviceType,
                                serviceId = serviceId,
                                controlUrl = controlUrl,
                                eventSubUrl = eventSubUrl,
                                scpdUrl = scpdUrl
                            ))
                            inService = false
                            serviceType = ""
                            serviceId = ""
                            controlUrl = ""
                            eventSubUrl = ""
                            scpdUrl = ""
                        }
                    }
                }
                eventType = parser.next()
            }

            return device.copy(
                name = friendlyName.ifEmpty { device.name },
                friendlyName = friendlyName,
                manufacturer = manufacturer,
                modelName = modelName,
                modelNumber = modelNumber,
                presentationUrl = presentationUrl,
                services = services,
                rawXml = xml
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing device XML", e)
            return device.copy(rawXml = xml)
        }
    }
}
