package com.rodriguesacai.entregador.ui.screens.rides

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rodriguesacai.entregador.data.Ride
import com.rodriguesacai.entregador.ui.components.AddressBlock
import com.rodriguesacai.entregador.ui.components.BasePage
import com.rodriguesacai.entregador.ui.components.DangerButton
import com.rodriguesacai.entregador.ui.components.Field
import com.rodriguesacai.entregador.ui.components.Metric
import com.rodriguesacai.entregador.ui.components.PrimaryButton
import com.rodriguesacai.entregador.ui.safeDistance
import com.rodriguesacai.entregador.ui.safeEta
import com.rodriguesacai.entregador.ui.safeMoney
import com.rodriguesacai.entregador.ui.theme.AppColors

@Composable
fun UrgentRideScreen(
    ride: Ride?,
    onBack: () -> Unit,
    onAccept: (String) -> Unit,
    onReject: (String, String) -> Unit
) {
    var reason by remember { mutableStateOf("") }
    if (ride == null) {
        BasePage("Corrida urgente", "Nenhuma oferta ativa", onBack) {
            Text("Quando o gestor enviar uma corrida, ela aparece aqui em tela cheia.", color = AppColors.Muted)
        }
        return
    }
    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(AppColors.Ink, Color(0xFF173E2F)))).padding(18.dp)) {
        Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text("Nova corrida", color = Color.White, fontWeight = FontWeight.Black, fontSize = 32.sp)
            Text("Pedido ${ride.numeroPedido}", color = Color.White.copy(alpha = .8f), fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Metric("Valor", safeMoney(ride.valorCorrida), AppColors.Green, Modifier.weight(1f))
                Metric("Distância", safeDistance(ride.distanciaKm), AppColors.Ink, Modifier.weight(1f))
                Metric("Tempo", safeEta(ride.tempoEstimadoMin), AppColors.Ink, Modifier.weight(1f))
            }
            AddressBlock("Coleta", ride.lojaNome, ride.lojaEndereco)
            AddressBlock("Entrega", ride.clienteBairro, "Endereço completo será liberado na etapa correta")
            Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp)) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Field(reason, { reason = it }, "Motivo se recusar")
                    PrimaryButton("Aceitar corrida") { onAccept(ride.id) }
                    DangerButton("Recusar") { onReject(ride.id, reason.ifBlank { "Sem motivo informado" }) }
                }
            }
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
    Box(
        Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(AppColors.Ink, Color(0xFF173E2F)))).padding(22.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(18.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(title, color = Color.White, fontWeight = FontWeight.Black, fontSize = 32.sp, textAlign = TextAlign.Center)
            Text(body, color = Color.White.copy(alpha = .82f), textAlign = TextAlign.Center)
            Text("Corrida: ${rideId.ifBlank { "aguardando sincronização" }}", color = AppColors.Green, fontWeight = FontWeight.Bold)
            PrimaryButton("Aceitar agora") { onAccept() }
            DangerButton("Recusar") { onReject() }
            androidx.compose.material3.TextButton(onClick = onClose) { Text("Fechar", color = Color.White) }
        }
    }
}
