package com.rodriguesacai.entregador.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rodriguesacai.entregador.data.DriverHistory
import com.rodriguesacai.entregador.data.DriverProfile
import com.rodriguesacai.entregador.data.DriverRegistrationRequest
import com.rodriguesacai.entregador.data.DriverRepository
import com.rodriguesacai.entregador.data.DriverRide
import com.rodriguesacai.entregador.data.DriverStats
import com.rodriguesacai.entregador.service.AppAlertPlayer

private enum class AppTab { Inicio, Ganhos, Historico, Conta, Mais }

private val AppFont = androidx.compose.ui.text.font.FontFamily.SansSerif

@Composable
fun DriverHomeScreen(
    onGoOnline: () -> Unit,
    onGoOffline: () -> Unit,
    onOpenNavigator: (pickup: String, dropoff: String) -> Unit,
    onOpenBatterySettings: () -> Unit
) {
    val context = LocalContext.current
    var profile by remember { mutableStateOf(DriverRepository.currentSession(context)) }
    var tab by remember { mutableStateOf(AppTab.Inicio) }
    var online by remember { mutableStateOf(false) }
    var pendingRide by remember { mutableStateOf<DriverRide?>(null) }
    var activeRide by remember { mutableStateOf<DriverRide?>(null) }
    var history by remember { mutableStateOf<List<DriverHistory>>(emptyList()) }
    var stats by remember { mutableStateOf(DriverStats()) }
    var error by remember { mutableStateOf("") }
    var notice by remember { mutableStateOf("") }

    DisposableEffect(profile?.id, online) {
        val pendingListener = if (profile != null && online) {
            DriverRepository.listenPendingRide(context, onRide = { pendingRide = it }, onError = { error = it })
        } else null
        val activeListener = if (profile != null) {
            DriverRepository.listenMyActiveRide(context, onRide = { activeRide = it }, onError = { error = it })
        } else null
        val historyListener = if (profile != null) {
            DriverRepository.listenMyHistory(context, onHistory = { history = it }, onError = { error = it })
        } else null
        val statsListener = if (profile != null) {
            DriverRepository.listenDailyStats(context, onStats = { stats = it }, onError = { error = it })
        } else null
        onDispose {
            pendingListener?.remove()
            activeListener?.remove()
            historyListener?.remove()
            statsListener?.remove()
        }
    }

    LaunchedEffect(pendingRide?.id, online) {
        if (online && pendingRide != null) {
            AppAlertPlayer.playNewRide(context)
        }
    }

    if (profile == null) {
        LoginScreen(
            error = error,
            notice = notice,
            onLogin = { value, password, setLoading ->
                error = ""
                notice = ""
                setLoading(true)
                DriverRepository.login(
                    context = context,
                    documentOrPhone = value,
                    password = password,
                    onSuccess = {
                        profile = it
                        setLoading(false)
                    },
                    onError = {
                        error = it
                        setLoading(false)
                    }
                )
            },
            onRegister = { request, setLoading ->
                error = ""
                notice = ""
                setLoading(true)
                DriverRepository.registerDriver(
                    request = request,
                    onSuccess = {
                        notice = "Cadastro enviado. Aguarde aprovacao no painel gestor para entrar."
                        setLoading(false)
                    },
                    onError = {
                        error = it
                        setLoading(false)
                    }
                )
            }
        )
        return
    }

    Scaffold(
        containerColor = Color(0xFF08050D),
        bottomBar = {
            NavigationBar(containerColor = Color(0xFF110D16), tonalElevation = 0.dp) {
                navItem(AppTab.Inicio, tab, "Início", "⌂") { tab = it }
                navItem(AppTab.Ganhos, tab, "Ganhos", "R$") { tab = it }
                navItem(AppTab.Historico, tab, "Histórico", "◷") { tab = it }
                navItem(AppTab.Conta, tab, "Conta", "◉") { tab = it }
                navItem(AppTab.Mais, tab, "Mais", "≡") { tab = it }
            }
        }
    ) { padding ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Brush.verticalGradient(listOf(Color(0xFF180722), Color(0xFF08050D))))
        ) {
            when (tab) {
                AppTab.Inicio -> HomeContent(
                    profile = profile!!,
                    online = online,
                    pendingRide = pendingRide,
                    activeRide = activeRide,
                    stats = stats,
                    error = error,
                    onToggleOnline = { checked ->
                        online = checked
                        if (checked) onGoOnline() else {
                            pendingRide = null
                            onGoOffline()
                            DriverRepository.setOnline(context, false)
                        }
                    },
                    onAccept = { ride ->
                        DriverRepository.acceptRide(context, ride.id, onDone = { pendingRide = null }, onError = { error = it })
                    },
                    onReject = { ride ->
                        DriverRepository.rejectRide(context, ride.id, onDone = { pendingRide = null }, onError = { error = it })
                    },
                    onUpdateRide = { ride, status ->
                        DriverRepository.updateRideStatus(context, ride.id, status, onDone = { }, onError = { error = it })
                    },
                    onOpenNavigator = onOpenNavigator
                )
                AppTab.Ganhos -> EarningsContent(stats, history)
                AppTab.Historico -> HistoryContent(history)
                AppTab.Conta -> AccountContent(profile!!, online, onLogout = {
                    onGoOffline()
                    DriverRepository.logout(context) {
                        profile = null
                        online = false
                        pendingRide = null
                        activeRide = null
                        tab = AppTab.Inicio
                    }
                })
                AppTab.Mais -> MoreContent(onOpenBatterySettings)
            }
        }
    }
}

@Composable
private fun LoginScreen(
    error: String,
    notice: String,
    onLogin: (String, String, (Boolean) -> Unit) -> Unit,
    onRegister: (DriverRegistrationRequest, (Boolean) -> Unit) -> Unit
) {
    var mode by remember { mutableStateOf("login") }
    var login by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var cpf by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var vehicle by remember { mutableStateOf("Moto") }
    var plate by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF13051D), Color(0xFF09090B))))
            .padding(22.dp),
        contentAlignment = Alignment.Center
    ) {
        PremiumCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(58.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(Color(0xFF4B0082), Color(0xFF82C91E))))
                        .border(1.dp, Color.White.copy(alpha = .16f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("R", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Black, fontFamily = AppFont)
                }
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text("Rodrigues Entregas", color = Color.White, fontSize = 25.sp, fontWeight = FontWeight.Black, fontFamily = AppFont)
                    Text("PainelUP Entregador • 100% nativo", color = Color(0xFF82C91E), fontSize = 13.sp, fontWeight = FontWeight.Bold, fontFamily = AppFont)
                }
            }

            Spacer(Modifier.height(18.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ModeButton("Entrar", mode == "login", Modifier.weight(1f)) { mode = "login" }
                ModeButton("Cadastrar", mode == "cadastro", Modifier.weight(1f)) { mode = "cadastro" }
            }

            Spacer(Modifier.height(18.dp))
            if (mode == "login") {
                Text("Acesso do entregador", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Black, fontFamily = AppFont)
                Text("Entre com CPF ou telefone. Senha será exigida quando existir no cadastro.", color = Color(0xFFD7CCDF), fontSize = 13.sp, fontFamily = AppFont)
                Spacer(Modifier.height(14.dp))
                AppField(
                    value = login,
                    onValueChange = { login = it },
                    label = "CPF ou telefone",
                    keyboardType = KeyboardType.Number
                )
                Spacer(Modifier.height(10.dp))
                AppField(
                    value = password,
                    onValueChange = { password = it },
                    label = "Senha, se cadastrada",
                    keyboardType = KeyboardType.Password,
                    visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                    trailing = if (showPassword) "ocultar" else "ver",
                    onTrailing = { showPassword = !showPassword }
                )
                Spacer(Modifier.height(14.dp))
                Button(
                    enabled = !loading,
                    onClick = { onLogin(login, password) { loading = it } },
                    modifier = Modifier.fillMaxWidth().height(58.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4B0082), contentColor = Color(0xFF82C91E))
                ) {
                    if (loading) CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp, color = Color(0xFF82C91E))
                    else Text("Entrar", fontWeight = FontWeight.Black, fontFamily = AppFont)
                }
            } else {
                Text("Cadastro de entregador", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Black, fontFamily = AppFont)
                Text("O cadastro nasce pendente e precisa ser aprovado no painel gestor antes de receber pedidos.", color = Color(0xFFD7CCDF), fontSize = 13.sp, fontFamily = AppFont)
                Spacer(Modifier.height(14.dp))
                AppField(name, { name = it }, "Nome completo")
                Spacer(Modifier.height(10.dp))
                AppField(cpf, { cpf = it }, "CPF", KeyboardType.Number)
                Spacer(Modifier.height(10.dp))
                AppField(phone, { phone = it }, "Telefone / WhatsApp", KeyboardType.Phone)
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    AppField(vehicle, { vehicle = it }, "Modalidade", modifier = Modifier.weight(1f))
                    AppField(plate, { plate = it }, "Placa", modifier = Modifier.weight(1f))
                }
                Spacer(Modifier.height(10.dp))
                AppField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = "Criar senha",
                    keyboardType = KeyboardType.Password,
                    visualTransformation = PasswordVisualTransformation()
                )
                Spacer(Modifier.height(14.dp))
                Button(
                    enabled = !loading,
                    onClick = {
                        onRegister(DriverRegistrationRequest(name, cpf, phone, newPassword, vehicle, plate)) { loading = it }
                    },
                    modifier = Modifier.fillMaxWidth().height(58.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4B0082), contentColor = Color(0xFF82C91E))
                ) {
                    if (loading) CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp, color = Color(0xFF82C91E))
                    else Text("Enviar cadastro", fontWeight = FontWeight.Black, fontFamily = AppFont)
                }
            }

            if (error.isNotBlank()) {
                Spacer(Modifier.height(12.dp))
                Text(error, color = Color(0xFFFFB5C4), fontSize = 13.sp, fontWeight = FontWeight.SemiBold, fontFamily = AppFont)
            }
            if (notice.isNotBlank()) {
                Spacer(Modifier.height(12.dp))
                Text(notice, color = Color(0xFF82C91E), fontSize = 13.sp, fontWeight = FontWeight.Bold, fontFamily = AppFont)
            }
            Spacer(Modifier.height(12.dp))
            Text(
                "Fluxo nativo: cadastro no app, aprovação no gestor, sessão salva no aparelho e pedidos reais pelo Firebase.",
                color = Color(0xFF9D91A8),
                fontSize = 12.sp,
                fontFamily = AppFont
            )
        }
    }
}

@Composable
private fun ModeButton(text: String, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = modifier.height(46.dp),
        shape = RoundedCornerShape(999.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) Color(0xFF4B0082) else Color.White.copy(alpha = .08f),
            contentColor = if (selected) Color(0xFF82C91E) else Color(0xFFD7CCDF)
        )
    ) { Text(text, fontWeight = FontWeight.Black, fontSize = 13.sp, fontFamily = AppFont) }
}

@Composable
private fun AppField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    trailing: String? = null,
    onTrailing: (() -> Unit)? = null,
    modifier: Modifier = Modifier.fillMaxWidth()
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, fontFamily = AppFont) },
        singleLine = true,
        modifier = modifier,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        visualTransformation = visualTransformation,
        trailingIcon = if (trailing != null && onTrailing != null) {
            { TextButton(onClick = onTrailing) { Text(trailing, color = Color(0xFF82C91E), fontSize = 11.sp, fontFamily = AppFont) } }
        } else null
    )
}

@Composable
private fun RowScope.navItem(item: AppTab, selected: AppTab, label: String, icon: String, onClick: (AppTab) -> Unit) {
    NavigationBarItem(
        selected = selected == item,
        onClick = { onClick(item) },
        icon = { Text(icon, fontSize = 19.sp, fontWeight = FontWeight.Bold) },
        label = { Text(label, fontSize = 11.sp) },
        colors = NavigationBarItemDefaults.colors(
            selectedIconColor = Color.White,
            selectedTextColor = Color.White,
            indicatorColor = Color(0xFF4B0082),
            unselectedIconColor = Color(0xFF95899E),
            unselectedTextColor = Color(0xFF95899E)
        )
    )
}

@Composable
private fun HomeContent(
    profile: DriverProfile,
    online: Boolean,
    pendingRide: DriverRide?,
    activeRide: DriverRide?,
    stats: DriverStats,
    error: String,
    onToggleOnline: (Boolean) -> Unit,
    onAccept: (DriverRide) -> Unit,
    onReject: (DriverRide) -> Unit,
    onUpdateRide: (DriverRide, String) -> Unit,
    onOpenNavigator: (pickup: String, dropoff: String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        HeaderCard(profile, online, onToggleOnline)
        if (error.isNotBlank()) AlertCard(error)
        StatsRow(stats)
        when {
            activeRide != null -> ActiveRideCard(activeRide, onOpenNavigator, onUpdateRide)
            pendingRide != null && online -> IncomingRideCard(pendingRide, onAccept, onReject)
            online -> WaitingRealRideCard()
            else -> OfflineCard()
        }
        Spacer(Modifier.height(12.dp))
    }
}

@Composable
private fun HeaderCard(profile: DriverProfile, online: Boolean, onToggleOnline: (Boolean) -> Unit) {
    PremiumCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Avatar(profile.name)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(profile.name, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Black, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(if (online) "Disponível para pedidos reais" else "Offline", color = if (online) Color(0xFF82C91E) else Color(0xFFFFC4D0), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }
            Switch(checked = online, onCheckedChange = onToggleOnline)
        }
        Spacer(Modifier.height(14.dp))
        StatusPill(if (online) "ONLINE" else "OFFLINE", online)
    }
}

@Composable
private fun Avatar(name: String) {
    Box(
        Modifier
            .size(54.dp)
            .clip(CircleShape)
            .background(Brush.linearGradient(listOf(Color(0xFF4B0082), Color(0xFF82C91E))))
            .border(2.dp, Color.White.copy(alpha = 0.20f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(name.trim().firstOrNull()?.uppercase() ?: "E", color = Color.White, fontWeight = FontWeight.Black, fontSize = 22.sp)
    }
}

@Composable
private fun StatusPill(text: String, online: Boolean) {
    Box(
        Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(if (online) Color(0xFF083C25) else Color(0xFF30151E))
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(text, color = if (online) Color(0xFF82C91E) else Color(0xFFFFB5C4), fontSize = 12.sp, fontWeight = FontWeight.Black)
    }
}

@Composable
private fun StatsRow(stats: DriverStats) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
        MiniStat("Hoje", DriverRepository.formatCurrency(stats.totalToday), Modifier.weight(1f))
        MiniStat("Entregas", stats.finishedCount.toString(), Modifier.weight(1f))
        MiniStat("Score", "${stats.score}%", Modifier.weight(1f))
    }
}

@Composable
private fun MiniStat(label: String, value: String, modifier: Modifier) {
    Card(colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.08f)), shape = RoundedCornerShape(20.dp), modifier = modifier) {
        Column(Modifier.padding(12.dp)) {
            Text(label, color = Color(0xFFBFAFCB), fontSize = 12.sp)
            Text(value, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Black, maxLines = 1)
        }
    }
}

@Composable
private fun OfflineCard() {
    PremiumCard {
        Text("Você está offline", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Black)
        Text("Ative o status para receber corridas reais do Firebase. A localização será solicitada somente ao ficar online.", color = Color(0xFFD7CCDF), fontSize = 14.sp)
        Spacer(Modifier.height(14.dp))
        NativeRoutePreview("Sem rota ativa", "Entre online para começar")
    }
}

@Composable
private fun WaitingRealRideCard() {
    PremiumCard {
        Text("Aguardando pedido real", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Black)
        Text("Sem botão de simulação na tela principal. Quando o painel criar uma corrida pendente, ela aparece aqui automaticamente.", color = Color(0xFFD7CCDF), fontSize = 14.sp)
        Spacer(Modifier.height(14.dp))
        NativeRoutePreview("Radar de entregas", "O app esta ouvindo rotas_entrega/pedidos em tempo real")
    }
}

@Composable
private fun IncomingRideCard(ride: DriverRide, onAccept: (DriverRide) -> Unit, onReject: (DriverRide) -> Unit) {
    PremiumCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Nova corrida", color = Color(0xFF82C91E), fontSize = 14.sp, fontWeight = FontWeight.Black)
                Text(ride.value, color = Color.White, fontSize = 46.sp, fontWeight = FontWeight.Black)
                Text("${ride.distance} • ${ride.duration} • ${ride.stops} paradas", color = Color(0xFFD7CCDF), fontSize = 15.sp)
            }
            CountdownBadge("60")
        }
        Spacer(Modifier.height(12.dp))
        StopLine("COLETA", ride.pickup, Color(0xFF82C91E))
        StopLine("ENTREGA", ride.dropoff, Color(0xFF4B0082))
        Spacer(Modifier.height(12.dp))
        NativeRoutePreview("Previa da rota", "${ride.distance} • ${ride.duration} • ${ride.stops} paradas")
        Spacer(Modifier.height(14.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(onClick = { onReject(ride) }, modifier = Modifier.weight(1f).height(58.dp), shape = RoundedCornerShape(18.dp)) { Text("Rejeitar") }
            Button(onClick = { onAccept(ride) }, modifier = Modifier.weight(1.5f).height(58.dp), shape = RoundedCornerShape(18.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF82C91E))) { Text("Aceitar", fontWeight = FontWeight.Black) }
        }
    }
}

@Composable
private fun ActiveRideCard(ride: DriverRide, onOpenNavigator: (pickup: String, dropoff: String) -> Unit, onUpdateRide: (DriverRide, String) -> Unit) {
    val isAccepted = ride.status == "accepted"
    val isPickup = ride.status == "pickup"
    PremiumCard {
        Text("Corrida em andamento", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Black)
        Text("Pedido #${ride.orderCode} • ${ride.value}", color = Color(0xFFD7CCDF), fontSize = 14.sp)
        Spacer(Modifier.height(12.dp))
        NativeRoutePreview(if (isAccepted || isPickup) "Indo para coleta" else "Indo para entrega", "${ride.distance} • ${ride.duration}")
        Spacer(Modifier.height(12.dp))
        StopLine("COLETA", ride.pickup, Color(0xFF82C91E))
        StopLine("ENTREGA", ride.dropoff, Color(0xFF4B0082))
        Spacer(Modifier.height(14.dp))
        Button(
            onClick = { onOpenNavigator(ride.pickup, if (ride.status == "delivering") ride.dropoff else ride.pickup) },
            modifier = Modifier.fillMaxWidth().height(54.dp),
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1677FF))
        ) { Text("Iniciar navegação", fontWeight = FontWeight.Black) }
        Spacer(Modifier.height(10.dp))
        when (ride.status) {
            "accepted" -> Button(onClick = { onUpdateRide(ride, "pickup") }, modifier = Modifier.fillMaxWidth().height(54.dp), shape = RoundedCornerShape(18.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4B0082))) { Text("Cheguei na coleta", fontWeight = FontWeight.Black) }
            "pickup" -> Button(onClick = { onUpdateRide(ride, "delivering") }, modifier = Modifier.fillMaxWidth().height(54.dp), shape = RoundedCornerShape(18.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF82C91E))) { Text("Pedido coletado", fontWeight = FontWeight.Black) }
            "delivering" -> Button(onClick = { onUpdateRide(ride, "finished") }, modifier = Modifier.fillMaxWidth().height(54.dp), shape = RoundedCornerShape(18.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF82C91E))) { Text("Finalizar entrega", fontWeight = FontWeight.Black) }
        }
    }
}

@Composable
private fun EarningsContent(stats: DriverStats, history: List<DriverHistory>) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        PremiumCard {
            Text("Ganhos", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Black)
            Text("Resumo vindo do histórico real", color = Color(0xFFD7CCDF), fontSize = 14.sp)
            Spacer(Modifier.height(18.dp))
            Text(DriverRepository.formatCurrency(stats.totalToday), color = Color.White, fontSize = 46.sp, fontWeight = FontWeight.Black)
            Text("${stats.finishedCount} entregas finalizadas", color = Color(0xFF82C91E), fontSize = 15.sp, fontWeight = FontWeight.Bold)
        }
        HistoryContent(history, embedded = true)
    }
}

@Composable
private fun HistoryContent(history: List<DriverHistory>, embedded: Boolean = false) {
    Column(
        modifier = if (embedded) Modifier.fillMaxWidth() else Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        PremiumCard {
            Text("Histórico", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Black)
            Text("Aceitas, recusadas, expiradas e finalizadas", color = Color(0xFFD7CCDF), fontSize = 14.sp)
            Spacer(Modifier.height(12.dp))
            if (history.isEmpty()) {
                Text("Nenhum registro ainda.", color = Color(0xFFBFAFCB), fontSize = 14.sp)
            } else {
                history.take(20).forEachIndexed { index, item ->
                    HistoryRow(item)
                    if (index != history.take(20).lastIndex) Divider(color = Color.White.copy(alpha = 0.08f))
                }
            }
        }
    }
}

@Composable
private fun HistoryRow(item: DriverHistory) {
    Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(item.action.statusLabel(), color = Color.White, fontWeight = FontWeight.Black, fontSize = 15.sp)
            Text("#${item.rideId.takeLast(6).uppercase()} • ${item.createdLabel}", color = Color(0xFFBFAFCB), fontSize = 12.sp)
        }
        Text(item.value.ifBlank { "—" }, color = Color(0xFF82C91E), fontWeight = FontWeight.Black)
    }
}

@Composable
private fun AccountContent(profile: DriverProfile, online: Boolean, onLogout: () -> Unit) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        PremiumCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Avatar(profile.name)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(profile.name, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Black)
                    Text(if (profile.verified) "Verificado profissional" else "Cadastro pendente", color = Color(0xFF82C91E), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.height(16.dp))
            InfoLine("Status", if (online) "Online" else "Offline")
            InfoLine("Telefone", profile.phone.ifBlank { "Não informado" })
            InfoLine("ID Firebase", profile.id)
            Spacer(Modifier.height(14.dp))
            Text("Dados pessoais e recebimento devem ser alterados pelo fluxo de solicitação/aprovação no painel gestor.", color = Color(0xFFBFAFCB), fontSize = 13.sp)
            Spacer(Modifier.height(14.dp))
            OutlinedButton(onClick = onLogout, modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(18.dp)) { Text("Sair da conta") }
        }
    }
}

@Composable
private fun MoreContent(onOpenBatterySettings: () -> Unit) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        PremiumCard {
            Text("Mais", color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Black)
            Text("Configurações operacionais do app nativo", color = Color(0xFFD7CCDF), fontSize = 14.sp)
            Spacer(Modifier.height(14.dp))
            TextButton(onClick = onOpenBatterySettings, modifier = Modifier.fillMaxWidth()) { Text("Abrir ajustes de bateria/permissões") }
            Text("Versao 3.2.0 nativo • visual PainelUP • login/cadastro", color = Color(0xFF9D91A8), fontSize = 12.sp)
        }
    }
}

@Composable
private fun PremiumCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.085f)),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color.White.copy(alpha = 0.10f), RoundedCornerShape(28.dp))
    ) {
        Column(Modifier.padding(16.dp), content = content)
    }
}

@Composable
private fun AlertCard(text: String) {
    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF421824)), shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
        Text(text, color = Color(0xFFFFC4D0), fontSize = 13.sp, modifier = Modifier.padding(12.dp))
    }
}

@Composable
private fun NativeRoutePreview(title: String, subtitle: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(210.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(Brush.linearGradient(listOf(Color(0xFF19232B), Color(0xFF0E1319))))
            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(24.dp))
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val grid = Color.White.copy(alpha = 0.08f)
            for (i in 0..6) {
                val y = size.height * i / 6f
                drawLine(grid, Offset(0f, y), Offset(size.width, y + 34f), strokeWidth = 2f)
            }
            for (i in 0..5) {
                val x = size.width * i / 5f
                drawLine(grid, Offset(x, 0f), Offset(x - 45f, size.height), strokeWidth = 2f)
            }
            drawLine(Color(0xFF4B0082), Offset(size.width * .16f, size.height * .72f), Offset(size.width * .82f, size.height * .30f), strokeWidth = 9f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 12f), 0f))
            drawCircle(Color(0xFF82C91E), 14f, Offset(size.width * .16f, size.height * .72f))
            drawCircle(Color(0xFF4B0082), 14f, Offset(size.width * .82f, size.height * .30f))
        }
        Column(Modifier.align(Alignment.BottomStart).padding(14.dp)) {
            Text(title, color = Color.White, fontWeight = FontWeight.Black, fontSize = 18.sp)
            Text(subtitle, color = Color(0xFFD7CCDF), fontSize = 13.sp)
        }
    }
}

@Composable
private fun StopLine(label: String, value: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 6.dp)) {
        Box(Modifier.size(12.dp).clip(CircleShape).background(color))
        Spacer(Modifier.width(10.dp))
        Column {
            Text(label, color = Color(0xFFBFAFCB), fontSize = 11.sp, fontWeight = FontWeight.Black)
            Text(value, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun CountdownBadge(text: String) {
    Box(
        Modifier
            .size(64.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.10f))
            .border(5.dp, Color.White, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = Color.White, fontWeight = FontWeight.Black, fontSize = 20.sp)
    }
}

@Composable
private fun InfoLine(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Text(label, color = Color(0xFFBFAFCB), fontSize = 13.sp, modifier = Modifier.weight(1f))
        Text(value, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.End, modifier = Modifier.weight(1.5f))
    }
}

private fun String.statusLabel(): String = when (this) {
    "accepted" -> "Corrida aceita"
    "pickup" -> "Chegou na coleta"
    "delivering" -> "Saiu para entrega"
    "finished" -> "Entrega finalizada"
    "rejected" -> "Corrida rejeitada"
    "expired" -> "Oferta expirada"
    else -> replaceFirstChar { it.uppercase() }
}
