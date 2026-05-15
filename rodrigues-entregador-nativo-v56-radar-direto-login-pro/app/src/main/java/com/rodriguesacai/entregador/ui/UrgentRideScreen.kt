package com.rodriguesacai.entregador.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

private val DeepTop = Color(0xFF0B0A10)
private val DeepBottom = Color(0xFF050507)
private val Purple = Color(0xFF6D36D9)
private val Purple2 = Color(0xFF9B6DFF)
private val Lime = Color(0xFF82C91E)
private val Muted = Color(0xFFC9C6D3)
private val Danger = Color(0xFFFF4D6D)

@Composable
fun UrgentRideScreen(
    rideId: String,
    value: String,
    distance: String,
    duration: String,
    pickup: String,
    dropoff: String,
    onAccept: () -> Unit,
    onReject: () -> Unit,
    onExpired: () -> Unit
) {
    var seconds by remember(rideId) { mutableStateOf(60) }
    LaunchedEffect(rideId) {
        while (seconds > 0) {
            delay(1000)
            seconds -= 1
        }
        onExpired()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(DeepTop, DeepBottom)))
            .padding(18.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Pill("OFERTA URGENTE", Lime)
                    Spacer(Modifier.weight(1f))
                    Countdown(seconds)
                }
                Spacer(Modifier.height(24.dp))
                Text("Nova corrida", color = Color.White, fontSize = 38.sp, fontWeight = FontWeight.Black)
                Text(value, color = Color.White, fontSize = 60.sp, fontWeight = FontWeight.Black)
                Text("$distance • $duration", color = Muted, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            }

            Card(
                shape = RoundedCornerShape(34.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = .08f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color.White.copy(alpha = .12f), RoundedCornerShape(34.dp))
            ) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    RealDeliveryMap(
                        title = "Mapa real da oferta",
                        subtitle = "$distance • $duration",
                        pickupAddress = pickup,
                        dropoffAddress = dropoff.ifBlank { "Campo Grande, MS" },
                        modifier = Modifier.height(190.dp)
                    )
                    RouteLine("COLETA", pickup, Lime)
                    RouteLine("ENTREGA", dropoff.ifBlank { "Bairro da entrega" }, Purple2)
                    Text("Endereço completo da entrega liberado após aceite.", color = Muted, fontSize = 12.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(onClick = onReject, modifier = Modifier.weight(1f).height(60.dp), shape = RoundedCornerShape(18.dp)) {
                            Text("Recusar", fontWeight = FontWeight.Bold, color = Danger)
                        }
                        Button(onClick = onAccept, modifier = Modifier.weight(1.45f).height(60.dp), shape = RoundedCornerShape(18.dp), colors = ButtonDefaults.buttonColors(containerColor = Lime, contentColor = Color(0xFF10200A))) {
                            Text("Aceitar", fontSize = 18.sp, fontWeight = FontWeight.Black)
                        }
                    }
                    Text("Pedido #${rideId.takeLast(6).uppercase()}", color = Color(0xFF95899E), fontSize = 12.sp, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                }
            }
        }
    }
}

@Composable
private fun Pill(text: String, color: Color) {
    Box(Modifier.clip(RoundedCornerShape(999.dp)).background(color.copy(alpha = .16f)).padding(horizontal = 14.dp, vertical = 8.dp)) {
        Text(text, color = color, fontSize = 13.sp, fontWeight = FontWeight.Black)
    }
}

@Composable
private fun Countdown(seconds: Int) {
    Box(
        modifier = Modifier
            .size(76.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.10f))
            .border(6.dp, if (seconds <= 10) Danger else Lime, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(seconds.toString(), color = Color.White, fontSize = 25.sp, fontWeight = FontWeight.Black)
            Text("seg", color = Muted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun RouteLine(label: String, value: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(12.dp).clip(CircleShape).background(color))
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(label, color = Muted, fontSize = 12.sp, fontWeight = FontWeight.Black)
            Text(value, color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
    }
}
