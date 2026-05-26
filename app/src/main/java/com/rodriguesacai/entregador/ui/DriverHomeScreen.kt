package com.rodriguesacai.entregador.ui

import android.content.Context
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.BatteryManager
import android.provider.Settings
import android.widget.Toast
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
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Payments
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
import com.rodriguesacai.entregador.data.AppNotice
import com.rodriguesacai.entregador.data.DriverHistory
import com.rodriguesacai.entregador.data.DriverOperationalPreferences
import com.rodriguesacai.entregador.data.DriverPayout
import com.rodriguesacai.entregador.data.DriverProfile
import com.rodriguesacai.entregador.data.DriverRegistrationRequest
import com.rodriguesacai.entregador.data.DriverRepository
import com.rodriguesacai.entregador.data.DriverRide
import com.rodriguesacai.entregador.data.DriverStats
import com.rodriguesacai.entregador.data.PaymentMachine
import com.rodriguesacai.entregador.data.PaymentSettlementInput
import com.rodriguesacai.entregador.data.AppRuntimeConfig
import com.rodriguesacai.entregador.service.AppAlertPlayer
import com.rodriguesacai.entregador.service.NotificationHelper
import kotlinx.coroutines.delay

private enum class AppTab { Inicio, Corridas, Mapa, Ganhos, Historico, Conta, Notificacoes }

private val AppFont = RodriguesFonts.Montserrat

private object WalletType {
    val title = 28.sp
    val label = 13.sp
    val tiny = 11.sp
    val body = 12.sp
    val bodyStrong = 14.sp
    val cardTitle = 16.sp
    val cardValue = 22.sp
    val heroValue = 36.sp
}
private val BgTop = Color.White
private val BgBottom = Color.White
private val Panel = Color.White
private val PanelSoft = Color.White
private val Purple = Color(0xFF0FAE4B)
private val Purple2 = Color(0xFFFF7A00)
private val Lime = Color(0xFF0FAE4B)
private val LimeDark = Color(0xFF07883E)
private val Ink = Color(0xFF101216)
private val Muted = Color(0xFF66717D)
private val Muted2 = Color(0xFFA1ABB5)
private val Danger = Color(0xFFEF233C)
private val Warning = Color(0xFFF97316)
private val Blue = Color(0xFF2563EB)
private val BorderSoft = Color(0xFFE8EEF3)
private val FillSoft = Color(0xFFF8FAFC)

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
    onOpenBatterySettings: () -> Unit,
    onRequestNotificationPermission: () -> Unit,
    onRequestLocationPermission: () -> Unit
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
    var appNotices by remember { mutableStateOf<List<AppNotice>>(emptyList()) }
    var paymentMachines by remember { mutableStateOf<List<PaymentMachine>>(emptyList()) }
    var appRuntimeConfig by remember { mutableStateOf(AppRuntimeConfig()) }
    var error by remember { mutableStateOf("") }
    var notice by remember { mutableStateOf("") }
    var hideValues by remember { mutableStateOf(AppSettings.getHideValues(context)) }
    var themeMode by remember { mutableStateOf(AppSettings.getThemeMode(context)) }
    var welcomeDone by remember { mutableStateOf(AppSettings.isWelcomeDone(context)) }
    var bootingSession by remember { mutableStateOf(profile != null) }

    DisposableEffect(profile?.id, online) {
        val pendingListener = if (profile != null && online) {
            DriverRepository.listenPendingRide(context, onRide = { ride ->
                pendingRide = if (activeRide != null && ride != null && !ride.isRouteAddition) null else ride
            }, onError = { error = it })
        } else null
        val activeListener = if (profile != null) {
            DriverRepository.listenMyActiveRide(context, onRide = { ride ->
                activeRide = ride
                if (ride != null && pendingRide != null && !pendingRide!!.isRouteAddition) pendingRide = null
            }, onError = { error = it })
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
        val noticeListener = if (profile != null) {
            DriverRepository.listenAppNotifications(context, onNotices = { appNotices = it }, onError = { /* avisos vazios nao bloqueiam operacao */ })
        } else null
        val profileListener = if (profile != null) {
            DriverRepository.listenDriverProfile(context, onProfile = { liveProfile -> profile = liveProfile }, onError = { /* perfil vazio nao bloqueia app */ })
        } else null
        val machinesListener = if (profile != null) {
            DriverRepository.listenMachineOptions(onMachines = { paymentMachines = it }, onError = { /* maquininhas vazias nao bloqueiam app */ })
        } else null
        val appConfigListener = if (profile != null) {
            DriverRepository.listenAppRuntimeConfig(onConfig = { appRuntimeConfig = it }, onError = { /* config vazia nao bloqueia app */ })
        } else null
        onDispose {
            pendingListener?.remove()
            activeListener?.remove()
            historyListener?.remove()
            statsListener?.remove()
            carouselListener?.remove()
            noticeListener?.remove()
            profileListener?.remove()
            machinesListener?.remove()
            appConfigListener?.remove()
        }
    }

    LaunchedEffect(profile?.id) {
        if (profile != null) {
            bootingSession = true
            delay(900)
            bootingSession = false
        }
    }

    LaunchedEffect(pendingRide?.id, online, activeRide?.id) {
        val ride = pendingRide
        if (online && ride != null && activeRide == null) {
            NotificationHelper.urgentRideNotification(
                context = context,
                rideId = ride.id,
                value = ride.value,
                distance = ride.distance,
                duration = ride.duration,
                pickup = ride.pickup,
                dropoff = ride.dropoff,
                paymentMethod = ride.paymentMethod,
                paymentStatus = ride.paymentStatus,
                amountToCollect = DriverRepository.formatCurrency(ride.amountToCollectNumber).takeIf { ride.amountToCollectNumber > 0.0 } ?: "",
                changeFor = DriverRepository.formatCurrency(ride.changeForNumber).takeIf { ride.changeForNumber > 0.0 } ?: "",
                requiresMachine = ride.requiresMachine.toString()
            )
        }
    }


    var lastSystemNoticeId by remember { mutableStateOf("") }
    LaunchedEffect(appNotices.map { it.id to it.read }) {
        val noticeToShow = appNotices.firstOrNull { it.isVisible() && !it.read }
        if (noticeToShow != null && noticeToShow.id != lastSystemNoticeId) {
            lastSystemNoticeId = noticeToShow.id
            NotificationHelper.appNoticeNotification(
                context = context,
                noticeId = noticeToShow.id,
                title = noticeToShow.title,
                message = noticeToShow.message,
                category = noticeToShow.category
            )
        }
    }

    if (!welcomeDone) {
        WelcomePermissionsScreen(
            onRequestNotificationPermission = onRequestNotificationPermission,
            onRequestLocationPermission = onRequestLocationPermission,
            onOpenFullScreenSettings = onOpenFullScreenSettings,
            onOpenBatterySettings = onOpenBatterySettings,
            onContinue = {
                AppSettings.setWelcomeDone(context, true)
                welcomeDone = true
            }
        )
        return
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

    if (bootingSession) {
        LoadingSessionSplash(profile = profile!!)
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
                navItem(AppTab.Conta, tab, "Mais", Icons.Filled.MoreHoriz) { tab = it }
            }
        }
    ) { padding ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color.White)
        ) {
            when (tab) {
                AppTab.Inicio -> HomeContent(
                    profile = profile!!,
                    online = online,
                    pendingRide = pendingRide,
                    activeRide = activeRide,
                    stats = stats,
                    appBanners = appBanners,
                    appNotices = appNotices,
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
                            normalized.contains("mapa") -> AppTab.Mapa
                            normalized.contains("rota") || normalized.contains("corrida") -> AppTab.Corridas
                            else -> AppTab.Inicio
                        }
                    },
                    onOpenRides = { tab = AppTab.Corridas },
                    onOpenMap = { tab = AppTab.Mapa },
                    onOpenNotifications = { tab = AppTab.Notificacoes },
                    onOpenSupport = { tab = AppTab.Conta }
                )
                AppTab.Corridas -> RidesContent(
                    pendingRide = pendingRide,
                    activeRide = activeRide,
                    paymentMachines = paymentMachines,
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
                AppTab.Mapa -> DriverMapContent(
                    activeRide = activeRide,
                    online = online,
                    onBackHome = { tab = AppTab.Inicio },
                    onOpenNavigator = onOpenNavigator,
                    onUpdateRide = { ride, status -> DriverRepository.updateRideStatus(context, ride.id, status, onDone = { }, onError = { error = it }) },
                    paymentMachines = paymentMachines
                )
                AppTab.Ganhos -> EarningsContent(profile!!, stats, history, onEditBank = { tab = AppTab.Conta })
                AppTab.Historico -> HistoryContent(history)
                AppTab.Notificacoes -> NotificationsReferenceScreen(appNotices, onBack = { tab = AppTab.Inicio })
                AppTab.Conta -> AccountContent(
                    profile = profile!!,
                    online = online,
                    hideValues = hideValues,
                    themeMode = themeMode,
                    appNotices = appNotices,
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
private fun WelcomePermissionsScreen(
    onRequestNotificationPermission: () -> Unit,
    onRequestLocationPermission: () -> Unit,
    onOpenFullScreenSettings: () -> Unit,
    onOpenBatterySettings: () -> Unit,
    onContinue: () -> Unit
) {
    val context = LocalContext.current
    var permissionStatus by remember { mutableStateOf(PermissionStatusReader.read(context)) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 20.dp, vertical = 18.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Spacer(Modifier.height(12.dp))
                RodriguesLogoBlock(compact = false)
                Spacer(Modifier.height(22.dp))
                Text(
                    "Permissões essenciais",
                    color = Ink,
                    fontSize = 30.sp,
                    lineHeight = 32.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = AppFont,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "Configure uma vez. Depois o app recebe corridas, localização e alerta urgente sem atrapalhar a operação.",
                    color = Muted,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    fontFamily = AppFont,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
                )
                Spacer(Modifier.height(22.dp))
                PremiumScreenCard {
                    PermissionSetupRow("Notificações", "Toca quando chegar oferta.", permissionStatus.notifications, Icons.Filled.NotificationsActive) {
                        onRequestNotificationPermission()
                        permissionStatus = PermissionStatusReader.read(context)
                    }
                    PermissionSetupRow("Localização", "Mapa, rota e rastreio.", permissionStatus.location, Icons.Filled.MyLocation) {
                        onRequestLocationPermission()
                        permissionStatus = PermissionStatusReader.read(context)
                    }
                    PermissionSetupRow("Alerta urgente", "Abre a tela da corrida.", permissionStatus.fullScreenIntent, Icons.Filled.Bolt) {
                        onOpenFullScreenSettings()
                        permissionStatus = PermissionStatusReader.read(context)
                    }
                    PermissionSetupRow("Bateria", "Mantém o serviço ativo.", permissionStatus.batteryUnrestricted, Icons.Filled.Shield) {
                        onOpenBatterySettings()
                        permissionStatus = PermissionStatusReader.read(context)
                    }
                }
            }
            Column {
                PrimaryButton(if (permissionStatus.ready) "Tudo pronto" else "Configurar agora") {
                    if (!permissionStatus.notifications) onRequestNotificationPermission()
                    if (!permissionStatus.location) onRequestLocationPermission()
                    permissionStatus = PermissionStatusReader.read(context)
                }
                Spacer(Modifier.height(10.dp))
                OutlinedButton(onClick = onContinue, modifier = Modifier.fillMaxWidth().height(54.dp), shape = RoundedCornerShape(22.dp)) {
                    Text("Continuar", color = Lime, fontWeight = FontWeight.Black, fontFamily = AppFont)
                }
            }
        }
    }
}

@Composable
private fun PermissionSetupRow(title: String, message: String, ok: Boolean, icon: ImageVector, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(if (ok) Color(0xFFF3FCF6) else Color.White)
            .border(1.dp, if (ok) Lime.copy(alpha = .22f) else BorderSoft, RoundedCornerShape(22.dp))
            .clickable { if (!ok) onClick() }
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(42.dp).clip(RoundedCornerShape(16.dp)).background(if (ok) Lime else Color(0xFFF2F7F4)), contentAlignment = Alignment.Center) {
            Icon(if (ok) Icons.Filled.CheckCircle else icon, contentDescription = null, tint = if (ok) Color.White else LimeDark, modifier = Modifier.size(22.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(title, color = Ink, fontSize = 15.sp, fontWeight = FontWeight.Black, fontFamily = AppFont)
            Text(message, color = Muted, fontSize = 12.sp, lineHeight = 15.sp, fontWeight = FontWeight.SemiBold, fontFamily = AppFont)
        }
        Text(if (ok) "OK" else "Ativar", color = if (ok) LimeDark else Lime, fontSize = 12.sp, fontWeight = FontWeight.Black, fontFamily = AppFont)
    }
}

@Composable
private fun LoadingSessionSplash(profile: DriverProfile) {
    Box(
        modifier = Modifier.fillMaxSize().background(BgTop).padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            RodriguesLogoBlock(compact = false)
            Spacer(Modifier.height(24.dp))
            CircularProgressIndicator(color = Lime, strokeWidth = 4.dp)
            Spacer(Modifier.height(18.dp))
            Text("Carregando operação", color = Ink, fontSize = 22.sp, fontWeight = FontWeight.Black, fontFamily = AppFont)
            Text("Preparando dados do app", color = Muted, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, fontFamily = AppFont)
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
private fun RodriguesLogoBlock(compact: Boolean = false) {
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
        modifier = Modifier.fillMaxWidth().border(1.dp, BorderSoft, RoundedCornerShape(30.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        shape = RoundedCornerShape(30.dp)
    ) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp), content = content)
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
    appNotices: List<AppNotice>,
    hideValues: Boolean,
    onToggleValues: () -> Unit,
    error: String,
    onToggleOnline: (Boolean) -> Unit,
    onAccept: (DriverRide) -> Unit,
    onReject: (DriverRide, String) -> Unit,
    onExpire: (DriverRide) -> Unit,
    onUpdateRide: (DriverRide, String) -> Unit,
    onOpenNavigator: (pickup: String, dropoff: String) -> Unit,
    onCarouselInternal: (String) -> Unit,
    onOpenRides: () -> Unit,
    onOpenMap: () -> Unit,
    onOpenNotifications: () -> Unit,
    onOpenSupport: () -> Unit
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
            unreadNotices = appNotices.count { it.isVisible() && !it.read },
            onToggleValues = onToggleValues,
            onNotificationsClick = onOpenNotifications,
            onSupportClick = onOpenSupport,
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

        when {
            activeRide != null -> CurrentRideHomeShortcut(activeRide, "Corrida em andamento", "Toque para continuar a entrega.", onOpenRides)
            pendingRide != null && online -> CurrentRideHomeShortcut(pendingRide, "Oferta recebida", "Toque para decidir pela aba Corridas.", onOpenRides)
            operational.kind == AvailabilityKind.Restricao -> RestrictionCard(operational)
        }

        val visibleBanners = if (appBanners.isNotEmpty()) appBanners else defaultHomeBanners(operational)
        AppHomeCarousel(visibleBanners, onInternalAction = onCarouselInternal)
        QuickActionsGrid(onInternalAction = onCarouselInternal, onOpenMap = onOpenMap)
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
private fun QuickActionsGrid(onInternalAction: (String) -> Unit, onOpenMap: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            QuickActionTile("Histórico", "Ver corridas", Icons.Filled.History, Modifier.weight(1f)) { onInternalAction("historico") }
            QuickActionTile("Ganhos", "Resumo financeiro", Icons.Filled.AccountBalanceWallet, Modifier.weight(1f)) { onInternalAction("ganhos") }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            QuickActionTile("Mapa", "Ver região", Icons.Filled.Map, Modifier.weight(1f)) { onOpenMap() }
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
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFE8F8EC)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = Lime, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(title, color = Ink, fontSize = 14.sp, fontWeight = FontWeight.Black, fontFamily = AppFont, maxLines = 1)
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
    unreadNotices: Int,
    onToggleValues: () -> Unit,
    onNotificationsClick: () -> Unit,
    onSupportClick: () -> Unit,
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
                    fontSize = 19.sp,
                    fontWeight = FontWeight.ExtraBold,
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
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Clip,
                    fontFamily = AppFont
                )
            }
            HeaderRoundIcon(Icons.Filled.Notifications, showDot = unreadNotices > 0, onClick = onNotificationsClick)
            Spacer(Modifier.width(8.dp))
            HeaderRoundIcon(Icons.Filled.ChatBubbleOutline, onClick = onSupportClick)
        }

        Button(
            onClick = onStatusClick,
            modifier = Modifier.fillMaxWidth().height(54.dp),
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = operational.buttonColor,
                contentColor = operational.textColor
            ),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = if (operational.kind == AvailabilityKind.Indisponivel) .42f else .18f)),
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
                Text(operational.label, fontSize = 17.sp, fontWeight = FontWeight.ExtraBold, fontFamily = AppFont, maxLines = 1, modifier = Modifier.weight(1f))
                Icon(Icons.Filled.KeyboardArrowRight, contentDescription = null, tint = operational.textColor.copy(alpha = .85f), modifier = Modifier.size(22.dp))
            }
        }
    }
}


@Composable
private fun HeaderRoundIcon(icon: ImageVector, showDot: Boolean = false, onClick: () -> Unit = {}) {
    Box(
        Modifier
            .size(42.dp)
            .clip(CircleShape)
            .background(Color.White)
            .border(1.dp, Color(0xFFE2E8E0), CircleShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = Ink, modifier = Modifier.size(20.dp))
        if (showDot) {
            Box(
                Modifier
                    .align(Alignment.TopEnd)
                    .padding(7.dp)
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(Danger)
            )
        }
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
                .padding(horizontal = 18.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                Modifier
                    .weight(1.25f)
                    .padding(end = 14.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text("Ganhos de hoje", color = Muted, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = AppFont, maxLines = 1)
                Spacer(Modifier.height(6.dp))
                Text(
                    if (hideValues) "R$ •••••" else DriverRepository.formatCurrency(stats.totalToday),
                    color = Ink,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontFamily = AppFont
                )
            }
            Divider(Modifier.height(58.dp).width(1.dp), color = Color(0xFFE5EAE4))
            Column(
                Modifier
                    .weight(.92f)
                    .padding(start = 14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DailyCounterLine(value = stats.ridesTodayCount, label = "Corridas")
                Divider(color = Color(0xFFE5EAE4), thickness = 1.dp)
                DailyCounterLine(value = stats.finishedTodayCount, label = "Finalizadas")
            }
        }
    }
}

@Composable
private fun DailyCounterLine(value: Int, label: String) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(value.toString(), color = LimeDark, fontSize = 20.sp, fontWeight = FontWeight.Black, fontFamily = AppFont, modifier = Modifier.width(38.dp), textAlign = TextAlign.Center)
        Text(label, color = Muted, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = AppFont, maxLines = 1, overflow = TextOverflow.Ellipsis)
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
private fun CurrentRideHomeShortcut(ride: DriverRide, title: String, message: String, onOpenRides: () -> Unit) {
    GlassCard(padding = 16, borderColor = Lime.copy(alpha = .28f)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(44.dp).clip(CircleShape).background(Color(0xFFE8F8EC)), contentAlignment = Alignment.Center) {
                Icon(Icons.Filled.Route, contentDescription = null, tint = Lime, modifier = Modifier.size(23.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(title, color = Ink, fontSize = 16.sp, fontWeight = FontWeight.Black, fontFamily = AppFont)
                Text(message, color = Muted, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, fontFamily = AppFont)
                val detail = listOf(ride.orderCode.takeIf { it.isNotBlank() }?.let { "Pedido #$it" }, ride.value.takeIf { it.isNotBlank() }).filterNotNull().joinToString(" • ")
                if (detail.isNotBlank()) Text(detail, color = LimeDark, fontSize = 12.sp, fontWeight = FontWeight.Black, fontFamily = AppFont)
            }
            OutlinedButton(onClick = onOpenRides, shape = RoundedCornerShape(16.dp)) {
                Text("Abrir", color = Lime, fontWeight = FontWeight.Black, fontFamily = AppFont)
            }
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
                StopLine("COLETA", ride.pickup.ifBlank { "Coleta não informada" }, Lime)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    MiniStat("Distância até coleta", ride.distance.ifBlank { "A definir" }, Modifier.weight(1f))
                    MiniStat("Tempo estimado", ride.duration.ifBlank { "A definir" }, Modifier.weight(1f))
                }
            }
            Divider(color = Color(0xFFE5EAE4))
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Valor da corrida", color = Muted, fontSize = 13.sp, fontWeight = FontWeight.Bold, fontFamily = AppFont)
                        Text(ride.value.ifBlank { "A definir" }, color = Lime, fontSize = 36.sp, fontWeight = FontWeight.Black, fontFamily = AppFont)
                    }
                    StatusPill("Oferta", true)
                }
                PaymentOperationPanel(ride, compact = true)
                RealDeliveryMap(
                    title = "Preview da rota",
                    subtitle = listOf(ride.distance, ride.duration).filter { it.isNotBlank() }.joinToString(" • ").ifBlank { "Dados da rota serão exibidos quando chegarem" },
                    pickupAddress = ride.pickup,
                    dropoffAddress = ride.dropoff.ifBlank { ride.neighborhood },
                    pickupLat = ride.pickupLat,
                    pickupLng = ride.pickupLng,
                    dropoffLat = ride.dropoffLat,
                    dropoffLng = ride.dropoffLng
                )
                StopLine("ENTREGA", ride.dropoff.ifBlank { ride.neighborhood.ifBlank { "Área da entrega não informada" } }, Purple2)
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
private fun ActiveRideCard(ride: DriverRide, routeAddition: DriverRide? = null, onOpenNavigator: (pickup: String, dropoff: String) -> Unit, onUpdateRide: (DriverRide, String) -> Unit, paymentMachines: List<PaymentMachine> = emptyList()) {
    val context = LocalContext.current
    var problemDialogOpen by remember(ride.id) { mutableStateOf(false) }
    val inOccurrence = ride.status == "occurrence"
    val title = when (ride.status) {
        "accepted", "ACEITA", "A_CAMINHO_LOJA" -> "Indo para coleta"
        "pickup", "COLETANDO", "NA_COLETA" -> "Na coleta"
        "delivering", "EM_ROTA", "SAIU_ENTREGA" -> "Em entrega"
        "arrived_client", "ENTREGADOR_NO_LOCAL", "CHEGOU_CLIENTE" -> "No cliente"
        "occurrence" -> "Ocorrência enviada"
        else -> "Rota atual"
    }
    val nextLabel = when (ride.status) {
        "accepted", "ACEITA", "A_CAMINHO_LOJA" -> "Cheguei na coleta"
        "pickup", "COLETANDO", "NA_COLETA" -> if (ride.pickupReleaseAllowed) "Iniciar entrega" else "Aguardando saída"
        "delivering", "EM_ROTA", "SAIU_ENTREGA" -> "Cheguei no cliente"
        "arrived_client", "ENTREGADOR_NO_LOCAL", "CHEGOU_CLIENTE" -> "Finalizar entrega"
        "occurrence" -> "Aguardando gestor"
        else -> "Atualizar etapa"
    }
    val nextStatus = when (ride.status) {
        "accepted", "ACEITA", "A_CAMINHO_LOJA" -> "pickup"
        "pickup", "COLETANDO", "NA_COLETA" -> "delivering"
        "delivering", "EM_ROTA", "SAIU_ENTREGA" -> "arrived_client"
        "arrived_client", "ENTREGADOR_NO_LOCAL", "CHEGOU_CLIENTE" -> "finished"
        "occurrence" -> "occurrence"
        else -> "pickup"
    }
    val isArrivedClient = ride.status in listOf("arrived_client", "ENTREGADOR_NO_LOCAL", "CHEGOU_CLIENTE")
    val isDelivering = ride.status in listOf("delivering", "EM_ROTA", "SAIU_ENTREGA", "arrived_client", "ENTREGADOR_NO_LOCAL", "CHEGOU_CLIENTE")
    val waitingPickupRelease = ride.status in listOf("pickup", "COLETANDO", "NA_COLETA") && !ride.pickupReleaseAllowed
    val routeCode = ride.routeReleaseCode.ifBlank { ride.routeId.ifBlank { ride.orderCode.ifBlank { ride.id.takeLast(4).uppercase(Locale.ROOT) } } }
    val totalOrders = ride.routeOrderCount.coerceAtLeast(if (ride.routeOrders.isNotEmpty()) ride.routeOrders.size else 1)
    val readyOrders = ride.routeReadyCount.coerceAtLeast(ride.routeOrders.count { it.ready }).coerceAtMost(totalOrders)
    val routeSubtitle = when {
        totalOrders > 1 -> "$readyOrders de $totalOrders prontos"
        readyOrders >= 1 || ride.routeOrders.firstOrNull()?.ready == true -> "1 pedido pronto"
        else -> "1 pedido"
    }
    val deliveryText = if (isDelivering) {
        ride.dropoff.ifBlank { ride.neighborhood.ifBlank { "Endereço indisponível" } }
    } else {
        ride.neighborhood.ifBlank { "Endereço após saída" }
    }
    val navDestination = if (isDelivering) ride.dropoff else ride.pickup

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        RideCompactHeader(
            routeCode = routeCode,
            title = title,
            subtitle = routeSubtitle,
            status = if (inOccurrence) "Aguardando gestor" else if (waitingPickupRelease) "Aguardando saída" else if (ride.pickupReleaseAllowed && !isDelivering) "Liberada" else ride.status.statusLabel(),
            statusOk = !waitingPickupRelease && !inOccurrence
        )

        if (routeAddition != null) {
            CompactNotice(Icons.Filled.ReceiptLong, "Pedido adicionado", "Rota atualizada pelo gestor", Blue)
        }

        if (inOccurrence) {
            CompactNotice(Icons.Filled.ErrorOutline, "Ocorrência enviada", "Aguardando resposta do gestor", Warning)
        }

        if (waitingPickupRelease) {
            PickupReleaseCard(routeCode = routeCode, ready = routeSubtitle)
        }

        RealDeliveryMap(
            title = "Rota atual",
            subtitle = listOf(ride.distance, ride.duration).filter { it.isNotBlank() }.joinToString(" • ").ifBlank { title },
            pickupAddress = ride.pickup,
            dropoffAddress = if (isDelivering) ride.dropoff else ride.neighborhood,
            pickupLat = ride.pickupLat,
            pickupLng = ride.pickupLng,
            dropoffLat = ride.dropoffLat,
            dropoffLng = ride.dropoffLng,
            mode = if (isDelivering) DeliveryMapMode.DRIVER_TO_DROPOFF else DeliveryMapMode.DRIVER_TO_PICKUP
        )

        RouteSummaryCompact(ride = ride, deliveryText = deliveryText, isDelivering = isDelivering)
        PaymentOperationPanel(ride, compact = true)
        RouteOrdersPanel(ride)

        if (isArrivedClient) {
            DeliveryCodeAndPaymentGate(ride = ride, paymentMachines = paymentMachines, onConfirm = { input ->
                DriverRepository.savePaymentSettlementForRide(
                    context = context,
                    rideId = ride.id,
                    input = input,
                    onDone = { onUpdateRide(ride, "finished") },
                    onError = { onUpdateRide(ride, "finished") }
                )
            })
        } else {
            RideActionArea(
                waitingPickupRelease = waitingPickupRelease || inOccurrence,
                canNavigate = navDestination.isNotBlank() && !waitingPickupRelease && !inOccurrence,
                nextLabel = nextLabel,
                onNavigate = { onOpenNavigator(ride.pickup, navDestination) },
                onProblem = { problemDialogOpen = true },
                onNext = { if (!inOccurrence) onUpdateRide(ride, nextStatus) },
                blockedLabel = if (inOccurrence) "Aguardando gestor" else "Aguardando saída",
                problemLabel = if (inOccurrence) "Ocorrência enviada" else "Problema na rota",
                problemEnabled = !inOccurrence
            )
        }

        RideFinancialPanel(ride, compact = true)
    }

    if (problemDialogOpen) {
        ProblemReportDialog(
            onDismiss = { problemDialogOpen = false },
            onConfirm = { reason, details ->
                problemDialogOpen = false
                DriverRepository.reportRideOccurrence(
                    context = context,
                    rideId = ride.id,
                    reason = reason,
                    details = details.ifBlank { "Entregador informou problema pela tela da corrida." },
                    onDone = { Toast.makeText(context, "Ocorrência enviada ao gestor.", Toast.LENGTH_SHORT).show() },
                    onError = { message -> Toast.makeText(context, message.ifBlank { "Falha ao enviar ocorrência." }, Toast.LENGTH_LONG).show() }
                )
            }
        )
    }
}

@Composable
private fun ProblemReportDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var selected by remember { mutableStateOf("Loja demorando") }
    var details by remember { mutableStateOf("") }
    val reasons = listOf(
        "Loja demorando",
        "Pedido não encontrado",
        "Problema no veículo",
        "Endereço divergente",
        "Cliente não atende",
        "Pagamento com problema",
        "Local inseguro",
        "Outro"
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Problema na rota", color = Ink, fontWeight = FontWeight.Black, fontFamily = AppFont) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Escolha o motivo. O gestor será avisado e a rota fica pendente de solução.", color = Muted, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, fontFamily = AppFont)
                reasons.forEach { reason ->
                    OutlinedButton(
                        onClick = { selected = reason },
                        modifier = Modifier.fillMaxWidth().height(42.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(if (selected == reason) "✓ $reason" else reason, color = if (selected == reason) LimeDark else Ink, fontWeight = FontWeight.Bold, fontFamily = AppFont, fontSize = 12.sp)
                    }
                }
                OutlinedTextField(
                    value = details,
                    onValueChange = { details = it.take(160) },
                    label = { Text("Detalhe opcional") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(selected, details) }) {
                Text("Enviar", color = Warning, fontWeight = FontWeight.Black, fontFamily = AppFont)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Voltar", color = Muted, fontWeight = FontWeight.Bold, fontFamily = AppFont)
            }
        },
        containerColor = Color.White
    )
}

@Composable
private fun RideCompactHeader(routeCode: String, title: String, subtitle: String, status: String, statusOk: Boolean) {
    Column(Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Rota #$routeCode", color = Ink, fontSize = 26.sp, lineHeight = 28.sp, fontWeight = FontWeight.Black, fontFamily = AppFont, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("$title • $subtitle", color = Muted, fontSize = 13.sp, fontWeight = FontWeight.Bold, fontFamily = AppFont, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            StatusPill(status, statusOk)
        }
    }
}

@Composable
private fun PickupReleaseCard(routeCode: String, ready: String) {
    GlassCard(padding = 18, borderColor = Warning.copy(alpha = .24f)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(56.dp).clip(RoundedCornerShape(20.dp)).background(Color(0xFFFFF4E7)), contentAlignment = Alignment.Center) {
                Icon(Icons.Filled.Shield, contentDescription = null, tint = Warning, modifier = Modifier.size(26.dp))
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text("Código de retirada", color = Ink, fontSize = 15.sp, fontWeight = FontWeight.Black, fontFamily = AppFont)
                Text("Mostre no balcão", color = Muted, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = AppFont)
            }
            Text(routeCode, color = Ink, fontSize = 36.sp, lineHeight = 38.sp, fontWeight = FontWeight.Black, fontFamily = AppFont)
        }
        Spacer(Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            StatusPill(ready, true)
            Spacer(Modifier.width(8.dp))
            Text("Saída pendente", color = Warning, fontSize = 12.sp, fontWeight = FontWeight.Black, fontFamily = AppFont)
        }
    }
}

@Composable
private fun RouteSummaryCompact(ride: DriverRide, deliveryText: String, isDelivering: Boolean) {
    GlassCard(padding = 14) {
        CompactStopLine(Icons.Filled.Storefront, "Coleta", ride.pickup.ifBlank { "Rodrigues Açaí e Cia." }, Lime)
        Divider(color = BorderSoft)
        CompactStopLine(Icons.Filled.Place, if (isDelivering) "Próxima entrega" else "Entrega", deliveryText, if (isDelivering) Lime else Purple2)
        val hasRouteMeta = ride.distance.isNotBlank() || ride.duration.isNotBlank()
        if (hasRouteMeta) {
            Spacer(Modifier.height(10.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (ride.distance.isNotBlank()) SmallMetric("Distância", ride.distance, Modifier.weight(1f))
                if (ride.duration.isNotBlank()) SmallMetric("Tempo", ride.duration, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun CompactStopLine(icon: ImageVector, label: String, value: String, color: Color) {
    Row(Modifier.fillMaxWidth().padding(vertical = 9.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(36.dp).clip(CircleShape).background(color.copy(alpha = .10f)), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(19.dp))
        }
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(label, color = Muted2, fontSize = 11.sp, fontWeight = FontWeight.Black, fontFamily = AppFont)
            Text(value.ifBlank { "Não informado" }, color = Ink, fontSize = 14.sp, fontWeight = FontWeight.Black, fontFamily = AppFont, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun SmallMetric(label: String, value: String, modifier: Modifier = Modifier) {
    Box(modifier.clip(RoundedCornerShape(16.dp)).background(FillSoft).border(1.dp, BorderSoft, RoundedCornerShape(16.dp)).padding(horizontal = 12.dp, vertical = 10.dp)) {
        Column {
            Text(label, color = Muted2, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = AppFont)
            Text(value, color = Ink, fontSize = 14.sp, fontWeight = FontWeight.Black, fontFamily = AppFont)
        }
    }
}

@Composable
private fun RideActionArea(
    waitingPickupRelease: Boolean,
    canNavigate: Boolean,
    nextLabel: String,
    onNavigate: () -> Unit,
    onProblem: () -> Unit,
    onNext: () -> Unit,
    blockedLabel: String = "Aguardando saída",
    problemLabel: String = "Problema na rota",
    problemEnabled: Boolean = true
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (canNavigate) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onNavigate, modifier = Modifier.weight(1f).height(52.dp), shape = RoundedCornerShape(18.dp)) {
                    Icon(Icons.Filled.Route, contentDescription = null, tint = Lime, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(7.dp))
                    Text("Navegar", color = Lime, fontWeight = FontWeight.Black, fontFamily = AppFont, maxLines = 1)
                }
                Button(onClick = onNext, modifier = Modifier.weight(1.25f).height(52.dp), shape = RoundedCornerShape(18.dp), colors = ButtonDefaults.buttonColors(containerColor = Lime, contentColor = Color.White)) {
                    Text(nextLabel, fontWeight = FontWeight.Black, fontFamily = AppFont, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        } else {
            Button(onClick = onNext, enabled = !waitingPickupRelease, modifier = Modifier.fillMaxWidth().height(54.dp), shape = RoundedCornerShape(18.dp), colors = ButtonDefaults.buttonColors(containerColor = if (waitingPickupRelease) Color(0xFFE0E4E6) else Lime, contentColor = if (waitingPickupRelease) Muted2 else Color.White)) {
                Text(if (waitingPickupRelease) blockedLabel else nextLabel, fontWeight = FontWeight.Black, fontFamily = AppFont)
            }
        }
        OutlinedButton(onClick = onProblem, enabled = problemEnabled, modifier = Modifier.fillMaxWidth().height(44.dp), shape = RoundedCornerShape(18.dp)) {
            Icon(Icons.Filled.ErrorOutline, contentDescription = null, tint = if (problemEnabled) Warning else Muted2, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(8.dp))
            Text(problemLabel, color = if (problemEnabled) Warning else Muted2, fontWeight = FontWeight.Black, fontFamily = AppFont, fontSize = 13.sp)
        }
    }
}

@Composable
private fun CompactNotice(icon: ImageVector, title: String, message: String, color: Color) {
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(color.copy(alpha = .08f)).border(1.dp, color.copy(alpha = .18f), RoundedCornerShape(18.dp)).padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(title, color = Ink, fontSize = 13.sp, fontWeight = FontWeight.Black, fontFamily = AppFont)
            Text(message, color = Muted, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, fontFamily = AppFont)
        }
    }
}

@Composable
private fun PaymentOperationPanel(ride: DriverRide, compact: Boolean) {
    val method = ride.paymentMethod.trim()
    val status = ride.paymentStatus.trim()
    val methodUpper = method.uppercase(Locale.ROOT)
    val statusUpper = status.uppercase(Locale.ROOT)
    val amount = ride.amountToCollectNumber
    val paidOnline = statusUpper.contains("PAGO") || methodUpper.contains("ONLINE") || methodUpper.contains("APP") || methodUpper.contains("INFINITE")
    val isCash = methodUpper.contains("DINHEIRO")
    val isCard = methodUpper.contains("CART") || methodUpper.contains("MAQUIN") || ride.requiresMachine
    val isPix = methodUpper.contains("PIX")
    val accent = when {
        paidOnline -> Lime
        isCard -> Blue
        isCash -> Warning
        isPix -> LimeDark
        else -> Muted2
    }
    val mainChip = when {
        method.isBlank() && status.isBlank() -> "Não informado"
        paidOnline -> "Pago online"
        isCash -> "Dinheiro"
        isCard -> "Maquininha"
        isPix -> "Pix"
        else -> method.ifBlank { status }.take(18)
    }
    val amountChip = when {
        paidOnline -> "Nada a cobrar"
        amount > 0.0 && isCard -> "Passar ${DriverRepository.formatCurrency(amount)}"
        amount > 0.0 -> "Receber ${DriverRepository.formatCurrency(amount)}"
        ride.clientTotalNumber > 0.0 -> "Pedido ${DriverRepository.formatCurrency(ride.clientTotalNumber)}"
        else -> "A conferir"
    }

    if (compact) {
        Row(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .background(accent.copy(alpha = .07f))
                .border(1.dp, accent.copy(alpha = .16f), RoundedCornerShape(22.dp))
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(if (isCard) Icons.Filled.CreditCard else Icons.Filled.Payments, contentDescription = null, tint = accent, modifier = Modifier.size(19.dp))
            Spacer(Modifier.width(9.dp))
            Column(Modifier.weight(1f)) {
                Text("Pagamento", color = Ink, fontSize = 12.sp, fontWeight = FontWeight.Black, fontFamily = AppFont)
                Text("$mainChip • $amountChip", color = accent, fontSize = 12.sp, fontWeight = FontWeight.Black, fontFamily = AppFont, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            if (isCash && ride.changeForNumber > 0.0) {
                StatusPill("Troco", false)
            }
        }
    } else {
        GlassCard(padding = 14, borderColor = accent.copy(alpha = .22f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(if (isCard) Icons.Filled.CreditCard else Icons.Filled.Payments, contentDescription = null, tint = accent, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Pagamento", color = Ink, fontSize = 14.sp, fontWeight = FontWeight.Black, fontFamily = AppFont, modifier = Modifier.weight(1f))
            }
            Spacer(Modifier.height(10.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PaymentChip(mainChip, accent, Modifier.weight(1f))
                    PaymentChip(amountChip, accent, Modifier.weight(1.25f))
                }
                if (isCash && ride.changeForNumber > 0.0) {
                    PaymentChip("Troco ${DriverRepository.formatCurrency(ride.changeForNumber)}", Warning, Modifier.fillMaxWidth())
                }
                if (method.isBlank() && status.isBlank()) {
                    Text("Confirme na coleta antes de sair.", color = Muted, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, fontFamily = AppFont)
                }
            }
        }
    }
}

@Composable
private fun PaymentChip(text: String, color: Color, modifier: Modifier = Modifier) {
    Box(modifier.clip(RoundedCornerShape(999.dp)).background(color.copy(alpha = .10f)).border(1.dp, color.copy(alpha = .20f), RoundedCornerShape(999.dp)).padding(horizontal = 12.dp, vertical = 9.dp), contentAlignment = Alignment.Center) {
        Text(text, color = color, fontSize = 12.sp, fontWeight = FontWeight.Black, fontFamily = AppFont, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun DeliveryCodeAndPaymentGate(ride: DriverRide, paymentMachines: List<PaymentMachine>, onConfirm: (PaymentSettlementInput) -> Unit) {
    var code by remember(ride.id) { mutableStateOf("") }
    val expected = ride.deliveryCode.filter { it.isDigit() }
    val typed = code.filter { it.isDigit() }
    val hasExpected = expected.length == 4
    val validCode = typed == "48" || (hasExpected && typed.length == 4 && typed == expected)

    val methodUpper = ride.paymentMethod.uppercase(Locale.ROOT)
    val statusUpper = ride.paymentStatus.uppercase(Locale.ROOT)
    val paidOnline = statusUpper.contains("PAGO") || methodUpper.contains("ONLINE") || methodUpper.contains("APP") || methodUpper.contains("INFINITE")
    val defaultMethod = when {
        paidOnline -> "PAGO_ONLINE"
        methodUpper.contains("DINHEIRO") -> "DINHEIRO"
        methodUpper.contains("PIX") -> "PIX"
        methodUpper.contains("CART") || methodUpper.contains("MAQUIN") || ride.requiresMachine -> "MAQUININHA"
        else -> ""
    }
    var paymentMethod by remember(ride.id) { mutableStateOf(defaultMethod) }
    var paymentConfirmed by remember(ride.id, defaultMethod) { mutableStateOf(defaultMethod == "PAGO_ONLINE") }
    var transactionType by remember(ride.id) { mutableStateOf(if (defaultMethod == "MAQUININHA") "DEBITO" else "") }
    var selectedMachineId by remember(ride.id, paymentMachines.size) { mutableStateOf(paymentMachines.firstOrNull()?.id.orEmpty()) }

    val needsPaymentChoice = !paidOnline
    val needsMachine = paymentMethod == "MAQUININHA"
    val machineOk = !needsMachine || transactionType.isNotBlank()
    val paymentOk = !needsPaymentChoice || (paymentMethod.isNotBlank() && paymentConfirmed && machineOk)
    val readyToFinish = validCode && paymentOk
    val orderTotal = when {
        ride.amountToCollectNumber > 0.0 -> ride.amountToCollectNumber
        ride.clientTotalNumber > 0.0 -> ride.clientTotalNumber
        else -> 0.0
    }
    val selectedMachine = paymentMachines.firstOrNull { it.id == selectedMachineId }

    val message = when {
        typed == "48" -> "Código secreto 48 aceito pela operação."
        validCode -> "Código de entrega confirmado."
        hasExpected -> "Peça ao cliente o código de 4 dígitos."
        else -> "Código não informado no pedido. Use 48 somente se a operação autorizar."
    }

    GlassCard(padding = 16, borderColor = if (readyToFinish) Lime.copy(alpha = .35f) else Warning.copy(alpha = .35f)) {
        Text("Confirmação da entrega", color = Ink, fontSize = 17.sp, fontWeight = FontWeight.Black, fontFamily = AppFont)
        Spacer(Modifier.height(6.dp))
        Text("A entrega só finaliza depois do código e do pagamento tratado.", color = Muted, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, fontFamily = AppFont)
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = code,
            onValueChange = { value -> code = value.filter { it.isDigit() }.take(4) },
            label = { Text("Código de entrega") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        Text(message, color = if (validCode) Lime else Muted, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = AppFont)

        Spacer(Modifier.height(14.dp))
        PaymentOperationPanel(ride, compact = true)
        Spacer(Modifier.height(12.dp))
        if (paidOnline) {
            StatusMessage("Pagamento online: nada a cobrar do cliente.", false)
        } else {
            Text("Como o cliente pagou?", color = Ink, fontSize = 13.sp, fontWeight = FontWeight.Black, fontFamily = AppFont)
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                TinyChip("Dinheiro", paymentMethod == "DINHEIRO", Modifier.weight(1f)) { paymentMethod = "DINHEIRO"; paymentConfirmed = false }
                TinyChip("Pix", paymentMethod == "PIX", Modifier.weight(1f)) { paymentMethod = "PIX"; paymentConfirmed = false }
                TinyChip("Cartão", paymentMethod == "MAQUININHA", Modifier.weight(1f)) { paymentMethod = "MAQUININHA"; paymentConfirmed = false }
            }
            if (paymentMethod == "MAQUININHA") {
                Spacer(Modifier.height(10.dp))
                Text("Maquininha usada", color = Ink, fontSize = 12.sp, fontWeight = FontWeight.Black, fontFamily = AppFont)
                Spacer(Modifier.height(6.dp))
                if (paymentMachines.isNotEmpty()) {
                    paymentMachines.take(3).forEach { machine ->
                        TinyChip(machine.name.take(18), selectedMachineId == machine.id, Modifier.fillMaxWidth()) { selectedMachineId = machine.id }
                        Spacer(Modifier.height(4.dp))
                    }
                } else {
                    Text("Nenhuma maquininha cadastrada no gestor. Será salvo para conferência manual.", color = Warning, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = AppFont)
                }
                Spacer(Modifier.height(6.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("DEBITO", "CREDITO", "PARCELADO", "TICKET").forEach { type ->
                        TinyChip(type.lowercase(Locale.ROOT).replaceFirstChar { it.uppercase() }, transactionType == type, Modifier.weight(1f)) { transactionType = type }
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
            OutlinedButton(
                onClick = { paymentConfirmed = true },
                enabled = paymentMethod.isNotBlank() && machineOk,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(if (paymentConfirmed) "Pagamento confirmado" else "Confirmar pagamento recebido", color = if (paymentConfirmed) Lime else Ink, fontWeight = FontWeight.Black, fontFamily = AppFont)
            }
        }

        Spacer(Modifier.height(12.dp))
        val confirmInput = PaymentSettlementInput(
            rideId = ride.id,
            orderTotal = orderTotal,
            driverFee = ride.valueNumber,
            paymentMethod = if (paidOnline) "PAGO_ONLINE" else paymentMethod.ifBlank { "NAO_INFORMADO" },
            transactionType = transactionType,
            machineId = selectedMachine?.id.orEmpty(),
            machineName = selectedMachine?.name.orEmpty(),
            receivedByDriver = !paidOnline && paymentMethod in setOf("DINHEIRO", "MAQUININHA"),
            receivedBy = if (!paidOnline && paymentMethod in setOf("DINHEIRO", "MAQUININHA")) "ENTREGADOR" else "SISTEMA",
            note = "codigoDigitado=$typed; codigoEsperadoInformado=$hasExpected; codigoSecretoUsado=${typed == "48"}"
        )
        PrimaryButton("Finalizar entrega", enabled = readyToFinish) { onConfirm(confirmInput) }
        if (!readyToFinish) {
            Spacer(Modifier.height(6.dp))
            Text("Falta confirmar código e/ou pagamento. Se houver problema, registre ocorrência antes de encerrar.", color = Danger, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, fontFamily = AppFont)
        }
    }
}

@Composable
private fun RideFinancialPanel(ride: DriverRide, compact: Boolean) {
    var expanded by remember(ride.id) { mutableStateOf(false) }
    val receivedByDriver = ride.receivedBy.uppercase().contains("ENTREGADOR") || ride.receivedBy.uppercase().contains("MOTOBOY") || ride.receivedBy.uppercase().contains("DRIVER")
    GlassCard(padding = 0) {
        Row(
            Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(horizontal = 14.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Filled.AccountBalanceWallet, contentDescription = null, tint = Lime, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text("Financeiro da rota", color = Ink, fontSize = 14.sp, fontWeight = FontWeight.Black, fontFamily = AppFont)
                Text(if (expanded) "Detalhes do acerto" else "Toque para ver repasse e acerto", color = Muted2, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, fontFamily = AppFont)
            }
            Icon(if (expanded) Icons.Filled.ExpandLess else Icons.Filled.KeyboardArrowRight, contentDescription = null, tint = Muted2, modifier = Modifier.size(22.dp))
        }
        if (expanded) {
            Divider(color = BorderSoft)
            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MiniStat("Seu repasse", ride.value.ifBlank { "A definir" }, Modifier.weight(1f))
                    MiniStat("Pedido", moneyOrDash(ride.clientTotalNumber), Modifier.weight(1f))
                }
                if (receivedByDriver || ride.amountToCollectNumber > 0.0) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        MiniStat("Receber", moneyOrDash(ride.amountToCollectNumber), Modifier.weight(1f))
                        MiniStat("Repassar loja", moneyOrDash(ride.storeReturnNumber), Modifier.weight(1f))
                    }
                    if (ride.machineFeeNumber > 0.0) {
                        Text("Taxa maquininha: ${moneyOrDash(ride.machineFeeNumber)}", color = Warning, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = AppFont)
                    }
                }
            }
        }
    }
}


@Composable
private fun RouteOrdersPanel(ride: DriverRide) {
    val orders = ride.routeOrders
    val total = ride.routeOrderCount.coerceAtLeast(if (orders.isNotEmpty()) orders.size else 1)
    val ready = ride.routeReadyCount.coerceAtLeast(orders.count { it.ready }).coerceAtMost(total)
    val shouldShowFallback = total <= 1 && orders.isEmpty()
    GlassCard(padding = 14) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(38.dp).clip(CircleShape).background(Lime.copy(alpha = .10f)), contentAlignment = Alignment.Center) {
                Icon(Icons.Filled.ReceiptLong, contentDescription = null, tint = Lime, modifier = Modifier.size(19.dp))
            }
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text("Pedidos da rota", color = Ink, fontSize = 15.sp, fontWeight = FontWeight.Black, fontFamily = AppFont)
                Text("$ready de $total prontos", color = Muted, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, fontFamily = AppFont)
            }
            StatusPill(if (ride.pickupReleaseAllowed) "Liberada" else if (ready >= total) "Pronto" else "Aguardando", ride.pickupReleaseAllowed || ready >= total)
        }
        Spacer(Modifier.height(8.dp))
        if (orders.isEmpty()) {
            CompactOrderFallback(ride, shouldShowFallback)
        } else {
            orders.forEachIndexed { index, order ->
                RouteOrderRow(order, fallbackPaymentSummary = ride.paymentSummaryForUi())
                if (index != orders.lastIndex) Divider(color = BorderSoft)
            }
        }
    }
}

@Composable
private fun CompactOrderFallback(ride: DriverRide, single: Boolean) {
    val code = ride.orderCode.ifBlank { ride.id.takeLast(4).uppercase(Locale.ROOT) }
    val payment = when (ride.paymentKind()) {
        "MAQUININHA" -> "Cartão/maquininha"
        "DINHEIRO" -> if (ride.changeForNumber > 0.0) "Dinheiro • Troco ${DriverRepository.formatCurrency(ride.changeForNumber)}" else "Dinheiro"
        "ONLINE" -> "Pago online"
        "PIX" -> "Pix"
        else -> ride.paymentMethod.ifBlank { "Pagamento a conferir" }
    }
    Row(Modifier.fillMaxWidth().padding(vertical = 9.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = Lime, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text("#$code ${ride.customerName.ifBlank { "Cliente" }}", color = Ink, fontSize = 13.sp, fontWeight = FontWeight.Black, fontFamily = AppFont, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(listOf(payment, moneyOrDash(ride.clientTotalNumber).takeIf { it != "—" }).filterNotNull().joinToString(" • "), color = Muted, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, fontFamily = AppFont, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        StatusPill(if (single) "Pronto" else "Rota", true)
    }
}

@Composable
private fun RouteOrderRow(order: com.rodriguesacai.entregador.data.RouteOrder, fallbackPaymentSummary: String = "") {
    Row(Modifier.fillMaxWidth().padding(vertical = 9.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(if (order.ready) Icons.Filled.CheckCircle else Icons.Filled.Schedule, contentDescription = null, tint = if (order.ready) Lime else Warning, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text("#${order.code.ifBlank { order.id.takeLast(4).uppercase(Locale.ROOT) }} ${order.customerName.ifBlank { "Cliente" }}", color = Ink, fontSize = 13.sp, fontWeight = FontWeight.Black, fontFamily = AppFont, maxLines = 1, overflow = TextOverflow.Ellipsis)
            val paymentLine = order.paymentSummary.takeUnless { it.isBlank() || it.contains("não informado", ignoreCase = true) } ?: fallbackPaymentSummary.ifBlank { "Pagamento a conferir" }
            Text(paymentLine, color = Muted, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, fontFamily = AppFont, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        StatusPill(if (order.ready) "Pronto" else order.status.ifBlank { "Preparo" }, order.ready)
    }
}

private fun moneyOrDash(value: Double): String = if (value > 0.0) DriverRepository.formatCurrency(value) else "—"

private fun DriverRide.paymentSummaryForUi(): String {
    return when (paymentKind()) {
        "MAQUININHA" -> if (amountToCollectNumber > 0.0) "Maquininha • ${DriverRepository.formatCurrency(amountToCollectNumber)}" else "Maquininha"
        "DINHEIRO" -> if (changeForNumber > 0.0) "Dinheiro • Troco ${DriverRepository.formatCurrency(changeForNumber)}" else "Dinheiro"
        "ONLINE" -> "Pago online"
        "PIX" -> if (amountToCollectNumber > 0.0) "Pix • ${DriverRepository.formatCurrency(amountToCollectNumber)}" else "Pix"
        else -> "Pagamento a conferir"
    }
}


@Composable
private fun RidesContent(
    pendingRide: DriverRide?,
    activeRide: DriverRide?,
    paymentMachines: List<PaymentMachine>,
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
        activeRide != null -> "Rota atual e próxima ação."
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

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (activeRide == null) {
            GlassCard(padding = 16) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(46.dp).clip(CircleShape).background(stateColor.copy(alpha = .12f)), contentAlignment = Alignment.Center) {
                        Icon(if (pendingRide != null && online) Icons.Filled.NotificationsActive else Icons.Filled.TwoWheeler, contentDescription = null, tint = stateColor, modifier = Modifier.size(24.dp))
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Corridas", color = Ink, fontSize = 24.sp, fontWeight = FontWeight.Black, fontFamily = AppFont)
                        Text(stateTitle, color = stateColor, fontSize = 13.sp, fontWeight = FontWeight.Black, fontFamily = AppFont)
                    }
                    StatusPill(if (pendingRide != null && online) "Nova" else if (online) "Livre" else "Off", online)
                }
                Spacer(Modifier.height(8.dp))
                Text(stateMessage, color = Muted, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, fontFamily = AppFont)
            }
        }

        when {
            activeRide != null -> ActiveRideCard(activeRide, pendingRide?.takeIf { it.isRouteAddition }, onOpenNavigator, onUpdateRide, paymentMachines)
            pendingRide != null && online -> IncomingRideCard(pendingRide, onAccept, onReject, onExpire)
            online -> {
                WaitingCard(OperationalStatus(AvailabilityKind.Disponivel, "Disponível", "Disponível para receber pedidos", Lime, Color(0xFF10200A), true))
                RidesEmptyGuide("Você está livre", "Mantenha localização, internet e bateria liberadas para não perder a próxima oferta.")
            }
            else -> {
                OfflineCard(OperationalStatus(AvailabilityKind.Indisponivel, "Indisponível", "Fique disponível para receber corridas", Color(0xFF232129), Ink, true))
                RidesEmptyGuide("Nenhuma corrida", "Fique disponível na Home para receber ofertas.")
            }
        }
    }
}

@Composable
private fun DriverMapContent(
    activeRide: DriverRide?,
    online: Boolean,
    onBackHome: () -> Unit,
    onOpenNavigator: (pickup: String, dropoff: String) -> Unit,
    onUpdateRide: (DriverRide, String) -> Unit,
    paymentMachines: List<PaymentMachine>
) {
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(1.dp, BorderSoft, CircleShape)
                    .clickable { onBackHome() },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.ArrowBack, contentDescription = null, tint = Ink, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(if (activeRide == null) "Mapa" else "Mapa da rota", color = Ink, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, fontFamily = AppFont)
                Text(if (activeRide == null) "((•)) ${if (online) "Disponível" else "Indisponível"}" else "Corrida em andamento", color = if (online || activeRide != null) Lime else Muted, fontSize = 13.sp, fontWeight = FontWeight.Black, fontFamily = AppFont)
            }
        }

        if (activeRide == null) {
            GlassCard(padding = 0, borderColor = Color(0xFFE5EAE4)) {
                Box {
                    RealDeliveryMap(
                        title = "Sua região",
                        subtitle = if (online) "((•)) Disponível" else "Indisponível",
                        pickupAddress = "",
                        dropoffAddress = "",
                        mode = DeliveryMapMode.DRIVER_TO_PICKUP,
                        modifier = Modifier.height(560.dp)
                    )
                    Box(
                        Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 14.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .background(Color.White.copy(alpha = .92f))
                            .border(1.dp, BorderSoft, RoundedCornerShape(999.dp))
                            .padding(horizontal = 16.dp, vertical = 9.dp)
                    ) {
                        Text("((•)) ${if (online) "Disponível" else "Indisponível"}", color = if (online) LimeDark else Muted, fontSize = 13.sp, fontWeight = FontWeight.Black, fontFamily = AppFont)
                    }
                }
            }
            Text("O mapa usa a localização do próprio celular para mostrar sua posição. O Firebase só é necessário para a operação acompanhar quando houver corrida.", color = Muted, fontSize = 12.sp, lineHeight = 17.sp, fontWeight = FontWeight.SemiBold, fontFamily = AppFont)
        } else {
            ActiveRideCard(activeRide, null, onOpenNavigator, onUpdateRide, paymentMachines)
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
private fun EarningsContent(profile: DriverProfile, stats: DriverStats, history: List<DriverHistory>, onEditBank: () -> Unit) {
    val context = LocalContext.current
    var visible by remember { mutableStateOf(!AppSettings.getHideValues(context)) }
    val receivedByDriver = stats.recebidoPeloEntregador
        ?: listOf(stats.dinheiroRecebido, stats.cartaoRecebido, stats.pixRecebido).filterNotNull().sum().takeIf { it > 0.0 }
        ?: 0.0
    val driverFee = stats.taxaMotoboy ?: stats.totalToday
    val cardFees = stats.taxasMaquininha ?: 0.0
    val amountToRepay = stats.valorARepassar ?: 0.0
    val amountToReceive = stats.valorAReceber ?: stats.totalAReceber ?: 0.0
    val settlementMode = when {
        amountToRepay > 0.0 -> "repay"
        amountToReceive > 0.0 -> "receive"
        else -> "ok"
    }
    val settlementTitle = when (settlementMode) {
        "repay" -> "Você deve repassar"
        "receive" -> "Você tem a receber"
        else -> "Tudo certo por enquanto"
    }
    val settlementValue = when (settlementMode) {
        "repay" -> amountToRepay
        "receive" -> amountToReceive
        else -> 0.0
    }
    val payoutRows = remember(stats) { stats.payoutRows.take(3) }
    val pixKey = remember(stats, profile) { stats.pixKey.ifBlank { profile.pixKey } }
    val bankName = remember(stats, profile) { stats.bankName.ifBlank { profile.bankName } }
    var showWalletInfo by remember { mutableStateOf(false) }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        WalletTopBar(onInfoClick = { showWalletInfo = !showWalletInfo })

        if (showWalletInfo) {
            WalletInfoCard()
        }

        WalletSettlementHero(
            label = "Acerto de hoje",
            title = settlementTitle,
            value = DriverRepository.formatCurrency(settlementValue),
            visible = visible,
            mode = settlementMode,
            onToggle = {
                visible = !visible
                AppSettings.setHideValues(context, !visible)
            }
        )

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            WalletSmallAmountCard(
                label = "Recebido por você",
                value = DriverRepository.formatCurrency(receivedByDriver),
                visible = visible,
                modifier = Modifier.weight(1f)
            )
            WalletSmallAmountCard(
                label = "Sua taxa",
                value = DriverRepository.formatCurrency(driverFee),
                visible = visible,
                modifier = Modifier.weight(1f)
            )
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            WalletSmallAmountCard(
                label = "Taxas de cartão",
                value = DriverRepository.formatCurrency(cardFees),
                visible = visible,
                modifier = Modifier.weight(1f)
            )
            WalletSmallAmountCard(
                label = "A repassar",
                value = DriverRepository.formatCurrency(amountToRepay),
                visible = visible,
                modifier = Modifier.weight(1f)
            )
        }

        PaymentBreakdownCard(
            cash = stats.dinheiroRecebido ?: 0.0,
            card = stats.cartaoRecebido ?: 0.0,
            pix = stats.pixRecebido ?: 0.0,
            visible = visible
        )

        NextPayoutCard(
            title = stats.proximoRepasseLabel,
            description = stats.proximoRepasseDescricao
        )

        PixSummaryCard(
            pixKey = pixKey,
            bankName = bankName,
            verified = stats.pixVerificada,
            visible = visible
        )

        LastPayoutsSection(
            rows = payoutRows,
            visible = visible
        )

        Button(
            onClick = onEditBank,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Lime)
        ) {
            Text("Atualizar dados bancários", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, fontFamily = AppFont)
        }

        Text(
            "Na finalização da entrega, o app deve registrar se o cliente pagou em dinheiro, Pix ou maquininha. O gestor alimenta taxas e acertos; esta tela já fica pronta para ouvir esses dados.",
            color = Muted,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
            fontFamily = AppFont
        )
    }
}

@Composable
private fun WalletTopBar(onInfoClick: () -> Unit) {
    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Text(
            "Carteira",
            color = Ink,
            fontSize = WalletType.title,
            fontWeight = FontWeight.ExtraBold,
            fontFamily = AppFont,
            textAlign = TextAlign.Center
        )
        Box(
            Modifier
                .align(Alignment.CenterEnd)
                .size(38.dp)
                .clip(CircleShape)
                .background(Color.White)
                .border(1.dp, BorderSoft, CircleShape)
                .clickable { onInfoClick() },
            contentAlignment = Alignment.Center
        ) {
            Text("i", color = Ink, fontSize = 17.sp, fontWeight = FontWeight.Bold, fontFamily = AppFont)
        }
    }
}

@Composable
private fun WalletInfoCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF1FAF1)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Como funciona o acerto", color = Ink, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, fontFamily = AppFont)
            Text("Dinheiro recebido pelo motoboy não é todo do motoboy. O app separa sua taxa, taxas de maquininha e valor a repassar para a operação.", color = Muted, fontSize = 12.sp, fontWeight = FontWeight.Medium, fontFamily = AppFont)
            Text("Exemplo: pedido R$ 100, taxa do entregador R$ 6 e débito com 2% de taxa = R$ 92 a repassar.", color = Muted, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, fontFamily = AppFont)
        }
    }
}

@Composable
private fun WalletSettlementHero(label: String, title: String, value: String, visible: Boolean, mode: String, onToggle: () -> Unit) {
    val colors = when (mode) {
        "repay" -> listOf(Color(0xFF7A3B00), Color(0xFFFF8A00))
        "receive" -> listOf(Color(0xFF005D25), Color(0xFF008D35))
        else -> listOf(Color(0xFF005D25), Color(0xFF008D35))
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = LimeDark),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(Modifier.background(Brush.horizontalGradient(colors))) {
            Row(Modifier.fillMaxWidth().padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(label, color = Color.White.copy(alpha = .90f), fontSize = WalletType.bodyStrong, fontWeight = FontWeight.Medium, fontFamily = AppFont)
                    Spacer(Modifier.height(4.dp))
                    Text(title, color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.ExtraBold, fontFamily = AppFont, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        if (visible) value else "R$ •••••",
                        color = Color.White,
                        fontSize = 34.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = AppFont,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Box(
                    Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = .13f))
                        .clickable { onToggle() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        if (visible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                        contentDescription = if (visible) "Ocultar valores" else "Mostrar valores",
                        tint = Color.White,
                        modifier = Modifier.size(25.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun WalletSmallAmountCard(label: String, value: String, visible: Boolean, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 13.dp), verticalArrangement = Arrangement.Center) {
            Text(label, color = Muted, fontSize = 12.sp, fontWeight = FontWeight.Medium, fontFamily = AppFont, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(8.dp))
            Text(
                if (visible) value else "R$ •••••",
                color = Ink,
                fontSize = 19.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = AppFont,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun PaymentBreakdownCard(cash: Double, card: Double, pix: Double, visible: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Formas recebidas", color = Ink, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, fontFamily = AppFont)
            WalletBreakdownLine("Dinheiro", DriverRepository.formatCurrency(cash), visible)
            WalletBreakdownLine("Maquininha/cartão", DriverRepository.formatCurrency(card), visible)
            WalletBreakdownLine("Pix", DriverRepository.formatCurrency(pix), visible)
            Text("Se o pagamento for maquininha, a taxa cadastrada no gestor desconta automaticamente do acerto.", color = Muted, fontSize = 11.sp, fontWeight = FontWeight.Medium, fontFamily = AppFont)
        }
    }
}

@Composable
private fun WalletBreakdownLine(label: String, value: String, visible: Boolean) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(label, color = Muted, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, fontFamily = AppFont, modifier = Modifier.weight(1f))
        Text(if (visible) value else "R$ •••••", color = Ink, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, fontFamily = AppFont)
    }
}

@Composable
private fun NextPayoutCard(title: String, description: String) {
    val cleanTitle = title.ifBlank { "A definir" }
    val cleanDescription = when {
        description.isBlank() -> "Aguardando programação do gestor"
        description == cleanTitle -> "Aguardando programação do gestor"
        cleanTitle == "A definir" && description == "A definir" -> "Aguardando programação do gestor"
        else -> description
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF1FAF1)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(46.dp).clip(RoundedCornerShape(14.dp)).background(Color.White), contentAlignment = Alignment.Center) {
                Icon(Icons.Filled.Schedule, contentDescription = null, tint = Lime, modifier = Modifier.size(25.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text("Próximo acerto", color = LimeDark, fontSize = WalletType.label, fontWeight = FontWeight.SemiBold, fontFamily = AppFont)
                Text(cleanTitle, color = Ink, fontSize = 19.sp, fontWeight = FontWeight.ExtraBold, fontFamily = AppFont)
                Text(cleanDescription, color = Muted, fontSize = WalletType.body, fontWeight = FontWeight.Medium, fontFamily = AppFont)
            }
            Icon(Icons.Filled.KeyboardArrowRight, contentDescription = null, tint = Muted2)
        }
    }
}

@Composable
private fun PixSummaryCard(pixKey: String, bankName: String, verified: Boolean, visible: Boolean) {
    val hasPix = pixKey.isNotBlank()
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(48.dp).clip(RoundedCornerShape(16.dp)).background(Color(0xFFEAF7EE)), contentAlignment = Alignment.Center) {
                Icon(Icons.Filled.AccountBalanceWallet, contentDescription = null, tint = Lime, modifier = Modifier.size(26.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(if (hasPix) "Chave Pix cadastrada" else "Chave Pix não cadastrada", color = Muted, fontSize = WalletType.label, fontWeight = FontWeight.Medium, fontFamily = AppFont)
                Text(
                    when {
                        !visible -> "••••••••"
                        hasPix -> pixKey
                        bankName.isNotBlank() -> bankName
                        else -> "Toque em Mais > Pix/banco"
                    },
                    color = Ink,
                    fontSize = WalletType.cardTitle,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = AppFont,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (hasPix && verified) {
                    Spacer(Modifier.height(6.dp))
                    StatusPill("Verificada", true)
                }
            }
            Icon(Icons.Filled.KeyboardArrowRight, contentDescription = null, tint = Muted2)
        }
    }
}

@Composable
private fun LastPayoutsSection(rows: List<DriverPayout>, visible: Boolean) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("Últimos repasses", color = Ink, fontSize = 19.sp, fontWeight = FontWeight.ExtraBold, fontFamily = AppFont, modifier = Modifier.weight(1f))
            Text("Histórico", color = Muted, fontSize = WalletType.label, fontWeight = FontWeight.SemiBold, fontFamily = AppFont)
        }
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            if (rows.isEmpty()) {
                Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(42.dp).clip(CircleShape).background(Color(0xFFEAF7EE)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Filled.ReceiptLong, contentDescription = null, tint = Lime, modifier = Modifier.size(22.dp))
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("Nenhum repasse encontrado", color = Ink, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, fontFamily = AppFont)
                        Text("Quando o gestor registrar pagamentos, eles aparecerão aqui.", color = Muted, fontSize = WalletType.body, fontWeight = FontWeight.Medium, fontFamily = AppFont)
                    }
                }
            } else {
                rows.forEachIndexed { index, item ->
                    WalletPayoutRow(item = item, visible = visible)
                    if (index < rows.lastIndex) Divider(color = Color(0xFFE9EEE8), thickness = 1.dp)
                }
            }
        }
    }
}

@Composable
private fun WalletPayoutRow(item: DriverPayout, visible: Boolean) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 13.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(item.createdLabel.ifBlank { "Hoje" }, color = Muted, fontSize = WalletType.label, fontWeight = FontWeight.Medium, fontFamily = AppFont)
            Text(item.method.ifBlank { "Repasse" }, color = Ink, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, fontFamily = AppFont)
        }
        Text(if (visible) item.valueLabel.ifBlank { "—" } else "R$ •••••", color = Ink, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, fontFamily = AppFont)
        Spacer(Modifier.width(12.dp))
        Text(item.statusLabel, color = if (item.statusLabel == "Pago") Lime else Warning, fontSize = 13.sp, fontWeight = FontWeight.Bold, fontFamily = AppFont)
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
                Box(
                    Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(1.dp, Color(0xFFE2E8E0), CircleShape)
                        .clickable {
                            val options = listOf("Todas", "Finalizadas", "Recusadas", "Expiradas")
                            val next = (options.indexOf(filter).takeIf { it >= 0 } ?: 0) + 1
                            filter = options[next % options.size]
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.FilterList, contentDescription = "Alternar filtro", tint = Lime, modifier = Modifier.size(22.dp))
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
                    Icon(item.historyIcon(), contentDescription = null, tint = accent, modifier = Modifier.size(22.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("#${item.rideId}", color = Ink, fontWeight = FontWeight.Black, fontSize = 18.sp, fontFamily = AppFont, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Spacer(Modifier.width(8.dp))
                        StatusPill(kind, kind == "Finalizada" || kind == "Aceita", Modifier)
                    }
                    Text(item.createdLabel.ifBlank { "Data não informada" }, color = Muted2, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, fontFamily = AppFont)
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
                HistoryDetailLine("Coleta", item.pickup.ifBlank { "Coleta não informada" }, Lime)
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
    appNotices: List<AppNotice>,
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
    var operationPrefs by remember { mutableStateOf(DriverRepository.loadOperationalPreferences(context)) }

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

            "operation" -> OperationPreferencesScreen(
                preferences = operationPrefs,
                onPreferences = { operationPrefs = it },
                onBack = { page = "main" },
                onSave = { prefs ->
                    loading = true
                    DriverRepository.saveOperationalPreferences(
                        context = context,
                        preferences = prefs,
                        onDone = { loading = false; message = "Preferências operacionais salvas." },
                        onError = { loading = false; localError = it }
                    )
                }
            )

            "notifications" -> NotificationsReferenceScreen(
                notices = appNotices,
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
    val context = LocalContext.current
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
            MoreMenuTile("Operação", "Maquininha, troco e horários", Icons.Filled.CreditCard, Modifier.weight(1f)) { onOpen("operation") }
            MoreMenuTile("Preferências", "Mapa e aparência", Icons.Filled.FilterList, Modifier.weight(1f)) { onOpen("settings") }
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
                    Icon(if (permissionStatus.ready) Icons.Filled.CheckCircle else Icons.Filled.ErrorOutline, contentDescription = null, tint = if (permissionStatus.ready) Lime else Warning, modifier = Modifier.size(22.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(if (permissionStatus.ready) "App pronto para corridas" else "Finalize as permissões", color = Ink, fontSize = 16.sp, fontWeight = FontWeight.Black, fontFamily = AppFont)
                    Text(if (permissionStatus.ready) "Alertas, localização e bateria estão configurados." else "Configure notificações, localização e bateria sem restrição.", color = Muted, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, fontFamily = AppFont)
                }
                Icon(Icons.Filled.KeyboardArrowRight, contentDescription = null, tint = Muted2, modifier = Modifier.clickable { onOpen("permissions") })
            }
        }

        GlassCard(padding = 14, borderColor = Warning.copy(alpha = .20f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Bolt, contentDescription = null, tint = Warning, modifier = Modifier.size(22.dp))
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text("Destravar operação", color = Ink, fontSize = 14.sp, fontWeight = FontWeight.Black, fontFamily = AppFont)
                    Text("Use se a corrida sumiu, foi cancelada ou o app ficou preso.", color = Muted, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, fontFamily = AppFont)
                }
            }
            Spacer(Modifier.height(10.dp))
            OutlinedButton(
                onClick = {
                    DriverRepository.forceClearActiveMission(
                        context = context,
                        reason = "DESTRAVAR_MANUAL_MAIS",
                        onDone = { Toast.makeText(context, "App destravado. Volte para Início.", Toast.LENGTH_SHORT).show() },
                        onError = { Toast.makeText(context, it.ifBlank { "Falha ao destravar." }, Toast.LENGTH_LONG).show() }
                    )
                },
                modifier = Modifier.fillMaxWidth().height(46.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Destravar agora", color = Warning, fontWeight = FontWeight.Black, fontFamily = AppFont)
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
            AccountDataLine(Icons.Filled.Place, "Cidade", profile.city.ifBlank { "Não informado" })
            AccountDataLine(Icons.Filled.TwoWheeler, "Veículo", profile.vehicle.ifBlank { "Não informado" })
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
private fun NotificationsReferenceScreen(notices: List<AppNotice>, onBack: () -> Unit) {
    var filter by remember { mutableStateOf("Todas") }
    val filtered = notices.filter { notice ->
        filter == "Todas" || notice.category.equals(filter, ignoreCase = true)
    }
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        ScreenTopTitle("Notificações", "Avisos enviados pela operação", onBack)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Todas", "Operação", "Sistema").forEach { item -> ModeButton(item, filter == item, Modifier.weight(1f)) { filter = item } }
        }
        if (filtered.isEmpty()) {
            GlassCard(padding = 24) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Box(Modifier.size(68.dp).clip(CircleShape).background(Color(0xFFEAF8EE)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Filled.Notifications, contentDescription = null, tint = Lime, modifier = Modifier.size(34.dp))
                    }
                    Spacer(Modifier.height(14.dp))
                    Text("Nenhuma notificação", color = Ink, fontSize = 20.sp, fontWeight = FontWeight.Black, fontFamily = AppFont, textAlign = TextAlign.Center)
                    Spacer(Modifier.height(6.dp))
                    Text("Quando o gestor enviar avisos, repasses ou comunicados, eles aparecerão aqui.", color = Muted, fontSize = 13.sp, lineHeight = 18.sp, fontWeight = FontWeight.SemiBold, fontFamily = AppFont, textAlign = TextAlign.Center)
                }
            }
        } else {
            filtered.forEach { notice ->
                val color = when (notice.priority.uppercase(Locale.ROOT)) {
                    "ALTA", "URGENTE", "HIGH" -> Warning
                    "SISTEMA", "INFO" -> Blue
                    else -> Lime
                }
                NotificationCard(notice.title, "${notice.createdLabel} • ${notice.message}", color)
            }
        }
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
private fun OperationPreferencesScreen(
    preferences: DriverOperationalPreferences,
    onPreferences: (DriverOperationalPreferences) -> Unit,
    onBack: () -> Unit,
    onSave: (DriverOperationalPreferences) -> Unit
) {
    var troco by remember(preferences.changeAvailableNumber) { mutableStateOf(if (preferences.changeAvailableNumber > 0.0) preferences.changeAvailableNumber.toInt().toString() else "") }
    fun update(next: DriverOperationalPreferences) { onPreferences(next) }
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        ScreenTopTitle("Operação", "Maquininha, troco e recebimentos", onBack)
        StatusInfoCard("Filtro inteligente", "Essas preferências ajudam o Gestor a não mandar pedido incompatível com sua operação.", Blue)
        GlassCard(padding = 18) {
            PreferenceSwitch("Tenho maquininha", "Permite receber pedidos de cartão/maquininha quando o Gestor enviar.", preferences.hasMachine) { update(preferences.copy(hasMachine = it)) }
            PreferenceSwitch("Aceito débito", "Transações de débito na maquininha.", preferences.acceptsDebit) { update(preferences.copy(acceptsDebit = it)) }
            PreferenceSwitch("Aceito crédito", "Transações de crédito na maquininha.", preferences.acceptsCredit) { update(preferences.copy(acceptsCredit = it)) }
            PreferenceSwitch("Aceito parcelado/ticket", "Use apenas se sua maquininha realmente aceitar.", preferences.acceptsInstallment || preferences.acceptsTicket) { update(preferences.copy(acceptsInstallment = it, acceptsTicket = it)) }
        }
        GlassCard(padding = 18) {
            PreferenceSwitch("Tenho troco", "Permite receber pedido em dinheiro com troco.", preferences.hasCashChange) { update(preferences.copy(hasCashChange = it)) }
            Spacer(Modifier.height(8.dp))
            AppField(troco, { value ->
                troco = value.filter { it.isDigit() }
                update(preferences.copy(changeAvailableNumber = troco.toDoubleOrNull() ?: 0.0))
            }, "Troco disponível em reais", keyboardType = KeyboardType.Number)
            Spacer(Modifier.height(10.dp))
            PreferenceSwitch("Somente pedidos pagos online", "Bloqueia dinheiro e maquininha enquanto estiver ativo.", preferences.onlyOnlinePaid) { update(preferences.copy(onlyOnlinePaid = it)) }
        }
        GlassCard(padding = 18) {
            SectionTitle("Restrição por horário", "Ideal para madrugada ou horários de risco.")
            PreferenceSwitch("00h às 06h sem dinheiro", "O app bloqueia ofertas em dinheiro nesse intervalo.", preferences.blockCashAtNight) { update(preferences.copy(blockCashAtNight = it, nightStartHour = 0, nightEndHour = 6)) }
            PreferenceSwitch("00h às 06h sem maquininha", "O app bloqueia cartão/maquininha nesse intervalo.", preferences.blockMachineAtNight) { update(preferences.copy(blockMachineAtNight = it, nightStartHour = 0, nightEndHour = 6)) }
        }
        PrimaryButton("Salvar preferências") { onSave(preferences.copy(changeAvailableNumber = troco.toDoubleOrNull() ?: 0.0)) }
    }
}

@Composable
private fun PreferenceSwitch(title: String, subtitle: String, checked: Boolean, onChecked: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(title, color = Ink, fontSize = 14.sp, fontWeight = FontWeight.Black, fontFamily = AppFont)
            Text(subtitle, color = Muted, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, fontFamily = AppFont, lineHeight = 15.sp)
        }
        Switch(checked = checked, onCheckedChange = onChecked)
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
        StatusInfoCard("Versão do app", "6.16.0 piloto seguro: destravamento, ocorrência real, cancelamento sincronizado e rota clara.", Blue)
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
                Icon(icon, contentDescription = null, tint = Lime, modifier = Modifier.size(22.dp))
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
                Icon(if (color == Warning) Icons.Filled.ErrorOutline else Icons.Filled.Notifications, contentDescription = null, tint = color, modifier = Modifier.size(22.dp))
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
    var operationPrefs by remember { mutableStateOf(DriverRepository.loadOperationalPreferences(context)) }

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
            SettingButton("Versão do app", "6.16.0 piloto seguro")
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
    var operationPrefs by remember { mutableStateOf(DriverRepository.loadOperationalPreferences(context)) }

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
            Text("Versão 6.16.0 • piloto seguro", color = Muted2, fontSize = 12.sp)
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
private fun GlassCard(padding: Int = 16, borderColor: Color = BorderSoft, content: @Composable ColumnScope.() -> Unit) {
    Card(
        shape = RoundedCornerShape(30.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp, pressedElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, borderColor, RoundedCornerShape(30.dp))
    ) {
        Column(Modifier.padding(padding.dp), content = content)
    }
}

@Composable
private fun SectionTitle(title: String, subtitle: String) {
    Text(title, color = Ink, fontSize = 24.sp, lineHeight = 26.sp, fontWeight = FontWeight.Black, fontFamily = AppFont)
    Text(subtitle, color = Muted, fontSize = 13.sp, lineHeight = 18.sp, fontWeight = FontWeight.SemiBold, fontFamily = AppFont)
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
        modifier = Modifier.fillMaxWidth().height(58.dp),
        shape = RoundedCornerShape(24.dp),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp, pressedElevation = 0.dp),
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
    Card(
        colors = CardDefaults.cardColors(containerColor = if (isError) Color(0xFFFFF1F3) else Color(0xFFF1FCF4)),
        shape = RoundedCornerShape(22.dp),
        modifier = Modifier.fillMaxWidth().border(1.dp, if (isError) Danger.copy(alpha = .20f) else Lime.copy(alpha = .22f), RoundedCornerShape(22.dp))
    ) {
        Text(text, color = if (isError) Danger else LimeDark, fontSize = 13.sp, fontWeight = FontWeight.Bold, fontFamily = AppFont, modifier = Modifier.padding(14.dp))
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
            Text(value.ifBlank { "A definir" }, color = Ink, fontSize = 17.sp, fontWeight = FontWeight.Black, maxLines = 1, overflow = TextOverflow.Ellipsis, fontFamily = AppFont)
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
            Text(value.ifBlank { "Não informado" }, color = Ink, fontSize = 15.sp, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis)
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
        return OperationalStatus(AvailabilityKind.EmEntrega, "Em entrega", "Corrida em andamento", Lime, Color.White, false)
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
        return OperationalStatus(AvailabilityKind.Indisponivel, "Indisponível", "Toque para ficar disponível", Color(0xFF232129), Color.White, true)
    }

    return OperationalStatus(AvailabilityKind.Disponivel, "Disponível", "Disponível para receber pedidos", Lime, Color.White, true)
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

private fun DriverHistory.shortRideLabel(): String {
    val source = rideId.ifBlank { id }
    if (source.isBlank()) return "#----"
    val clean = source.replace("pedido", "", ignoreCase = true).replace("corrida", "", ignoreCase = true).trim('-', '_', ' ')
    val compact = clean.takeLast(5).ifBlank { source.takeLast(5) }
    return if (compact.startsWith("#")) compact else "#$compact"
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
    equals("arrived_client", true) || equals("ENTREGADOR_NO_LOCAL", true) || equals("CHEGOU_CLIENTE", true) -> "No cliente"
    equals("occurrence", true) || contains("OCORR", true) || contains("PROBLEMA", true) -> "Ocorrência"
    equals("finished", true) -> "Finalizada"
    contains("REJEIT", true) -> "Recusada"
    contains("EXPIR", true) -> "Expirada"
    contains("CONCL", true) || contains("ENTREG", true) || contains("FINALIZ", true) -> "Finalizada"
    else -> replace('_', ' ').lowercase().replaceFirstChar { it.uppercase() }
}

private fun String.shortName(): String {
    val parts = trim().split(" ").filter { it.isNotBlank() }
    return when {
        parts.isEmpty() -> "entregador"
        else -> parts.first()
    }
}
