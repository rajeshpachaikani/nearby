package `in`.unartech.nearbydevs.util

import java.util.UUID

object HexUtils {

    fun byteArrayToHex(bytes: ByteArray?): String {
        if (bytes == null) return ""
        return bytes.joinToString(" ") { byte ->
            String.format("%02X", byte)
        }
    }

    fun byteArrayToFormattedHex(bytes: ByteArray?, bytesPerLine: Int = 16): String {
        if (bytes == null) return ""
        return bytes.toList().chunked(bytesPerLine).joinToString("\n") { chunk ->
            val hex = chunk.joinToString(" ") { byte -> String.format("%02X", byte) }
            val ascii = chunk.joinToString("") { byte ->
                val b = byte.toInt() and 0xFF
                if (b in 32..126) b.toChar().toString() else "."
            }
            hex.padEnd(bytesPerLine * 3) + "  " + ascii
        }
    }

    fun formatUuid(uuid: UUID): String {
        val str = uuid.toString().uppercase()
        return when {
            str.startsWith("0000") && str.endsWith("-0000-1000-8000-00805F9B34FB") -> "0x${str.substring(4, 8)}"
            else -> str
        }
    }

    fun formatMacAddress(mac: String): String {
        return mac.uppercase()
    }

    fun manufacturerDataToHex(data: Map<Int, ByteArray>): String {
        return data.entries.joinToString("\n") { (id, bytes) ->
            val mfrName = when (id) {
                0x004C -> "Apple Inc."
                0x0006 -> "Microsoft"
                0x0075 -> "Samsung"
                0x0157 -> "Xiaomi"
                0x000D -> "Google"
                else -> String.format("0x%04X", id)
            }
            "$mfrName (0x${String.format("%04X", id)}): ${byteArrayToHex(bytes)}"
        }
    }
}
