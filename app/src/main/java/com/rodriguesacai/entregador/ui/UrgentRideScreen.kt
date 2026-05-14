package com.rodriguesacai.entregador.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun UrgentRideScreen(
    rideId: String,
    value: String,
    distance: String,
    pickup: String,
    dropoff: String,
    onAccept: () -> Unit,
    onReject: () -> Unit
) {
    var seconds by remember { mutableStateOf(60) }
    LaunchedEffect(Unit) {
        while (seconds > 0) {
            delay(1000)
            seconds -= 1
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF09130E), Color(0xFF062D1B), Color(0xFF05080A))))
            .padding(18.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(color = Color(0xFF083C25), shape = RoundedCornerShape(999.dp)) {
                        Text("NOVA ROTA", color = Color(0xFF58F7A5), fontSize = 13.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp))
                    }
                    Spacer(Modifier.weight(1f))
                    Countdown(seconds.toString())
                }
                Spacer(Modifier.height(28.dp))
                Text("Aceitar a rota?", color = Color.White, fontSize = 36.sp, fontWeight = FontWeight.Black)
                Text(value, color = Color.White, fontSize = 58.sp, fontWeight = FontWeight.Black)
                Text("$distance • 22 min • 2 paradas", color = Color(0xFFD7EFE2), fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            }

            Card(
                shape = RoundedCornerShape(34.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    RoutePreview()
                    RouteLine("COLETA", pickup, Color(0xFF078244))
                    RouteLine("ENTREGA", dropoff, Color(0xFFE90045))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(onClick = onReject, modifier = Modifier.weight(1f).height(60.dp), shape = RoundedCornerShape(18.dp)) {
                            Text("Rejeitar", fontWeight = FontWeight.Bold, color = Color(0xFFE90045))
                        }
                        Button(onClick = onAccept, modifier = Modifier.weight(1.45f).height(60.dp), shape = RoundedCornerShape(18.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF078244))) {
                            Text("Aceitar", fontSize = 18.sp, fontWeight = FontWeight.Black)
                        }
                    }
                    Text("ID: $rideId", color = Color(0xFF86798E), fontSize = 12.sp, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                }
            }
        }
    }
}

@Composable
private fun Countdown(text: String) {
    Box(
        modifier = Modifier
            .size(76.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.10f))
            .border(6.dp, Color.White, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = Color.White, fontSize = 25.sp, fontWeight = FontWeight.Black)
    }
}

@Composable
private fun RoutePreview() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(Brush.linearGradient(listOf(Color(0xFFF4F6F5), Color(0xFFE9EEEC))))
            .border(1.dp, Color(0xFFE0E2E0), RoundedCornerShape(24.dp))
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val grid = Color(0xFFBFC8C5).copy(alpha = 0.55f)
            for (i in 0..6) {
                val y = size.height * i / 6f
                drawLine(grid, Offset(0f, y), Offset(size.width, y + 28f), strokeWidth = 2f)
            }
            for (i in 0..5) {
                val x = size.width * i / 5f
                drawLine(grid, Offset(x, 0f), Offset(x - 45f, size.height), strokeWidth = 2f)
            }
            drawLine(Color(0xFFE90045), Offset(size.width * .18f, size.height * .70f), Offset(size.width * .82f, size.height * .35f), strokeWidth = 8f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 12f), 0f))
        }
        Surface(color = Color.White, shape = RoundedCornerShape(16.dp), modifier = Modifier.align(Alignment.Center)) {
            Text("Preview da rota", color = Color(0xFF222222), modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp), fontWeight = FontWeight.Black)
        }
    }
}

@Composable
private fun RouteLine(label: String, value: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(12.dp).clip(CircleShape).background(color))
        Spacer(Modifier.width(10.dp))
        Column {
            Text(label, color = Color(0xFF7A707F), fontSize = 12.sp, fontWeight = FontWeight.Black)
            Text(value, color = Color(0xFF171219), fontSize = 17.sp, fontWeight = FontWeight.Bold)
        }
    }
}
