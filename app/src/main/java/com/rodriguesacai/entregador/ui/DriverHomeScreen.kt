package com.rodriguesacai.entregador.ui

import android.content.Context
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.BatteryManager
import android.widget.Toast
import java.util.Calendar
import java.util.Locale
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.rodriguesacai.entregador.AppSettings
import com.rodriguesacai.entregador.PermissionStatus
import com.rodriguesacai.entregador.PermissionStatusReader
import com.rodriguesacai.entregador.R
import com.rodriguesacai.entregador.RodriguesFonts
import com.rodriguesacai.entregador.data.AppCarouselBanner
import com.rodriguesacai.entregador.data.AppNotice
import com.rodriguesacai.entregador.data.AppRuntimeConfig
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
import com.rodriguesacai.entregador.data.RouteOrder
import com.rodriguesacai.entregador.service.NotificationHelper
import kotlinx.coroutines.delay

private enum class AppTab { Inicio, Corridas, Mapa, Ganhos, Historico, Conta, Notificacoes }
private enum class AvailabilityKind { Disponivel, Indisponivel, Restricao, EmEntrega }

private val AppFont = RodriguesFonts.Montserrat
private val Bg = Color(0xFFFFFFFF)
private val Surface = Color(0xFFFFFFFF)
private val SurfaceSoft = Color(0xFFF7FAFC)
private val Border = Color(0xFFE8EEF3)
private val Ink = Color(0xFF101216)
private val Muted = Color(0xFF677381)
private val Muted2 = Color(0xFF9AA6B2)
private val Green = Color(0xFF0FAE4B)
private val GreenDark = Color(0xFF07883E)
private val GreenSoft = Color(0xFFEAF8EF)
private val Orange = Color(0xFFFF7A00)
private val OrangeSoft = Color(0xFFFFF2E5)
private val Red = Color(0xFFEF233C)
private val RedSoft = Color(0xFFFFEAEE)
private val Blue = Color(0xFF2563EB)
private val BlueSoft = Color(0xFFEFF5FF)
private val CardShape = RoundedCornerShape(30.dp)
private val ButtonShape = RoundedCornerShape(22.dp)

private data class OperationalStatus(
    val kind: AvailabilityKind,
    val label: String,
    val message: String,
    val buttonColor: Color,
    val textColor: Color,
    val canGoOnline: Boolean
)

private data class PaymentUi(
    val label: String,
    val detail: String,
    val color: Color,
    val amountLabel: String,
    val requiresMachine: Boolean,
    val requiresChange: Boolean
)

private data class RouteAction(
    val title: String,
    val message: String,
    val button: String,
    val enabled: Boolean,
    val statusToSend: String,
    val accent: Color,
    val needsPaymentConfirmation: Boolean = false
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
    var lastActiveId by remember { mutableStateOf("") }

    DisposableEffect(profile?.id, online) {
        val pendingListener = if (profile != null && online) {
            DriverRepository.listenPendingRide(context, onRide = { ride ->
                pendingRide = if (activeRide != null && ride != null && !ride.isRouteAddition) null else ride
            }, onError = { error = it })
        } else null
        val activeListener = if (profile != null) {
            DriverRepository.listenMyActiveRide(context, onRide = { ride ->
                if (ride == null && lastActiveId.isNotBlank()) notice = "Rota encerrada pela operação. Você está livre para nova corrida."
                activeRide = ride
                if (ride != null) {
                    lastActiveId = ride.id
                    if (pendingRide != null && !pendingRide!!.isRouteAddition) pendingRide = null
                }
            }, onError = { error = it })
        } else null
        val historyListener = if (profile != null) DriverRepository.listenMyHistory(context, { history = it }, { error = it }) else null
        val statsListener = if (profile != null) DriverRepository.listenDailyStats(context, { stats = it }, { error = it }) else null
        val carouselListener = if (profile != null) DriverRepository.listenAppCarousel({ appBanners = it }, {}) else null
        val noticeListener = if (profile != null) DriverRepository.listenAppNotifications(context, { appNotices = it }, {}) else null
        val profileListener = if (profile != null) DriverRepository.listenDriverProfile(context, { profile = it }, {}) else null
        val machinesListener = if (profile != null) DriverRepository.listenMachineOptions({ paymentMachines = it }, {}) else null
        val appConfigListener = if (profile != null) DriverRepository.listenAppRuntimeConfig({ appRuntimeConfig = it }, {}) else null
        onDispose {
            pendingListener?.remove(); activeListener?.remove(); historyListener?.remove(); statsListener?.remove()
            carouselListener?.remove(); noticeListener?.remove(); profileListener?.remove(); machinesListener?.remove(); appConfigListener?.remove()
        }
    }

    LaunchedEffect(profile?.id) {
        if (profile != null) {
            bootingSession = true
            delay(650)
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
        val visible = appNotices.firstOrNull { it.isVisible() && !it.read }
        if (visible != null && visible.id != lastSystemNoticeId) {
            lastSystemNoticeId = visible.id
            NotificationHelper.appNoticeNotification(context, visible.id, visible.title, visible.message, visible.category)
        }
    }

    if (!welcomeDone) {
        PermissionsOnboardingScreen(
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
                error = ""; notice = ""; setLoading(true)
                DriverRepository.login(
                    context = context,
                    documentOrPhone = value,
                    password = password,
                    onSuccess = { profile = it; setLoading(false) },
                    onError = { error = it; setLoading(false) }
                )
            },
            onRegister = { request, setLoading ->
                error = ""; notice = ""; setLoading(true)
                DriverRepository.registerDriver(
                    request = request,
                    onSuccess = { notice = "Cadastro enviado. Aguarde aprovação do gestor."; setLoading(false) },
                    onError = { error = it; setLoading(false) }
                )
            }
        )
        return
    }

    if (profile?.needsPasswordSetup == true) {
        CreatePasswordScreen(profile = profile!!, onSaved = { profile = profile!!.copy(needsPasswordSetup = false) }, onLogout = { DriverRepository.logout(context) { profile = null } })
        return
    }

    if (bootingSession) {
        LoadingSessionSplash(profile = profile!!)
        return
    }

    if (appRuntimeConfig.maintenanceActive || appRuntimeConfig.forceUpdate) {
        SystemStateScreen(
            title = if (appRuntimeConfig.forceUpdate) "Atualização necessária" else "App em manutenção",
            message = if (appRuntimeConfig.forceUpdate) "Atualize o app para continuar operando com segurança." else appRuntimeConfig.maintenanceMessage.ifBlank { "A operação pausou o app temporariamente." },
            icon = if (appRuntimeConfig.forceUpdate) Icons.Filled.Bolt else Icons.Filled.Shield,
            color = if (appRuntimeConfig.forceUpdate) Orange else Blue
        )
        return
    }

    Scaffold(
        containerColor = Bg,
        bottomBar = {
            PremiumBottomBar(tab = tab, onTab = { tab = it })
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding).background(Bg)) {
            Column(Modifier.fillMaxSize()) {
                if (notice.isNotBlank()) InlineAppMessage(text = notice, color = Green, onClose = { notice = "" })
                if (error.isNotBlank()) InlineAppMessage(text = error, color = Red, onClose = { error = "" })
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
                        onToggleValues = { hideValues = !hideValues; AppSettings.setHideValues(context, hideValues) },
                        onToggleOnline = { checked ->
                            online = checked
                            DriverRepository.setOnline(context, checked)
                            if (checked) onGoOnline() else { pendingRide = null; onGoOffline() }
                        },
                        onAccept = { ride -> DriverRepository.acceptRide(context, ride.id, onDone = { pendingRide = null; tab = AppTab.Corridas }, onError = { error = it }) },
                        onReject = { ride, reason -> DriverRepository.rejectRide(context, ride.id, reason, onDone = { pendingRide = null }, onError = { error = it }) },
                        onExpire = { ride -> DriverRepository.expireRide(context, ride.id, onDone = { pendingRide = null }, onError = { error = it }) },
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
                        onAccept = { ride -> DriverRepository.acceptRide(context, ride.id, onDone = { pendingRide = null }, onError = { error = it }) },
                        onReject = { ride, reason -> DriverRepository.rejectRide(context, ride.id, reason, onDone = { pendingRide = null }, onError = { error = it }) },
                        onExpire = { ride -> DriverRepository.expireRide(context, ride.id, onDone = { pendingRide = null }, onError = { error = it }) },
                        onUpdateRide = { ride, status -> DriverRepository.updateRideStatus(context, ride.id, status, onDone = { notice = "Rota atualizada." }, onError = { error = it }) },
                        onOpenNavigator = onOpenNavigator,
                        onOccurrence = { ride, reason, details ->
                            DriverRepository.reportRideOccurrence(context, ride.id, reason, details, onDone = { notice = "Ocorrência enviada. Aguarde o gestor." }, onError = { error = it })
                        },
                        onSettleAndFinish = { ride, input ->
                            DriverRepository.savePaymentSettlementForRide(context, ride.id, input, onDone = {
                                DriverRepository.updateRideStatus(context, ride.id, "FINALIZADA", onDone = { notice = "Entrega finalizada." }, onError = { error = it })
                            }, onError = { error = it })
                        }
                    )
                    AppTab.Mapa -> RouteMapContent(
                        activeRide = activeRide,
                        online = online,
                        onBackHome = { tab = AppTab.Inicio },
                        onOpenNavigator = onOpenNavigator
                    )
                    AppTab.Ganhos -> WalletContent(profile = profile!!, stats = stats, history = history, hideValues = hideValues, onToggleValues = { hideValues = !hideValues; AppSettings.setHideValues(context, hideValues) })
                    AppTab.Historico -> HistoryContent(history = history)
                    AppTab.Notificacoes -> NotificationsScreen(notices = appNotices, onBack = { tab = AppTab.Inicio })
                    AppTab.Conta -> MoreContent(
                        profile = profile!!,
                        online = online,
                        themeMode = themeMode,
                        hideValues = hideValues,
                        notices = appNotices,
                        onThemeChanged = { themeMode = it; AppSettings.setThemeMode(context, it) },
                        onToggleValues = { hideValues = !hideValues; AppSettings.setHideValues(context, hideValues) },
                        onProfileChanged = { profile = DriverRepository.currentSession(context) ?: profile },
                        onOpenNotificationSettings = onOpenNotificationSettings,
                        onOpenLocationSettings = onOpenLocationSettings,
                        onOpenFullScreenSettings = onOpenFullScreenSettings,
                        onOpenBatterySettings = onOpenBatterySettings,
                        onForceUnlock = {
                            DriverRepository.forceClearActiveMission(context, onDone = { notice = "Operação destravada."; activeRide = null; tab = AppTab.Inicio }, onError = { error = it })
                        },
                        onLogout = {
                            onGoOffline()
                            DriverRepository.logout(context) { profile = null; online = false; pendingRide = null; activeRide = null; tab = AppTab.Inicio }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun PremiumBottomBar(tab: AppTab, onTab: (AppTab) -> Unit) {
    NavigationBar(containerColor = Color.White, tonalElevation = 0.dp, modifier = Modifier.border(1.dp, Border)) {
        BottomItem(AppTab.Inicio, tab, "Início", Icons.Filled.Home, onTab)
        BottomItem(AppTab.Corridas, tab, "Rota", Icons.Filled.Route, onTab)
        BottomItem(AppTab.Ganhos, tab, "Carteira", Icons.Filled.AccountBalanceWallet, onTab)
        BottomItem(AppTab.Historico, tab, "Histórico", Icons.Filled.History, onTab)
        BottomItem(AppTab.Conta, tab, "Mais", Icons.Filled.MoreHoriz, onTab)
    }
}

@Composable
private fun RowScope.BottomItem(item: AppTab, selected: AppTab, label: String, icon: ImageVector, onTab: (AppTab) -> Unit) {
    val isSelected = item == selected
    NavigationBarItem(
        selected = isSelected,
        onClick = { onTab(item) },
        icon = { Icon(icon, label, modifier = Modifier.size(23.dp)) },
        label = { Text(label, fontSize = 10.sp, fontFamily = AppFont, fontWeight = if (isSelected) FontWeight.Black else FontWeight.SemiBold) },
        colors = NavigationBarItemDefaults.colors(
            selectedIconColor = Green,
            selectedTextColor = Green,
            indicatorColor = GreenSoft,
            unselectedIconColor = Muted2,
            unselectedTextColor = Muted2
        )
    )
}

@Composable
private fun ScreenScroll(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        content = content
    )
}

@Composable
private fun PremiumCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = modifier.fillMaxWidth().border(1.dp, Border, CardShape),
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = CardShape
    ) { Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp), content = content) }
}

@Composable
private fun MiniCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = modifier.clip(RoundedCornerShape(22.dp)).background(SurfaceSoft).border(1.dp, Border, RoundedCornerShape(22.dp)).padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        content = content
    )
}

@Composable
private fun PrimaryButton(text: String, icon: ImageVector? = null, enabled: Boolean = true, color: Color = Green, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = ButtonShape,
        colors = ButtonDefaults.buttonColors(containerColor = color, contentColor = Color.White, disabledContainerColor = Color(0xFFE6EBEF), disabledContentColor = Muted2),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp, pressedElevation = 0.dp),
        modifier = modifier.fillMaxWidth().height(56.dp)
    ) {
        if (icon != null) { Icon(icon, null, modifier = Modifier.size(20.dp)); Spacer(Modifier.width(8.dp)) }
        Text(text, fontFamily = AppFont, fontSize = 15.sp, fontWeight = FontWeight.Black)
    }
}

@Composable
private fun SecondaryButton(text: String, icon: ImageVector? = null, color: Color = Ink, modifier: Modifier = Modifier, onClick: () -> Unit) {
    OutlinedButton(onClick = onClick, modifier = modifier.fillMaxWidth().height(52.dp), shape = ButtonShape, colors = ButtonDefaults.outlinedButtonColors(contentColor = color)) {
        if (icon != null) { Icon(icon, null, modifier = Modifier.size(19.dp)); Spacer(Modifier.width(8.dp)) }
        Text(text, fontFamily = AppFont, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold)
    }
}

@Composable
private fun StatusChip(text: String, color: Color, icon: ImageVector? = null) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.clip(RoundedCornerShape(999.dp)).background(color.copy(alpha = .12f)).border(1.dp, color.copy(alpha = .25f), RoundedCornerShape(999.dp)).padding(horizontal = 11.dp, vertical = 7.dp)
    ) {
        if (icon != null) { Icon(icon, null, tint = color, modifier = Modifier.size(15.dp)); Spacer(Modifier.width(5.dp)) }
        Text(text, color = color, fontFamily = AppFont, fontSize = 11.sp, fontWeight = FontWeight.Black, maxLines = 1)
    }
}

@Composable
private fun Field(label: String, value: String, onChange: (String) -> Unit, placeholder: String = "", password: Boolean = false, keyboardType: KeyboardType = KeyboardType.Text, lines: Int = 1) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label, color = Muted, fontFamily = AppFont, fontSize = 11.sp, fontWeight = FontWeight.Black)
        OutlinedTextField(
            value = value,
            onValueChange = onChange,
            placeholder = { Text(placeholder, fontSize = 13.sp, fontFamily = AppFont) },
            visualTransformation = if (password) PasswordVisualTransformation() else VisualTransformation.None,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            maxLines = lines,
            minLines = lines,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp)
        )
    }
}

@Composable
private fun InlineAppMessage(text: String, color: Color, onClose: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().background(color.copy(alpha = .10f)).padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(if (color == Red) Icons.Filled.ErrorOutline else Icons.Filled.CheckCircle, null, tint = color, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text(text, color = Ink, fontFamily = AppFont, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
        Text("OK", color = color, fontFamily = AppFont, fontSize = 12.sp, fontWeight = FontWeight.Black, modifier = Modifier.clickable { onClose() })
    }
}

@Composable
private fun PermissionsOnboardingScreen(
    onRequestNotificationPermission: () -> Unit,
    onRequestLocationPermission: () -> Unit,
    onOpenFullScreenSettings: () -> Unit,
    onOpenBatterySettings: () -> Unit,
    onContinue: () -> Unit
) {
    val context = LocalContext.current
    var permissionStatus by remember { mutableStateOf(PermissionStatusReader.read(context)) }
    LaunchedEffect(Unit) { permissionStatus = PermissionStatusReader.read(context) }

    ScreenScroll {
        Spacer(Modifier.height(8.dp))
        BrandHeader(title = "Permissões essenciais", subtitle = "Ative uma vez para receber corridas com segurança.", icon = Icons.Filled.Shield)
        PermissionSetupCard("Notificações", "Alerta de nova corrida e avisos do gestor.", permissionStatus.notifications, Icons.Filled.NotificationsActive, onClick = { onRequestNotificationPermission(); permissionStatus = PermissionStatusReader.read(context) })
        PermissionSetupCard("Localização", "Usada para rota e acompanhamento da entrega.", permissionStatus.location, Icons.Filled.MyLocation, onClick = { onRequestLocationPermission(); permissionStatus = PermissionStatusReader.read(context) })
        PermissionSetupCard("Alerta urgente", "Permite abrir a corrida mesmo com a tela bloqueada.", permissionStatus.fullScreenIntent, Icons.Filled.Bolt, onClick = { onOpenFullScreenSettings(); permissionStatus = PermissionStatusReader.read(context) })
        PermissionSetupCard("Bateria sem restrição", "Evita que o Android mate o app em segundo plano.", permissionStatus.batteryUnrestricted, Icons.Filled.Shield, onClick = { onOpenBatterySettings(); permissionStatus = PermissionStatusReader.read(context) })
        PermissionSetupCard("Internet e GPS", "O app precisa de conexão e GPS ativo para operar.", hasInternet(context) && isGpsEnabled(context), Icons.Filled.Map, onClick = { permissionStatus = PermissionStatusReader.read(context) })
        PrimaryButton("Continuar", icon = Icons.Filled.CheckCircle, onClick = onContinue)
        Text("Se algo faltar depois, o status ficará em Restrição.", color = Muted, fontFamily = AppFont, fontSize = 12.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
    }
}

@Composable
private fun PermissionSetupCard(title: String, message: String, ok: Boolean, icon: ImageVector, onClick: () -> Unit) {
    PremiumCard {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(Modifier.size(48.dp).clip(CircleShape).background(if (ok) GreenSoft else OrangeSoft), contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = if (ok) Green else Orange, modifier = Modifier.size(23.dp))
            }
            Column(Modifier.weight(1f)) {
                Text(title, color = Ink, fontFamily = AppFont, fontSize = 15.sp, fontWeight = FontWeight.Black)
                Text(message, color = Muted, fontFamily = AppFont, fontSize = 12.sp, lineHeight = 16.sp)
            }
            StatusChip(if (ok) "OK" else "Ativar", if (ok) Green else Orange)
        }
    }
}

@Composable
private fun BrandHeader(title: String, subtitle: String, icon: ImageVector) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(Modifier.size(52.dp).clip(RoundedCornerShape(18.dp)).background(Green), contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = Color.White, modifier = Modifier.size(28.dp))
            }
            Column {
                Text("Rodrigues Entregador", color = Green, fontFamily = AppFont, fontSize = 12.sp, fontWeight = FontWeight.Black)
                Text(title, color = Ink, fontFamily = AppFont, fontSize = 27.sp, lineHeight = 29.sp, fontWeight = FontWeight.Black)
            }
        }
        Text(subtitle, color = Muted, fontFamily = AppFont, fontSize = 14.sp, lineHeight = 19.sp, fontWeight = FontWeight.SemiBold)
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
    var loading by remember { mutableStateOf(false) }
    var doc by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var cpf by remember { mutableStateOf("") }
    var vehicle by remember { mutableStateOf("Moto") }
    var plate by remember { mutableStateOf("") }
    var pix by remember { mutableStateOf("") }
    var bank by remember { mutableStateOf("") }

    ScreenScroll {
        BrandHeader(title = if (mode == "login") "Acesso do entregador" else "Cadastro do entregador", subtitle = if (mode == "login") "Entre com CPF ou telefone para operar suas rotas." else "Seu cadastro fica em análise até aprovação do gestor.", icon = Icons.Filled.TwoWheeler)
        if (notice.isNotBlank()) InlineNoticeCard(notice, Green)
        if (error.isNotBlank()) InlineNoticeCard(error, Red)
        PremiumCard {
            if (mode == "login") {
                Field("CPF ou telefone", doc, { doc = it }, "Digite seu acesso")
                Field("Senha", password, { password = it }, "Senha do app", password = true)
                PrimaryButton("Entrar", enabled = !loading, icon = Icons.Filled.CheckCircle) { onLogin(doc, password) { loading = it } }
                SecondaryButton("Cadastrar entregador", icon = Icons.Filled.Person) { mode = "cadastro" }
            } else {
                Field("Nome completo", name, { name = it }, "Seu nome")
                Field("CPF", cpf, { cpf = it }, "Somente números", keyboardType = KeyboardType.Number)
                Field("Telefone / WhatsApp", phone, { phone = it }, "(67) 99999-9999", keyboardType = KeyboardType.Phone)
                Field("Senha", password, { password = it }, "Mínimo 6 caracteres", password = true)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    Field("Veículo", vehicle, { vehicle = it }, modifierWeightPlaceholder(), lines = 1)
                }
                Field("Placa", plate, { plate = it.uppercase(Locale.ROOT) }, "Opcional")
                Field("Chave Pix", pix, { pix = it }, "Opcional")
                Field("Banco", bank, { bank = it }, "Opcional")
                PrimaryButton("Enviar cadastro", enabled = !loading, icon = Icons.Filled.CheckCircle) { onRegister(DriverRegistrationRequest(name, cpf, phone, password, vehicle, plate, pix, bank)) { loading = it } }
                SecondaryButton("Voltar ao login", icon = Icons.Filled.ArrowBack) { mode = "login" }
            }
        }
    }
}

private fun modifierWeightPlaceholder(): String = "Moto"

@Composable
private fun InlineNoticeCard(text: String, color: Color) {
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(color.copy(alpha = .10f)).border(1.dp, color.copy(alpha = .25f), RoundedCornerShape(22.dp)).padding(14.dp)) {
        Text(text, color = if (color == Red) Red else GreenDark, fontFamily = AppFont, fontSize = 13.sp, lineHeight = 18.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun CreatePasswordScreen(profile: DriverProfile, onSaved: () -> Unit, onLogout: () -> Unit) {
    val context = LocalContext.current
    var password by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    ScreenScroll {
        BrandHeader(title = "Crie sua senha", subtitle = "Esse acesso será usado nas próximas entradas no app.", icon = Icons.Filled.Shield)
        if (message.isNotBlank()) InlineNoticeCard(message, Red)
        PremiumCard {
            Text("Olá, ${profile.firstName()}", color = Ink, fontFamily = AppFont, fontSize = 19.sp, fontWeight = FontWeight.Black)
            Field("Nova senha", password, { password = it }, password = true)
            Field("Confirmar senha", confirm, { confirm = it }, password = true)
            PrimaryButton("Salvar senha", enabled = !loading, icon = Icons.Filled.CheckCircle) {
                when {
                    password.length < 6 -> message = "Use pelo menos 6 caracteres."
                    password != confirm -> message = "As senhas não conferem."
                    else -> {
                        loading = true
                        DriverRepository.updateAccessPassword(context, password, onSuccess = { loading = false; onSaved() }, onError = { loading = false; message = it })
                    }
                }
            }
            SecondaryButton("Sair", icon = Icons.Filled.ArrowBack, color = Red, onClick = onLogout)
        }
    }
}

@Composable
private fun LoadingSessionSplash(profile: DriverProfile) {
    Box(Modifier.fillMaxSize().background(Bg), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Box(Modifier.size(86.dp).clip(RoundedCornerShape(30.dp)).background(Green), contentAlignment = Alignment.Center) { Icon(Icons.Filled.TwoWheeler, null, tint = Color.White, modifier = Modifier.size(46.dp)) }
            CircularProgressIndicator(color = Green)
            Text("Preparando sua operação, ${profile.firstName()}...", color = Muted, fontFamily = AppFont, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun SystemStateScreen(title: String, message: String, icon: ImageVector, color: Color) {
    ScreenScroll {
        Spacer(Modifier.height(80.dp))
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Box(Modifier.size(82.dp).clip(CircleShape).background(color.copy(alpha = .12f)), contentAlignment = Alignment.Center) { Icon(icon, null, tint = color, modifier = Modifier.size(42.dp)) }
            Text(title, color = Ink, fontFamily = AppFont, fontSize = 25.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Center)
            Text(message, color = Muted, fontFamily = AppFont, fontSize = 14.sp, lineHeight = 20.sp, textAlign = TextAlign.Center)
        }
    }
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
    onToggleOnline: (Boolean) -> Unit,
    onAccept: (DriverRide) -> Unit,
    onReject: (DriverRide, String) -> Unit,
    onExpire: (DriverRide) -> Unit,
    onOpenRides: () -> Unit,
    onOpenMap: () -> Unit,
    onOpenNotifications: () -> Unit,
    onOpenSupport: () -> Unit
) {
    val context = LocalContext.current
    val permissions = PermissionStatusReader.read(context)
    val status = operationalStatus(online, activeRide, permissions, context)
    ScreenScroll {
        DriverTopHeader(profile, status, unread = appNotices.count { !it.read && it.isVisible() }, onOpenNotifications = onOpenNotifications)
        StatusHeroCard(status = status, online = online, onToggleOnline = onToggleOnline)
        EarningsSummaryCard(stats = stats, hideValues = hideValues, onToggleValues = onToggleValues)
        if (activeRide != null) {
            ActiveRouteShortcut(activeRide, onOpenRides)
        } else if (pendingRide != null) {
            IncomingOfferPanel(pendingRide, onAccept = onAccept, onReject = onReject, onExpire = onExpire)
        } else {
            OperationBanner(appBanners.firstOrNull { it.isVisible() }, status)
        }
        QuickActions(onOpenRides, onOpenMap, onOpenNotifications, onOpenSupport)
        if (!permissions.ready) PermissionRestrictionMini(permissions, context)
    }
}

@Composable
private fun DriverTopHeader(profile: DriverProfile, status: OperationalStatus, unread: Int, onOpenNotifications: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
        Avatar(profile.name, profile.photoUrl, 54)
        Column(Modifier.weight(1f)) {
            Text("Olá, ${profile.firstName()}", color = Ink, fontFamily = AppFont, fontSize = 24.sp, fontWeight = FontWeight.Black, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(status.label, color = status.buttonColor, fontFamily = AppFont, fontSize = 13.sp, fontWeight = FontWeight.Black)
        }
        Box(Modifier.size(48.dp).clip(CircleShape).background(SurfaceSoft).border(1.dp, Border, CircleShape).clickable { onOpenNotifications() }, contentAlignment = Alignment.Center) {
            Icon(Icons.Filled.Notifications, null, tint = Ink, modifier = Modifier.size(22.dp))
            if (unread > 0) Box(Modifier.align(Alignment.TopEnd).size(13.dp).clip(CircleShape).background(Red).border(2.dp, Color.White, CircleShape))
        }
    }
}

@Composable
private fun StatusHeroCard(status: OperationalStatus, online: Boolean, onToggleOnline: (Boolean) -> Unit) {
    PremiumCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(54.dp).clip(CircleShape).background(status.buttonColor.copy(alpha = .14f)), contentAlignment = Alignment.Center) {
                Icon(if (status.kind == AvailabilityKind.Restricao) Icons.Filled.ErrorOutline else Icons.Filled.TwoWheeler, null, tint = status.buttonColor, modifier = Modifier.size(30.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(status.label, color = Ink, fontFamily = AppFont, fontSize = 22.sp, fontWeight = FontWeight.Black)
                Text(status.message, color = Muted, fontFamily = AppFont, fontSize = 13.sp, lineHeight = 17.sp, fontWeight = FontWeight.SemiBold)
            }
        }
        PrimaryButton(
            text = if (online) "Ficar indisponível" else "Ficar disponível",
            icon = if (online) Icons.Filled.Cancel else Icons.Filled.CheckCircle,
            enabled = status.canGoOnline || online,
            color = if (online) Ink else status.buttonColor,
            onClick = { onToggleOnline(!online) }
        )
    }
}

@Composable
private fun EarningsSummaryCard(stats: DriverStats, hideValues: Boolean, onToggleValues: () -> Unit) {
    PremiumCard {
        Row(verticalAlignment = Alignment.Top) {
            Column(Modifier.weight(1f)) {
                Text("Ganhos de hoje", color = Muted, fontFamily = AppFont, fontSize = 12.sp, fontWeight = FontWeight.Black)
                Text(if (hideValues) "••••" else DriverRepository.formatCurrency(stats.totalToday), color = Ink, fontFamily = AppFont, fontSize = 30.sp, fontWeight = FontWeight.Black)
            }
            Box(Modifier.size(44.dp).clip(CircleShape).background(SurfaceSoft).clickable { onToggleValues() }, contentAlignment = Alignment.Center) {
                Icon(if (hideValues) Icons.Filled.VisibilityOff else Icons.Filled.Visibility, null, tint = Muted, modifier = Modifier.size(21.dp))
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            SmallMetric("Corridas", stats.ridesTodayCount.toString(), Modifier.weight(1f))
            SmallMetric("Finalizadas", stats.finishedTodayCount.toString(), Modifier.weight(1f))
            SmallMetric("Semana", if (hideValues) "••••" else DriverRepository.formatCurrency(stats.totalWeek), Modifier.weight(1f))
        }
    }
}

@Composable
private fun SmallMetric(label: String, value: String, modifier: Modifier = Modifier) {
    MiniCard(modifier = modifier) {
        Text(label, color = Muted, fontFamily = AppFont, fontSize = 10.sp, fontWeight = FontWeight.Black, maxLines = 1)
        Text(value, color = Ink, fontFamily = AppFont, fontSize = 14.sp, fontWeight = FontWeight.Black, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun ActiveRouteShortcut(ride: DriverRide, onOpenRides: () -> Unit) {
    PremiumCard(modifier = Modifier.clickable { onOpenRides() }) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(48.dp).clip(CircleShape).background(GreenSoft), contentAlignment = Alignment.Center) { Icon(Icons.Filled.Route, null, tint = Green, modifier = Modifier.size(25.dp)) }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text("Rota atual", color = Ink, fontFamily = AppFont, fontSize = 18.sp, fontWeight = FontWeight.Black)
                Text("#${ride.routeCode()} • ${ride.stageLabel()} • ${ride.ordersReadyLabel()}", color = Muted, fontFamily = AppFont, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            Icon(Icons.Filled.KeyboardArrowRight, null, tint = Muted)
        }
    }
}

@Composable
private fun OperationBanner(banner: AppCarouselBanner?, status: OperationalStatus) {
    val title = banner?.title?.takeIf { it.isNotBlank() } ?: "Pronto para rodar"
    val desc = banner?.description?.takeIf { it.isNotBlank() } ?: "Fique disponível e aguarde uma corrida da operação."
    PremiumCard {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            if (banner?.imageUrl?.isNotBlank() == true) {
                AsyncImage(model = banner.imageUrl, contentDescription = title, contentScale = ContentScale.Crop, modifier = Modifier.size(72.dp).clip(RoundedCornerShape(22.dp)))
            } else {
                Box(Modifier.size(72.dp).clip(RoundedCornerShape(22.dp)).background(status.buttonColor.copy(alpha = .12f)), contentAlignment = Alignment.Center) { Icon(Icons.Filled.Bolt, null, tint = status.buttonColor, modifier = Modifier.size(35.dp)) }
            }
            Column(Modifier.weight(1f)) {
                Text(title, color = Ink, fontFamily = AppFont, fontSize = 18.sp, fontWeight = FontWeight.Black)
                Text(desc, color = Muted, fontFamily = AppFont, fontSize = 13.sp, lineHeight = 17.sp)
            }
        }
    }
}

@Composable
private fun QuickActions(onRides: () -> Unit, onMap: () -> Unit, onNotices: () -> Unit, onSupport: () -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
        QuickTile("Rota", Icons.Filled.Route, Green, Modifier.weight(1f), onRides)
        QuickTile("Mapa", Icons.Filled.Map, Blue, Modifier.weight(1f), onMap)
        QuickTile("Avisos", Icons.Filled.Notifications, Orange, Modifier.weight(1f), onNotices)
        QuickTile("Ajuda", Icons.Filled.SupportAgent, Ink, Modifier.weight(1f), onSupport)
    }
}

@Composable
private fun QuickTile(title: String, icon: ImageVector, color: Color, modifier: Modifier, onClick: () -> Unit) {
    Column(modifier.clip(RoundedCornerShape(24.dp)).background(Surface).border(1.dp, Border, RoundedCornerShape(24.dp)).clickable { onClick() }.padding(vertical = 15.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(7.dp)) {
        Icon(icon, null, tint = color, modifier = Modifier.size(24.dp))
        Text(title, color = Ink, fontFamily = AppFont, fontSize = 11.sp, fontWeight = FontWeight.Black)
    }
}

@Composable
private fun PermissionRestrictionMini(status: PermissionStatus, context: Context) {
    val missing = buildList {
        if (!status.notifications) add("notificações")
        if (!status.location) add("localização")
        if (!status.fullScreenIntent) add("alerta urgente")
        if (!status.batteryUnrestricted) add("bateria")
        if (!hasInternet(context)) add("internet")
        if (!isGpsEnabled(context)) add("GPS")
    }
    if (missing.isNotEmpty()) InlineNoticeCard("Restrição: ajuste ${missing.joinToString(", ")} para receber corridas sem falha.", Orange)
}

@Composable
private fun IncomingOfferPanel(ride: DriverRide, onAccept: (DriverRide) -> Unit, onReject: (DriverRide, String) -> Unit, onExpire: (DriverRide) -> Unit) {
    var rejectOpen by remember(ride.id) { mutableStateOf(false) }
    PremiumCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            StatusChip("Nova oferta", Red, Icons.Filled.Bolt)
            Spacer(Modifier.weight(1f))
            Text(ride.value.ifBlank { "A definir" }, color = Green, fontFamily = AppFont, fontSize = 22.sp, fontWeight = FontWeight.Black)
        }
        RealDeliveryMap(
            title = "Oferta",
            subtitle = listOf(ride.distance, ride.duration).filter { it.isNotBlank() }.joinToString(" • "),
            pickupAddress = ride.pickup,
            dropoffAddress = ride.dropoff,
            pickupLat = ride.pickupLat,
            pickupLng = ride.pickupLng,
            dropoffLat = ride.dropoffLat,
            dropoffLng = ride.dropoffLng,
            mode = DeliveryMapMode.PICKUP_TO_DROPOFF,
            modifier = Modifier.height(210.dp)
        )
        RoutePointLine(Icons.Filled.Storefront, "Coleta", ride.pickup.ifBlank { "Rodrigues Açaí e Cia." }, Green)
        RoutePointLine(Icons.Filled.Place, "Entrega", ride.neighborhood.ifBlank { ride.dropoff.ifBlank { "Área da entrega" } }, Orange)
        PaymentChips(ride.paymentUi())
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            SecondaryButton("Recusar", color = Red, modifier = Modifier.weight(1f)) { rejectOpen = true }
            PrimaryButton("Aceitar", icon = Icons.Filled.CheckCircle, modifier = Modifier.weight(1.3f)) { onAccept(ride) }
        }
    }
    if (rejectOpen) RejectDialog(onClose = { rejectOpen = false }, onConfirm = { reason -> rejectOpen = false; onReject(ride, reason) })
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
    onOpenNavigator: (String, String) -> Unit,
    onOccurrence: (DriverRide, String, String) -> Unit,
    onSettleAndFinish: (DriverRide, PaymentSettlementInput) -> Unit
) {
    when {
        activeRide != null -> CurrentRouteScreen(activeRide, paymentMachines, onUpdateRide, onOpenNavigator, onOccurrence, onSettleAndFinish)
        pendingRide != null -> ScreenScroll { IncomingOfferPanel(pendingRide, onAccept, onReject, onExpire) }
        else -> EmptyOperationScreen(
            title = if (online) "Aguardando corrida" else "Você está indisponível",
            message = if (online) "Quando a operação enviar uma rota, ela aparece aqui com alerta." else "Volte para a Home e fique disponível para receber ofertas.",
            icon = Icons.Filled.Route,
            color = if (online) Green else Muted
        )
    }
}

@Composable
private fun CurrentRouteScreen(
    ride: DriverRide,
    paymentMachines: List<PaymentMachine>,
    onUpdateRide: (DriverRide, String) -> Unit,
    onOpenNavigator: (String, String) -> Unit,
    onOccurrence: (DriverRide, String, String) -> Unit,
    onSettleAndFinish: (DriverRide, PaymentSettlementInput) -> Unit
) {
    var occurrenceOpen by remember(ride.id) { mutableStateOf(false) }
    var financeOpen by remember(ride.id) { mutableStateOf(false) }
    var ordersOpen by remember(ride.id) { mutableStateOf(ride.routeOrderCount > 1) }
    var paymentConfirmOpen by remember(ride.id) { mutableStateOf(false) }
    val action = ride.nextAction()
    val payment = ride.paymentUi()
    val isDelivering = ride.isDeliveringStage()
    val mapMode = if (isDelivering) DeliveryMapMode.DRIVER_TO_DROPOFF else DeliveryMapMode.DRIVER_TO_PICKUP

    ScreenScroll {
        RouteTopHeader(ride)
        RealDeliveryMap(
            title = "Rota #${ride.routeCode()}",
            subtitle = listOf(ride.distance, ride.duration).filter { it.isNotBlank() }.joinToString(" • ").ifBlank { ride.stageLabel() },
            pickupAddress = ride.pickup,
            dropoffAddress = ride.dropoff,
            pickupLat = ride.pickupLat,
            pickupLng = ride.pickupLng,
            dropoffLat = ride.dropoffLat,
            dropoffLng = ride.dropoffLng,
            mode = mapMode,
            modifier = Modifier.height(230.dp)
        )
        NextActionCard(ride, action, onMainClick = {
            if (action.needsPaymentConfirmation) paymentConfirmOpen = true else onUpdateRide(ride, action.statusToSend)
        })
        CompactStopsCard(ride, isDelivering)
        PaymentChipsCard(payment)
        OrdersCompactSection(ride, expanded = ordersOpen, onToggle = { ordersOpen = !ordersOpen })
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            SecondaryButton("Problema na rota", icon = Icons.Filled.ErrorOutline, color = Orange, modifier = Modifier.weight(1f)) { occurrenceOpen = true }
            SecondaryButton("Navegação", icon = Icons.Filled.Map, color = Green, modifier = Modifier.weight(1f)) { onOpenNavigator(ride.pickup, ride.dropoff) }
        }
        CollapsibleFinance(ride, expanded = financeOpen, onToggle = { financeOpen = !financeOpen })
    }
    if (occurrenceOpen) OccurrenceDialog(onClose = { occurrenceOpen = false }, onConfirm = { reason, details -> occurrenceOpen = false; onOccurrence(ride, reason, details) })
    if (paymentConfirmOpen) PaymentConfirmDialog(ride, paymentMachines, onClose = { paymentConfirmOpen = false }, onConfirm = { input -> paymentConfirmOpen = false; onSettleAndFinish(ride, input) })
}

@Composable
private fun RouteTopHeader(ride: DriverRide) {
    PremiumCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Rota #${ride.routeCode()}", color = Ink, fontFamily = AppFont, fontSize = 25.sp, fontWeight = FontWeight.Black)
                Text("${ride.stageLabel()} • ${ride.ordersReadyLabel()}", color = Muted, fontFamily = AppFont, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
            StatusChip(ride.stageShort(), ride.stageColor(), Icons.Filled.Route)
        }
    }
}

@Composable
private fun NextActionCard(ride: DriverRide, action: RouteAction, onMainClick: () -> Unit) {
    PremiumCard {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(Modifier.size(52.dp).clip(CircleShape).background(action.accent.copy(alpha = .12f)), contentAlignment = Alignment.Center) { Icon(Icons.Filled.Bolt, null, tint = action.accent, modifier = Modifier.size(27.dp)) }
            Column(Modifier.weight(1f)) {
                Text(action.title, color = Ink, fontFamily = AppFont, fontSize = 19.sp, fontWeight = FontWeight.Black)
                Text(action.message, color = Muted, fontFamily = AppFont, fontSize = 13.sp, lineHeight = 17.sp, fontWeight = FontWeight.SemiBold)
            }
        }
        if (ride.shouldShowReleaseCode()) {
            ReleaseCodeCard(ride)
        }
        PrimaryButton(action.button, icon = if (action.enabled) Icons.Filled.CheckCircle else Icons.Filled.Schedule, enabled = action.enabled, color = action.accent, onClick = onMainClick)
    }
}

@Composable
private fun ReleaseCodeCard(ride: DriverRide) {
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(26.dp)).background(GreenSoft).border(1.dp, Green.copy(alpha = .22f), RoundedCornerShape(26.dp)).padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Código de retirada", color = GreenDark, fontFamily = AppFont, fontSize = 12.sp, fontWeight = FontWeight.Black)
        Text(ride.releaseCode(), color = Ink, fontFamily = AppFont, fontSize = 44.sp, fontWeight = FontWeight.Black)
        Text("Mostre no balcão", color = Muted, fontFamily = AppFont, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun CompactStopsCard(ride: DriverRide, isDelivering: Boolean) {
    PremiumCard {
        RoutePointLine(Icons.Filled.Storefront, "Coleta", ride.pickup.ifBlank { "Rodrigues Açaí e Cia." }, Green)
        RoutePointLine(Icons.Filled.Place, "Entrega", if (isDelivering) ride.dropoff.ifBlank { ride.neighborhood.ifBlank { "Endereço não informado" } } else "Endereço após saída", Orange)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            SmallMetric("Distância", ride.distance.ifBlank { "—" }, Modifier.weight(1f))
            SmallMetric("Tempo", ride.duration.ifBlank { "—" }, Modifier.weight(1f))
        }
    }
}

@Composable
private fun RoutePointLine(icon: ImageVector, label: String, value: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(11.dp), modifier = Modifier.fillMaxWidth()) {
        Box(Modifier.size(36.dp).clip(CircleShape).background(color.copy(alpha = .12f)), contentAlignment = Alignment.Center) { Icon(icon, null, tint = color, modifier = Modifier.size(20.dp)) }
        Column(Modifier.weight(1f)) {
            Text(label, color = Muted, fontFamily = AppFont, fontSize = 10.sp, fontWeight = FontWeight.Black)
            Text(value.ifBlank { "Não informado" }, color = Ink, fontFamily = AppFont, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun PaymentChipsCard(payment: PaymentUi) {
    PremiumCard {
        Text("Pagamento", color = Ink, fontFamily = AppFont, fontSize = 16.sp, fontWeight = FontWeight.Black)
        PaymentChips(payment)
    }
}

@Composable
private fun PaymentChips(payment: PaymentUi) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        StatusChip(payment.label, payment.color, if (payment.requiresMachine) Icons.Filled.CreditCard else Icons.Filled.Payments)
        if (payment.amountLabel.isNotBlank()) StatusChip(payment.amountLabel, if (payment.requiresChange) Orange else Green)
    }
    if (payment.detail.isNotBlank()) Text(payment.detail, color = Muted, fontFamily = AppFont, fontSize = 12.sp, lineHeight = 16.sp, fontWeight = FontWeight.SemiBold)
}

@Composable
private fun OrdersCompactSection(ride: DriverRide, expanded: Boolean, onToggle: () -> Unit) {
    PremiumCard {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { onToggle() }) {
            Column(Modifier.weight(1f)) {
                Text("Pedidos da rota", color = Ink, fontFamily = AppFont, fontSize = 16.sp, fontWeight = FontWeight.Black)
                Text(ride.ordersReadyLabel(), color = Muted, fontFamily = AppFont, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            Icon(Icons.Filled.KeyboardArrowRight, null, tint = Muted)
        }
        if (expanded) {
            val orders = ride.displayOrders()
            orders.forEach { RouteOrderLine(it, ride.paymentUi().label) }
        } else {
            val first = ride.displayOrders().firstOrNull()
            if (first != null) RouteOrderLine(first, ride.paymentUi().label)
        }
    }
}

@Composable
private fun RouteOrderLine(order: RouteOrder, fallbackPayment: String) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(SurfaceSoft).padding(12.dp)) {
        Column(Modifier.weight(1f)) {
            Text("#${order.code.ifBlank { order.id.takeLast(4).uppercase(Locale.ROOT) }} • ${order.customerName.ifBlank { "Cliente" }}", color = Ink, fontFamily = AppFont, fontSize = 13.sp, fontWeight = FontWeight.Black, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(order.paymentSummary.ifBlank { fallbackPayment }, color = Muted, fontFamily = AppFont, fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        StatusChip(if (order.terminal) "Cancelado" else if (order.ready) "Pronto" else "Preparo", if (order.terminal) Red else if (order.ready) Green else Orange)
    }
}

@Composable
private fun CollapsibleFinance(ride: DriverRide, expanded: Boolean, onToggle: () -> Unit) {
    PremiumCard {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { onToggle() }) {
            Text("Financeiro e repasse", color = Ink, fontFamily = AppFont, fontSize = 16.sp, fontWeight = FontWeight.Black, modifier = Modifier.weight(1f))
            Icon(Icons.Filled.KeyboardArrowRight, null, tint = Muted)
        }
        if (expanded) {
            FinanceLine("Corrida", ride.value.ifBlank { DriverRepository.formatCurrency(ride.valueNumber) })
            FinanceLine("Pedido", DriverRepository.formatCurrency(ride.clientTotalNumber))
            FinanceLine("Receber cliente", DriverRepository.formatCurrency(ride.amountToCollectNumber))
            FinanceLine("Repassar loja", DriverRepository.formatCurrency(ride.storeReturnNumber))
            FinanceLine("Taxa maquininha", DriverRepository.formatCurrency(ride.machineFeeNumber))
        }
    }
}

@Composable
private fun FinanceLine(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = Muted, fontFamily = AppFont, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Text(value, color = Ink, fontFamily = AppFont, fontSize = 13.sp, fontWeight = FontWeight.Black)
    }
}

@Composable
private fun RouteMapContent(activeRide: DriverRide?, online: Boolean, onBackHome: () -> Unit, onOpenNavigator: (String, String) -> Unit) {
    if (activeRide == null) {
        ScreenScroll {
            BrandHeader(title = "Mapa", subtitle = if (online) "Você está disponível. O mapa acompanha sua posição e a rota quando houver corrida." else "Fique disponível para usar mapa operacional em rota.", icon = Icons.Filled.Map)
            RealDeliveryMap("Mapa do entregador", if (online) "Disponível" else "Indisponível", "Rodrigues Açaí e Cia Campo Grande MS", "Campo Grande MS", mode = DeliveryMapMode.DRIVER_TO_PICKUP, modifier = Modifier.height(360.dp))
            PrimaryButton("Voltar para início", icon = Icons.Filled.Home, onClick = onBackHome)
        }
        return
    }
    ScreenScroll {
        BrandHeader(title = "Mapa da rota", subtitle = "${activeRide.stageLabel()} • ${activeRide.distance.ifBlank { "distância pendente" }}", icon = Icons.Filled.Map)
        RealDeliveryMap(
            title = "Rota #${activeRide.routeCode()}",
            subtitle = listOf(activeRide.distance, activeRide.duration).filter { it.isNotBlank() }.joinToString(" • "),
            pickupAddress = activeRide.pickup,
            dropoffAddress = activeRide.dropoff,
            pickupLat = activeRide.pickupLat,
            pickupLng = activeRide.pickupLng,
            dropoffLat = activeRide.dropoffLat,
            dropoffLng = activeRide.dropoffLng,
            mode = if (activeRide.isDeliveringStage()) DeliveryMapMode.DRIVER_TO_DROPOFF else DeliveryMapMode.DRIVER_TO_PICKUP,
            modifier = Modifier.height(380.dp)
        )
        PrimaryButton("Abrir navegação", icon = Icons.Filled.Map) { onOpenNavigator(activeRide.pickup, activeRide.dropoff) }
    }
}

@Composable
private fun EmptyOperationScreen(title: String, message: String, icon: ImageVector, color: Color) {
    ScreenScroll {
        Spacer(Modifier.height(50.dp))
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(15.dp)) {
            Box(Modifier.size(88.dp).clip(CircleShape).background(color.copy(alpha = .12f)), contentAlignment = Alignment.Center) { Icon(icon, null, tint = color, modifier = Modifier.size(44.dp)) }
            Text(title, color = Ink, fontFamily = AppFont, fontSize = 24.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Center)
            Text(message, color = Muted, fontFamily = AppFont, fontSize = 14.sp, lineHeight = 20.sp, textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun WalletContent(profile: DriverProfile, stats: DriverStats, history: List<DriverHistory>, hideValues: Boolean, onToggleValues: () -> Unit) {
    ScreenScroll {
        Row(verticalAlignment = Alignment.CenterVertically) {
            BrandHeader(title = "Carteira", subtitle = "Ganhos, acerto e repasse sem confundir saldo.", icon = Icons.Filled.AccountBalanceWallet)
        }
        PremiumCard {
            Row(verticalAlignment = Alignment.Top) {
                Column(Modifier.weight(1f)) {
                    Text("Ganhos de hoje", color = Muted, fontFamily = AppFont, fontSize = 12.sp, fontWeight = FontWeight.Black)
                    Text(if (hideValues) "••••" else DriverRepository.formatCurrency(stats.totalToday), color = Ink, fontFamily = AppFont, fontSize = 34.sp, fontWeight = FontWeight.Black)
                    Text("${stats.finishedTodayCount} finalizadas hoje", color = Muted, fontFamily = AppFont, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                Box(Modifier.size(44.dp).clip(CircleShape).background(SurfaceSoft).clickable { onToggleValues() }, contentAlignment = Alignment.Center) { Icon(if (hideValues) Icons.Filled.VisibilityOff else Icons.Filled.Visibility, null, tint = Muted) }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            AmountCard("Semana", stats.totalWeek, hideValues, Modifier.weight(1f))
            AmountCard("Mês", stats.totalMonth, hideValues, Modifier.weight(1f))
        }
        PremiumCard {
            Text("Acerto da operação", color = Ink, fontFamily = AppFont, fontSize = 17.sp, fontWeight = FontWeight.Black)
            FinanceLine("Dinheiro recebido", moneyMaybe(stats.dinheiroRecebido, hideValues))
            FinanceLine("Cartão/maquininha", moneyMaybe(stats.cartaoRecebido, hideValues))
            FinanceLine("Pix", moneyMaybe(stats.pixRecebido, hideValues))
            FinanceLine("Taxas", moneyMaybe(stats.taxasMaquininha, hideValues))
            FinanceLine("A repassar loja", moneyMaybe(stats.valorARepassar, hideValues))
            FinanceLine("A receber", moneyMaybe(stats.valorAReceber, hideValues))
        }
        PremiumCard {
            Text("Próximo repasse", color = Ink, fontFamily = AppFont, fontSize = 17.sp, fontWeight = FontWeight.Black)
            Text(stats.proximoRepasseLabel, color = Green, fontFamily = AppFont, fontSize = 19.sp, fontWeight = FontWeight.Black)
            Text(stats.proximoRepasseDescricao, color = Muted, fontFamily = AppFont, fontSize = 12.sp, lineHeight = 17.sp)
        }
        PremiumCard {
            Text("Pix/Banco", color = Ink, fontFamily = AppFont, fontSize = 17.sp, fontWeight = FontWeight.Black)
            RoutePointLine(Icons.Filled.Payments, "Pix", stats.pixKey.ifBlank { profile.pixKey.ifBlank { "Não informado" } }, Green)
            RoutePointLine(Icons.Filled.AccountBalanceWallet, "Banco", stats.bankName.ifBlank { profile.bankName.ifBlank { "Não informado" } }, Blue)
        }
    }
}

@Composable
private fun AmountCard(label: String, amount: Double, hide: Boolean, modifier: Modifier) {
    PremiumCard(modifier = modifier) {
        Text(label, color = Muted, fontFamily = AppFont, fontSize = 11.sp, fontWeight = FontWeight.Black)
        Text(if (hide) "••••" else DriverRepository.formatCurrency(amount), color = Ink, fontFamily = AppFont, fontSize = 19.sp, fontWeight = FontWeight.Black, maxLines = 1)
    }
}

@Composable
private fun HistoryContent(history: List<DriverHistory>) {
    ScreenScroll {
        BrandHeader(title = "Histórico", subtitle = "Uma corrida por card. Linha do tempo completa fica para o gestor.", icon = Icons.Filled.History)
        if (history.isEmpty()) {
            EmptyInsideCard("Sem corridas ainda", "Corridas aceitas, recusadas, expiradas e canceladas aparecerão aqui.", Icons.Filled.History)
        } else {
            history.take(60).forEach { HistoryCard(it) }
        }
    }
}

@Composable
private fun HistoryCard(item: DriverHistory) {
    val color = when {
        item.action.contains("CANCEL", true) -> Red
        item.action.contains("RECUS", true) -> Orange
        item.action.contains("EXPIR", true) -> Muted
        item.action.contains("FINAL", true) || item.action.contains("ENTREG", true) -> Green
        item.action.contains("OCOR", true) -> Orange
        else -> Blue
    }
    PremiumCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            StatusChip(historyLabel(item.action), color)
            Spacer(Modifier.weight(1f))
            Text(item.value.ifBlank { "—" }, color = Ink, fontFamily = AppFont, fontSize = 15.sp, fontWeight = FontWeight.Black)
        }
        Text("#${item.rideId.takeLast(6).uppercase(Locale.ROOT)} • ${item.createdLabel}", color = Ink, fontFamily = AppFont, fontSize = 15.sp, fontWeight = FontWeight.Black)
        if (item.pickup.isNotBlank() || item.dropoff.isNotBlank()) Text(listOf(item.pickup, item.neighborhood.ifBlank { item.dropoff }).filter { it.isNotBlank() }.joinToString(" → "), color = Muted, fontFamily = AppFont, fontSize = 12.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun NotificationsScreen(notices: List<AppNotice>, onBack: () -> Unit) {
    ScreenScroll {
        ScreenHeader("Notificações", "Avisos da operação e alertas do app.", Icons.Filled.Notifications, onBack)
        if (notices.isEmpty()) EmptyInsideCard("Nenhum aviso", "Quando o gestor enviar avisos, eles aparecem aqui.", Icons.Filled.Notifications)
        else notices.sortedBy { it.read }.take(50).forEach { notice ->
            PremiumCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    StatusChip(notice.category.ifBlank { "Operação" }, if (notice.priority.uppercase(Locale.ROOT).contains("ALTA")) Red else Green)
                    Spacer(Modifier.weight(1f))
                    Text(notice.createdLabel, color = Muted2, fontFamily = AppFont, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                Text(notice.title.ifBlank { "Aviso" }, color = Ink, fontFamily = AppFont, fontSize = 17.sp, fontWeight = FontWeight.Black)
                Text(notice.message.ifBlank { "Sem descrição." }, color = Muted, fontFamily = AppFont, fontSize = 13.sp, lineHeight = 18.sp)
            }
        }
    }
}

@Composable
private fun MoreContent(
    profile: DriverProfile,
    online: Boolean,
    themeMode: String,
    hideValues: Boolean,
    notices: List<AppNotice>,
    onThemeChanged: (String) -> Unit,
    onToggleValues: () -> Unit,
    onProfileChanged: () -> Unit,
    onOpenNotificationSettings: () -> Unit,
    onOpenLocationSettings: () -> Unit,
    onOpenFullScreenSettings: () -> Unit,
    onOpenBatterySettings: () -> Unit,
    onForceUnlock: () -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    var screen by remember { mutableStateOf("menu") }
    when (screen) {
        "perfil" -> ProfileScreen(profile, online, onBack = { screen = "menu" })
        "pix" -> PixBankScreen(profile, onBack = { screen = "menu" }, onSaved = onProfileChanged)
        "operacao" -> OperationPreferencesScreen(onBack = { screen = "menu" })
        "permissoes" -> PermissionsSettingsScreen(onBack = { screen = "menu" }, onOpenNotificationSettings, onOpenLocationSettings, onOpenFullScreenSettings, onOpenBatterySettings)
        "suporte" -> SupportScreen(onBack = { screen = "menu" }, onForceUnlock = onForceUnlock)
        else -> ScreenScroll {
            BrandHeader(title = "Mais", subtitle = "Conta, operação, permissões e suporte.", icon = Icons.Filled.MoreHoriz)
            PremiumCard {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Avatar(profile.name, profile.photoUrl, 58)
                    Column(Modifier.weight(1f)) {
                        Text(profile.name, color = Ink, fontFamily = AppFont, fontSize = 18.sp, fontWeight = FontWeight.Black, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(if (online) "Disponível" else "Indisponível", color = if (online) Green else Muted, fontFamily = AppFont, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            MenuTile("Perfil", "Foto, telefone e dados da conta", Icons.Filled.Person) { screen = "perfil" }
            MenuTile("Pix/Banco", "Dados de recebimento", Icons.Filled.Payments) { screen = "pix" }
            MenuTile("Preferências de operação", "Maquininha, troco e restrições", Icons.Filled.TwoWheeler) { screen = "operacao" }
            MenuTile("Permissões", "Notificações, localização e bateria", Icons.Filled.Shield) { screen = "permissoes" }
            MenuTile("Suporte e destravar", "Ajuda para rota travada", Icons.Filled.SupportAgent) { screen = "suporte" }
            PremiumCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Ocultar valores", color = Ink, fontFamily = AppFont, fontSize = 15.sp, fontWeight = FontWeight.Black)
                        Text("Mostra ou esconde ganhos e repasses", color = Muted, fontFamily = AppFont, fontSize = 12.sp)
                    }
                    Switch(checked = hideValues, onCheckedChange = { onToggleValues() })
                }
                Text("Versão 6.18.0 • Reconstrução Real do Entregador", color = Muted2, fontFamily = AppFont, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
            SecondaryButton("Sair do app", icon = Icons.Filled.ArrowBack, color = Red, onClick = onLogout)
        }
    }
}

@Composable
private fun MenuTile(title: String, subtitle: String, icon: ImageVector, onClick: () -> Unit) {
    PremiumCard(modifier = Modifier.clickable { onClick() }) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(Modifier.size(46.dp).clip(CircleShape).background(GreenSoft), contentAlignment = Alignment.Center) { Icon(icon, null, tint = Green, modifier = Modifier.size(23.dp)) }
            Column(Modifier.weight(1f)) {
                Text(title, color = Ink, fontFamily = AppFont, fontSize = 16.sp, fontWeight = FontWeight.Black)
                Text(subtitle, color = Muted, fontFamily = AppFont, fontSize = 12.sp)
            }
            Icon(Icons.Filled.KeyboardArrowRight, null, tint = Muted)
        }
    }
}

@Composable
private fun ScreenHeader(title: String, subtitle: String, icon: ImageVector, onBack: (() -> Unit)? = null) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        if (onBack != null) Box(Modifier.size(42.dp).clip(CircleShape).background(SurfaceSoft).border(1.dp, Border, CircleShape).clickable { onBack() }, contentAlignment = Alignment.Center) { Icon(Icons.Filled.ArrowBack, null, tint = Ink) }
        Column(Modifier.weight(1f)) { BrandHeader(title, subtitle, icon) }
    }
}

@Composable
private fun ProfileScreen(profile: DriverProfile, online: Boolean, onBack: () -> Unit) {
    ScreenScroll {
        ScreenHeader("Perfil", "Dados principais do entregador.", Icons.Filled.Person, onBack)
        PremiumCard {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Avatar(profile.name, profile.photoUrl, 94)
                Text(profile.name, color = Ink, fontFamily = AppFont, fontSize = 23.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Center)
                StatusChip(if (online) "Disponível" else "Indisponível", if (online) Green else Muted)
            }
        }
        PremiumCard {
            RoutePointLine(Icons.Filled.TwoWheeler, "Veículo", profile.vehicle.ifBlank { "Moto" }, Green)
            RoutePointLine(Icons.Filled.ChatBubbleOutline, "Telefone", profile.phone.ifBlank { "Não informado" }, Blue)
            RoutePointLine(Icons.Filled.Place, "Cidade", profile.city.ifBlank { "Campo Grande - MS" }, Orange)
        }
    }
}

@Composable
private fun PixBankScreen(profile: DriverProfile, onBack: () -> Unit, onSaved: () -> Unit) {
    val context = LocalContext.current
    var pix by remember { mutableStateOf(profile.pixKey) }
    var bank by remember { mutableStateOf(profile.bankName) }
    var payoutType by remember { mutableStateOf("Pix") }
    var message by remember { mutableStateOf("") }
    ScreenScroll {
        ScreenHeader("Pix/Banco", "Recebimento do entregador.", Icons.Filled.Payments, onBack)
        if (message.isNotBlank()) InlineNoticeCard(message, if (message.contains("salv", true)) Green else Red)
        PremiumCard {
            Field("Chave Pix", pix, { pix = it }, "CPF, telefone, e-mail ou aleatória")
            Field("Banco", bank, { bank = it }, "Nome do banco")
            Field("Tipo de repasse", payoutType, { payoutType = it }, "Pix")
            Text("A conta deve estar no nome do titular aprovado.", color = Muted, fontFamily = AppFont, fontSize = 12.sp, lineHeight = 17.sp)
            PrimaryButton("Salvar recebimento", icon = Icons.Filled.CheckCircle) {
                DriverRepository.updatePayoutData(context, pix, bank, payoutType, onSuccess = { message = "Dados salvos para conferência."; onSaved() }, onError = { message = it })
            }
        }
    }
}

@Composable
private fun OperationPreferencesScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var prefs by remember { mutableStateOf(DriverRepository.loadOperationalPreferences(context)) }
    fun save(newPrefs: DriverOperationalPreferences) { prefs = newPrefs; DriverRepository.saveOperationalPreferences(context, newPrefs) }
    ScreenScroll {
        ScreenHeader("Operação", "Preferências usadas no despacho da rota.", Icons.Filled.TwoWheeler, onBack)
        PreferenceSwitch("Tenho maquininha", "Permite receber pedido no cartão.", prefs.hasMachine) { save(prefs.copy(hasMachine = it)) }
        PreferenceSwitch("Aceito débito", "Disponível se você usa maquininha.", prefs.acceptsDebit) { save(prefs.copy(acceptsDebit = it)) }
        PreferenceSwitch("Aceito crédito", "Disponível se você usa maquininha.", prefs.acceptsCredit) { save(prefs.copy(acceptsCredit = it)) }
        PreferenceSwitch("Aceito parcelado/ticket", "Usado quando o gestor precisar filtrar rotas.", prefs.acceptsInstallment || prefs.acceptsTicket) { save(prefs.copy(acceptsInstallment = it, acceptsTicket = it)) }
        PreferenceSwitch("Tenho troco", "Informe se pode receber dinheiro.", prefs.hasCashChange) { save(prefs.copy(hasCashChange = it)) }
        PreferenceSwitch("Somente pago online", "Bloqueia dinheiro/maquininha para você.", prefs.onlyOnlinePaid) { save(prefs.copy(onlyOnlinePaid = it)) }
        PreferenceSwitch("Bloquear dinheiro à noite", "Evita pedidos em dinheiro no horário configurado.", prefs.blockCashAtNight) { save(prefs.copy(blockCashAtNight = it)) }
        PreferenceSwitch("Bloquear maquininha à noite", "Evita cartão/maquininha no horário configurado.", prefs.blockMachineAtNight) { save(prefs.copy(blockMachineAtNight = it)) }
    }
}

@Composable
private fun PreferenceSwitch(title: String, subtitle: String, checked: Boolean, onChecked: (Boolean) -> Unit) {
    PremiumCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(title, color = Ink, fontFamily = AppFont, fontSize = 15.sp, fontWeight = FontWeight.Black)
                Text(subtitle, color = Muted, fontFamily = AppFont, fontSize = 12.sp, lineHeight = 16.sp)
            }
            Switch(checked = checked, onCheckedChange = onChecked)
        }
    }
}

@Composable
private fun PermissionsSettingsScreen(onBack: () -> Unit, onOpenNotificationSettings: () -> Unit, onOpenLocationSettings: () -> Unit, onOpenFullScreenSettings: () -> Unit, onOpenBatterySettings: () -> Unit) {
    val context = LocalContext.current
    val status = PermissionStatusReader.read(context)
    ScreenScroll {
        ScreenHeader("Permissões", "O app precisa disso para tocar corrida fora da tela.", Icons.Filled.Shield, onBack)
        PermissionSetupCard("Notificações", "Receber corrida e avisos do gestor.", status.notifications, Icons.Filled.NotificationsActive, onOpenNotificationSettings)
        PermissionSetupCard("Localização", "Rota e acompanhamento.", status.location, Icons.Filled.MyLocation, onOpenLocationSettings)
        PermissionSetupCard("Alerta urgente", "Tela cheia para oferta importante.", status.fullScreenIntent, Icons.Filled.Bolt, onOpenFullScreenSettings)
        PermissionSetupCard("Bateria", "Evita o app morrer em segundo plano.", status.batteryUnrestricted, Icons.Filled.Shield, onOpenBatterySettings)
        PermissionSetupCard("Internet/GPS", "Conexão e localização do aparelho.", hasInternet(context) && isGpsEnabled(context), Icons.Filled.Map) { }
    }
}

@Composable
private fun SupportScreen(onBack: () -> Unit, onForceUnlock: () -> Unit) {
    ScreenScroll {
        ScreenHeader("Suporte", "Ações de segurança para operação real.", Icons.Filled.SupportAgent, onBack)
        PremiumCard {
            Text("Destravar operação", color = Ink, fontFamily = AppFont, fontSize = 18.sp, fontWeight = FontWeight.Black)
            Text("Use se o gestor cancelou/finalizou uma rota e o app ficou preso nela.", color = Muted, fontFamily = AppFont, fontSize = 13.sp, lineHeight = 18.sp)
            SecondaryButton("Destravar operação", icon = Icons.Filled.Shield, color = Orange, onClick = onForceUnlock)
        }
        PremiumCard {
            Text("Versão", color = Ink, fontFamily = AppFont, fontSize = 18.sp, fontWeight = FontWeight.Black)
            Text("6.18.0 — Reconstrução Real do Entregador", color = Muted, fontFamily = AppFont, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun OccurrenceDialog(onClose: () -> Unit, onConfirm: (String, String) -> Unit) {
    var selected by remember { mutableStateOf("Loja demorando") }
    var details by remember { mutableStateOf("") }
    val reasons = listOf("Problema no veículo", "Loja demorando", "Pedido não encontrado", "Cliente não atende", "Endereço divergente", "Pagamento com problema", "Local inseguro", "Outro")
    AlertDialog(
        onDismissRequest = onClose,
        title = { Text("Problema na rota", color = Ink, fontFamily = AppFont, fontWeight = FontWeight.Black) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                reasons.forEach { reason ->
                    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(if (selected == reason) OrangeSoft else SurfaceSoft).clickable { selected = reason }.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(reason, color = if (selected == reason) Orange else Ink, fontFamily = AppFont, fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                        if (selected == reason) Icon(Icons.Filled.CheckCircle, null, tint = Orange, modifier = Modifier.size(18.dp))
                    }
                }
                OutlinedTextField(value = details, onValueChange = { details = it }, label = { Text("Detalhes opcionais") }, shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = { TextButton(onClick = { onConfirm(selected, details.ifBlank { "Ocorrência enviada pelo app do entregador." }) }) { Text("Enviar", color = Orange, fontFamily = AppFont, fontWeight = FontWeight.Black) } },
        dismissButton = { TextButton(onClick = onClose) { Text("Cancelar", color = Muted, fontFamily = AppFont, fontWeight = FontWeight.Bold) } },
        containerColor = Color.White,
        shape = RoundedCornerShape(28.dp)
    )
}

@Composable
private fun RejectDialog(onClose: () -> Unit, onConfirm: (String) -> Unit) {
    var selected by remember { mutableStateOf("Não consigo atender agora") }
    val reasons = listOf("Não consigo atender agora", "Muito longe", "Sem maquininha", "Sem troco", "Problema no veículo", "Outro")
    AlertDialog(
        onDismissRequest = onClose,
        title = { Text("Recusar corrida", color = Ink, fontFamily = AppFont, fontWeight = FontWeight.Black) },
        text = { Column(verticalArrangement = Arrangement.spacedBy(8.dp)) { reasons.forEach { Text(it, color = if (selected == it) Red else Ink, fontFamily = AppFont, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(if (selected == it) RedSoft else SurfaceSoft).clickable { selected = it }.padding(12.dp)) } } },
        confirmButton = { TextButton(onClick = { onConfirm(selected) }) { Text("Recusar", color = Red, fontFamily = AppFont, fontWeight = FontWeight.Black) } },
        dismissButton = { TextButton(onClick = onClose) { Text("Voltar", color = Muted, fontFamily = AppFont, fontWeight = FontWeight.Bold) } },
        containerColor = Color.White,
        shape = RoundedCornerShape(28.dp)
    )
}

@Composable
private fun PaymentConfirmDialog(ride: DriverRide, machines: List<PaymentMachine>, onClose: () -> Unit, onConfirm: (PaymentSettlementInput) -> Unit) {
    var method by remember { mutableStateOf(ride.paymentUi().label) }
    var transaction by remember { mutableStateOf(if (ride.requiresMachine) "Débito" else "") }
    val machine = machines.firstOrNull { it.active }
    AlertDialog(
        onDismissRequest = onClose,
        title = { Text("Finalizar entrega", color = Ink, fontFamily = AppFont, fontWeight = FontWeight.Black) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Confirme o recebimento antes de finalizar.", color = Muted, fontFamily = AppFont, fontSize = 13.sp)
                listOf("Pago online", "Dinheiro", "Pix", "Maquininha", "Não informado").forEach { opt ->
                    Text(opt, color = if (method == opt) Green else Ink, fontFamily = AppFont, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(if (method == opt) GreenSoft else SurfaceSoft).clickable { method = opt }.padding(12.dp))
                }
                if (method == "Maquininha") {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { listOf("Débito", "Crédito", "Parcelado", "Ticket").forEach { opt -> StatusChip(opt, if (transaction == opt) Green else Muted) } }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onConfirm(PaymentSettlementInput(
                    rideId = ride.id,
                    orderTotal = ride.clientTotalNumber.takeIf { it > 0.0 } ?: ride.amountToCollectNumber,
                    driverFee = ride.valueNumber,
                    paymentMethod = method,
                    transactionType = transaction,
                    machineId = machine?.id ?: "",
                    machineName = machine?.name ?: "",
                    receivedByDriver = method != "Pago online",
                    receivedBy = if (method == "Pago online") "SISTEMA" else "ENTREGADOR",
                    note = "Confirmado no app entregador"
                ))
            }) { Text("Confirmar", color = Green, fontFamily = AppFont, fontWeight = FontWeight.Black) }
        },
        dismissButton = { TextButton(onClick = onClose) { Text("Voltar", color = Muted, fontFamily = AppFont, fontWeight = FontWeight.Bold) } },
        containerColor = Color.White,
        shape = RoundedCornerShape(28.dp)
    )
}

@Composable
private fun EmptyInsideCard(title: String, message: String, icon: ImageVector) {
    PremiumCard {
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(Modifier.size(72.dp).clip(CircleShape).background(GreenSoft), contentAlignment = Alignment.Center) { Icon(icon, null, tint = Green, modifier = Modifier.size(34.dp)) }
            Text(title, color = Ink, fontFamily = AppFont, fontSize = 19.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Center)
            Text(message, color = Muted, fontFamily = AppFont, fontSize = 13.sp, lineHeight = 18.sp, textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun Avatar(name: String, photoUrl: String = "", size: Int) {
    Box(Modifier.size(size.dp).clip(CircleShape).background(GreenSoft).border(2.dp, Color.White, CircleShape), contentAlignment = Alignment.Center) {
        if (photoUrl.isNotBlank()) AsyncImage(model = photoUrl, contentDescription = name, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
        else Text(name.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "E", color = Green, fontFamily = AppFont, fontSize = (size / 2.4).sp, fontWeight = FontWeight.Black)
    }
}

private fun operationalStatus(online: Boolean, activeRide: DriverRide?, permissions: PermissionStatus, context: Context): OperationalStatus {
    val batteryLow = currentBattery(context) in 1..9
    val netOk = hasInternet(context)
    val gpsOk = isGpsEnabled(context)
    return when {
        activeRide != null -> OperationalStatus(AvailabilityKind.EmEntrega, activeRide.stageShort(), "Rota ativa #${activeRide.routeCode()}", Blue, Color.White, false)
        batteryLow -> OperationalStatus(AvailabilityKind.Restricao, "Restrição", "Bateria abaixo de 10%. Carregue o aparelho.", Red, Color.White, false)
        !permissions.notifications -> OperationalStatus(AvailabilityKind.Restricao, "Restrição", "Ative notificações para receber corridas.", Red, Color.White, false)
        !permissions.location || !gpsOk -> OperationalStatus(AvailabilityKind.Restricao, "Restrição", "Ative localização/GPS para ficar disponível.", Red, Color.White, false)
        !permissions.fullScreenIntent -> OperationalStatus(AvailabilityKind.Restricao, "Restrição", "Ative o alerta urgente em tela cheia.", Red, Color.White, false)
        !permissions.batteryUnrestricted -> OperationalStatus(AvailabilityKind.Restricao, "Restrição", "Remova restrição de bateria do app.", Red, Color.White, false)
        !netOk -> OperationalStatus(AvailabilityKind.Restricao, "Sem conexão", "Internet indisponível no momento.", Red, Color.White, false)
        online -> OperationalStatus(AvailabilityKind.Disponivel, "Disponível", "Aguardando corrida da operação.", Green, Color.White, true)
        else -> OperationalStatus(AvailabilityKind.Indisponivel, "Indisponível", "Toque para ficar disponível.", Ink, Color.White, true)
    }
}

private fun DriverProfile.firstName(): String = name.trim().split(" ").firstOrNull().orEmpty().ifBlank { "Entregador" }
private fun DriverRide.routeCode(): String = routeReleaseCode.ifBlank { orderCode.ifBlank { routeId.ifBlank { id }.takeLast(4).uppercase(Locale.ROOT) } }
private fun DriverRide.releaseCode(): String = routeReleaseCode.ifBlank { orderCode.ifBlank { id.takeLast(4).uppercase(Locale.ROOT) } }
private fun DriverRide.stageText(): String = listOf(status, rawStatus, pickupReleaseStatus).joinToString(" ").uppercase(Locale.ROOT)
private fun DriverRide.isOccurrence(): Boolean = stageText().contains("OCORRENCIA") || stageText().contains("OCORRÊNCIA")
private fun DriverRide.isDeliveringStage(): Boolean = stageText().let { it.contains("ENTREGA") || it.contains("CLIENTE") || it.contains("ROTA") || it.contains("SAIU") || it.contains("COM_ENTREGADOR") }
private fun DriverRide.isAtPickup(): Boolean = stageText().let { it.contains("NA_COLETA") || it.contains("COLETA") || it.contains("AGUARDANDO_SAIDA") || it.contains("RETIRADA") }
private fun DriverRide.hasPickupRelease(): Boolean = pickupReleaseAllowed || pickupReleaseStatus.uppercase(Locale.ROOT).let { it.contains("LIBER") || it.contains("COM_ENTREGADOR") || it.contains("SAIU") }
private fun DriverRide.shouldShowReleaseCode(): Boolean = isAtPickup() && !isDeliveringStage()
private fun DriverRide.stageLabel(): String = when {
    isOccurrence() -> "Ocorrência"
    stageText().contains("ENTREGADOR_NO_LOCAL") -> "No cliente"
    isDeliveringStage() -> "Em entrega"
    isAtPickup() && !hasPickupRelease() -> "Aguardando saída"
    isAtPickup() -> "Na coleta"
    stageText().contains("ACEIT") -> "Indo para coleta"
    else -> "Indo para coleta"
}
private fun DriverRide.stageShort(): String = when {
    isOccurrence() -> "Aguardando gestor"
    stageText().contains("ENTREGADOR_NO_LOCAL") -> "No local"
    isDeliveringStage() -> "Em entrega"
    isAtPickup() && !hasPickupRelease() -> "Saída pendente"
    isAtPickup() -> "Na coleta"
    else -> "A caminho"
}
private fun DriverRide.stageColor(): Color = when {
    isOccurrence() -> Orange
    isDeliveringStage() -> Blue
    isAtPickup() -> Green
    else -> Green
}
private fun DriverRide.ordersReadyLabel(): String = if (routeOrderCount > 1) "$routeReadyCount de $routeOrderCount prontos" else "1 pedido pronto"
private fun DriverRide.displayOrders(): List<RouteOrder> = routeOrders.takeIf { it.isNotEmpty() } ?: listOf(RouteOrder(id = id, code = orderCode, customerName = customerName, status = status, paymentSummary = paymentUi().label, ready = true, requiresMachine = requiresMachine, requiresChange = changeForNumber > 0.0))

private fun DriverRide.nextAction(): RouteAction {
    return when {
        isOccurrence() -> RouteAction("Ocorrência enviada", "Aguarde o gestor resolver antes de continuar.", "Aguardando gestor", false, "OCORRENCIA", Orange)
        stageText().contains("ENTREGADOR_NO_LOCAL") -> RouteAction("Finalizar entrega", "Confirme o recebimento/código antes de encerrar.", "Finalizar entrega", true, "FINALIZADA", Green, needsPaymentConfirmation = true)
        isDeliveringStage() -> RouteAction("Próxima ação", "Siga até o cliente e confirme quando chegar.", "Cheguei no cliente", true, "ENTREGADOR_NO_LOCAL", Blue)
        isAtPickup() && !hasPickupRelease() -> RouteAction("Retirada na loja", "Mostre o código e aguarde a saída do gestor.", "Aguardando saída", false, "AGUARDANDO_SAIDA_GESTOR", Orange)
        isAtPickup() && hasPickupRelease() -> RouteAction("Saída liberada", "A rota foi liberada. Inicie a entrega.", "Iniciar entrega", true, "EM_ENTREGA", Green)
        else -> RouteAction("Próxima ação", "Vá até a loja e confirme sua chegada.", "Cheguei na coleta", true, "NA_COLETA", Green)
    }
}

private fun DriverRide.paymentUi(): PaymentUi {
    val method = paymentMethod.uppercase(Locale.ROOT)
    val statusUp = paymentStatus.uppercase(Locale.ROOT)
    val kind = paymentKind()
    return when {
        statusUp.contains("PAGO") || kind == "ONLINE" -> PaymentUi("Pago online", "Nada a cobrar do cliente.", Green, "Nada a cobrar", false, false)
        kind == "DINHEIRO" -> PaymentUi("Dinheiro", if (changeForNumber > 0.0) "Troco para ${DriverRepository.formatCurrency(changeForNumber)}." else "Receber na entrega.", Orange, DriverRepository.formatCurrency(amountToCollectNumber.takeIf { it > 0.0 } ?: clientTotalNumber), false, changeForNumber > 0.0)
        kind == "MAQUININHA" -> PaymentUi("Maquininha", "Receber no cartão/maquininha.", Blue, DriverRepository.formatCurrency(amountToCollectNumber.takeIf { it > 0.0 } ?: clientTotalNumber), true, false)
        kind == "PIX" -> PaymentUi("Pix na entrega", "Confirmar recebimento do Pix.", Green, DriverRepository.formatCurrency(amountToCollectNumber.takeIf { it > 0.0 } ?: clientTotalNumber), false, false)
        method.isBlank() && statusUp.isBlank() -> PaymentUi("Não informado", "Confirme com o gestor antes de finalizar.", Orange, "Confirmar", false, false)
        else -> PaymentUi(paymentMethod.ifBlank { paymentStatus.ifBlank { "Não informado" } }, "Confirme o pagamento com a operação.", Orange, DriverRepository.formatCurrency(amountToCollectNumber.takeIf { it > 0.0 } ?: clientTotalNumber), requiresMachine, changeForNumber > 0.0)
    }
}

private fun historyLabel(action: String): String = when {
    action.contains("CANCEL", true) -> "Cancelada"
    action.contains("RECUS", true) -> "Recusada"
    action.contains("EXPIR", true) -> "Expirada"
    action.contains("OCOR", true) -> "Ocorrência"
    action.contains("FINAL", true) || action.contains("ENTREG", true) -> "Finalizada"
    action.contains("ACEIT", true) -> "Aceita"
    else -> action.lowercase(Locale.ROOT).replaceFirstChar { it.uppercase() }
}
private fun moneyMaybe(value: Double?, hide: Boolean): String = if (hide) "••••" else DriverRepository.formatCurrency(value ?: 0.0)
private fun hasInternet(context: Context): Boolean = runCatching {
    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val net = cm.activeNetwork ?: return false
    val caps = cm.getNetworkCapabilities(net) ?: return false
    caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
}.getOrDefault(false)
private fun isGpsEnabled(context: Context): Boolean = runCatching { (context.getSystemService(Context.LOCATION_SERVICE) as LocationManager).isProviderEnabled(LocationManager.GPS_PROVIDER) }.getOrDefault(false)
private fun currentBattery(context: Context): Int = runCatching { (context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager).getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) }.getOrDefault(100)
