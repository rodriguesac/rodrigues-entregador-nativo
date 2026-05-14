package com.rodriguesacai.entregador.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import com.rodriguesacai.entregador.MainActivity
import com.rodriguesacai.entregador.R
import com.rodriguesacai.entregador.UrgentRideActivity

object NotificationHelper {
    const val CHANNEL_ONLINE = "driver_online"
    const val CHANNEL_URGENT = "urgent_ride"

    fun createChannels(context: Context) {
        if (Build.VERSION.SDK_INT < 26) return

        val manager = context.getSystemService(NotificationManager::class.java)

        val online = NotificationChannel(
            CHANNEL_ONLINE,
            "Entregador online",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Serviço ativo enquanto o entregador está disponível"
        }

        val urgent = NotificationChannel(
            CHANNEL_URGENT,
            "Corrida urgente",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Alertas de novas corridas em tela cheia"
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            enableVibration(true)
            setSound(
                Uri.parse("android.resource://${context.packageName}/${R.raw.alerta}"),
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
        }

        manager.createNotificationChannel(online)
        manager.createNotificationChannel(urgent)
    }

    fun onlineNotification(context: Context): Notification {
        val pending = PendingIntent.getActivity(
            context,
            1,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(context, CHANNEL_ONLINE)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Você está online")
            .setContentText("Serviço ativo. Aguardando entregas próximas.")
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(pending)
            .build()
    }

    fun urgentRideNotification(
        context: Context,
        rideId: String,
        value: String,
        distance: String,
        pickup: String,
        dropoff: String
    ) {
        val fullScreenIntent = Intent(context, UrgentRideActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("rideId", rideId)
            putExtra("value", value)
            putExtra("distance", distance)
            putExtra("pickup", pickup)
            putExtra("dropoff", dropoff)
        }
        val fullScreenPendingIntent = PendingIntent.getActivity(
            context,
            2,
            fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_URGENT)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Nova corrida disponível")
            .setContentText("$value • $distance")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(1001, notification)
        context.startActivity(fullScreenIntent)
    }
}
