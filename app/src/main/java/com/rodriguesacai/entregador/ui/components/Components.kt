package com.rodriguesacai.entregador.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.DeliveryDining
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.ReportProblem
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.rodriguesacai.entregador.data.Driver
import com.rodriguesacai.entregador.data.Ride
import com.rodriguesacai.entregador.ui.deliveryAddressVisible
import com.rodriguesacai.entregador.ui.format1
import com.rodriguesacai.entregador.ui.humanStatus
import com.rodriguesacai.entregador.ui.pickupVisibleAddress
import com.rodriguesacai.entregador.ui.money
import com.rodriguesacai.entregador.ui.safeDeliveryAddress
import com.rodriguesacai.entregador.ui.safeDistance
import com.rodriguesacai.entregador.ui.safeEta
import com.rodriguesacai.entregador.ui.safeMoney
import com.rodriguesacai.entregador.ui.navigation.AppRoute
import com.rodriguesacai.entregador.ui.statusColor
import com.rodriguesacai.entregador.ui.theme.AppColors

@Composable
fun BasePage(
    title: String,
    subtitle: String,
    onBack: () -> Unit,
    bottomBar: (@Composable () -> Unit)? = null,
    content: @Composable Column.() -> Unit
) {
    Scaffold(
        containerColor = AppColors.Bg,
        bottomBar = { bottomBar?.invoke() }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 18.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = onBack) { Text("Voltar", color = AppColors.Ink, fontWeight = FontWeight.SemiBold) }
                Spacer(Modifier.width(8.dp))
                Column(Modifier.weight(1f)) {
                    Text(title, fontSize = 22.sp, fontWeight = FontWeight.Black, color = AppColors.Ink, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(subtitle, color = AppColors.Muted, fontSize = 13.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                }
            }
            content()
        }
    }
}

@Composable
fun AppBottomBar(current: AppRoute, onNav: (AppRoute) -> Unit) {
    NavigationBar(containerColor = Color.White, tonalElevation = 10.dp) {
        val items = listOf(
            Triple(AppRoute.Home, "Início", Icons.Rounded.Home),
            Triple(AppRoute.CorridaAndamento, "Corridas", Icons.Rounded.DeliveryDining),
            Triple(AppRoute.Carteira, "Carteira", Icons.Rounded.AccountBalanceWallet),
            Triple(AppRoute.Notificacoes, "Avisos", Icons.Rounded.Notifications),
            Triple(AppRoute.Perfil, "Mais", Icons.Rounded.MoreHoriz)
        )
        items.forEach { (route, label, icon) ->
            val selected = when (route) {
                AppRoute.CorridaAndamento -> current in listOf(AppRoute.CorridaAndamento, AppRoute.CorridaUrgente, AppRoute.Mapa, AppRoute.Ocorrencia)
                AppRoute.Carteira -> current in listOf(AppRoute.Carteira, AppRoute.Ganhos, AppRoute.PixBanco)
                AppRoute.Perfil -> current in listOf(AppRoute.Perfil, AppRoute.Solicitacao, AppRoute.Permissoes, AppRoute.SemInternet, AppRoute.Manutencao, AppRoute.ErroFirebase, AppRoute.Atualizacao)
                else -> current == route
            }
            NavigationBarItem(
                selected = selected,
                onClick = { onNav(route) },
                icon = { Icon(icon, contentDescription = label, modifier = Modifier.size(21.dp)) },
                label = { Text(label, fontSize = 11.sp, maxLines = 1) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = AppColors.Green,
                    selectedTextColor = AppColors.Green,
                    indicatorColor = AppColors.Green.copy(alpha = .10f),
                    unselectedIconColor = AppColors.Muted,
                    unselectedTextColor = AppColors.Muted
                )
            )
        }
    }
}

@Composable
fun Header(driver: Driver?, onLogout: () -> Unit) {
    val restricted = driver?.statusOperacional == "RESTRICAO"
    val subtitle = when {
        restricted -> "Resolva a restrição para receber corridas"
        driver?.online == true -> "Disponível para novas corridas"
        else -> "Ative disponibilidade para iniciar"
    }
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 2.dp, bottom = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        DriverAvatar(driver)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text("Olá, ${driver?.nome ?: "entregador"}", color = AppColors.Ink, fontWeight = FontWeight.Black, fontSize = 23.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(subtitle, color = if (restricted) AppColors.Red else AppColors.Muted, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        SmallIconButton(AppColors.Green.copy(alpha = .10f), AppColors.Green, { Icon(Icons.Rounded.ReportProblem, contentDescription = "Alertas", tint = AppColors.Green, modifier = Modifier.size(22.dp)) }) { }
        Spacer(Modifier.width(8.dp))
        SmallIconButton(AppColors.Green.copy(alpha = .10f), AppColors.Green, { Icon(Icons.Rounded.MoreVert, contentDescription = "Mais", tint = AppColors.Green, modifier = Modifier.size(23.dp)) }, onLogout)
    }
}

@Composable
private fun DriverAvatar(driver: Driver?) {
    Box(
        modifier = Modifier
            .size(58.dp)
            .clip(CircleShape)
            .background(AppColors.Green.copy(alpha = .10f)),
        contentAlignment = Alignment.Center
    ) {
        if (!driver?.fotoUrl.isNullOrBlank()) {
            AsyncImage(
                model = driver?.fotoUrl,
                contentDescription = "Foto do entregador",
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(58.dp).clip(CircleShape)
            )
        } else {
            Text((driver?.nome ?: "E").take(1).uppercase(), color = AppColors.Green, fontWeight = FontWeight.Black, fontSize = 20.sp)
        }
    }
}

@Composable
private fun SmallIconButton(bg: Color, fg: Color, icon: @Composable () -> Unit, onClick: () -> Unit) {
    Box(
        modifier = Modifier.size(48.dp).clip(CircleShape).background(bg).clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Box(modifier = Modifier, contentAlignment = Alignment.Center) { icon() }
    }
}

@Composable
fun StatusSwitch(driver: Driver?, onOnline: (Boolean) -> Unit) {
    val online = driver?.online == true
    val restricted = driver?.statusOperacional == "RESTRICAO"
    val text = if (restricted) "Restrição" else if (online) "Disponível" else "Indisponível"
    val color = if (restricted) AppColors.Red else if (online) AppColors.Green else AppColors.Ink
    Button(
        onClick = { if (!restricted) onOnline(!online) },
        colors = ButtonDefaults.buttonColors(containerColor = color),
        shape = RoundedCornerShape(999.dp),
        modifier = Modifier.fillMaxWidth().height(46.dp)
    ) {
        Text(text, color = Color.White, fontWeight = FontWeight.Black, fontSize = 16.sp, maxLines = 1)
    }
}

@Composable
fun PrimaryButton(text: String, background: Color = AppColors.Green, content: Color = Color.White, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = background, contentColor = content),
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier.fillMaxWidth().height(52.dp)
    ) { Text(text, fontWeight = FontWeight.Black) }
}

@Composable
fun DangerButton(text: String, onClick: () -> Unit) = PrimaryButton(text, AppColors.Red, Color.White, onClick)

@Composable
fun OutlineAction(text: String, onClick: () -> Unit) {
    OutlinedButton(onClick = onClick, shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth().height(50.dp)) {
        Text(text, color = AppColors.Ink, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun Field(
    value: String,
    onValue: (String) -> Unit,
    label: String,
    type: KeyboardType = KeyboardType.Text,
    password: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValue,
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = type),
        visualTransformation = if (password) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
        singleLine = !label.contains("observação", ignoreCase = true) && !label.contains("detalhe", ignoreCase = true),
        shape = RoundedCornerShape(18.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            focusedIndicatorColor = AppColors.Green,
            unfocusedIndicatorColor = AppColors.Line
        ),
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun Metric(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Card(modifier = modifier, colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(22.dp), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
        Column(Modifier.padding(14.dp)) {
            Text(label, color = AppColors.Muted, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            Text(value, color = color, fontWeight = FontWeight.Black, fontSize = 18.sp, maxLines = 1)
        }
    }
}

@Composable
fun AlertBox(text: String, color: Color = AppColors.Muted) {
    Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(22.dp), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
        Text(text, color = color, modifier = Modifier.padding(16.dp), fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun EmptyCard(text: String) = AlertBox(text, AppColors.Muted)

@Composable
fun AddressBlock(label: String, title: String, text: String) {
    Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(22.dp), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
        Column(Modifier.padding(16.dp)) {
            Text(label, color = AppColors.Muted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text(title, color = AppColors.Ink, fontSize = 17.sp, fontWeight = FontWeight.Black, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Text(text, color = AppColors.Muted, fontSize = 13.sp, maxLines = 3, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
fun CardLine(title: String, subtitle: String, trailing: String, color: Color = AppColors.Green) {
    Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(22.dp), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(title, color = AppColors.Ink, fontWeight = FontWeight.Black, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(subtitle, color = AppColors.Muted, fontSize = 12.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
            Text(trailing, color = color, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
fun UrgentCard(ride: Ride, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = AppColors.Ink),
        shape = RoundedCornerShape(26.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Nova corrida", color = Color.White, fontWeight = FontWeight.Black, fontSize = 20.sp)
                    Text("Pedido ${ride.numeroPedido} • ${ride.clienteBairro}", color = Color.White.copy(alpha = .78f), fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Text(safeMoney(ride.valorCorrida), color = AppColors.Green, fontWeight = FontWeight.Black, fontSize = 20.sp)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OfferChip(safeDistance(ride.distanciaKm))
                OfferChip(safeEta(ride.tempoEstimadoMin))
                OfferChip("Toque para abrir")
            }
        }
    }
}

@Composable
private fun OfferChip(text: String) {
    Box(
        modifier = Modifier.clip(RoundedCornerShape(999.dp)).background(Color.White.copy(alpha = .10f)).padding(horizontal = 12.dp, vertical = 7.dp)
    ) { Text(text, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp) }
}


@Composable
fun ActiveRideCard(ride: Ride, onOpen: () -> Unit, onMap: () -> Unit) {
    val address = ride.safeDeliveryAddress()
    Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(26.dp), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Pedido ${ride.numeroPedido}", color = AppColors.Ink, fontWeight = FontWeight.Black, fontSize = 19.sp)
                    Text(humanStatus(ride.status), color = statusColor(ride.status), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
                Text(safeMoney(ride.valorCorrida), color = AppColors.Green, fontWeight = FontWeight.Black, fontSize = 18.sp)
            }
            Divider(color = AppColors.Line)
            Text("Coleta: ${ride.pickupVisibleAddress()}", color = AppColors.Ink, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text("Entrega: $address", color = AppColors.Muted, fontSize = 13.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Text("${safeDistance(ride.distanciaKm)} • ${safeEta(ride.tempoEstimadoMin)} • ${ride.clienteBairro}", color = AppColors.Muted, fontSize = 12.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onOpen, modifier = Modifier.weight(1f), shape = RoundedCornerShape(16.dp)) { Text("Abrir") }
                OutlinedButton(onClick = onMap, modifier = Modifier.weight(1f), shape = RoundedCornerShape(16.dp)) { Text("Mapa") }
            }
        }
    }
}

@Composable
fun EarningsCompact(driver: Driver?, onToggleValues: (Boolean) -> Unit) {
    val hidden = driver?.ocultarValores == true
    val hoje = if (hidden) "••••" else money(driver?.saldoHoje ?: 0.0)
    Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(26.dp), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
        Row(Modifier.fillMaxWidth().padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Ganhos de hoje", color = AppColors.Muted, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Text(hoje, color = AppColors.Ink, fontWeight = FontWeight.Black, fontSize = 28.sp, maxLines = 1)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("${driver?.corridasHoje ?: 0}", color = AppColors.Ink, fontWeight = FontWeight.Black, fontSize = 22.sp)
                Text("corridas hoje", color = AppColors.Muted, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.width(8.dp))
            Box(
                modifier = Modifier.size(38.dp).clip(CircleShape).background(AppColors.Bg).clickable { onToggleValues(!hidden) },
                contentAlignment = Alignment.Center
            ) {
                Icon(if (hidden) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility, contentDescription = "Alternar valores", tint = AppColors.Muted, modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
fun QuickGrid(items: List<Pair<String, AppRoute>>, onNav: (AppRoute) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                row.forEach { item ->
                    Card(
                        modifier = Modifier.weight(1f).height(82.dp).clickable { onNav(item.second) },
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(22.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Box(Modifier.fillMaxWidth().padding(14.dp), contentAlignment = Alignment.CenterStart) {
                            Text(item.first, color = AppColors.Ink, fontWeight = FontWeight.Black, fontSize = 15.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun MiniMapDrawing(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.clip(RoundedCornerShape(26.dp)).background(Color.White)) {
        val w = size.width
        val h = size.height
        drawLine(AppColors.Line, Offset(w * .08f, h * .26f), Offset(w * .92f, h * .2f), strokeWidth = 10f, cap = StrokeCap.Round)
        drawLine(AppColors.Line, Offset(w * .14f, h * .78f), Offset(w * .88f, h * .82f), strokeWidth = 10f, cap = StrokeCap.Round)
        drawLine(AppColors.Line.copy(alpha = .8f), Offset(w * .5f, h * .12f), Offset(w * .38f, h * .9f), strokeWidth = 8f, cap = StrokeCap.Round)
        drawCircle(AppColors.Green, 18f, Offset(w * .25f, h * .52f))
        drawCircle(AppColors.Red, 18f, Offset(w * .76f, h * .52f))
        drawLine(AppColors.Green, Offset(w * .25f, h * .52f), Offset(w * .76f, h * .52f), strokeWidth = 8f, cap = StrokeCap.Round)
        drawCircle(Color.White, 30f, Offset(w * .5f, h * .52f), style = Stroke(width = 6f))
    }
}

@Composable
fun UploadCard(title: String, subtitle: String) {
    Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(22.dp), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
        Column(Modifier.fillMaxWidth().border(1.dp, AppColors.Line, RoundedCornerShape(22.dp)).padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(title, color = AppColors.Ink, fontWeight = FontWeight.Black)
            Text(subtitle, color = AppColors.Muted, textAlign = TextAlign.Center, fontSize = 12.sp)
        }
    }
}

@Composable
fun PasswordChecklist(password: String) {
    val checks = listOf(
        "mínimo 6 caracteres" to (password.length >= 6),
        "contém número" to password.any { it.isDigit() },
        "contém letra" to password.any { it.isLetter() }
    )
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        checks.forEach { (label, ok) ->
            Text((if (ok) "OK " else "-- ") + label, color = if (ok) AppColors.Green else AppColors.Muted, fontSize = 12.sp)
        }
    }
}
