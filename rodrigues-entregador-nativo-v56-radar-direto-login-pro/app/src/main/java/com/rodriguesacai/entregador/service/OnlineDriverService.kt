package com.rodriguesacai.entregador.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.google.firebase.firestore.ListenerRegistration
import com.rodriguesacai.entregador.data.DriverRepository

class OnlineDriverService : Service() {
    private var pendingRideListener: ListenerRegistration? = null
    private var lastNotifiedRideId: String? = null

    override fun onCreate() {
        super.onCreate()
        DriverRepository.setOnline(this, true)
        startForeground(10, NotificationHelper.onlineNotification(this))
        startRideListener()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        DriverRepository.setOnline(this, true)
        if (pendingRideListener == null) startRideListener()
        return START_STICKY
    }

    private fun startRideListener() {
        pendingRideListener?.remove()
        pendingRideListener = DriverRepository.listenPendingRide(
            context = this,
            onRide = { ride ->
                if (ride != null && ride.id != lastNotifiedRideId) {
                    lastNotifiedRideId = ride.id
                    NotificationHelper.urgentRideNotification(
                        context = this,
                        rideId = ride.id,
                        value = ride.value,
                        distance = ride.distance,
                        duration = ride.duration,
                        pickup = ride.pickup,
                        dropoff = ride.dropoff
                    )
                }
            },
            onError = { }
        )
    }

    override fun onDestroy() {
        pendingRideListener?.remove()
        pendingRideListener = null
        // Não marca offline aqui: se o Android matar/recriar o serviço, o entregador continua disponível.
        // O offline real é gravado quando o motoboy desliga o status no app.
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
