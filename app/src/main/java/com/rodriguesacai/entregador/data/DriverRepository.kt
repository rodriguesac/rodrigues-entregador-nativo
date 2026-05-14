package com.rodriguesacai.entregador.data

import android.content.Context
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import com.google.firebase.messaging.FirebaseMessaging
import java.security.MessageDigest
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DriverRepository {
    private const val PREFS = "driver_session"
    private const val KEY_ID = "driver_id"
    private const val KEY_NAME = "driver_name"
    private const val KEY_PHONE = "driver_phone"
    private const val KEY_PHOTO = "driver_photo"
    private const val KEY_COLLECTION = "driver_collection"

    private const val REAL_DRIVER_COLLECTION = "entregadores"
    private val DRIVER_COLLECTIONS = listOf("entregadores", "drivers", "motoboys", "deliveryDrivers", "couriers")
    private val MISSION_COLLECTIONS = listOf("rotas_entrega", "pedidos", "rides")

    private val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    fun currentSession(context: Context): DriverProfile? {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val id = prefs.getString(KEY_ID, null)?.takeIf { it.isNotBlank() } ?: return null
        return DriverProfile(
            id = id,
            name = prefs.getString(KEY_NAME, null).orEmpty().ifBlank { "Entregador" },
            phone = prefs.getString(KEY_PHONE, null).orEmpty(),
            photoUrl = prefs.getString(KEY_PHOTO, null).orEmpty(),
            collectionName = prefs.getString(KEY_COLLECTION, null).orEmpty().ifBlank { REAL_DRIVER_COLLECTION },
            verified = true,
            approved = true,
            blocked = false
        )
    }

    fun logout(context: Context, onDone: () -> Unit = {}) {
        val session = currentSession(context)
        if (session != null) {
            db.collection(session.collectionName).document(session.id).set(
                mapOf(
                    "online" to false,
                    "status" to "Offline",
                    "statusOnline" to "Offline",
                    "atualizadoEm" to Timestamp.now(),
                    "updatedAt" to Timestamp.now()
                ),
                SetOptions.merge()
            )
        }
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().clear().apply()
        onDone()
    }

    fun login(
        context: Context,
        documentOrPhone: String,
        password: String = "",
        onSuccess: (DriverProfile) -> Unit,
        onError: (String) -> Unit
    ) {
        val raw = documentOrPhone.trim()
        val normalized = raw.onlyDigits()
        if (raw.length < 4 && normalized.length < 4) {
            onError("Informe o CPF ou telefone cadastrado do entregador.")
            return
        }

        val searchValues = buildSearchValues(raw, normalized)
        findDriverProfile(searchValues, onFound = { profile ->
            when {
                profile.blocked -> onError("Entregador bloqueado/reprovado no painel gestor.")
                !profile.approved -> onError("Cadastro encontrado, mas ainda aguardando aprovacao do gestor.")
                profile.hasPassword && !profile.passwordMatches(password) -> onError("Senha incorreta. Confira a senha cadastrada para este entregador.")
                else -> saveSession(context, profile, onSuccess)
            }
        }, onNotFound = {
            onError("Entregador nao encontrado. Cadastre-se ou aprove o cadastro no painel gestor.")
        })
    }

    fun registerDriver(
        request: DriverRegistrationRequest,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val cpfDigits = request.cpf.onlyDigits()
        val phoneDigits = request.phone.onlyDigits()
        when {
            request.name.trim().length < 3 -> { onError("Informe o nome completo do entregador."); return }
            cpfDigits.length != 11 -> { onError("CPF precisa ter 11 numeros."); return }
            phoneDigits.length < 10 -> { onError("Informe um telefone/WhatsApp valido."); return }
            request.password.length < 6 -> { onError("Crie uma senha com pelo menos 6 caracteres."); return }
        }

        val now = Timestamp.now()
        val id = cpfDigits
        val payload = linkedMapOf<String, Any?>(
            "id" to id,
            "uid" to id,
            "nome" to request.name.trim(),
            "nomeCompleto" to request.name.trim(),
            "cpf" to maskCpf(cpfDigits),
            "cpfLimpo" to cpfDigits,
            "telefone" to request.phone.trim(),
            "telefoneLimpo" to phoneDigits,
            "whatsapp" to request.phone.trim(),
            "modalidade" to request.vehicle.ifBlank { "Moto" },
            "placa" to request.plate.trim().uppercase(Locale.ROOT),
            "statusCadastro" to "PENDENTE",
            "statusAprovacao" to "PENDENTE",
            "aprovado" to false,
            "online" to false,
            "status" to "Aguardando aprovacao",
            "senhaHash" to sha256(request.password),
            "senhaCriadaEm" to now,
            "origemCadastro" to "android_native",
            "platform" to "android_native",
            "appVersion" to "3.2.0-nativo-login-cadastro",
            "criadoEm" to now,
            "createdAt" to now,
            "atualizadoEm" to now,
            "updatedAt" to now
        )

        db.collection(REAL_DRIVER_COLLECTION).document(id).set(payload, SetOptions.merge())
            .addOnSuccessListener {
                db.collection("solicitacoesEntregadores").document(id).set(
                    payload + mapOf("tipo" to "CADASTRO_ENTREGADOR", "prioridade" to "NORMAL"),
                    SetOptions.merge()
                ).addOnSuccessListener { onSuccess() }
                    .addOnFailureListener { onError(it.message ?: "Cadastro salvo, mas falhou ao criar solicitacao.") }
            }
            .addOnFailureListener { onError(it.message ?: "Falha ao enviar cadastro.") }
    }

    private fun buildSearchValues(raw: String, normalized: String): List<String> {
        val values = linkedSetOf<String>()
        fun add(value: String?) {
            val v = value?.trim().orEmpty()
            if (v.isNotBlank()) values.add(v)
        }
        add(raw)
        add(normalized)
        if (normalized.length == 11) {
            add(maskCpf(normalized))
        }
        if (normalized.length == 10) {
            add("(${normalized.substring(0, 2)}) ${normalized.substring(2, 6)}-${normalized.substring(6)}")
        }
        if (normalized.length == 11) {
            add("(${normalized.substring(0, 2)}) ${normalized.substring(2, 7)}-${normalized.substring(7)}")
        }
        return values.toList()
    }

    private fun findDriverProfile(
        searchValues: List<String>,
        onFound: (DriverProfile) -> Unit,
        onNotFound: () -> Unit
    ) {
        fun tryCollection(index: Int) {
            if (index >= DRIVER_COLLECTIONS.size) {
                onNotFound()
                return
            }

            val collectionName = DRIVER_COLLECTIONS[index]

            fun scanCollection() {
                db.collection(collectionName).get()
                    .addOnSuccessListener { snap ->
                        val doc = snap.documents.firstOrNull { it.matchesLogin(searchValues) }
                        if (doc != null) {
                            onFound(doc.toProfile(collectionName))
                        } else {
                            tryCollection(index + 1)
                        }
                    }
                    .addOnFailureListener {
                        tryCollection(index + 1)
                    }
            }

            fun tryDirect(valueIndex: Int) {
                if (valueIndex >= searchValues.size) {
                    scanCollection()
                    return
                }
                val id = searchValues[valueIndex]
                if (id.isBlank()) {
                    tryDirect(valueIndex + 1)
                    return
                }
                db.collection(collectionName).document(id).get()
                    .addOnSuccessListener { doc ->
                        if (doc.exists()) {
                            onFound(doc.toProfile(collectionName))
                        } else {
                            tryDirect(valueIndex + 1)
                        }
                    }
                    .addOnFailureListener {
                        tryDirect(valueIndex + 1)
                    }
            }

            tryDirect(0)
        }

        tryCollection(0)
    }

    private fun saveSession(context: Context, profile: DriverProfile, onSuccess: (DriverProfile) -> Unit) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putString(KEY_ID, profile.id)
            .putString(KEY_NAME, profile.name)
            .putString(KEY_PHONE, profile.phone)
            .putString(KEY_PHOTO, profile.photoUrl)
            .putString(KEY_COLLECTION, profile.collectionName)
            .apply()

        db.collection(profile.collectionName).document(profile.id).set(
            mapOf(
                "ultimoLoginEm" to Timestamp.now(),
                "lastLoginAt" to Timestamp.now(),
                "platform" to "android_native",
                "appVersion" to "3.2.0-nativo-login-cadastro"
            ),
            SetOptions.merge()
        )
        saveMessagingToken(context)
        onSuccess(profile)
    }

    fun setOnline(context: Context, online: Boolean) {
        val profile = currentSession(context) ?: return
        val payload = linkedMapOf<String, Any?>(
            "id" to profile.id,
            "uid" to profile.id,
            "nome" to profile.name,
            "nomeCompleto" to profile.name,
            "telefone" to profile.phone,
            "online" to online,
            "status" to if (online) "Livre" else "Offline",
            "statusOnline" to if (online) "Livre" else "Offline",
            "atualizadoEm" to Timestamp.now(),
            "updatedAt" to Timestamp.now(),
            "platform" to "android_native",
            "appVersion" to "3.2.0-nativo-login-cadastro"
        )
        db.collection(profile.collectionName).document(profile.id).set(payload, SetOptions.merge())
        if (online) saveMessagingToken(context)
    }

    fun saveMessagingToken(context: Context) {
        val profile = currentSession(context) ?: return
        FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
            db.collection(profile.collectionName).document(profile.id).set(
                mapOf(
                    "fcmToken" to token,
                    "pushToken" to token,
                    "tokenPush" to token,
                    "tokenUpdatedAt" to Timestamp.now(),
                    "tokenAtualizadoEm" to Timestamp.now(),
                    "platform" to "android_native"
                ),
                SetOptions.merge()
            )
        }
    }

    fun listenPendingRide(
        context: Context,
        onRide: (DriverRide?) -> Unit,
        onError: (String) -> Unit
    ): ListenerRegistration? {
        val profile = currentSession(context) ?: return null
        val state = mutableMapOf<String, List<DriverRide>>()
        fun emit() {
            val ride = MISSION_COLLECTIONS.flatMap { state[it].orEmpty() }
                .firstOrNull { it.status == "pending" }
            onRide(ride)
        }
        val registrations = MISSION_COLLECTIONS.map { collectionName ->
            db.collection(collectionName)
                .limit(120)
                .addSnapshotListener { snap, err ->
                    if (err != null) {
                        onError(err.message ?: "Erro ao ouvir $collectionName.")
                        return@addSnapshotListener
                    }
                    val rides = snap?.documents.orEmpty()
                        .mapNotNull { doc -> doc.toDriverRide(collectionName) }
                        .filter { ride -> ride.status == "pending" && ride.canBeOfferedTo(profile.id) }
                    state[collectionName] = rides
                    emit()
                }
        }
        return CompositeListenerRegistration(registrations)
    }

    fun listenMyActiveRide(
        context: Context,
        onRide: (DriverRide?) -> Unit,
        onError: (String) -> Unit
    ): ListenerRegistration? {
        val profile = currentSession(context) ?: return null
        val state = mutableMapOf<String, List<DriverRide>>()
        fun emit() {
            val active = MISSION_COLLECTIONS.flatMap { state[it].orEmpty() }
                .firstOrNull { it.status in listOf("accepted", "pickup", "delivering") }
            onRide(active)
        }
        val registrations = MISSION_COLLECTIONS.map { collectionName ->
            db.collection(collectionName)
                .limit(120)
                .addSnapshotListener { snap, err ->
                    if (err != null) {
                        onError(err.message ?: "Erro ao ouvir $collectionName.")
                        return@addSnapshotListener
                    }
                    val rides = snap?.documents.orEmpty()
                        .mapNotNull { doc -> doc.toDriverRide(collectionName) }
                        .filter { ride -> ride.matchesDriver(profile.id) && ride.status in listOf("accepted", "pickup", "delivering") }
                    state[collectionName] = rides
                    emit()
                }
        }
        return CompositeListenerRegistration(registrations)
    }

    fun listenMyHistory(
        context: Context,
        onHistory: (List<DriverHistory>) -> Unit,
        onError: (String) -> Unit
    ): ListenerRegistration? {
        val profile = currentSession(context) ?: return null
        return db.collection("historicoEntregador")
            .limit(120)
            .addSnapshotListener { snap, err ->
                if (err != null) {
                    onError(err.message ?: "Erro ao ouvir historico.")
                    return@addSnapshotListener
                }
                val list = snap?.documents.orEmpty()
                    .filter { it.matchesDriverId(profile.id) }
                    .map { doc ->
                        val action = doc.anyString("tipo", "status", "action", "titulo").ifBlank { "registro" }
                        DriverHistory(
                            id = doc.id,
                            rideId = doc.anyString("rotaId", "rideId", "pedidoId", "missaoId"),
                            action = action,
                            value = formatCurrency(valueNumberFromDoc(doc)),
                            createdAtMillis = doc.anyTimestamp("criadoEm", "createdAt", "atualizadoEm")?.toDate()?.time ?: 0L,
                            createdLabel = doc.anyTimestamp("criadoEm", "createdAt", "atualizadoEm")?.toDate()?.formatHour().orEmpty().ifBlank { "agora" }
                        )
                    }
                    .sortedByDescending { it.createdAtMillis }
                onHistory(list)
            }
    }

    fun listenDailyStats(
        context: Context,
        onStats: (DriverStats) -> Unit,
        onError: (String) -> Unit
    ): ListenerRegistration? {
        val profile = currentSession(context) ?: return null
        return db.collection("historicoEntregador")
            .limit(200)
            .addSnapshotListener { snap, err ->
                if (err != null) {
                    onError(err.message ?: "Erro ao ouvir ganhos.")
                    return@addSnapshotListener
                }
                val finished = snap?.documents.orEmpty()
                    .filter { it.matchesDriverId(profile.id) }
                    .filter { doc -> doc.anyString("tipo", "status", "action").upperOrTrim() in FINAL_HISTORY_STATUSES }
                val total = finished.sumOf { doc -> valueNumberFromDoc(doc) }
                onStats(DriverStats(totalToday = total, finishedCount = finished.size, score = 100))
            }
    }

    fun acceptRide(context: Context, rideId: String, onDone: () -> Unit = {}, onError: (String) -> Unit = {}) {
        val profile = currentSession(context)
        if (profile == null) {
            onError("Faca login antes de aceitar corrida.")
            return
        }
        findMissionDocument(rideId, onFound = { doc ->
            val collectionName = doc.reference.parent.id
            val ride = doc.toDriverRide(collectionName)
            val newStatus = if (collectionName == "pedidos") "A_CAMINHO_LOJA" else if (collectionName == "rides") "accepted" else "ACEITA"
            val update = linkedMapOf<String, Any?>(
                "status" to newStatus,
                "statusEntregador" to newStatus,
                "statusMotoboy" to newStatus,
                "entregadorId" to profile.id,
                "entregadorUid" to profile.id,
                "uidEntregador" to profile.id,
                "driverId" to profile.id,
                "entregadorNome" to profile.name,
                "driverName" to profile.name,
                "aceitaEm" to Timestamp.now(),
                "acceptedAt" to Timestamp.now(),
                "atualizadoEm" to Timestamp.now(),
                "updatedAt" to Timestamp.now()
            )
            doc.reference.set(update, SetOptions.merge())
                .addOnSuccessListener {
                    db.collection(profile.collectionName).document(profile.id).set(
                        mapOf("status" to "Em rota", "online" to true, "ultimaAceitacaoEm" to Timestamp.now()),
                        SetOptions.merge()
                    )
                    addHistory(profile, rideId, "ACEITA", ride?.valueNumber ?: 0.0, collectionName)
                    onDone()
                }
                .addOnFailureListener { onError(it.message ?: "Falha ao aceitar corrida.") }
        }, onNotFound = {
            onError("Corrida nao encontrada no Firebase.")
        })
    }

    fun rejectRide(context: Context, rideId: String, onDone: () -> Unit = {}, onError: (String) -> Unit = {}) {
        val profile = currentSession(context)
        if (profile == null) {
            onError("Faca login antes de rejeitar corrida.")
            return
        }
        findMissionDocument(rideId, onFound = { doc ->
            val collectionName = doc.reference.parent.id
            val ride = doc.toDriverRide(collectionName)
            val update = linkedMapOf<String, Any?>(
                "rejeitadaPor" to profile.id,
                "ultimoRejeitadoPor" to profile.id,
                "lastRejectedBy" to profile.id,
                "rejeitados" to FieldValue.arrayUnion(profile.id),
                "rejectedDriverIds" to FieldValue.arrayUnion(profile.id),
                "rejeitadaEm" to Timestamp.now(),
                "ultimaAcaoEntregador" to "REJEITADA",
                "statusOfertaEntregador" to "REJEITADA",
                "atualizadoEm" to Timestamp.now(),
                "updatedAt" to Timestamp.now()
            )
            if (collectionName != "pedidos") {
                update["entregadorAtualOferta"] = null
                update["motoboyAtualOferta"] = null
                update["statusEntregador"] = "REJEITADA"
                update["statusMotoboy"] = "REJEITADA"
            } else {
                update["entregadorAtualOferta"] = null
                update["motoboyAtualOferta"] = null
            }
            doc.reference.set(update, SetOptions.merge())
                .addOnSuccessListener {
                    addHistory(profile, rideId, "REJEITADA", ride?.valueNumber ?: 0.0, collectionName)
                    onDone()
                }
                .addOnFailureListener { onError(it.message ?: "Falha ao rejeitar corrida.") }
        }, onNotFound = {
            onError("Corrida nao encontrada no Firebase.")
        })
    }

    fun expireRide(context: Context, rideId: String, onDone: () -> Unit = {}, onError: (String) -> Unit = {}) {
        val profile = currentSession(context)
        if (profile == null) {
            onError("Faca login antes de expirar corrida.")
            return
        }
        findMissionDocument(rideId, onFound = { doc ->
            val collectionName = doc.reference.parent.id
            doc.reference.set(
                mapOf(
                    "lastExpiredFor" to profile.id,
                    "expiredDriverIds" to FieldValue.arrayUnion(profile.id),
                    "statusEntregador" to "EXPIRADA",
                    "statusMotoboy" to "EXPIRADA",
                    "expiradaEm" to Timestamp.now(),
                    "atualizadoEm" to Timestamp.now(),
                    "updatedAt" to Timestamp.now()
                ),
                SetOptions.merge()
            ).addOnSuccessListener {
                addHistory(profile, rideId, "EXPIRADA", 0.0, collectionName)
                onDone()
            }.addOnFailureListener { onError(it.message ?: "Falha ao expirar oferta.") }
        }, onNotFound = {
            onError("Corrida nao encontrada no Firebase.")
        })
    }

    fun updateRideStatus(context: Context, rideId: String, status: String, onDone: () -> Unit = {}, onError: (String) -> Unit = {}) {
        val profile = currentSession(context)
        if (profile == null) {
            onError("Faca login para atualizar a corrida.")
            return
        }
        findMissionDocument(rideId, onFound = { doc ->
            val collectionName = doc.reference.parent.id
            val ride = doc.toDriverRide(collectionName)
            val realStatus = when (status) {
                "pickup" -> "COLETANDO"
                "delivering" -> "EM_ROTA"
                "finished" -> "CONCLUIDA"
                else -> status
            }
            val fields = linkedMapOf<String, Any?>(
                "status" to realStatus,
                "statusEntregador" to realStatus,
                "statusMotoboy" to realStatus,
                "atualizadoEm" to Timestamp.now(),
                "updatedAt" to Timestamp.now()
            )
            when (status) {
                "pickup" -> {
                    fields["chegouColetaEm"] = Timestamp.now()
                    fields["pickupStartedAt"] = Timestamp.now()
                }
                "delivering" -> {
                    fields["saiuEntregaEm"] = Timestamp.now()
                    fields["deliveryStartedAt"] = Timestamp.now()
                }
                "finished" -> {
                    fields["concluidaEm"] = Timestamp.now()
                    fields["finishedAt"] = Timestamp.now()
                    fields["entregueEm"] = Timestamp.now()
                }
            }
            doc.reference.set(fields, SetOptions.merge())
                .addOnSuccessListener {
                    addHistory(profile, rideId, realStatus, ride?.valueNumber ?: 0.0, collectionName)
                    if (status == "finished") {
                        db.collection(profile.collectionName).document(profile.id).set(
                            mapOf(
                                "status" to "Livre",
                                "online" to true,
                                "ultimaConclusaoEm" to Timestamp.now(),
                                "totalEntregas" to FieldValue.increment(1),
                                "saldoLiquido" to FieldValue.increment(ride?.valueNumber ?: 0.0)
                            ),
                            SetOptions.merge()
                        )
                    }
                    onDone()
                }
                .addOnFailureListener { onError(it.message ?: "Falha ao atualizar corrida.") }
        }, onNotFound = {
            onError("Corrida nao encontrada no Firebase.")
        })
    }

    private fun findMissionDocument(rideId: String, onFound: (DocumentSnapshot) -> Unit, onNotFound: () -> Unit) {
        fun tryCollection(index: Int) {
            if (index >= MISSION_COLLECTIONS.size) {
                onNotFound()
                return
            }
            val collectionName = MISSION_COLLECTIONS[index]
            db.collection(collectionName).document(rideId).get()
                .addOnSuccessListener { doc ->
                    if (doc.exists()) onFound(doc) else tryCollection(index + 1)
                }
                .addOnFailureListener { tryCollection(index + 1) }
        }
        tryCollection(0)
    }

    private fun addHistory(profile: DriverProfile, rideId: String, action: String, valueNumber: Double, collectionName: String) {
        db.collection("historicoEntregador").add(
            mapOf(
                "entregadorId" to profile.id,
                "entregadorUid" to profile.id,
                "driverId" to profile.id,
                "entregadorNome" to profile.name,
                "rotaId" to rideId,
                "rideId" to rideId,
                "missaoTipo" to collectionName,
                "tipo" to action,
                "status" to action,
                "titulo" to action,
                "valorRota" to valueNumber,
                "valueNumber" to valueNumber,
                "criadoEm" to Timestamp.now(),
                "createdAt" to Timestamp.now(),
                "origem" to "android_native"
            )
        )
    }

    private fun DocumentSnapshot.toProfile(collectionName: String): DriverProfile {
        val name = anyString("nome", "nomeCompleto", "name", "driverName", "apelido").ifBlank { "Entregador" }
        val statusCadastro = anyString("statusCadastro", "statusAprovacao", "statusAnalise", "aprovacao").upperOrTrim()
        val blocked = anyBoolean("bloqueado", "blocked") == true || statusCadastro in BLOCKED_STATUSES
        val approvedFlag = anyBoolean("aprovado", "approved")
        val approved = when {
            approvedFlag == true -> true
            approvedFlag == false -> false
            statusCadastro.isBlank() -> true
            else -> statusCadastro in APPROVED_STATUSES
        }
        return DriverProfile(
            id = id,
            name = name,
            phone = anyString("telefone", "celular", "phone", "whatsapp"),
            photoUrl = anyString("fotoPerfilUrl", "urlPerfil", "avatarUrl", "photoUrl", "avatar"),
            collectionName = collectionName,
            verified = approved,
            approved = approved,
            blocked = blocked,
            approvalStatus = statusCadastro,
            passwordHash = anyString("senhaHash", "senhaAppHash", "passwordHash", "pinHash"),
            passwordPlain = anyString("senha", "senhaApp", "password", "pin")
        )
    }

    private fun valueNumberFromDoc(doc: DocumentSnapshot): Double {
        return doc.anyDouble(
            "valueNumber",
            "valorRota",
            "valorEntrega",
            "valorEntregador",
            "valorRepasseMotoboy",
            "valorCorrida",
            "taxaEntrega"
        ) ?: doc.anyString("value", "valor", "valorFormatado").toMoneyDouble() ?: 0.0
    }

    fun formatCurrency(value: Double): String = NumberFormat.getCurrencyInstance(Locale("pt", "BR")).format(value)

    private fun sha256(value: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(value.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    private fun Date.formatHour(): String = SimpleDateFormat("HH:mm", Locale("pt", "BR")).format(this)

    private fun String.onlyDigits(): String = filter { it.isDigit() }

    private fun maskCpf(digits: String): String {
        if (digits.length != 11) return digits
        return "${digits.substring(0, 3)}.${digits.substring(3, 6)}.${digits.substring(6, 9)}-${digits.substring(9)}"
    }
}

private class CompositeListenerRegistration(private val listeners: List<ListenerRegistration>) : ListenerRegistration {
    override fun remove() {
        listeners.forEach { it.remove() }
    }
}

data class DriverProfile(
    val id: String,
    val name: String,
    val phone: String = "",
    val photoUrl: String = "",
    val collectionName: String = "entregadores",
    val verified: Boolean = true,
    val approved: Boolean = true,
    val blocked: Boolean = false,
    val approvalStatus: String = "",
    val passwordHash: String = "",
    val passwordPlain: String = ""
) {
    val hasPassword: Boolean get() = passwordHash.isNotBlank() || passwordPlain.isNotBlank()

    fun passwordMatches(password: String): Boolean {
        if (!hasPassword) return true
        if (password.isBlank()) return false
        if (passwordPlain.isNotBlank() && password == passwordPlain) return true
        if (passwordHash.isNotBlank()) {
            val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
            val hash = bytes.joinToString("") { "%02x".format(it) }
            return hash.equals(passwordHash, ignoreCase = true)
        }
        return false
    }
}

data class DriverRegistrationRequest(
    val name: String,
    val cpf: String,
    val phone: String,
    val password: String,
    val vehicle: String = "Moto",
    val plate: String = ""
)

data class DriverStats(
    val totalToday: Double = 0.0,
    val finishedCount: Int = 0,
    val score: Int = 100
)

data class DriverHistory(
    val id: String,
    val rideId: String,
    val action: String,
    val value: String,
    val createdAtMillis: Long,
    val createdLabel: String
)

data class DriverRide(
    val id: String,
    val collectionName: String,
    val status: String,
    val rawStatus: String,
    val value: String,
    val valueNumber: Double,
    val distance: String,
    val duration: String,
    val pickup: String,
    val dropoff: String,
    val assignedDriverId: String,
    val targetDriverId: String,
    val broadcast: Boolean,
    val customerName: String,
    val orderCode: String,
    val stops: Int,
    val rejectedDriverIds: List<String>,
    val expiredDriverIds: List<String>
) {
    fun matchesDriver(driverId: String): Boolean {
        val ids = listOf(assignedDriverId, targetDriverId).filter { it.isNotBlank() }
        return ids.contains(driverId)
    }

    fun canBeOfferedTo(driverId: String): Boolean {
        if (rejectedDriverIds.contains(driverId) || expiredDriverIds.contains(driverId)) return false
        if (matchesDriver(driverId)) return true
        return broadcast || (assignedDriverId.isBlank() && targetDriverId.isBlank())
    }
}

private val OFFER_STATUSES = setOf("BUSCANDO_ENTREGADOR", "OFERTA", "PENDENTE", "AGUARDANDO_ENTREGADOR", "PENDING", "BUSCANDO_MOTOBOY")
private val ACCEPTED_STATUSES = setOf("ACEITA", "A_CAMINHO_LOJA", "AGUARDANDO_PRONTOS", "ACCEPTED", "ACEITO", "INDO_COLETA")
private val PICKUP_STATUSES = setOf("COLETANDO", "LIBERADA_PARA_SAIDA", "PICKUP", "EM_COLETA", "COLETADO")
private val DELIVERING_STATUSES = setOf("EM_ROTA", "SAIU_ENTREGA", "A_CAMINHO_CLIENTE", "ENTREGADOR_NO_LOCAL", "DELIVERING", "EM_ENTREGA", "CHEGOU_ENTREGA")
private val FINAL_HISTORY_STATUSES = setOf("CONCLUIDA", "ENTREGUE", "FINALIZADA", "FINISHED", "DELIVERED", "finished", "delivered")
private val APPROVED_STATUSES = setOf("APROVADO", "APPROVED", "LIBERADO", "ATIVO", "ACTIVE")
private val BLOCKED_STATUSES = setOf("REPROVADO", "BLOQUEADO", "BLOCKED", "SUSPENSO", "SUSPENDED", "CANCELADO")

private fun normalizeUiStatus(raw: String): String {
    val status = raw.upperOrTrim()
    return when {
        status in OFFER_STATUSES -> "pending"
        status in ACCEPTED_STATUSES -> "accepted"
        status in PICKUP_STATUSES -> "pickup"
        status in DELIVERING_STATUSES -> "delivering"
        status in FINAL_HISTORY_STATUSES -> "finished"
        else -> raw.ifBlank { "pending" }
    }
}

private fun DocumentSnapshot.toDriverRide(collectionName: String): DriverRide? {
    val rawStatus = anyString("status", "statusEntregador", "statusMotoboy", "statusOfertaEntregador").ifBlank { "pending" }
    val number = anyDouble(
        "valueNumber",
        "valorRota",
        "valorCorrida",
        "valorEntregador",
        "valorRepasseMotoboy",
        "valorEntrega",
        "taxaEntrega"
    ) ?: anyString("value", "valor", "valorFormatado").toMoneyDouble() ?: 0.0
    val assigned = anyString("entregadorId", "entregadorUid", "motoboyId", "uidEntregador", "driverId", "assignedDriverId")
    val target = anyString("entregadorAtualOferta", "motoboyAtualOferta", "targetDriverId")
    val rejected = anyStringList("rejeitados", "rejeitadoPor", "rejeitadosIds", "entregadoresRejeitaram", "rejectedDriverIds")
    val expired = anyStringList("expiredDriverIds", "expiradoPara", "expirados")
    val pickup = anyString("lojaEndereco", "pickup", "pickupAddress", "enderecoLoja", "nomeLoja", "lojaNome")
        .ifBlank { "Rodrigues Acai e Cia" }
    val dropoff = anyAddressString()
    val km = anyDouble("kmTotal", "distanciaKm", "distanciaTotal", "distancia") ?: 0.0
    val minutes = anyDouble("tempoTotalMin", "tempoMin", "tempoEstimado", "tempo") ?: 0.0

    return DriverRide(
        id = id,
        collectionName = collectionName,
        status = normalizeUiStatus(rawStatus),
        rawStatus = rawStatus,
        value = DriverRepository.formatCurrency(number),
        valueNumber = number,
        distance = if (km > 0.0) "${String.format(Locale("pt", "BR"), "%.1f", km)} km" else anyString("distance").ifBlank { "-- km" },
        duration = if (minutes > 0.0) "${minutes.toInt()} min" else anyString("duration", "estimatedTime").ifBlank { "-- min" },
        pickup = pickup,
        dropoff = dropoff,
        assignedDriverId = assigned,
        targetDriverId = target,
        broadcast = anyBoolean("broadcast", "paraTodos", "ofertaParaTodos") ?: false,
        customerName = anyString("customerName", "clientName", "clienteNome", "nomeCliente", "nome").ifBlank { "Cliente" },
        orderCode = anyString("orderCode", "orderId", "numeroPedido", "codigoPedido").ifBlank { id.takeLast(6).uppercase() },
        stops = (anyDouble("stops", "paradas", "quantidadePedidos") ?: 2.0).toInt().coerceAtLeast(1),
        rejectedDriverIds = rejected,
        expiredDriverIds = expired
    )
}

private fun DocumentSnapshot.matchesLogin(values: List<String>): Boolean {
    val normalizedValues = values.flatMap { value -> listOf(value.trim(), value.onlyDigitsLocal()) }.filter { it.isNotBlank() }.toSet()
    val fields = listOf(
        id,
        anyString("id", "uid", "cpf", "cpfLimpo", "cpfMascarado", "documento", "telefone", "celular", "phone", "whatsapp")
    ) + listOf("id", "uid", "cpf", "cpfLimpo", "cpfMascarado", "documento", "telefone", "celular", "phone", "whatsapp").map { key -> get(key)?.toString().orEmpty() }
    return fields.any { field ->
        val value = field.trim()
        value.isNotBlank() && (normalizedValues.contains(value) || normalizedValues.contains(value.onlyDigitsLocal()))
    }
}

private fun DocumentSnapshot.matchesDriverId(driverId: String): Boolean {
    val ids = listOf(
        anyString("entregadorId", "entregadorUid", "motoboyId", "uidEntregador", "driverId", "assignedDriverId"),
        id
    )
    return ids.any { it == driverId }
}

private fun DocumentSnapshot.anyString(vararg keys: String): String {
    for (key in keys) {
        val value = get(key)
        if (value != null) return value.toString().trim()
    }
    return ""
}

private fun DocumentSnapshot.anyDouble(vararg keys: String): Double? {
    for (key in keys) {
        val value = get(key)
        when (value) {
            is Number -> return value.toDouble()
            is String -> value.toMoneyDouble()?.let { return it }
        }
    }
    return null
}

private fun DocumentSnapshot.anyBoolean(vararg keys: String): Boolean? {
    for (key in keys) {
        val value = get(key)
        when (value) {
            is Boolean -> return value
            is String -> {
                val normalized = value.upperOrTrim()
                if (normalized in setOf("TRUE", "SIM", "YES", "1", "APROVADO")) return true
                if (normalized in setOf("FALSE", "NAO", "NÃO", "NO", "0", "REPROVADO", "BLOQUEADO", "PENDENTE")) return false
            }
        }
    }
    return null
}

private fun DocumentSnapshot.anyTimestamp(vararg keys: String): Timestamp? {
    for (key in keys) {
        val value = getTimestamp(key)
        if (value != null) return value
    }
    return null
}

private fun DocumentSnapshot.anyStringList(vararg keys: String): List<String> {
    val result = linkedSetOf<String>()
    for (key in keys) {
        val value = get(key)
        when (value) {
            is List<*> -> value.mapNotNullTo(result) { it?.toString() }
            is String -> if (value.isNotBlank()) result.add(value)
        }
    }
    return result.toList()
}

private fun DocumentSnapshot.anyAddressString(): String {
    val direct = anyString("dropoff", "dropoffAddress", "enderecoCompleto", "enderecoFormatado", "enderecoEntrega")
    if (direct.isNotBlank()) return direct
    val endereco = get("endereco")
    if (endereco is Map<*, *>) {
        val parts = listOf("rua", "logradouro", "numero", "bairro", "cidade", "uf").mapNotNull { key ->
            endereco[key]?.toString()?.takeIf { it.isNotBlank() }
        }
        if (parts.isNotEmpty()) return parts.joinToString(", ")
    }
    val bairro = anyString("bairro", "regiao")
    return if (bairro.isNotBlank()) "Regiao: $bairro" else "Endereco do cliente liberado apos aceite"
}

private fun String.upperOrTrim(): String = trim().uppercase(Locale.ROOT)

private fun String.onlyDigitsLocal(): String = filter { it.isDigit() }

private fun String.toMoneyDouble(): Double? {
    val cleaned = replace("R$", "")
        .replace(" ", "")
        .replace(".", "")
        .replace(",", ".")
        .trim()
    return cleaned.toDoubleOrNull()
}
