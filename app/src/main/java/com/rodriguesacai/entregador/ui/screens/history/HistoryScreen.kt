package com.rodriguesacai.entregador.ui.screens.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.rodriguesacai.entregador.data.Ride
import com.rodriguesacai.entregador.ui.components.AppBottomBar
import com.rodriguesacai.entregador.ui.components.BasePage
import com.rodriguesacai.entregador.ui.components.CardLine
import com.rodriguesacai.entregador.ui.components.EmptyCard
import com.rodriguesacai.entregador.ui.humanStatus
import com.rodriguesacai.entregador.ui.safeMoney
import com.rodriguesacai.entregador.ui.navigation.AppRoute
import com.rodriguesacai.entregador.ui.shortDate
import com.rodriguesacai.entregador.ui.statusColor

@Composable
fun HistoryScreen(history: List<Ride>, onBack: () -> Unit, onNav: (AppRoute) -> Unit) {
    BasePage("Histórico", "Uma corrida por card, sem repetir cada status", onBack, bottomBar = { AppBottomBar(AppRoute.Historico, onNav) }) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            if (history.isEmpty()) EmptyCard("Nenhuma corrida no histórico.")
            history.take(40).forEach { ride ->
                CardLine(
                    title = "Pedido ${ride.numeroPedido}",
                    subtitle = "${shortDate(ride.atualizadaEm ?: ride.criadaEm)} • ${humanStatus(ride.status)}",
                    trailing = safeMoney(ride.valorCorrida),
                    color = statusColor(ride.status)
                )
            }
        }
    }
}
