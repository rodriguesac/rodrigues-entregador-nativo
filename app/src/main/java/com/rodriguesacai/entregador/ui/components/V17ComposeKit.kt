package com.rodriguesacai.entregador.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.Assignment
import androidx.compose.material.icons.rounded.BatteryAlert
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DeliveryDining
import androidx.compose.material.icons.rounded.DirectionsBike
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material.icons.rounded.GpsFixed
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Route
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Storefront
import androidx.compose.material.icons.rounded.SupportAgent
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.icons.rounded.TwoWheeler
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material.icons.rounded.WifiOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.rodriguesacai.entregador.R
import com.rodriguesacai.entregador.data.Driver
import com.rodriguesacai.entregador.data.Ride
import com.rodriguesacai.entregador.ui.DriverUiState
import com.rodriguesacai.entregador.ui.deliveryAddressVisible
import com.rodriguesacai.entregador.ui.humanStatus
import com.rodriguesacai.entregador.ui.moneyOrEmpty
import com.rodriguesacai.entregador.ui.navigation.AppRoute
import com.rodriguesacai.entregador.ui.nextActionText
import com.rodriguesacai.entregador.ui.safeDeliveryAddress
import com.rodriguesacai.entregador.ui.safeDistance
import com.rodriguesacai.entregador.ui.safeEta
import com.rodriguesacai.entregador.ui.safeMoney
import com.rodriguesacai.entregador.ui.shortDate
import com.rodriguesacai.entregador.ui.statusColor
import com.rodriguesacai.entregador.ui.design.UpAppFont
import com.rodriguesacai.entregador.ui.design.UpBorders
import com.rodriguesacai.entregador.ui.design.UpColors
import com.rodriguesacai.entregador.ui.design.UpElevations

private val V17GreenGradient = Brush.verticalGradient(listOf(Color(0xFF0BAA3E), Color(0xFF047221)))
private val V17SoftGreenGradient = Brush.linearGradient(listOf(Color(0xFFF6FFF8), Color(0xFFEAF8EE)))
private val V17RedGradient = Brush.verticalGradient(listOf(Color(0xFFFF3048), Color(0xFFD7192B)))
private val V17DarkText = Color(0xFF101817)
private val V17MutedText = Color(0xFF68716F)
private val V17Line = Color(0xFFE5EBE8)

@Composable
fun V17HomeScaffold(
    state: DriverUiState,
    onOnline: (Boolean) -> Unit,
    onUrgent: (Ride) -> Unit,
    onRide: (Ride) -> Unit,
    onNav: (AppRoute) -> Unit,
    onToggleValues: (Boolean) -> Unit
) {
    Scaffold(
        containerColor = UpColors.Screen,
        bottomBar = { V17BottomBar(AppRoute.Home, onNav) }
    ) { pad ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(pad)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            V17DriverHeader(
                driver = state.driver,
                onNotifications = { onNav(AppRoute.Notificacoes) },
                onMenu = { onNav(AppRoute.Perfil) }
            )
            V17AvailabilityControl(driver = state.driver, onOnline = onOnline)
            V17RestrictionExplanation(driver = state.driver, error = state.error, onPermissions = { onNav(AppRoute.Permissoes) })
            V17TodaySummary(driver = state.driver, onToggleValues = onToggleValues, onWallet = { onNav(AppRoute.Carteira) })
            V17OperationHero(onClick = { onNav(AppRoute.Notificacoes) })
            V17ShortcutPanel(
                onHistory = { onNav(AppRoute.Historico) },
                onEarnings = { onNav(AppRoute.Ganhos) },
                onMap = { onNav(AppRoute.Mapa) },
                onSupport = { onNav(AppRoute.Perfil) }
            )
            V17RideArea(state = state, onUrgent = onUrgent, onRide = onRide)
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
fun V17DriverHeader(driver: Driver?, onNotifications: () -> Unit, onMenu: () -> Unit) {
    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        V17Avatar(driver)
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = if (driver?.nome.isNullOrBlank()) "Olá" else "Olá, ${driver?.nome?.substringBefore(' ')}",
                color = V17DarkText,
                fontFamily = UpAppFont,
                fontWeight = FontWeight.Black,
                fontSize = 22.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = V17HeaderSubtitle(driver),
                color = V17MutedText,
                fontFamily = UpAppFont,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        V17CircleIcon(Icons.Rounded.Notifications, onNotifications, badge = false)
        Spacer(Modifier.width(8.dp))
        V17CircleIcon(Icons.Rounded.MoreHoriz, onMenu)
    }
}

private fun V17HeaderSubtitle(driver: Driver?): String {
    if (driver == null) return "Sincronizando perfil da operação"
    return when (driver.statusOperacional.uppercase()) {
        "DISPONIVEL" -> "Pronto para receber corridas"
        "RESTRICAO" -> driver.restricaoMotivo.ifBlank { "Restrição ativa na operação" }
        "INDISPONIVEL" -> "Indisponível para novas corridas"
        else -> humanStatus(driver.statusOperacional)
    }
}

@Composable
private fun V17Avatar(driver: Driver?) {
    Box(
        modifier = Modifier
            .size(58.dp)
            .clip(CircleShape)
            .background(UpColors.GreenSoft),
        contentAlignment = Alignment.Center
    ) {
        if (!driver?.fotoUrl.isNullOrBlank()) {
            AsyncImage(
                model = driver?.fotoUrl,
                contentDescription = "Foto do entregador",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Icon(Icons.Rounded.Person, contentDescription = null, tint = UpColors.Green, modifier = Modifier.size(30.dp))
        }
        Box(
            Modifier
                .align(Alignment.BottomEnd)
                .size(14.dp)
                .clip(CircleShape)
                .background(if (driver?.online == true) UpColors.Green else UpColors.Subtle)
                .border(2.dp, UpColors.Screen, CircleShape)
        )
    }
}

@Composable
fun V17CircleIcon(icon: ImageVector, onClick: () -> Unit, badge: Boolean = false) {
    Box(contentAlignment = Alignment.TopEnd) {
        IconButton(
            onClick = onClick,
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(UpColors.GreenSoft)
        ) {
            Icon(icon, contentDescription = null, tint = UpColors.Green, modifier = Modifier.size(22.dp))
        }
        if (badge) Box(Modifier.size(9.dp).clip(CircleShape).background(UpColors.Red))
    }
}

@Composable
fun V17AvailabilityControl(driver: Driver?, onOnline: (Boolean) -> Unit) {
    val status = driver?.statusOperacional?.uppercase().orEmpty()
    val isAvailable = status == "DISPONIVEL" || driver?.online == true
    val isRestricted = status == "RESTRICAO"
    val targetColor = when {
        isRestricted -> UpColors.Red
        isAvailable -> UpColors.Green
        else -> V17DarkText
    }
    val animatedColor by animateColorAsState(targetValue = targetColor, label = "availabilityColor")
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(999.dp))
            .clickable(enabled = driver != null && !isRestricted) { onOnline(!isAvailable) },
        color = animatedColor,
        shadowElevation = 0.dp
    ) {
        Row(
            Modifier.padding(horizontal = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = when {
                    isRestricted -> Icons.Rounded.Warning
                    isAvailable -> Icons.Rounded.CheckCircle
                    else -> Icons.Rounded.Close
                },
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(21.dp)
            )
            Spacer(Modifier.width(10.dp))
            Text(
                text = when {
                    isRestricted -> "Restrição"
                    isAvailable -> "Disponível"
                    else -> "Indisponível"
                },
                color = Color.White,
                fontFamily = UpAppFont,
                fontWeight = FontWeight.Black,
                fontSize = 16.sp,
                maxLines = 1
            )
            Spacer(Modifier.width(8.dp))
            Icon(Icons.Rounded.KeyboardArrowDown, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
fun V17RestrictionExplanation(driver: Driver?, error: String?, onPermissions: () -> Unit) {
    if (!error.isNullOrBlank()) {
        V17MessageCard(
            title = "Falha de sincronização",
            message = error,
            icon = Icons.Rounded.Error,
            tint = UpColors.Red,
            actionText = "Verificar",
            onAction = onPermissions
        )
        return
    }
    if (driver?.statusOperacional == "RESTRICAO") {
        V17MessageCard(
            title = "Operação com restrição",
            message = driver.restricaoMotivo.ifBlank { "Verifique localização, notificações, bateria, internet ou liberação do gestor." },
            icon = Icons.Rounded.Warning,
            tint = UpColors.Red,
            actionText = "Permissões",
            onAction = onPermissions
        )
    }
}

@Composable
fun V17MessageCard(title: String, message: String, icon: ImageVector, tint: Color, actionText: String? = null, onAction: (() -> Unit)? = null) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(tint.copy(alpha = .07f)),
        shape = RoundedCornerShape(18.dp),
        border = UpBorders.normal,
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(44.dp).clip(CircleShape).background(tint.copy(alpha = .14f)), contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(24.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(title, color = V17DarkText, fontFamily = UpAppFont, fontWeight = FontWeight.Black, fontSize = 14.sp)
                Text(message, color = V17MutedText, fontFamily = UpAppFont, fontSize = 12.sp, lineHeight = 16.sp, maxLines = 3, overflow = TextOverflow.Ellipsis)
            }
            if (actionText != null && onAction != null) {
                Text(
                    text = actionText,
                    color = tint,
                    fontFamily = UpAppFont,
                    fontWeight = FontWeight.Black,
                    fontSize = 12.sp,
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .clickable(onClick = onAction)
                        .background(Color.White)
                        .padding(horizontal = 10.dp, vertical = 7.dp)
                )
            }
        }
    }
}

@Composable
fun V17TodaySummary(driver: Driver?, onToggleValues: (Boolean) -> Unit, onWallet: () -> Unit) {
    val hidden = driver?.ocultarValores == true
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onWallet),
        colors = CardDefaults.cardColors(UpColors.Surface),
        shape = RoundedCornerShape(24.dp),
        border = UpBorders.normal,
        elevation = UpElevations.card
    ) {
        Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1.2f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Payments, contentDescription = null, tint = UpColors.Green, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Ganhos de hoje", color = V17MutedText, fontFamily = UpAppFont, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
                Text(moneyOrEmpty(driver?.saldoHoje, hidden), color = V17DarkText, fontFamily = UpAppFont, fontWeight = FontWeight.Black, fontSize = 26.sp, maxLines = 1)
            }
            Divider(Modifier.height(58.dp).width(1.dp), color = V17Line)
            Column(Modifier.weight(.9f).padding(start = 14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                V17TinyMetric(Icons.Rounded.Route, "Corridas", driver?.corridasHoje?.toString() ?: "0")
                V17TinyMetric(Icons.Rounded.CheckCircle, "Finalizadas", "--")
            }
            IconButton(onClick = { onToggleValues(!hidden) }) {
                Icon(if (hidden) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility, contentDescription = null, tint = UpColors.Green)
            }
        }
    }
}

@Composable
private fun V17TinyMetric(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = UpColors.Green, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(6.dp))
        Text(value, color = V17DarkText, fontFamily = UpAppFont, fontWeight = FontWeight.Black, fontSize = 14.sp, maxLines = 1)
        Spacer(Modifier.width(5.dp))
        Text(label, color = V17MutedText, fontFamily = UpAppFont, fontWeight = FontWeight.SemiBold, fontSize = 11.sp, maxLines = 1)
    }
}

@Composable
fun V17OperationHero(onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(152.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(Color.Transparent),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Box(Modifier.fillMaxSize().background(V17GreenGradient)) {
            Image(
                painter = painterResource(id = R.drawable.up_v17_ops_banner),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = .18f)))
            Box(Modifier.fillMaxSize().padding(18.dp)) {
            Column(Modifier.align(Alignment.CenterStart).fillMaxWidth(.62f)) {
                Text(
                    "CENTRAL",
                    color = Color.White,
                    fontFamily = UpAppFont,
                    fontWeight = FontWeight.Black,
                    fontSize = 10.sp,
                    modifier = Modifier.background(Color.White.copy(.18f), RoundedCornerShape(999.dp)).padding(horizontal = 10.dp, vertical = 4.dp)
                )
                Spacer(Modifier.height(10.dp))
                Text("Avisos da operação", color = Color.White, fontFamily = UpAppFont, fontWeight = FontWeight.Black, fontSize = 21.sp, lineHeight = 25.sp)
                Spacer(Modifier.height(6.dp))
                Text("Comunicados, regras e novidades aparecem aqui sem ocupar a tela toda.", color = Color.White.copy(.88f), fontFamily = UpAppFont, fontSize = 12.sp, lineHeight = 16.sp)
            }
            V17DeliveryBoxIllustration(Modifier.align(Alignment.CenterEnd).size(122.dp))
            }
        }
    }
}

@Composable
fun V17DeliveryBoxIllustration(modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        val white = Color.White
        val soft = Color.White.copy(alpha = .30f)
        drawCircle(soft, radius = w * .42f, center = Offset(w * .52f, h * .50f))
        drawRoundRect(white.copy(alpha = .94f), Offset(w * .24f, h * .40f), Size(w * .52f, h * .38f), CornerRadius(14f, 14f))
        drawLine(Color(0xFF078C35), Offset(w * .24f, h * .51f), Offset(w * .76f, h * .51f), 5f)
        drawLine(Color(0xFF078C35), Offset(w * .50f, h * .40f), Offset(w * .50f, h * .78f), 5f)
        drawCircle(Color(0xFF078C35), radius = w * .09f, center = Offset(w * .50f, h * .30f))
        drawLine(Color(0xFF078C35), Offset(w * .50f, h * .23f), Offset(w * .50f, h * .13f), 7f, cap = StrokeCap.Round)
        drawLine(Color(0xFF078C35), Offset(w * .50f, h * .13f), Offset(w * .60f, h * .22f), 7f, cap = StrokeCap.Round)
    }
}

@Composable
fun V17ShortcutPanel(onHistory: () -> Unit, onEarnings: () -> Unit, onMap: () -> Unit, onSupport: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            V17Shortcut("Histórico", "Ver corridas", Icons.Rounded.History, onHistory, Modifier.weight(1f))
            V17Shortcut("Ganhos", "Resumo financeiro", Icons.Rounded.Payments, onEarnings, Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            V17Shortcut("Mapa", "Ver região", Icons.Rounded.Map, onMap, Modifier.weight(1f))
            V17Shortcut("Suporte", "Fale conosco", Icons.Rounded.SupportAgent, onSupport, Modifier.weight(1f))
        }
    }
}

@Composable
fun V17Shortcut(title: String, subtitle: String, icon: ImageVector, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.height(92.dp).clickable(onClick = onClick),
        colors = CardDefaults.cardColors(UpColors.Surface),
        shape = RoundedCornerShape(20.dp),
        border = UpBorders.normal,
        elevation = UpElevations.card
    ) {
        Row(Modifier.fillMaxSize().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(42.dp).clip(RoundedCornerShape(14.dp)).background(UpColors.GreenSoft), contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = UpColors.Green, modifier = Modifier.size(23.dp))
            }
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(title, color = V17DarkText, fontFamily = UpAppFont, fontWeight = FontWeight.Black, fontSize = 14.sp, maxLines = 1)
                Text(subtitle, color = V17MutedText, fontFamily = UpAppFont, fontWeight = FontWeight.SemiBold, fontSize = 11.sp, maxLines = 1)
            }
        }
    }
}

@Composable
fun V17RideArea(state: DriverUiState, onUrgent: (Ride) -> Unit, onRide: (Ride) -> Unit) {
    val offer = state.activeRides.firstOrNull { it.status == "OFERTA_RECEBIDA" }
    val running = state.activeRides.firstOrNull { it.status != "OFERTA_RECEBIDA" }
    when {
        offer != null -> V17RideCard(offer, highlight = true, onClick = { onUrgent(offer) })
        running != null -> V17RideCard(running, highlight = false, onClick = { onRide(running) })
        state.driver == null -> V17EmptyPanel("Perfil aguardando sincronização", "Entre com uma conta aprovada para carregar corridas, ganhos e avisos reais.", Icons.Rounded.Security)
        else -> V17EmptyPanel("Nenhuma corrida disponível", "Quando a torre enviar uma corrida real para você, ela aparece aqui e também dispara alerta urgente.", Icons.Rounded.DeliveryDining)
    }
}

@Composable
fun V17RideCard(ride: Ride, highlight: Boolean, onClick: () -> Unit) {
    val tint = if (highlight) UpColors.Red else statusColor(ride.status)
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(UpColors.Surface),
        shape = RoundedCornerShape(24.dp),
        border = if (highlight) UpBorders.red else UpBorders.normal,
        elevation = UpElevations.card
    ) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(46.dp).clip(CircleShape).background(tint.copy(alpha = .12f)), contentAlignment = Alignment.Center) {
                    Icon(if (highlight) Icons.Rounded.NotificationsActive else Icons.Rounded.Assignment, contentDescription = null, tint = tint, modifier = Modifier.size(24.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(ride.numeroPedido.ifBlank { "Pedido sem número" }, color = V17DarkText, fontFamily = UpAppFont, fontWeight = FontWeight.Black, fontSize = 18.sp, maxLines = 1)
                    Text(humanStatus(ride.status), color = tint, fontFamily = UpAppFont, fontWeight = FontWeight.Black, fontSize = 12.sp)
                }
                Text(safeMoney(ride.valorCorrida), color = UpColors.Green, fontFamily = UpAppFont, fontWeight = FontWeight.Black, fontSize = 18.sp, maxLines = 1)
            }
            Divider(color = V17Line)
            V17LocationLine("Coleta", ride.lojaNome.ifBlank { "Coleta ainda não informada" }, ride.lojaEndereco.ifBlank { "Endereço da coleta pendente" }, Icons.Rounded.Storefront, UpColors.Green)
            V17LocationLine("Entrega", ride.clienteBairro.ifBlank { "Bairro pendente" }, if (ride.deliveryAddressVisible()) ride.safeDeliveryAddress() else "Endereço completo liberado após retirar o pedido", Icons.Rounded.LocationOn, UpColors.Orange)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                V17MetricChip(Icons.Rounded.Route, "Distância", safeDistance(ride.distanciaKm), Modifier.weight(1f))
                V17MetricChip(Icons.Rounded.Schedule, "Tempo", safeEta(ride.tempoEstimadoMin), Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun V17LocationLine(label: String, title: String, message: String, icon: ImageVector, tint: Color) {
    Row(verticalAlignment = Alignment.Top) {
        Box(Modifier.size(40.dp).clip(CircleShape).background(tint.copy(alpha = .12f)), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(22.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(label, color = tint, fontFamily = UpAppFont, fontWeight = FontWeight.Black, fontSize = 12.sp)
            Text(title, color = V17DarkText, fontFamily = UpAppFont, fontWeight = FontWeight.Black, fontSize = 15.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(message, color = V17MutedText, fontFamily = UpAppFont, fontSize = 12.sp, lineHeight = 16.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
fun V17MetricChip(icon: ImageVector, label: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier, colors = CardDefaults.cardColors(UpColors.SurfaceSoft), shape = RoundedCornerShape(16.dp), border = UpBorders.normal) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = UpColors.Green, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Column {
                Text(label, color = V17MutedText, fontFamily = UpAppFont, fontWeight = FontWeight.SemiBold, fontSize = 11.sp)
                Text(value, color = V17DarkText, fontFamily = UpAppFont, fontWeight = FontWeight.Black, fontSize = 14.sp, maxLines = 1)
            }
        }
    }
}

@Composable
fun V17EmptyPanel(title: String, message: String, icon: ImageVector) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(UpColors.Surface),
        shape = RoundedCornerShape(24.dp),
        border = UpBorders.normal,
        elevation = UpElevations.card
    ) {
        Column(Modifier.padding(22.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(Modifier.size(74.dp).clip(CircleShape).background(UpColors.GreenSoft), contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = UpColors.Green, modifier = Modifier.size(34.dp))
            }
            Spacer(Modifier.height(14.dp))
            Text(title, color = V17DarkText, fontFamily = UpAppFont, fontWeight = FontWeight.Black, fontSize = 17.sp, textAlign = TextAlign.Center)
            Spacer(Modifier.height(5.dp))
            Text(message, color = V17MutedText, fontFamily = UpAppFont, fontSize = 13.sp, lineHeight = 18.sp, textAlign = TextAlign.Center)
        }
    }
}

@Composable
fun V17BottomBar(current: AppRoute, onNav: (AppRoute) -> Unit) {
    NavigationBar(containerColor = UpColors.Surface, tonalElevation = 8.dp, modifier = Modifier.height(74.dp)) {
        val items = listOf(
            Triple(AppRoute.Home, "Início", Icons.Rounded.Home),
            Triple(AppRoute.CorridaAndamento, "Corridas", Icons.Rounded.TwoWheeler),
            Triple(AppRoute.Carteira, "Carteira", Icons.Rounded.AccountBalanceWallet),
            Triple(AppRoute.Notificacoes, "Avisos", Icons.Rounded.Notifications),
            Triple(AppRoute.Perfil, "Mais", Icons.Rounded.MoreHoriz)
        )
        items.forEach { (route, label, icon) ->
            val selected = when (route) {
                AppRoute.CorridaAndamento -> current in listOf(AppRoute.CorridaAndamento, AppRoute.CorridaUrgente, AppRoute.Mapa, AppRoute.Ocorrencia)
                AppRoute.Carteira -> current in listOf(AppRoute.Carteira, AppRoute.Ganhos, AppRoute.PixBanco)
                AppRoute.Perfil -> current in listOf(AppRoute.Perfil, AppRoute.Solicitacao, AppRoute.Permissoes, AppRoute.Atualizacao, AppRoute.SemInternet, AppRoute.Manutencao, AppRoute.ErroFirebase)
                else -> current == route
            }
            NavigationBarItem(
                selected = selected,
                onClick = { onNav(route) },
                icon = { Icon(icon, contentDescription = label, modifier = Modifier.size(22.dp)) },
                label = { Text(label, fontFamily = UpAppFont, fontWeight = FontWeight.Black, fontSize = 10.sp, maxLines = 1) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = UpColors.Green,
                    selectedTextColor = UpColors.Green,
                    indicatorColor = UpColors.Green.copy(alpha = .10f),
                    unselectedIconColor = UpColors.Subtle,
                    unselectedTextColor = UpColors.Muted
                )
            )
        }
    }
}

@Composable
fun V17LoginHero() {
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Image(
            painter = painterResource(id = R.drawable.up_logo_reference),
            contentDescription = "Up entregas",
            modifier = Modifier.height(58.dp).fillMaxWidth(),
            contentScale = ContentScale.Fit
        )
        Spacer(Modifier.height(10.dp))
        Text("Bem-vindo", color = V17DarkText, fontFamily = UpAppFont, fontWeight = FontWeight.Black, fontSize = 27.sp)
        Text("Acesse sua conta para receber corridas reais da operação.", color = V17MutedText, fontFamily = UpAppFont, fontSize = 13.sp, lineHeight = 18.sp, textAlign = TextAlign.Center)
    }
}

@Composable
fun V17VersionBadge() {
    Row(
        Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(UpColors.GreenSoft)
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = UpColors.Green, modifier = Modifier.size(14.dp))
        Spacer(Modifier.width(5.dp))
        Text("V18 UI Gradle Nuvem", color = UpColors.Green, fontFamily = UpAppFont, fontWeight = FontWeight.Black, fontSize = 10.sp)
    }
}

@Composable
fun V17AuthFooterIllustration(modifier: Modifier = Modifier) {
    Box(modifier, contentAlignment = Alignment.Center) {
        Image(
            painter = painterResource(id = R.drawable.up_login_moto),
            contentDescription = null,
            modifier = Modifier.fillMaxWidth().heightIn(min = 115.dp, max = 160.dp),
            contentScale = ContentScale.Fit
        )
    }
}

@Composable
fun V17ActiveRidePanel(ride: Ride, onMap: () -> Unit, onAdvance: () -> Unit, onOccurrence: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        V17RideStatusHeader(ride)
        RoutePreviewCard(ride = ride, height = 218.dp, onClick = onMap)
        V17RouteSteps(ride)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            V17MetricChip(Icons.Rounded.Route, "Distância", safeDistance(ride.distanciaKm), Modifier.weight(1f))
            V17MetricChip(Icons.Rounded.Schedule, "Tempo", safeEta(ride.tempoEstimadoMin), Modifier.weight(1f))
        }
        SecondaryAction("Abrir mapa da rota", onMap, icon = Icons.Rounded.Map)
        PrimaryAction(nextActionText(ride.status), onAdvance, icon = Icons.Rounded.CheckCircle, enabled = ride.status != "OCORRENCIA")
        SecondaryAction("Registrar ocorrência", onOccurrence, icon = Icons.Rounded.Warning, red = true)
    }
}

@Composable
fun V17RideStatusHeader(ride: Ride) {
    val tint = statusColor(ride.status)
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(tint.copy(alpha = .08f)),
        shape = RoundedCornerShape(22.dp),
        border = UpBorders.normal,
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(48.dp).clip(CircleShape).background(tint.copy(alpha = .14f)), contentAlignment = Alignment.Center) {
                Icon(Icons.Rounded.DirectionsBike, contentDescription = null, tint = tint, modifier = Modifier.size(26.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(humanStatus(ride.status), color = tint, fontFamily = UpAppFont, fontWeight = FontWeight.Black, fontSize = 17.sp)
                Text(V17RideHint(ride.status), color = V17MutedText, fontFamily = UpAppFont, fontSize = 12.sp, lineHeight = 16.sp)
            }
        }
    }
}

private fun V17RideHint(status: String): String = when (status) {
    "ACEITA", "INDO_COLETA" -> "Siga para o local de coleta. A entrega completa continua protegida."
    "CHEGUEI_COLETA" -> "Confirme a retirada somente quando o pedido estiver com você."
    "PEDIDO_RETIRADO", "INDO_ENTREGA" -> "Endereço do cliente liberado. Use a navegação para seguir."
    "ENTREGADOR_NO_LOCAL" -> "Você chegou ao local. Finalize apenas após concluir a entrega."
    "OCORRENCIA" -> "Ocorrência enviada. Aguarde orientação da operação."
    else -> "Atualize a etapa da corrida conforme o andamento real."
}

@Composable
fun V17RouteSteps(ride: Ride) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(UpColors.Surface),
        shape = RoundedCornerShape(22.dp),
        border = UpBorders.normal,
        elevation = UpElevations.card
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            V17LocationLine("Coleta", ride.lojaNome.ifBlank { "Coleta ainda não informada" }, ride.lojaEndereco.ifBlank { "Endereço da coleta pendente" }, Icons.Rounded.Storefront, UpColors.Green)
            Divider(color = V17Line)
            V17LocationLine("Entrega", ride.clienteBairro.ifBlank { "Bairro pendente" }, if (ride.deliveryAddressVisible()) ride.safeDeliveryAddress() else "Endereço completo liberado após retirar o pedido", Icons.Rounded.LocationOn, UpColors.Orange)
        }
    }
}

@Composable
fun V17UrgentRideLayout(ride: Ride, onAccept: (String) -> Unit, onReject: (String, String) -> Unit) {
    Column(Modifier.fillMaxSize().background(UpColors.Screen)) {
        V17UrgentHeader()
        Column(Modifier.weight(1f).padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            V17MessageCard(
                title = "Nova corrida urgente",
                message = "Confira coleta, distância, tempo e valor. O endereço completo do cliente só libera após retirar o pedido.",
                icon = Icons.Rounded.NotificationsActive,
                tint = UpColors.Red
            )
            V17RideCard(ride = ride, highlight = true, onClick = {})
            RoutePreviewCard(ride = ride, height = 205.dp)
            Spacer(Modifier.weight(1f))
            PrimaryAction("Aceitar", onClick = { onAccept(ride.id) }, icon = Icons.Rounded.CheckCircle)
            SecondaryAction("Recusar", onClick = { onReject(ride.id, "Recusada pelo entregador") }, icon = Icons.Rounded.Close, red = true)
        }
    }
}

@Composable
fun V17UrgentHeader() {
    val transition = rememberInfiniteTransition(label = "urgentPulse")
    val alpha by transition.animateFloat(
        initialValue = .55f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(animation = tween(720), repeatMode = RepeatMode.Reverse),
        label = "urgentAlpha"
    )
    Box(Modifier.fillMaxWidth().height(82.dp).background(V17RedGradient).padding(horizontal = 18.dp), contentAlignment = Alignment.Center) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(42.dp).clip(CircleShape).background(Color.White.copy(alpha = .16f)), contentAlignment = Alignment.Center) {
                Icon(Icons.Rounded.Warning, contentDescription = null, tint = Color.White.copy(alpha = alpha), modifier = Modifier.size(25.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text("NOVA CORRIDA URGENTE", color = Color.White, fontFamily = UpAppFont, fontWeight = FontWeight.Black, fontSize = 15.sp, maxLines = 1)
                Text("Responda antes de expirar", color = Color.White.copy(.82f), fontFamily = UpAppFont, fontWeight = FontWeight.SemiBold, fontSize = 11.sp)
            }
            Row(Modifier.clip(RoundedCornerShape(12.dp)).background(Color.White.copy(alpha = .15f)).padding(horizontal = 11.dp, vertical = 7.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Timer, contentDescription = null, tint = Color.White, modifier = Modifier.size(17.dp))
                Spacer(Modifier.width(5.dp))
                Text("00:18", color = Color.White, fontFamily = UpAppFont, fontWeight = FontWeight.Black, fontSize = 15.sp)
            }
        }
    }
}

@Composable
fun V17StandaloneUrgentContent(rideId: String, title: String, body: String, onAccept: () -> Unit, onReject: () -> Unit, onClose: () -> Unit) {
    Column(Modifier.fillMaxSize().background(UpColors.Screen)) {
        Box(Modifier.fillMaxWidth().height(84.dp).background(V17RedGradient).padding(horizontal = 18.dp), contentAlignment = Alignment.Center) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(42.dp).clip(CircleShape).background(Color.White.copy(alpha = .16f)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Rounded.Warning, contentDescription = null, tint = Color.White, modifier = Modifier.size(25.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(title.ifBlank { "Oferta urgente" }.uppercase(), color = Color.White, fontFamily = UpAppFont, fontWeight = FontWeight.Black, fontSize = 14.sp, maxLines = 1)
                    if (rideId.isNotBlank()) Text("Corrida #$rideId", color = Color.White.copy(alpha = .82f), fontFamily = UpAppFont, fontSize = 11.sp, maxLines = 1)
                }
                IconButton(onClick = onClose) { Icon(Icons.Rounded.Close, contentDescription = "Fechar", tint = Color.White) }
            }
        }
        Column(Modifier.weight(1f).padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            V17EmptyPanel("Oferta recebida", body.ifBlank { "Abra o app para carregar os dados reais da corrida pelo Firebase." }, Icons.Rounded.DeliveryDining)
            Text("A tela completa da corrida será sincronizada com a operação assim que o app abrir.", color = V17MutedText, fontFamily = UpAppFont, fontSize = 13.sp, lineHeight = 18.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 10.dp))
            Spacer(Modifier.weight(1f))
            PrimaryAction("Aceitar corrida", onClick = onAccept, icon = Icons.Rounded.CheckCircle)
            SecondaryAction("Recusar", onClick = onReject, icon = Icons.Rounded.Close, red = true)
        }
    }
}

@Composable
fun V17PermissionRow(title: String, message: String, icon: ImageVector, active: Boolean, onClick: () -> Unit) {
    val tint = if (active) UpColors.Green else UpColors.Orange
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(UpColors.Surface),
        shape = RoundedCornerShape(20.dp),
        border = if (active) UpBorders.green else UpBorders.orange,
        elevation = UpElevations.card
    ) {
        Row(Modifier.padding(15.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(46.dp).clip(CircleShape).background(tint.copy(alpha = .12f)), contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(24.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(title, color = V17DarkText, fontFamily = UpAppFont, fontWeight = FontWeight.Black, fontSize = 14.sp)
                Text(message, color = V17MutedText, fontFamily = UpAppFont, fontSize = 12.sp, lineHeight = 16.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
            Icon(if (active) Icons.Rounded.CheckCircle else Icons.Rounded.Warning, contentDescription = null, tint = tint)
        }
    }
}

@Composable
fun V17FinancialSkeleton(title: String, message: String, icon: ImageVector) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(UpColors.Surface),
        shape = RoundedCornerShape(24.dp),
        border = UpBorders.normal,
        elevation = UpElevations.card
    ) {
        Column(Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(Modifier.size(70.dp).clip(CircleShape).background(UpColors.GreenSoft), contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = UpColors.Green, modifier = Modifier.size(32.dp))
            }
            Spacer(Modifier.height(14.dp))
            Text(title, color = V17DarkText, fontFamily = UpAppFont, fontWeight = FontWeight.Black, fontSize = 17.sp, textAlign = TextAlign.Center)
            Spacer(Modifier.height(5.dp))
            Text(message, color = V17MutedText, fontFamily = UpAppFont, fontSize = 13.sp, lineHeight = 18.sp, textAlign = TextAlign.Center)
        }
    }
}

@Composable
fun V17MaterialStatusStrip(items: List<Pair<ImageVector, String>>, modifier: Modifier = Modifier) {
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items.forEach { (icon, label) ->
            Row(
                Modifier
                    .weight(1f)
                    .height(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(UpColors.Surface)
                    .border(1.dp, V17Line, RoundedCornerShape(14.dp))
                    .padding(horizontal = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(icon, contentDescription = null, tint = UpColors.Green, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text(label, color = V17DarkText, fontFamily = UpAppFont, fontWeight = FontWeight.Bold, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable
fun V17MapSketch(modifier: Modifier = Modifier, statusColor: Color = UpColors.Green) {
    Canvas(modifier.background(Color(0xFFEFF3EF), RoundedCornerShape(22.dp))) {
        val w = size.width
        val h = size.height
        val grid = Color.White.copy(alpha = .65f)
        repeat(5) { i ->
            val y = h * (i + 1) / 6f
            drawLine(grid, Offset(0f, y), Offset(w, y + ((i % 2) * 18f)), 4f)
        }
        repeat(4) { i ->
            val x = w * (i + 1) / 5f
            drawLine(grid, Offset(x, 0f), Offset(x - 30f, h), 4f)
        }
        val path = Path().apply {
            moveTo(w * .15f, h * .72f)
            lineTo(w * .32f, h * .58f)
            lineTo(w * .48f, h * .63f)
            lineTo(w * .63f, h * .44f)
            lineTo(w * .82f, h * .31f)
        }
        drawPath(path, statusColor, style = Stroke(width = 9f, cap = StrokeCap.Round))
        drawCircle(UpColors.Green, radius = 14f, center = Offset(w * .15f, h * .72f))
        drawCircle(UpColors.Orange, radius = 14f, center = Offset(w * .82f, h * .31f))
        drawCircle(Color.White, radius = 7f, center = Offset(w * .15f, h * .72f))
        drawCircle(Color.White, radius = 7f, center = Offset(w * .82f, h * .31f))
        drawRoundRect(Color.Black.copy(alpha = .70f), Offset(w * .46f, h * .52f), Size(36f, 20f), CornerRadius(8f, 8f))
    }
}

@Composable
fun V17ScreenTitle(title: String, subtitle: String? = null, icon: ImageVector? = null) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        if (icon != null) {
            Box(Modifier.size(42.dp).clip(CircleShape).background(UpColors.GreenSoft), contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = UpColors.Green, modifier = Modifier.size(23.dp))
            }
            Spacer(Modifier.width(12.dp))
        }
        Column(Modifier.weight(1f)) {
            Text(title, color = V17DarkText, fontFamily = UpAppFont, fontWeight = FontWeight.Black, fontSize = 22.sp, maxLines = 1)
            if (!subtitle.isNullOrBlank()) Text(subtitle, color = V17MutedText, fontFamily = UpAppFont, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, maxLines = 1)
        }
    }
}

@Composable
fun V17InfoColumn(title: String, value: String, icon: ImageVector, tint: Color = UpColors.Green, modifier: Modifier = Modifier) {
    Card(modifier = modifier, colors = CardDefaults.cardColors(UpColors.Surface), shape = RoundedCornerShape(18.dp), border = UpBorders.normal, elevation = UpElevations.card) {
        Column(Modifier.padding(14.dp)) {
            Box(Modifier.size(38.dp).clip(CircleShape).background(tint.copy(alpha = .12f)), contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.height(10.dp))
            Text(title, color = V17MutedText, fontFamily = UpAppFont, fontWeight = FontWeight.SemiBold, fontSize = 11.sp, maxLines = 1)
            Text(value, color = V17DarkText, fontFamily = UpAppFont, fontWeight = FontWeight.Black, fontSize = 16.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
fun V17DebugSizeMarker() {
    // Mantido propositalmente como função visual pequena para confirmar que a V17 foi instalada.
    Text(
        "V17",
        color = UpColors.Green,
        fontFamily = UpAppFont,
        fontWeight = FontWeight.Black,
        fontSize = 10.sp,
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(UpColors.GreenSoft)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    )
}
