package com.rodriguesacai.entregador.service

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.rodriguesacai.entregador.data.DriverRepository

class RideFirebaseMessagingService : FirebaseMessagingService() {
    override fun onMessageReceived(message: RemoteMessage) {
        val data = message.data
        val type = data["type"] ?: return

        if (type == "NEW_RIDE") {
            NotificationHelper.urgentRideNotification(
                context = this,
                rideId = data["rideId"] ?: "sem-id",
                value = data["value"] ?: "R$ --",
                distance = data["distance"] ?: "-- km",
                pickup = data["pickup"] ?: "Rodrigues Açaí e Cia",
                dropoff = data["dropoff"] ?: "Endereço do cliente liberado após aceite"
            )
        }
    }

    override fun onNewToken(token: String) {
        DriverRepository.saveMessagingToken(this)
    }
}
