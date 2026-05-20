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
import java.util.Calendar
import java.util.Locale

object DriverRepository {
    private const val PREFS = "driver_session"
    private const val KEY_ID = "driver_id"
    private const val KEY_NAME = "driver_name"
    private const val KEY_PHONE = "driver_phone"
    private const val KEY_PHOTO = "driver_photo"
    private const val KEY_COLLECTION = "driver_collection"
    private const val KEY_PIX = "driver_pix"
    private const val KEY_BANK = "driver_bank"
    private const val KEY_NEEDS_PASSWORD = "driver_needs_password"

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
            pixKey = prefs.getString(KEY_PIX, null).orEmpty(),
            bankName = prefs.getString(KEY_BANK, null).orEmpty(),
            needsPasswordSetup = prefs.getBoolean(KEY_NEEDS_PASSWORD, false),
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
                !profile.approved -> onError("Cadastro encontrado, mas ainda aguardando aprovação do gestor.")
                profile.hasPassword && !profile.passwordMatches(password) -> onError("Senha incorreta. Confira a senha cadastrada para este entregador.")
                else -> saveSession(context, profile.copy(needsPasswordSetup = !profile.hasPassword), onSuccess)
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
            "chavePix" to request.pixKey.trim(),
            "pix" to request.pixKey.trim(),
            "banco" to request.bankName.trim(),
            "statusCadastro" to "PENDENTE",
            "statusAprovacao" to "PENDENTE",
            "aprovado" to false,
            "online" to false,
            "status" to "Aguardando aprovação",
            "senhaHash" to sha256(request.password),
            "senhaCriadaEm" to now,
            "origemCadastro" to "android_native",
            "platform" to "android_native",
            "appVersion" to "5.7.0-alpha-rastreio-real",
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
                    .addOnFailureListener { onError(it.message ?: "Cadastro salvo, mas falhou ao criar solicitação.") }
            }
            .addOnFailureListener { onError(it.message ?: "Falha ao enviar cadastro.") }
    }


    fun updateAccessPassword(
        context: Context,
        newPassword: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val profile = currentSession(context)
        if (profile == null) {
            onError("Faça login antes de alterar a senha.")
            return
        }
        if (newPassword.length < 6) {
            onError("A senha precisa ter pelo menos 6 caracteres.")
            return
        }
        val now = Timestamp.now()
        db.collection(profile.collectionName).document(profile.id).set(
            mapOf(
                "senhaHash" to sha256(newPassword),
                "senhaAppHash" to sha256(newPassword),
                "senhaAtualizadaEm" to now,
                "passwordUpdatedAt" to now,
                "atualizadoEm" to now,
                "updatedAt" to now,
                "appVersion" to "5.7.0-alpha-rastreio-real"
            ),
            SetOptions.merge()
        ).addOnSuccessListener {
            context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
                .putBoolean(KEY_NEEDS_PASSWORD, false)
                .apply()
            onSuccess()
        }
            .addOnFailureListener { onError(it.message ?: "Falha ao salvar senha.") }
    }

    fun updatePayoutData(
        context: Context,
        pixKey: String,
        bankName: String,
        payoutType: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val profile = currentSession(context)
        if (profile == null) {
            onError("Faça login antes de salvar recebimento.")
            return
        }
        if (pixKey.trim().length < 3) {
            onError("Informe uma chave Pix valida.")
            return
        }
        val now = Timestamp.now()
        db.collection(profile.collectionName).document(profile.id).set(
            mapOf(
                "chavePix" to pixKey.trim(),
                "pix" to pixKey.trim(),
                "banco" to bankName.trim(),
                "tipoRepasse" to payoutType.trim().ifBlank { "Pix" },
                "recebimentoAtualizadoEm" to now,
                "recebimentoStatus" to "PENDENTE_CONFERENCIA",
                "atualizadoEm" to now,
                "updatedAt" to now,
                "appVersion" to "5.7.0-alpha-rastreio-real"
            ),
            SetOptions.merge()
        ).addOnSuccessListener {
            context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
                .putString(KEY_PIX, pixKey.trim())
                .putString(KEY_BANK, bankName.trim())
                .apply()
            onSuccess()
        }.addOnFailureListener { onError(it.message ?: "Falha ao salvar dados de recebimento.") }
    }

    fun requestProfileChange(
        context: Context,
        requestText: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val profile = currentSession(context)
        if (profile == null) {
            onError("Faça login antes de solicitar alteracao.")
            return
        }
        val text = requestText.trim()
        if (text.length < 8) {
            onError("Descreva o que precisa alterar.")
            return
        }
        val now = Timestamp.now()
        db.collection("solicitacoesEntregadores").add(
            mapOf(
                "tipo" to "ALTERACAO_DADOS_ENTREGADOR",
                "entregadorId" to profile.id,
                "entregadorNome" to profile.name,
                "telefone" to profile.phone,
                "descricao" to text,
                "status" to "PENDENTE",
                "prioridade" to "NORMAL",
                "origem" to "android_native",
                "appVersion" to "5.7.0-alpha-rastreio-real",
                "criadoEm" to now,
                "createdAt" to now
            )
        ).addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it.message ?: "Falha ao enviar solicitação.") }
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
            .putString(KEY_PIX, profile.pixKey)
            .putString(KEY_BANK, profile.bankName)
            .putBoolean(KEY_NEEDS_PASSWORD, profile.needsPasswordSetup)
            .apply()

        db.collection(profile.collectionName).document(profile.id).set(
            mapOf(
                "ultimoLoginEm" to Timestamp.now(),
                "lastLoginAt" to Timestamp.now(),
                "platform" to "android_native",
                "appVersion" to "5.7.0-alpha-rastreio-real"
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
            "appVersion" to "5.7.0-alpha-rastreio-real"
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
            val ride = state.values.flatten()
                .distinctBy { "${it.collectionName}:${it.id}" }
                .firstOrNull { it.status == "pending" }
            onRide(ride)
        }

        val registrations = mutableListOf<ListenerRegistration>()
        MISSION_COLLECTIONS.forEach { collectionName ->
            val base = db.collection(collectionName)
            val queries = listOf(
                base.limit(250),
                base.whereEqualTo("status", "BUSCANDO_ENTREGADOR").limit(80),
                base.whereEqualTo("statusEntrega", "BUSCANDO_ENTREGADOR").limit(80),
                base.whereEqualTo("statusOfertaEntregador", "OFERTA").limit(80),
                base.whereEqualTo("statusEntregador", "OFERTA").limit(80),
                base.whereEqualTo("statusMotoboy", "OFERTA").limit(80),
                base.whereEqualTo("liberadoParaEntregador", true).limit(80),
                base.whereEqualTo("ofertaLiberada", true).limit(80),
                base.whereEqualTo("entregadorAtualOferta", profile.id).limit(80),
                base.whereEqualTo("targetDriverId", profile.id).limit(80),
                base.whereEqualTo("motoboyAtualOferta", profile.id).limit(80),
                base.whereEqualTo("driverAtualOferta", profile.id).limit(80),
                base.whereEqualTo("status", "TOCANDO").limit(80),
                base.whereEqualTo("status", "RADAR_ATIVO").limit(80)
            )

            queries.forEachIndexed { index, firestoreQuery ->
                val key = "$collectionName:$index"
                registrations.add(
                    firestoreQuery.addSnapshotListener { snap, err ->
                        if (err != null) {
                            onError(err.message ?: "Erro ao ouvir $collectionName.")
                            return@addSnapshotListener
                        }
                        val rides = snap?.documents.orEmpty()
                            .mapNotNull { doc -> doc.toDriverRide(collectionName) }
                            .filter { ride -> ride.status == "pending" && ride.canBeOfferedTo(profile.id) }
                            .distinctBy { it.id }
                        state[key] = rides
                        emit()
                    }
                )
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
            val active = state.values.flatten()
                .distinctBy { "${it.collectionName}:${it.id}" }
                .firstOrNull { it.status in listOf("accepted", "pickup", "delivering") }
            onRide(active)
        }

        val registrations = mutableListOf<ListenerRegistration>()
        MISSION_COLLECTIONS.forEach { collectionName ->
            val base = db.collection(collectionName)
            val queries = listOf(
                base.limit(250),
                base.whereEqualTo("entregadorUid", profile.id).limit(80),
                base.whereEqualTo("entregadorId", profile.id).limit(80),
                base.whereEqualTo("driverId", profile.id).limit(80),
                base.whereEqualTo("uidEntregador", profile.id).limit(80),
                base.whereEqualTo("entregadorAtualOferta", profile.id).limit(80),
                base.whereEqualTo("targetDriverId", profile.id).limit(80)
            )
            queries.forEachIndexed { index, firestoreQuery ->
                val key = "$collectionName:$index"
                registrations.add(
                    firestoreQuery.addSnapshotListener { snap, err ->
                        if (err != null) {
                            onError(err.message ?: "Erro ao ouvir $collectionName.")
                            return@addSnapshotListener
                        }
                        val rides = snap?.documents.orEmpty()
                            .mapNotNull { doc -> doc.toDriverRide(collectionName) }
                            .filter { ride -> ride.matchesDriver(profile.id) && ride.status in listOf("accepted", "pickup", "delivering") }
                            .distinctBy { it.id }
                        state[key] = rides
                        emit()
                    }
                )
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
                val mapped = snap?.documents.orEmpty()
                    .filter { it.matchesDriverId(profile.id) }
                    .map { doc ->
                        val action = doc.anyString("statusAtual", "tipo", "status", "action", "titulo").ifBlank { "registro" }
                        val rawRideId = doc.anyString("rotaId", "rideId", "pedidoId", "missaoId", "corridaId").ifBlank { doc.id }
                        val displayCode = doc.anyString("codigoPedido", "numeroPedido", "orderCode", "pedidoCodigo")
                            .ifBlank { rawRideId.takeLast(4).uppercase(Locale.ROOT) }
                        val date = doc.anyTimestamp("statusAtualizadoEm", "atualizadoEm", "updatedAt", "criadoEm", "createdAt")?.toDate()
                        DriverHistory(
                            id = doc.id,
                            rideId = displayCode,
                            action = action,
                            value = formatCurrency(valueNumberFromDoc(doc)),
                            createdAtMillis = date?.time ?: 0L,
                            createdLabel = date?.formatHistoryLabel().orEmpty().ifBlank { "Agora" }
                        )
                    }

                val list = mapped
                    .groupBy { it.rideId }
                    .mapNotNull { (_, items) -> items.maxByOrNull { it.createdAtMillis } }
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
                onStats(DriverStats(totalToday = total, totalWeek = total, totalMonth = total, finishedCount = finished.size, score = 100))
            }
    }

    fun acceptRide(context: Context, rideId: String, onDone: () -> Unit = {}, onError: (String) -> Unit = {}) {
        val profile = currentSession(context)
        if (profile == null) {
            onError("Faça login antes de aceitar corrida.")
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
                "repasseEntregadorConfirmado" to (ride?.valueNumber ?: 0.0),
                "valorRepasseMotoboy" to (ride?.valueNumber ?: 0.0),
                "aceitaEm" to Timestamp.now(),
                "acceptedAt" to Timestamp.now(),
                "statusAtualizadoEm" to Timestamp.now(),
                "atualizadoEm" to Timestamp.now(),
                "updatedAt" to Timestamp.now()
            )
            doc.reference.set(update, SetOptions.merge())
                .addOnSuccessListener {
                    db.collection(profile.collectionName).document(profile.id).set(
                        mapOf("status" to "Em rota", "online" to true, "ultimaAceitacaoEm" to Timestamp.now()),
                        SetOptions.merge()
                    )
                    db.collection(profile.collectionName).document(profile.id).set(
                        mapOf(
                            "statusOperacional" to "A_CAMINHO_LOJA",
                            "corridaAtualId" to rideId,
                            "missaoAtualId" to rideId,
                            "pedidoAtualId" to if (collectionName == "pedidos") rideId else null,
                            "rotaAtualId" to if (collectionName == "rotas_entrega") rideId else null,
                            "rastreamentoAtivo" to true,
                            "codigoPedidoAtual" to (ride?.orderCode?.ifBlank { rideId.takeLast(4).uppercase(Locale.ROOT) } ?: rideId.takeLast(4).uppercase(Locale.ROOT)),
                            "atualizadoEm" to Timestamp.now(),
                            "updatedAt" to Timestamp.now()
                        ),
                        SetOptions.merge()
                    )
                    addHistory(profile, rideId, "ACEITA", ride?.valueNumber ?: 0.0, collectionName, ride)
                    onDone()
                }
                .addOnFailureListener { onError(it.message ?: "Falha ao aceitar corrida.") }
        }, onNotFound = {
            onError("Corrida nao encontrada.")
        })
    }

    fun rejectRide(context: Context, rideId: String, reason: String = "", onDone: () -> Unit = {}, onError: (String) -> Unit = {}) {
        val profile = currentSession(context)
        if (profile == null) {
            onError("Faça login antes de rejeitar corrida.")
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
                "motivoRejeicao" to reason.trim(),
                "rejectionReason" to reason.trim(),
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
                    addHistory(profile, rideId, if (reason.isBlank()) "REJEITADA" else "REJEITADA: $reason", ride?.valueNumber ?: 0.0, collectionName, ride)
                    onDone()
                }
                .addOnFailureListener { onError(it.message ?: "Falha ao rejeitar corrida.") }
        }, onNotFound = {
            onError("Corrida nao encontrada.")
        })
    }

    fun expireRide(context: Context, rideId: String, onDone: () -> Unit = {}, onError: (String) -> Unit = {}) {
        val profile = currentSession(context)
        if (profile == null) {
            onError("Faça login antes de expirar corrida.")
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
                addHistory(profile, rideId, "EXPIRADA", 0.0, collectionName, doc.toDriverRide(collectionName))
                onDone()
            }.addOnFailureListener { onError(it.message ?: "Falha ao expirar oferta.") }
        }, onNotFound = {
            onError("Corrida nao encontrada.")
        })
    }

    fun updateRideStatus(context: Context, rideId: String, status: String, onDone: () -> Unit = {}, onError: (String) -> Unit = {}) {
        val profile = currentSession(context)
        if (profile == null) {
            onError("Faça login para atualizar a corrida.")
            return
        }
        findMissionDocument(rideId, onFound = { doc ->
            val collectionName = doc.reference.parent.id
            val ride = doc.toDriverRide(collectionName)
            val realStatus = when (status) {
                "pickup" -> "COLETANDO"
                "delivering" -> "EM_ROTA"
                "arrived_client" -> "ENTREGADOR_NO_LOCAL"
                "finished" -> "CONCLUIDA"
                else -> status
            }
            val fields = linkedMapOf<String, Any?>(
                "status" to realStatus,
                "statusEntregador" to realStatus,
                "statusMotoboy" to realStatus,
                "statusAtualizadoEm" to Timestamp.now(),
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
                "arrived_client" -> {
                    fields["entregadorChegouClienteEm"] = Timestamp.now()
                    fields["arrivedAtClientAt"] = Timestamp.now()
                    fields["manterCorridaAberta"] = true
                }
                "finished" -> {
                    fields["concluidaEm"] = Timestamp.now()
                    fields["finishedAt"] = Timestamp.now()
                    fields["entregueEm"] = Timestamp.now()
                    fields["repasseEntregadorConfirmado"] = ride?.valueNumber ?: 0.0
                    fields["valorRepasseMotoboy"] = ride?.valueNumber ?: 0.0
                    fields["financeiroConferidoPeloApp"] = true
                }
            }
            doc.reference.set(fields, SetOptions.merge())
                .addOnSuccessListener {
                    addHistory(profile, rideId, realStatus, ride?.valueNumber ?: 0.0, collectionName, ride)
                    db.collection(profile.collectionName).document(profile.id).set(
                        mapOf(
                            "statusOperacional" to realStatus.toOperationalFirestoreStatus(),
                            "corridaAtualId" to rideId,
                            "missaoAtualId" to rideId,
                            "pedidoAtualId" to if (collectionName == "pedidos") rideId else null,
                            "rotaAtualId" to if (collectionName == "rotas_entrega") rideId else null,
                            "rastreamentoAtivo" to (status != "finished"),
                            "codigoPedidoAtual" to (ride?.orderCode?.ifBlank { rideId.takeLast(4).uppercase(Locale.ROOT) } ?: rideId.takeLast(4).uppercase(Locale.ROOT)),
                            "atualizadoEm" to Timestamp.now(),
                            "updatedAt" to Timestamp.now()
                        ),
                        SetOptions.merge()
                    )
                    if (status == "finished") {
                        db.collection(profile.collectionName).document(profile.id).set(
                            mapOf(
                                "status" to "Livre",
                                "online" to true,
                                "ultimaConclusaoEm" to Timestamp.now(),
                                "totalEntregas" to FieldValue.increment(1),
                                "saldoLiquido" to FieldValue.increment(ride?.valueNumber ?: 0.0),
                                "rastreamentoAtivo" to false,
                                "statusOperacional" to "LIVRE",
                                "corridaAtualId" to null,
                                "missaoAtualId" to null,
                                "pedidoAtualId" to null,
                                "rotaAtualId" to null
                            ),
                            SetOptions.merge()
                        )
                    }
                    onDone()
                }
                .addOnFailureListener { onError(it.message ?: "Falha ao atualizar corrida.") }
        }, onNotFound = {
            onError("Corrida nao encontrada.")
        })
    }

    fun registerRideOccurrence(
        context: Context,
        rideId: String,
        reason: String,
        note: String = "",
        onDone: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        val profile = currentSession(context)
        if (profile == null) {
            onError("Faça login para registrar ocorrência.")
            return
        }
        val reasonText = reason.trim().ifBlank { "Ocorrência sem motivo informado" }
        findMissionDocument(rideId, onFound = { doc ->
            val collectionName = doc.reference.parent.id
            val ride = doc.toDriverRide(collectionName)
            val now = Timestamp.now()
            val payload = linkedMapOf<String, Any?>(
                "statusOcorrencia" to "PENDENTE_SOLUCAO",
                "ocorrenciaAtiva" to true,
                "ultimaOcorrenciaMotivo" to reasonText,
                "ultimaOcorrenciaObservacao" to note.trim(),
                "ultimaOcorrenciaEm" to now,
                "statusAtualizadoEm" to now,
                "atualizadoEm" to now,
                "updatedAt" to now,
                "ocorrenciaEntrega" to mapOf(
                    "ativa" to true,
                    "motivo" to reasonText,
                    "observacao" to note.trim(),
                    "entregadorId" to profile.id,
                    "entregadorNome" to profile.name,
                    "criadoEm" to now
                )
            )
            doc.reference.set(payload, SetOptions.merge())
                .addOnSuccessListener {
                    addHistory(profile, rideId, "OCORRENCIA: $reasonText", ride?.valueNumber ?: 0.0, collectionName, ride)
                    db.collection(profile.collectionName).document(profile.id).set(
                        mapOf(
                            "statusOperacional" to "OCORRENCIA_ENTREGA",
                            "corridaAtualId" to rideId,
                            "missaoAtualId" to rideId,
                            "ultimaOcorrenciaMotivo" to reasonText,
                            "rastreamentoAtivo" to true,
                            "atualizadoEm" to now,
                            "updatedAt" to now
                        ),
                        SetOptions.merge()
                    )
                    onDone()
                }
                .addOnFailureListener { onError(it.message ?: "Falha ao registrar ocorrência.") }
        }, onNotFound = {
            onError("Corrida nao encontrada.")
        })
    }

    fun updateDriverLocationForTracking(
        context: Context,
        ride: DriverRide,
        lat: Double,
        lng: Double,
        accuracy: Float,
        speed: Float,
        bearing: Float,
        distanciaPercorridaKm: Double,
        tempoMovimentoMin: Long,
        intervaloSeg: Long,
        onError: (String) -> Unit = {}
    ) {
        val profile = currentSession(context) ?: return
        val now = Timestamp.now()
        val statusOperacional = ride.status.toOperationalFirestoreStatus()

        val payload = linkedMapOf<String, Any?>(
            "online" to true,
            "status" to "Em rota",
            "statusOnline" to "Online",
            "statusOperacional" to statusOperacional,
            "corridaAtualId" to ride.id,
            "missaoAtualId" to ride.id,
            "missaoAtualTipo" to ride.collectionName,
            "pedidoAtualId" to if (ride.collectionName == "pedidos") ride.id else null,
            "rotaAtualId" to if (ride.collectionName == "rotas_entrega") ride.id else null,
            "codigoPedidoAtual" to ride.orderCode.ifBlank { ride.id.takeLast(4).uppercase(Locale.ROOT) },
            "rastreamentoAtivo" to true,
            "localizacaoAtualizadaEm" to now,
            "atualizadoEm" to now,
            "updatedAt" to now,
            "coords" to mapOf(
                "lat" to lat,
                "lng" to lng,
                "accuracy" to accuracy.toDouble(),
                "speed" to speed.toDouble(),
                "bearing" to bearing.toDouble(),
                "updatedAt" to now
            ),
            "rastreamento" to mapOf(
                "ativo" to true,
                "pedidoAtualId" to if (ride.collectionName == "pedidos") ride.id else null,
                "rotaAtualId" to if (ride.collectionName == "rotas_entrega") ride.id else null,
                "corridaAtualId" to ride.id,
                "statusOperacional" to statusOperacional,
                "distanciaPercorridaKm" to distanciaPercorridaKm,
                "tempoMovimentoMin" to tempoMovimentoMin,
                "intervaloSeg" to intervaloSeg,
                "atualizadoEm" to now
            ),
            "localizacaoOrigem" to "android_native_v5_7"
        )

        db.collection(profile.collectionName).document(profile.id)
            .set(payload, SetOptions.merge())
            .addOnFailureListener { onError(it.message ?: "Falha ao atualizar localização.") }
    }

    fun clearDriverTracking(context: Context) {
        val profile = currentSession(context) ?: return
        val now = Timestamp.now()
        db.collection(profile.collectionName).document(profile.id).set(
            mapOf(
                "rastreamentoAtivo" to false,
                "statusOperacional" to "LIVRE",
                "corridaAtualId" to null,
                "missaoAtualId" to null,
                "pedidoAtualId" to null,
                "rotaAtualId" to null,
                "rastreamento" to mapOf(
                    "ativo" to false,
                    "finalizadoEm" to now
                ),
                "atualizadoEm" to now,
                "updatedAt" to now
            ),
            SetOptions.merge()
        )
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

    private fun addHistory(
        profile: DriverProfile,
        rideId: String,
        action: String,
        valueNumber: Double,
        collectionName: String,
        ride: DriverRide? = null
    ) {
        val now = Timestamp.now()
        val displayCode = ride?.orderCode?.takeIf { it.isNotBlank() } ?: rideId.takeLast(4).uppercase(Locale.ROOT)
        val historyId = "${profile.id}_${collectionName}_${rideId}"
            .replace(Regex("[^A-Za-z0-9_-]"), "_")
            .take(180)
        val humanTitle = action.historyHumanLabel()

        db.collection("historicoEntregador").document(historyId).set(
            mapOf(
                "entregadorId" to profile.id,
                "entregadorUid" to profile.id,
                "driverId" to profile.id,
                "entregadorNome" to profile.name,
                "rotaId" to rideId,
                "rideId" to rideId,
                "pedidoId" to if (collectionName == "pedidos") rideId else null,
                "corridaId" to rideId,
                "missaoTipo" to collectionName,
                "codigoPedido" to displayCode,
                "numeroPedido" to displayCode,
                "tipo" to action,
                "status" to action,
                "statusAtual" to action,
                "titulo" to humanTitle,
                "valorRota" to valueNumber,
                "valueNumber" to valueNumber,
                "repasseEntregador" to valueNumber,
                "valorRepasseMotoboy" to valueNumber,
                "statusAtualizadoEm" to now,
                "atualizadoEm" to now,
                "updatedAt" to now,
                "criadoEm" to now,
                "createdAt" to now,
                "origem" to "android_native_v5_7_alpha",
                "eventosStatus" to FieldValue.arrayUnion(
                    mapOf(
                        "status" to action,
                        "titulo" to humanTitle,
                        "data" to Date().time,
                        "origem" to "android_native"
                    )
                )
            ),
            SetOptions.merge()
        )
    }

    private fun DocumentSnapshot.toProfile(collectionName: String): DriverProfile {
        val name = anyString("nome", "nomeCompleto", "name", "driverName", "apelido").ifBlank { "Entregador" }
        val statusCadastro = anyString("statusCadastro", "statusAprovacao", "statusAnalise", "aprovação").upperOrTrim()
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
            pixKey = anyString("chavePix", "pix", "pixKey", "chavePIX"),
            bankName = anyString("banco", "bank", "bankName", "instituicao"),
            verified = approved,
            approved = approved,
            blocked = blocked,
            approvalStatus = statusCadastro,
            passwordHash = anyString("senhaHash", "senhaAppHash", "passwordHash", "pinHash"),
            passwordPlain = anyString("senha", "senhaApp", "password", "pin")
        )
    }

    private fun valueNumberFromDoc(doc: DocumentSnapshot): Double {
        return doc.driverPayoutValue()
    }

    private fun DocumentSnapshot.driverPayoutValue(): Double {
        return anyDouble(
            "repasseFrota",
            "repassePiloto",
            "valorRepasseFrota",
            "valorRepassePiloto",
            "financeiroEntrega.repasseFrota",
            "financeiroEntrega.repassePiloto",
            "valores.repasseFrota",
            "valores.repassePiloto",
            "logistica.repasseFrota",
            "logistica.repassePiloto",
            "calculo.valorTotalMotoboy",
            "valorTotalMotoboy",
            "valorMotoboy",
            "valorEntregador",
            "valorRepasseMotoboy",
            "valorCorrida",
            "valorRota"
        ) ?: anyString("repasse", "valorRepasse", "valorMotoboyFormatado").toMoneyDouble() ?: 0.0
    }

    private fun DocumentSnapshot.clientTotalValue(): Double {
        return anyDouble(
            "valorTotalPedido",
            "totalPedido",
            "total",
            "valorTotalCliente",
            "pedidoTotal",
            "valorCobrarCliente"
        ) ?: anyString("totalFormatado", "valorTotal", "valorCobrar").toMoneyDouble() ?: 0.0
    }

    private fun DocumentSnapshot.machineFeeValue(): Double {
        return anyDouble(
            "taxaMaquininha",
            "valorTaxaMaquininha",
            "maquininhaTaxaValor",
            "financeiroEntrega.taxaMaquininha"
        ) ?: 0.0
    }

    fun formatCurrency(value: Double): String = NumberFormat.getCurrencyInstance(Locale("pt", "BR")).format(value)

    private fun sha256(value: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(value.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    private fun Date.formatHour(): String = SimpleDateFormat("HH:mm", Locale("pt", "BR")).format(this)

    private fun Date.formatHistoryLabel(): String {
        val now = Calendar.getInstance()
        val dateCal = Calendar.getInstance().apply { time = this@formatHistoryLabel }
        val hour = SimpleDateFormat("HH:mm", Locale("pt", "BR")).format(this)
        val sameYear = now.get(Calendar.YEAR) == dateCal.get(Calendar.YEAR)
        val sameDay = sameYear && now.get(Calendar.DAY_OF_YEAR) == dateCal.get(Calendar.DAY_OF_YEAR)
        val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
        val isYesterday = yesterday.get(Calendar.YEAR) == dateCal.get(Calendar.YEAR) &&
            yesterday.get(Calendar.DAY_OF_YEAR) == dateCal.get(Calendar.DAY_OF_YEAR)

        return when {
            sameDay -> "Hoje • $hour"
            isYesterday -> "Ontem • $hour"
            else -> "${SimpleDateFormat("dd/MM", Locale("pt", "BR")).format(this)} • $hour"
        }
    }

    private fun String.historyHumanLabel(): String {
        val s = upperOrTrim()
        return when {
            s.contains("REJEIT") -> "Recusada"
            s.contains("EXPIR") -> "Expirada"
            s in setOf("ACEITA", "ACEITO", "ACCEPTED", "A_CAMINHO_LOJA") -> "Aceita"
            s in setOf("COLETANDO", "EM_COLETA", "PICKUP") -> "Na coleta"
            s in setOf("EM_ROTA", "SAIU_ENTREGA", "A_CAMINHO_CLIENTE", "DELIVERING") -> "Em rota"
            s.contains("CONCL") || s.contains("ENTREG") || s.contains("FINALIZ") || s == "FINISHED" -> "Finalizada"
            else -> replace('_', ' ').lowercase(Locale.ROOT).replaceFirstChar { it.uppercase() }
        }
    }

    private fun String.toOperationalFirestoreStatus(): String {
        val s = upperOrTrim()
        return when {
            s == "accepted" || s == "ACEITA" || s == "ACEITO" || s == "A_CAMINHO_LOJA" -> "A_CAMINHO_LOJA"
            s == "pickup" || s == "COLETANDO" || s == "EM_COLETA" -> "COLETANDO"
            s == "delivering" || s == "EM_ROTA" || s == "SAIU_ENTREGA" || s == "A_CAMINHO_CLIENTE" -> "A_CAMINHO_CLIENTE"
            s == "finished" || s.contains("CONCL") || s.contains("ENTREG") || s.contains("FINALIZ") -> "ENTREGUE"
            else -> s
        }
    }

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
    val pixKey: String = "",
    val bankName: String = "",
    val needsPasswordSetup: Boolean = false,
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
    val plate: String = "",
    val pixKey: String = "",
    val bankName: String = ""
)

data class DriverStats(
    val totalToday: Double = 0.0,
    val totalWeek: Double = 0.0,
    val totalMonth: Double = 0.0,
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
    val neighborhood: String,
    val assignedDriverId: String,
    val targetDriverId: String,
    val broadcast: Boolean,
    val customerName: String,
    val orderCode: String,
    val stops: Int,
    val rejectedDriverIds: List<String>,
    val expiredDriverIds: List<String>,
    val pickupLat: Double? = null,
    val pickupLng: Double? = null,
    val dropoffLat: Double? = null,
    val dropoffLng: Double? = null,
    val clientTotalNumber: Double = 0.0,
    val amountToCollectNumber: Double = 0.0,
    val storeReturnNumber: Double = 0.0,
    val machineFeeNumber: Double = 0.0,
    val paymentMethod: String = "",
    val receivedBy: String = ""
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

private val OFFER_STATUSES = setOf(
    "BUSCANDO_ENTREGADOR",
    "OFERTA",
    "OFERTA_REAL",
    "OFERTA_ENTREGADOR",
    "TOCANDO",
    "CHAMANDO",
    "RADAR",
    "RADAR_ATIVO",
    "AGUARDANDO_ENTREGADOR",
    "BUSCANDO_MOTOBOY",
    "LIBERADO_PARA_ENTREGA",
    "LIBERADO_ENTREGA",
    "PRONTO_PARA_ENTREGA",
    "PRONTO_ENTREGA",
    "DISPATCH",
    "PENDENTE",
    "PENDING"
)
private val ROUTE_OFFER_STATUSES = OFFER_STATUSES
private val PEDIDO_OFFER_STATUSES = setOf(
    "BUSCANDO_ENTREGADOR",
    "OFERTA",
    "OFERTA_REAL",
    "OFERTA_ENTREGADOR",
    "TOCANDO",
    "CHAMANDO",
    "RADAR",
    "RADAR_ATIVO",
    "AGUARDANDO_ENTREGADOR",
    "BUSCANDO_MOTOBOY",
    "LIBERADO_PARA_ENTREGA",
    "LIBERADO_ENTREGA",
    "PRONTO_PARA_ENTREGA",
    "PRONTO_ENTREGA",
    "DISPATCH"
)
private val STORE_ACCEPTED_STATUSES = setOf(
    "ACEITO",
    "ACEITA",
    "APROVADO",
    "APROVADA",
    "CONFIRMADO",
    "CONFIRMADA",
    "EM_PREPARO",
    "PREPARANDO",
    "PRONTO",
    "PRONTO_PARA_ENTREGA",
    "LIBERADO",
    "LIBERADO_PARA_ENTREGA",
    "LIBERADO_ENTREGA"
)
private val STORE_NOT_ACCEPTED_STATUSES = setOf(
    "PENDENTE",
    "PENDING",
    "NOVO",
    "CRIADO",
    "ABERTO",
    "RECEBIDO",
    "AGUARDANDO",
    "AGUARDANDO_LOJA",
    "AGUARDANDO_ACEITE",
    "AGUARDANDO_CONFIRMACAO",
    "AGUARDANDO_CONFIRMAÇÃO",
    "CARRINHO"
)
private val ACCEPTED_STATUSES = setOf("ACEITA", "A_CAMINHO_LOJA", "AGUARDANDO_PRONTOS", "ACCEPTED", "ACEITO", "INDO_COLETA")
private val PICKUP_STATUSES = setOf("COLETANDO", "LIBERADA_PARA_SAIDA", "PICKUP", "EM_COLETA", "COLETADO")
private val DELIVERING_STATUSES = setOf("EM_ROTA", "SAIU_ENTREGA", "A_CAMINHO_CLIENTE", "ENTREGADOR_NO_LOCAL", "DELIVERING", "EM_ENTREGA", "CHEGOU_ENTREGA")
private val FINAL_HISTORY_STATUSES = setOf("CONCLUIDA", "ENTREGUE", "FINALIZADA", "FINISHED", "DELIVERED", "finished", "delivered")
private val APPROVED_STATUSES = setOf("APROVADO", "APPROVED", "LIBERADO", "ATIVO", "ACTIVE")
private val BLOCKED_STATUSES = setOf("REPROVADO", "BLOQUEADO", "BLOCKED", "SUSPENSO", "SUSPENDED", "CANCELADO")

private fun normalizeUiStatus(raw: String, collectionName: String = ""): String {
    val status = raw.upperOrTrim()
    val offerStatuses = if (collectionName == "pedidos") PEDIDO_OFFER_STATUSES else ROUTE_OFFER_STATUSES
    return when {
        status in offerStatuses -> "pending"
        status in ACCEPTED_STATUSES -> "accepted"
        status in PICKUP_STATUSES -> "pickup"
        status in DELIVERING_STATUSES -> "delivering"
        status in FINAL_HISTORY_STATUSES -> "finished"
        else -> raw.ifBlank { "" }
    }
}

private fun DocumentSnapshot.storeAcceptedForDelivery(): Boolean {
    val explicitRelease = anyBoolean(
        "liberadoParaEntregador",
        "liberadoParaMotoboy",
        "liberadoParaEntrega",
        "enviarParaEntregador",
        "ofertarEntregador",
        "ofertaLiberada",
        "rotaCriada",
        "possuiRota",
        "dispatchReleased"
    ) == true

    val acceptedFlag = anyBoolean(
        "aceitoPelaLoja",
        "lojaAceitou",
        "pedidoAceito",
        "confirmadoPelaLoja",
        "aceito",
        "confirmado"
    ) == true

    val acceptedTimestamp = anyTimestamp(
        "aceitoEm",
        "lojaAceitouEm",
        "confirmadoEm",
        "aprovadoEm",
        "preparoIniciadoEm"
    ) != null

    val storeStatus = anyString(
        "statusLoja",
        "statusPedido",
        "statusRestaurante",
        "statusOperacao",
        "statusOperação",
        "pedidoStatus",
        "cozinhaStatus",
        "status"
    ).upperOrTrim()

    if (storeStatus in STORE_NOT_ACCEPTED_STATUSES && !explicitRelease && !acceptedFlag && !acceptedTimestamp) {
        return false
    }

    return explicitRelease || acceptedFlag || acceptedTimestamp || storeStatus in STORE_ACCEPTED_STATUSES
}

private fun DocumentSnapshot.deliveryReleasedToDriver(collectionName: String): Boolean {
    if (collectionName != "pedidos") return true

    val deliveryStatus = anyString(
        "statusOfertaEntregador",
        "statusEntregador",
        "statusMotoboy",
        "statusEntrega",
        "statusRota",
        "logistica.status",
        "deliveryStatus",
        "ofertaStatus"
    ).upperOrTrim()

    val mainStatus = anyString("status").upperOrTrim()
    val explicitRelease = anyBoolean(
        "liberadoParaEntregador",
        "liberadoParaMotoboy",
        "liberadoParaEntrega",
        "enviarParaEntregador",
        "ofertarEntregador",
        "ofertaLiberada",
        "rotaCriada",
        "possuiRota",
        "dispatchReleased"
    ) == true

    val deliveryIsOffering = deliveryStatus in PEDIDO_OFFER_STATUSES
    val mainStatusIsDispatch = mainStatus in PEDIDO_OFFER_STATUSES

    // Compatibilidade operacional:
    // Se a Torre já colocou o pedido em BUSCANDO_ENTREGADOR/OFERTA/RADAR, o app deve tocar.
    // Isso não libera pedido cru PENDENTE/NOVO/RECEBIDO, porque esses status não entram aqui.
    if (explicitRelease || deliveryIsOffering || mainStatusIsDispatch) return true

    return storeAcceptedForDelivery() && (explicitRelease || deliveryIsOffering || mainStatusIsDispatch)
}

private fun DocumentSnapshot.driverPayoutValue(): Double {
    return anyDouble(
        "repasseFrota",
        "repassePiloto",
        "valorRepasseFrota",
        "valorRepassePiloto",
        "financeiroEntrega.repasseFrota",
        "financeiroEntrega.repassePiloto",
        "valores.repasseFrota",
        "valores.repassePiloto",
        "logistica.repasseFrota",
        "logistica.repassePiloto",
        "calculo.valorTotalMotoboy",
        "valorTotalMotoboy",
        "valorMotoboy",
        "valorEntregador",
        "valorRepasseMotoboy",
        "valorCorrida",
        "valorRota"
    ) ?: anyString("repasse", "valorRepasse", "valorMotoboyFormatado").toMoneyDouble() ?: 0.0
}

private fun DocumentSnapshot.clientTotalValue(): Double {
    return anyDouble(
        "valorTotalPedido",
        "totalPedido",
        "total",
        "valorTotalCliente",
        "pedidoTotal",
        "valorCobrarCliente"
    ) ?: anyString("totalFormatado", "valorTotal", "valorCobrar").toMoneyDouble() ?: 0.0
}

private fun DocumentSnapshot.machineFeeValue(): Double {
    return anyDouble(
        "taxaMaquininha",
        "valorTaxaMaquininha",
        "maquininhaTaxaValor",
        "financeiroEntrega.taxaMaquininha"
    ) ?: 0.0
}

private fun DocumentSnapshot.toDriverRide(collectionName: String): DriverRide? {
    if (!deliveryReleasedToDriver(collectionName)) return null

    val rawStatus = if (collectionName == "pedidos") {
        val main = anyString("status").upperOrTrim()
        if (main in ACCEPTED_STATUSES || main in PICKUP_STATUSES || main in DELIVERING_STATUSES || main in FINAL_HISTORY_STATUSES) {
            anyString("status")
        } else {
            anyString(
                "statusOfertaEntregador",
                "statusEntregador",
                "statusMotoboy",
                "statusEntrega",
                "statusRota",
                "logistica.status",
                "deliveryStatus",
                "ofertaStatus",
                "status"
            )
        }
    } else {
        anyString("status", "statusEntregador", "statusMotoboy", "statusOfertaEntregador")
    }.ifBlank { "" }
    val number = driverPayoutValue()
    val clientTotal = clientTotalValue()
    val machineFee = machineFeeValue()
    val paymentMethod = anyString("formaPagamento", "pagamento", "paymentMethod", "metodoPagamento").ifBlank { "Não informado" }
    val receivedBy = anyString("recebidoPor", "quemRecebe", "recebedor", "paymentReceiver").ifBlank { "Loja/App" }
    val amountToCollect = anyDouble("valorReceberCliente", "valorCobrarCliente", "trocoValorCobrar", "cobrarDoCliente") ?: clientTotal
    val storeReturn = if (receivedBy.upperOrTrim() in setOf("ENTREGADOR", "MOTOBOY", "DRIVER")) (amountToCollect - machineFee - number).coerceAtLeast(0.0) else 0.0
    val assigned = anyString(
        "entregadorId", "entregadorUid", "motoboyId", "motoboyUid", "uidEntregador",
        "driverId", "assignedDriverId", "pilotoId", "courierId"
    )
    val target = anyString(
        "entregadorAtualOferta", "motoboyAtualOferta", "targetDriverId", "ofertaParaEntregadorId",
        "ofertaDriverId", "driverAtualOferta", "pilotoAtualOferta", "entregadorSelecionadoId"
    )
    val rejected = anyStringList("rejeitados", "rejeitadoPor", "rejeitadosIds", "entregadoresRejeitaram", "rejectedDriverIds")
    val expired = anyStringList("expiredDriverIds", "expiradoPara", "expirados")
    val pickup = anyString("lojaEndereco", "pickup", "pickupAddress", "enderecoLoja", "nomeLoja", "lojaNome")
        .ifBlank { "Rodrigues Acai e Cia" }
    val dropoff = anyAddressString()
    val km = anyDouble("kmTotal", "distanciaKm", "distanciaTotal", "distancia", "calculo.kmTotalEstimado", "calculo.kmTotal", "calculo.distanciaKm") ?: 0.0
    val minutes = anyDouble("tempoTotalMin", "tempoMin", "tempoEstimado", "tempo", "calculo.tempoTotalMin", "calculo.tempoMin") ?: 0.0
    val pickupLat = anyCoordinate("latLoja", "lojaLat", "latitudeLoja", "pickupLat", "pickupLatitude", "coletaLat", "latColeta", "origemLat")
    val pickupLng = anyCoordinate("lngLoja", "lojaLng", "longitudeLoja", "pickupLng", "pickupLongitude", "coletaLng", "lngColeta", "origemLng", "lonLoja")
    val dropoffLat = anyCoordinate("latEntrega", "entregaLat", "clienteLat", "dropoffLat", "dropoffLatitude", "destinationLat", "destinoLat") ?: nestedCoordinate("endereco", "lat", "latitude")
    val dropoffLng = anyCoordinate("lngEntrega", "entregaLng", "clienteLng", "dropoffLng", "dropoffLongitude", "destinationLng", "destinoLng", "lonEntrega") ?: nestedCoordinate("endereco", "lng", "lon", "longitude")

    return DriverRide(
        id = id,
        collectionName = collectionName,
        status = normalizeUiStatus(rawStatus, collectionName),
        rawStatus = rawStatus,
        value = DriverRepository.formatCurrency(number),
        valueNumber = number,
        distance = if (km > 0.0) "${String.format(Locale("pt", "BR"), "%.1f", km)} km" else anyString("distance").ifBlank { "-- km" },
        duration = if (minutes > 0.0) "${minutes.toInt()} min" else anyString("duration", "estimatedTime").ifBlank { "-- min" },
        pickup = pickup,
        dropoff = dropoff,
        neighborhood = anyString("bairro", "bairroEntrega", "regiao", "neighborhood").ifBlank { "Bairro não informado" },
        assignedDriverId = assigned,
        targetDriverId = target,
        broadcast = anyBoolean("broadcast", "paraTodos", "ofertaParaTodos") ?: false,
        customerName = anyString("customerName", "clientName", "clienteNome", "nomeCliente", "nome").ifBlank { "Cliente" },
        orderCode = anyString("orderCode", "orderId", "numeroPedido", "codigoPedido").ifBlank { id.takeLast(6).uppercase() },
        stops = (anyDouble("stops", "paradas", "quantidadePedidos") ?: 2.0).toInt().coerceAtLeast(1),
        rejectedDriverIds = rejected,
        expiredDriverIds = expired,
        pickupLat = pickupLat,
        pickupLng = pickupLng,
        dropoffLat = dropoffLat,
        dropoffLng = dropoffLng,
        clientTotalNumber = clientTotal,
        amountToCollectNumber = amountToCollect,
        storeReturnNumber = storeReturn,
        machineFeeNumber = machineFee,
        paymentMethod = paymentMethod,
        receivedBy = receivedBy
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

private fun DocumentSnapshot.anyString(vararg keys: String): String {
    for (key in keys) {
        val value = getDeep(key)
        if (value != null) return value.toString().trim()
    }
    return ""
}

private fun DocumentSnapshot.anyDouble(vararg keys: String): Double? {
    for (key in keys) {
        val value = getDeep(key)
        when (value) {
            is Number -> return value.toDouble()
            is String -> value.toMoneyDouble()?.let { return it }
        }
    }
    return null
}

private fun DocumentSnapshot.anyCoordinate(vararg keys: String): Double? {
    for (key in keys) {
        val value = getDeep(key)
        when (value) {
            is Number -> return value.toDouble()
            is String -> value.replace(',', '.').trim().toDoubleOrNull()?.let { return it }
        }
    }
    return null
}

private fun DocumentSnapshot.nestedCoordinate(mapKey: String, vararg keys: String): Double? {
    val value = get(mapKey)
    if (value !is Map<*, *>) return null
    for (key in keys) {
        val raw = value[key]
        when (raw) {
            is Number -> return raw.toDouble()
            is String -> raw.replace(',', '.').trim().toDoubleOrNull()?.let { return it }
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
