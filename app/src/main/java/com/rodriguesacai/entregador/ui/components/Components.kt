package com.rodriguesacai.entregador.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Assignment
import androidx.compose.material.icons.rounded.BatteryAlert
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material.icons.rounded.DeliveryDining
import androidx.compose.material.icons.rounded.Directions
import androidx.compose.material.icons.rounded.DirectionsBike
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material.icons.rounded.GpsFixed
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.ListAlt
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material.icons.rounded.PhotoCamera
import androidx.compose.material.icons.rounded.Route
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.SupportAgent
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material.icons.rounded.Wallet
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material.icons.rounded.WifiOff
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material.icons.rounded.UploadFile
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.rodriguesacai.entregador.R
import com.rodriguesacai.entregador.data.Driver
import com.rodriguesacai.entregador.data.Ride
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
import com.rodriguesacai.entregador.ui.design.UpDimens
import com.rodriguesacai.entregador.ui.design.UpElevations

@Composable
fun UpLogo(modifier: Modifier = Modifier, compact: Boolean = false) {
    Image(
        painter = painterResource(id = R.drawable.up_logo_reference),
        contentDescription = "Up entregas",
        modifier = modifier.height(if (compact) 42.dp else 58.dp).fillMaxWidth(),
        contentScale = ContentScale.Fit
    )
}

@Composable
fun AuthCard(content: @Composable Column.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(UpColors.Screen)
            .padding(horizontal = 18.dp, vertical = 18.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(UpColors.Surface),
            shape = RoundedCornerShape(24.dp),
            border = UpBorders.normal,
            elevation = UpElevations.card
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 22.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                content = content
            )
        }
    }
}

@Composable
fun TopBar(title: String, onBack: (() -> Unit)? = null, right: (@Composable () -> Unit)? = null) {
    Row(
        modifier = Modifier.fillMaxWidth().height(48.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (onBack != null) {
            IconButton(onClick = onBack, modifier = Modifier.size(42.dp)) {
                Icon(Icons.Rounded.ArrowBack, contentDescription = "Voltar", tint = UpColors.Ink)
            }
        } else {
            Spacer(Modifier.size(42.dp))
        }
        Text(title, modifier = Modifier.weight(1f), textAlign = TextAlign.Center, color = UpColors.Ink, fontWeight = FontWeight.Black, fontSize = 18.sp, maxLines = 1)
        Box(Modifier.size(42.dp), contentAlignment = Alignment.Center) { right?.invoke() }
    }
}

@Composable
fun UpPage(
    title: String,
    onBack: (() -> Unit)? = null,
    current: AppRoute? = null,
    onNav: ((AppRoute) -> Unit)? = null,
    right: (@Composable () -> Unit)? = null,
    content: @Composable Column.() -> Unit
) {
    Scaffold(
        containerColor = UpColors.Screen,
        bottomBar = { if (current != null && onNav != null) UpBottomBar(current = current, onNav = onNav) }
    ) { pad ->
        Column(
            modifier = Modifier.padding(pad).fillMaxSize().padding(horizontal = 18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            TopBar(title = title, onBack = onBack, right = right)
            content()
        }
    }
}

@Composable
fun UpBottomBar(current: AppRoute, onNav: (AppRoute) -> Unit) {
    NavigationBar(containerColor = UpColors.Surface, tonalElevation = 8.dp, modifier = Modifier.height(72.dp)) {
        val items = listOf(
            NavItem(AppRoute.Home, "Início", Icons.Rounded.Home),
            NavItem(AppRoute.CorridaAndamento, "Corridas", Icons.Rounded.DeliveryDining),
            NavItem(AppRoute.Carteira, "Carteira", Icons.Rounded.AccountBalanceWallet),
            NavItem(AppRoute.Notificacoes, "Avisos", Icons.Rounded.Notifications),
            NavItem(AppRoute.Perfil, "Mais", Icons.Rounded.MoreHoriz)
        )
        items.forEach { item ->
            val selected = when (item.route) {
                AppRoute.CorridaAndamento -> current in listOf(AppRoute.CorridaAndamento, AppRoute.CorridaUrgente, AppRoute.Mapa, AppRoute.Ocorrencia)
                AppRoute.Carteira -> current in listOf(AppRoute.Carteira, AppRoute.Ganhos, AppRoute.PixBanco)
                AppRoute.Perfil -> current in listOf(AppRoute.Perfil, AppRoute.Solicitacao, AppRoute.Permissoes, AppRoute.Atualizacao, AppRoute.SemInternet, AppRoute.Manutencao, AppRoute.ErroFirebase)
                else -> current == item.route
            }
            NavigationBarItem(
                selected = selected,
                onClick = { onNav(item.route) },
                icon = { Icon(item.icon, contentDescription = item.label, modifier = Modifier.size(21.dp)) },
                label = { Text(item.label, fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1) },
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

data class NavItem(val route: AppRoute, val label: String, val icon: ImageVector)

@Composable
fun FormField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    password: Boolean = false,
    minLines: Int = 1,
    trailingIcon: (@Composable () -> Unit)? = null
) {
    var visible by remember { mutableStateOf(false) }
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label, color = UpColors.Ink, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = if (icon != null) ({ Icon(icon, contentDescription = null, tint = UpColors.Muted, modifier = Modifier.size(18.dp)) }) else null,
            trailingIcon = trailingIcon ?: if (password) ({ IconButton(onClick = { visible = !visible }) { Icon(if (visible) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility, contentDescription = "Mostrar senha", tint = UpColors.Muted) } }) else null,
            placeholder = { Text(label, color = UpColors.Subtle, fontSize = 13.sp, fontFamily = UpAppFont, maxLines = 1) },
            singleLine = minLines == 1,
            minLines = minLines,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            visualTransformation = if (password && !visible) PasswordVisualTransformation() else VisualTransformation.None,
            textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp, lineHeight = 18.sp, fontFamily = UpAppFont),
            shape = RoundedCornerShape(12.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = UpColors.Surface,
                unfocusedContainerColor = UpColors.Surface,
                disabledContainerColor = UpColors.SurfaceSoft,
                focusedIndicatorColor = UpColors.Green,
                unfocusedIndicatorColor = UpColors.Line,
                focusedTextColor = UpColors.Ink,
                unfocusedTextColor = UpColors.Ink
            )
        )
    }
}

@Composable
fun PrimaryAction(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true, icon: ImageVector? = null) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.fillMaxWidth().height(54.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(containerColor = UpColors.Green, contentColor = Color.White, disabledContainerColor = UpColors.Line, disabledContentColor = UpColors.Muted)
    ) {
        if (icon != null) { Icon(icon, contentDescription = null, modifier = Modifier.size(19.dp)); Spacer(Modifier.width(8.dp)) }
        Text(text, fontWeight = FontWeight.Black, fontSize = 15.sp)
    }
}

@Composable
fun SecondaryAction(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, icon: ImageVector? = null, red: Boolean = false) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().height(54.dp),
        shape = RoundedCornerShape(14.dp),
        border = if (red) UpBorders.red else UpBorders.green,
        colors = ButtonDefaults.outlinedButtonColors(contentColor = if (red) UpColors.Red else UpColors.Green)
    ) {
        if (icon != null) { Icon(icon, contentDescription = null, modifier = Modifier.size(19.dp)); Spacer(Modifier.width(8.dp)) }
        Text(text, fontWeight = FontWeight.Black, fontSize = 15.sp)
    }
}

@Composable
fun UpCard(modifier: Modifier = Modifier, color: Color = UpColors.Surface, border: Boolean = true, content: @Composable Column.() -> Unit) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(color),
        shape = RoundedCornerShape(22.dp),
        border = if (border) UpBorders.normal else null,
        elevation = UpElevations.card
    ) { Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp), content = content) }
}

@Composable
fun UpInfoBox(title: String, message: String, icon: ImageVector, tint: Color = UpColors.Green, soft: Color = UpColors.GreenSoft) {
    Card(colors = CardDefaults.cardColors(soft), shape = RoundedCornerShape(18.dp), border = BorderStroke(1.dp, tint.copy(alpha = .22f))) {
        Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(42.dp).clip(CircleShape).background(Color.White.copy(alpha = .7f)), contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(23.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(title, color = UpColors.Ink, fontWeight = FontWeight.Black, fontSize = 14.sp)
                Text(message, color = UpColors.Text, fontSize = 13.sp, lineHeight = 17.sp)
            }
        }
    }
}

@Composable
fun EmptyState(title: String, message: String, icon: ImageVector = Icons.Rounded.Info, action: String? = null, onAction: (() -> Unit)? = null) {
    UpCard(color = UpColors.Surface) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(48.dp).clip(CircleShape).background(UpColors.GreenSoft), contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = UpColors.Green)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(title, color = UpColors.Ink, fontWeight = FontWeight.Black, fontSize = 16.sp)
                Text(message, color = UpColors.Muted, fontSize = 13.sp, lineHeight = 18.sp)
            }
        }
        if (action != null && onAction != null) SecondaryAction(action, onAction)
    }
}

@Composable
fun DriverHeader(driver: Driver?, onNotifications: () -> Unit, onMenu: () -> Unit) {
    val name = driver?.nome?.takeIf { it.isNotBlank() }?.split(" ")?.firstOrNull() ?: "entregador"
    val subtitle = when {
        driver == null -> "Sincronizando perfil"
        driver.statusOperacional == "RESTRICAO" -> driver.restricaoMotivo.ifBlank { "Restrição operacional" }
        driver.online -> "Pronto para receber corridas"
        else -> "Indisponível no momento"
    }
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        DriverPhoto(driver)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text("Olá, $name", fontSize = 21.sp, color = UpColors.Ink, fontWeight = FontWeight.Black, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(subtitle, fontSize = 13.sp, color = if (driver?.statusOperacional == "RESTRICAO") UpColors.Red else UpColors.Muted, fontWeight = FontWeight.SemiBold, maxLines = 2)
        }
        RoundIcon(Icons.Rounded.Notifications, onNotifications)
        Spacer(Modifier.width(8.dp))
        RoundIcon(Icons.Rounded.MoreVert, onMenu)
    }
}

@Composable
fun DriverPhoto(driver: Driver?, size: Dp = 60.dp) {
    Box(Modifier.size(size).clip(CircleShape).background(UpColors.GreenSoft), contentAlignment = Alignment.Center) {
        if (!driver?.fotoUrl.isNullOrBlank()) {
            AsyncImage(model = driver?.fotoUrl, contentDescription = "Foto", contentScale = ContentScale.Crop, modifier = Modifier.size(size).clip(CircleShape))
        } else {
            Text(driver?.nome?.take(1)?.uppercase().takeIf { !it.isNullOrBlank() } ?: "UP", color = UpColors.Green, fontWeight = FontWeight.Black, fontSize = if (size > 50.dp) 18.sp else 13.sp)
        }
    }
}

@Composable
fun RoundIcon(icon: ImageVector, onClick: () -> Unit, tint: Color = UpColors.Ink, bg: Color = UpColors.Surface) {
    Box(Modifier.size(44.dp).clip(CircleShape).background(bg).border(1.dp, UpColors.Line, CircleShape).clickable(onClick = onClick), contentAlignment = Alignment.Center) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(21.dp))
    }
}

@Composable
fun AvailabilityPill(driver: Driver?, onToggle: (Boolean) -> Unit) {
    val restricted = driver?.statusOperacional == "RESTRICAO"
    val online = driver?.online == true
    val label = when {
        driver == null -> "Sincronizando"
        restricted -> "Restrição"
        online -> "Disponível"
        else -> "Indisponível"
    }
    val brush = when {
        restricted -> UpColors.RedGradient
        online -> UpColors.SuccessGradient
        else -> Brush.verticalGradient(listOf(UpColors.Ink, Color(0xFF374151)))
    }
    Box(
        modifier = Modifier.fillMaxWidth().height(58.dp).clip(RoundedCornerShape(22.dp)).background(brush).clickable(enabled = driver != null && !restricted) { onToggle(!online) }.padding(horizontal = 18.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(34.dp).clip(CircleShape).background(Color.White.copy(alpha = .14f)), contentAlignment = Alignment.Center) {
                Icon(if (restricted) Icons.Rounded.Warning else if (online) Icons.Rounded.GpsFixed else Icons.Rounded.Close, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(14.dp))
            Text(label, color = Color.White, fontWeight = FontWeight.Black, fontSize = 18.sp, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
            Icon(Icons.Rounded.KeyboardArrowDown, contentDescription = null, tint = Color.White)
        }
    }
}

@Composable
fun FinancialMini(driver: Driver?, onToggle: (Boolean) -> Unit, onWallet: () -> Unit) {
    val hidden = driver?.ocultarValores == true
    UpCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Ganhos de hoje", color = UpColors.Muted, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                Text(moneyOrEmpty(driver?.saldoHoje, hidden), color = UpColors.Ink, fontWeight = FontWeight.Black, fontSize = 28.sp)
            }
            Divider(Modifier.height(48.dp).width(1.dp), color = UpColors.Line)
            Spacer(Modifier.width(14.dp))
            Column(horizontalAlignment = Alignment.End) {
                Text("${driver?.corridasHoje ?: 0} corridas", color = UpColors.Ink, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(if (driver == null) "sincronizando" else "hoje", color = UpColors.Muted, fontSize = 13.sp)
            }
            Spacer(Modifier.width(8.dp))
            RoundIcon(if (hidden) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility, { onToggle(!hidden) }, tint = UpColors.Green, bg = UpColors.GreenSoft)
        }
    }
}

@Composable
fun OperationBanner(title: String, message: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().height(158.dp).clickable(onClick = onClick),
        colors = CardDefaults.cardColors(Color.Transparent),
        shape = RoundedCornerShape(22.dp),
        border = null
    ) {
        Box(Modifier.fillMaxSize().background(UpColors.DarkGradient).padding(18.dp)) {
            Column(Modifier.align(Alignment.CenterStart).fillMaxWidth(.55f)) {
                Text("NOVIDADES", color = Color.White.copy(.92f), fontSize = 11.sp, fontWeight = FontWeight.Black, modifier = Modifier.background(Color.White.copy(.15f), RoundedCornerShape(999.dp)).padding(horizontal = 10.dp, vertical = 4.dp))
                Spacer(Modifier.height(10.dp))
                Text(title, color = Color.White, fontWeight = FontWeight.Black, fontSize = 22.sp, lineHeight = 25.sp)
                Spacer(Modifier.height(8.dp))
                Text(message, color = Color.White.copy(.88f), fontSize = 13.sp, lineHeight = 17.sp)
            }
            PhoneBoxIllustration(Modifier.align(Alignment.CenterEnd).size(122.dp))
        }
    }
}

@Composable
fun ShortcutGrid(onHistory: () -> Unit, onEarnings: () -> Unit, onMap: () -> Unit, onSupport: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ShortcutCard("Histórico", "Ver corridas", Icons.Rounded.ListAlt, onHistory, Modifier.weight(1f))
            ShortcutCard("Ganhos", "Resumo financeiro", Icons.Rounded.AccountBalanceWallet, onEarnings, Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ShortcutCard("Mapa", "Ver região", Icons.Rounded.Map, onMap, Modifier.weight(1f))
            ShortcutCard("Suporte", "Fale conosco", Icons.Rounded.SupportAgent, onSupport, Modifier.weight(1f))
        }
    }
}

@Composable
fun ShortcutCard(title: String, subtitle: String, icon: ImageVector, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(modifier = modifier.height(86.dp).clickable(onClick = onClick), colors = CardDefaults.cardColors(UpColors.Surface), shape = RoundedCornerShape(18.dp), border = UpBorders.normal, elevation = UpElevations.card) {
        Row(Modifier.fillMaxSize().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = UpColors.Green, modifier = Modifier.size(26.dp))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(title, color = UpColors.Ink, fontWeight = FontWeight.Black, fontSize = 15.sp, maxLines = 1)
                Text(subtitle, color = UpColors.Muted, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, maxLines = 1)
            }
        }
    }
}

@Composable
fun RideOfferCard(ride: Ride, onClick: () -> Unit, compact: Boolean = false) {
    val number = ride.numeroPedido.ifBlank { "Pedido sem número" }
    UpCard(modifier = Modifier.clickable(onClick = onClick), border = true) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(number, color = UpColors.Ink, fontWeight = FontWeight.Black, fontSize = if (compact) 17.sp else 20.sp)
                Text(humanStatus(ride.status), color = statusColor(ride.status), fontWeight = FontWeight.Black, fontSize = 13.sp)
            }
            Text(safeMoney(ride.valorCorrida), color = UpColors.Green, fontWeight = FontWeight.Black, fontSize = 19.sp)
        }
        Divider(color = UpColors.Line)
        LocationRow("Coleta", ride.lojaNome.ifBlank { "Coleta pendente" }, ride.lojaEndereco.ifBlank { "Endereço da coleta ainda não informado" }, UpColors.Green)
        LocationRow("Entrega", ride.clienteBairro.ifBlank { "Bairro pendente" }, if (ride.deliveryAddressVisible()) ride.safeDeliveryAddress() else "Endereço completo liberado após retirar o pedido", UpColors.Orange)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            MetricBox("Distância", safeDistance(ride.distanciaKm), Modifier.weight(1f))
            MetricBox("Tempo", safeEta(ride.tempoEstimadoMin), Modifier.weight(1f))
        }
    }
}

@Composable
fun ActiveRidePanel(ride: Ride, onMap: () -> Unit, onAdvance: () -> Unit, onOccurrence: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        UpInfoBox(humanStatus(ride.status), when (ride.status) {
            "ACEITA", "INDO_COLETA" -> "Siga para o local de coleta."
            "CHEGUEI_COLETA" -> "Confirme a retirada quando o pedido estiver com você."
            "PEDIDO_RETIRADO", "INDO_ENTREGA" -> "Entrega liberada. Use a navegação para chegar ao cliente."
            "ENTREGADOR_NO_LOCAL" -> "Você chegou ao endereço. Finalize apenas depois da entrega."
            "OCORRENCIA" -> "Ocorrência registrada. Aguarde orientação da operação."
            else -> "Atualize a etapa conforme a operação."
        }, Icons.Rounded.DirectionsBike, statusColor(ride.status), statusColor(ride.status).copy(alpha = .10f))
        RoutePreviewCard(ride = ride, height = 205.dp, onClick = onMap)
        RideOfferCard(ride = ride, onClick = onMap, compact = true)
        SecondaryAction("Abrir mapa da rota", onMap, icon = Icons.Rounded.Map)
        PrimaryAction(nextActionText(ride.status), onAdvance, icon = Icons.Rounded.CheckCircle, enabled = ride.status != "OCORRENCIA")
        SecondaryAction("Registrar ocorrência", onOccurrence, icon = Icons.Rounded.Warning, red = true)
    }
}

@Composable
fun LocationRow(label: String, title: String, text: String, color: Color) {
    Row(verticalAlignment = Alignment.Top) {
        Box(Modifier.size(36.dp).clip(CircleShape).background(color.copy(alpha = .12f)), contentAlignment = Alignment.Center) {
            Icon(Icons.Rounded.LocationOn, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(label, color = color, fontWeight = FontWeight.Black, fontSize = 12.sp)
            Text(title, color = UpColors.Ink, fontWeight = FontWeight.Black, fontSize = 16.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(text, color = UpColors.Muted, fontSize = 13.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
fun MetricBox(label: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier, colors = CardDefaults.cardColors(UpColors.SurfaceSoft), shape = RoundedCornerShape(15.dp), border = UpBorders.normal) {
        Column(Modifier.padding(12.dp)) {
            Text(label, color = UpColors.Muted, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            Text(value, color = UpColors.Ink, fontSize = 18.sp, fontWeight = FontWeight.Black, maxLines = 1)
        }
    }
}

@Composable
fun HistoryRow(ride: Ride, onClick: () -> Unit = {}) {
    val color = statusColor(ride.status)
    UpCard(modifier = Modifier.clickable(onClick = onClick)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(48.dp).clip(CircleShape).background(color.copy(alpha = .12f)), contentAlignment = Alignment.Center) {
                Icon(if (ride.status == "RECUSADA") Icons.Rounded.Close else if (ride.status == "EXPIRADA") Icons.Rounded.Warning else Icons.Rounded.Assignment, contentDescription = null, tint = color)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(ride.numeroPedido.ifBlank { "Pedido sem número" }, color = UpColors.Ink, fontWeight = FontWeight.Black, fontSize = 16.sp)
                Text(shortDate(ride.atualizadaEm ?: ride.criadaEm), color = UpColors.Muted, fontSize = 12.sp)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(safeMoney(ride.valorCorrida), color = if (ride.valorCorrida == null) UpColors.Muted else UpColors.Green, fontWeight = FontWeight.Black)
                Text(humanStatus(ride.status), color = color, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun NotificationRow(title: String, message: String, type: String, unread: Boolean) {
    val color = when (type.uppercase()) {
        "ALERTA", "URGENTE", "WARNING" -> UpColors.Orange
        "ERRO", "ERROR" -> UpColors.Red
        else -> UpColors.Green
    }
    UpCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(48.dp).clip(CircleShape).background(color.copy(alpha = .12f)), contentAlignment = Alignment.Center) {
                Icon(if (color == UpColors.Orange) Icons.Rounded.Warning else Icons.Rounded.Notifications, contentDescription = null, tint = color)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(title.ifBlank { "Aviso" }, color = UpColors.Ink, fontWeight = FontWeight.Black, fontSize = 15.sp)
                Text(message.ifBlank { "Sem mensagem" }, color = UpColors.Muted, fontSize = 13.sp, lineHeight = 18.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
            if (unread) Box(Modifier.size(8.dp).clip(CircleShape).background(UpColors.Green))
        }
    }
}

@Composable
fun ProfileLine(icon: ImageVector, label: String, value: String, onClick: (() -> Unit)? = null) {
    Row(Modifier.fillMaxWidth().clickable(enabled = onClick != null) { onClick?.invoke() }.padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = UpColors.Green, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(14.dp))
        Text(label, color = UpColors.Text, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
        Text(value.ifBlank { "Não informado" }, color = if (value.isBlank()) UpColors.Muted else UpColors.Ink, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
fun UploadBox(title: String, subtitle: String, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().height(72.dp).clickable(onClick = onClick), colors = CardDefaults.cardColors(UpColors.Surface), border = UpBorders.normal, shape = RoundedCornerShape(14.dp)) {
        Row(Modifier.fillMaxSize().padding(horizontal = 14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(42.dp).clip(RoundedCornerShape(14.dp)).background(UpColors.GreenSoft), contentAlignment = Alignment.Center) {
                Icon(Icons.Rounded.UploadFile, contentDescription = null, tint = UpColors.Green)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(title, color = UpColors.Ink, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(subtitle, color = UpColors.Muted, fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun Stepper(current: Int, labels: List<String>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            labels.forEachIndexed { index, _ ->
                val step = index + 1
                val done = step <= current
                Box(Modifier.size(28.dp).clip(CircleShape).background(if (done) UpColors.Green else UpColors.Surface).border(1.dp, if (done) UpColors.Green else UpColors.Line, CircleShape), contentAlignment = Alignment.Center) {
                    Text(step.toString(), color = if (done) Color.White else UpColors.Muted, fontWeight = FontWeight.Black, fontSize = 12.sp)
                }
                if (index != labels.lastIndex) Box(Modifier.weight(1f).height(1.dp).background(if (step < current) UpColors.Green else UpColors.Line))
            }
        }
        Row(Modifier.fillMaxWidth()) {
            labels.forEach { Text(it, modifier = Modifier.weight(1f), textAlign = TextAlign.Center, color = UpColors.Muted, fontSize = 10.sp, maxLines = 1) }
        }
    }
}

@Composable
fun RoutePreviewCard(ride: Ride?, height: Dp = 220.dp, onClick: () -> Unit = {}) {
    Card(modifier = Modifier.fillMaxWidth().height(height).clickable(enabled = ride != null, onClick = onClick), colors = CardDefaults.cardColors(UpColors.Surface), shape = RoundedCornerShape(22.dp), border = UpBorders.normal, elevation = UpElevations.card) {
        Box(Modifier.fillMaxSize()) {
            Canvas(Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                drawRect(Color(0xFFEFF4F1))
                repeat(7) { i ->
                    val y = h * (i + 1) / 8f
                    drawLine(UpColors.MapLine.copy(alpha = .7f), Offset(0f, y), Offset(w, y + if (i % 2 == 0) 40f else -25f), strokeWidth = 4f)
                }
                repeat(6) { i ->
                    val x = w * (i + 1) / 7f
                    drawLine(Color.White.copy(alpha = .85f), Offset(x, 0f), Offset(x - 40f, h), strokeWidth = 6f)
                }
                if (ride != null && (ride.lojaLat != null || ride.clienteLat != null)) {
                    val path = Path().apply {
                        moveTo(w * .18f, h * .70f)
                        lineTo(w * .32f, h * .55f)
                        lineTo(w * .47f, h * .62f)
                        lineTo(w * .62f, h * .42f)
                        lineTo(w * .82f, h * .30f)
                    }
                    drawPath(path, UpColors.MapGreen, style = Stroke(width = 10f, cap = StrokeCap.Round))
                    drawCircle(UpColors.MapGreen, 18f, Offset(w * .18f, h * .70f))
                    drawCircle(UpColors.MapOrange, 18f, Offset(w * .82f, h * .30f))
                    drawRoundRect(Color.White, topLeft = Offset(w * .47f - 18f, h * .56f - 12f), size = Size(36f, 24f), cornerRadius = CornerRadius(8f, 8f))
                }
            }
            if (ride == null || (ride.lojaLat == null && ride.clienteLat == null)) {
                Column(Modifier.align(Alignment.Center).padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Rounded.Map, contentDescription = null, tint = UpColors.Green, modifier = Modifier.size(36.dp))
                    Spacer(Modifier.height(8.dp))
                    Text("Mapa aguardando coordenadas", color = UpColors.Ink, fontWeight = FontWeight.Black)
                    Text("A rota aparece quando a corrida real tiver localização.", color = UpColors.Muted, fontSize = 12.sp, textAlign = TextAlign.Center)
                }
            }
        }
    }
}

@Composable
fun PhoneBoxIllustration(modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        drawRoundRect(Color.White.copy(alpha = .18f), topLeft = Offset(w*.24f, h*.18f), size = Size(w*.48f, h*.66f), cornerRadius = CornerRadius(20f,20f))
        drawRoundRect(Color.White.copy(alpha = .92f), topLeft = Offset(w*.31f, h*.08f), size = Size(w*.38f, h*.58f), cornerRadius = CornerRadius(24f,24f))
        drawCircle(UpColors.Green, radius = w*.12f, center = Offset(w*.50f, h*.36f))
        drawLine(Color.White, Offset(w*.44f, h*.36f), Offset(w*.49f, h*.42f), strokeWidth = 7f, cap = StrokeCap.Round)
        drawLine(Color.White, Offset(w*.49f, h*.42f), Offset(w*.59f, h*.28f), strokeWidth = 7f, cap = StrokeCap.Round)
        drawRoundRect(Color.White.copy(alpha = .92f), topLeft = Offset(w*.10f, h*.62f), size = Size(w*.72f, h*.24f), cornerRadius = CornerRadius(8f,8f))
        drawRoundRect(UpColors.Green.copy(alpha = .65f), topLeft = Offset(w*.14f, h*.58f), size = Size(w*.22f, h*.20f), cornerRadius = CornerRadius(6f,6f))
        drawRoundRect(UpColors.Green.copy(alpha = .65f), topLeft = Offset(w*.38f, h*.58f), size = Size(w*.22f, h*.20f), cornerRadius = CornerRadius(6f,6f))
    }
}

@Composable
fun LoginMotoIllustration(modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(id = R.drawable.up_login_moto),
        contentDescription = null,
        modifier = modifier,
        contentScale = ContentScale.Fit
    )
}

@Composable
fun AnalysisIllustration(modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(id = R.drawable.up_analysis_driver),
        contentDescription = null,
        modifier = modifier,
        contentScale = ContentScale.Fit
    )
}

@Composable
fun LockIllustration(modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(id = R.drawable.up_lock_shield),
        contentDescription = null,
        modifier = modifier,
        contentScale = ContentScale.Fit
    )
}

@Composable
fun StatusHero(title: String, message: String, icon: ImageVector, color: Color, action: String, onAction: () -> Unit, secondary: String? = null, onSecondary: (() -> Unit)? = null) {
    Column(Modifier.fillMaxSize().padding(18.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(18.dp)) {
        UpLogo(compact = true)
        Spacer(Modifier.height(6.dp))
        Box(Modifier.size(170.dp).clip(CircleShape).background(color.copy(alpha = .10f)), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(76.dp))
        }
        Text(title, color = UpColors.Ink, fontSize = 23.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Center)
        Text(message, color = UpColors.Text, fontSize = 15.sp, textAlign = TextAlign.Center, lineHeight = 21.sp)
        PrimaryAction(action, onAction, icon = Icons.Rounded.CheckCircle)
        if (secondary != null && onSecondary != null) SecondaryAction(secondary, onSecondary)
    }
}

@Composable
fun PermissionItem(title: String, subtitle: String, icon: ImageVector, enabled: Boolean, onClick: () -> Unit) {
    UpCard(modifier = Modifier.clickable(onClick = onClick)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(52.dp).clip(CircleShape).background(UpColors.GreenSoft), contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = UpColors.Green, modifier = Modifier.size(26.dp))
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(title, color = UpColors.Ink, fontWeight = FontWeight.Black, fontSize = 16.sp)
                Text(subtitle, color = UpColors.Muted, fontSize = 13.sp, lineHeight = 18.sp)
            }
            if (enabled) Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = UpColors.Green) else Text("Ativar", color = UpColors.Green, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
fun CompactList(content: @Composable LazyColumn.() -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        content = content
    )
}

@Composable
fun SectionTitle(title: String, action: String? = null, onAction: (() -> Unit)? = null) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(title, color = UpColors.Ink, fontWeight = FontWeight.Black, fontSize = 17.sp, modifier = Modifier.weight(1f))
        if (action != null && onAction != null) TextButton(onClick = onAction) { Text(action, color = UpColors.Green, fontWeight = FontWeight.Black) }
    }
}
