
package com.rodriguesacai.entregador

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.ArrowForwardIos
import androidx.compose.material.icons.rounded.ChatBubbleOutline
import androidx.compose.material.icons.rounded.CheckCircleOutline
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.FilterAlt
import androidx.compose.material.icons.rounded.HeadsetMic
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Logout
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.NotificationsNone
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Place
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material.icons.rounded.Work
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat

private val UpGreen = Color(0xFF087D2B)
private val UpGreenDark = Color(0xFF006B28)
private val UpGreenSoft = Color(0xFFEAF6EA)
private val AppBg = Color(0xFFFAFBF7)
private val CardStroke = Color(0xFFE4E8DE)
private val TextStrong = Color(0xFF171A23)
private val TextMuted = Color(0xFF737985)
private val Warning = Color(0xFFFF6B3D)
private val AppFont = FontFamily.SansSerif

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = Color.Transparent.toArgb()
        window.navigationBarColor = Color.White.toArgb()
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = true
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightNavigationBars = true

        setContent {
            UpEntregasApp()
        }
    }
}

@Composable
private fun UpEntregasApp() {
    val colors = lightColorScheme(
        primary = UpGreen,
        onPrimary = Color.White,
        secondary = UpGreenDark,
        background = AppBg,
        surface = Color.White,
        onSurface = TextStrong,
        outline = CardStroke
    )

    MaterialTheme(
        colorScheme = colors,
        typography = MaterialTheme.typography
    ) {
        var loggedIn by remember { mutableStateOf(false) }
        Surface(modifier = Modifier.fillMaxSize(), color = AppBg) {
            if (loggedIn) {
                DriverMainScreen(onLogout = { loggedIn = false })
            } else {
                LoginScreen(onLogin = { loggedIn = true })
            }
        }
    }
}

@Composable
private fun LoginScreen(onLogin: () -> Unit) {
    var document by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(WindowInsets.statusBars.asPaddingValues())
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 26.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Spacer(modifier = Modifier.height(22.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                LogoUp(size = 92)
                Spacer(modifier = Modifier.weight(1f))
                Image(
                    painter = painterResource(id = R.drawable.login_illustration),
                    contentDescription = null,
                    modifier = Modifier
                        .size(160.dp)
                        .clip(RoundedCornerShape(30.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(22.dp))

            Text(
                text = "Bem-vindo",
                color = TextStrong,
                fontFamily = AppFont,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 29.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Acesse sua conta para receber corridas.",
                color = TextMuted,
                fontFamily = AppFont,
                lineHeight = 22.sp,
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.height(46.dp))

            FieldLabel("CPF ou telefone")
            OutlinedTextField(
                value = document,
                onValueChange = { document = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(62.dp),
                leadingIcon = { Icon(Icons.Rounded.Person, null, tint = TextMuted) },
                placeholder = { Text("Digite seu CPF ou telefone", color = TextMuted) },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                textStyle = TextStyle(fontFamily = AppFont, fontSize = 15.sp),
                colors = textFieldColors()
            )

            Spacer(modifier = Modifier.height(20.dp))

            FieldLabel("Senha")
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(62.dp),
                leadingIcon = { Icon(Icons.Rounded.Lock, null, tint = TextMuted) },
                trailingIcon = {
                    IconButton(onClick = { showPassword = !showPassword }) {
                        Icon(
                            if (showPassword) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                            contentDescription = null,
                            tint = TextMuted
                        )
                    }
                },
                placeholder = { Text("Digite sua senha", color = TextMuted) },
                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                textStyle = TextStyle(fontFamily = AppFont, fontSize = 15.sp),
                colors = textFieldColors()
            )

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Esqueci minha senha",
                color = UpGreen,
                fontFamily = AppFont,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(vertical = 8.dp)
            )

            Spacer(modifier = Modifier.height(18.dp))

            Button(
                onClick = onLogin,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = UpGreen)
            ) {
                Text(
                    text = "Entrar",
                    fontFamily = AppFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = { },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.5.dp, UpGreen),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = UpGreen)
            ) {
                Text(
                    text = "Solicitar cadastro",
                    fontFamily = AppFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 34.dp, bottom = 16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Rounded.Security, null, tint = TextMuted, modifier = Modifier.size(19.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Cadastro sujeito à aprovação",
                color = TextMuted,
                fontFamily = AppFont,
                fontSize = 13.sp
            )
        }
    }
}

@Composable
private fun textFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = UpGreen,
    unfocusedBorderColor = CardStroke,
    focusedContainerColor = Color.White,
    unfocusedContainerColor = Color.White,
    cursorColor = UpGreen,
    focusedTextColor = TextStrong,
    unfocusedTextColor = TextStrong
)

@Composable
private fun FieldLabel(text: String) {
    Text(
        text = text,
        color = TextStrong,
        fontFamily = AppFont,
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

private enum class AppTab(val label: String, val icon: ImageVector) {
    Inicio("Início", Icons.Rounded.Home),
    Corridas("Corridas", Icons.Rounded.Work),
    Carteira("Carteira", Icons.Rounded.AccountBalanceWallet),
    Notificacoes("Notificações", Icons.Rounded.NotificationsNone),
    Mais("Mais", Icons.Rounded.Menu)
}

@Composable
private fun DriverMainScreen(onLogout: () -> Unit) {
    var selectedTab by remember { mutableStateOf(AppTab.Inicio) }

    Scaffold(
        containerColor = AppBg,
        bottomBar = {
            DriverBottomBar(selected = selectedTab, onSelect = { selectedTab = it })
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                AppTab.Inicio -> HomeScreen()
                AppTab.Corridas -> RidesScreen()
                AppTab.Carteira -> WalletScreen()
                AppTab.Notificacoes -> NotificationsScreen()
                AppTab.Mais -> MoreScreen(onLogout = onLogout)
            }
        }
    }
}

@Composable
private fun DriverBottomBar(selected: AppTab, onSelect: (AppTab) -> Unit) {
    NavigationBar(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        tonalElevation = 10.dp,
        containerColor = Color.White
    ) {
        AppTab.entries.forEach { tab ->
            NavigationBarItem(
                selected = selected == tab,
                onClick = { onSelect(tab) },
                icon = {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = tab.label,
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = {
                    Text(
                        text = tab.label,
                        fontFamily = AppFont,
                        fontSize = 11.sp,
                        maxLines = 1
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = UpGreen,
                    selectedTextColor = UpGreen,
                    indicatorColor = UpGreenSoft,
                    unselectedIconColor = Color(0xFF7B828B),
                    unselectedTextColor = Color(0xFF7B828B)
                )
            )
        }
    }
}

@Composable
private fun HomeScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBg)
            .padding(WindowInsets.statusBars.asPaddingValues())
            .verticalScroll(rememberScrollState())
            .padding(start = 22.dp, end = 22.dp, top = 14.dp, bottom = 22.dp)
    ) {
        DriverHeader()
        Spacer(Modifier.height(20.dp))
        StatusButton()
        Spacer(Modifier.height(20.dp))
        EarningsCard()
        Spacer(Modifier.height(18.dp))
        HomeCarousel()
        Spacer(Modifier.height(22.dp))
        QuickActionsGrid()
    }
}

@Composable
private fun DriverHeader() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(R.drawable.avatar_diego),
            contentDescription = "Foto do entregador",
            modifier = Modifier
                .size(68.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Olá, Diego",
                color = TextStrong,
                fontFamily = AppFont,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 22.sp
            )
            Spacer(Modifier.height(5.dp))
            Text(
                text = "Pronto para receber corridas",
                color = TextMuted,
                fontFamily = AppFont,
                fontSize = 14.sp
            )
        }

        Box {
            Icon(
                imageVector = Icons.Rounded.NotificationsNone,
                contentDescription = "Notificações",
                tint = TextStrong,
                modifier = Modifier.size(29.dp)
            )
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = (-2).dp, y = 2.dp)
                    .clip(CircleShape)
                    .background(UpGreen)
            )
        }
        Spacer(modifier = Modifier.width(18.dp))
        Icon(
            imageVector = Icons.Rounded.ChatBubbleOutline,
            contentDescription = "Mensagens",
            tint = TextStrong,
            modifier = Modifier.size(28.dp)
        )
    }
}

@Composable
private fun StatusButton() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(Color(0xFF05752B), Color(0xFF008435), Color(0xFF05752B))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Rounded.Wifi, null, tint = Color.White, modifier = Modifier.size(25.dp))
            Spacer(Modifier.width(12.dp))
            Text(
                text = "Disponível",
                color = Color.White,
                fontFamily = AppFont,
                fontWeight = FontWeight.Bold,
                fontSize = 19.sp
            )
        }
        Icon(
            imageVector = Icons.Rounded.KeyboardArrowDown,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 18.dp)
                .size(28.dp)
        )
    }
}

@Composable
private fun EarningsCard() {
    ElevatedWhiteCard(
        modifier = Modifier
            .fillMaxWidth()
            .height(112.dp),
        shape = 24
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Ganhos de hoje", color = TextMuted, fontSize = 15.sp, fontFamily = AppFont)
                    Spacer(Modifier.weight(1f))
                    Icon(Icons.Rounded.Visibility, null, tint = TextMuted, modifier = Modifier.size(20.dp))
                }
                Spacer(Modifier.height(14.dp))
                Text(
                    text = "R\$ 128,50",
                    color = TextStrong,
                    fontFamily = AppFont,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 33.sp,
                    maxLines = 1
                )
            }

            Spacer(Modifier.width(14.dp))
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .fillMaxHeight()
                    .background(CardStroke)
            )
            Spacer(Modifier.width(14.dp))

            Column(
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.width(116.dp)
            ) {
                MetricLine(Icons.Rounded.Work, "8", "Corridas")
                Spacer(Modifier.height(14.dp))
                MetricLine(Icons.Rounded.CheckCircleOutline, "7", "Finalizadas")
            }
        }
    }
}

@Composable
private fun MetricLine(icon: ImageVector, value: String, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = TextMuted, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(10.dp))
        Text(value, color = TextStrong, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, fontFamily = AppFont)
        Spacer(Modifier.width(6.dp))
        Text(label, color = TextMuted, fontSize = 12.sp, fontFamily = AppFont)
    }
}

@Composable
private fun HomeCarousel() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Image(
                painter = painterResource(R.drawable.banner_novidades),
                contentDescription = "Novidades da operação",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(196.dp)
                    .clip(RoundedCornerShape(24.dp))
            )
        }
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .width(22.dp)
                    .height(8.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(UpGreen)
            )
            Spacer(Modifier.width(8.dp))
            Dot(active = false)
            Spacer(Modifier.width(8.dp))
            Dot(active = false)
        }
    }
}

@Composable
private fun Dot(active: Boolean) {
    Box(
        modifier = Modifier
            .size(if (active) 9.dp else 8.dp)
            .clip(CircleShape)
            .background(if (active) UpGreen else Color(0xFFD0D5D2))
    )
}

@Composable
private fun QuickActionsGrid() {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        QuickAction(Icons.Rounded.History, "Histórico", "Ver corridas", Modifier.weight(1f))
        QuickAction(Icons.Rounded.AccountBalanceWallet, "Ganhos", "Resumo\nfinanceiro", Modifier.weight(1f))
        QuickAction(Icons.Rounded.Place, "Mapa", "Ver região", Modifier.weight(1f))
        QuickAction(Icons.Rounded.HeadsetMic, "Suporte", "Fale conosco", Modifier.weight(1f))
    }
}

@Composable
private fun QuickAction(icon: ImageVector, title: String, subtitle: String, modifier: Modifier = Modifier) {
    ElevatedWhiteCard(
        modifier = modifier.height(128.dp),
        shape = 18
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 11.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, null, tint = UpGreen, modifier = Modifier.size(34.dp))
            Spacer(Modifier.height(9.dp))
            Text(
                text = title,
                color = TextStrong,
                fontFamily = AppFont,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 13.5.sp,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = subtitle,
                color = TextMuted,
                fontFamily = AppFont,
                fontSize = 11.5.sp,
                textAlign = TextAlign.Center,
                lineHeight = 15.sp,
                maxLines = 2
            )
        }
    }
}

data class Ride(
    val tag: String,
    val tagColor: Color,
    val pickup: String,
    val delivery: String,
    val distance: String,
    val price: String,
    val eta: String
)

@Composable
private fun RidesScreen() {
    val rides = listOf(
        Ride("Curta", Color(0xFFE6F4E6), "Rua das Flores, 123", "Av. Paulista, 1000", "1,2 km", "R\$ 12,50", "18 min"),
        Ride("Média", Color(0xFFFFF2DD), "Shopping Eldorado", "Rua Augusta, 500", "4,5 km", "R\$ 18,90", "25 min"),
        Ride("Longa", Color(0xFFFFE8E8), "Aeroporto de Congonhas", "Vila Mariana", "8,7 km", "R\$ 32,80", "40 min")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBg)
            .padding(WindowInsets.statusBars.asPaddingValues())
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 22.dp, vertical = 16.dp)
    ) {
        ScreenTitle("Corridas", trailing = {
            Icon(Icons.Rounded.FilterAlt, null, tint = TextStrong, modifier = Modifier.size(27.dp))
        })
        Spacer(Modifier.height(20.dp))
        SegmentTabs(listOf("Disponíveis", "Em andamento", "Histórico"), selectedIndex = 0)
        Spacer(Modifier.height(18.dp))

        ElevatedWhiteCard(
            modifier = Modifier.fillMaxWidth(),
            shape = 18,
            container = UpGreenSoft
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Rounded.Wifi, null, tint = UpGreen, modifier = Modifier.size(28.dp))
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Fique disponível para receber mais corridas",
                        fontFamily = AppFont,
                        fontWeight = FontWeight.Bold,
                        color = TextStrong,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Quanto mais tempo online, mais oportunidades!",
                        fontFamily = AppFont,
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                }
                Icon(Icons.Rounded.ChevronRight, null, tint = UpGreen)
            }
        }

        Spacer(Modifier.height(18.dp))
        rides.forEach {
            RideCard(it)
            Spacer(Modifier.height(14.dp))
        }
    }
}

@Composable
private fun SegmentTabs(labels: List<String>, selectedIndex: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        labels.forEachIndexed { index, label ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = label,
                    color = if (index == selectedIndex) UpGreen else TextStrong,
                    fontFamily = AppFont,
                    fontWeight = if (index == selectedIndex) FontWeight.ExtraBold else FontWeight.Medium,
                    fontSize = 14.sp
                )
                Spacer(Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .height(2.dp)
                        .width(90.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (index == selectedIndex) UpGreen else Color.Transparent)
                )
            }
        }
    }
}

@Composable
private fun RideCard(ride: Ride) {
    ElevatedWhiteCard(
        modifier = Modifier.fillMaxWidth(),
        shape = 20
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(ride.tagColor)
                        .padding(horizontal = 12.dp, vertical = 5.dp)
                ) {
                    Text(
                        ride.tag,
                        color = when (ride.tag) {
                            "Longa" -> Color(0xFFBF3B3B)
                            "Média" -> Color(0xFFAD6A00)
                            else -> UpGreen
                        },
                        fontFamily = AppFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
                Spacer(Modifier.weight(1f))
                Text(ride.distance, color = TextMuted, fontFamily = AppFont, fontSize = 12.sp)
            }

            Spacer(Modifier.height(16.dp))

            Row {
                Column(modifier = Modifier.weight(1f)) {
                    LocationLine(UpGreen, ride.pickup)
                    Spacer(Modifier.height(12.dp))
                    LocationLine(Warning, ride.delivery)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        ride.price,
                        color = UpGreen,
                        fontFamily = AppFont,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(ride.distance, color = TextStrong, fontFamily = AppFont, fontSize = 12.sp)
                }
            }

            Spacer(Modifier.height(18.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.AccessTime, null, tint = TextMuted, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Estimativa: ${ride.eta}", color = TextMuted, fontFamily = AppFont, fontSize = 12.sp)
                Spacer(Modifier.weight(1f))
                Button(
                    onClick = { },
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 18.dp, vertical = 8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = UpGreenSoft, contentColor = UpGreen)
                ) {
                    Text("Aceitar", fontFamily = AppFont, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun LocationLine(color: Color, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(Modifier.width(10.dp))
        Text(
            text,
            color = TextStrong,
            fontFamily = AppFont,
            fontSize = 14.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun WalletScreen() {
    var visible by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBg)
            .padding(WindowInsets.statusBars.asPaddingValues())
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 22.dp, vertical = 16.dp)
    ) {
        ScreenTitle("Carteira", trailing = {
            IconButton(onClick = { visible = !visible }) {
                Icon(if (visible) Icons.Rounded.Visibility else Icons.Rounded.VisibilityOff, null, tint = TextStrong)
            }
        })

        Spacer(Modifier.height(18.dp))
        ElevatedWhiteCard(modifier = Modifier.fillMaxWidth(), shape = 26) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Ganhos disponíveis", color = TextMuted, fontFamily = AppFont, fontSize = 14.sp)
                Spacer(Modifier.height(8.dp))
                Text(
                    text = if (visible) "R\$ 128,50" else "••••••",
                    color = TextStrong,
                    fontFamily = AppFont,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 38.sp
                )
                Spacer(Modifier.height(12.dp))
                Text("Próximo repasse: sexta-feira", color = TextMuted, fontFamily = AppFont, fontSize = 13.sp)
            }
        }

        Spacer(Modifier.height(18.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            SmallBalance("Hoje", if (visible) "R\$ 128,50" else "••••", Modifier.weight(1f))
            SmallBalance("Semana", if (visible) "R\$ 642,00" else "••••", Modifier.weight(1f))
        }
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            SmallBalance("Mês", if (visible) "R\$ 2.380,00" else "••••", Modifier.weight(1f))
            SmallBalance("Corridas", "32", Modifier.weight(1f))
        }

        Spacer(Modifier.height(24.dp))
        Text("Resumo", color = TextStrong, fontFamily = AppFont, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
        Spacer(Modifier.height(12.dp))
        FinanceRow("Taxas de entrega", if (visible) "R\$ 118,50" else "••••")
        FinanceRow("Gorjetas", if (visible) "R\$ 10,00" else "••••")
        FinanceRow("Descontos", if (visible) "R\$ 0,00" else "••••")
    }
}

@Composable
private fun SmallBalance(title: String, value: String, modifier: Modifier = Modifier) {
    ElevatedWhiteCard(modifier = modifier.height(96.dp), shape = 18) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.Center) {
            Text(title, color = TextMuted, fontFamily = AppFont, fontSize = 13.sp)
            Spacer(Modifier.height(8.dp))
            Text(value, color = TextStrong, fontFamily = AppFont, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
        }
    }
}

@Composable
private fun FinanceRow(title: String, value: String) {
    ElevatedWhiteCard(modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp), shape = 16) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(title, color = TextStrong, fontFamily = AppFont, fontSize = 15.sp, modifier = Modifier.weight(1f))
            Text(value, color = UpGreen, fontFamily = AppFont, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
        }
    }
}

@Composable
private fun NotificationsScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBg)
            .padding(WindowInsets.statusBars.asPaddingValues())
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 22.dp, vertical = 16.dp)
    ) {
        ScreenTitle("Notificações")
        Spacer(Modifier.height(18.dp))
        NotificationItem("Nova campanha", "Fique atento aos banners e avisos do gestor.", "Agora")
        NotificationItem("Pagamento atualizado", "Seu resumo financeiro foi sincronizado.", "Hoje")
        NotificationItem("Permissões", "Ative notificações e localização para receber ofertas urgentes.", "Ontem")
    }
}

@Composable
private fun NotificationItem(title: String, body: String, time: String) {
    ElevatedWhiteCard(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp), shape = 18) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(UpGreenSoft),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.Notifications, null, tint = UpGreen)
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = TextStrong, fontFamily = AppFont, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
                Spacer(Modifier.height(4.dp))
                Text(body, color = TextMuted, fontFamily = AppFont, fontSize = 12.sp, lineHeight = 16.sp)
            }
            Text(time, color = TextMuted, fontFamily = AppFont, fontSize = 11.sp)
        }
    }
}

@Composable
private fun MoreScreen(onLogout: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBg)
            .padding(WindowInsets.statusBars.asPaddingValues())
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 22.dp, vertical = 16.dp)
    ) {
        ScreenTitle("Mais")
        Spacer(Modifier.height(18.dp))
        ElevatedWhiteCard(modifier = Modifier.fillMaxWidth(), shape = 24) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(R.drawable.avatar_diego),
                    contentDescription = null,
                    modifier = Modifier.size(62.dp).clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
                Spacer(Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Diego Rodrigues", color = TextStrong, fontFamily = AppFont, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                    Text("Entregador verificado", color = UpGreen, fontFamily = AppFont, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
                Icon(Icons.Rounded.ChevronRight, null, tint = TextMuted)
            }
        }
        Spacer(Modifier.height(18.dp))
        MenuRow(Icons.Rounded.Settings, "Configurações", "Preferências do app")
        MenuRow(Icons.Rounded.Place, "Navegação", "Google Maps, Waze ou padrão")
        MenuRow(Icons.Rounded.Security, "Permissões", "Localização e alerta urgente")
        MenuRow(Icons.Rounded.HeadsetMic, "Suporte", "Fale com a operação")
        Spacer(Modifier.height(12.dp))
        OutlinedButton(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth().height(54.dp),
            border = BorderStroke(1.dp, Color(0xFFFFB4A9)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(Icons.Rounded.Logout, null, tint = Color(0xFFE24537))
            Spacer(Modifier.width(8.dp))
            Text("Sair", color = Color(0xFFE24537), fontFamily = AppFont, fontWeight = FontWeight.ExtraBold)
        }
    }
}

@Composable
private fun MenuRow(icon: ImageVector, title: String, subtitle: String) {
    ElevatedWhiteCard(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp), shape = 18) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(UpGreenSoft),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = UpGreen, modifier = Modifier.size(23.dp))
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = TextStrong, fontFamily = AppFont, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
                Text(subtitle, color = TextMuted, fontFamily = AppFont, fontSize = 12.sp)
            }
            Icon(Icons.Rounded.ChevronRight, null, tint = TextMuted)
        }
    }
}

@Composable
private fun ScreenTitle(title: String, trailing: (@Composable () -> Unit)? = null) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            title,
            color = TextStrong,
            fontFamily = AppFont,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 27.sp,
            modifier = Modifier.weight(1f)
        )
        trailing?.invoke()
    }
}

@Composable
private fun ElevatedWhiteCard(
    modifier: Modifier = Modifier,
    shape: Int = 20,
    container: Color = Color.White,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier.border(1.dp, CardStroke, RoundedCornerShape(shape.dp)),
        shape = RoundedCornerShape(shape.dp),
        colors = CardDefaults.cardColors(containerColor = container),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        content()
    }
}

@Composable
private fun LogoUp(size: Int) {
    Column {
        Text(
            text = "up",
            color = UpGreen,
            fontFamily = AppFont,
            fontWeight = FontWeight.Black,
            fontSize = (size / 2.2f).sp,
            lineHeight = (size / 2.5f).sp
        )
        Text(
            text = "entregas",
            color = UpGreen,
            fontFamily = AppFont,
            fontWeight = FontWeight.ExtraBold,
            fontSize = (size / 6f).sp
        )
    }
}
