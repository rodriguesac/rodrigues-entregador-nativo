package com.rodriguesacai.entregador

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.rodriguesacai.entregador.data.DriverRepository
import com.rodriguesacai.entregador.ui.UrgentRideScreen

class UrgentRideActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        )
        val rideId = intent.getStringExtra("rideId") ?: "sem-id"
        val value = intent.getStringExtra("value") ?: "R$ --"
        val distance = intent.getStringExtra("distance") ?: "-- km"
        val duration = intent.getStringExtra("duration") ?: "-- min"
        val pickup = intent.getStringExtra("pickup") ?: "Rodrigues Açaí e Cia"
        val dropoff = intent.getStringExtra("dropoff") ?: "Endereço do cliente liberado após aceite"
        setContent {
            RodriguesNativeTheme {
                UrgentRideScreen(
                    rideId = rideId,
                    value = value,
                    distance = distance,
                    duration = duration,
                    pickup = pickup,
                    dropoff = dropoff,
                    onAccept = { DriverRepository.acceptRide(this, rideId, onDone = { finish() }, onError = { finish() }) },
                    onReject = { DriverRepository.rejectRide(this, rideId, onDone = { finish() }, onError = { finish() }) },
                    onExpired = { DriverRepository.expireRide(this, rideId, onDone = { finish() }, onError = { finish() }) }
                )
            }
        }
    }
}
