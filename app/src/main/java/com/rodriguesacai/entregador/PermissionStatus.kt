package com.rodriguesacai.entregador

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.PowerManager
import androidx.core.content.ContextCompat

object PermissionStatusReader {
    fun read(context: Context): PermissionStatus {
        val notifications = if (Build.VERSION.SDK_INT >= 33) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        } else true

        val fine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

        val fullScreen = if (Build.VERSION.SDK_INT >= 34) {
            runCatching { context.getSystemService(NotificationManager::class.java).canUseFullScreenIntent() }.getOrDefault(false)
        } else true

        val battery = runCatching {
            val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
            pm.isIgnoringBatteryOptimizations(context.packageName)
        }.getOrDefault(true)

        return PermissionStatus(
            notifications = notifications,
            location = fine || coarse,
            fullScreenIntent = fullScreen,
            batteryUnrestricted = battery
        )
    }
}

data class PermissionStatus(
    val notifications: Boolean,
    val location: Boolean,
    val fullScreenIntent: Boolean,
    val batteryUnrestricted: Boolean
) {
    val ready: Boolean get() = notifications && location && fullScreenIntent && batteryUnrestricted
}
