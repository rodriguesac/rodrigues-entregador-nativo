package com.rodriguesacai.entregador.data

import android.content.Context
import android.provider.Settings
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.messaging.FirebaseMessaging

object DriverRepository {
    private val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    fun driverId(context: Context): String {
        val androidId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID) ?: "sem-device"
        return "driver_$androidId"
    }

    fun setOnline(context: Context, online: Boolean) {
        val id = driverId(context)
        val payload = linkedMapOf<String, Any?>(
            "id" to id,
            "name" to "Entregador Rodrigues",
            "online" to online,
            "status" to if (online) "available" else "offline",
            "updatedAt" to Timestamp.now(),
            "platform" to "android"
        )
        db.collection("drivers").document(id).set(payload, com.google.firebase.firestore.SetOptions.merge())
        if (online) saveMessagingToken(context)
    }

    fun saveMessagingToken(context: Context) {
        val id = driverId(context)
        FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
            db.collection("drivers").document(id).set(
                mapOf(
                    "fcmToken" to token,
                    "tokenUpdatedAt" to Timestamp.now()
                ),
                com.google.firebase.firestore.SetOptions.merge()
            )
        }
    }

    fun listenPendingRide(
        context: Context,
        onRide: (DriverRide?) -> Unit,
        onError: (String) -> Unit
    ): ListenerRegistration {
        val id = driverId(context)
        return db.collection("rides")
            .whereEqualTo("status", "pending")
            .addSnapshotListener { snap, err ->
                if (err != null) {
                    onError(err.message ?: "Erro ao ouvir corridas")
                    return@addSnapshotListener
                }
                val ride = snap?.documents
                    ?.mapNotNull { it.toDriverRide() }
                    ?.firstOrNull { it.assignedDriverId.isBlank() || it.assignedDriverId == id }
                onRide(ride)
            }
    }

    fun listenMyActiveRide(
        context: Context,
        onRide: (DriverRide?) -> Unit,
        onError: (String) -> Unit
    ): ListenerRegistration {
        val id = driverId(context)
        return db.collection("rides")
            .whereEqualTo("driverId", id)
            .addSnapshotListener { snap, err ->
                if (err != null) {
                    onError(err.message ?: "Erro ao ouvir corrida ativa")
                    return@addSnapshotListener
                }
                val ride = snap?.documents
                    ?.mapNotNull { it.toDriverRide() }
                    ?.firstOrNull { it.status in listOf("accepted", "pickup", "delivering") }
                onRide(ride)
            }
    }

    fun acceptRide(context: Context, rideId: String, onDone: () -> Unit = {}, onError: (String) -> Unit = {}) {
        val id = driverId(context)
        val update = mapOf(
            "status" to "accepted",
            "driverId" to id,
            "assignedDriverId" to id,
            "acceptedAt" to Timestamp.now(),
            "updatedAt" to Timestamp.now()
        )
        db.collection("rides").document(rideId).update(update)
            .addOnSuccessListener {
                addHistory(context, rideId, "accepted")
                onDone()
            }
            .addOnFailureListener { onError(it.message ?: "Falha ao aceitar") }
    }

    fun rejectRide(context: Context, rideId: String, onDone: () -> Unit = {}, onError: (String) -> Unit = {}) {
        val id = driverId(context)
        db.collection("rides").document(rideId).collection("rejections").document(id).set(
            mapOf("driverId" to id, "reason" to "rejected_by_driver", "createdAt" to Timestamp.now())
        )
        db.collection("rides").document(rideId).update(
            mapOf("lastRejectedBy" to id, "updatedAt" to Timestamp.now())
        ).addOnSuccessListener {
            addHistory(context, rideId, "rejected")
            onDone()
        }.addOnFailureListener { onError(it.message ?: "Falha ao rejeitar") }
    }

    fun updateRideStatus(context: Context, rideId: String, status: String, onDone: () -> Unit = {}, onError: (String) -> Unit = {}) {
        val fields = mutableMapOf<String, Any>("status" to status, "updatedAt" to Timestamp.now())
        when (status) {
            "pickup" -> fields["pickupStartedAt"] = Timestamp.now()
            "delivering" -> fields["deliveryStartedAt"] = Timestamp.now()
            "finished" -> fields["finishedAt"] = Timestamp.now()
        }
        db.collection("rides").document(rideId).update(fields)
            .addOnSuccessListener {
                addHistory(context, rideId, status)
                onDone()
            }
            .addOnFailureListener { onError(it.message ?: "Falha ao atualizar") }
    }

    private fun addHistory(context: Context, rideId: String, action: String) {
        val id = driverId(context)
        db.collection("driverHistory").add(
            mapOf(
                "driverId" to id,
                "rideId" to rideId,
                "action" to action,
                "createdAt" to Timestamp.now()
            )
        )
    }
}

data class DriverRide(
    val id: String,
    val status: String,
    val value: String,
    val distance: String,
    val duration: String,
    val pickup: String,
    val dropoff: String,
    val assignedDriverId: String,
    val customerName: String
)

private fun DocumentSnapshot.toDriverRide(): DriverRide? {
    return DriverRide(
        id = id,
        status = getString("status") ?: "pending",
        value = getString("value") ?: getDouble("valueNumber")?.let { "R$ %.2f".format(it).replace('.', ',') } ?: "R$ --",
        distance = getString("distance") ?: "-- km",
        duration = getString("duration") ?: "-- min",
        pickup = getString("pickup") ?: getString("pickupAddress") ?: "Rodrigues Açaí e Cia",
        dropoff = getString("dropoff") ?: getString("dropoffAddress") ?: "Endereço do cliente liberado após aceite",
        assignedDriverId = getString("assignedDriverId") ?: "",
        customerName = getString("customerName") ?: "Cliente"
    )
}
