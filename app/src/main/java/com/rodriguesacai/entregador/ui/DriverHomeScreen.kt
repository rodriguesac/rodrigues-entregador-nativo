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
import androidx.compose.foundation.Image
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
import androidx.compose.ui.res.painterResource
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
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.CloudOff
import coil.compose.AsyncImage
import com.rodriguesacai.entregador.AppSettings
import com.rodriguesacai.entregador.R
import com.rodriguesacai.entregador.PermissionStatusReader
import com.rodriguesacai.entregador.PermissionStatus
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
private val BgTop = Color(0xFFF7FAF6)
private val BgBottom = Color(0xFFEFF4EF)
private val Panel = Color.White
private val PanelSoft = Color.White
private val Purple = Color(0xFF008A2E)
private val Purple2 = Color(0xFFFF7A00)
private val Lime = Color(0xFF008A2E)
private val LimeDark = Color(0xFF006B2A)
private val Ink = Color(0xFF111318)
private val Muted = Color(0xFF5D6670)
private val Muted2 = Color(0xFF96A0AA)
private val Danger = Color(0xFFE7192B)
private val Warning = Color(0xFFFF9900)
private val Blue = Color(0xFF1677FF)
private val BorderSoft = Color(0xFFE1E8DF)
private val FillSoft = Color(0xFFF8FBF6)

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
                navItem(AppTab.Ganhos, tab, "Carteira", Icons.Filled.AccountBalanceWallet) { tab = it }
                navItem(AppTab.Historico, tab, "Histórico", Icons.Filled.History) { tab = it }
                navItem(AppTab.Conta, tab, "Mais", Icons.Filled.Person) { tab = it }
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
                            normalized.contains("histor") || normalized.contains("aviso") -> AppTab.Historico
                            normalized.contains("conta") || normalized.contains("perfil") || normalized.contains("pix") || normalized.contains("banco") || normalized.contains("suporte") -> AppTab.Conta
                            normalized.contains("rota") || normalized.contains("mapa") || normalized.contains("corrida") -> AppTab.Corridas
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
            .background(BgTop)
            .padding(horizontal = 22.dp, vertical = 18.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Center
        ) {
            RodriguesLogoBlock(compact = false)
            Spacer(Modifier.height(22.dp))

            if (mode == "login") {
                PremiumScreenCard {
                    Text("Bem-vindo(a)!", color = Ink, fontSize = 28.sp, fontWeight = FontWeight.Black, fontFamily = AppFont, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(8.dp))
                    Text("Acesse sua conta para continuar fazendo entregas.", color = Muted, fontSize = 14.sp, lineHeight = 20.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth(), fontFamily = AppFont)
                    Spacer(Modifier.height(24.dp))
                    AppField(login, { login = it }, "CPF ou telefone", KeyboardType.Number)
                    Spacer(Modifier.height(12.dp))
                    AppField(
                        value = password,
                        onValueChange = { password = it },
                        label = "Senha",
                        keyboardType = KeyboardType.Password,
                        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        trailing = if (showPassword) "ocultar" else "ver",
                        onTrailing = { showPassword = !showPassword }
                    )
                    Spacer(Modifier.height(18.dp))
                    PrimaryButton(text = "Entrar", enabled = !loading, loading = loading) { onLogin(login, password) { loading = it } }
                    Spacer(Modifier.height(12.dp))
                    OutlinedActionButton("Solicitar cadastro", Icons.Filled.Person) { mode = "cadastro" }
                    Spacer(Modifier.height(12.dp))
                    TextButton(onClick = { mode = "senha" }, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                        Text("Esqueci minha senha", color = LimeDark, fontWeight = FontWeight.Bold, fontFamily = AppFont)
                    }
                    Spacer(Modifier.height(12.dp))
                    LoginHeroIllustration()
                }
            } else if (mode == "cadastro") {
                PremiumScreenCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        HeaderBackCircle { mode = "login" }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Solicitar cadastro", color = Ink, fontSize = 22.sp, fontWeight = FontWeight.Black, fontFamily = AppFont)
                            Text("Dados, veículo e recebimento", color = Muted, fontSize = 13.sp, fontFamily = AppFont)
                        }
                    }
                    Spacer(Modifier.height(18.dp))
                    RegistrationSteps(current = 1)
                    Spacer(Modifier.height(18.dp))
                    AppField(name, { name = it }, "Nome completo")
                    Spacer(Modifier.height(10.dp))
                    AppField(cpf, { cpf = it }, "CPF", KeyboardType.Number)
                    Spacer(Modifier.height(10.dp))
                    AppField(phone, { phone = it }, "Telefone", KeyboardType.Phone)
                    Spacer(Modifier.height(10.dp))
                    AppField(vehicle, { vehicle = it }, "Veículo")
                    Spacer(Modifier.height(10.dp))
                    AppField(plate, { plate = it }, "Placa")
                    Spacer(Modifier.height(10.dp))
                    AppField(pix, { pix = it }, "Chave Pix")
                    Spacer(Modifier.height(10.dp))
                    AppField(bank, { bank = it }, "Banco")
                    Spacer(Modifier.height(10.dp))
                    AppField(newPassword, { newPassword = it }, "Criar senha", KeyboardType.Password, PasswordVisualTransformation())
                    Spacer(Modifier.height(18.dp))
                    PrimaryButton(text = "Enviar cadastro", enabled = !loading, loading = loading) {
                        onRegister(
                            DriverRegistrationRequest(name, cpf, phone, newPassword, vehicle, plate, pix, bank)
                        ) { loading = it }
                    }
                    TextButton(onClick = { mode = "login" }, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                        Text("Já tenho conta", color = LimeDark, fontWeight = FontWeight.Bold, fontFamily = AppFont)
                    }
                }
            } else {
                PremiumScreenCard {
                    RodriguesLogoBlock(compact = true)
                    Spacer(Modifier.height(22.dp))
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Box(Modifier.size(120.dp).clip(RoundedCornerShape(34.dp)).background(Color(0xFFEAF8EE)), contentAlignment = Alignment.Center) {
                            Icon(Icons.Filled.Shield, contentDescription = null, tint = Lime, modifier = Modifier.size(72.dp))
                        }
                    }
                    Spacer(Modifier.height(18.dp))
                    Text("Criar sua senha", color = Ink, fontSize = 25.sp, fontWeight = FontWeight.Black, fontFamily = AppFont, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                    Text("Informe CPF/telefone e uma nova senha. O gestor poderá validar a alteração.", color = Muted, fontSize = 13.sp, lineHeight = 19.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth(), fontFamily = AppFont)
                    Spacer(Modifier.height(18.dp))
                    AppField(login, { login = it }, "CPF ou telefone", KeyboardType.Number)
                    Spacer(Modifier.height(10.dp))
                    AppField(newPassword, { newPassword = it }, "Nova senha", KeyboardType.Password, PasswordVisualTransformation())
                    Spacer(Modifier.height(16.dp))
                    PrimaryButton("Salvar e continuar") { mode = "login" }
                    Spacer(Modifier.height(10.dp))
                    OutlinedActionButton("Voltar ao login", Icons.Filled.KeyboardArrowRight) { mode = "login" }
                }
            }

            if (error.isNotBlank()) {
                Spacer(Modifier.height(12.dp))
                StatusMessage(error, true)
            }
            if (notice.isNotBlank()) {
                Spacer(Modifier.height(12.dp))
                StatusMessage(notice, false)
            }
        }
    }
}

@Composable
private fun RodriguesLogoBlock(compact: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.Center) {
            Text("Rodrigues", color = Lime, fontSize = if (compact) 28.sp else 38.sp, fontWeight = FontWeight.Black, fontFamily = AppFont)
            Spacer(Modifier.width(6.dp))
            Text("entregas", color = Ink, fontSize = if (compact) 18.sp else 24.sp, fontWeight = FontWeight.Black, fontFamily = AppFont)
        }
        if (!compact) Text("Operação nativa do entregador", color = Muted, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = AppFont)
    }
}

@Composable
private fun PremiumScreenCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth().border(1.dp, BorderSoft, RoundedCornerShape(28.dp))
    ) {
        Column(Modifier.padding(22.dp), content = content)
    }
}

@Composable
private fun LoginHeroIllustration() {
    Box(
        Modifier
            .fillMaxWidth()
            .height(150.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(Brush.verticalGradient(listOf(Color(0xFFF3FAF0), Color(0xFFE4F1E3))))
    ) {
        Canvas(Modifier.matchParentSize()) {
            drawCircle(Color(0xFFB9E8C1).copy(alpha = .35f), radius = size.minDimension * .55f, center = Offset(size.width * .82f, size.height * .62f))
            drawLine(Color.White, Offset(0f, size.height * .78f), Offset(size.width, size.height * .52f), strokeWidth = 16f)
        }
        Box(Modifier.align(Alignment.BottomCenter).padding(bottom = 18.dp).size(80.dp, 54.dp).clip(RoundedCornerShape(18.dp)).background(Lime), contentAlignment = Alignment.Center) {
            Text("R", color = Color.White, fontWeight = FontWeight.Black, fontSize = 26.sp, fontFamily = AppFont)
        }
        Icon(Icons.Filled.TwoWheeler, contentDescription = null, tint = LimeDark, modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 18.dp).size(120.dp))
    }
}

@Composable
private fun RegistrationSteps(current: Int) {
    val steps = listOf("Dados", "Documentos", "Confirmação", "Conclusão")
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        steps.forEachIndexed { index, label ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(Modifier.size(30.dp).clip(CircleShape).background(if (index + 1 <= current) Lime else Color(0xFFF1F3F1)).border(1.dp, if (index + 1 <= current) Lime else BorderSoft, CircleShape), contentAlignment = Alignment.Center) {
                    Text("${index + 1}", color = if (index + 1 <= current) Color.White else Muted, fontWeight = FontWeight.Black, fontSize = 12.sp, fontFamily = AppFont)
                }
                Spacer(Modifier.height(5.dp))
                Text(label, color = if (index + 1 == current) LimeDark else Muted2, fontSize = 9.sp, fontWeight = FontWeight.Bold, fontFamily = AppFont, maxLines = 1)
            }
        }
    }
}

@Composable
private fun HeaderBackCircle(onClick: () -> Unit) {
    Box(Modifier.size(40.dp).clip(CircleShape).background(Color(0xFFF4F7F3)).clickable { onClick() }, contentAlignment = Alignment.Center) {
        Text("‹", color = Ink, fontSize = 28.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun OutlinedActionButton(text: String, icon: ImageVector, onClick: () -> Unit) {
    OutlinedButton(onClick = onClick, modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(18.dp)) {
        Icon(icon, contentDescription = null, tint = Lime, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(8.dp))
        Text(text, color = LimeDark, fontWeight = FontWeight.Black, fontFamily = AppFont)
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
            .padding(horizontal = 16.dp, vertical = 14.dp),
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
            operational.kind == AvailabilityKind.Restricao -> RestrictionCard(operational)
            operational.kind == AvailabilityKind.Indisponivel -> OfflineCard(operational)
        }
        Spacer(Modifier.height(10.dp))
    }
}

@Composable
private fun LocalOrRemoteBannerImage(imageUrl: String, contentDescription: String, modifier: Modifier = Modifier) {
    val localRes = imageUrl.localBannerResource()
    if (localRes != null) {
        Image(
            painter = painterResource(localRes),
            contentDescription = contentDescription,
            contentScale = ContentScale.Crop,
            modifier = modifier
        )
    } else {
        AsyncImage(
            model = imageUrl,
            contentDescription = contentDescription,
            contentScale = ContentScale.Crop,
            modifier = modifier
        )
    }
}

private fun String.localBannerResource(): Int? {
    val key = trim().lowercase(Locale.ROOT)
    return when (key) {
        "asset://banner_pico", "asset://pico", "local://banner_pico" -> R.drawable.banner_pico
        "asset://banner_indique_ganhe", "asset://indique", "local://banner_indique_ganhe" -> R.drawable.banner_indique_ganhe
        "asset://banner_seguranca", "asset://seguranca", "local://banner_seguranca" -> R.drawable.banner_seguranca
        else -> null
    }
}

private fun defaultHomeBanners(status: OperationalStatus): List<AppCarouselBanner> {
    val first = when (status.kind) {
        AvailabilityKind.Disponivel -> AppCarouselBanner(
            id = "local-novidades-operacao",
            title = "",
            badge = "",
            description = "",
            buttonText = "",
            imageUrl = "asset://banner_pico",
            displayMode = "image_only",
            order = 1,
            active = true,
            actionType = "internal",
            actionTarget = "avisos"
        )
        AvailabilityKind.Restricao -> AppCarouselBanner(
            id = "local-restricao",
            title = "Resolva a restrição",
            badge = "ATENÇÃO",
            description = status.message.ifBlank { "Confira localização, internet, notificações e bateria para voltar a receber corridas." },
            buttonText = "Ver ajustes",
            order = 1,
            active = true,
            actionType = "internal",
            actionTarget = "conta"
        )
        AvailabilityKind.EmEntrega -> AppCarouselBanner(
            id = "local-rota",
            title = "Corrida em andamento",
            badge = "ROTA",
            description = "Siga as etapas da coleta e entrega. A operação atual fica sempre na aba Corridas.",
            buttonText = "Abrir corrida",
            order = 1,
            active = true,
            actionType = "internal",
            actionTarget = "corridas"
        )
        else -> AppCarouselBanner(
            id = "local-offline",
            title = "Fique disponível",
            badge = "INÍCIO",
            description = "Ative o status quando estiver pronto. Os avisos e campanhas aparecem aqui.",
            buttonText = "Conectar",
            order = 1,
            active = true
        )
    }
    return listOf(
        first,
        AppCarouselBanner(
            id = "local-indique",
            title = "",
            badge = "",
            description = "",
            buttonText = "",
            imageUrl = "asset://banner_indique_ganhe",
            displayMode = "image_only",
            order = 2,
            active = true,
            actionType = "internal",
            actionTarget = "avisos"
        ),
        AppCarouselBanner(
            id = "local-seguranca",
            title = "",
            badge = "",
            description = "",
            buttonText = "",
            imageUrl = "asset://banner_seguranca",
            displayMode = "image_only",
            order = 3,
            active = true,
            actionType = "internal",
            actionTarget = "suporte"
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
            .clip(RoundedCornerShape(22.dp))
            .background(Brush.linearGradient(listOf(Color(0xFF007A28), Color(0xFF009B38), Color(0xFF005C22))))
            .border(1.dp, Color(0xFFDCE9DA), RoundedCornerShape(22.dp))
            .then(if (clickable) Modifier.clickable { onAction() } else Modifier)
    ) {
        if (hasImage && !textOnly) {
            LocalOrRemoteBannerImage(
                imageUrl = banner.imageUrl,
                contentDescription = banner.title.ifBlank { "Banner do app" },
                modifier = Modifier.matchParentSize()
            )
            if (!imageOnly) {
                Box(Modifier.matchParentSize().background(Brush.horizontalGradient(listOf(Color(0xD9005C22), Color(0xA6005C22), Color.Transparent))))
            }
        } else {
            Canvas(Modifier.matchParentSize()) {
                drawCircle(color = Color.White.copy(alpha = .14f), radius = size.minDimension * .56f, center = Offset(size.width * .83f, size.height * .22f))
                drawCircle(color = Color(0xFF002D11).copy(alpha = .25f), radius = size.minDimension * .30f, center = Offset(size.width * .92f, size.height * .90f))
                drawLine(Color.White.copy(alpha = .20f), Offset(size.width * .53f, size.height * .18f), Offset(size.width * .88f, size.height * .80f), strokeWidth = 8f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(18f, 14f), 0f))
            }
            Box(Modifier.align(Alignment.CenterEnd).padding(end = 18.dp).size(86.dp).clip(RoundedCornerShape(28.dp)).background(Color.White.copy(alpha = .14f)), contentAlignment = Alignment.Center) {
                Icon(Icons.Filled.Bolt, contentDescription = null, tint = Color.White.copy(alpha = .90f), modifier = Modifier.size(44.dp))
            }
        }

        if (!imageOnly) {
            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(Modifier.fillMaxWidth(.68f)) {
                    if (showBadge) {
                        Box(Modifier.clip(RoundedCornerShape(999.dp)).background(Color.White.copy(alpha = .18f)).padding(horizontal = 10.dp, vertical = 5.dp)) {
                            Text(
                                banner.badge.ifBlank { "AVISO" }.uppercase(Locale.ROOT),
                                color = Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.0.sp,
                                fontFamily = AppFont,
                                maxLines = 1
                            )
                        }
                        Spacer(Modifier.height(7.dp))
                    }
                    if (showTitle) {
                        Text(
                            banner.title,
                            color = Color.White,
                            fontSize = 21.sp,
                            lineHeight = 22.sp,
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
                            color = Color.White.copy(alpha = .92f),
                            fontSize = 11.sp,
                            lineHeight = 14.sp,
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
                        modifier = Modifier.height(36.dp),
                        shape = RoundedCornerShape(13.dp),
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
            QuickActionTile("Mapa", "Ver região", Icons.Filled.Map, Modifier.weight(1f)) { onInternalAction("mapa") }
            QuickActionTile("Suporte", "Fale conosco", Icons.Filled.SupportAgent, Modifier.weight(1f)) { onInternalAction("conta") }
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
                    fontSize = 21.sp,
                    fontWeight = FontWeight.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontFamily = AppFont
                )
                Text(
                    when (operational.kind) {
                        AvailabilityKind.Disponivel -> "Pronto para receber corridas"
                        AvailabilityKind.Restricao -> "Ajuste pendências para ficar online"
                        AvailabilityKind.EmEntrega -> "Corrida em andamento"
                        else -> "Você está offline"
                    },
                    color = Muted,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontFamily = AppFont
                )
            }
            HeaderRoundIcon(Icons.Filled.Notifications)
            Spacer(Modifier.width(8.dp))
            HeaderRoundIcon(Icons.Filled.ChatBubbleOutline)
        }

        Button(
            onClick = onStatusClick,
            modifier = Modifier.fillMaxWidth().height(58.dp),
            shape = RoundedCornerShape(22.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = operational.buttonColor,
                contentColor = operational.textColor
            ),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = if (operational.kind == AvailabilityKind.Indisponivel) .42f else .16f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        when (operational.kind) {
                            AvailabilityKind.Restricao -> Icons.Filled.ErrorOutline
                            AvailabilityKind.EmEntrega -> Icons.Filled.Route
                            else -> Icons.Filled.Circle
                        },
                        contentDescription = null,
                        tint = operational.textColor,
                        modifier = Modifier.size(if (operational.kind == AvailabilityKind.Disponivel) 13.dp else 20.dp)
                    )
                }
                Spacer(Modifier.width(12.dp))
                Text(operational.label, fontSize = 18.sp, fontWeight = FontWeight.Black, fontFamily = AppFont, maxLines = 1, modifier = Modifier.weight(1f))
                Icon(Icons.Filled.KeyboardArrowRight, contentDescription = null, tint = operational.textColor.copy(alpha = .85f), modifier = Modifier.size(22.dp))
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
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(22.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFFE5EAE4), RoundedCornerShape(22.dp))
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1.35f)) {
                Text("Ganhos de hoje", color = Muted, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = AppFont)
                Text(
                    if (hideValues) "R$ •••••" else DriverRepository.formatCurrency(stats.totalToday),
                    color = Ink,
                    fontSize = 25.sp,
                    fontWeight = FontWeight.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontFamily = AppFont
                )
            }
            Divider(Modifier.height(44.dp).width(1.dp), color = Color(0xFFE5EAE4))
            Column(Modifier.weight(.75f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(stats.finishedCount.toString(), color = LimeDark, fontSize = 21.sp, fontWeight = FontWeight.Black, fontFamily = AppFont)
                Text("Corridas", color = Muted, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = AppFont)
            }
            Divider(Modifier.height(44.dp).width(1.dp), color = Color(0xFFE5EAE4))
            Column(Modifier.weight(.75f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("${stats.score}%", color = LimeDark, fontSize = 21.sp, fontWeight = FontWeight.Black, fontFamily = AppFont)
                Text("Finalizadas", color = Muted, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = AppFont)
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
    val filtered = remember(history, filter) {
        history.filter { item ->
            when (filter) {
                "Finalizadas" -> item.historyKind() == "Finalizada"
                "Recusadas" -> item.historyKind() == "Recusada"
                "Expiradas" -> item.historyKind() == "Expirada"
                else -> true
            }
        }
    }

    Column(
        Modifier
            .then(if (embedded) Modifier else Modifier.fillMaxSize().verticalScroll(rememberScrollState()))
            .padding(if (embedded) 0.dp else 16.dp),
        verticalArrangement = Arrangement.spacedBy(13.dp)
    ) {
        if (!embedded) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Histórico", color = Ink, fontSize = 28.sp, fontWeight = FontWeight.Black, fontFamily = AppFont)
                    Text("Finalizadas, recusadas e expiradas em um lugar só.", color = Muted, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, fontFamily = AppFont)
                }
                Box(Modifier.size(44.dp).clip(CircleShape).background(Color.White).border(1.dp, Color(0xFFE2E8E0), CircleShape), contentAlignment = Alignment.Center) {
                    Icon(Icons.Filled.FilterList, contentDescription = null, tint = Lime, modifier = Modifier.size(22.dp))
                }
            }
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
    var page by remember { mutableStateOf("main") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var pix by remember { mutableStateOf(profile.pixKey) }
    var bank by remember { mutableStateOf(profile.bankName) }
    var payoutType by remember { mutableStateOf("Pix") }
    var changeType by remember { mutableStateOf("Telefone") }
    var changeCurrent by remember { mutableStateOf(profile.phone.ifBlank { "Não informado" }) }
    var changeNewValue by remember { mutableStateOf("") }
    var changeReason by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var localError by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var permissionStatus by remember { mutableStateOf(PermissionStatusReader.read(context)) }

    fun sendChangeRequest() {
        localError = ""
        message = ""
        val body = buildString {
            append("Tipo: ").append(changeType).append("\n")
            append("Atual: ").append(changeCurrent.ifBlank { "Não informado" }).append("\n")
            append("Novo: ").append(changeNewValue.ifBlank { "Não informado" }).append("\n")
            append("Motivo: ").append(changeReason.ifBlank { "Sem motivo informado" })
        }
        loading = true
        DriverRepository.requestProfileChange(
            context = context,
            requestText = body,
            onSuccess = {
                loading = false
                changeNewValue = ""
                changeReason = ""
                message = "Solicitação enviada para análise da operação."
            },
            onError = {
                loading = false
                localError = it
            }
        )
    }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        when (page) {
            "main" -> MoreHubScreen(
                profile = profile,
                online = online,
                hideValues = hideValues,
                permissionStatus = permissionStatus,
                onToggleValues = onToggleValues,
                onOpen = { page = it },
                onLogout = onLogout
            )

            "profile" -> ProfileReferenceScreen(
                profile = profile,
                online = online,
                onBack = { page = "main" },
                onRequestChange = {
                    changeType = "Telefone"
                    changeCurrent = profile.phone.ifBlank { "Não informado" }
                    page = "change"
                }
            )

            "payout" -> PixBankReferenceScreen(
                pix = pix,
                bank = bank,
                payoutType = payoutType,
                loading = loading,
                onPix = { pix = it },
                onBank = { bank = it },
                onPayout = { payoutType = it },
                onBack = { page = "main" },
                onSave = {
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
                },
                onRequestChange = {
                    changeType = "Pix"
                    changeCurrent = pix.ifBlank { "Não informado" }
                    page = "change"
                }
            )

            "change" -> ChangeRequestReferenceScreen(
                changeType = changeType,
                currentValue = changeCurrent,
                newValue = changeNewValue,
                reason = changeReason,
                loading = loading,
                onBack = { page = "main" },
                onType = {
                    changeType = it
                    changeCurrent = when (it) {
                        "Telefone" -> profile.phone.ifBlank { "Não informado" }
                        "Pix" -> pix.ifBlank { "Não informado" }
                        "Banco" -> bank.ifBlank { "Não informado" }
                        else -> "Não informado"
                    }
                },
                onNewValue = { changeNewValue = it },
                onReason = { changeReason = it },
                onSubmit = { sendChangeRequest() }
            )

            "notifications" -> NotificationsReferenceScreen(
                onBack = { page = "main" }
            )

            "permissions" -> PermissionsReferenceScreen(
                permissionStatus = permissionStatus,
                onRefresh = { permissionStatus = PermissionStatusReader.read(context) },
                onBack = { page = "main" },
                onOpenNotificationSettings = onOpenNotificationSettings,
                onOpenLocationSettings = onOpenLocationSettings,
                onOpenFullScreenSettings = onOpenFullScreenSettings,
                onOpenBatterySettings = onOpenBatterySettings
            )

            "access" -> AccessReferenceScreen(
                password = password,
                confirmPassword = confirmPassword,
                loading = loading,
                onPassword = { password = it },
                onConfirm = { confirmPassword = it },
                onBack = { page = "main" },
                onSave = {
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
            )

            "settings" -> SettingsReferenceScreen(
                themeMode = themeMode,
                onThemeChanged = onThemeChanged,
                onBack = { page = "main" }
            )

            "support" -> SupportReferenceScreen(onBack = { page = "main" })
        }

        if (localError.isNotBlank()) StatusMessage(localError, true)
        if (message.isNotBlank()) StatusMessage(message, false)
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun MoreHubScreen(
    profile: DriverProfile,
    online: Boolean,
    hideValues: Boolean,
    permissionStatus: PermissionStatus,
    onToggleValues: () -> Unit,
    onOpen: (String) -> Unit,
    onLogout: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        ScreenTopTitle("Mais", "Conta, recebimento e ajustes")
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(28.dp),
            modifier = Modifier.fillMaxWidth().border(1.dp, BorderSoft, RoundedCornerShape(28.dp))
        ) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Avatar(profile.name, profile.photoUrl)
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(profile.name, color = Ink, fontSize = 22.sp, fontWeight = FontWeight.Black, fontFamily = AppFont, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Spacer(Modifier.width(7.dp))
                            Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = Lime, modifier = Modifier.size(18.dp))
                        }
                        Text(if (online) "Entregador ativo" else "Entregador indisponível", color = if (online) Lime else Muted, fontSize = 13.sp, fontWeight = FontWeight.Bold, fontFamily = AppFont)
                    }
                    Icon(Icons.Filled.KeyboardArrowRight, contentDescription = null, tint = Muted2, modifier = Modifier.clickable { onOpen("profile") })
                }
                Divider(color = BorderSoft)
                AccountDataLine(Icons.Filled.Person, "Telefone", profile.phone.ifBlank { "Não informado" })
                AccountDataLine(Icons.Filled.Shield, "Status cadastral", if (profile.approved) "Aprovado" else "Pendente")
                AccountDataLine(Icons.Filled.AccountBalanceWallet, "Recebimento", if (profile.pixKey.isNotBlank()) "Pix cadastrado" else "Cadastrar Pix")
            }
        }

        SectionTitle("Acesso rápido", "Telas do entregador organizadas como no padrão visual.")
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            MoreMenuTile("Perfil", "Dados pessoais", Icons.Filled.Person, Modifier.weight(1f)) { onOpen("profile") }
            MoreMenuTile("Pix/banco", "Recebimento", Icons.Filled.AccountBalanceWallet, Modifier.weight(1f)) { onOpen("payout") }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            MoreMenuTile("Notificações", "Avisos e operação", Icons.Filled.Notifications, Modifier.weight(1f)) { onOpen("notifications") }
            MoreMenuTile("Permissões", "Checklist", Icons.Filled.Shield, Modifier.weight(1f)) { onOpen("permissions") }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            MoreMenuTile("Alteração", "Telefone, Pix ou banco", Icons.Filled.ReceiptLong, Modifier.weight(1f)) { onOpen("change") }
            MoreMenuTile("Suporte", "Ajuda e problemas", Icons.Filled.SupportAgent, Modifier.weight(1f)) { onOpen("support") }
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = if (permissionStatus.ready) Color(0xFFEAF8EE) else Color(0xFFFFF7E8)),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth().border(1.dp, if (permissionStatus.ready) Lime.copy(alpha = .16f) else Warning.copy(alpha = .25f), RoundedCornerShape(24.dp))
        ) {
            Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(48.dp).clip(CircleShape).background(Color.White), contentAlignment = Alignment.Center) {
                    Icon(if (permissionStatus.ready) Icons.Filled.CheckCircle else Icons.Filled.ErrorOutline, contentDescription = null, tint = if (permissionStatus.ready) Lime else Warning, modifier = Modifier.size(24.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(if (permissionStatus.ready) "App pronto para corridas" else "Finalize as permissões", color = Ink, fontSize = 16.sp, fontWeight = FontWeight.Black, fontFamily = AppFont)
                    Text(if (permissionStatus.ready) "Alertas, localização e bateria estão configurados." else "Configure notificações, localização e bateria sem restrição.", color = Muted, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, fontFamily = AppFont)
                }
                Icon(Icons.Filled.KeyboardArrowRight, contentDescription = null, tint = Muted2, modifier = Modifier.clickable { onOpen("permissions") })
            }
        }

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(onClick = onToggleValues, modifier = Modifier.weight(1f).height(52.dp), shape = RoundedCornerShape(18.dp)) {
                Icon(if (hideValues) Icons.Filled.VisibilityOff else Icons.Filled.Visibility, contentDescription = null, tint = Lime, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(if (hideValues) "Mostrar" else "Ocultar", color = Lime, fontWeight = FontWeight.Black, fontFamily = AppFont)
            }
            OutlinedButton(onClick = onLogout, modifier = Modifier.weight(1f).height(52.dp), shape = RoundedCornerShape(18.dp)) {
                Text("Sair da conta", color = Danger, fontWeight = FontWeight.Black, fontFamily = AppFont)
            }
        }
    }
}

@Composable
private fun ProfileReferenceScreen(profile: DriverProfile, online: Boolean, onBack: () -> Unit, onRequestChange: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        ScreenTopTitle("Perfil", "Dados do entregador", onBack)
        GlassCard(padding = 18) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Avatar(profile.name, profile.photoUrl)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(profile.name, color = Ink, fontSize = 22.sp, fontWeight = FontWeight.Black, fontFamily = AppFont, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Spacer(Modifier.width(7.dp))
                        Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = Lime, modifier = Modifier.size(18.dp))
                    }
                    Text(if (online) "Entregador ativo" else "Indisponível no momento", color = if (online) Lime else Muted, fontSize = 13.sp, fontWeight = FontWeight.Bold, fontFamily = AppFont)
                }
            }
            Spacer(Modifier.height(16.dp))
            AccountDataLine(Icons.Filled.Person, "Telefone", profile.phone.ifBlank { "Não informado" })
            AccountDataLine(Icons.Filled.Place, "Cidade", "Definida pela operação")
            AccountDataLine(Icons.Filled.TwoWheeler, "Veículo", "Moto")
            AccountDataLine(Icons.Filled.Shield, "Status cadastral", if (profile.approved) "Aprovado" else "Pendente")
        }
        StatusInfoCard("Alterações sensíveis", "Telefone e e-mail precisam de aprovação do gestor antes de mudar no cadastro.", Lime)
        PrimaryButton("Solicitar alteração") { onRequestChange() }
    }
}

@Composable
private fun PixBankReferenceScreen(
    pix: String,
    bank: String,
    payoutType: String,
    loading: Boolean,
    onPix: (String) -> Unit,
    onBank: (String) -> Unit,
    onPayout: (String) -> Unit,
    onBack: () -> Unit,
    onSave: () -> Unit,
    onRequestChange: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        ScreenTopTitle("Recebimento", "Pix e dados bancários", onBack)
        Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFEAF8EE)), shape = RoundedCornerShape(24.dp), modifier = Modifier.fillMaxWidth().border(1.dp, Lime.copy(alpha = .16f), RoundedCornerShape(24.dp))) {
            Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(48.dp).clip(RoundedCornerShape(18.dp)).background(Lime.copy(alpha = .14f)), contentAlignment = Alignment.Center) { Icon(Icons.Filled.AccountBalanceWallet, contentDescription = null, tint = Lime, modifier = Modifier.size(26.dp)) }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(if (pix.isNotBlank()) "Chave Pix ativa" else "Cadastre sua chave Pix", color = LimeDark, fontSize = 16.sp, fontWeight = FontWeight.Black, fontFamily = AppFont)
                    Text("Seus repasses usam estes dados. A conta precisa estar no nome do titular.", color = Muted, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, fontFamily = AppFont)
                }
            }
        }
        GlassCard(padding = 18) {
            Text("Chave Pix", color = Ink, fontSize = 15.sp, fontWeight = FontWeight.Black, fontFamily = AppFont)
            Spacer(Modifier.height(8.dp))
            AppField(pix, onPix, "Digite sua chave Pix")
            Spacer(Modifier.height(12.dp))
            Text("Dados bancários", color = Ink, fontSize = 15.sp, fontWeight = FontWeight.Black, fontFamily = AppFont)
            Spacer(Modifier.height(8.dp))
            AppField(bank, onBank, "Banco")
            Spacer(Modifier.height(10.dp))
            AppField(payoutType, onPayout, "Tipo de repasse")
            Spacer(Modifier.height(14.dp))
            StatusInfoCard("Titularidade obrigatória", "Não aceitamos contas de terceiros para repasse.", Lime)
        }
        PrimaryButton("Salvar dados", enabled = !loading, loading = loading) { onSave() }
        OutlinedButton(onClick = onRequestChange, modifier = Modifier.fillMaxWidth().height(54.dp), shape = RoundedCornerShape(18.dp)) {
            Text("Solicitar alteração", color = Lime, fontWeight = FontWeight.Black, fontFamily = AppFont)
        }
    }
}

@Composable
private fun ChangeRequestReferenceScreen(
    changeType: String,
    currentValue: String,
    newValue: String,
    reason: String,
    loading: Boolean,
    onBack: () -> Unit,
    onType: (String) -> Unit,
    onNewValue: (String) -> Unit,
    onReason: (String) -> Unit,
    onSubmit: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        ScreenTopTitle("Solicitação de alteração", "Envie para análise da operação", onBack)
        GlassCard(padding = 18) {
            Text("Selecione o tipo de alteração", color = Ink, fontSize = 15.sp, fontWeight = FontWeight.Black, fontFamily = AppFont)
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Telefone", "E-mail", "Pix", "Banco").forEach { item ->
                    ModeButton(item, changeType == item, Modifier.weight(1f)) { onType(item) }
                }
            }
            Spacer(Modifier.height(14.dp))
            Text("Valor atual", color = Muted, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = AppFont)
            Spacer(Modifier.height(6.dp))
            Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Color(0xFFF3F6F1)).padding(14.dp)) {
                Text(currentValue, color = Muted, fontSize = 14.sp, fontWeight = FontWeight.Bold, fontFamily = AppFont)
            }
            Spacer(Modifier.height(12.dp))
            AppField(newValue, onNewValue, "Novo valor")
            Spacer(Modifier.height(10.dp))
            AppField(reason, onReason, "Motivo da alteração")
        }
        StatusInfoCard("Análise obrigatória", "Sua solicitação será analisada pelo gestor antes de alterar o cadastro.", Lime)
        PrimaryButton("Enviar solicitação", enabled = !loading, loading = loading) { onSubmit() }
    }
}

@Composable
private fun NotificationsReferenceScreen(onBack: () -> Unit) {
    var filter by remember { mutableStateOf("Todas") }
    val rows = listOf(
        Triple("Nova corrida disponível", "Agora mesmo • toque para abrir", Lime),
        Triple("Seu repasse foi programado", "Hoje • crédito previsto até 12:00", Lime),
        Triple("Atualize seus dados bancários", "Ontem • necessário para próximo repasse", Warning),
        Triple("Cadastro atualizado com sucesso", "Ontem • suas informações estão em dia", Blue),
        Triple("Nenhuma nova mensagem do suporte", "2 dias atrás • tudo certo por aqui", Muted2)
    )
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        ScreenTopTitle("Notificações", "Avisos da operação e sistema", onBack)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Todas", "Operação", "Sistema").forEach { item -> ModeButton(item, filter == item, Modifier.weight(1f)) { filter = item } }
        }
        rows.forEach { NotificationCard(it.first, it.second, it.third) }
        PrimaryButton("Marcar todas como lidas") { }
    }
}

@Composable
private fun PermissionsReferenceScreen(
    permissionStatus: PermissionStatus,
    onRefresh: () -> Unit,
    onBack: () -> Unit,
    onOpenNotificationSettings: () -> Unit,
    onOpenLocationSettings: () -> Unit,
    onOpenFullScreenSettings: () -> Unit,
    onOpenBatterySettings: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        ScreenTopTitle("Permissões do app", "Checklist para receber corridas", onBack)
        RodriguesLogoBlock()
        Text("Precisamos de algumas permissões para você receber corridas com segurança e eficiência.", color = Muted, fontSize = 15.sp, lineHeight = 20.sp, fontWeight = FontWeight.SemiBold, fontFamily = AppFont, textAlign = TextAlign.Center)
        GlassCard(padding = 0) {
            PermissionRow("Notificações", permissionStatus.notifications) { onOpenNotificationSettings() }
            Divider(color = BorderSoft)
            PermissionRow("Localização", permissionStatus.location) { onOpenLocationSettings() }
            Divider(color = BorderSoft)
            PermissionRow("Alerta em tela cheia", permissionStatus.fullScreenIntent) { onOpenFullScreenSettings() }
            Divider(color = BorderSoft)
            PermissionRow("Bateria sem restrição", permissionStatus.batteryUnrestricted) { onOpenBatterySettings() }
        }
        PrimaryButton("Configurar permissões") { onRefresh() }
        OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth().height(54.dp), shape = RoundedCornerShape(18.dp)) { Text("Continuar", color = Lime, fontWeight = FontWeight.Black, fontFamily = AppFont) }
    }
}

@Composable
private fun AccessReferenceScreen(
    password: String,
    confirmPassword: String,
    loading: Boolean,
    onPassword: (String) -> Unit,
    onConfirm: (String) -> Unit,
    onBack: () -> Unit,
    onSave: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        ScreenTopTitle("Criar senha", "Segurança da conta", onBack)
        RodriguesLogoBlock()
        Box(Modifier.size(118.dp).clip(RoundedCornerShape(36.dp)).background(Color(0xFFEAF8EE)), contentAlignment = Alignment.Center) {
            Icon(Icons.Filled.Shield, contentDescription = null, tint = Lime, modifier = Modifier.size(62.dp))
        }
        Text("Criar sua senha", color = Ink, fontSize = 24.sp, fontWeight = FontWeight.Black, fontFamily = AppFont)
        Text("Defina uma senha segura para acessar sua conta.", color = Muted, textAlign = TextAlign.Center, fontSize = 14.sp, fontFamily = AppFont)
        GlassCard(padding = 18) {
            AppField(password, onPassword, "Nova senha", KeyboardType.Password, PasswordVisualTransformation())
            Spacer(Modifier.height(10.dp))
            AppField(confirmPassword, onConfirm, "Confirmar senha", KeyboardType.Password, PasswordVisualTransformation())
            Spacer(Modifier.height(12.dp))
            listOf("Mínimo de 8 caracteres", "Pelo menos uma letra maiúscula", "Pelo menos um número", "Pelo menos um caractere especial").forEach {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = Lime, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(it, color = Muted, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, fontFamily = AppFont)
                }
                Spacer(Modifier.height(5.dp))
            }
        }
        PrimaryButton("Salvar e continuar", enabled = !loading, loading = loading) { onSave() }
    }
}

@Composable
private fun SettingsReferenceScreen(themeMode: String, onThemeChanged: (String) -> Unit, onBack: () -> Unit) {
    val context = LocalContext.current
    var navPreference by remember { mutableStateOf(AppSettings.getNavigationApp(context)) }
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        ScreenTopTitle("Preferências", "Navegação e aparência", onBack)
        GlassCard(padding = 18) {
            SectionTitle("Navegação padrão", "Escolha o app usado nas rotas.")
            Spacer(Modifier.height(10.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ModeButton("Celular", navPreference == AppSettings.NAV_AUTO, Modifier.weight(1f)) { navPreference = AppSettings.NAV_AUTO; AppSettings.setNavigationApp(context, navPreference) }
                ModeButton("Maps", navPreference == AppSettings.NAV_GOOGLE, Modifier.weight(1f)) { navPreference = AppSettings.NAV_GOOGLE; AppSettings.setNavigationApp(context, navPreference) }
                ModeButton("Waze", navPreference == AppSettings.NAV_WAZE, Modifier.weight(1f)) { navPreference = AppSettings.NAV_WAZE; AppSettings.setNavigationApp(context, navPreference) }
            }
        }
        GlassCard(padding = 18) {
            SectionTitle("Aparência", "Tema do aplicativo.")
            Spacer(Modifier.height(10.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ModeButton("Claro", themeMode == AppSettings.THEME_LIGHT, Modifier.weight(1f)) { onThemeChanged(AppSettings.THEME_LIGHT) }
                ModeButton("Escuro", themeMode == AppSettings.THEME_DARK, Modifier.weight(1f)) { onThemeChanged(AppSettings.THEME_DARK) }
            }
        }
        StatusInfoCard("Versão do app", "6.6.0 visual completo aplicado em todas as áreas principais.", Blue)
    }
}

@Composable
private fun SupportReferenceScreen(onBack: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        ScreenTopTitle("Suporte", "Ajuda, estabilidade e operação", onBack)
        RodriguesLogoBlock()
        StateVisualBlock(Icons.Filled.CloudOff, "Erro Firebase", "Se não conseguir carregar dados, tente atualizar ou use o último dado salvo.", Danger)
        StateVisualBlock(Icons.Filled.CloudOff, "Sem internet", "O app tenta reconectar automaticamente em segundo plano.", Blue)
        StateVisualBlock(Icons.Filled.Shield, "Permissão negada", "Fale com a operação caso sua conta não tenha acesso a alguma função.", Warning)
        StateVisualBlock(Icons.Filled.Schedule, "Manutenção", "Acompanhe avisos pela central de notificações quando o sistema voltar.", Lime)
    }
}

@Composable
private fun ScreenTopTitle(title: String, subtitle: String, onBack: (() -> Unit)? = null) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        if (onBack != null) {
            HeaderBackCircle(onBack)
            Spacer(Modifier.width(10.dp))
        }
        Column(Modifier.weight(1f), horizontalAlignment = if (onBack == null) Alignment.Start else Alignment.Start) {
            Text(title, color = Ink, fontSize = 24.sp, fontWeight = FontWeight.Black, fontFamily = AppFont)
            Text(subtitle, color = Muted, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, fontFamily = AppFont)
        }
    }
}

@Composable
private fun MoreMenuTile(title: String, subtitle: String, icon: ImageVector, modifier: Modifier, onClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(24.dp),
        modifier = modifier.border(1.dp, BorderSoft, RoundedCornerShape(24.dp)).clickable { onClick() }
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(44.dp).clip(RoundedCornerShape(16.dp)).background(Color(0xFFEAF8EE)), contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = Lime, modifier = Modifier.size(24.dp))
            }
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(title, color = Ink, fontSize = 14.sp, fontWeight = FontWeight.Black, fontFamily = AppFont, maxLines = 1)
                Text(subtitle, color = Muted, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, fontFamily = AppFont, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable
private fun AccountDataLine(icon: ImageVector, label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = Lime, modifier = Modifier.size(19.dp))
        Spacer(Modifier.width(12.dp))
        Text(label, color = Muted, fontSize = 13.sp, fontWeight = FontWeight.Bold, fontFamily = AppFont, modifier = Modifier.weight(1f))
        Text(value, color = Ink, fontSize = 13.sp, fontWeight = FontWeight.Bold, fontFamily = AppFont, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun StatusInfoCard(title: String, message: String, color: Color) {
    Card(colors = CardDefaults.cardColors(containerColor = color.copy(alpha = .08f)), shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth().border(1.dp, color.copy(alpha = .18f), RoundedCornerShape(20.dp))) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(40.dp).clip(CircleShape).background(Color.White), contentAlignment = Alignment.Center) { Icon(Icons.Filled.Shield, contentDescription = null, tint = color, modifier = Modifier.size(20.dp)) }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(title, color = Ink, fontSize = 14.sp, fontWeight = FontWeight.Black, fontFamily = AppFont)
                Text(message, color = Muted, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, fontFamily = AppFont)
            }
        }
    }
}

@Composable
private fun NotificationCard(title: String, subtitle: String, color: Color) {
    Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(22.dp), modifier = Modifier.fillMaxWidth().border(1.dp, BorderSoft, RoundedCornerShape(22.dp))) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(50.dp).clip(CircleShape).background(color.copy(alpha = .10f)), contentAlignment = Alignment.Center) {
                Icon(if (color == Warning) Icons.Filled.ErrorOutline else Icons.Filled.Notifications, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(title, color = Ink, fontSize = 14.sp, fontWeight = FontWeight.Black, fontFamily = AppFont)
                Text(subtitle, color = Muted, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, fontFamily = AppFont)
            }
            Box(Modifier.size(8.dp).clip(CircleShape).background(color))
        }
    }
}

@Composable
private fun StateVisualBlock(icon: ImageVector, title: String, message: String, color: Color) {
    Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(24.dp), modifier = Modifier.fillMaxWidth().border(1.dp, BorderSoft, RoundedCornerShape(24.dp))) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(58.dp).clip(RoundedCornerShape(20.dp)).background(color.copy(alpha = .10f)), contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(30.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(title, color = Ink, fontSize = 16.sp, fontWeight = FontWeight.Black, fontFamily = AppFont)
                Text(message, color = Muted, fontSize = 12.sp, lineHeight = 16.sp, fontWeight = FontWeight.SemiBold, fontFamily = AppFont)
            }
        }
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
            SettingButton("Versão do app", "6.6.0 todas as telas")
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
            .background(FillSoft)
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
            Text("Versão 6.5.0 • reconstrução visual fiel", color = Muted2, fontSize = 12.sp)
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
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = PanelSoft),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, borderColor, RoundedCornerShape(24.dp))
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
        Text(text, color = if (isError) Color(0xFFFFC4D0) else Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(12.dp))
    }
}

@Composable
private fun Avatar(name: String, photoUrl: String = "") {
    Box(
        Modifier
            .size(58.dp)
            .clip(CircleShape)
            .background(Color(0xFFE2F7E7))
            .border(2.dp, Color.White, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        if (photoUrl.isNotBlank()) {
            AsyncImage(
                model = photoUrl,
                contentDescription = "Foto do entregador",
                modifier = Modifier.fillMaxSize().clip(CircleShape)
            )
        } else {
            Text(name.trim().firstOrNull()?.uppercase() ?: "E", color = LimeDark, fontWeight = FontWeight.Black, fontSize = 22.sp, fontFamily = AppFont)
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
