package com.rodriguesacai.entregador.data

import android.content.Context
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import java.text.SimpleDateFormat
import java.util.Locale

object RealtimeExtrasRepository {
    private val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    private val bannerCollections = listOf("carrosselApp", "bannersAppEntregador", "appBanners", "banners")
    private val noticeCollections = listOf("notificacoesEntregador", "avisosEntregador", "notificacoes", "avisos")

    fun listenAppBanners(
        onBanners: (List<AppBanner>) -> Unit,
        onError: (String) -> Unit
    ): ListenerRegistration {
        val state = linkedMapOf<String, List<AppBanner>>()
        fun emit() {
            val merged = state.values.flatten()
                .distinctBy { it.id }
                .sortedBy { it.order }
            onBanners(merged)
        }
        val listeners = bannerCollections.map { collection ->
            db.collection(collection).limit(30).addSnapshotListener { snap, err ->
                if (err != null) {
                    onError(err.message ?: "Erro ao ler carrossel do app.")
                    return@addSnapshotListener
                }
                state[collection] = snap?.documents.orEmpty()
                    .mapNotNull { it.toAppBanner(collection) }
                    .filter { it.active }
                emit()
            }
        }
        return CompositeRegistration(listeners)
    }

    fun listenNotifications(
        context: Context,
        onNotifications: (List<DriverNotice>) -> Unit,
        onError: (String) -> Unit
    ): ListenerRegistration? {
        val profile = DriverRepository.currentSession(context) ?: return null
        val state = linkedMapOf<String, List<DriverNotice>>()
        fun emit() {
            val merged = state.values.flatten()
                .distinctBy { it.id }
                .sortedByDescending { it.createdAtMillis }
            onNotifications(merged)
        }
        val listeners = noticeCollections.map { collection ->
            db.collection(collection).limit(80).addSnapshotListener { snap, err ->
                if (err != null) {
                    onError(err.message ?: "Erro ao ler notificações reais.")
                    return@addSnapshotListener
                }
                state[collection] = snap?.documents.orEmpty()
                    .mapNotNull { it.toDriverNotice(collection, profile.id) }
                emit()
            }
        }
        return CompositeRegistration(listeners)
    }
}

private class CompositeRegistration(private val registrations: List<ListenerRegistration>) : ListenerRegistration {
    override fun remove() {
        registrations.forEach { it.remove() }
    }
}

data class AppBanner(
    val id: String,
    val title: String,
    val subtitle: String,
    val label: String,
    val imageUrl: String,
    val actionText: String,
    val actionUrl: String,
    val order: Int,
    val active: Boolean
)

data class DriverNotice(
    val id: String,
    val title: String,
    val body: String,
    val type: String,
    val createdAtMillis: Long,
    val createdLabel: String,
    val read: Boolean
)

private fun DocumentSnapshot.toAppBanner(collection: String): AppBanner? {
    val title = anyString("titulo", "title", "headline", "nome")
    val subtitle = anyString("subtitulo", "descricao", "description", "texto", "body")
    val image = anyString("imagem", "image", "imageUrl", "urlImagem", "bannerUrl", "foto")
    val hasContent = title.isNotBlank() || subtitle.isNotBlank() || image.isNotBlank()
    if (!hasContent) return null

    val rawStatus = anyString("status", "situacao", "state").uppercase(Locale.ROOT)
    val explicitActive = anyBoolean("ativo", "active", "habilitado", "enabled", "publicado")
    val active = explicitActive ?: rawStatus.isBlank() || rawStatus in setOf("ATIVO", "ACTIVE", "PUBLICADO", "ONLINE")

    return AppBanner(
        id = "$collection:$id",
        title = title,
        subtitle = subtitle,
        label = anyString("label", "tag", "categoria").ifBlank { "AVISO" },
        imageUrl = image,
        actionText = anyString("botao", "acaoTexto", "buttonText", "cta").ifBlank { "Abrir" },
        actionUrl = anyString("link", "url", "actionUrl", "destino"),
        order = anyNumber("ordem", "order", "posicao", "index")?.toInt() ?: 999,
        active = active
    )
}

private fun DocumentSnapshot.toDriverNotice(collection: String, driverId: String): DriverNotice? {
    val target = anyString("entregadorId", "driverId", "motoboyId", "uidEntregador", "targetDriverId")
    val global = anyBoolean("global", "paraTodos", "broadcast", "todosEntregadores") ?: false
    val channel = anyString("canal", "audiencia", "tipoApp", "target", "app").uppercase(Locale.ROOT)
    val isForDriverApp = channel.isBlank() || channel.contains("ENTREGADOR") || channel.contains("DRIVER") || channel.contains("MOTOBOY")
    if (target.isNotBlank() && target != driverId) return null
    if (target.isBlank() && !global && !isForDriverApp) return null

    val title = anyString("titulo", "title", "assunto", "nome")
    val body = anyString("mensagem", "body", "texto", "descricao", "description")
    if (title.isBlank() && body.isBlank()) return null

    val date = anyTimestamp("criadoEm", "createdAt", "data", "enviadoEm", "updatedAt")?.toDate()
    return DriverNotice(
        id = "$collection:$id",
        title = title.ifBlank { "Notificação" },
        body = body,
        type = anyString("tipo", "categoria", "type").ifBlank { "Operação" },
        createdAtMillis = date?.time ?: 0L,
        createdLabel = date?.formatShortLabel().orEmpty().ifBlank { "Agora" },
        read = anyBoolean("lida", "read", "visualizada") ?: false
    )
}

private fun DocumentSnapshot.anyString(vararg keys: String): String {
    for (key in keys) {
        val value = getDeep(key)
        if (value != null) return value.toString().trim()
    }
    return ""
}

private fun DocumentSnapshot.anyNumber(vararg keys: String): Double? {
    for (key in keys) {
        val value = getDeep(key)
        when (value) {
            is Number -> return value.toDouble()
            is String -> value.replace("R$", "").replace(".", "").replace(",", ".").trim().toDoubleOrNull()?.let { return it }
        }
    }
    return null
}

private fun DocumentSnapshot.anyBoolean(vararg keys: String): Boolean? {
    for (key in keys) {
        val value = getDeep(key)
        when (value) {
            is Boolean -> return value
            is String -> {
                val normalized = value.trim().uppercase(Locale.ROOT)
                if (normalized in setOf("TRUE", "SIM", "YES", "1", "ATIVO", "ACTIVE", "PUBLICADO")) return true
                if (normalized in setOf("FALSE", "NAO", "NÃO", "NO", "0", "INATIVO", "INACTIVE", "RASCUNHO")) return false
            }
        }
    }
    return null
}

private fun DocumentSnapshot.anyTimestamp(vararg keys: String): Timestamp? {
    for (key in keys) {
        getTimestamp(key)?.let { return it }
    }
    return null
}

private fun DocumentSnapshot.getDeep(path: String): Any? {
    if (!path.contains('.')) return get(path)
    var current: Any? = data
    for (part in path.split('.')) {
        current = when (current) {
            is Map<*, *> -> current[part]
            else -> return null
        }
    }
    return current
}

private fun java.util.Date.formatShortLabel(): String {
    return SimpleDateFormat("dd/MM • HH:mm", Locale("pt", "BR")).format(this)
}
