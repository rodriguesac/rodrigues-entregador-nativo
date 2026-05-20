package com.rodriguesacai.entregador.ui

import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.BatteryManager
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.rodriguesacai.entregador.AppSettings
import com.rodriguesacai.entregador.PermissionStatus
import com.rodriguesacai.entregador.PermissionStatusReader
import com.rodriguesacai.entregador.RodriguesFonts
import com.rodriguesacai.entregador.data.AppBanner
import com.rodriguesacai.entregador.data.DriverHistory
import com.rodriguesacai.entregador.data.DriverNotice
import com.rodriguesacai.entregador.data.DriverProfile
import com.rodriguesacai.entregador.data.DriverRegistrationRequest
import com.rodriguesacai.entregador.data.DriverRepository
import com.rodriguesacai.entregador.data.DriverRide
import com.rodriguesacai.entregador.data.DriverStats
import com.rodriguesacai.entregador.data.RealtimeExtrasRepository
import com.rodriguesacai.entregador.service.NotificationHelper
import kotlinx.coroutines.delay
import java.util.Locale

private enum class AppTab { Inicio, Corridas, Carteira, Notificacoes, Mais }
private enum class LoginMode { Login, Cadastro, CriarSenha }
private enum class AvailabilityKind { Disponivel, Indisponivel, Restricao, EmCorrida }

private val AppFont = RodriguesFonts.Montserrat
private val Bg = Color(0xFFF5F8F1)
private val SurfaceWhite = Color.White
private val SurfaceSoft = Color(0xFFF9FBF6)
private val Primary = Color(0xFF22A447)
private val PrimaryDark = Color(0xFF13752F)
private val PrimarySoft = Color(0xFFEAF7EE)
private val Ink = Color(0xFF162016)
private val Muted = Color(0xFF687469)
private val Border = Color(0xFFE1E8DD)
private val Warning = Color(0xFFF59E0B)
private val Danger = Color(0xFFDC2626)
private val Blue = Color(0xFF2563EB)

private data class AvailabilityState(
    val kind: AvailabilityKind,
    val label: String,
    val message: String,
    val canGoOnline: Boolean,
    val color: Color
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
    var mode by remember { mutableStateOf(if (profile?.needsPasswordSetup == true) LoginMode.CriarSenha else LoginMode.Login) }
    var activeTab by remember { mutableStateOf(AppTab.Inicio) }
    var online by remember { mutableStateOf(false) }
    var pendingRide by remember { mutableStateOf<DriverRide?>(null) }
    var activeRide by remember { mutableStateOf<DriverRide?>(null) }
    var history by remember { mutableStateOf<List<DriverHistory>>(emptyList()) }
    var stats by remember { mutableStateOf(DriverStats()) }
    var banners by remember { mutableStateOf<List<AppBanner>>(emptyList()) }
    var notices by remember { mutableStateOf<List<DriverNotice>>(emptyList()) }
    var error by remember { mutableStateOf("") }
    var notice by remember { mutableStateOf("") }
    var hideValues by remember { mutableStateOf(AppSettings.getHideValues(context)) }
    var permissions by remember { mutableStateOf(PermissionStatusReader.read(context)) }

    LaunchedEffect(Unit) {
        while (true) {
            permissions = PermissionStatusReader.read(context)
            delay(3000)
        }
    }

    DisposableEffect(profile?.id, online) {
        val pendingListener = if (profile != null && online) {
            DriverRepository.listenPendingRide(
                context = context,
                onRide = { pendingRide = it },
                onError = { error = it }
            )
        } else null
        val activeListener = if (profile != null) {
            DriverRepository.listenMyActiveRide(
                context = context,
                onRide = { activeRide = it },
                onError = { error = it }
            )
        } else null
        val historyListener = if (profile != null) {
            DriverRepository.listenMyHistory(
                context = context,
                onHistory = { history = it },
                onError = { error = it }
            )
        } else null
        val statsListener = if (profile != null) {
            DriverRepository.listenDailyStats(
                context = context,
                onStats = { stats = it },
                onError = { error = it }
            )
        } else null
        val bannerListener = if (profile != null) {
            RealtimeExtrasRepository.listenAppBanners(
                onBanners = { banners = it },
                onError = { error = it }
            )
        } else null
        val noticeListener = if (profile != null) {
            RealtimeExtrasRepository.listenNotifications(
                context = context,
                onNotifications = { notices = it },
                onError = { error = it }
            )
        } else null
        onDispose {
            pendingListener?.remove()
            activeListener?.remove()
            historyListener?.remove()
            statsListener?.remove()
            bannerListener?.remove()
            noticeListener?.remove()
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
        when (mode) {
            LoginMode.Cadastro -> RegisterScreen(
                error = error,
                notice = notice,
                onBack = { error = ""; notice = ""; mode = LoginMode.Login },
                onRegister = { request, setLoading ->
                    error = ""
                    notice = ""
                    setLoading(true)
                    DriverRepository.registerDriver(
                        request = request,
                        onSuccess = {
                            setLoading(false)
                            notice = "Cadastro enviado. Aguarde a aprovação do gestor."
                            mode = LoginMode.Login
                        },
                        onError = {
                            setLoading(false)
                            error = it
                        }
                    )
                }
            )
            else -> LoginScreen(
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
                            mode = if (it.needsPasswordSetup) LoginMode.CriarSenha else LoginMode.Login
                            setLoading(false)
                        },
                        onError = {
                            error = it
                            setLoading(false)
                        }
                    )
                },
                onRegister = { mode = LoginMode.Cadastro }
            )
        }
        return
    }

    if (profile?.needsPasswordSetup == true || mode == LoginMode.CriarSenha) {
        CreatePasswordScreen(
            profile = profile!!,
            error = error,
            notice = notice,
            onSave = { password, setLoading ->
                error = ""
                notice = ""
                setLoading(true)
                DriverRepository.updateAccessPassword(
                    context = context,
                    newPassword = password,
                    onSuccess = {
                        val updated = DriverRepository.currentSession(context)
                        profile = updated ?: profile?.copy(needsPasswordSetup = false)
                        mode = LoginMode.Login
                        setLoading(false)
                        notice = "Senha criada com sucesso."
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

    val operational = remember(online, activeRide, permissions) {
        readAvailability(context, online, activeRide, permissions)
    }

    Scaffold(
        containerColor = Bg,
        bottomBar = {
            BottomNavigation(
                selected = activeTab,
                unread = notices.count { !it.read },
                onSelect = { activeTab = it }
            )
        }
    ) { inner ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Bg)
                .padding(inner)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 18.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Header(
                    profile = profile!!,
                    notices = notices,
                    onNotifications = { activeTab = AppTab.Notificacoes },
                    onMessages = {
                        activeTab = AppTab.Notificacoes
                        notice = "Mensagens reais aparecerão aqui quando o gestor cadastrar avisos ou conversas."
                    }
                )

                if (error.isNotBlank()) AlertCard(text = error, color = Danger) { error = "" }
                if (notice.isNotBlank()) AlertCard(text = notice, color = PrimaryDark) { notice = "" }

                when (activeTab) {
                    AppTab.Inicio -> HomeTab(
                        profile = profile!!,
                        operational = operational,
                        pendingRide = pendingRide,
                        activeRide = activeRide,
                        stats = stats,
                        hideValues = hideValues,
                        banners = banners,
                        permissions = permissions,
                        onToggleValues = {
                            hideValues = !hideValues
                            AppSettings.setHideValues(context, hideValues)
                        },
                        onToggleOnline = {
                            if (online) {
                                online = false
                                pendingRide = null
                                DriverRepository.setOnline(context, false)
                                onGoOffline()
                            } else {
                                if (!operational.canGoOnline) {
                                    notice = operational.message
                                } else {
                                    online = true
                                    DriverRepository.setOnline(context, true)
                                    onGoOnline()
                                }
                            }
                        },
                        onAccept = { ride ->
                            DriverRepository.acceptRide(
                                context = context,
                                rideId = ride.id,
                                onDone = {
                                    notice = "Corrida aceita."
                                    pendingRide = null
                                    activeTab = AppTab.Corridas
                                },
                                onError = { error = it }
                            )
                        },
                        onReject = { ride ->
                            DriverRepository.rejectRide(
                                context = context,
                                rideId = ride.id,
                                reason = "Recusada pelo entregador",
                                onDone = {
                                    notice = "Corrida recusada."
                                    pendingRide = null
                                },
                                onError = { error = it }
                            )
                        },
                        onQuickHistory = { activeTab = AppTab.Carteira },
                        onQuickWallet = { activeTab = AppTab.Carteira },
                        onQuickMap = { activeTab = AppTab.Corridas },
                        onQuickSupport = { activeTab = AppTab.Notificacoes },
                        onOpenPermission = { target ->
                            when (target) {
                                "notificacoes" -> onOpenNotificationSettings()
                                "localizacao" -> onOpenLocationSettings()
                                "tela_cheia" -> onOpenFullScreenSettings()
                                "bateria" -> onOpenBatterySettings()
                            }
                        }
                    )

                    AppTab.Corridas -> RidesTab(
                        online = online,
                        operational = operational,
                        pendingRide = pendingRide,
                        activeRide = activeRide,
                        onAccept = { ride ->
                            DriverRepository.acceptRide(
                                context = context,
                                rideId = ride.id,
                                onDone = {
                                    notice = "Corrida aceita."
                                    pendingRide = null
                                },
                                onError = { error = it }
                            )
                        },
                        onReject = { ride ->
                            DriverRepository.rejectRide(
                                context = context,
                                rideId = ride.id,
                                reason = "Recusada pelo entregador",
                                onDone = { pendingRide = null; notice = "Corrida recusada." },
                                onError = { error = it }
                            )
                        },
                        onNextStatus = { ride, status ->
                            DriverRepository.updateRideStatus(
                                context = context,
                                rideId = ride.id,
                                status = status,
                                onDone = { notice = statusDoneMessage(status) },
                                onError = { error = it }
                            )
                        },
                        onOccurrence = { ride, reason ->
                            DriverRepository.registerRideOccurrence(
                                context = context,
                                rideId = ride.id,
                                reason = reason,
                                note = "Registrado pelo app do entregador",
                                onDone = { notice = "Ocorrência registrada e corrida mantida em aberto." },
                                onError = { error = it }
                            )
                        },
                        onOpenNavigator = onOpenNavigator
                    )

                    AppTab.Carteira -> WalletTab(
                        stats = stats,
                        history = history,
                        hideValues = hideValues,
                        onToggleValues = {
                            hideValues = !hideValues
                            AppSettings.setHideValues(context, hideValues)
                        }
                    )

                    AppTab.Notificacoes -> NotificationsTab(notices = notices)

                    AppTab.Mais -> MoreTab(
                        profile = profile!!,
                        permissions = permissions,
                        onLogout = {
                            DriverRepository.logout(context) {
                                profile = null
                                online = false
                                pendingRide = null
                                activeRide = null
                                activeTab = AppTab.Inicio
                            }
                        },
                        onSavePayout = { pix, bank, setLoading ->
                            setLoading(true)
                            DriverRepository.updatePayoutData(
                                context = context,
                                pixKey = pix,
                                bankName = bank,
                                payoutType = "Pix",
                                onSuccess = {
                                    setLoading(false)
                                    profile = DriverRepository.currentSession(context) ?: profile
                                    notice = "Dados de recebimento enviados para conferência."
                                },
                                onError = {
                                    setLoading(false)
                                    error = it
                                }
                            )
                        },
                        onRequestChange = { text, setLoading ->
                            setLoading(true)
                            DriverRepository.requestProfileChange(
                                context = context,
                                requestText = text,
                                onSuccess = {
                                    setLoading(false)
                                    notice = "Solicitação enviada ao gestor."
                                },
                                onError = {
                                    setLoading(false)
                                    error = it
                                }
                            )
                        },
                        onOpenNotificationSettings = onOpenNotificationSettings,
                        onOpenLocationSettings = onOpenLocationSettings,
                        onOpenFullScreenSettings = onOpenFullScreenSettings,
                        onOpenBatterySettings = onOpenBatterySettings
                    )
                }
                Spacer(Modifier.height(18.dp))
            }
        }
    }
}

@Composable
private fun LoginScreen(
    error: String,
    notice: String,
    onLogin: (String, String, (Boolean) -> Unit) -> Unit,
    onRegister: () -> Unit
) {
    var document by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var showPassword by remember { mutableStateOf(false) }
    var helper by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Bg)
            .padding(22.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            LogoBlock()
            CardBox {
                Text("Bem-vindo", fontFamily = AppFont, color = Ink, fontSize = 30.sp, fontWeight = FontWeight.Black)
                Text("Acesse sua conta para receber corridas reais da operação.", fontFamily = AppFont, color = Muted, fontSize = 14.sp)
                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = document,
                    onValueChange = { document = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("CPF ou telefone") },
                    leadingIcon = { Icon(Icons.Filled.Phone, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Senha") },
                    leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) },
                    trailingIcon = {
                        IconButton(onClick = { showPassword = !showPassword }) {
                            Icon(if (showPassword) Icons.Filled.VisibilityOff else Icons.Filled.Visibility, contentDescription = null)
                        }
                    },
                    visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true
                )

                TextButton(onClick = { helper = "Peça ao gestor para redefinir sua senha no painel de entregadores." }) {
                    Text("Esqueci minha senha", fontFamily = AppFont, color = PrimaryDark)
                }

                PrimaryButton(
                    text = if (loading) "Entrando..." else "Entrar",
                    enabled = !loading,
                    onClick = { onLogin(document, password) { loading = it } }
                )
                OutlinedButton(
                    onClick = onRegister,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    contentPadding = PaddingValues(vertical = 14.dp)
                ) {
                    Text("Solicitar cadastro", fontFamily = AppFont, color = PrimaryDark, fontWeight = FontWeight.Bold)
                }

                if (error.isNotBlank()) InlineMessage(error, Danger)
                if (notice.isNotBlank()) InlineMessage(notice, PrimaryDark)
                if (helper.isNotBlank()) InlineMessage(helper, PrimaryDark)

                Text("Cadastro sujeito à aprovação do gestor.", fontFamily = AppFont, color = Muted, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun RegisterScreen(
    error: String,
    notice: String,
    onBack: () -> Unit,
    onRegister: (DriverRegistrationRequest, (Boolean) -> Unit) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var cpf by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var pix by remember { mutableStateOf("") }
    var bank by remember { mutableStateOf("") }
    var plate by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Bg)
            .padding(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            LogoBlock()
            CardBox {
                Text("Solicitar cadastro", fontFamily = AppFont, color = Ink, fontSize = 26.sp, fontWeight = FontWeight.Black)
                Text("Envie seus dados reais para aprovação no painel gestor.", fontFamily = AppFont, color = Muted, fontSize = 14.sp)
                FormField(value = name, onChange = { name = it }, label = "Nome completo")
                FormField(value = cpf, onChange = { cpf = it }, label = "CPF", keyboardType = KeyboardType.Number)
                FormField(value = phone, onChange = { phone = it }, label = "Telefone/WhatsApp", keyboardType = KeyboardType.Phone)
                FormField(value = password, onChange = { password = it }, label = "Senha", password = true)
                FormField(value = plate, onChange = { plate = it }, label = "Placa da moto")
                FormField(value = pix, onChange = { pix = it }, label = "Chave Pix")
                FormField(value = bank, onChange = { bank = it }, label = "Banco")
                PrimaryButton(
                    text = if (loading) "Enviando..." else "Enviar cadastro",
                    enabled = !loading,
                    onClick = {
                        onRegister(
                            DriverRegistrationRequest(
                                name = name,
                                cpf = cpf,
                                phone = phone,
                                password = password,
                                vehicle = "Moto",
                                plate = plate,
                                pixKey = pix,
                                bankName = bank
                            )
                        ) { loading = it }
                    }
                )
                OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp)) {
                    Text("Voltar para login", fontFamily = AppFont, color = PrimaryDark, fontWeight = FontWeight.Bold)
                }
                if (error.isNotBlank()) InlineMessage(error, Danger)
                if (notice.isNotBlank()) InlineMessage(notice, PrimaryDark)
            }
        }
    }
}

@Composable
private fun CreatePasswordScreen(
    profile: DriverProfile,
    error: String,
    notice: String,
    onSave: (String, (Boolean) -> Unit) -> Unit
) {
    var password by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Bg)
            .padding(22.dp),
        contentAlignment = Alignment.Center
    ) {
        CardBox {
            Text("Crie sua senha", fontFamily = AppFont, color = Ink, fontSize = 26.sp, fontWeight = FontWeight.Black)
            Text("Olá, ${profile.name.firstName()}. Para proteger sua conta, defina uma senha de acesso.", fontFamily = AppFont, color = Muted)
            FormField(value = password, onChange = { password = it }, label = "Nova senha", password = true)
            FormField(value = confirm, onChange = { confirm = it }, label = "Confirmar senha", password = true)
            PrimaryButton(
                text = if (loading) "Salvando..." else "Salvar senha",
                enabled = !loading,
                onClick = {
                    if (password == confirm) {
                        onSave(password) { loading = it }
                    }
                }
            )
            if (password != confirm && confirm.isNotBlank()) InlineMessage("As senhas não conferem.", Danger)
            if (error.isNotBlank()) InlineMessage(error, Danger)
            if (notice.isNotBlank()) InlineMessage(notice, PrimaryDark)
        }
    }
}

@Composable
private fun Header(
    profile: DriverProfile,
    notices: List<DriverNotice>,
    onNotifications: () -> Unit,
    onMessages: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        DriverAvatar(profile)
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text("Olá, ${profile.name.firstName()}", fontFamily = AppFont, color = Ink, fontSize = 24.sp, fontWeight = FontWeight.Black)
            Text("Pronto para receber corridas", fontFamily = AppFont, color = Muted, fontSize = 13.sp)
        }
        RoundIconButton(Icons.Filled.Notifications, hasDot = notices.any { !it.read }, onClick = onNotifications)
        Spacer(Modifier.width(8.dp))
        RoundIconButton(Icons.Filled.Chat, hasDot = false, onClick = onMessages)
    }
}

@Composable
private fun HomeTab(
    profile: DriverProfile,
    operational: AvailabilityState,
    pendingRide: DriverRide?,
    activeRide: DriverRide?,
    stats: DriverStats,
    hideValues: Boolean,
    banners: List<AppBanner>,
    permissions: PermissionStatus,
    onToggleValues: () -> Unit,
    onToggleOnline: () -> Unit,
    onAccept: (DriverRide) -> Unit,
    onReject: (DriverRide) -> Unit,
    onQuickHistory: () -> Unit,
    onQuickWallet: () -> Unit,
    onQuickMap: () -> Unit,
    onQuickSupport: () -> Unit,
    onOpenPermission: (String) -> Unit
) {
    StatusButton(operational = operational, activeRide = activeRide, onClick = onToggleOnline)

    EarningsCard(stats = stats, hideValues = hideValues, onToggleValues = onToggleValues)

    if (pendingRide != null) {
        PendingRideCard(ride = pendingRide, onAccept = { onAccept(pendingRide) }, onReject = { onReject(pendingRide) })
    }

    if (activeRide != null) {
        CompactActiveRide(ride = activeRide, onOpen = onQuickMap)
    }

    BannerCarousel(banners = banners)

    QuickActions(
        onHistory = onQuickHistory,
        onWallet = onQuickWallet,
        onMap = onQuickMap,
        onSupport = onQuickSupport
    )

    PermissionChecklist(permissions = permissions, onOpen = onOpenPermission)
}

@Composable
private fun RidesTab(
    online: Boolean,
    operational: AvailabilityState,
    pendingRide: DriverRide?,
    activeRide: DriverRide?,
    onAccept: (DriverRide) -> Unit,
    onReject: (DriverRide) -> Unit,
    onNextStatus: (DriverRide, String) -> Unit,
    onOccurrence: (DriverRide, String) -> Unit,
    onOpenNavigator: (pickup: String, dropoff: String) -> Unit
) {
    SectionTitle("Corridas", "Ofertas e rota atual vindas do Firebase.")
    if (!online && pendingRide == null && activeRide == null) {
        EmptyState("Você está indisponível", operational.message)
    }
    if (pendingRide != null) {
        PendingRideCard(ride = pendingRide, onAccept = { onAccept(pendingRide) }, onReject = { onReject(pendingRide) })
    }
    if (activeRide != null) {
        ActiveRideDetails(
            ride = activeRide,
            onNextStatus = { status -> onNextStatus(activeRide, status) },
            onOccurrence = { reason -> onOccurrence(activeRide, reason) },
            onOpenNavigator = { onOpenNavigator(activeRide.pickup, activeRide.dropoff) }
        )
    }
    if (pendingRide == null && activeRide == null && online) {
        EmptyState("Nenhuma corrida real disponível", "Quando a torre liberar uma corrida para este entregador, ela aparecerá aqui e tocará na tela urgente.")
    }
}

@Composable
private fun WalletTab(
    stats: DriverStats,
    history: List<DriverHistory>,
    hideValues: Boolean,
    onToggleValues: () -> Unit
) {
    SectionTitle("Carteira", "Valores calculados somente pelo histórico real.")
    EarningsCard(stats = stats, hideValues = hideValues, onToggleValues = onToggleValues)
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
        MiniMetric("Semana", if (hideValues) "••••" else DriverRepository.formatCurrency(stats.totalWeek), Modifier.weight(1f))
        MiniMetric("Mês", if (hideValues) "••••" else DriverRepository.formatCurrency(stats.totalMonth), Modifier.weight(1f))
    }
    SectionTitle("Histórico", "Uma linha por corrida/pedido.")
    if (history.isEmpty()) {
        EmptyState("Sem histórico real", "As corridas finalizadas, recusadas e expiradas aparecerão aqui quando existirem no banco.")
    } else {
        history.forEach { item -> HistoryCard(item) }
    }
}

@Composable
private fun NotificationsTab(notices: List<DriverNotice>) {
    SectionTitle("Notificações", "Avisos reais enviados pelo gestor.")
    if (notices.isEmpty()) {
        EmptyState("Nenhuma notificação real", "Quando houver avisos cadastrados no Firebase, eles aparecerão aqui.")
    } else {
        notices.forEach { notice ->
            CardBox {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Notifications, contentDescription = null, tint = if (notice.read) Muted else Primary)
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text(notice.title, fontFamily = AppFont, color = Ink, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        if (notice.body.isNotBlank()) Text(notice.body, fontFamily = AppFont, color = Muted, fontSize = 13.sp)
                        Text("${notice.type} • ${notice.createdLabel}", fontFamily = AppFont, color = Muted, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun MoreTab(
    profile: DriverProfile,
    permissions: PermissionStatus,
    onLogout: () -> Unit,
    onSavePayout: (String, String, (Boolean) -> Unit) -> Unit,
    onRequestChange: (String, (Boolean) -> Unit) -> Unit,
    onOpenNotificationSettings: () -> Unit,
    onOpenLocationSettings: () -> Unit,
    onOpenFullScreenSettings: () -> Unit,
    onOpenBatterySettings: () -> Unit
) {
    val context = LocalContext.current
    var pix by remember(profile.id) { mutableStateOf(profile.pixKey) }
    var bank by remember(profile.id) { mutableStateOf(profile.bankName) }
    var requestText by remember { mutableStateOf("") }
    var loadingPayout by remember { mutableStateOf(false) }
    var loadingRequest by remember { mutableStateOf(false) }
    var navApp by remember { mutableStateOf(AppSettings.getNavigationApp(context)) }

    SectionTitle("Mais", "Conta, recebimento e permissões.")
    CardBox {
        Row(verticalAlignment = Alignment.CenterVertically) {
            DriverAvatar(profile)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(profile.name, fontFamily = AppFont, color = Ink, fontSize = 18.sp, fontWeight = FontWeight.Black)
                Text(if (profile.verified) "Perfil verificado" else "Perfil sem selo", fontFamily = AppFont, color = PrimaryDark, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                if (profile.phone.isNotBlank()) Text(profile.phone, fontFamily = AppFont, color = Muted, fontSize = 12.sp)
            }
        }
    }

    CardBox {
        Text("Recebimento", fontFamily = AppFont, color = Ink, fontWeight = FontWeight.Black, fontSize = 18.sp)
        Text("A conta precisa estar no nome do titular cadastrado.", fontFamily = AppFont, color = Muted, fontSize = 12.sp)
        FormField(value = pix, onChange = { pix = it }, label = "Chave Pix")
        FormField(value = bank, onChange = { bank = it }, label = "Banco")
        PrimaryButton(
            text = if (loadingPayout) "Salvando..." else "Salvar recebimento",
            enabled = !loadingPayout,
            onClick = { onSavePayout(pix, bank) { loadingPayout = it } }
        )
    }

    CardBox {
        Text("Solicitar alteração", fontFamily = AppFont, color = Ink, fontWeight = FontWeight.Black, fontSize = 18.sp)
        Text("Use para telefone, e-mail ou dados pessoais que exigem aprovação.", fontFamily = AppFont, color = Muted, fontSize = 12.sp)
        FormField(value = requestText, onChange = { requestText = it }, label = "Descreva a alteração")
        PrimaryButton(
            text = if (loadingRequest) "Enviando..." else "Enviar solicitação",
            enabled = !loadingRequest,
            onClick = { onRequestChange(requestText) { loadingRequest = it } }
        )
    }

    CardBox {
        Text("Navegação padrão", fontFamily = AppFont, color = Ink, fontWeight = FontWeight.Black, fontSize = 18.sp)
        SettingChoice("Padrão do celular", navApp == AppSettings.NAV_AUTO) { navApp = AppSettings.NAV_AUTO; AppSettings.setNavigationApp(context, navApp) }
        SettingChoice("Google Maps", navApp == AppSettings.NAV_GOOGLE) { navApp = AppSettings.NAV_GOOGLE; AppSettings.setNavigationApp(context, navApp) }
        SettingChoice("Waze", navApp == AppSettings.NAV_WAZE) { navApp = AppSettings.NAV_WAZE; AppSettings.setNavigationApp(context, navApp) }
    }

    CardBox {
        Text("Permissões", fontFamily = AppFont, color = Ink, fontWeight = FontWeight.Black, fontSize = 18.sp)
        PermissionLine("Notificações", permissions.notifications, onOpenNotificationSettings)
        PermissionLine("Localização", permissions.location, onOpenLocationSettings)
        PermissionLine("Tela cheia urgente", permissions.fullScreenIntent, onOpenFullScreenSettings)
        PermissionLine("Bateria sem restrição", permissions.batteryUnrestricted, onOpenBatterySettings)
    }

    OutlinedButton(
        onClick = onLogout,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        contentPadding = PaddingValues(vertical = 14.dp)
    ) {
        Icon(Icons.Filled.Logout, contentDescription = null, tint = Danger)
        Spacer(Modifier.width(8.dp))
        Text("Sair", fontFamily = AppFont, color = Danger, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun StatusButton(operational: AvailabilityState, activeRide: DriverRide?, onClick: () -> Unit) {
    val enabled = operational.kind != AvailabilityKind.EmCorrida || activeRide == null
    Button(
        onClick = onClick,
        enabled = enabled || operational.kind != AvailabilityKind.EmCorrida,
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp),
        shape = RoundedCornerShape(22.dp),
        colors = ButtonDefaults.buttonColors(containerColor = operational.color, disabledContainerColor = operational.color.copy(alpha = 0.90f)),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        Icon(Icons.Filled.Circle, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(operational.label, fontFamily = AppFont, color = Color.White, fontWeight = FontWeight.Black, fontSize = 18.sp)
            Text(operational.message, fontFamily = AppFont, color = Color.White.copy(alpha = 0.88f), fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Icon(Icons.Filled.KeyboardArrowDown, contentDescription = null, tint = Color.White)
    }
}

@Composable
private fun EarningsCard(stats: DriverStats, hideValues: Boolean, onToggleValues: () -> Unit) {
    CardBox {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Ganhos de hoje", fontFamily = AppFont, color = Muted, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Text(if (hideValues) "R$ ••••" else DriverRepository.formatCurrency(stats.totalToday), fontFamily = AppFont, color = Ink, fontWeight = FontWeight.Black, fontSize = 28.sp)
            }
            IconButton(onClick = onToggleValues) {
                Icon(if (hideValues) Icons.Filled.VisibilityOff else Icons.Filled.Visibility, contentDescription = null, tint = Muted)
            }
            Divider(modifier = Modifier.height(48.dp).width(1.dp), color = Border)
            Spacer(Modifier.width(12.dp))
            Column(horizontalAlignment = Alignment.End) {
                Text("${stats.finishedCount}", fontFamily = AppFont, color = Ink, fontWeight = FontWeight.Black, fontSize = 18.sp)
                Text("Finalizadas", fontFamily = AppFont, color = Muted, fontSize = 11.sp)
                Text("${stats.score}%", fontFamily = AppFont, color = PrimaryDark, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }
    }
}

@Composable
private fun BannerCarousel(banners: List<AppBanner>) {
    val banner = banners.firstOrNull()
    if (banner == null) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = PrimarySoft),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Notifications, contentDescription = null, tint = PrimaryDark)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text("Carrossel sem banners reais", fontFamily = AppFont, color = Ink, fontWeight = FontWeight.Black, fontSize = 17.sp)
                    Text("Cadastre banners no painel gestor para aparecerem aqui.", fontFamily = AppFont, color = Muted, fontSize = 12.sp)
                }
            }
        }
        return
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(30.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(listOf(PrimaryDark, Primary)),
                    RoundedCornerShape(30.dp)
                )
                .padding(20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(color = Color.White.copy(alpha = 0.18f), shape = RoundedCornerShape(50.dp)) {
                        Text(banner.label.uppercase(Locale.ROOT), fontFamily = AppFont, color = Color.White, fontWeight = FontWeight.Black, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp))
                    }
                    Text(banner.title.ifBlank { "Aviso" }, fontFamily = AppFont, color = Color.White, fontWeight = FontWeight.Black, fontSize = 21.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    if (banner.subtitle.isNotBlank()) Text(banner.subtitle, fontFamily = AppFont, color = Color.White.copy(alpha = 0.88f), fontSize = 12.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    Surface(color = Color.White, shape = RoundedCornerShape(16.dp)) {
                        Text(banner.actionText.ifBlank { "Abrir" }, fontFamily = AppFont, color = PrimaryDark, fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp))
                    }
                }
                if (banner.imageUrl.isNotBlank()) {
                    AsyncImage(
                        model = banner.imageUrl,
                        contentDescription = banner.title,
                        modifier = Modifier
                            .size(96.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(Color.White.copy(alpha = 0.16f))
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(96.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(Color.White.copy(alpha = 0.16f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("up", fontFamily = AppFont, color = Color.White, fontWeight = FontWeight.Black, fontSize = 28.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickActions(onHistory: () -> Unit, onWallet: () -> Unit, onMap: () -> Unit, onSupport: () -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        QuickAction(Icons.Filled.History, "Histórico", "Ver corridas", Modifier.weight(1f), onHistory)
        QuickAction(Icons.Filled.AccountBalanceWallet, "Ganhos", "Resumo", Modifier.weight(1f), onWallet)
    }
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        QuickAction(Icons.Filled.Map, "Mapa", "Ver rota", Modifier.weight(1f), onMap)
        QuickAction(Icons.Filled.SupportAgent, "Suporte", "Avisos", Modifier.weight(1f), onSupport)
    }
}

@Composable
private fun QuickAction(icon: ImageVector, title: String, subtitle: String, modifier: Modifier, onClick: () -> Unit) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Icon(icon, contentDescription = null, tint = Primary, modifier = Modifier.size(24.dp))
            Text(title, fontFamily = AppFont, color = Ink, fontWeight = FontWeight.Black, fontSize = 15.sp)
            Text(subtitle, fontFamily = AppFont, color = Muted, fontSize = 11.sp)
        }
    }
}

@Composable
private fun PendingRideCard(ride: DriverRide, onAccept: () -> Unit, onReject: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Primary.copy(alpha = 0.28f), RoundedCornerShape(28.dp)),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(color = PrimarySoft, shape = RoundedCornerShape(50.dp)) {
                    Text("NOVA CORRIDA", fontFamily = AppFont, color = PrimaryDark, fontWeight = FontWeight.Black, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp))
                }
                Spacer(Modifier.weight(1f))
                Text(ride.value, fontFamily = AppFont, color = PrimaryDark, fontWeight = FontWeight.Black, fontSize = 22.sp)
            }
            Text("Pedido ${ride.orderCode}", fontFamily = AppFont, color = Ink, fontWeight = FontWeight.Black, fontSize = 19.sp)
            RideLine("Coleta", ride.pickup)
            RideLine("Entrega", ride.neighborhood.ifBlank { ride.dropoff })
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                MiniPill(ride.distance)
                MiniPill(ride.duration)
                MiniPill("${ride.stops} parada(s)")
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(onClick = onReject, modifier = Modifier.weight(1f), shape = RoundedCornerShape(18.dp)) {
                    Text("Recusar", fontFamily = AppFont, color = Danger, fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = onAccept,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary)
                ) {
                    Text("Aceitar", fontFamily = AppFont, fontWeight = FontWeight.Black)
                }
            }
        }
    }
}

@Composable
private fun CompactActiveRide(ride: DriverRide, onOpen: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onOpen() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Route, contentDescription = null, tint = Primary)
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text("Corrida em andamento", fontFamily = AppFont, color = Ink, fontWeight = FontWeight.Black)
                Text("Pedido ${ride.orderCode} • ${ride.status.humanStatus(ride.rawStatus)}", fontFamily = AppFont, color = Muted, fontSize = 12.sp)
            }
            Icon(Icons.Filled.KeyboardArrowRight, contentDescription = null, tint = Muted)
        }
    }
}

@Composable
private fun ActiveRideDetails(
    ride: DriverRide,
    onNextStatus: (String) -> Unit,
    onOccurrence: (String) -> Unit,
    onOpenNavigator: () -> Unit
) {
    CardBox {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Pedido ${ride.orderCode}", fontFamily = AppFont, color = Ink, fontWeight = FontWeight.Black, fontSize = 22.sp)
                Text(ride.status.humanStatus(ride.rawStatus), fontFamily = AppFont, color = PrimaryDark, fontWeight = FontWeight.Bold)
            }
            Text(ride.value, fontFamily = AppFont, color = PrimaryDark, fontWeight = FontWeight.Black, fontSize = 20.sp)
        }
        RealDeliveryMap(
            title = "Mapa da rota",
            subtitle = ride.status.humanStatus(ride.rawStatus),
            pickupAddress = ride.pickup,
            dropoffAddress = ride.dropoff,
            pickupLat = ride.pickupLat,
            pickupLng = ride.pickupLng,
            dropoffLat = ride.dropoffLat,
            dropoffLng = ride.dropoffLng,
            mode = when (ride.status) {
                "accepted" -> DeliveryMapMode.DRIVER_TO_PICKUP
                "pickup" -> DeliveryMapMode.DRIVER_TO_PICKUP
                else -> DeliveryMapMode.DRIVER_TO_DROPOFF
            }
        )
        RideLine("Coleta", ride.pickup)
        RideLine("Entrega", ride.dropoff)
        RideLine("Cliente", ride.customerName)
        RideLine("Pagamento", ride.paymentMethod)
        if (ride.amountToCollectNumber > 0.0) RideLine("Receber do cliente", DriverRepository.formatCurrency(ride.amountToCollectNumber))
        if (ride.storeReturnNumber > 0.0) RideLine("Repassar para loja", DriverRepository.formatCurrency(ride.storeReturnNumber))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            MiniPill(ride.distance)
            MiniPill(ride.duration)
            MiniPill(ride.collectionName)
        }
        Button(
            onClick = onOpenNavigator,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Blue)
        ) {
            Icon(Icons.Filled.Navigation, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Iniciar navegação", fontFamily = AppFont, fontWeight = FontWeight.Black)
        }
        PrimaryButton(text = nextStatusLabel(ride), onClick = { onNextStatus(nextStatusCode(ride)) })
        OccurrenceSelector(onOccurrence = onOccurrence)
    }
}

@Composable
private fun OccurrenceSelector(onOccurrence: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val reasons = listOf("Cliente não atende", "Endereço divergente", "Local inseguro", "Pagamento pendente", "Pedido danificado", "Cliente ausente", "Aguardando cliente", "Outro motivo")
    OutlinedButton(onClick = { expanded = !expanded }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp)) {
        Icon(Icons.Filled.ErrorOutline, contentDescription = null, tint = Warning)
        Spacer(Modifier.width(8.dp))
        Text("Registrar ocorrência no local", fontFamily = AppFont, color = Warning, fontWeight = FontWeight.Bold)
    }
    if (expanded) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            reasons.forEach { reason ->
                Surface(
                    modifier = Modifier.fillMaxWidth().clickable { expanded = false; onOccurrence(reason) },
                    color = Color(0xFFFFF7ED),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(reason, fontFamily = AppFont, color = Ink, modifier = Modifier.padding(12.dp), fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun PermissionChecklist(permissions: PermissionStatus, onOpen: (String) -> Unit) {
    CardBox {
        Text("Checklist operacional", fontFamily = AppFont, color = Ink, fontWeight = FontWeight.Black, fontSize = 18.sp)
        PermissionLine("Notificações", permissions.notifications) { onOpen("notificacoes") }
        PermissionLine("Localização", permissions.location) { onOpen("localizacao") }
        PermissionLine("Alerta tela cheia", permissions.fullScreenIntent) { onOpen("tela_cheia") }
        PermissionLine("Bateria sem restrição", permissions.batteryUnrestricted) { onOpen("bateria") }
    }
}

@Composable
private fun PermissionLine(title: String, ok: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { if (!ok) onClick() }
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(if (ok) Icons.Filled.CheckCircle else Icons.Filled.ErrorOutline, contentDescription = null, tint = if (ok) Primary else Warning)
        Spacer(Modifier.width(10.dp))
        Text(title, fontFamily = AppFont, color = Ink, modifier = Modifier.weight(1f), fontWeight = FontWeight.SemiBold)
        Text(if (ok) "OK" else "Ajustar", fontFamily = AppFont, color = if (ok) PrimaryDark else Warning, fontWeight = FontWeight.Bold, fontSize = 12.sp)
    }
}

@Composable
private fun BottomNavigation(selected: AppTab, unread: Int, onSelect: (AppTab) -> Unit) {
    NavigationBar(containerColor = SurfaceWhite, tonalElevation = 4.dp) {
        BottomItem(selected, AppTab.Inicio, Icons.Filled.Home, "Início", 0, onSelect)
        BottomItem(selected, AppTab.Corridas, Icons.Filled.Route, "Corridas", 0, onSelect)
        BottomItem(selected, AppTab.Carteira, Icons.Filled.AccountBalanceWallet, "Carteira", 0, onSelect)
        BottomItem(selected, AppTab.Notificacoes, Icons.Filled.Notifications, "Notificações", unread, onSelect)
        BottomItem(selected, AppTab.Mais, Icons.Filled.Person, "Mais", 0, onSelect)
    }
}

@Composable
private fun BottomItem(selected: AppTab, tab: AppTab, icon: ImageVector, label: String, badge: Int, onSelect: (AppTab) -> Unit) {
    NavigationBarItem(
        selected = selected == tab,
        onClick = { onSelect(tab) },
        icon = {
            Box {
                Icon(icon, contentDescription = label)
                if (badge > 0) DotBadge(modifier = Modifier.align(Alignment.TopEnd))
            }
        },
        label = { Text(label, fontFamily = AppFont, fontSize = 10.sp, maxLines = 1) },
        colors = NavigationBarItemDefaults.colors(
            selectedIconColor = Primary,
            selectedTextColor = Primary,
            indicatorColor = PrimarySoft,
            unselectedIconColor = Muted,
            unselectedTextColor = Muted
        )
    )
}

@Composable
private fun LogoBlock() {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .size(82.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(Brush.linearGradient(listOf(Primary, PrimaryDark))),
            contentAlignment = Alignment.Center
        ) {
            Text("up", fontFamily = AppFont, color = Color.White, fontWeight = FontWeight.Black, fontSize = 34.sp)
        }
        Spacer(Modifier.height(8.dp))
        Text("entregas", fontFamily = AppFont, color = PrimaryDark, fontWeight = FontWeight.Black, fontSize = 18.sp)
    }
}

@Composable
private fun DriverAvatar(profile: DriverProfile) {
    if (profile.photoUrl.isNotBlank()) {
        AsyncImage(
            model = profile.photoUrl,
            contentDescription = "Foto do entregador",
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(PrimarySoft)
                .border(2.dp, SurfaceWhite, CircleShape)
        )
    } else {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(PrimarySoft)
                .border(2.dp, SurfaceWhite, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(profile.name.initials(), fontFamily = AppFont, color = PrimaryDark, fontWeight = FontWeight.Black, fontSize = 18.sp)
        }
    }
}

@Composable
private fun RoundIconButton(icon: ImageVector, hasDot: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(46.dp)
            .clip(CircleShape)
            .background(SurfaceWhite)
            .border(1.dp, Border, CircleShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = Ink)
        if (hasDot) DotBadge(modifier = Modifier.align(Alignment.TopEnd).padding(8.dp))
    }
}

@Composable
private fun DotBadge(modifier: Modifier = Modifier) {
    Box(modifier = modifier.size(9.dp).clip(CircleShape).background(Primary))
}

@Composable
private fun CardBox(content: @Composable Column.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp), content = content)
    }
}

@Composable
private fun SectionTitle(title: String, subtitle: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(title, fontFamily = AppFont, color = Ink, fontWeight = FontWeight.Black, fontSize = 24.sp)
        Text(subtitle, fontFamily = AppFont, color = Muted, fontSize = 12.sp)
    }
}

@Composable
private fun PrimaryButton(text: String, enabled: Boolean = true, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Primary, disabledContainerColor = Border),
        contentPadding = PaddingValues(vertical = 14.dp)
    ) {
        if (!enabled && text.contains("...")) CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
        else Text(text, fontFamily = AppFont, fontWeight = FontWeight.Black, fontSize = 15.sp)
    }
}

@Composable
private fun FormField(value: String, onChange: (String) -> Unit, label: String, keyboardType: KeyboardType = KeyboardType.Text, password: Boolean = false) {
    var visible by remember { mutableStateOf(false) }
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(label) },
        visualTransformation = if (password && !visible) PasswordVisualTransformation() else VisualTransformation.None,
        trailingIcon = if (password) {
            { IconButton(onClick = { visible = !visible }) { Icon(if (visible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility, contentDescription = null) } }
        } else null,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        singleLine = true
    )
}

@Composable
private fun InlineMessage(text: String, color: Color) {
    Surface(color = color.copy(alpha = 0.10f), shape = RoundedCornerShape(16.dp)) {
        Text(text, fontFamily = AppFont, color = color, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(12.dp))
    }
}

@Composable
private fun AlertCard(text: String, color: Color, onDismiss: () -> Unit) {
    Surface(color = color.copy(alpha = 0.10f), shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(text, fontFamily = AppFont, color = color, modifier = Modifier.weight(1f), fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
            Text("Fechar", fontFamily = AppFont, color = color, fontWeight = FontWeight.Black, fontSize = 12.sp, modifier = Modifier.clickable { onDismiss() })
        }
    }
}

@Composable
private fun EmptyState(title: String, message: String) {
    CardBox {
        Icon(Icons.Filled.FilterList, contentDescription = null, tint = Muted, modifier = Modifier.size(30.dp))
        Text(title, fontFamily = AppFont, color = Ink, fontWeight = FontWeight.Black, fontSize = 18.sp)
        Text(message, fontFamily = AppFont, color = Muted, fontSize = 13.sp)
    }
}

@Composable
private fun RideLine(label: String, value: String) {
    if (value.isBlank()) return
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        Text(label, fontFamily = AppFont, color = Muted, fontSize = 12.sp, modifier = Modifier.width(86.dp))
        Text(value, fontFamily = AppFont, color = Ink, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun MiniPill(text: String) {
    Surface(color = SurfaceSoft, shape = RoundedCornerShape(50.dp), border = androidx.compose.foundation.BorderStroke(1.dp, Border)) {
        Text(text.ifBlank { "--" }, fontFamily = AppFont, color = Ink, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp))
    }
}

@Composable
private fun MiniMetric(title: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(title, fontFamily = AppFont, color = Muted, fontSize = 12.sp)
            Text(value, fontFamily = AppFont, color = Ink, fontWeight = FontWeight.Black, fontSize = 18.sp)
        }
    }
}

@Composable
private fun HistoryCard(item: DriverHistory) {
    CardBox {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.History, contentDescription = null, tint = Primary)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text("Pedido ${item.rideId}", fontFamily = AppFont, color = Ink, fontWeight = FontWeight.Black, fontSize = 16.sp)
                Text(item.action.humanHistory(), fontFamily = AppFont, color = Muted, fontSize = 12.sp)
                Text(item.createdLabel, fontFamily = AppFont, color = Muted, fontSize = 11.sp)
            }
            Text(item.value, fontFamily = AppFont, color = PrimaryDark, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
private fun SettingChoice(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).clickable { onClick() }.padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(if (selected) Icons.Filled.CheckCircle else Icons.Filled.Circle, contentDescription = null, tint = if (selected) Primary else Border, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(10.dp))
        Text(label, fontFamily = AppFont, color = Ink, fontWeight = FontWeight.SemiBold)
    }
}

private fun readAvailability(context: Context, online: Boolean, activeRide: DriverRide?, permissions: PermissionStatus): AvailabilityState {
    if (activeRide != null) {
        return AvailabilityState(AvailabilityKind.EmCorrida, "Em corrida", activeRide.status.humanStatus(activeRide.rawStatus), false, Blue)
    }
    val battery = runCatching {
        val bm = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
    }.getOrDefault(100)
    if (battery in 1..9) return AvailabilityState(AvailabilityKind.Restricao, "Restrição", "Bateria abaixo de 10%", false, Danger)
    if (!hasInternet(context)) return AvailabilityState(AvailabilityKind.Restricao, "Restrição", "Sem conexão com internet", false, Danger)
    if (!isGpsEnabled(context)) return AvailabilityState(AvailabilityKind.Restricao, "Restrição", "Ative a localização", false, Danger)
    if (!permissions.location) return AvailabilityState(AvailabilityKind.Restricao, "Restrição", "Permita localização", false, Danger)
    if (!permissions.notifications) return AvailabilityState(AvailabilityKind.Restricao, "Restrição", "Permita notificações", false, Danger)
    if (!permissions.fullScreenIntent) return AvailabilityState(AvailabilityKind.Restricao, "Restrição", "Permita alerta urgente", false, Danger)
    if (!permissions.batteryUnrestricted) return AvailabilityState(AvailabilityKind.Restricao, "Restrição", "Remova restrição de bateria", false, Danger)
    return if (online) AvailabilityState(AvailabilityKind.Disponivel, "Disponível", "Aguardando corridas reais", true, Primary)
    else AvailabilityState(AvailabilityKind.Indisponivel, "Indisponível", "Toque para ficar disponível", true, Color(0xFF1F2937))
}

private fun hasInternet(context: Context): Boolean {
    val manager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val network = manager.activeNetwork ?: return false
    val capabilities = manager.getNetworkCapabilities(network) ?: return false
    return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
}

private fun isGpsEnabled(context: Context): Boolean {
    return runCatching {
        val manager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        manager.isProviderEnabled(LocationManager.GPS_PROVIDER) || manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }.getOrDefault(true)
}

private fun nextStatusCode(ride: DriverRide): String {
    val raw = ride.rawStatus.uppercase(Locale.ROOT)
    return when {
        ride.status == "accepted" -> "pickup"
        ride.status == "pickup" -> "delivering"
        ride.status == "delivering" && raw.contains("ENTREGADOR_NO_LOCAL") -> "finished"
        ride.status == "delivering" -> "arrived_client"
        else -> "pickup"
    }
}

private fun nextStatusLabel(ride: DriverRide): String {
    val raw = ride.rawStatus.uppercase(Locale.ROOT)
    return when {
        ride.status == "accepted" -> "Cheguei na coleta"
        ride.status == "pickup" -> "Pedido retirado"
        ride.status == "delivering" && raw.contains("ENTREGADOR_NO_LOCAL") -> "Finalizar entrega"
        ride.status == "delivering" -> "Cheguei no cliente"
        else -> "Atualizar status"
    }
}

private fun statusDoneMessage(status: String): String = when (status) {
    "pickup" -> "Status atualizado: na coleta."
    "delivering" -> "Status atualizado: em rota de entrega."
    "arrived_client" -> "Status atualizado: entregador chegou no cliente."
    "finished" -> "Entrega finalizada."
    else -> "Status atualizado."
}

private fun String.humanStatus(raw: String = this): String {
    val r = raw.uppercase(Locale.ROOT)
    return when {
        r.contains("ENTREGADOR_NO_LOCAL") -> "Chegou no cliente"
        this == "pending" -> "Oferta recebida"
        this == "accepted" -> "Indo para coleta"
        this == "pickup" -> "Na coleta"
        this == "delivering" -> "Em rota"
        this == "finished" -> "Finalizada"
        r.contains("REJEIT") -> "Recusada"
        r.contains("EXPIR") -> "Expirada"
        else -> raw.ifBlank { "Status não informado" }
    }
}

private fun String.humanHistory(): String {
    val v = uppercase(Locale.ROOT)
    return when {
        v.contains("ACEIT") -> "Aceita"
        v.contains("COLETA") || v.contains("COLET") -> "Na coleta"
        v.contains("ROTA") || v.contains("ENTREGA") -> "Em rota"
        v.contains("FINAL") || v.contains("CONCL") || v.contains("ENTREGUE") -> "Finalizada"
        v.contains("REJEIT") -> "Recusada"
        v.contains("EXPIR") -> "Expirada"
        v.contains("OCORR") -> "Ocorrência"
        else -> replace('_', ' ').lowercase(Locale.ROOT).replaceFirstChar { it.uppercase() }
    }
}

private fun String.firstName(): String = trim().split(" ").firstOrNull()?.ifBlank { "Entregador" } ?: "Entregador"

private fun String.initials(): String {
    val parts = trim().split(" ").filter { it.isNotBlank() }
    if (parts.isEmpty()) return "UP"
    return parts.take(2).joinToString("") { it.first().uppercaseChar().toString() }
}
