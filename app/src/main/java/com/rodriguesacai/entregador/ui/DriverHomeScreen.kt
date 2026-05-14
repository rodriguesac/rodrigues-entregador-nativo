package com.rodriguesacai.entregador.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private enum class AppTab { Inicio, Ganhos, Historico, Conta, Mais }
private enum class OperationStage { Aguardando, Oferta, Coleta, Entrega }

@Composable
fun DriverHomeScreen(
    onGoOnline: () -> Unit,
    onGoOffline: () -> Unit,
    onOpenNavigator: () -> Unit,
    onOpenBatterySettings: () -> Unit,
    onSimulateRide: () -> Unit
) {
    var online by remember { mutableStateOf(false) }
    var tab by remember { mutableStateOf(AppTab.Inicio) }
    var stage by remember { mutableStateOf(OperationStage.Aguardando) }

    Scaffold(
        containerColor = Color(0xFF09060D),
        bottomBar = {
            NavigationBar(containerColor = Color(0xFF110D16), tonalElevation = 0.dp) {
                navItem(AppTab.Inicio, tab, "Início", "⌂") { tab = it }
                navItem(AppTab.Ganhos, tab, "Ganhos", "$") { tab = it }
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
                .background(Brush.verticalGradient(listOf(Color(0xFF15051F), Color(0xFF07040A))))
        ) {
            when (tab) {
                AppTab.Inicio -> HomeContent(
                    online = online,
                    stage = stage,
                    onToggle = {
                        online = !online
                        if (online) onGoOnline() else {
                            stage = OperationStage.Aguardando
                            onGoOffline()
                        }
                    },
                    onOpenNavigator = onOpenNavigator,
                    onSimulateRide = onSimulateRide,
                    onStageChange = { stage = it }
                )
                AppTab.Ganhos -> EarningsContent()
                AppTab.Historico -> HistoryContent(onSimulateRide)
                AppTab.Conta -> AccountContent()
                AppTab.Mais -> MoreContent(onOpenBatterySettings)
            }
        }
    }
}

@Composable
private fun RowScope.navItem(item: AppTab, selected: AppTab, label: String, icon: String, onClick: (AppTab) -> Unit) {
    NavigationBarItem(
        selected = selected == item,
        onClick = { onClick(item) },
        icon = { Text(icon, fontSize = 20.sp, fontWeight = FontWeight.Bold) },
        label = { Text(label, fontSize = 11.sp) },
        colors = NavigationBarItemDefaults.colors(
            selectedIconColor = Color.White,
            selectedTextColor = Color.White,
            indicatorColor = Color(0xFFE90045),
            unselectedIconColor = Color(0xFF95899E),
            unselectedTextColor = Color(0xFF95899E)
        )
    )
}

@Composable
private fun HomeContent(
    online: Boolean,
    stage: OperationStage,
    onToggle: () -> Unit,
    onOpenNavigator: () -> Unit,
    onSimulateRide: () -> Unit,
    onStageChange: (OperationStage) -> Unit
) {
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        HeaderBar(online)
        OperationHero(online, stage, onToggle, onStageChange)
        when (stage) {
            OperationStage.Aguardando -> WaitingRoutePreview(onSimulateRide)
            OperationStage.Oferta -> IncomingRideCard(onSimulateRide, onStageChange)
            OperationStage.Coleta -> PickupCard(onOpenNavigator, onStageChange)
            OperationStage.Entrega -> DeliveryCard(onOpenNavigator, onStageChange)
        }
        EarningsStrip()
        QuickActionsCard(onSimulateRide, onOpenNavigator)
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun HeaderBar(online: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Brush.radialGradient(listOf(Color(0xFFFF1B5E), Color(0xFF4C0D73))))
                .border(1.dp, Color.White.copy(alpha = 0.18f), CircleShape),
            contentAlignment = Alignment.Center
        ) { Text("R", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Black) }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text("Rodrigues Entregador", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Black)
            Text(
                if (online) "Online • alerta urgente ativo" else "Offline • toque para operar",
                color = Color(0xFFC7BED2),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        StatusPill(online)
    }
}

@Composable
private fun StatusPill(online: Boolean) {
    val bg = if (online) Color(0xFF053B25) else Color(0xFF351318)
    val fg = if (online) Color(0xFF42F39B) else Color(0xFFFF8B9D)
    Surface(color = bg, shape = RoundedCornerShape(999.dp)) {
        Text(if (online) "ONLINE" else "OFF", color = fg, fontWeight = FontWeight.Black, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp))
    }
}

@Composable
private fun OperationHero(online: Boolean, stage: OperationStage, onToggle: () -> Unit, onStageChange: (OperationStage) -> Unit) {
    PremiumCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(stageTitle(online, stage), color = Color.White, fontSize = 25.sp, fontWeight = FontWeight.Black)
                Spacer(Modifier.height(6.dp))
                Text(stageSubtitle(online, stage), color = Color(0xFFD6CCDF), lineHeight = 20.sp)
            }
            Switch(checked = online, onCheckedChange = { onToggle() })
        }
        Spacer(Modifier.height(16.dp))
        StageStepper(stage = stage, enabled = online, onStageChange = onStageChange)
    }
}

private fun stageTitle(online: Boolean, stage: OperationStage): String = if (!online) "Você está fora da operação" else when (stage) {
    OperationStage.Aguardando -> "Disponível para entregas"
    OperationStage.Oferta -> "Oferta de corrida aberta"
    OperationStage.Coleta -> "Indo para coleta"
    OperationStage.Entrega -> "Entrega em andamento"
}

private fun stageSubtitle(online: Boolean, stage: OperationStage): String = if (!online) "Ative para iniciar serviço em segundo plano, GPS e alertas de nova corrida." else when (stage) {
    OperationStage.Aguardando -> "Aguardando pedidos próximos. Tela limpa e foco total no próximo chamado."
    OperationStage.Oferta -> "Valor, distância e tempo em destaque para decidir rápido."
    OperationStage.Coleta -> "Mostre somente o essencial: loja, chegada e navegação."
    OperationStage.Entrega -> "Cliente e ação principal ficam sempre a um toque."
}

@Composable
private fun StageStepper(stage: OperationStage, enabled: Boolean, onStageChange: (OperationStage) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        StageChip("Livre", stage == OperationStage.Aguardando, enabled, Modifier.weight(1f)) { onStageChange(OperationStage.Aguardando) }
        StageChip("Oferta", stage == OperationStage.Oferta, enabled, Modifier.weight(1f)) { onStageChange(OperationStage.Oferta) }
        StageChip("Coleta", stage == OperationStage.Coleta, enabled, Modifier.weight(1f)) { onStageChange(OperationStage.Coleta) }
        StageChip("Entrega", stage == OperationStage.Entrega, enabled, Modifier.weight(1f)) { onStageChange(OperationStage.Entrega) }
    }
}

@Composable
private fun StageChip(label: String, selected: Boolean, enabled: Boolean, modifier: Modifier, onClick: () -> Unit) {
    val bg = if (selected) Color(0xFFE90045) else Color.White.copy(alpha = 0.07f)
    val fg = if (enabled) Color.White else Color(0xFF756A7F)
    Surface(onClick = onClick, enabled = enabled, modifier = modifier.height(38.dp), color = bg, shape = RoundedCornerShape(999.dp)) {
        Box(contentAlignment = Alignment.Center) { Text(label, color = fg, fontSize = 12.sp, fontWeight = FontWeight.Bold) }
    }
}

@Composable
private fun WaitingRoutePreview(onSimulateRide: () -> Unit) {
    PremiumCard {
        Text("Mapa da operação", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Black)
        Spacer(Modifier.height(10.dp))
        MapMock(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp),
            title = "Aguardando rota",
            subtitle = "Preview estático sem cobrir os cards"
        )
        Spacer(Modifier.height(12.dp))
        Button(onClick = onSimulateRide, modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(18.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE90045))) {
            Text("Simular nova corrida", fontWeight = FontWeight.Black)
        }
    }
}

@Composable
private fun IncomingRideCard(onSimulateRide: () -> Unit, onStageChange: (OperationStage) -> Unit) {
    PremiumCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Aceitar a rota?", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Black)
                Text("R$ 7,00", color = Color.White, fontSize = 44.sp, fontWeight = FontWeight.Black)
                Text("3,52 km • 22 min • 2 paradas", color = Color(0xFFD6CCDF), fontSize = 16.sp)
            }
            CountdownBadge("60")
        }
        Spacer(Modifier.height(14.dp))
        MiniRouteBox("Coleta 1", "Panificadora Dico Hiroshima & Padaria")
        MiniRouteBox("Entrega 1", "Carandá Bosque")
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(onClick = { onStageChange(OperationStage.Aguardando) }, modifier = Modifier.weight(1f).height(58.dp), shape = RoundedCornerShape(18.dp)) { Text("Rejeitar") }
            Button(onClick = { onStageChange(OperationStage.Coleta) }, modifier = Modifier.weight(1.6f).height(58.dp), shape = RoundedCornerShape(18.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF078244))) { Text("Aceitar", fontWeight = FontWeight.Black) }
        }
        TextButton(onClick = onSimulateRide, modifier = Modifier.align(Alignment.CenterHorizontally)) { Text("Abrir tela urgente real") }
    }
}

@Composable
private fun PickupCard(onOpenNavigator: () -> Unit, onStageChange: (OperationStage) -> Unit) {
    PremiumCard {
        MapMock(modifier = Modifier.fillMaxWidth().height(260.dp), title = "Coleta 1", subtitle = "Loja → retirada")
        Spacer(Modifier.height(14.dp))
        StopCard(kind = "COLETA", name = "Panificadora Dico Hiroshima & Padaria", address = "Avenida Hiroshima, 812 • Vila Nascente", primary = "Cheguei na coleta", onPrimary = { onStageChange(OperationStage.Entrega) }, onOpenNavigator = onOpenNavigator)
    }
}

@Composable
private fun DeliveryCard(onOpenNavigator: () -> Unit, onStageChange: (OperationStage) -> Unit) {
    PremiumCard {
        MapMock(modifier = Modifier.fillMaxWidth().height(260.dp), title = "Entrega 1", subtitle = "Rota em andamento")
        Spacer(Modifier.height(14.dp))
        StopCard(kind = "ENTREGA", name = "Cliente • endereço liberado", address = "Carandá Bosque • 8 min restantes", primary = "Finalizar entrega", onPrimary = { onStageChange(OperationStage.Aguardando) }, onOpenNavigator = onOpenNavigator)
        Spacer(Modifier.height(8.dp))
        OutlinedButton(onClick = {}, modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(16.dp)) { Text("Rota de devolução") }
    }
}

@Composable
private fun StopCard(kind: String, name: String, address: String, primary: String, onPrimary: () -> Unit, onOpenNavigator: () -> Unit) {
    Surface(color = Color.White.copy(alpha = 0.06f), shape = RoundedCornerShape(24.dp), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Column(Modifier.weight(1f)) {
                    Text(kind, color = Color(0xFFFFD24A), fontSize = 12.sp, fontWeight = FontWeight.Black)
                    Text(name, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Black)
                    Text(address, color = Color(0xFFC7BED2), fontSize = 15.sp)
                }
                OutlinedButton(onClick = onOpenNavigator, shape = RoundedCornerShape(16.dp)) { Text("Mapa") }
            }
            Button(onClick = onPrimary, modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(18.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE90045))) { Text(primary, fontWeight = FontWeight.Black) }
        }
    }
}

@Composable
private fun MapMock(modifier: Modifier, title: String, subtitle: String) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(28.dp))
            .background(Brush.linearGradient(listOf(Color(0xFF272238), Color(0xFF152229), Color(0xFF3A114D))))
            .border(1.dp, Color.White.copy(alpha = 0.10f), RoundedCornerShape(28.dp))
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val lineColor = Color.White.copy(alpha = 0.13f)
            val pathEffect = PathEffect.dashPathEffect(floatArrayOf(16f, 18f), 0f)
            for (i in 0..7) {
                val y = size.height * i / 7f
                drawLine(lineColor, Offset(0f, y), Offset(size.width, y + 60f), strokeWidth = 2f)
            }
            for (i in 0..5) {
                val x = size.width * i / 5f
                drawLine(lineColor, Offset(x, 0f), Offset(x - 80f, size.height), strokeWidth = 2f)
            }
            drawLine(Color(0xFFE90045), Offset(size.width * .20f, size.height * .68f), Offset(size.width * .78f, size.height * .32f), strokeWidth = 8f, pathEffect = pathEffect)
        }
        Surface(color = Color.White.copy(alpha = 0.92f), shape = RoundedCornerShape(18.dp), modifier = Modifier.align(Alignment.Center).padding(16.dp)) {
            Column(Modifier.padding(horizontal = 18.dp, vertical = 14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(title, color = Color(0xFF201626), fontSize = 19.sp, fontWeight = FontWeight.Black)
                Text(subtitle, color = Color(0xFF625669), fontSize = 13.sp)
            }
        }
        Surface(color = Color(0xFFE90045), shape = CircleShape, modifier = Modifier.align(Alignment.BottomEnd).padding(18.dp).size(54.dp)) {
            Box(contentAlignment = Alignment.Center) { Text("⌖", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Black) }
        }
    }
}

@Composable
private fun CountdownBadge(text: String) {
    Box(modifier = Modifier.size(70.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.08f)).border(5.dp, Color.White, CircleShape), contentAlignment = Alignment.Center) {
        Text(text, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Black)
    }
}

@Composable
private fun MiniRouteBox(label: String, value: String) {
    Surface(color = Color.White.copy(alpha = 0.06f), shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(10.dp).clip(CircleShape).background(if (label.startsWith("Coleta")) Color(0xFF39DD8E) else Color(0xFFFFD24A)))
            Spacer(Modifier.width(10.dp))
            Column {
                Text(label, color = Color(0xFF95899E), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text(value, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun EarningsStrip() {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
        MetricBox("Hoje", "R$ 0,00", Modifier.weight(1f))
        MetricBox("Corridas", "0", Modifier.weight(1f))
        MetricBox("Score", "100%", Modifier.weight(1f))
    }
}

@Composable
private fun MetricBox(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier.clip(RoundedCornerShape(20.dp)).background(Color.White.copy(alpha = 0.07f)).padding(12.dp)) {
        Text(label, color = Color(0xFF9D92A8), fontSize = 12.sp)
        Text(value, color = Color.White, fontWeight = FontWeight.Black, fontSize = 17.sp)
    }
}

@Composable
private fun QuickActionsCard(onSimulateRide: () -> Unit, onOpenNavigator: () -> Unit) {
    PremiumCard {
        Text("Ações rápidas", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Black)
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(onClick = onSimulateRide, modifier = Modifier.weight(1f).height(56.dp), shape = RoundedCornerShape(18.dp)) { Text("Testar alerta") }
            OutlinedButton(onClick = onOpenNavigator, modifier = Modifier.weight(1f).height(56.dp), shape = RoundedCornerShape(18.dp)) { Text("GPS") }
        }
    }
}

@Composable
private fun EarningsContent() {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("Ganhos", color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Black)
        PremiumCard {
            Text("Resumo do dia", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Black)
            Spacer(Modifier.height(18.dp))
            Text("R$ 0,00", color = Color.White, fontSize = 48.sp, fontWeight = FontWeight.Black)
            Text("Nenhuma corrida finalizada hoje.", color = Color(0xFFB9AFC6))
        }
        PremiumCard {
            Text("Próximo repasse", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Black)
            Text("Pix pendente de configuração no painel gestor.", color = Color(0xFFB9AFC6))
        }
    }
}

@Composable
private fun HistoryContent(onSimulateRide: () -> Unit) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("Histórico", color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Black)
        RideHistoryItem("Aguardando novas corridas", "Ofertas aceitas, recusadas e expiradas aparecem aqui.", "AGORA")
        RideHistoryItem("Rejeitadas", "Registre motivo e mantenha histórico igual app profissional.", "PENDENTE")
        Button(onClick = onSimulateRide, modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(18.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE90045))) { Text("Simular corrida") }
    }
}

@Composable
private fun RideHistoryItem(title: String, subtitle: String, tag: String) {
    PremiumCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(title, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Black)
                Text(subtitle, color = Color(0xFFB9AFC6), lineHeight = 20.sp)
            }
            Surface(color = Color.White.copy(alpha = 0.08f), shape = RoundedCornerShape(999.dp)) {
                Text(tag, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp))
            }
        }
    }
}

@Composable
private fun AccountContent() {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("Conta", color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Black)
        PremiumCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(62.dp).clip(CircleShape).background(Color(0xFF2C1838)), contentAlignment = Alignment.Center) { Text("R", color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Black) }
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text("Entregador Rodrigues", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Black)
                    Text("Perfil profissional verificado", color = Color(0xFFB9AFC6))
                }
                Text("✓", color = Color(0xFF42F39B), fontSize = 24.sp, fontWeight = FontWeight.Black)
            }
        }
        SettingsRow("Dados pessoais", "Telefone e e-mail com solicitação de alteração")
        SettingsRow("Recebimento", "Pix, banco e repasse com aprovação do gestor")
    }
}

@Composable
private fun MoreContent(onOpenBatterySettings: () -> Unit) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("Mais", color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Black)
        SettingsRow("Navegação padrão", "Google Maps, Waze ou padrão do celular")
        SettingsRow("Notificações urgentes", "Canal full screen + alerta sonoro")
        SettingsRow("Permissões", "Localização só quando necessário")
        Button(onClick = onOpenBatterySettings, modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(18.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE90045))) { Text("Abrir ajustes de bateria") }
        Text("v1.2.0 nativo • Rodrigues Açaí e Cia", color = Color(0xFF8F839C), modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
    }
}

@Composable
private fun SettingsRow(title: String, subtitle: String) {
    PremiumCard {
        Text(title, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Black)
        Text(subtitle, color = Color(0xFFB9AFC6), lineHeight = 20.sp)
    }
}

@Composable
private fun PremiumCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(30.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF17101F)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) { Column(Modifier.padding(18.dp), content = content) }
}
