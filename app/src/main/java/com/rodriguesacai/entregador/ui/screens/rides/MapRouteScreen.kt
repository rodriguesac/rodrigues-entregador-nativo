package com.rodriguesacai.entregador.ui.screens.rides

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.rodriguesacai.entregador.data.Ride
import com.rodriguesacai.entregador.ui.components.AddressBlock
import com.rodriguesacai.entregador.ui.components.BasePage
import com.rodriguesacai.entregador.ui.components.NativeMapPreview
import com.rodriguesacai.entregador.ui.components.OutlineAction
import com.rodriguesacai.entregador.ui.deliveryAddressVisible
import com.rodriguesacai.entregador.ui.pickupVisibleAddress
import com.rodriguesacai.entregador.ui.safeDeliveryAddress

@Composable
fun MapRouteScreen(ride: Ride?, onBack: () -> Unit) {
    val context = LocalContext.current
    BasePage("Mapa da rota", "Mapa limpo, rota visível e ações fora do mapa", onBack) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            NativeMapPreview(ride, Modifier.fillMaxWidth().height(420.dp))
            if (ride != null) {
                val entregaTitulo = if (ride.deliveryAddressVisible()) ride.clienteNome else ride.clienteBairro
                val entregaTexto = ride.safeDeliveryAddress()
                AddressBlock("Coleta", ride.lojaNome, ride.pickupVisibleAddress())
                AddressBlock("Entrega", entregaTitulo, entregaTexto)
            }
            OutlineAction("Iniciar navegação") { ride?.let { openNavigation(context, it) } }
        }
    }
}

private fun openNavigation(context: Context, ride: Ride) {
    val goingToDelivery = ride.deliveryAddressVisible()
    val lat = if (goingToDelivery) ride.clienteLat else ride.lojaLat
    val lng = if (goingToDelivery) ride.clienteLng else ride.lojaLng
    val google = Intent(Intent.ACTION_VIEW, Uri.parse("google.navigation:q=$lat,$lng&mode=d")).apply {
        setPackage("com.google.android.apps.maps")
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    val fallback = Intent(Intent.ACTION_VIEW, Uri.parse("geo:$lat,$lng?q=$lat,$lng")).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    runCatching { context.startActivity(google) }.onFailure { context.startActivity(fallback) }
}
