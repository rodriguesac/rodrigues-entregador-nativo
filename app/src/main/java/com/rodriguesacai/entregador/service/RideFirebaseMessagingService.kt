package com.rodriguesacai.entregador.service

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.rodriguesacai.entregador.data.DriverRepository

class RideFirebaseMessagingService : FirebaseMessagingService() {
    override fun onMessageReceived(message: RemoteMessage) {
        val data = message.data
        val type = (data["type"] ?: data["event"] ?: data["acao"] ?: data["action"] ?: "").lowercase()
        val rideId = data["rideId"] ?: data["rotaId"] ?: data["pedidoId"] ?: data["id"]

        val looksLikeRide = rideId != null || type in setOf("new_ride", "newride", "nova_corrida", "nova_rota", "pedido_novo", "new_order")
        if (!looksLikeRide) return

        NotificationHelper.urgentRideNotification(
            context = this,
            rideId = rideId ?: "sem-id",
            value = data["value"] ?: data["valor"] ?: data["valorRota"] ?: "R$ --",
            distance = data["distance"] ?: data["distancia"] ?: data["distanciaKm"] ?: "-- km",
            duration = data["duration"] ?: data["tempo"] ?: data["tempoMin"] ?: "-- min",
            pickup = data["pickup"] ?: data["pickupAddress"] ?: data["lojaEndereco"] ?: data["nomeLoja"] ?: "Rodrigues Açaí e Cia",
            dropoff = data["dropoff"] ?: data["dropoffAddress"] ?: data["enderecoEntrega"] ?: data["enderecoCompleto"] ?: "Endereço liberado após aceite"
        )
    }

    override fun onNewToken(token: String) {
        DriverRepository.saveMessagingToken(this)
    }
}
