package com.rodriguesacai.entregador.ui.screens.rides

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
import com.rodriguesacai.entregador.ui.components.V17ActiveRidePanel
import com.rodriguesacai.entregador.ui.components.EmptyState
import com.rodriguesacai.entregador.ui.components.UpPage
import com.rodriguesacai.entregador.ui.navigation.AppRoute

@Composable
fun ActiveRideScreen(ride: Ride?, onBack: () -> Unit, onMap: () -> Unit, onAdvance: (Ride) -> Unit, onOccurrence: (Ride) -> Unit) {
    UpPage(title = "Corrida em andamento", onBack = onBack) {
        Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            if (ride == null) {
                EmptyState("Nenhuma corrida em andamento", "Quando uma corrida for aceita, as etapas de coleta e entrega aparecem aqui.", Icons.Rounded.DeliveryDining, "Voltar", onBack)
            } else {
                V17ActiveRidePanel(ride = ride, onMap = onMap, onAdvance = { onAdvance(ride) }, onOccurrence = { onOccurrence(ride) })
            }
        }
    }
}
