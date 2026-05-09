package `in`.unartech.nearbydevs.data

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import `in`.unartech.nearbydevs.data.model.DeviceType
import `in`.unartech.nearbydevs.data.model.UiDevice
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.favoritesDataStore by preferencesDataStore(name = "favorites")

private val FAVORITES_KEY = stringPreferencesKey("favorites_json")

data class SavedDevice(
    val id: String,
    val protocol: DeviceType,
    val name: String,
    val mac: String,
    val vendor: String,
    val intent: String,
    val port: Int? = null,
    val ipv4: String? = null,
    val location: String? = null,
    val server: String? = null,
    val txt: Map<String, String> = emptyMap(),
    val services: List<String> = emptyList(),
    val connectable: Boolean = false,
    val seenAt: String = "",
    val lastSeenMs: Long = 0L,
)

fun SavedDevice.toUiDevice(): UiDevice = UiDevice(
    id = id,
    protocol = protocol,
    name = name,
    mac = mac,
    vendor = vendor,
    intent = intent,
    port = port,
    ipv4 = ipv4,
    location = location,
    server = server,
    txt = txt,
    services = services,
    connectable = connectable,
    seenAt = seenAt,
    lastSeenMs = lastSeenMs,
    favorite = true,
)

fun UiDevice.toSavedDevice(): SavedDevice = SavedDevice(
    id = id,
    protocol = protocol,
    name = name,
    mac = mac,
    vendor = vendor,
    intent = intent,
    port = port,
    ipv4 = ipv4,
    location = location,
    server = server,
    txt = txt,
    services = services,
    connectable = connectable,
    seenAt = seenAt,
    lastSeenMs = lastSeenMs,
)

class FavoritesStore(private val context: Context) {
    private val gson = Gson()
    private val listType = object : TypeToken<List<SavedDevice>>() {}.type

    val flow: Flow<List<SavedDevice>> = context.favoritesDataStore.data.map { decode(it) }

    private fun decode(prefs: Preferences): List<SavedDevice> {
        val json = prefs[FAVORITES_KEY] ?: return emptyList()
        return runCatching { gson.fromJson<List<SavedDevice>>(json, listType) ?: emptyList() }
            .getOrDefault(emptyList())
    }

    suspend fun load(): List<SavedDevice> = flow.first()

    suspend fun save(devices: List<SavedDevice>) {
        val json = gson.toJson(devices, listType)
        context.favoritesDataStore.edit { it[FAVORITES_KEY] = json }
    }
}
