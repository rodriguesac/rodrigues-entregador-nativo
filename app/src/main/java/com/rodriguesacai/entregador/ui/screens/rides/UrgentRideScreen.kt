package com.rodriguesacai.entregador.ui.screens.rides

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DeliveryDining
import androidx.compose.material.icons.rounded.Timer
import com.rodriguesacai.entregador.ui.theme.RodriguesTheme
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rodriguesacai.entregador.data.Ride
import com.rodriguesacai.entregador.ui.components.EmptyState
import com.rodriguesacai.entregador.ui.components.PrimaryAction
import com.rodriguesacai.entregador.ui.components.RideOfferCard
import com.rodriguesacai.entregador.ui.components.RoutePreviewCard
import com.rodriguesacai.entregador.ui.components.SecondaryAction
import com.rodriguesacai.entregador.ui.components.UpInfoBox
import com.rodriguesacai.entregador.ui.design.UpColors

@Composable
fun UrgentRideScreen(ride: Ride?, onBack: () -> Unit, onAccept: (String) -> Unit, onReject: (String, String) -> Unit) {
    if (ride == null) {
        Column(Modifier.fillMaxSize().padding(18.dp), verticalArrangement = Arrangement.Center) {
            EmptyState("Nenhuma oferta ativa", "A tela urgente abre quando uma corrida real for enviada para este entregador.", Icons.Rounded.DeliveryDining, "Voltar", onBack)
        }
        return
    }
    Column(Modifier.fillMaxSize().background(UpColors.Screen)) {
        Box(Modifier.fillMaxWidth().height(78.dp).background(UpColors.Red).padding(horizontal = 18.dp), contentAlignment = Alignment.Center) {
            androidx.compose.foundation.layout.Row(verticalAlignment = Alignment.CenterVertically) {
                Text("⚠", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Black)
                Spacer(Modifier.width(10.dp))
                Text("NOVA CORRIDA URGENTE", color = Color.White, fontWeight = FontWeight.Black, fontSize = 16.sp, modifier = Modifier.weight(1f))
                Text("00:18", color = Color.White, fontWeight = FontWeight.Black, fontSize = 17.sp, modifier = Modifier.background(Color.White.copy(alpha = .12f), androidx.compose.foundation.shape.RoundedCornerShape(10.dp)).padding(horizontal = 12.dp, vertical = 7.dp))
            }
        }
        Column(Modifier.weight(1f).padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            UpInfoBox("Oferta recebida", "Analise coleta, distância, tempo e valor. Endereço completo do cliente só libera após retirar o pedido.", Icons.Rounded.Timer, UpColors.Red, UpColors.RedSoft)
            RideOfferCard(ride, onClick = {})
            RoutePreviewCard(ride = ride, height = 205.dp)
            Spacer(Modifier.weight(1f))
            PrimaryAction("Aceitar", onClick = { onAccept(ride.id) })
            SecondaryAction("Recusar", onClick = { onReject(ride.id, "Recusada pelo entregador") }, red = true)
        }
    }
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
        Column(Modifier.fillMaxSize().background(UpColors.Screen)) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(84.dp)
                    .background(UpColors.Red)
                    .padding(horizontal = 18.dp),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.foundation.layout.Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("⚠", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Black)
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text(title.uppercase(), color = Color.White, fontWeight = FontWeight.Black, fontSize = 15.sp, maxLines = 1)
                        if (rideId.isNotBlank()) {
                            Text("Corrida #$rideId", color = Color.White.copy(alpha = .82f), fontSize = 12.sp, maxLines = 1)
                        }
                    }
                    IconButton(onClick = onClose) {
                        Icon(Icons.Rounded.Close, contentDescription = "Fechar", tint = Color.White)
                    }
                }
            }
            Column(
                Modifier
                    .weight(1f)
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(8.dp))
                UpInfoBox(
                    title = "Oferta urgente",
                    message = body.ifBlank { "Abra o app para ver os dados reais da corrida." },
                    icon = Icons.Rounded.DeliveryDining,
                    tint = UpColors.Red,
                    soft = UpColors.RedSoft
                )
                Text(
                    text = "Os detalhes completos serão carregados do Firebase ao abrir a corrida no app.",
                    color = UpColors.Muted,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 10.dp)
                )
                Spacer(Modifier.weight(1f))
                PrimaryAction("Aceitar corrida", onClick = onAccept)
                SecondaryAction("Recusar", onClick = onReject, red = true)
            }
        }
    }
}
