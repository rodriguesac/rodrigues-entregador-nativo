package com.rodriguesacai.entregador.ui

import android.content.Context
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.BatteryManager
import android.provider.Settings
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
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
import androidx.compose.foundation.clickable
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import coil.compose.AsyncImage
import com.rodriguesacai.entregador.AppSettings
import com.rodriguesacai.entregador.PermissionStatusReader
import com.rodriguesacai.entregador.RodriguesFonts
import com.rodriguesacai.entregador.data.DriverHistory
import com.rodriguesacai.entregador.data.DriverProfile
import com.rodriguesacai.entregador.data.DriverRegistrationRequest
import com.rodriguesacai.entregador.data.DriverRepository
import com.rodriguesacai.entregador.data.DriverRide
import com.rodriguesacai.entregador.data.DriverStats
import com.rodriguesacai.entregador.service.AppAlertPlayer
import kotlinx.coroutines.delay

private enum class AppTab { Inicio, Corridas, Ganhos, Historico, Conta }

private val AppFont = RodriguesFonts.Montserrat
private val BgTop = Color(0xFF0B0A10)
private val BgBottom = Color(0xFF050507)
private val Panel = Color(0xFF111116)
private val PanelSoft = Color(0xFF15151C).copy(alpha = 0.96f)
private val Purple = Color(0xFF6D36D9)
private val Purple2 = Color(0xFF9B6DFF)
private val Lime = Color(0xFF82C91E)
private val LimeDark = Color(0xFF0E3E24)
private val Ink = Color.White
private val Muted = Color(0xFFC9C6D3)
private val Muted2 = Color(0xFF8C8797)
private val Danger = Color(0xFFFF4D6D)
private val Warning = Color(0xFFFFB020)
private val Blue = Color(0xFF1677FF)

private enum class AvailabilityKind { Disponivel, Indisponivel, Restricao, EmEntrega }

private data class OperationalStatus(
    val kind: AvailabilityKind,
    val label: String,
    val message: String,
    val buttonColor: Color,
    val textColor: Color,
    val canGoOnline: Boolean
)


@Composable
fun DriverHomeScreen(
    onGoOnline: () -> Unit,
    onGoOffline: () -> Unit,
    onOpenNavigator: (pickup: String, dropoff: String) -> Unit,
    onOpenNotificationSettings: () -> Unit,
    onOpenLocationSettings: () -> Unit,
    onOpenFullScreenSettings: () -> Unit,
    onOpenBatterySettings: () -> Unit
) {
    val context = LocalContext.current
    var profile by remember { mutableStateOf(DriverRepository.currentSession(context)) }
    var tab by remember { mutableStateOf(AppTab.Inicio) }
    var online by remember { mutableStateOf(DriverRepository.isOnlineSession(context)) }
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
        if (online && pendingRide != null) AppAlertPlayer.playNewRide(context)
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
                        notice = "Cadastro enviado. Aguarde a aprovação do gestor."
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

    LaunchedEffect(profile?.id) {
        if (profile != null && online) onGoOnline()
    }

    if (profile?.needsPasswordSetup == true) {
        FirstPasswordScreen(
            profile = profile!!,
            onSaved = { profile = profile!!.copy(needsPasswordSetup = false) },
            onLogout = {
                DriverRepository.logout(context) { profile = null }
            }
        )
        return
    }

    Scaffold(
        containerColor = BgBottom,
        bottomBar = {
            NavigationBar(containerColor = Color(0xFF100B16), tonalElevation = 0.dp) {
                navItem(AppTab.Inicio, tab, "Início", Icons.Filled.Home) { tab = it }
                navItem(AppTab.Corridas, tab, "Corridas", Icons.Filled.Route) { tab = it }
                navItem(AppTab.Ganhos, tab, "Ganhos", Icons.Filled.AccountBalanceWallet) { tab = it }
                navItem(AppTab.Historico, tab, "Histórico", Icons.Filled.History) { tab = it }
                navItem(AppTab.Conta, tab, "Conta", Icons.Filled.Person) { tab = it }
            }
        }
    ) { padding ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Brush.verticalGradient(listOf(BgTop, BgBottom)))
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
                        DriverRepository.setOnline(context, checked)
                        if (checked) onGoOnline() else {
                            pendingRide = null
                            onGoOffline()
                        }
                    },
                    onAccept = { ride ->
                        DriverRepository.acceptRide(context, ride.id, onDone = { pendingRide = null }, onError = { error = it })
                    },
                    onReject = { ride, reason ->
                        DriverRepository.rejectRide(context, ride.id, reason = reason, onDone = { pendingRide = null }, onError = { error = it })
                    },
                    onExpire = { ride ->
                        DriverRepository.expireRide(context, ride.id, onDone = { pendingRide = null }, onError = { error = it })
                    },
                    onUpdateRide = { ride, status ->
                        DriverRepository.updateRideStatus(context, ride.id, status, onDone = { }, onError = { error = it })
                    },
                    onOpenNavigator = onOpenNavigator
                )
                AppTab.Corridas -> RidesContent(
                    pendingRide = pendingRide,
                    activeRide = activeRide,
                    online = online,
                    onAccept = { ride ->
                        DriverRepository.acceptRide(context, ride.id, onDone = { pendingRide = null }, onError = { error = it })
                    },
                    onReject = { ride, reason ->
                        DriverRepository.rejectRide(context, ride.id, reason = reason, onDone = { pendingRide = null }, onError = { error = it })
                    },
                    onExpire = { ride ->
                        DriverRepository.expireRide(context, ride.id, onDone = { pendingRide = null }, onError = { error = it })
                    },
                    onUpdateRide = { ride, status ->
                        DriverRepository.updateRideStatus(context, ride.id, status, onDone = { }, onError = { error = it })
                    },
                    onOpenNavigator = onOpenNavigator
                )
                AppTab.Ganhos -> EarningsContent(profile!!, stats, history)
                AppTab.Historico -> HistoryContent(history)
                AppTab.Conta -> AccountContent(
                    profile = profile!!,
                    online = online,
                    onProfileChanged = { profile = DriverRepository.currentSession(context) ?: profile },
                    onOpenNotificationSettings = onOpenNotificationSettings,
                    onOpenLocationSettings = onOpenLocationSettings,
                    onOpenFullScreenSettings = onOpenFullScreenSettings,
                    onOpenBatterySettings = onOpenBatterySettings,
                    onLogout = {
                        onGoOffline()
                        DriverRepository.logout(context) {
                            profile = null
                            online = false
                            pendingRide = null
                            activeRide = null
                            tab = AppTab.Inicio
                        }
                    }
                )
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
    var vehicle by remember { mutableStateOf("Moto") }
    var plate by remember { mutableStateOf("") }
    var pix by remember { mutableStateOf("") }
    var bank by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF08090C), Color(0xFF020304))))
    ) {
        Canvas(Modifier.fillMaxSize().alpha(.55f)) {
            drawCircle(color = Lime.copy(alpha = .18f), radius = size.width * .34f, center = Offset(size.width * .86f, size.height * .05f))
            drawCircle(color = Purple.copy(alpha = .18f), radius = size.width * .30f, center = Offset(size.width * .05f, size.height * .96f))
            drawCircle(color = Color.White.copy(alpha = .04f), radius = size.width * .46f, center = Offset(size.width * .52f, size.height * .45f), style = Stroke(width = 2.dp.toPx()))
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.Center
        ) {
            LoginBrandHero()
            Spacer(Modifier.height(18.dp))

            Card(
                shape = RoundedCornerShape(34.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF111318).copy(alpha = .96f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color.White.copy(alpha = .10f), RoundedCornerShape(34.dp))
            ) {
                Column(Modifier.padding(18.dp)) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .background(Color.White.copy(alpha = .05f), RoundedCornerShape(999.dp))
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        ModeButton("Entrar", mode == "login", Modifier.weight(1f)) { mode = "login" }
                        ModeButton("Cadastrar", mode == "cadastro", Modifier.weight(1f)) { mode = "cadastro" }
                    }

                    Spacer(Modifier.height(18.dp))

                    if (mode == "login") {
                        Text("Acesso do entregador", color = Ink, fontSize = 24.sp, fontWeight = FontWeight.Black, fontFamily = AppFont)
                        Text("Entre, fique disponível e receba corridas em tempo real.", color = Muted, fontSize = 13.sp, fontFamily = AppFont)
                        Spacer(Modifier.height(16.dp))
                        AppField(login, { login = it }, "CPF ou telefone", KeyboardType.Number)
                        Spacer(Modifier.height(10.dp))
                        AppField(
                            value = password,
                            onValueChange = { password = it },
                            label = "Senha",
                            keyboardType = KeyboardType.Password,
                            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                            trailing = if (showPassword) "ocultar" else "ver",
                            onTrailing = { showPassword = !showPassword }
                        )
                        Spacer(Modifier.height(16.dp))
                        PrimaryButton(
                            text = "Entrar no app",
                            enabled = !loading,
                            loading = loading,
                            onClick = { onLogin(login, password) { loading = it } }
                        )
                        Spacer(Modifier.height(12.dp))
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .background(Lime.copy(alpha = .09f), RoundedCornerShape(20.dp))
                                .border(1.dp, Lime.copy(alpha = .18f), RoundedCornerShape(20.dp))
                                .padding(13.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("✓", color = Lime, fontWeight = FontWeight.Black, fontSize = 18.sp)
                            Spacer(Modifier.width(10.dp))
                            Text(
                                "Entregador antigo sem senha entra uma vez e cria a senha no primeiro acesso.",
                                color = Muted,
                                fontSize = 12.sp,
                                fontFamily = AppFont,
                                lineHeight = 16.sp
                            )
                        }
                    } else {
                        Text("Cadastro profissional", color = Ink, fontSize = 24.sp, fontWeight = FontWeight.Black, fontFamily = AppFont)
                        Text("Envie seus dados. O gestor aprova antes de liberar pedidos.", color = Muted, fontSize = 13.sp, fontFamily = AppFont)
                        Spacer(Modifier.height(16.dp))
                        AppField(name, { name = it }, "Nome completo")
                        Spacer(Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                            AppField(cpf, { cpf = it }, "CPF", KeyboardType.Number, modifier = Modifier.weight(1f))
                            AppField(phone, { phone = it }, "WhatsApp", KeyboardType.Phone, modifier = Modifier.weight(1f))
                        }
                        Spacer(Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                            AppField(vehicle, { vehicle = it }, "Veículo", modifier = Modifier.weight(1f))
                            AppField(plate, { plate = it.uppercase() }, "Placa", modifier = Modifier.weight(1f))
                        }
                        Spacer(Modifier.height(10.dp))
                        AppField(pix, { pix = it }, "Chave Pix")
                        Spacer(Modifier.height(10.dp))
                        AppField(bank, { bank = it }, "Banco / recebimento")
                        Spacer(Modifier.height(10.dp))
                        AppField(newPassword, { newPassword = it }, "Criar senha", KeyboardType.Password, PasswordVisualTransformation())
                        Spacer(Modifier.height(16.dp))
                        PrimaryButton(
                            text = "Enviar cadastro",
                            enabled = !loading,
                            loading = loading,
                            onClick = { onRegister(DriverRegistrationRequest(name, cpf, phone, newPassword, vehicle, plate, pix, bank)) { loading = it } }
                        )
                    }

                    if (error.isNotBlank()) {
                        Spacer(Modifier.height(12.dp))
                        StatusMessage(error, isError = true)
                    }
                    if (notice.isNotBlank()) {
                        Spacer(Modifier.height(12.dp))
                        StatusMessage(notice, isError = false)
                    }
                }
            }
            Spacer(Modifier.height(18.dp))
        }
    }
}

@Composable
private fun LoginBrandHero() {
    Card(
        shape = RoundedCornerShape(34.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF101217).copy(alpha = .90f)),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color.White.copy(alpha = .10f), RoundedCornerShape(34.dp))
    ) {
        Column(Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(68.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Brush.linearGradient(listOf(Lime, Color(0xFF3E8E18))))
                        .border(1.dp, Color.White.copy(alpha = .25f), RoundedCornerShape(24.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("R", color = Color(0xFF050706), fontSize = 32.sp, fontWeight = FontWeight.Black, fontFamily = AppFont)
                }
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text("Rodrigues Entregador", color = Ink, fontSize = 25.sp, fontWeight = FontWeight.Black, fontFamily = AppFont, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text("100% nativo • radar urgente • rota ao toque", color = Lime, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = AppFont)
                }
            }
            Spacer(Modifier.height(18.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                LoginMiniStat("Radar", "ao vivo", Modifier.weight(1f))
                LoginMiniStat("Tela cheia", "urgente", Modifier.weight(1f))
                LoginMiniStat("Repasse", "claro", Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun LoginMiniStat(title: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier
            .background(Color.White.copy(alpha = .055f), RoundedCornerShape(20.dp))
            .border(1.dp, Color.White.copy(alpha = .08f), RoundedCornerShape(20.dp))
            .padding(12.dp)
    ) {
        Text(title, color = Muted2, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = AppFont, maxLines = 1)
        Text(value, color = Ink, fontSize = 13.sp, fontWeight = FontWeight.Black, fontFamily = AppFont, maxLines = 1)
    }
}

@Composable
private fun BrandHero() {
    GlassCard(padding = 18) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(Brush.linearGradient(listOf(Purple, Purple2, Lime)))
                    .border(1.dp, Color.White.copy(alpha = .20f), RoundedCornerShape(22.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("R", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Black, fontFamily = AppFont)
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text("Rodrigues Entregador", color = Ink, fontSize = 25.sp, fontWeight = FontWeight.Black, fontFamily = AppFont)
                Text("Operação nativa para motoboys", color = Lime, fontSize = 13.sp, fontWeight = FontWeight.Bold, fontFamily = AppFont)
            }
        }
        Spacer(Modifier.height(16.dp))
        RealDeliveryMap(
            title = "Rodrigues no mapa",
            subtitle = "Mapa real nativo com rota por TomTom",
            pickupAddress = "Rodrigues Açaí e Cia, Campo Grande, MS",
            dropoffAddress = "Carandá Bosque, Campo Grande, MS"
        )
    }
}

@Composable
private fun FirstPasswordScreen(profile: DriverProfile, onSaved: () -> Unit, onLogout: () -> Unit) {
    val context = LocalContext.current
    var password by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }

    Box(
        Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(BgTop, BgBottom)))
            .padding(18.dp),
        contentAlignment = Alignment.Center
    ) {
        GlassCard(padding = 18) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Avatar(profile.name)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text("Crie sua senha", color = Ink, fontSize = 24.sp, fontWeight = FontWeight.Black)
                    Text(profile.name, color = Muted, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
            Spacer(Modifier.height(16.dp))
            Text(
                "Este entregador já existia no gestor. Para segurança, crie uma senha antes de receber corridas.",
                color = Muted,
                fontSize = 14.sp
            )
            Spacer(Modifier.height(14.dp))
            AppField(password, { password = it }, "Nova senha", KeyboardType.Password, PasswordVisualTransformation())
            Spacer(Modifier.height(10.dp))
            AppField(confirm, { confirm = it }, "Confirmar senha", KeyboardType.Password, PasswordVisualTransformation())
            if (error.isNotBlank()) {
                Spacer(Modifier.height(10.dp))
                StatusMessage(error, true)
            }
            Spacer(Modifier.height(14.dp))
            PrimaryButton(
                text = "Salvar e continuar",
                enabled = !loading,
                loading = loading,
                onClick = {
                    error = ""
                    if (password != confirm) {
                        error = "As senhas não conferem."
                    } else {
                        loading = true
                        DriverRepository.updateAccessPassword(
                            context = context,
                            newPassword = password,
                            onSuccess = {
                                loading = false
                                onSaved()
                            },
                            onError = {
                                loading = false
                                error = it
                            }
                        )
                    }
                }
            )
            Spacer(Modifier.height(10.dp))
            OutlinedButton(onClick = onLogout, modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(18.dp)) {
                Text("Sair", color = Muted)
            }
        }
    }
}

@Composable
private fun RowScope.navItem(item: AppTab, selected: AppTab, label: String, icon: ImageVector, onClick: (AppTab) -> Unit) {
    NavigationBarItem(
        selected = selected == item,
        onClick = { onClick(item) },
        icon = { Icon(icon, contentDescription = label, modifier = Modifier.size(22.dp)) },
        label = { Text(label, fontSize = 10.sp, fontFamily = AppFont, fontWeight = if (selected == item) FontWeight.Black else FontWeight.SemiBold) },
        colors = NavigationBarItemDefaults.colors(
            selectedIconColor = Ink,
            selectedTextColor = Ink,
            indicatorColor = Purple,
            unselectedIconColor = Muted2,
            unselectedTextColor = Muted2
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
    onReject: (DriverRide, String) -> Unit,
    onExpire: (DriverRide) -> Unit,
    onUpdateRide: (DriverRide, String) -> Unit,
    onOpenNavigator: (pickup: String, dropoff: String) -> Unit
) {
    val context = LocalContext.current
    var operational by remember { mutableStateOf(readOperationalStatus(context, profile, online, activeRide)) }

    LaunchedEffect(profile.id, online, activeRide?.id) {
        while (true) {
            operational = readOperationalStatus(context, profile, online, activeRide)
            delay(30_000)
        }
    }

    LaunchedEffect(operational.kind, online) {
        if (online && operational.kind == AvailabilityKind.Restricao) {
            onToggleOnline(false)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        DriverHeader(
            profile = profile,
            stats = stats,
            operational = operational,
            onStatusClick = {
                if (operational.kind == AvailabilityKind.Restricao) {
                    if (online) onToggleOnline(false)
                } else {
                    onToggleOnline(!online)
                }
            }
        )
        if (error.isNotBlank()) StatusMessage(error, true)
        EarningsStrip(stats)
        when {
            activeRide != null -> ActiveRideCard(activeRide, onOpenNavigator, onUpdateRide)
            pendingRide != null && online -> IncomingRideCard(pendingRide, onAccept, onReject, onExpire)
            operational.kind == AvailabilityKind.Disponivel -> WaitingCard(operational)
            operational.kind == AvailabilityKind.Restricao -> RestrictionCard(operational)
            else -> OfflineCard(operational)
        }
        Spacer(Modifier.height(10.dp))
    }
}

@Composable
private fun DriverHeader(
    profile: DriverProfile,
    stats: DriverStats,
    operational: OperationalStatus,
    onStatusClick: () -> Unit
) {
    GlassCard(padding = 16) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Avatar(profile.name, profile.photoUrl)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    "Olá, ${profile.name.shortName()}",
                    color = Ink,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontFamily = AppFont
                )
                Text(
                    operational.message.ifBlank { "Hoje ${DriverRepository.formatCurrency(stats.totalToday)}" },
                    color = if (operational.kind == AvailabilityKind.Restricao) Danger else if (operational.kind == AvailabilityKind.Disponivel) Lime else Muted2,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontFamily = AppFont
                )
            }
        }
        Spacer(Modifier.height(14.dp))
        Button(
            onClick = onStatusClick,
            modifier = Modifier.fillMaxWidth().height(58.dp),
            shape = RoundedCornerShape(22.dp),
            colors = ButtonDefaults.buttonColors(containerColor = operational.buttonColor, contentColor = operational.textColor)
        ) {
            Text(operational.label, fontSize = 15.sp, fontWeight = FontWeight.Black, fontFamily = AppFont)
        }
    }
}

@Composable
private fun EarningsStrip(stats: DriverStats) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
        MiniStat("Hoje", DriverRepository.formatCurrency(stats.totalToday), Modifier.weight(1.15f))
        MiniStat("Corridas", stats.finishedCount.toString(), Modifier.weight(.85f))
        MiniStat("Aceitação", "${stats.score}%", Modifier.weight(.9f))
    }
}

@Composable
private fun OfflineCard(status: OperationalStatus) {
    GlassCard(padding = 18) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Indisponível", color = Ink, fontSize = 30.sp, fontWeight = FontWeight.Black, fontFamily = AppFont)
                Text(status.message.ifBlank { "Toque no botão acima para ficar disponível" }, color = Muted, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, fontFamily = AppFont)
            }
            Box(Modifier.size(54.dp).clip(CircleShape).background(Color(0xFF25222B)), contentAlignment = Alignment.Center) {
                Icon(Icons.Filled.Circle, contentDescription = null, tint = Muted2, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
private fun RestrictionCard(status: OperationalStatus) {
    GlassCard(padding = 18, borderColor = Danger.copy(alpha = .35f)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Restrição ativa", color = Ink, fontSize = 28.sp, fontWeight = FontWeight.Black, fontFamily = AppFont)
                Text(status.message, color = Danger, fontSize = 15.sp, fontWeight = FontWeight.Black, fontFamily = AppFont)
            }
            RadarPulse(Danger, slow = true)
        }
    }
}

@Composable
private fun WaitingCard(status: OperationalStatus) {
    GlassCard(padding = 18, borderColor = Lime.copy(alpha = .30f)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Aguardando corridas", color = Ink, fontSize = 27.sp, fontWeight = FontWeight.Black, fontFamily = AppFont)
                Text(status.message.ifBlank { "Disponível para receber pedidos" }, color = Lime, fontSize = 14.sp, fontWeight = FontWeight.Black, fontFamily = AppFont)
            }
            RadarPulse(Lime, slow = false)
        }
    }
}

@Composable
private fun IncomingRideCard(
    ride: DriverRide,
    onAccept: (DriverRide) -> Unit,
    onReject: (DriverRide, String) -> Unit,
    onExpire: (DriverRide) -> Unit
) {
    var seconds by remember(ride.id) { mutableStateOf(60) }
    var rejectReason by remember(ride.id) { mutableStateOf("") }
    LaunchedEffect(ride.id) {
        seconds = 60
        while (seconds > 0) {
            delay(1000)
            seconds -= 1
        }
        onExpire(ride)
    }

    GlassCard(padding = 18, borderColor = Lime.copy(alpha = .35f)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("OFERTA URGENTE", color = Lime, fontSize = 12.sp, fontWeight = FontWeight.Black)
                Text(ride.value, color = Ink, fontSize = 44.sp, fontWeight = FontWeight.Black)
                Text("${ride.distance} • ${ride.duration} • ${ride.stops} paradas", color = Muted, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            }
            CountdownBadge(seconds)
        }
        Spacer(Modifier.height(12.dp))
        StopLine("COLETA", ride.pickup, Lime)
        StopLine("ENTREGA", ride.neighborhood.ifBlank { "Bairro da entrega" }, Purple2)
        Text("Endereço completo da entrega será liberado após aceitar.", color = Muted2, fontSize = 12.sp)
        Spacer(Modifier.height(12.dp))
        RealDeliveryMap(
            title = "Rota real da oferta",
            subtitle = "${ride.distance} • ${ride.duration}",
            pickupAddress = ride.pickup,
            dropoffAddress = ride.neighborhood.ifBlank { ride.dropoff },
            pickupLat = ride.pickupLat,
            pickupLng = ride.pickupLng,
            dropoffLat = ride.dropoffLat,
            dropoffLng = ride.dropoffLng
        )
        Spacer(Modifier.height(12.dp))
        RideFinancialPanel(ride, compact = true)
        Spacer(Modifier.height(14.dp))
        Text("Motivo da recusa (opcional)", color = Muted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        val reasons = listOf("Muito longe", "Valor baixo", "Ocupado", "Veículo", "Outro")
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            reasons.take(3).forEach { reason ->
                TinyChip(reason, rejectReason == reason, Modifier.weight(1f)) { rejectReason = if (rejectReason == reason) "" else reason }
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            reasons.drop(3).forEach { reason ->
                TinyChip(reason, rejectReason == reason, Modifier.weight(1f)) { rejectReason = if (rejectReason == reason) "" else reason }
            }
            Spacer(Modifier.weight(1f))
        }
        Spacer(Modifier.height(14.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(onClick = { onReject(ride, rejectReason) }, modifier = Modifier.weight(1f).height(58.dp), shape = RoundedCornerShape(18.dp)) { Text("Recusar", color = Danger, fontWeight = FontWeight.Bold) }
            Button(onClick = { onAccept(ride) }, modifier = Modifier.weight(1.45f).height(58.dp), shape = RoundedCornerShape(18.dp), colors = ButtonDefaults.buttonColors(containerColor = Lime, contentColor = Color(0xFF10200A))) { Text("Aceitar corrida", fontWeight = FontWeight.Black) }
        }
    }
}

@Composable
private fun ActiveRideCard(ride: DriverRide, onOpenNavigator: (pickup: String, dropoff: String) -> Unit, onUpdateRide: (DriverRide, String) -> Unit) {
    val title = when (ride.status) {
        "accepted" -> "Indo para coleta"
        "pickup" -> "Na coleta"
        "delivering" -> "Indo para entrega"
        else -> "Corrida em andamento"
    }
    GlassCard(padding = 18, borderColor = Purple2.copy(alpha = .40f)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(title, color = Ink, fontSize = 24.sp, fontWeight = FontWeight.Black)
                Text("Pedido #${ride.orderCode} • ${ride.value}", color = Muted, fontSize = 14.sp)
            }
            StatusPill(ride.status.statusLabel(), true)
        }
        Spacer(Modifier.height(12.dp))
        RealDeliveryMap(
            title = title,
            subtitle = "${ride.distance} • ${ride.duration}",
            pickupAddress = ride.pickup,
            dropoffAddress = ride.dropoff,
            pickupLat = ride.pickupLat,
            pickupLng = ride.pickupLng,
            dropoffLat = ride.dropoffLat,
            dropoffLng = ride.dropoffLng,
            mode = if (ride.status == "delivering") DeliveryMapMode.DRIVER_TO_DROPOFF else DeliveryMapMode.DRIVER_TO_PICKUP
        )
        Spacer(Modifier.height(12.dp))
        StopLine("COLETA", ride.pickup, Lime)
        StopLine("ENTREGA", ride.dropoff, Purple2)
        Spacer(Modifier.height(12.dp))
        RideFinancialPanel(ride, compact = false)
        Spacer(Modifier.height(14.dp))
        Button(
            onClick = { onOpenNavigator(ride.pickup, if (ride.status == "delivering") ride.dropoff else ride.pickup) },
            modifier = Modifier.fillMaxWidth().height(54.dp),
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Blue, contentColor = Color.White)
        ) { Text("Iniciar navegação", fontWeight = FontWeight.Black) }
        Spacer(Modifier.height(10.dp))
        when (ride.status) {
            "accepted" -> PrimaryButton("Cheguei na coleta") { onUpdateRide(ride, "pickup") }
            "pickup" -> PrimaryButton("Pedido retirado") { onUpdateRide(ride, "delivering") }
            "delivering" -> PrimaryButton("Finalizar entrega") { onUpdateRide(ride, "finished") }
        }
    }
}


@Composable
private fun RideFinancialPanel(ride: DriverRide, compact: Boolean) {
    val receivedByDriver = ride.receivedBy.uppercase().contains("ENTREGADOR") || ride.receivedBy.uppercase().contains("MOTOBOY") || ride.receivedBy.uppercase().contains("DRIVER")
    Box(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFF0B0D12))
            .border(1.dp, Color.White.copy(alpha = .08f), RoundedCornerShape(20.dp))
            .padding(14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Financeiro da corrida", color = Ink, fontSize = 15.sp, fontWeight = FontWeight.Black, fontFamily = AppFont)
                    Text("Valor do app = repasse frota/piloto", color = Muted2, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, fontFamily = AppFont)
                }
                StatusPill(ride.paymentMethod.ifBlank { "Pagamento" }.take(12), true)
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MiniStat("Seu repasse", ride.value, Modifier.weight(1f))
                if (!compact) MiniStat("Pedido", moneyOrDash(ride.clientTotalNumber), Modifier.weight(1f))
            }
            if (receivedByDriver || ride.amountToCollectNumber > 0.0) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MiniStat("Receber", moneyOrDash(ride.amountToCollectNumber), Modifier.weight(1f))
                    MiniStat("Repassar loja", moneyOrDash(ride.storeReturnNumber), Modifier.weight(1f))
                }
                if (ride.machineFeeNumber > 0.0) {
                    Text("Taxa maquininha: ${moneyOrDash(ride.machineFeeNumber)}", color = Warning, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = AppFont)
                }
            } else {
                Text("Cliente pago pela loja/app. Nada a repassar agora.", color = Lime, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = AppFont)
            }
        }
    }
}

private fun moneyOrDash(value: Double): String = if (value > 0.0) DriverRepository.formatCurrency(value) else "—"


@Composable
private fun RidesContent(
    pendingRide: DriverRide?,
    activeRide: DriverRide?,
    online: Boolean,
    onAccept: (DriverRide) -> Unit,
    onReject: (DriverRide, String) -> Unit,
    onExpire: (DriverRide) -> Unit,
    onUpdateRide: (DriverRide, String) -> Unit,
    onOpenNavigator: (pickup: String, dropoff: String) -> Unit
) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        GlassCard(padding = 18) {
            Text("Corridas", color = Ink, fontSize = 28.sp, fontWeight = FontWeight.Black, fontFamily = AppFont)
            Text("Oferta recebida, rota ativa e operação em andamento.", color = Muted, fontSize = 14.sp, fontFamily = AppFont)
        }
        when {
            activeRide != null -> ActiveRideCard(activeRide, onOpenNavigator, onUpdateRide)
            pendingRide != null && online -> IncomingRideCard(pendingRide, onAccept, onReject, onExpire)
            online -> WaitingCard(OperationalStatus(AvailabilityKind.Disponivel, "Disponível", "Disponível para receber pedidos", Lime, Color(0xFF10200A), true))
            else -> OfflineCard(OperationalStatus(AvailabilityKind.Indisponivel, "Indisponível", "Fique disponível para receber corridas", Color(0xFF232129), Ink, true))
        }
    }
}

@Composable
private fun EarningsContent(profile: DriverProfile, stats: DriverStats, history: List<DriverHistory>) {
    var visible by remember { mutableStateOf(true) }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        GlassCard(padding = 18) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Meus ganhos", color = Ink, fontSize = 28.sp, fontWeight = FontWeight.Black, fontFamily = AppFont)
                    Text("Resumo financeiro do entregador", color = Muted, fontSize = 14.sp, fontFamily = AppFont)
                }
                TextButton(onClick = { visible = !visible }) {
                    Icon(
                        if (visible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                        contentDescription = if (visible) "Ocultar valores" else "Mostrar valores",
                        tint = Muted,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
            Spacer(Modifier.height(18.dp))
            MoneyText(DriverRepository.formatCurrency(stats.totalToday), visible, 46)
            Text("Hoje • ${stats.finishedCount} corridas finalizadas", color = Lime, fontSize = 15.sp, fontWeight = FontWeight.Bold, fontFamily = AppFont)
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            MiniStat("Semana", if (visible) DriverRepository.formatCurrency(stats.totalWeek) else "R$ •••••", Modifier.weight(1f))
            MiniStat("Mês", if (visible) DriverRepository.formatCurrency(stats.totalMonth) else "R$ •••••", Modifier.weight(1f))
        }
        GlassCard(padding = 16) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Próximo repasse", color = Ink, fontSize = 20.sp, fontWeight = FontWeight.Black, fontFamily = AppFont)
                    Text(if (visible) "Pix: ${profile.pixKey.ifBlank { "não cadastrado" }}" else "Pix: •••••", color = Muted, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, fontFamily = AppFont)
                    Text(if (visible) "Banco: ${profile.bankName.ifBlank { "não informado" }}" else "Banco: •••••", color = Muted2, fontSize = 13.sp, fontFamily = AppFont)
                }
            }
        }
        HistoryContent(history, embedded = true)
    }
}

@Composable
private fun MoneyText(value: String, visible: Boolean, fontSize: Int) {
    Text(
        if (visible) value else "R$ •••••",
        color = Ink,
        fontSize = fontSize.sp,
        fontWeight = FontWeight.Black,
        fontFamily = AppFont,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}

@Composable
private fun HistoryContent(history: List<DriverHistory>, embedded: Boolean = false) {
    Column(
        modifier = if (embedded) Modifier.fillMaxWidth() else Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        GlassCard(padding = 16) {
            Text("Histórico de corridas", color = Ink, fontSize = 24.sp, fontWeight = FontWeight.Black)
            Text("Aceitas, recusadas, expiradas e finalizadas", color = Muted, fontSize = 14.sp)
            Spacer(Modifier.height(12.dp))
            if (history.isEmpty()) {
                EmptyState("Nenhum registro ainda", "As corridas aparecerão aqui automaticamente.")
            } else {
                history.take(30).forEachIndexed { index, item ->
                    HistoryRow(item)
                    if (index != history.take(30).lastIndex) Divider(color = Color.White.copy(alpha = 0.08f))
                }
            }
        }
    }
}

@Composable
private fun HistoryRow(item: DriverHistory) {
    Row(Modifier.fillMaxWidth().padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(item.action.statusLabel(), color = Ink, fontWeight = FontWeight.Black, fontSize = 15.sp)
            Text("#${item.rideId.takeLast(6).uppercase()} • ${item.createdLabel}", color = Muted2, fontSize = 12.sp)
        }
        Text(item.value.ifBlank { "—" }, color = if (item.action.contains("REJEIT", true) || item.action.contains("EXPIR", true)) Muted2 else Lime, fontWeight = FontWeight.Black)
    }
}

@Composable
private fun AccountContent(
    profile: DriverProfile,
    online: Boolean,
    onProfileChanged: () -> Unit,
    onOpenNotificationSettings: () -> Unit,
    onOpenLocationSettings: () -> Unit,
    onOpenFullScreenSettings: () -> Unit,
    onOpenBatterySettings: () -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var pix by remember { mutableStateOf(profile.pixKey) }
    var bank by remember { mutableStateOf(profile.bankName) }
    var payoutType by remember { mutableStateOf("Pix") }
    var changeRequest by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var localError by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        GlassCard(padding = 18) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Avatar(profile.name, profile.photoUrl)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(profile.name, color = Ink, fontSize = 22.sp, fontWeight = FontWeight.Black, maxLines = 1, overflow = TextOverflow.Ellipsis, fontFamily = AppFont)
                    Text("Verificado profissional", color = Lime, fontSize = 13.sp, fontWeight = FontWeight.Bold, fontFamily = AppFont)
                }
            }
            Spacer(Modifier.height(16.dp))
            InfoLine("Telefone", profile.phone.ifBlank { "Não informado" })
            InfoLine("Conta", "Verificada")
            Spacer(Modifier.height(12.dp))
            OutlinedButton(onClick = onLogout, modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(18.dp)) { Text("Sair da conta", color = Muted) }
        }

        GlassCard(padding = 18) {
            SectionTitle("Recebimento", "Pix e banco podem ser salvos direto. O gestor confere depois.")
            Spacer(Modifier.height(12.dp))
            AppField(pix, { pix = it }, "Chave Pix")
            Spacer(Modifier.height(10.dp))
            AppField(bank, { bank = it }, "Banco")
            Spacer(Modifier.height(10.dp))
            AppField(payoutType, { payoutType = it }, "Tipo de repasse")
            Spacer(Modifier.height(12.dp))
            PrimaryButton("Salvar recebimento", enabled = !loading, loading = loading) {
                localError = ""
                message = ""
                loading = true
                DriverRepository.updatePayoutData(
                    context = context,
                    pixKey = pix,
                    bankName = bank,
                    payoutType = payoutType,
                    onSuccess = {
                        loading = false
                        message = "Recebimento salvo. Conferência pendente no gestor."
                        onProfileChanged()
                    },
                    onError = {
                        loading = false
                        localError = it
                    }
                )
            }
        }

        GlassCard(padding = 18) {
            SectionTitle("Senha de acesso", "Altere sua senha quando precisar.")
            Spacer(Modifier.height(12.dp))
            AppField(password, { password = it }, "Nova senha", KeyboardType.Password, PasswordVisualTransformation())
            Spacer(Modifier.height(10.dp))
            AppField(confirmPassword, { confirmPassword = it }, "Confirmar senha", KeyboardType.Password, PasswordVisualTransformation())
            Spacer(Modifier.height(12.dp))
            PrimaryButton("Salvar senha", enabled = !loading, loading = loading) {
                localError = ""
                message = ""
                if (password != confirmPassword) {
                    localError = "As senhas não conferem."
                } else {
                    loading = true
                    DriverRepository.updateAccessPassword(
                        context = context,
                        newPassword = password,
                        onSuccess = {
                            loading = false
                            password = ""
                            confirmPassword = ""
                            message = "Senha atualizada com sucesso."
                            onProfileChanged()
                        },
                        onError = {
                            loading = false
                            localError = it
                        }
                    )
                }
            }
        }

        GlassCard(padding = 18) {
            SectionTitle("Solicitar alteração", "Telefone e e-mail passam por aprovação do gestor.")
            Spacer(Modifier.height(12.dp))
            AppField(changeRequest, { changeRequest = it }, "O que deseja alterar?")
            Spacer(Modifier.height(12.dp))
            OutlinedButton(
                enabled = !loading,
                onClick = {
                    localError = ""
                    message = ""
                    loading = true
                    DriverRepository.requestProfileChange(
                        context = context,
                        requestText = changeRequest,
                        onSuccess = {
                            loading = false
                            changeRequest = ""
                            message = "Solicitação enviada ao gestor."
                        },
                        onError = {
                            loading = false
                            localError = it
                        }
                    )
                },
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = RoundedCornerShape(18.dp)
            ) { Text("Enviar solicitação", color = Muted) }
        }

        SettingsCenterContent(
            onOpenNotificationSettings = onOpenNotificationSettings,
            onOpenLocationSettings = onOpenLocationSettings,
            onOpenFullScreenSettings = onOpenFullScreenSettings,
            onOpenBatterySettings = onOpenBatterySettings
        )

        if (localError.isNotBlank()) StatusMessage(localError, true)
        if (message.isNotBlank()) StatusMessage(message, false)
    }
}


@Composable
private fun SettingsCenterContent(
    onOpenNotificationSettings: () -> Unit,
    onOpenLocationSettings: () -> Unit,
    onOpenFullScreenSettings: () -> Unit,
    onOpenBatterySettings: () -> Unit
) {
    val context = LocalContext.current
    var navPreference by remember { mutableStateOf(AppSettings.getNavigationApp(context)) }
    var permissionStatus by remember { mutableStateOf(PermissionStatusReader.read(context)) }

    GlassCard(padding = 18) {
        SectionTitle("Ajustes", "Central organizada do app.")
        Spacer(Modifier.height(14.dp))
        SettingsSection("Configurações") {
            SettingButton("Navegação", "Atual: ${AppSettings.navigationLabel(navPreference)}")
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ModeButton("Celular", navPreference == AppSettings.NAV_AUTO, Modifier.weight(1f)) { navPreference = AppSettings.NAV_AUTO; AppSettings.setNavigationApp(context, navPreference) }
                ModeButton("Maps", navPreference == AppSettings.NAV_GOOGLE, Modifier.weight(1f)) { navPreference = AppSettings.NAV_GOOGLE; AppSettings.setNavigationApp(context, navPreference) }
                ModeButton("Waze", navPreference == AppSettings.NAV_WAZE, Modifier.weight(1f)) { navPreference = AppSettings.NAV_WAZE; AppSettings.setNavigationApp(context, navPreference) }
            }
            Spacer(Modifier.height(10.dp))
            PermissionRow("Notificações urgentes", permissionStatus.notifications) { onOpenNotificationSettings() }
            PermissionRow("Localização", permissionStatus.location) { onOpenLocationSettings() }
            PermissionRow("Tela cheia urgente", permissionStatus.fullScreenIntent) { onOpenFullScreenSettings() }
            PermissionRow("Bateria sem restrição", permissionStatus.batteryUnrestricted) { onOpenBatterySettings() }
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = { permissionStatus = PermissionStatusReader.read(context) },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(containerColor = if (permissionStatus.ready) Lime else Purple, contentColor = if (permissionStatus.ready) Color(0xFF10200A) else Color.White)
            ) { Text(if (permissionStatus.ready) "Permissões prontas" else "Atualizar permissões", fontWeight = FontWeight.Black, fontFamily = AppFont) }
        }
        SettingsSection("Financeiro") {
            SettingButton("Meu repasse", "Ganhos e próximo acerto")
            SettingButton("Acertos com a loja", "Valores recebidos e repassados")
            SettingButton("Pix e banco", "Dados de recebimento")
        }
        SettingsSection("Conta") {
            SettingButton("Dados pessoais", "Telefone/e-mail via solicitação")
            SettingButton("Veículo", "Placa, modalidade e documentos")
            SettingButton("Solicitar alteração", "Envia pedido ao gestor")
        }
        SettingsSection("Suporte") {
            SettingButton("Ajuda", "Dúvidas de operação")
            SettingButton("Falar com suporte", "Contato com a gestão")
            SettingButton("Problemas com corrida", "Relatar ocorrência")
        }
        SettingsSection("Sobre") {
            SettingButton("Versão do app", "5.3.2 aguarda loja")
            SettingButton("Termos e privacidade", "Documentos do app")
        }
    }
}

@Composable
private fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Spacer(Modifier.height(14.dp))
    Text(title, color = Lime, fontSize = 12.sp, fontWeight = FontWeight.Black, fontFamily = AppFont)
    Spacer(Modifier.height(8.dp))
    Column(verticalArrangement = Arrangement.spacedBy(8.dp), content = content)
}

@Composable
private fun SettingButton(title: String, subtitle: String) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White.copy(alpha = .06f))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, color = Ink, fontSize = 14.sp, fontWeight = FontWeight.Black, fontFamily = AppFont)
            Text(subtitle, color = Muted2, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, fontFamily = AppFont)
        }
    }
}

@Composable
private fun MoreContent(
    onOpenNotificationSettings: () -> Unit,
    onOpenLocationSettings: () -> Unit,
    onOpenFullScreenSettings: () -> Unit,
    onOpenBatterySettings: () -> Unit
) {
    val context = LocalContext.current
    var navPreference by remember { mutableStateOf(AppSettings.getNavigationApp(context)) }
    var permissionStatus by remember { mutableStateOf(PermissionStatusReader.read(context)) }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        GlassCard(padding = 18) {
            SectionTitle("Mais", "Preferências e suporte operacional.")
            Spacer(Modifier.height(16.dp))
            Text("Navegação padrão", color = Ink, fontSize = 18.sp, fontWeight = FontWeight.Black)
            Text("Escolha o app usado no botão Iniciar navegação.", color = Muted, fontSize = 13.sp)
            Spacer(Modifier.height(10.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ModeButton("Celular", navPreference == AppSettings.NAV_AUTO, Modifier.weight(1f)) { navPreference = AppSettings.NAV_AUTO; AppSettings.setNavigationApp(context, navPreference) }
                ModeButton("Maps", navPreference == AppSettings.NAV_GOOGLE, Modifier.weight(1f)) { navPreference = AppSettings.NAV_GOOGLE; AppSettings.setNavigationApp(context, navPreference) }
                ModeButton("Waze", navPreference == AppSettings.NAV_WAZE, Modifier.weight(1f)) { navPreference = AppSettings.NAV_WAZE; AppSettings.setNavigationApp(context, navPreference) }
            }
            Spacer(Modifier.height(10.dp))
            Text("Atual: ${AppSettings.navigationLabel(navPreference)}", color = Lime, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }

        GlassCard(padding = 18) {
            SectionTitle("Preparar para corridas", "Configure uma vez. Depois o alerta urgente abre automaticamente.")
            Spacer(Modifier.height(12.dp))
            PermissionRow("Notificações", permissionStatus.notifications) { onOpenNotificationSettings() }
            PermissionRow("Localização", permissionStatus.location) { onOpenLocationSettings() }
            PermissionRow("Tela urgente", permissionStatus.fullScreenIntent) { onOpenFullScreenSettings() }
            PermissionRow("Bateria liberada", permissionStatus.batteryUnrestricted) { onOpenBatterySettings() }
            Spacer(Modifier.height(10.dp))
            Button(
                onClick = { permissionStatus = PermissionStatusReader.read(context) },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(containerColor = if (permissionStatus.ready) Lime else Purple, contentColor = if (permissionStatus.ready) Color(0xFF10200A) else Color.White)
            ) { Text(if (permissionStatus.ready) "Tudo pronto" else "Atualizar checklist", fontWeight = FontWeight.Black) }
        }

        GlassCard(padding = 18) {
            SectionTitle("Suporte", "Problemas na corrida, pagamento ou acesso.")
            Spacer(Modifier.height(10.dp))
            InfoLine("Central", "Fale com o gestor")
            InfoLine("Segurança", "Use capacete e atenção na rota")
        }


        GlassCard(padding = 18) {
            Text("Rodrigues Entregador", color = Ink, fontSize = 20.sp, fontWeight = FontWeight.Black)
            Text("Versão 5.4.0 • Torre V9.4", color = Muted2, fontSize = 12.sp)
        }
    }
}


@Composable
private fun PermissionRow(label: String, ok: Boolean, onFix: () -> Unit) {
    Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
        StatusPill(if (ok) "OK" else "AJUSTAR", ok)
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(label, color = Ink, fontSize = 14.sp, fontWeight = FontWeight.Black, fontFamily = AppFont)
            Text(if (ok) "Configurado" else "Toque para liberar", color = if (ok) Lime else Warning, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, fontFamily = AppFont)
        }
        if (!ok) {
            OutlinedButton(onClick = onFix, shape = RoundedCornerShape(16.dp)) {
                Text("Abrir", color = Ink, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun GlassCard(padding: Int = 16, borderColor: Color = Color.White.copy(alpha = 0.10f), content: @Composable ColumnScope.() -> Unit) {
    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = PanelSoft),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, borderColor, RoundedCornerShape(28.dp))
    ) {
        Column(Modifier.padding(padding.dp), content = content)
    }
}

@Composable
private fun SectionTitle(title: String, subtitle: String) {
    Text(title, color = Ink, fontSize = 22.sp, fontWeight = FontWeight.Black, fontFamily = AppFont)
    Text(subtitle, color = Muted, fontSize = 13.sp, fontFamily = AppFont)
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
        label = { Text(label, fontFamily = AppFont, color = Muted) },
        singleLine = true,
        modifier = modifier,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        visualTransformation = visualTransformation,
        trailingIcon = if (trailing != null && onTrailing != null) {
            { TextButton(onClick = onTrailing) { Text(trailing, color = Lime, fontSize = 11.sp, fontFamily = AppFont) } }
        } else null
    )
}

@Composable
private fun PrimaryButton(text: String, enabled: Boolean = true, loading: Boolean = false, onClick: () -> Unit) {
    Button(
        enabled = enabled,
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(56.dp),
        shape = RoundedCornerShape(18.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Lime, contentColor = Color(0xFF061005))
    ) {
        if (loading) CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp, color = Lime)
        else Text(text, fontWeight = FontWeight.Black, fontFamily = AppFont)
    }
}

@Composable
private fun ModeButton(text: String, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = modifier.height(46.dp),
        shape = RoundedCornerShape(999.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) Lime else Color.Transparent,
            contentColor = if (selected) Color(0xFF061005) else Muted
        )
    ) { Text(text, fontWeight = FontWeight.Black, fontSize = 13.sp, fontFamily = AppFont) }
}

@Composable
private fun TinyChip(text: String, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = modifier.height(38.dp),
        shape = RoundedCornerShape(999.dp),
        colors = ButtonDefaults.buttonColors(containerColor = if (selected) Purple else Color.White.copy(alpha = .07f), contentColor = if (selected) Color.White else Muted)
    ) { Text(text, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis) }
}

@Composable
private fun StatusMessage(text: String, isError: Boolean) {
    Card(colors = CardDefaults.cardColors(containerColor = if (isError) Color(0xFF421824) else LimeDark), shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
        Text(text, color = if (isError) Color(0xFFFFC4D0) else Lime, fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(12.dp))
    }
}

@Composable
private fun Avatar(name: String, photoUrl: String = "") {
    Box(
        Modifier
            .size(58.dp)
            .clip(CircleShape)
            .background(Brush.linearGradient(listOf(Purple, Purple2, Lime)))
            .border(2.dp, Color.White.copy(alpha = 0.20f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        if (photoUrl.isNotBlank()) {
            AsyncImage(
                model = photoUrl,
                contentDescription = "Foto do entregador",
                modifier = Modifier.fillMaxSize().clip(CircleShape)
            )
        } else {
            Text(name.trim().firstOrNull()?.uppercase() ?: "E", color = Ink, fontWeight = FontWeight.Black, fontSize = 22.sp, fontFamily = AppFont)
        }
    }
}

@Composable
private fun StatusPill(text: String, positive: Boolean, modifier: Modifier = Modifier) {
    Box(
        modifier
            .clip(RoundedCornerShape(999.dp))
            .background(if (positive) LimeDark else Color(0xFF30151E))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = if (positive) Lime else Color(0xFFFFB5C4), fontSize = 11.sp, fontWeight = FontWeight.Black, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun MiniStat(label: String, value: String, modifier: Modifier) {
    Card(colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.08f)), shape = RoundedCornerShape(20.dp), modifier = modifier.border(1.dp, Color.White.copy(alpha = .08f), RoundedCornerShape(20.dp))) {
        Column(Modifier.padding(12.dp)) {
            Text(label, color = Muted2, fontSize = 12.sp)
            Text(value, color = Ink, fontSize = 17.sp, fontWeight = FontWeight.Black, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun EmptyState(title: String, subtitle: String) {
    Box(Modifier.fillMaxWidth().padding(vertical = 16.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(title, color = Ink, fontSize = 16.sp, fontWeight = FontWeight.Black)
            Text(subtitle, color = Muted2, fontSize = 12.sp, textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun RadarPulse(color: Color, slow: Boolean) {
    val transition = rememberInfiniteTransition(label = "radar")
    val pulse by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (slow) 2400 else 1700, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "radarPulse"
    )
    Box(Modifier.size(86.dp), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val base = size.minDimension / 2.6f
            drawCircle(color.copy(alpha = 0.08f * (1f - pulse)), radius = base * (0.75f + pulse), center = center)
            drawCircle(color.copy(alpha = 0.18f * (1f - pulse)), radius = base * (0.45f + pulse * 0.55f), center = center, style = Stroke(width = 4f))
            drawCircle(color.copy(alpha = 0.22f * (1f - pulse)), radius = base * (0.25f + pulse * 0.78f), center = center, style = Stroke(width = 3f))
            drawCircle(color, radius = 11f, center = center)
            drawCircle(Color.White.copy(alpha = .28f), radius = 4f, center = Offset(center.x + 4f, center.y - 4f))
        }
    }
}

@Composable
private fun NativeRoutePreview(title: String, subtitle: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(210.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(Brush.linearGradient(listOf(Color(0xFF1B2630), Color(0xFF0A0F14))))
            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(24.dp))
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val grid = Color.White.copy(alpha = 0.08f)
            for (i in 0..7) {
                val y = size.height * i / 7f
                drawLine(grid, Offset(0f, y), Offset(size.width, y + 42f), strokeWidth = 2f)
            }
            for (i in 0..6) {
                val x = size.width * i / 6f
                drawLine(grid, Offset(x, 0f), Offset(x - 46f, size.height), strokeWidth = 2f)
            }
            drawLine(Purple2, Offset(size.width * .13f, size.height * .74f), Offset(size.width * .84f, size.height * .32f), strokeWidth = 11f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(18f, 13f), 0f))
            drawCircle(Lime, 16f, Offset(size.width * .13f, size.height * .74f))
            drawCircle(Purple2, 16f, Offset(size.width * .84f, size.height * .32f))
            drawCircle(Color.White.copy(alpha = .20f), 28f, Offset(size.width * .84f, size.height * .32f))
        }
        Column(Modifier.align(Alignment.TopStart).padding(14.dp)) {
            Text("VILA NASCENTE", color = Color.White.copy(alpha = .28f), fontSize = 11.sp, fontWeight = FontWeight.Black)
            Text("CARANDÁ BOSQUE", color = Color.White.copy(alpha = .22f), fontSize = 11.sp, fontWeight = FontWeight.Black)
        }
        Column(Modifier.align(Alignment.BottomStart).padding(14.dp)) {
            Text(title, color = Ink, fontWeight = FontWeight.Black, fontSize = 18.sp)
            Text(subtitle, color = Muted, fontSize = 13.sp)
        }
    }
}

@Composable
private fun StopLine(label: String, value: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 7.dp)) {
        Box(Modifier.size(13.dp).clip(CircleShape).background(color))
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(label, color = Muted2, fontSize = 11.sp, fontWeight = FontWeight.Black)
            Text(value, color = Ink, fontSize = 15.sp, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun CountdownBadge(seconds: Int) {
    Box(
        Modifier
            .size(68.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.10f))
            .border(5.dp, if (seconds <= 10) Danger else Lime, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(seconds.toString(), color = Ink, fontWeight = FontWeight.Black, fontSize = 20.sp)
            Text("seg", color = Muted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun InfoLine(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Text(label, color = Muted2, fontSize = 13.sp, modifier = Modifier.weight(1f))
        Text(value, color = Ink, fontSize = 13.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.End, modifier = Modifier.weight(1.6f), maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}


private fun readOperationalStatus(context: Context, profile: DriverProfile, online: Boolean, activeRide: DriverRide?): OperationalStatus {
    if (activeRide != null) {
        return OperationalStatus(AvailabilityKind.EmEntrega, "Em entrega", "Corrida em andamento", Purple, Ink, false)
    }
    if (profile.blocked) {
        return OperationalStatus(AvailabilityKind.Restricao, "Restrição", "Conta bloqueada", Danger, Color.White, false)
    }
    if (!profile.approved) {
        return OperationalStatus(AvailabilityKind.Restricao, "Restrição", "Cadastro em análise", Danger, Color.White, false)
    }

    val batteryLevel = context.batteryLevelPercent()
    if (batteryLevel in 0..9) {
        return OperationalStatus(AvailabilityKind.Restricao, "Restrição", "Bateria baixa", Danger, Color.White, false)
    }
    if (!context.hasUsableInternet()) {
        return OperationalStatus(AvailabilityKind.Restricao, "Restrição", "Sem conexão", Danger, Color.White, false)
    }
    if (!context.isLocationEnabled()) {
        return OperationalStatus(AvailabilityKind.Restricao, "Restrição", "Ative a localização", Danger, Color.White, false)
    }

    val permissions = PermissionStatusReader.read(context)
    if (!permissions.location) {
        return OperationalStatus(AvailabilityKind.Restricao, "Restrição", "Permita localização", Danger, Color.White, false)
    }
    if (!permissions.notifications) {
        return OperationalStatus(AvailabilityKind.Restricao, "Restrição", "Ative alertas", Danger, Color.White, false)
    }
    if (!permissions.fullScreenIntent) {
        return OperationalStatus(AvailabilityKind.Restricao, "Restrição", "Alerta urgente pendente", Danger, Color.White, false)
    }
    if (!permissions.batteryUnrestricted) {
        return OperationalStatus(AvailabilityKind.Restricao, "Restrição", "Liberar bateria", Danger, Color.White, false)
    }

    if (!online) {
        return OperationalStatus(AvailabilityKind.Indisponivel, "Indisponível", "Toque para ficar disponível", Color(0xFF232129), Ink, true)
    }

    return OperationalStatus(AvailabilityKind.Disponivel, "Disponível", "Disponível para receber pedidos", Lime, Color(0xFF10200A), true)
}

private fun Context.batteryLevelPercent(): Int {
    val manager = getSystemService(Context.BATTERY_SERVICE) as? BatteryManager ?: return 100
    return manager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY).takeIf { it >= 0 } ?: 100
}

private fun Context.hasUsableInternet(): Boolean {
    val manager = getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return true
    val network = manager.activeNetwork ?: return false
    val caps = manager.getNetworkCapabilities(network) ?: return false
    return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) && caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
}

private fun Context.isLocationEnabled(): Boolean {
    return runCatching {
        val manager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        manager.isProviderEnabled(LocationManager.GPS_PROVIDER) || manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }.getOrDefault(false)
}

private fun String.statusLabel(): String = when {
    equals("accepted", true) -> "Aceita"
    equals("pickup", true) -> "Coleta"
    equals("delivering", true) -> "Entrega"
    equals("finished", true) -> "Finalizada"
    contains("REJEIT", true) -> "Rejeitada"
    contains("EXPIR", true) -> "Expirada"
    contains("CONCL", true) || contains("ENTREG", true) -> "Finalizada"
    else -> replaceFirstChar { it.uppercase() }
}

private fun String.shortName(): String {
    val parts = trim().split(" ").filter { it.isNotBlank() }
    return when {
        parts.isEmpty() -> "Entregador"
        parts.size == 1 -> parts.first()
        else -> "${parts.first()} ${parts.last().take(1)}."
    }
}
