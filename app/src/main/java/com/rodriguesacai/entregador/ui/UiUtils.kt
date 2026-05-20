package com.rodriguesacai.entregador.ui

import androidx.compose.ui.graphics.Color
import com.rodriguesacai.entregador.data.Ride
import com.rodriguesacai.entregador.ui.theme.AppColors
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

fun money(value: Double): String = NumberFormat.getCurrencyInstance(Locale("pt", "BR")).format(value)
fun safeMoney(value: Double): String = if (value <= 0.0) "A calcular" else money(value)
fun Double.format1(): String = String.format(Locale("pt", "BR"), "%.1f", this)
fun safeDistance(value: Double): String = if (value <= 0.0) "rota pendente" else "${value.format1()} km"
fun safeEta(value: Int): String = if (value <= 0) "tempo pendente" else "${value} min"

fun Ride.deliveryAddressVisible(): Boolean = status in listOf(
    "PEDIDO_RETIRADO",
    "INDO_ENTREGA",
    "ENTREGADOR_NO_LOCAL",
    "OCORRENCIA",
    "FINALIZADA"
)

fun Ride.pickupVisibleAddress(): String = lojaEndereco.ifBlank { lojaNome.ifBlank { "Endereço da loja pendente" } }
fun Ride.safeDeliveryAddress(): String = if (deliveryAddressVisible()) {
    clienteEnderecoCompleto.ifBlank { "Endereço do cliente pendente" }
} else {
    "Endereço completo liberado após retirar o pedido"
}

fun humanStatus(status: String): String = when (status) {
    "OFERTA_RECEBIDA" -> "Oferta recebida"
    "DISPONIVEL" -> "Disponível"
    "ACEITA" -> "Aceita"
    "INDO_COLETA" -> "Indo para coleta"
    "CHEGUEI_COLETA" -> "Na coleta"
    "PEDIDO_RETIRADO" -> "Pedido retirado"
    "INDO_ENTREGA" -> "Em rota"
    "ENTREGADOR_NO_LOCAL" -> "No local"
    "FINALIZADA" -> "Finalizada"
    "RECUSADA" -> "Recusada"
    "EXPIRADA" -> "Expirada"
    "OCORRENCIA" -> "Ocorrência"
    "RESTRICAO" -> "Restrição"
    "INDISPONIVEL" -> "Indisponível"
    else -> status.lowercase().replace("_", " ").replaceFirstChar { it.titlecase() }
}

fun nextActionText(status: String): String = when (status) {
    "ACEITA" -> "Iniciar ida à coleta"
    "INDO_COLETA" -> "Cheguei na coleta"
    "CHEGUEI_COLETA" -> "Pedido retirado"
    "PEDIDO_RETIRADO" -> "Ir para entrega"
    "INDO_ENTREGA" -> "Cheguei no cliente"
    "ENTREGADOR_NO_LOCAL" -> "Finalizar entrega"
    else -> "Atualizar corrida"
}

fun statusColor(status: String): Color = when (status) {
    "DISPONIVEL", "FINALIZADA" -> AppColors.Green
    "RECUSADA", "EXPIRADA", "RESTRICAO" -> AppColors.Red
    "OCORRENCIA" -> AppColors.Yellow
    "OFERTA_RECEBIDA" -> AppColors.Purple
    "INDISPONIVEL" -> AppColors.Ink
    else -> AppColors.DarkGreen
}

fun shortDate(timestamp: com.google.firebase.Timestamp?): String {
    val date = timestamp?.toDate() ?: return "Agora"
    return SimpleDateFormat("dd/MM • HH:mm", Locale("pt", "BR")).format(date)
}
