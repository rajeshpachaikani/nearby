package `in`.unartech.nearbydevs

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import `in`.unartech.nearbydevs.ui.navigation.NearbyDevsNavGraph

class MainActivity : ComponentActivity() {

    private var hasBlePermissionState by mutableStateOf(false)

    private val blePermissions: Array<String> = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> arrayOf(
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT,
        )
        else -> arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        )
    }

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        hasBlePermissionState = results.values.all { it }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkInitialPermissions()

        enableEdgeToEdge()
        setContent {
            NearbyDevsNavGraph(
                onRequestBlePermissions = { requestBlePermissions() },
                onOpenBluetoothSettings = { openBluetoothSettings() },
                onOpenAppSettings = { openAppSettings() },
                hasBlePermission = hasBlePermissionState,
            )
        }
    }

    override fun onResume() {
        super.onResume()
        checkInitialPermissions()
    }

    private fun checkInitialPermissions() {
        hasBlePermissionState = blePermissions.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestBlePermissions() {
        permissionLauncher.launch(blePermissions)
    }

    private fun openBluetoothSettings() {
        val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        startActivity(intent)
    }

    private fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = android.net.Uri.fromParts("package", packageName, null)
        }
        startActivity(intent)
    }
}
