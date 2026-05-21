package com.rodriguesacai.entregador.ui

import android.content.Context
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.BatteryManager
import android.provider.Settings
import java.util.Locale
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.layout.ContentScale
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
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Place
import coil.compose.AsyncImage
import com.rodriguesacai.entregador.AppSettings
import com.rodriguesacai.entregador.PermissionStatusReader
import com.rodriguesacai.entregador.RodriguesFonts
import com.rodriguesacai.entregador.data.AppCarouselBanner
import com.rodriguesacai.entregador.data.DriverHistory
import com.rodriguesacai.entregador.data.DriverProfile
import com.rodriguesacai.entregador.data.DriverRegistrationRequest
import com.rodriguesacai.entregador.data.DriverRepository
import com.rodriguesacai.entregador.data.DriverRide
import com.rodriguesacai.entregador.data.DriverStats
import com.rodriguesacai.entregador.service.AppAlertPlayer
import com.rodriguesacai.entregador.service.NotificationHelper
import kotlinx.coroutines.delay

private enum class AppTab { Inicio, Corridas, Ganhos, Historico, Conta }

private val AppFont = RodriguesFonts.Montserrat
private val BgTop = Color(0xFFF7FAF4)
private val BgBottom = Color(0xFFF1F5EE)
private val Panel = Color.White
private val PanelSoft = Color.White
private val Purple = Color(0xFF008A2E)
private val Purple2 = Color(0xFFFF7A00)
private val Lime = Color(0xFF118C35)
private val LimeDark = Color(0xFF006B2A)
private val Ink = Color(0xFF111318)
private val Muted = Color(0xFF4F5864)
private val Muted2 = Color(0xFF8A929D)
private val Danger = Color(0xFFFF4D6D)
private val Warning = Color(0xFFE99A00)
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
    var online by remember { mutableStateOf(false) }
    var pendingRide by remember { mutableStateOf<DriverRide?>(null) }
    var activeRide by remember { mutableStateOf<DriverRide?>(null) }
    var history by remember { mutableStateOf<List<DriverHistory>>(emptyList()) }
    var stats by remember { mutableStateOf(DriverStats()) }
    var appBanners by remember { mutableStateOf<List<AppCarouselBanner>>(emptyList()) }
    var error by remember { mutableStateOf("") }
    var notice by remember { mutableStateOf("") }
    var hideValues by remember { mutableStateOf(AppSettings.getHideValues(context)) }
    var themeMode by remember { mutableStateOf(AppSettings.getThemeMode(context)) }

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
        val carouselListener = if (profile != null) {
            DriverRepository.listenAppCarousel(onBanners = { appBanners = it }, onError = { /* carrossel vazio nao bloqueia operacao */ })
        } else null
        onDispose {
            pendingListener?.remove()
            activeListener?.remove()
            historyListener?.remove()
            statsListener?.remove()
            carouselListener?.remove()
        }
    }

    LaunchedEffect(pendingRide?.id, online) {
        val ride = pendingRide
        if (online && ride != null) {
            NotificationHelper.urgentRideNotification(
                context = context,
                rideId = ride.id,
                value = ride.value,
                distance = ride.distance,
                duration = ride.duration,
                pickup = ride.pickup,
                dropoff = ride.dropoff
            )
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

    val isLightTheme = themeMode == AppSettings.THEME_LIGHT
    val screenBg = BgBottom
    val topBg = BgTop
    val navBg = Color.White

    Scaffold(
        containerColor = screenBg,
        bottomBar = {
            NavigationBar(containerColor = navBg, tonalElevation = 0.dp) {
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
                .background(Brush.verticalGradient(listOf(topBg, screenBg)))
        ) {
            when (tab) {
                AppTab.Inicio -> HomeContent(
                    profile = profile!!,
                    online = online,
                    pendingRide = pendingRide,
                    activeRide = activeRide,
                    stats = stats,
                    appBanners = appBanners,
                    hideValues = hideValues,
                    onToggleValues = {
                        hideValues = !hideValues
                        AppSettings.setHideValues(context, hideValues)
                    },
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
                    onOpenNavigator = onOpenNavigator,
                    onCarouselInternal = { target ->
                        val normalized = target.lowercase(Locale.ROOT)
                        tab = when {
                            normalized.contains("ganho") || normalized.contains("carteira") || normalized.contains("repasse") -> AppTab.Ganhos
                            normalized.contains("histor") || normalized.contains("corrida") -> AppTab.Historico
                            normalized.contains("conta") || normalized.contains("perfil") || normalized.contains("pix") || normalized.contains("banco") -> AppTab.Conta
                            normalized.contains("rota") || normalized.contains("mapa") -> AppTab.Corridas
                            else -> AppTab.Inicio
                        }
                    }
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
                    hideValues = hideValues,
                    themeMode = themeMode,
                    onToggleValues = {
                        hideValues = !hideValues
                        AppSettings.setHideValues(context, hideValues)
                    },
                    onThemeChanged = {
                        themeMode = it
                        AppSettings.setThemeMode(context, it)
                    },
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
            .background(Brush.verticalGradient(listOf(BgTop, BgBottom)))
            .padding(18.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Center
        ) {
            Text("Rodrigues entregas", color = Ink, fontSize = 30.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth(), fontFamily = AppFont)
            Text("Acesso do entregador", color = Lime, fontSize = 14.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth(), fontFamily = AppFont)
            Spacer(Modifier.height(18.dp))
            GlassCard(padding = 18) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    ModeButton("Entrar", mode == "login", Modifier.weight(1f)) { mode = "login" }
                    ModeButton("Cadastrar", mode == "cadastro", Modifier.weight(1f)) { mode = "cadastro" }
                }
                Spacer(Modifier.height(18.dp))

                if (mode == "login") {
                    SectionTitle("Entrar", "CPF ou telefone e senha.")
                    Spacer(Modifier.height(14.dp))
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
                    Spacer(Modifier.height(14.dp))
                    PrimaryButton(
                        text = "Entrar",
                        enabled = !loading,
                        loading = loading,
                        onClick = { onLogin(login, password) { loading = it } }
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(
                        "Entregador antigo sem senha entra uma vez e cria a senha no primeiro acesso.",
                        color = Muted2,
                        fontSize = 12.sp,
                        fontFamily = AppFont
                    )
                } else {
                    SectionTitle("Cadastro de entregador", "Envie seus dados. O gestor aprova antes de liberar pedidos.")
                    Spacer(Modifier.height(14.dp))
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
                    Spacer(Modifier.height(14.dp))
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
            Spacer(Modifier.height(18.dp))
        }
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
            indicatorColor = Lime.copy(alpha = .16f),
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
    appBanners: List<AppCarouselBanner>,
    hideValues: Boolean,
    onToggleValues: () -> Unit,
    error: String,
    onToggleOnline: (Boolean) -> Unit,
    onAccept: (DriverRide) -> Unit,
    onReject: (DriverRide, String) -> Unit,
    onExpire: (DriverRide) -> Unit,
    onUpdateRide: (DriverRide, String) -> Unit,
    onOpenNavigator: (pickup: String, dropoff: String) -> Unit,
    onCarouselInternal: (String) -> Unit
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
            hideValues = hideValues,
            onToggleValues = onToggleValues,
            onStatusClick = {
                if (operational.kind == AvailabilityKind.Restricao) {
                    if (online) onToggleOnline(false)
                } else {
                    onToggleOnline(!online)
                }
            }
        )
        if (error.isNotBlank()) StatusMessage(error, true)
        EarningsStrip(stats, hideValues)
        if (activeRide == null && pendingRide == null) {
            val visibleBanners = if (appBanners.isNotEmpty()) appBanners else defaultHomeBanners(operational)
            AppHomeCarousel(visibleBanners, onInternalAction = onCarouselInternal)
            QuickActionsGrid(onInternalAction = onCarouselInternal)
        }
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


private fun defaultHomeBanners(status: OperationalStatus): List<AppCarouselBanner> {
    val first = when (status.kind) {
        AvailabilityKind.Disponivel -> AppCarouselBanner(
            id = "local-disponivel",
            title = "Você está na fila",
            badge = "DISPONÍVEL",
            description = "Mantenha o app aberto ou em segundo plano. Quando uma corrida chegar, o alerta urgente aparece na tela.",
            buttonText = "Aguardando",
            order = 1,
            active = true
        )
        AvailabilityKind.Restricao -> AppCarouselBanner(
            id = "local-restricao",
            title = "Resolva a restrição",
            badge = "ATENÇÃO",
            description = status.message.ifBlank { "Confira localização, internet, notificações e bateria para voltar a receber corridas." },
            buttonText = "Ver conta",
            order = 1,
            active = true
        )
        AvailabilityKind.EmEntrega -> AppCarouselBanner(
            id = "local-rota",
            title = "Corrida em andamento",
            badge = "ROTA",
            description = "Siga as etapas da coleta e entrega. O carrossel volta quando você estiver livre.",
            buttonText = "Em rota",
            order = 1,
            active = true
        )
        else -> AppCarouselBanner(
            id = "local-offline",
            title = "Fique disponível",
            badge = "INÍCIO",
            description = "Ative o status quando estiver pronto. O painel pode enviar avisos, campanhas e comunicados aqui.",
            buttonText = "Conectar",
            order = 1,
            active = true
        )
    }
    return listOf(
        first,
        AppCarouselBanner(
            id = "local-repasse",
            title = "Repasse organizado",
            badge = "CARTEIRA",
            description = "Acompanhe corridas, valores do dia e conferência de recebimento direto no app.",
            buttonText = "Ganhos",
            order = 2,
            active = true
        ),
        AppCarouselBanner(
            id = "local-dica",
            title = "Operação limpa",
            badge = "DICA",
            description = "Endereço completo e rota aparecem na hora certa, sem poluir a tela principal do entregador.",
            buttonText = "Entendi",
            order = 3,
            active = true
        )
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AppHomeCarousel(banners: List<AppCarouselBanner>, onInternalAction: (String) -> Unit) {
    if (banners.isEmpty()) return
    val safeBanners = banners.filter { it.isVisible() }.ifEmpty { banners.filter { it.active }.ifEmpty { banners } }
    val pagerState = rememberPagerState(pageCount = { safeBanners.size })
    val uriHandler = LocalUriHandler.current

    LaunchedEffect(safeBanners.size) {
        if (safeBanners.size > 1) {
            while (true) {
                delay(6_000)
                val next = (pagerState.currentPage + 1) % safeBanners.size
                pagerState.animateScrollToPage(next)
            }
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(21f / 8f)
        ) { page ->
            val banner = safeBanners[page]
            AppHomeBannerCard(
                banner = banner,
                onAction = {
                    val action = banner.actionType.lowercase(Locale.ROOT)
                    val target = banner.actionTarget.trim()
                    when {
                        action == "link" && target.startsWith("http", ignoreCase = true) -> uriHandler.openUri(target)
                        action == "internal" || action == "modal" -> onInternalAction(target)
                    }
                }
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            safeBanners.forEachIndexed { index, _ ->
                val selected = index == pagerState.currentPage
                Box(
                    Modifier
                        .padding(horizontal = 3.dp)
                        .height(7.dp)
                        .width(if (selected) 26.dp else 7.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(if (selected) Lime else Color(0xFFD8DED6))
                )
            }
        }
    }
}

@Composable
private fun AppHomeBannerCard(banner: AppCarouselBanner, onAction: () -> Unit) {
    val hasImage = banner.imageUrl.isNotBlank()
    val normalizedMode = banner.displayMode.normalizedBannerMode()
    val placeholderText = banner.title.isPlaceholderBannerText() && banner.description.isPlaceholderBannerText()
    val textOnly = normalizedMode == "text_only"
    val manualNoOverlay = banner.showTitle == false && banner.showDescription == false && banner.showBadge == false
    val imageOnly = !textOnly && (normalizedMode == "image_only" || manualNoOverlay || (hasImage && ((banner.title.isBlank() && banner.description.isBlank()) || placeholderText)))
    val showBadge = !imageOnly && (banner.showBadge ?: banner.badge.isNotBlank())
    val showTitle = !imageOnly && (banner.showTitle ?: banner.title.isNotBlank())
    val showDescription = !imageOnly && (banner.showDescription ?: banner.description.isNotBlank())
    val showButton = !imageOnly && (banner.showButton ?: true) && banner.buttonText.isNotBlank() && banner.actionType.lowercase(Locale.ROOT) != "none"
    val clickable = banner.actionType.lowercase(Locale.ROOT) != "none" && banner.actionTarget.isNotBlank()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(26.dp))
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFF003B17), Color(0xFF058832), Color(0xFF06130A))
                )
            )
            .border(1.dp, Color(0xFFE1E7DE), RoundedCornerShape(26.dp))
            .then(if (clickable) Modifier.clickable { onAction() } else Modifier)
    ) {
        if (hasImage && !textOnly) {
            AsyncImage(
                model = banner.imageUrl,
                contentDescription = banner.title.ifBlank { "Banner do app" },
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize()
            )
            if (!imageOnly) {
                Box(Modifier.matchParentSize().background(Brush.horizontalGradient(listOf(Color(0xE6000000), Color(0xAA000000), Color.Transparent))))
            }
        } else {
            Canvas(Modifier.matchParentSize()) {
                drawCircle(color = Color.White.copy(alpha = .14f), radius = size.minDimension * .52f, center = Offset(size.width * .92f, size.height * .10f))
                drawCircle(color = Lime.copy(alpha = .24f), radius = size.minDimension * .36f, center = Offset(size.width * .74f, size.height * .92f))
                drawLine(Color.White.copy(alpha = .16f), Offset(size.width * .12f, size.height * .78f), Offset(size.width * .85f, size.height * .28f), strokeWidth = 8f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(18f, 14f), 0f))
            }
        }

        if (!imageOnly) {
            Column(
                modifier = Modifier.fillMaxSize().padding(18.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    if (showBadge) {
                        Box(Modifier.clip(RoundedCornerShape(999.dp)).background(Color.White.copy(alpha = .18f)).padding(horizontal = 10.dp, vertical = 5.dp)) {
                            Text(
                                banner.badge.ifBlank { "AVISO" }.uppercase(Locale.ROOT),
                                color = Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.1.sp,
                                fontFamily = AppFont,
                                maxLines = 1
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                    }
                    if (showTitle) {
                        Text(
                            banner.title,
                            color = Color.White,
                            fontSize = 23.sp,
                            lineHeight = 24.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = AppFont,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(Modifier.height(5.dp))
                    }
                    if (showDescription) {
                        Text(
                            banner.description,
                            color = Color.White.copy(alpha = .88f),
                            fontSize = 11.sp,
                            lineHeight = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = AppFont,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                if (showButton) {
                    Button(
                        onClick = onAction,
                        modifier = Modifier.height(38.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = LimeDark)
                    ) { Text(banner.buttonText, fontSize = 11.sp, fontWeight = FontWeight.Black, fontFamily = AppFont) }
                }
            }
        }
    }
}

private fun String.normalizedBannerMode(): String {
    val value = trim().lowercase(Locale.ROOT)
        .replace("-", "_")
        .replace(" ", "_")
        .replace("ó", "o")
        .replace("ã", "a")
        .replace("ç", "c")
    return when (value) {
        "image_only", "imagem", "imagem_only", "so_imagem", "somente_imagem", "banner_pronto", "full_image", "arte_pronta", "apenas_imagem", "foto_cheia", "sem_overlay" -> "image_only"
        "text_only", "texto", "so_texto", "somente_texto", "card_texto" -> "text_only"
        else -> value.ifBlank { "auto" }
    }
}

private fun String.isPlaceholderBannerText(): Boolean {
    val value = trim().lowercase(Locale.ROOT)
        .replace("í", "i")
        .replace("ã", "a")
        .replace("á", "a")
        .replace("é", "e")
        .replace("ç", "c")
    if (value.isBlank()) return true
    return value.contains("banner sem") ||
        value.contains("sem titulo") ||
        value.contains("sem titul") ||
        value.contains("comunicado do painel") ||
        value.contains("mensagem enviada pelo gestor") ||
        value.contains("informe um titulo") ||
        value == "saiba mais"
}

@Composable
private fun QuickActionsGrid(onInternalAction: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            QuickActionTile("Histórico", "Ver corridas", Icons.Filled.History, Modifier.weight(1f)) { onInternalAction("historico") }
            QuickActionTile("Ganhos", "Resumo financeiro", Icons.Filled.AccountBalanceWallet, Modifier.weight(1f)) { onInternalAction("ganhos") }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            QuickActionTile("Mapa", "Rota e região", Icons.Filled.Map, Modifier.weight(1f)) { onInternalAction("mapa") }
            QuickActionTile("Conta", "Pix e ajustes", Icons.Filled.Person, Modifier.weight(1f)) { onInternalAction("conta") }
        }
    }
}

@Composable
private fun QuickActionTile(title: String, subtitle: String, icon: ImageVector, modifier: Modifier, onClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(24.dp),
        modifier = modifier
            .border(1.dp, Color(0xFFE4EAE2), RoundedCornerShape(24.dp))
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color(0xFFE8F8EC)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = Lime, modifier = Modifier.size(24.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(title, color = Ink, fontSize = 15.sp, fontWeight = FontWeight.Black, fontFamily = AppFont, maxLines = 1)
                Text(subtitle, color = Muted, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, fontFamily = AppFont, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable
private fun DriverHeader(
    profile: DriverProfile,
    stats: DriverStats,
    operational: OperationalStatus,
    hideValues: Boolean,
    onToggleValues: () -> Unit,
    onStatusClick: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Avatar(profile.name, profile.photoUrl)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    "Olá, ${profile.name.shortName()}",
                    color = Ink,
                    fontSize = 23.sp,
                    fontWeight = FontWeight.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontFamily = AppFont
                )
                Text(
                    operational.message.ifBlank { "Pronto para receber corridas" },
                    color = Muted,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontFamily = AppFont
                )
            }
            HeaderRoundIcon(Icons.Filled.History)
            Spacer(Modifier.width(8.dp))
            HeaderRoundIcon(Icons.Filled.Person)
        }

        Button(
            onClick = onStatusClick,
            modifier = Modifier.fillMaxWidth().height(72.dp),
            shape = RoundedCornerShape(26.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = operational.buttonColor,
                contentColor = operational.textColor
            )
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = if (operational.kind == AvailabilityKind.Indisponivel) .55f else .16f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Circle, contentDescription = null, tint = operational.textColor, modifier = Modifier.size(15.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.Center) {
                    Text(operational.label, fontSize = 19.sp, fontWeight = FontWeight.Black, fontFamily = AppFont, maxLines = 1)
                    Text(
                        when (operational.kind) {
                            AvailabilityKind.Disponivel -> "Você está online para receber corridas"
                            AvailabilityKind.Restricao -> "Resolva a pendência para voltar a receber ofertas"
                            AvailabilityKind.EmEntrega -> "Corrida ativa em andamento"
                            else -> "Toque para ficar disponível"
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = operational.textColor.copy(alpha = .82f),
                        fontFamily = AppFont,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Icon(Icons.Filled.ExpandLess, contentDescription = null, tint = operational.textColor.copy(alpha = .85f), modifier = Modifier.size(22.dp))
            }
        }
    }
}

@Composable
private fun HeaderRoundIcon(icon: ImageVector) {
    Box(
        Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(Color.White)
            .border(1.dp, Color(0xFFE2E8E0), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = Ink, modifier = Modifier.size(21.dp))
    }
}

@Composable
private fun EarningsStrip(stats: DriverStats, hideValues: Boolean) {
    GlassCard(padding = 0) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1.2f)) {
                Text("Ganhos do dia", color = Muted, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = AppFont)
                Text(
                    if (hideValues) "R$ •••••" else DriverRepository.formatCurrency(stats.totalToday),
                    color = Ink,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontFamily = AppFont
                )
            }
            Divider(Modifier.height(46.dp).width(1.dp), color = Color(0xFFE5EAE4))
            Column(Modifier.weight(.85f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Corridas", color = Muted, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = AppFont)
                Text(stats.finishedCount.toString(), color = Ink, fontSize = 22.sp, fontWeight = FontWeight.Black, fontFamily = AppFont)
            }
            Divider(Modifier.height(46.dp).width(1.dp), color = Color(0xFFE5EAE4))
            Column(Modifier.weight(.85f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Aceitação", color = Muted, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = AppFont)
                Text("${stats.score}%", color = Lime, fontSize = 22.sp, fontWeight = FontWeight.Black, fontFamily = AppFont)
            }
        }
    }
}

@Composable
private fun OfflineCard(status: OperationalStatus) {
    GlassCard(padding = 18) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Indisponível", color = Ink, fontSize = 25.sp, fontWeight = FontWeight.Black, fontFamily = AppFont)
                Text(status.message.ifBlank { "Fique disponível para receber corridas" }, color = Muted, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, fontFamily = AppFont)
            }
            Box(Modifier.size(54.dp).clip(CircleShape).background(Color(0xFFE9ECE7)), contentAlignment = Alignment.Center) {
                Icon(Icons.Filled.Circle, contentDescription = null, tint = Muted2, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
private fun RestrictionCard(status: OperationalStatus) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF4F7)),
        shape = RoundedCornerShape(28.dp),
        modifier = Modifier.fillMaxWidth().border(1.dp, Danger.copy(alpha = .28f), RoundedCornerShape(28.dp))
    ) {
        Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Restrição ativa", color = Ink, fontSize = 25.sp, fontWeight = FontWeight.Black, fontFamily = AppFont)
                Text(status.message, color = Danger, fontSize = 15.sp, fontWeight = FontWeight.Black, fontFamily = AppFont)
                Spacer(Modifier.height(6.dp))
                Text("O app bloqueia disponibilidade para evitar oferta perdida e problema operacional.", color = Muted, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, fontFamily = AppFont)
            }
            RadarPulse(Danger, slow = true)
        }
    }
}

@Composable
private fun WaitingCard(status: OperationalStatus) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFFEFFAF2)),
        shape = RoundedCornerShape(28.dp),
        modifier = Modifier.fillMaxWidth().border(1.dp, Lime.copy(alpha = .24f), RoundedCornerShape(28.dp))
    ) {
        Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Aguardando corridas", color = Ink, fontSize = 25.sp, fontWeight = FontWeight.Black, fontFamily = AppFont)
                Text("Você está pronto para receber ofertas da operação.", color = Lime, fontSize = 13.sp, fontWeight = FontWeight.Black, fontFamily = AppFont)
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

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .background(Danger)
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("NOVA CORRIDA URGENTE", color = Color.White, fontWeight = FontWeight.Black, fontSize = 14.sp, fontFamily = AppFont, modifier = Modifier.weight(1f))
                Box(Modifier.clip(RoundedCornerShape(16.dp)).background(Color.White.copy(alpha = .16f)).padding(horizontal = 13.dp, vertical = 8.dp)) {
                    Text("00:${seconds.toString().padStart(2, '0')}", color = Color.White, fontWeight = FontWeight.Black, fontSize = 16.sp, fontFamily = AppFont)
                }
            }
        }

        GlassCard(padding = 0, borderColor = Color(0xFFE5EAE4)) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                StopLine("COLETA", ride.pickup, Lime)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    MiniStat("Distância até coleta", ride.distance, Modifier.weight(1f))
                    MiniStat("Tempo estimado", ride.duration, Modifier.weight(1f))
                }
            }
            Divider(color = Color(0xFFE5EAE4))
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Valor da corrida", color = Muted, fontSize = 13.sp, fontWeight = FontWeight.Bold, fontFamily = AppFont)
                        Text(ride.value, color = Lime, fontSize = 36.sp, fontWeight = FontWeight.Black, fontFamily = AppFont)
                    }
                    StatusPill("Oferta", true)
                }
                RealDeliveryMap(
                    title = "Preview da rota",
                    subtitle = "${ride.distance} • ${ride.duration}",
                    pickupAddress = ride.pickup,
                    dropoffAddress = ride.dropoff.ifBlank { ride.neighborhood },
                    pickupLat = ride.pickupLat,
                    pickupLng = ride.pickupLng,
                    dropoffLat = ride.dropoffLat,
                    dropoffLng = ride.dropoffLng
                )
                StopLine("ENTREGA", ride.dropoff.ifBlank { ride.neighborhood.ifBlank { "Bairro da entrega" } }, Purple2)
                Text("O endereço completo do cliente aparece conforme a regra operacional da coleta.", color = Muted, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, fontFamily = AppFont)
            }
        }

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Muito longe", "Ocupado", "Outro").forEach { reason ->
                TinyChip(reason, rejectReason == reason, Modifier.weight(1f)) { rejectReason = if (rejectReason == reason) "" else reason }
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(onClick = { onReject(ride, rejectReason) }, modifier = Modifier.weight(1f).height(58.dp), shape = RoundedCornerShape(20.dp)) {
                Text("Recusar", color = Danger, fontWeight = FontWeight.Black, fontFamily = AppFont)
            }
            Button(
                onClick = { onAccept(ride) },
                modifier = Modifier.weight(1.4f).height(58.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Lime, contentColor = Color.White)
            ) { Text("Aceitar", fontWeight = FontWeight.Black, fontFamily = AppFont) }
        }
    }
}

@Composable
private fun ActiveRideCard(ride: DriverRide, onOpenNavigator: (pickup: String, dropoff: String) -> Unit, onUpdateRide: (DriverRide, String) -> Unit) {
    val title = when (ride.status) {
        "accepted", "ACEITA", "A_CAMINHO_LOJA" -> "Indo para coleta"
        "pickup", "COLETANDO", "NA_COLETA" -> "Na coleta"
        "delivering", "EM_ROTA", "SAIU_ENTREGA" -> "Indo para entrega"
        else -> "Corrida em andamento"
    }
    val nextLabel = when (ride.status) {
        "accepted", "ACEITA", "A_CAMINHO_LOJA" -> "Cheguei na coleta"
        "pickup", "COLETANDO", "NA_COLETA" -> "Pedido retirado"
        "delivering", "EM_ROTA", "SAIU_ENTREGA" -> "Finalizar entrega"
        else -> "Atualizar etapa"
    }
    val nextStatus = when (ride.status) {
        "accepted", "ACEITA", "A_CAMINHO_LOJA" -> "pickup"
        "pickup", "COLETANDO", "NA_COLETA" -> "delivering"
        "delivering", "EM_ROTA", "SAIU_ENTREGA" -> "finished"
        else -> "pickup"
    }
    val deliveryText = if (ride.status in listOf("delivering", "EM_ROTA", "SAIU_ENTREGA")) {
        ride.dropoff.ifBlank { ride.neighborhood.ifBlank { "Endereço do cliente" } }
    } else {
        ride.neighborhood.ifBlank { "Endereço será liberado após a coleta" }
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        GlassCard(padding = 0, borderColor = Color(0xFFE5EAE4)) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(48.dp).clip(CircleShape).background(Color(0xFFE8F8EC)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Filled.Route, contentDescription = null, tint = Lime, modifier = Modifier.size(25.dp))
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(title, color = Lime, fontSize = 18.sp, fontWeight = FontWeight.Black, fontFamily = AppFont)
                        Text("Pedido #${ride.orderCode.ifBlank { ride.id.takeLast(4).uppercase(Locale.ROOT) }} • ${ride.value}", color = Muted, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = AppFont, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    StatusPill(ride.status.statusLabel(), true)
                }
                RealDeliveryMap(
                    title = title,
                    subtitle = "${ride.distance} • ${ride.duration}",
                    pickupAddress = ride.pickup,
                    dropoffAddress = if (ride.status in listOf("delivering", "EM_ROTA", "SAIU_ENTREGA")) ride.dropoff else ride.pickup,
                    pickupLat = ride.pickupLat,
                    pickupLng = ride.pickupLng,
                    dropoffLat = ride.dropoffLat,
                    dropoffLng = ride.dropoffLng,
                    mode = if (ride.status in listOf("delivering", "EM_ROTA", "SAIU_ENTREGA")) DeliveryMapMode.DRIVER_TO_DROPOFF else DeliveryMapMode.DRIVER_TO_PICKUP
                )
            }
        }

        GlassCard(padding = 16) {
            StopLine("COLETA", ride.pickup, Lime)
            Divider(color = Color(0xFFE5EAE4))
            StopLine("ENTREGA", deliveryText, Purple2)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MiniStat("Distância", ride.distance, Modifier.weight(1f))
                MiniStat("Tempo", ride.duration, Modifier.weight(1f))
            }
        }

        RideFinancialPanel(ride, compact = false)

        OutlinedButton(
            onClick = { onOpenNavigator(ride.pickup, if (ride.status in listOf("delivering", "EM_ROTA", "SAIU_ENTREGA")) ride.dropoff else ride.pickup) },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(20.dp)
        ) {
            Icon(Icons.Filled.Route, contentDescription = null, tint = Lime, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text("Abrir navegação", color = Lime, fontWeight = FontWeight.Black, fontFamily = AppFont)
        }
        PrimaryButton(nextLabel) { onUpdateRide(ride, nextStatus) }
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
                    Text("Financeiro da corrida", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Black, fontFamily = AppFont)
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
    val stateTitle = when {
        activeRide != null -> "Corrida em andamento"
        pendingRide != null && online -> "Oferta recebida"
        online -> "Aguardando corrida"
        else -> "Sem operação ativa"
    }
    val stateMessage = when {
        activeRide != null -> "Esta aba mostra só a corrida atual e as próximas ações."
        pendingRide != null && online -> "Aceite ou recuse antes do tempo expirar."
        online -> "As próximas ofertas da operação aparecerão aqui."
        else -> "Fique disponível na Home para receber corridas."
    }
    val stateColor = when {
        pendingRide != null && online -> Danger
        activeRide != null -> Lime
        online -> LimeDark
        else -> Muted2
    }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        GlassCard(padding = 18) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(50.dp).clip(CircleShape).background(stateColor.copy(alpha = .12f)), contentAlignment = Alignment.Center) {
                    Icon(if (activeRide != null) Icons.Filled.Route else Icons.Filled.TwoWheeler, contentDescription = null, tint = stateColor, modifier = Modifier.size(25.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text("Corridas", color = Ink, fontSize = 28.sp, fontWeight = FontWeight.Black, fontFamily = AppFont)
                    Text(stateTitle, color = stateColor, fontSize = 14.sp, fontWeight = FontWeight.Black, fontFamily = AppFont)
                }
                StatusPill(if (activeRide != null) "Ativa" else if (pendingRide != null && online) "Nova" else if (online) "Livre" else "Off", online || activeRide != null)
            }
            Spacer(Modifier.height(10.dp))
            Text(stateMessage, color = Muted, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, fontFamily = AppFont)
        }

        when {
            activeRide != null -> {
                ActiveRideCard(activeRide, onOpenNavigator, onUpdateRide)
                GlassCard(padding = 14) {
                    Text("Ao finalizar, a corrida sai daqui e aparece no Histórico.", color = Muted, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, fontFamily = AppFont)
                }
            }
            pendingRide != null && online -> IncomingRideCard(pendingRide, onAccept, onReject, onExpire)
            online -> {
                WaitingCard(OperationalStatus(AvailabilityKind.Disponivel, "Disponível", "Disponível para receber pedidos", Lime, Color(0xFF10200A), true))
                RidesEmptyGuide("Você está livre", "Mantenha localização, internet e bateria liberadas para não perder a próxima oferta.")
            }
            else -> {
                OfflineCard(OperationalStatus(AvailabilityKind.Indisponivel, "Indisponível", "Fique disponível para receber corridas", Color(0xFF232129), Ink, true))
                RidesEmptyGuide("Nenhuma corrida em andamento", "A aba Corridas é o atalho para oferta, rota ativa, etapas da entrega e ocorrência.")
            }
        }
    }
}

@Composable
private fun RidesEmptyGuide(title: String, message: String) {
    GlassCard(padding = 18) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(44.dp).clip(CircleShape).background(Color(0xFFEAF7EE)), contentAlignment = Alignment.Center) {
                Icon(Icons.Filled.ReceiptLong, contentDescription = null, tint = Lime, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(title, color = Ink, fontSize = 18.sp, fontWeight = FontWeight.Black, fontFamily = AppFont)
                Text(message, color = Muted, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, fontFamily = AppFont)
            }
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
    var filter by remember { mutableStateOf("Todas") }
    var expandedId by remember { mutableStateOf<String?>(null) }
    val ordered = remember(history) { history.sortedByDescending { it.createdAtMillis } }
    val finalizadas = ordered.count { it.historyKind() == "Finalizada" }
    val recusadas = ordered.count { it.historyKind() == "Recusada" }
    val expiradas = ordered.count { it.historyKind() == "Expirada" }
    val filtered = when (filter) {
        "Finalizadas" -> ordered.filter { it.historyKind() == "Finalizada" }
        "Recusadas" -> ordered.filter { it.historyKind() == "Recusada" }
        "Expiradas" -> ordered.filter { it.historyKind() == "Expirada" }
        else -> ordered
    }

    Column(
        modifier = if (embedded) Modifier.fillMaxWidth() else Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        if (!embedded) {
            HistoryHero(total = ordered.size, finalizadas = finalizadas, recusadas = recusadas, expiradas = expiradas)
        } else {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Últimas corridas", color = Ink, fontSize = 21.sp, fontWeight = FontWeight.Black, fontFamily = AppFont)
                    Text("Resumo recente para conferência rápida.", color = Muted, fontSize = 13.sp, fontFamily = AppFont)
                }
                Icon(Icons.Filled.History, contentDescription = null, tint = Lime)
            }
        }

        HistoryFilters(selected = filter, onSelected = { filter = it })

        if (filtered.isEmpty()) {
            GlassCard(padding = 22) {
                EmptyState(
                    title = if (history.isEmpty()) "Nenhuma corrida ainda" else "Nada em ${filter.lowercase(Locale.ROOT)}",
                    subtitle = if (history.isEmpty()) "Aceitas, recusadas, expiradas e finalizadas aparecerão aqui." else "Troque o filtro para ver outros registros."
                )
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                filtered.take(if (embedded) 5 else 80).forEach { item ->
                    HistoryRideCard(
                        item = item,
                        expanded = expandedId == item.id,
                        onClick = { expandedId = if (expandedId == item.id) null else item.id }
                    )
                }
            }
        }
    }
}

@Composable
private fun HistoryHero(total: Int, finalizadas: Int, recusadas: Int, expiradas: Int) {
    GlassCard(padding = 18) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Histórico", color = Ink, fontSize = 30.sp, fontWeight = FontWeight.Black, fontFamily = AppFont)
                Text("Uma corrida por card. Toque para ver detalhes.", color = Muted, fontSize = 13.sp, fontFamily = AppFont)
            }
            Box(Modifier.size(46.dp).clip(CircleShape).background(Color(0xFFEAF7EE)), contentAlignment = Alignment.Center) {
                Icon(Icons.Filled.FilterList, contentDescription = null, tint = Lime)
            }
        }
        Spacer(Modifier.height(16.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            HistoryMetric("Total", total.toString(), Lime, Modifier.weight(1f))
            HistoryMetric("Finalizadas", finalizadas.toString(), LimeDark, Modifier.weight(1f))
            HistoryMetric("Ocorrências", (recusadas + expiradas).toString(), Warning, Modifier.weight(1f))
        }
    }
}

@Composable
private fun HistoryMetric(label: String, value: String, color: Color, modifier: Modifier) {
    Box(
        modifier
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0xFFF6F8F3))
            .border(1.dp, Color(0xFFE6EBE2), RoundedCornerShape(18.dp))
            .padding(12.dp)
    ) {
        Column {
            Text(label, color = Muted2, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = AppFont, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(value, color = color, fontSize = 22.sp, fontWeight = FontWeight.Black, fontFamily = AppFont)
        }
    }
}

@Composable
private fun HistoryFilters(selected: String, onSelected: (String) -> Unit) {
    val options = listOf("Todas", "Finalizadas", "Recusadas", "Expiradas")
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEach { option ->
            TinyChip(option, selected == option, Modifier.weight(1f)) { onSelected(option) }
        }
    }
}

@Composable
private fun HistoryRideCard(item: DriverHistory, expanded: Boolean, onClick: () -> Unit) {
    val kind = item.historyKind()
    val accent = item.historyAccent()
    val bg = when (kind) {
        "Recusada" -> Color(0xFFFFF1F2)
        "Expirada" -> Color(0xFFF3F4F6)
        "Ocorrência" -> Color(0xFFFFF7E8)
        else -> Color(0xFFEAF8EE)
    }
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(26.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFFE5EAE4), RoundedCornerShape(26.dp))
            .clickable { onClick() }
    ) {
        Column(Modifier.padding(15.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(50.dp).clip(CircleShape).background(bg), contentAlignment = Alignment.Center) {
                    Icon(item.historyIcon(), contentDescription = null, tint = accent, modifier = Modifier.size(24.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("#${item.rideId}", color = Ink, fontWeight = FontWeight.Black, fontSize = 18.sp, fontFamily = AppFont, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Spacer(Modifier.width(8.dp))
                        StatusPill(kind, kind == "Finalizada" || kind == "Aceita", Modifier)
                    }
                    Text(item.createdLabel.ifBlank { "Agora" }, color = Muted2, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, fontFamily = AppFont)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(item.value.ifBlank { "—" }, color = if (kind == "Recusada" || kind == "Expirada") Muted2 else LimeDark, fontWeight = FontWeight.Black, fontSize = 17.sp, fontFamily = AppFont)
                    Icon(Icons.Filled.KeyboardArrowRight, contentDescription = null, tint = Muted2, modifier = Modifier.size(22.dp))
                }
            }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val routeText = item.routeSummary()
                if (routeText.isNotBlank()) {
                    HistoryInlineInfo(Icons.Filled.Route, routeText, Modifier.weight(1f))
                }
                val timeText = listOf(item.distance, item.duration).filter { it.isNotBlank() }.joinToString(" • ")
                if (timeText.isNotBlank()) {
                    HistoryInlineInfo(Icons.Filled.Schedule, timeText, Modifier.weight(1f))
                }
            }

            if (item.reason.isNotBlank()) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (kind == "Finalizada") Color(0xFFF5F7F2) else Color(0xFFFFF7E8))
                        .padding(12.dp)
                ) {
                    Text(item.reason.take(95), color = Muted, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, fontFamily = AppFont, maxLines = 2, overflow = TextOverflow.Ellipsis)
                }
            }

            if (expanded) {
                Divider(color = Color(0xFFE9EEE6))
                HistoryDetailLine("Coleta", item.pickup.ifBlank { "Rodrigues Açaí e Cia" }, Lime)
                HistoryDetailLine("Entrega", item.dropoff.ifBlank { item.neighborhood.ifBlank { "Endereço não informado" } }, Purple2)
                if (item.paymentMethod.isNotBlank()) HistoryDetailLine("Pagamento", item.paymentMethod, Blue)
                Text("Registro operacional salvo no histórico. A linha do tempo completa deve ficar no detalhe da corrida quando disponível.", color = Muted2, fontSize = 11.sp, fontFamily = AppFont)
            }
        }
    }
}

@Composable
private fun HistoryInlineInfo(icon: ImageVector, text: String, modifier: Modifier) {
    Row(
        modifier
            .clip(RoundedCornerShape(15.dp))
            .background(Color(0xFFF6F8F3))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = Lime, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(6.dp))
        Text(text, color = Muted, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = AppFont, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun HistoryDetailLine(label: String, value: String, color: Color) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        Box(Modifier.size(28.dp).clip(CircleShape).background(color.copy(alpha = .12f)), contentAlignment = Alignment.Center) {
            Icon(Icons.Filled.Circle, contentDescription = null, tint = color, modifier = Modifier.size(9.dp))
        }
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(label.uppercase(Locale.ROOT), color = color, fontSize = 10.sp, fontWeight = FontWeight.Black, fontFamily = AppFont)
            Text(value, color = Ink, fontSize = 13.sp, fontWeight = FontWeight.Bold, fontFamily = AppFont, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun AccountContent(
    profile: DriverProfile,
    online: Boolean,
    hideValues: Boolean,
    themeMode: String,
    onToggleValues: () -> Unit,
    onThemeChanged: (String) -> Unit,
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
            SectionTitle("Preferências", "Tema e privacidade dos valores.")
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ModeButton("Escuro", themeMode == AppSettings.THEME_DARK, Modifier.weight(1f)) { onThemeChanged(AppSettings.THEME_DARK) }
                ModeButton("Claro", themeMode == AppSettings.THEME_LIGHT, Modifier.weight(1f)) { onThemeChanged(AppSettings.THEME_LIGHT) }
            }
            Spacer(Modifier.height(10.dp))
            Button(
                onClick = onToggleValues,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF232129), contentColor = Ink)
            ) {
                Icon(if (hideValues) Icons.Filled.VisibilityOff else Icons.Filled.Visibility, contentDescription = null, tint = Muted, modifier = Modifier.size(19.dp))
                Spacer(Modifier.width(8.dp))
                Text(if (hideValues) "Mostrar valores" else "Ocultar valores", fontWeight = FontWeight.Black)
            }
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
            Text("Versão 6.0.0 • UI operacional", color = Muted2, fontSize = 12.sp)
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
private fun GlassCard(padding: Int = 16, borderColor: Color = Color(0xFFE4E8EF), content: @Composable ColumnScope.() -> Unit) {
    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = PanelSoft),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
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
        colors = ButtonDefaults.buttonColors(containerColor = Lime, contentColor = Color.White)
    ) {
        if (loading) CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp, color = Color.White)
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
            containerColor = if (selected) Lime else Color(0xFFF2F5EE),
            contentColor = if (selected) Color.White else Muted
        )
    ) { Text(text, fontWeight = FontWeight.Black, fontSize = 13.sp, fontFamily = AppFont) }
}

@Composable
private fun TinyChip(text: String, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = modifier.height(38.dp),
        shape = RoundedCornerShape(999.dp),
        colors = ButtonDefaults.buttonColors(containerColor = if (selected) Lime else Color(0xFFF2F5EE), contentColor = if (selected) Color.White else Muted)
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
            .background(if (positive) Color(0xFFE9F8ED) else Color(0xFFFFEEF2))
            .border(1.dp, if (positive) Lime.copy(alpha = .20f) else Danger.copy(alpha = .20f), RoundedCornerShape(999.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = if (positive) LimeDark else Danger, fontSize = 11.sp, fontWeight = FontWeight.Black, maxLines = 1, overflow = TextOverflow.Ellipsis, fontFamily = AppFont)
    }
}

@Composable
private fun MiniStat(label: String, value: String, modifier: Modifier) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(20.dp),
        modifier = modifier.border(1.dp, Color(0xFFE5EAF0), RoundedCornerShape(20.dp))
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(label, color = Muted2, fontSize = 12.sp, fontFamily = AppFont)
            Text(value, color = Ink, fontSize = 17.sp, fontWeight = FontWeight.Black, maxLines = 1, overflow = TextOverflow.Ellipsis, fontFamily = AppFont)
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

private fun DriverHistory.historyKind(): String = when {
    action.contains("REJEIT", true) || action.contains("RECUS", true) || action.contains("CANCEL", true) || action.equals("OUTRO", true) -> "Recusada"
    action.contains("EXPIR", true) || action.contains("TIMEOUT", true) -> "Expirada"
    action.contains("OCOR", true) || action.contains("PROBLEM", true) || action.contains("PROBLEMA", true) -> "Ocorrência"
    action.contains("CONCL", true) || action.contains("ENTREG", true) || action.contains("FINALIZ", true) || action.equals("finished", true) -> "Finalizada"
    action.contains("ACEIT", true) || action.equals("accepted", true) || action.contains("COLETA", true) || action.contains("ROTA", true) -> "Aceita"
    else -> action.statusLabel()
}

private fun DriverHistory.historyAccent(): Color = when (historyKind()) {
    "Finalizada" -> Lime
    "Aceita" -> LimeDark
    "Recusada" -> Danger
    "Expirada" -> Muted2
    "Ocorrência" -> Warning
    else -> Muted
}

private fun DriverHistory.historyIcon(): ImageVector = when (historyKind()) {
    "Finalizada" -> Icons.Filled.CheckCircle
    "Aceita" -> Icons.Filled.TwoWheeler
    "Recusada" -> Icons.Filled.Cancel
    "Expirada" -> Icons.Filled.Schedule
    "Ocorrência" -> Icons.Filled.ErrorOutline
    else -> Icons.Filled.ReceiptLong
}

private fun DriverHistory.routeSummary(): String {
    val to = neighborhood.ifBlank { dropoff }
    return when {
        pickup.isNotBlank() && to.isNotBlank() -> "${pickup.take(18)} → ${to.take(18)}"
        to.isNotBlank() -> to
        pickup.isNotBlank() -> pickup
        else -> ""
    }
}

private fun String.statusLabel(): String = when {
    equals("accepted", true) || equals("ACEITA", true) || equals("ACEITO", true) || equals("A_CAMINHO_LOJA", true) -> "Aceita"
    equals("pickup", true) || equals("COLETANDO", true) || equals("EM_COLETA", true) -> "Na coleta"
    equals("delivering", true) || equals("EM_ROTA", true) || equals("SAIU_ENTREGA", true) || equals("A_CAMINHO_CLIENTE", true) -> "Em rota"
    equals("finished", true) -> "Finalizada"
    contains("REJEIT", true) -> "Recusada"
    contains("EXPIR", true) -> "Expirada"
    contains("CONCL", true) || contains("ENTREG", true) || contains("FINALIZ", true) -> "Finalizada"
    else -> replace('_', ' ').lowercase().replaceFirstChar { it.uppercase() }
}

private fun String.shortName(): String {
    val parts = trim().split(" ").filter { it.isNotBlank() }
    return when {
        parts.isEmpty() -> "Entregador"
        parts.size == 1 -> parts.first()
        else -> "${parts.first()} ${parts.last().take(1)}."
    }
}
