package com.rodriguesacai.entregador

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toUri
import com.rodriguesacai.entregador.service.OnlineDriverService
import com.rodriguesacai.entregador.ui.DriverHomeScreen

class MainActivity : ComponentActivity() {
    private var pendingOnlineStart: Boolean = false

    private val notificationLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    private val locationLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        val fine = result[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarse = result[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (pendingOnlineStart && (fine || coarse)) startOnlineService()
        pendingOnlineStart = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        askNotificationPermissionOnly()
        setContent {
            RodriguesNativeTheme {
                DriverHomeScreen(
                    onGoOnline = { requestLocationAndStartOnline() },
                    onGoOffline = { stopService(Intent(this, OnlineDriverService::class.java)) },
                    onOpenNavigator = { pickup, dropoff -> openNavigator(pickup, dropoff) },
                    onOpenNotificationSettings = { openNotificationSettings() },
                    onOpenLocationSettings = { openAppSettings() },
                    onOpenFullScreenSettings = { openFullScreenSettings() },
                    onOpenBatterySettings = { openBatterySettings() }
                )
            }
        }
    }

    private fun askNotificationPermissionOnly() {
        if (Build.VERSION.SDK_INT >= 33) {
            notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    private fun requestLocationAndStartOnline() {
        pendingOnlineStart = true
        locationLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
    }

    private fun startOnlineService() {
        val intent = Intent(this, OnlineDriverService::class.java)
        if (Build.VERSION.SDK_INT >= 26) startForegroundService(intent) else startService(intent)
    }

    private fun openNavigator(pickup: String, dropoff: String) {
        val destination = dropoff.ifBlank { pickup }.ifBlank { "Rodrigues Açaí e Cia" }
        val encoded = Uri.encode(destination)
        val preference = AppSettings.getNavigationApp(this)

        val intents = when (preference) {
            AppSettings.NAV_GOOGLE -> listOf(
                Intent(Intent.ACTION_VIEW, "google.navigation:q=$encoded".toUri()).apply { setPackage("com.google.android.apps.maps") },
                Intent(Intent.ACTION_VIEW, "geo:0,0?q=$encoded".toUri())
            )
            AppSettings.NAV_WAZE -> listOf(
                Intent(Intent.ACTION_VIEW, "waze://?q=$encoded&navigate=yes".toUri()).apply { setPackage("com.waze") },
                Intent(Intent.ACTION_VIEW, "https://waze.com/ul?q=$encoded&navigate=yes".toUri())
            )
            else -> listOf(
                Intent(Intent.ACTION_VIEW, "google.navigation:q=$encoded".toUri()).apply { setPackage("com.google.android.apps.maps") },
                Intent(Intent.ACTION_VIEW, "waze://?q=$encoded&navigate=yes".toUri()).apply { setPackage("com.waze") },
                Intent(Intent.ACTION_VIEW, "geo:0,0?q=$encoded".toUri())
            )
        }

        for (intent in intents) {
            if (runCatching { startActivity(intent) }.isSuccess) return
        }
    }

    private fun openNotificationSettings() {
        val intent = if (Build.VERSION.SDK_INT >= 26) {
            Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
            }
        } else {
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply { data = Uri.fromParts("package", packageName, null) }
        }
        runCatching { startActivity(intent) }.onFailure { openAppSettings() }
    }

    private fun openFullScreenSettings() {
        val intent = if (Build.VERSION.SDK_INT >= 34) {
            Intent(Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT).apply {
                data = Uri.fromParts("package", packageName, null)
            }
        } else {
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply { data = Uri.fromParts("package", packageName, null) }
        }
        runCatching { startActivity(intent) }.onFailure { openAppSettings() }
    }

    private fun openBatterySettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", packageName, null)
        }
        runCatching { startActivity(intent) }.onFailure { openAppSettings() }
    }

    private fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", packageName, null)
        }
        startActivity(intent)
    }
}
