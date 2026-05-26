package com.rodriguesacai.entregador.ui

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rodriguesacai.entregador.RodriguesFonts
import kotlinx.coroutines.delay

private val Font = RodriguesFonts.Montserrat
private val Bg = Color.White
private val Ink = Color(0xFF101216)
private val Muted = Color(0xFF677381)
private val Border = Color(0xFFE8EEF3)
private val Green = Color(0xFF0FAE4B)
private val Orange = Color(0xFFFF7A00)
private val Red = Color(0xFFEF233C)
private val SurfaceSoft = Color(0xFFF7FAFC)
private val GreenSoft = Color(0xFFEAF8EF)
private val RedSoft = Color(0xFFFFEAEE)

@Composable
fun UrgentRideScreen(
    rideId: String,
    value: String,
    distance: String,
    duration: String,
    pickup: String,
    dropoff: String,
    paymentMethod: String = "",
    paymentStatus: String = "",
    amountToCollect: String = "",
    changeFor: String = "",
    requiresMachine: String = "",
    onAccept: () -> Unit,
    onReject: () -> Unit,
    onExpired: () -> Unit
) {
    var seconds by remember(rideId) { mutableStateOf(60) }
    LaunchedEffect(rideId) {
        seconds = 60
        while (seconds > 0) {
            delay(1000)
            seconds -= 1
        }
        onExpired()
    }

    Column(
        modifier = Modifier.fillMaxSize().background(Bg).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        UrgentTop(seconds)
        Card(
            modifier = Modifier.fillMaxWidth().border(1.dp, Border, RoundedCornerShape(30.dp)),
            shape = RoundedCornerShape(30.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Nova corrida", color = Ink, fontFamily = Font, fontSize = 28.sp, fontWeight = FontWeight.Black)
                Text(value.ifBlank { "Valor a definir" }, color = Green, fontFamily = Font, fontSize = 42.sp, fontWeight = FontWeight.Black)
                Text(listOf(distance, duration).filter { it.isNotBlank() }.joinToString(" • ").ifBlank { "Dados da rota aguardando sincronização" }, color = Muted, fontFamily = Font, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }
        RealDeliveryMap(
            title = "Oferta #${rideId.takeLast(6).uppercase()}",
            subtitle = listOf(distance, duration).filter { it.isNotBlank() }.joinToString(" • "),
            pickupAddress = pickup,
            dropoffAddress = dropoff,
            mode = DeliveryMapMode.PICKUP_TO_DROPOFF,
            modifier = Modifier.height(230.dp)
        )
        Card(
            modifier = Modifier.fillMaxWidth().border(1.dp, Border, RoundedCornerShape(30.dp)),
            shape = RoundedCornerShape(30.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                RouteLine(Icons.Filled.Storefront, "Coleta", pickup.ifBlank { "Rodrigues Açaí e Cia." }, Green)
                RouteLine(Icons.Filled.Place, "Entrega", dropoff.ifBlank { "Área da entrega" }, Orange)
                UrgentPaymentLine(paymentMethod, paymentStatus, amountToCollect, changeFor, requiresMachine)
            }
        }
        Spacer(Modifier.weight(1f))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(onClick = onReject, modifier = Modifier.weight(1f).height(58.dp), shape = RoundedCornerShape(22.dp)) {
                Text("Recusar", color = Red, fontFamily = Font, fontWeight = FontWeight.Black)
            }
            Button(
                onClick = onAccept,
                modifier = Modifier.weight(1.4f).height(58.dp),
                shape = RoundedCornerShape(22.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Green, contentColor = Color.White)
            ) {
                Icon(Icons.Filled.CheckCircle, null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Aceitar", fontFamily = Font, fontSize = 16.sp, fontWeight = FontWeight.Black)
            }
        }
        Text("Pedido #${rideId.takeLast(6).uppercase()}", color = Muted, fontFamily = Font, fontSize = 11.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
    }
}

@Composable
private fun UrgentTop(seconds: Int) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Row(Modifier.clip(RoundedCornerShape(999.dp)).background(RedSoft).border(1.dp, Red.copy(alpha = .25f), RoundedCornerShape(999.dp)).padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Bolt, null, tint = Red, modifier = Modifier.size(17.dp))
            Spacer(Modifier.width(6.dp))
            Text("URGENTE", color = Red, fontFamily = Font, fontSize = 12.sp, fontWeight = FontWeight.Black)
        }
        Spacer(Modifier.weight(1f))
        Box(Modifier.size(62.dp).clip(CircleShape).background(if (seconds <= 10) Red else Green).border(4.dp, Color.White, CircleShape), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(seconds.toString(), color = Color.White, fontFamily = Font, fontSize = 21.sp, fontWeight = FontWeight.Black)
                Text("seg", color = Color.White.copy(alpha = .85f), fontFamily = Font, fontSize = 9.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun RouteLine(icon: ImageVector, label: String, value: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(11.dp), modifier = Modifier.fillMaxWidth()) {
        Box(Modifier.size(38.dp).clip(CircleShape).background(color.copy(alpha = .12f)), contentAlignment = Alignment.Center) { Icon(icon, null, tint = color, modifier = Modifier.size(20.dp)) }
        Column(Modifier.weight(1f)) {
            Text(label, color = Muted, fontFamily = Font, fontSize = 10.sp, fontWeight = FontWeight.Black)
            Text(value.ifBlank { "Não informado" }, color = Ink, fontFamily = Font, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun UrgentPaymentLine(method: String, status: String, amount: String, changeFor: String, requiresMachine: String) {
    val raw = method.ifBlank { status }.ifBlank { "Pagamento não informado" }
    val machine = requiresMachine.equals("true", ignoreCase = true) || raw.contains("cart", ignoreCase = true) || raw.contains("maquin", ignoreCase = true)
    val paid = raw.contains("pago", ignoreCase = true) || raw.contains("online", ignoreCase = true)
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(SurfaceSoft).padding(13.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(if (machine) Icons.Filled.CreditCard else Icons.Filled.Payments, null, tint = if (machine) Orange else Green, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text(if (paid) "Pago online" else raw, color = Ink, fontFamily = Font, fontSize = 15.sp, fontWeight = FontWeight.Black, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Text(if (paid) "Nada a cobrar" else amount.ifBlank { "Confirmar pagamento com a operação" }, color = Muted, fontFamily = Font, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        if (machine) Text("Maquininha necessária", color = Orange, fontFamily = Font, fontSize = 12.sp, fontWeight = FontWeight.Black)
        if (changeFor.isNotBlank()) Text("Troco para $changeFor", color = Red, fontFamily = Font, fontSize = 12.sp, fontWeight = FontWeight.Black)
    }
}
