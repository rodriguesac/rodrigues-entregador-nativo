package com.rodriguesacai.entregador.ui.screens.rides

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DeliveryDining
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.rodriguesacai.entregador.data.Ride
import com.rodriguesacai.entregador.ui.components.EmptyState
import com.rodriguesacai.entregador.ui.components.V17StandaloneUrgentContent
import com.rodriguesacai.entregador.ui.components.V17UrgentRideLayout
import com.rodriguesacai.entregador.ui.design.UpColors
import com.rodriguesacai.entregador.ui.theme.RodriguesTheme

@Composable
fun UrgentRideScreen(ride: Ride?, onBack: () -> Unit, onAccept: (String) -> Unit, onReject: (String, String) -> Unit) {
    if (ride == null) {
        Column(
            Modifier
                .fillMaxSize()
                .background(UpColors.Screen)
                .padding(18.dp),
            verticalArrangement = Arrangement.Center
        ) {
            EmptyState(
                "Nenhuma oferta ativa",
                "A tela urgente abre quando uma corrida real for enviada para este entregador.",
                Icons.Rounded.DeliveryDining,
                "Voltar",
                onBack
            )
        }
        return
    }
    V17UrgentRideLayout(ride = ride, onAccept = onAccept, onReject = onReject)
}

@Composable
fun UrgentRideStandaloneScreen(
    rideId: String,
    title: String,
    body: String,
    onAccept: () -> Unit,
    onReject: () -> Unit,
    onClose: () -> Unit
) {
    RodriguesTheme {
        V17StandaloneUrgentContent(
            rideId = rideId,
            title = title,
            body = body,
            onAccept = onAccept,
            onReject = onReject,
            onClose = onClose
        )
    }
}
