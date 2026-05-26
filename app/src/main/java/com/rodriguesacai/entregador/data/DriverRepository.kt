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
import java.time.Instant

object DriverRepository {
    private const val APP_VERSION = "6.18.0"
    private const val PREFS = "driver_session"
    private const val KEY_ID = "driver_id"
    private const val KEY_NAME = "driver_name"
    private const val KEY_PHONE = "driver_phone"
    private const val KEY_PHOTO = "driver_photo"
    private const val KEY_COLLECTION = "driver_collection"
    private const val KEY_PIX = "driver_pix"
    private const val KEY_BANK = "driver_bank"
    private const val KEY_CITY = "driver_city"
    private const val KEY_VEHICLE = "driver_vehicle"
    private const val KEY_NEEDS_PASSWORD = "driver_needs_password"
    private const val KEY_ACTIVE_MISSION = "driver_active_mission"
    private const val KEY_ACTIVE_ROUTE = "driver_active_route"
    private const val KEY_ACTIVE_ORDER = "driver_active_order"

    private const val OP_PREFS = "driver_operational_preferences"

    private const val REAL_DRIVER_COLLECTION = "entregadores"
    private val DRIVER_COLLECTIONS = listOf("entregadores", "drivers", "motoboys", "deliveryDrivers", "couriers")
    private val MISSION_COLLECTIONS = listOf("rotas_entrega", "pedidos", "rides")
    private val CAROUSEL_COLLECTIONS = listOf("app_carousel_banners", "carrosselApp", "bannersApp", "appBanners", "bannersEntregador", "carrossel_entregador", "entregadorBanners")
    private val NOTICE_COLLECTIONS = listOf("app_notifications", "notificacoesEntregador", "avisosEntregador", "appAvisos", "operacaoAvisos")
    private val MACHINE_COLLECTIONS = listOf("maquininhas", "maquininhasEntrega", "cardMachines")

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
            city = prefs.getString(KEY_CITY, null).orEmpty(),
            vehicle = prefs.getString(KEY_VEHICLE, null).orEmpty(),
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
            "appVersion" to APP_VERSION,
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
                "appVersion" to APP_VERSION
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
                "appVersion" to APP_VERSION
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
                "appVersion" to APP_VERSION,
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
            .putString(KEY_CITY, profile.city)
            .putString(KEY_VEHICLE, profile.vehicle)
            .putBoolean(KEY_NEEDS_PASSWORD, profile.needsPasswordSetup)
            .apply()

        db.collection(profile.collectionName).document(profile.id).set(
            mapOf(
                "ultimoLoginEm" to Timestamp.now(),
                "lastLoginAt" to Timestamp.now(),
                "platform" to "android_native",
                "appVersion" to APP_VERSION
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
            "statusOperacional" to if (online) "DISPONIVEL" else "INDISPONIVEL",
            "aceitaNovasOfertas" to online,
            "atualizadoEm" to Timestamp.now(),
            "updatedAt" to Timestamp.now(),
            "platform" to "android_native",
            "appVersion" to APP_VERSION
        )
        db.collection(profile.collectionName).document(profile.id).set(payload, SetOptions.merge())
        if (online) saveMessagingToken(context)
    }



    fun loadOperationalPreferences(context: Context): DriverOperationalPreferences {
        val prefs = context.getSharedPreferences(OP_PREFS, Context.MODE_PRIVATE)
        return DriverOperationalPreferences(
            hasMachine = prefs.getBoolean("hasMachine", false),
            acceptsDebit = prefs.getBoolean("acceptsDebit", false),
            acceptsCredit = prefs.getBoolean("acceptsCredit", false),
            acceptsInstallment = prefs.getBoolean("acceptsInstallment", false),
            acceptsTicket = prefs.getBoolean("acceptsTicket", false),
            hasCashChange = prefs.getBoolean("hasCashChange", false),
            changeAvailableNumber = prefs.getFloat("changeAvailableNumber", 0f).toDouble(),
            onlyOnlinePaid = prefs.getBoolean("onlyOnlinePaid", false),
            blockCashAtNight = prefs.getBoolean("blockCashAtNight", false),
            blockMachineAtNight = prefs.getBoolean("blockMachineAtNight", false),
            nightStartHour = prefs.getInt("nightStartHour", 0),
            nightEndHour = prefs.getInt("nightEndHour", 6)
        )
    }

    fun saveOperationalPreferences(
        context: Context,
        preferences: DriverOperationalPreferences,
        onDone: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        val profile = currentSession(context)
        context.getSharedPreferences(OP_PREFS, Context.MODE_PRIVATE).edit()
            .putBoolean("hasMachine", preferences.hasMachine)
            .putBoolean("acceptsDebit", preferences.acceptsDebit)
            .putBoolean("acceptsCredit", preferences.acceptsCredit)
            .putBoolean("acceptsInstallment", preferences.acceptsInstallment)
            .putBoolean("acceptsTicket", preferences.acceptsTicket)
            .putBoolean("hasCashChange", preferences.hasCashChange)
            .putFloat("changeAvailableNumber", preferences.changeAvailableNumber.toFloat())
            .putBoolean("onlyOnlinePaid", preferences.onlyOnlinePaid)
            .putBoolean("blockCashAtNight", preferences.blockCashAtNight)
            .putBoolean("blockMachineAtNight", preferences.blockMachineAtNight)
            .putInt("nightStartHour", preferences.nightStartHour)
            .putInt("nightEndHour", preferences.nightEndHour)
            .apply()

        if (profile == null) {
            onDone()
            return
        }
        val payload = mapOf(
            "operacao" to mapOf(
                "temMaquininha" to preferences.hasMachine,
                "aceitaDebito" to preferences.acceptsDebit,
                "aceitaCredito" to preferences.acceptsCredit,
                "aceitaParcelado" to preferences.acceptsInstallment,
                "aceitaTicket" to preferences.acceptsTicket,
                "temTroco" to preferences.hasCashChange,
                "trocoDisponivel" to preferences.changeAvailableNumber,
                "somentePagoOnline" to preferences.onlyOnlinePaid,
                "bloquearDinheiroNoite" to preferences.blockCashAtNight,
                "bloquearMaquininhaNoite" to preferences.blockMachineAtNight,
                "horaInicioRestricao" to preferences.nightStartHour,
                "horaFimRestricao" to preferences.nightEndHour,
                "atualizadoEm" to Timestamp.now()
            ),
            "preferenciasOperacionais" to mapOf(
                "temMaquininha" to preferences.hasMachine,
                "temTroco" to preferences.hasCashChange,
                "trocoDisponivel" to preferences.changeAvailableNumber,
                "somentePagoOnline" to preferences.onlyOnlinePaid,
                "janelaBloqueioInicio" to preferences.nightStartHour,
                "janelaBloqueioFim" to preferences.nightEndHour
            ),
            "atualizadoEm" to Timestamp.now(),
            "updatedAt" to Timestamp.now()
        )
        db.collection(profile.collectionName).document(profile.id).set(payload, SetOptions.merge())
            .addOnSuccessListener { onDone() }
            .addOnFailureListener { onError(it.message ?: "Falha ao salvar preferências de operação.") }
    }

    fun rememberActiveRide(context: Context, ride: DriverRide) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putString(KEY_ACTIVE_MISSION, ride.id)
            .putString(KEY_ACTIVE_ROUTE, ride.routeId.ifBlank { if (ride.collectionName == "rotas_entrega") ride.id else "" })
            .putString(KEY_ACTIVE_ORDER, if (ride.collectionName == "pedidos") ride.id else "")
            .apply()
    }

    fun forgetActiveRide(context: Context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .remove(KEY_ACTIVE_MISSION)
            .remove(KEY_ACTIVE_ROUTE)
            .remove(KEY_ACTIVE_ORDER)
            .apply()
    }

    fun hasLocalActiveMission(context: Context): Boolean {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_ACTIVE_MISSION, "").orEmpty().isNotBlank()
    }

    fun localActiveRouteId(context: Context): String {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_ACTIVE_ROUTE, "").orEmpty()
    }

    fun shouldSuppressUrgentPush(context: Context, data: Map<String, String>): Boolean {
        val active = hasLocalActiveMission(context)
        if (!active) return false
        val type = (data["tipoOferta"] ?: data["offerType"] ?: data["type"] ?: data["event"] ?: "").upperOrTrim()
        val routeId = data["rotaId"] ?: data["routeId"] ?: data["rotaAtualId"] ?: ""
        val currentRoute = localActiveRouteId(context)
        val isRouteAddition = type.contains("ADICAO") || type.contains("ADIÇÃO") || type.contains("ADD_ROUTE") || type.contains("ROTA_ADICIONAL") || type.contains("PEDIDO_ADICIONADO")
        return !(isRouteAddition && currentRoute.isNotBlank() && routeId == currentRoute)
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


    fun listenAppCarousel(
        onBanners: (List<AppCarouselBanner>) -> Unit,
        onError: (String) -> Unit = {}
    ): ListenerRegistration {
        val state = mutableMapOf<String, List<AppCarouselBanner>>()
        fun emit() {
            val now = System.currentTimeMillis()
            val banners = state.values.flatten()
                .distinctBy { "${it.collectionName}:${it.id}" }
                .filter { it.isVisible(now) }
                .sortedWith(compareBy<AppCarouselBanner> { it.order }.thenBy { it.title.lowercase(Locale.ROOT) })
                .take(12)
            onBanners(banners)
        }

        val registrations = CAROUSEL_COLLECTIONS.map { collectionName ->
            db.collection(collectionName).limit(30).addSnapshotListener { snap, err ->
                if (err != null) {
                    onError(err.message ?: "Erro ao ouvir carrossel do app.")
                    return@addSnapshotListener
                }
                state[collectionName] = snap?.documents.orEmpty().mapNotNull { doc ->
                    runCatching { doc.toAppCarouselBanner(collectionName) }.getOrNull()
                }
                emit()
            }
        }
        return CompositeListenerRegistration(registrations)
    }

    fun listenAppNotifications(
        context: Context,
        onNotices: (List<AppNotice>) -> Unit,
        onError: (String) -> Unit = {}
    ): ListenerRegistration? {
        val profile = currentSession(context) ?: return null
        val state = mutableMapOf<String, List<AppNotice>>()
        fun emit() {
            val now = System.currentTimeMillis()
            val notices = state.values.flatten()
                .distinctBy { "${it.collectionName}:${it.id}" }
                .filter { it.isVisible(now) && it.matchesDriver(profile.id) }
                .sortedWith(compareByDescending<AppNotice> { it.createdAtMillis }.thenBy { it.order })
                .take(80)
            onNotices(notices)
        }
        val regs = NOTICE_COLLECTIONS.map { collectionName ->
            db.collection(collectionName).limit(120).addSnapshotListener { snap, err ->
                if (err != null) {
                    onError(err.message ?: "Erro ao ouvir notificações do app.")
                    return@addSnapshotListener
                }
                state[collectionName] = snap?.documents.orEmpty().mapNotNull { doc ->
                    runCatching { doc.toAppNotice(collectionName) }.getOrNull()
                }
                emit()
            }
        }
        return CompositeListenerRegistration(regs)
    }

    fun listenPendingRide(
        context: Context,
        onRide: (DriverRide?) -> Unit,
        onError: (String) -> Unit
    ): ListenerRegistration? {
        val profile = currentSession(context) ?: return null
        val state = mutableMapOf<String, List<DriverRide>>()
        fun emit() {
            val preferences = loadOperationalPreferences(context)
            val hasActive = hasLocalActiveMission(context)
            val activeRouteId = localActiveRouteId(context)
            val ride = state.values.flatten()
                .distinctBy { "${it.collectionName}:${it.id}" }
                .filter { it.status == "pending" }
                .filter { it.canBeOfferedTo(profile.id, preferences, hasActive, activeRouteId) }
                .sortedWith(compareBy<DriverRide> { it.isRouteAddition.not() }.thenBy { it.offerExpiresAtMillis.takeIf { exp -> exp > 0L } ?: Long.MAX_VALUE })
                .firstOrNull()
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
                            .mapNotNull { doc -> runCatching { doc.toDriverRide(collectionName) }.getOrNull() }
                            .filter { ride -> ride.status == "pending" && ride.canBeOfferedTo(profile.id, loadOperationalPreferences(context), hasLocalActiveMission(context), localActiveRouteId(context)) }
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
                .firstOrNull { it.status in listOf("accepted", "pickup", "delivering", "arrived_client", "occurrence") }
            if (active != null) {
                rememberActiveRide(context, active)
            } else {
                if (hasLocalActiveMission(context)) {
                    releaseStaleMission(context, profile, "MISSAO_ENCERRADA_OU_CANCELADA")
                } else {
                    forgetActiveRide(context)
                }
            }
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
                            .mapNotNull { doc -> runCatching { doc.toDriverRide(collectionName) }.getOrNull() }
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
                    .filter { doc -> runCatching { doc.matchesDriverId(profile.id) }.getOrDefault(false) }
                    .mapNotNull { doc ->
                        runCatching {
                            val action = doc.anyString("statusAtual", "tipo", "status", "action", "titulo").ifBlank { "registro" }
                            val rawRideId = doc.anyString("rotaId", "rideId", "pedidoId", "missaoId", "corridaId").ifBlank { doc.id }
                            val displayCode = doc.anyString("codigoPedido", "numeroPedido", "orderCode", "pedidoCodigo")
                                .ifBlank { rawRideId.takeLast(4).uppercase(Locale.ROOT) }
                            val date = doc.anyTimestamp("statusAtualizadoEm", "atualizadoEm", "updatedAt", "criadoEm", "createdAt")?.toDate()
                            val hasValue = doc.hasAnyValue(
                                "valorRota", "valor", "valueNumber", "repasseEntregador", "valorRepasseMotoboy",
                                "taxaEntrega", "valorEntrega", "preco", "total", "valorTotal"
                            )
                            DriverHistory(
                                id = doc.id,
                                rideId = displayCode,
                                action = action,
                                value = if (hasValue) formatCurrency(valueNumberFromDoc(doc)) else "",
                                createdAtMillis = date?.time ?: 0L,
                                createdLabel = date?.formatHistoryLabel().orEmpty().ifBlank { "Data não informada" },
                                pickup = doc.anyString("loja.nome", "storeName", "nomeLoja", "coleta", "pickup", "pickupAddress", "enderecoColeta", "origem"),
                                dropoff = doc.anyString("cliente.endereco", "enderecoEntrega", "deliveryAddress", "dropoff", "dropoffAddress", "destino"),
                                neighborhood = doc.anyString("bairro", "bairroEntrega", "cliente.bairro", "deliveryNeighborhood", "regiaoEntrega"),
                                distance = doc.anyString("distancia", "distance", "distanciaKm", "km"),
                                duration = doc.anyString("tempo", "duration", "tempoEstimado", "estimatedDuration"),
                                reason = doc.anyString("motivo", "reason", "motivoRecusa", "motivoOcorrencia", "observacao", "descricao"),
                                paymentMethod = doc.anyString("pagamento", "paymentMethod", "formaPagamento", "payment")
                            )
                        }.getOrNull()
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

        var historyBase = DriverStats()
        var walletDocStats = DriverStats()
        var payoutRows: List<DriverPayout> = emptyList()

        fun emit() {
            val walletAvailable = walletDocStats.saldoDisponivel ?: historyBase.totalToday
            val walletPending = walletDocStats.saldoPendente ?: 0.0
            val walletTotal = walletDocStats.totalAReceber ?: (walletAvailable + walletPending)
            onStats(
                historyBase.copy(
                    saldoDisponivel = walletAvailable,
                    saldoPendente = walletPending,
                    totalAReceber = walletTotal,
                    recebidoPeloEntregador = walletDocStats.recebidoPeloEntregador,
                    taxaMotoboy = walletDocStats.taxaMotoboy,
                    taxasMaquininha = walletDocStats.taxasMaquininha,
                    valorARepassar = walletDocStats.valorARepassar,
                    valorAReceber = walletDocStats.valorAReceber,
                    dinheiroRecebido = walletDocStats.dinheiroRecebido,
                    cartaoRecebido = walletDocStats.cartaoRecebido,
                    pixRecebido = walletDocStats.pixRecebido,
                    proximoRepasseLabel = walletDocStats.proximoRepasseLabel,
                    proximoRepasseDescricao = walletDocStats.proximoRepasseDescricao,
                    pixKey = walletDocStats.pixKey.ifBlank { profile.pixKey },
                    pixVerificada = walletDocStats.pixVerificada,
                    bankName = walletDocStats.bankName.ifBlank { profile.bankName },
                    payoutRows = payoutRows
                )
            )
        }

        val historyRegistration = db.collection("historicoEntregador")
            .limit(200)
            .addSnapshotListener { snap, err ->
                if (err != null) {
                    onError(err.message ?: "Erro ao ouvir ganhos.")
                    return@addSnapshotListener
                }
                val allForDriver = snap?.documents.orEmpty()
                    .filter { doc -> runCatching { doc.matchesDriverId(profile.id) }.getOrDefault(false) }
                val todayDocs = allForDriver.filter { doc ->
                    val millis = doc.anyTimestamp("statusAtualizadoEm", "atualizadoEm", "updatedAt", "criadoEm", "createdAt")?.toDate()?.time
                    millis != null && millis.isTodayMillis()
                }
                val finishedToday = todayDocs.filter { doc ->
                    runCatching { doc.anyString("tipo", "status", "action", "statusAtual").upperOrTrim() in FINAL_HISTORY_STATUSES }.getOrDefault(false)
                }
                val finishedWeek = allForDriver.filter { doc ->
                    val statusOk = runCatching { doc.anyString("tipo", "status", "action", "statusAtual").upperOrTrim() in FINAL_HISTORY_STATUSES }.getOrDefault(false)
                    val millis = doc.anyTimestamp("statusAtualizadoEm", "atualizadoEm", "updatedAt", "criadoEm", "createdAt")?.toDate()?.time
                    statusOk && millis != null && millis.isWithinLastDays(7)
                }
                val finishedMonth = allForDriver.filter { doc ->
                    val statusOk = runCatching { doc.anyString("tipo", "status", "action", "statusAtual").upperOrTrim() in FINAL_HISTORY_STATUSES }.getOrDefault(false)
                    val millis = doc.anyTimestamp("statusAtualizadoEm", "atualizadoEm", "updatedAt", "criadoEm", "createdAt")?.toDate()?.time
                    statusOk && millis != null && millis.isWithinLastDays(31)
                }
                historyBase = DriverStats(
                    totalToday = finishedToday.sumOf { doc -> runCatching { valueNumberFromDoc(doc) }.getOrDefault(0.0) },
                    totalWeek = finishedWeek.sumOf { doc -> runCatching { valueNumberFromDoc(doc) }.getOrDefault(0.0) },
                    totalMonth = finishedMonth.sumOf { doc -> runCatching { valueNumberFromDoc(doc) }.getOrDefault(0.0) },
                    finishedCount = finishedToday.size,
                    ridesTodayCount = todayDocs.size,
                    finishedTodayCount = finishedToday.size,
                    score = 0
                )
                emit()
            }

        val driverWalletRegistration = db.collection(profile.collectionName).document(profile.id)
            .addSnapshotListener { doc, err ->
                if (err != null) {
                    onError(err.message ?: "Erro ao ouvir carteira do entregador.")
                    return@addSnapshotListener
                }
                walletDocStats = doc?.toWalletStats() ?: DriverStats()
                emit()
            }

        val payoutByUid = db.collection("repassesEntregadores")
            .whereEqualTo("entregadorUid", profile.id)
            .limit(50)
            .addSnapshotListener { snap, err ->
                if (err != null) {
                    onError(err.message ?: "Erro ao ouvir repasses do gestor.")
                    return@addSnapshotListener
                }
                val rows = snap?.documents.orEmpty()
                    .mapNotNull { doc -> runCatching { doc.toDriverPayout(profile.id) }.getOrNull() }
                payoutRows = (payoutRows + rows)
                    .distinctBy { it.id }
                    .sortedByDescending { it.createdAtMillis }
                    .take(8)
                emit()
            }

        val payoutById = db.collection("repassesEntregadores")
            .whereEqualTo("entregadorId", profile.id)
            .limit(50)
            .addSnapshotListener { snap, err ->
                if (err != null) {
                    onError(err.message ?: "Erro ao ouvir repasses do gestor.")
                    return@addSnapshotListener
                }
                val rows = snap?.documents.orEmpty()
                    .mapNotNull { doc -> runCatching { doc.toDriverPayout(profile.id) }.getOrNull() }
                payoutRows = (payoutRows + rows)
                    .distinctBy { it.id }
                    .sortedByDescending { it.createdAtMillis }
                    .take(8)
                emit()
            }

        return CompositeListenerRegistration(listOf(historyRegistration, driverWalletRegistration, payoutByUid, payoutById))
    }


    fun listenDriverProfile(
        context: Context,
        onProfile: (DriverProfile) -> Unit,
        onError: (String) -> Unit = {}
    ): ListenerRegistration? {
        val session = currentSession(context) ?: return null
        return db.collection(session.collectionName).document(session.id).addSnapshotListener { doc, err ->
            if (err != null) {
                onError(err.message ?: "Erro ao ouvir perfil do entregador.")
                return@addSnapshotListener
            }
            if (doc != null && doc.exists()) {
                onProfile(doc.toProfile(session.collectionName))
            }
        }
    }

    fun listenMachineOptions(
        onMachines: (List<PaymentMachine>) -> Unit,
        onError: (String) -> Unit = {}
    ): ListenerRegistration {
        val state = mutableMapOf<String, List<PaymentMachine>>()
        fun emit() {
            val machines = state.values.flatten()
                .distinctBy { "${it.collectionName}:${it.id}" }
                .filter { it.active }
                .sortedWith(compareBy<PaymentMachine> { it.order }.thenBy { it.name.lowercase(Locale.ROOT) })
            onMachines(machines)
        }
        val regs = MACHINE_COLLECTIONS.map { collectionName ->
            db.collection(collectionName).limit(120).addSnapshotListener { snap, err ->
                if (err != null) {
                    onError(err.message ?: "Erro ao ouvir maquininhas.")
                    return@addSnapshotListener
                }
                state[collectionName] = snap?.documents.orEmpty().mapNotNull { doc ->
                    runCatching { doc.toPaymentMachine(collectionName) }.getOrNull()
                }
                emit()
            }
        }
        return CompositeListenerRegistration(regs)
    }

    fun listenAppRuntimeConfig(
        onConfig: (AppRuntimeConfig) -> Unit,
        onError: (String) -> Unit = {}
    ): ListenerRegistration {
        val docs = listOf(
            db.collection("app_config").document("entregador"),
            db.collection("app_config").document("global"),
            db.collection("app_config").document("manutencao")
        )
        val state = mutableMapOf<String, AppRuntimeConfig>()
        fun emit() {
            val configs = state.values.toList()
            val maintenance = configs.firstOrNull { it.maintenanceActive }
            val chosen = maintenance ?: configs.firstOrNull { it.forceUpdate || it.globalAlert.isNotBlank() } ?: configs.firstOrNull() ?: AppRuntimeConfig()
            onConfig(chosen)
        }
        val regs = docs.map { ref ->
            ref.addSnapshotListener { doc, err ->
                if (err != null) {
                    onError(err.message ?: "Erro ao ouvir configurações do app.")
                    return@addSnapshotListener
                }
                state[ref.path] = if (doc != null && doc.exists()) doc.toAppRuntimeConfig(ref.id) else AppRuntimeConfig(id = ref.id)
                emit()
            }
        }
        return CompositeListenerRegistration(regs)
    }

    fun markNotificationRead(
        context: Context,
        notice: AppNotice,
        onDone: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        val profile = currentSession(context)
        if (profile == null) {
            onError("Faça login para marcar notificação como lida.")
            return
        }
        val now = Timestamp.now()
        val readId = "${profile.id}_${notice.collectionName}_${notice.id}".replace(Regex("[^A-Za-z0-9_-]"), "_").take(180)
        db.collection("notificacoesEntregadorLidas").document(readId).set(
            mapOf(
                "entregadorUid" to profile.id,
                "entregadorId" to profile.id,
                "notificationId" to notice.id,
                "notificationCollection" to notice.collectionName,
                "title" to notice.title,
                "category" to notice.category,
                "lido" to true,
                "read" to true,
                "lidoEm" to now,
                "readAt" to now,
                "origem" to "APP_ENTREGADOR",
                "appVersion" to APP_VERSION
            ),
            SetOptions.merge()
        ).addOnSuccessListener { onDone() }
            .addOnFailureListener { onError(it.message ?: "Falha ao marcar notificação como lida.") }
    }

    fun calculateSettlementPreview(input: PaymentSettlementInput, machine: PaymentMachine? = null): PaymentSettlementPreview {
        val gross = input.orderTotal.coerceAtLeast(0.0)
        val driverFee = input.driverFee.coerceAtLeast(0.0)
        val method = input.paymentMethod.upperOrTrim()
        val transaction = input.transactionType.upperOrTrim()
        val percent = when {
            method.contains("MAQUIN") || method.contains("CART") || transaction.contains("DEBIT") || transaction.contains("DÉBIT") -> {
                when {
                    transaction.contains("DEBIT") || transaction.contains("DÉBIT") -> machine?.debitFeePercent ?: input.manualFeePercent
                    transaction.contains("PARCEL") -> machine?.installmentFeePercent ?: input.manualFeePercent
                    transaction.contains("TICKET") -> machine?.ticketFeePercent ?: input.manualFeePercent
                    else -> machine?.creditFeePercent ?: input.manualFeePercent
                }
            }
            else -> 0.0
        }.coerceAtLeast(0.0)
        val machineFee = if (gross > 0.0) gross * (percent / 100.0) else 0.0
        val liquid = (gross - machineFee).coerceAtLeast(0.0)
        val receivedByDriver = input.receivedByDriver || method.contains("DINHEIRO") || method.contains("MAQUIN") || method.contains("CART") || input.receivedBy.upperOrTrim() in setOf("ENTREGADOR", "MOTOBOY", "DRIVER")
        val amountToRepay = if (receivedByDriver) (liquid - driverFee).coerceAtLeast(0.0) else 0.0
        val amountToReceive = if (receivedByDriver) 0.0 else driverFee
        val cash = if (method.contains("DINHEIRO")) gross else 0.0
        val card = if (method.contains("MAQUIN") || method.contains("CART") || transaction.isNotBlank()) gross else 0.0
        val pix = if (method.contains("PIX")) gross else 0.0
        return PaymentSettlementPreview(
            grossAmount = gross,
            driverFee = driverFee,
            machineFee = machineFee,
            machineFeePercent = percent,
            liquidAmount = liquid,
            amountToRepay = amountToRepay,
            amountToReceive = amountToReceive,
            cashAmount = cash,
            cardAmount = card,
            pixAmount = pix,
            status = "AGUARDANDO_CONFERENCIA"
        )
    }

    fun savePaymentSettlementForRide(
        context: Context,
        rideId: String,
        input: PaymentSettlementInput,
        onDone: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        val profile = currentSession(context)
        if (profile == null) {
            onError("Faça login antes de registrar o acerto.")
            return
        }
        fun persist(machine: PaymentMachine?) {
            findMissionDocument(rideId, onFound = { doc ->
                val preview = calculateSettlementPreview(input, machine)
                val now = Timestamp.now()
                val acerto = linkedMapOf<String, Any?>(
                    "status" to preview.status,
                    "origem" to "APP_ENTREGADOR",
                    "entregadorUid" to profile.id,
                    "entregadorId" to profile.id,
                    "entregadorNome" to profile.name,
                    "pedidoId" to rideId,
                    "corridaId" to rideId,
                    "formaPagamento" to input.paymentMethod,
                    "tipoTransacao" to input.transactionType,
                    "maquininhaId" to input.machineId,
                    "maquininhaNome" to (machine?.name ?: input.machineName),
                    "recebidoPor" to if (preview.amountToRepay > 0.0) "ENTREGADOR" else input.receivedBy.ifBlank { "SISTEMA" },
                    "valorBruto" to preview.grossAmount,
                    "valorLiquido" to preview.liquidAmount,
                    "recebidoPeloEntregador" to if (preview.amountToRepay > 0.0) preview.grossAmount else 0.0,
                    "taxaMotoboy" to preview.driverFee,
                    "taxasMaquininha" to preview.machineFee,
                    "taxaMaquininhaPercentual" to preview.machineFeePercent,
                    "valorARepassar" to preview.amountToRepay,
                    "valorAReceber" to preview.amountToReceive,
                    "dinheiro" to preview.cashAmount,
                    "cartao" to preview.cardAmount,
                    "pix" to preview.pixAmount,
                    "observacao" to input.note,
                    "precisaConferencia" to true,
                    "criadoEm" to now,
                    "createdAt" to now,
                    "atualizadoEm" to now,
                    "updatedAt" to now,
                    "appVersion" to APP_VERSION
                )
                val missionPayload = mapOf(
                    "pagamentoRecebidoPeloEntregador" to acerto,
                    "financeiroEntrega" to mapOf(
                        "precisaConferencia" to true,
                        "formaPagamento" to input.paymentMethod,
                        "tipoTransacao" to input.transactionType,
                        "maquininhaId" to input.machineId,
                        "taxaMaquininha" to preview.machineFee,
                        "valorLiquido" to preview.liquidAmount,
                        "valorARepassar" to preview.amountToRepay,
                        "valorAReceber" to preview.amountToReceive
                    ),
                    "acerto" to acerto,
                    "atualizadoEm" to now,
                    "updatedAt" to now
                )
                val acertoId = "${profile.id}_${rideId}".replace(Regex("[^A-Za-z0-9_-]"), "_").take(180)
                doc.reference.set(missionPayload, SetOptions.merge())
                    .addOnSuccessListener {
                        db.collection("acertosEntregadores").document(acertoId).set(acerto, SetOptions.merge())
                        db.collection(profile.collectionName).document(profile.id).set(
                            mapOf(
                                "acerto" to acerto,
                                "financeiro.precisaConferencia" to true,
                                "financeiro.valorARepassar" to preview.amountToRepay,
                                "financeiro.valorAReceber" to preview.amountToReceive,
                                "financeiro.recebidoPeloEntregador" to if (preview.amountToRepay > 0.0) preview.grossAmount else 0.0,
                                "financeiro.taxasMaquininha" to preview.machineFee,
                                "financeiro.taxaMotoboy" to preview.driverFee,
                                "financeiroAtualizadoEm" to now,
                                "updatedAt" to now
                            ),
                            SetOptions.merge()
                        )
                        onDone()
                    }
                    .addOnFailureListener { onError(it.message ?: "Falha ao salvar acerto da corrida.") }
            }, onNotFound = { onError("Corrida não encontrada para registrar acerto.") })
        }
        if (input.machineId.isNotBlank()) {
            db.collection("maquininhas").document(input.machineId).get()
                .addOnSuccessListener { doc -> persist(if (doc.exists()) doc.toPaymentMachine("maquininhas") else null) }
                .addOnFailureListener { persist(null) }
        } else {
            persist(null)
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
            if (ride == null) {
                onError("Esta oferta não está mais disponível.")
                return@findMissionDocument
            }
            val preferences = loadOperationalPreferences(context)
            val hasActive = hasLocalActiveMission(context)
            val activeRouteId = localActiveRouteId(context)
            if (!ride.canBeOfferedTo(profile.id, preferences, hasActive, activeRouteId)) {
                onError(ride.blockReason(preferences, hasActive, activeRouteId).ifBlank { "Oferta bloqueada pela regra operacional." })
                return@findMissionDocument
            }
            val newStatus = if (ride.isRouteAddition) "ADICAO_ROTA_ACEITA" else if (collectionName == "pedidos") "A_CAMINHO_LOJA" else if (collectionName == "rides") "accepted" else "ACEITA"
            val update = linkedMapOf<String, Any?>(
                "status" to newStatus,
                "statusEntregador" to newStatus,
                "statusMotoboy" to newStatus,
                "statusOferta" to if (ride.isRouteAddition) "ADICAO_ROTA_ACEITA" else "ACEITA",
                "ofertaAceita" to true,
                "ativa" to false,
                "entregadorId" to profile.id,
                "entregadorUid" to profile.id,
                "uidEntregador" to profile.id,
                "driverId" to profile.id,
                "entregadorNome" to profile.name,
                "driverName" to profile.name,
                "repasseEntregadorConfirmado" to ride.valueNumber,
                "valorRepasseMotoboy" to ride.valueNumber,
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
                            "codigoPedidoAtual" to ((ride?.orderCode ?: "").ifBlank { rideId.takeLast(4).uppercase(Locale.ROOT) }),
                            "atualizadoEm" to Timestamp.now(),
                            "updatedAt" to Timestamp.now()
                        ),
                        SetOptions.merge()
                    )
                    rememberActiveRide(context, ride)
                    addHistory(profile, rideId, if (ride.isRouteAddition) "ADICAO_ROTA_ACEITA" else "ACEITA", ride.valueNumber, collectionName, ride)
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
                    "statusOferta" to "EXPIRADA",
                    "ativa" to false,
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



    fun reportRideOccurrence(
        context: Context,
        rideId: String,
        reason: String = "Problema na rota",
        details: String = "Solicitado pelo app do entregador",
        onDone: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        val profile = currentSession(context)
        if (profile == null) {
            onError("Faça login para registrar ocorrência.")
            return
        }
        findMissionDocument(rideId, onFound = { doc ->
            val collectionName = doc.reference.parent.id
            val ride = doc.toDriverRide(collectionName)
            val now = Timestamp.now()
            val occurrence = linkedMapOf<String, Any?>(
                "rideId" to rideId,
                "missaoId" to rideId,
                "collectionName" to collectionName,
                "pedidoId" to if (collectionName == "pedidos") rideId else null,
                "rotaId" to (ride?.routeId?.takeIf { it.isNotBlank() } ?: if (collectionName == "rotas_entrega") rideId else null),
                "entregadorId" to profile.id,
                "entregadorUid" to profile.id,
                "entregadorNome" to profile.name,
                "codigoPedido" to (ride?.orderCode ?: rideId.takeLast(6).uppercase(Locale.ROOT)),
                "motivo" to reason,
                "descricao" to details,
                "status" to "ABERTA",
                "origem" to "APP_ENTREGADOR",
                "criadaEm" to now,
                "createdAt" to now,
                "atualizadoEm" to now
            )
            val occurrenceId = db.collection("ocorrencias_entregadores").document().id
            val batch = db.batch()
            batch.set(db.collection("ocorrencias_entregadores").document(occurrenceId), occurrence)
            batch.set(db.collection("ocorrenciasEntregadores").document(occurrenceId), occurrence)
            batch.set(db.collection("ocorrenciasOperacao").document(occurrenceId), occurrence + mapOf("visivelGestor" to true, "prioridade" to "ALTA"))
            batch.set(
                doc.reference,
                mapOf(
                    "status" to "OCORRENCIA",
                    "statusRota" to "OCORRENCIA",
                    "statusEntrega" to "OCORRENCIA",
                    "statusEntregador" to "OCORRENCIA",
                    "statusMotoboy" to "OCORRENCIA",
                    "ocorrenciaAtiva" to true,
                    "statusOcorrencia" to "ABERTA",
                    "ultimaOcorrenciaId" to occurrenceId,
                    "ultimaOcorrenciaMotivo" to reason,
                    "ultimaOcorrenciaEm" to now,
                    "pendenteGestor" to true,
                    "acaoNecessariaGestor" to "RESOLVER_OCORRENCIA",
                    "bloqueadoPorOcorrencia" to true,
                    "atualizadoEm" to now,
                    "updatedAt" to now
                ),
                SetOptions.merge()
            )
            batch.set(
                db.collection(profile.collectionName).document(profile.id),
                mapOf(
                    "statusOperacional" to "OCORRENCIA",
                    "ocorrenciaAtiva" to true,
                    "ultimaOcorrenciaId" to occurrenceId,
                    "ultimaOcorrenciaEm" to now,
                    "emCorrida" to true,
                    "atualizadoEm" to now,
                    "updatedAt" to now
                ),
                SetOptions.merge()
            )
            batch.commit()
                .addOnSuccessListener {
                    addHistory(profile, rideId, "OCORRENCIA: $reason", ride?.valueNumber ?: 0.0, collectionName, ride)
                    onDone()
                }
                .addOnFailureListener { onError(it.message ?: "Falha ao registrar ocorrência.") }
        }, onNotFound = {
            onError("Corrida nao encontrada para registrar ocorrência.")
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
            if (status == "delivering" && ride != null && !ride.pickupReleaseAllowed) {
                onError("A rota ainda precisa ser liberada pelo gestor antes da saída.")
                return@findMissionDocument
            }
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
                    fields["chegouClienteEm"] = Timestamp.now()
                    fields["arrivedClientAt"] = Timestamp.now()
                    fields["aguardandoCodigoEntrega"] = true
                    fields["deliveryCodeRequired"] = true
                }
                "finished" -> {
                    fields["concluidaEm"] = Timestamp.now()
                    fields["finishedAt"] = Timestamp.now()
                    fields["entregueEm"] = Timestamp.now()
                    fields["repasseEntregadorConfirmado"] = ride?.valueNumber ?: 0.0
                    fields["valorRepasseMotoboy"] = ride?.valueNumber ?: 0.0
                    fields["financeiroConferidoPeloApp"] = true
                    fields["acerto.origem"] = "APP_ENTREGADOR"
                    fields["acerto.status"] = "AGUARDANDO_CONFERENCIA"
                    fields["acerto.taxaMotoboy"] = ride?.valueNumber ?: 0.0
                    fields["acerto.precisaInformarPagamento"] = ride?.paymentMethod.isNullOrBlank()
                    fields["financeiro.precisaConferencia"] = true
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
                            "codigoPedidoAtual" to ((ride?.orderCode ?: "").ifBlank { rideId.takeLast(4).uppercase(Locale.ROOT) }),
                            "atualizadoEm" to Timestamp.now(),
                            "updatedAt" to Timestamp.now()
                        ),
                        SetOptions.merge()
                    )
                    if (status == "finished") {
                        forgetActiveRide(context)
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
            "localizacaoOrigem" to "android_native_v6_10_1"
        )

        db.collection(profile.collectionName).document(profile.id)
            .set(payload, SetOptions.merge())
            .addOnFailureListener { onError(it.message ?: "Falha ao atualizar localização.") }
    }

    fun clearDriverTracking(context: Context) {
        val profile = currentSession(context) ?: return
        val now = Timestamp.now()
        forgetActiveRide(context)
        db.collection(profile.collectionName).document(profile.id).set(
            mapOf(
                "rastreamentoAtivo" to false,
                "statusOperacional" to "LIVRE",
                "corridaAtualId" to null,
                "missaoAtualId" to null,
                "pedidoAtualId" to null,
                "rotaAtualId" to null,
                "rotaAtualStatus" to null,
                "pedidoAtualStatus" to null,
                "emCorrida" to false,
                "ocorrenciaAtiva" to false,
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

    fun forceClearActiveMission(context: Context, reason: String = "DESTRAVAMENTO_APP", onDone: () -> Unit = {}, onError: (String) -> Unit = {}) {
        val profile = currentSession(context)
        if (profile == null) {
            onError("Faça login para destravar o app.")
            return
        }
        releaseStaleMission(context, profile, reason, onDone, onError)
    }

    private fun releaseStaleMission(
        context: Context,
        profile: DriverProfile,
        reason: String,
        onDone: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val missionId = prefs.getString(KEY_ACTIVE_MISSION, "").orEmpty()
        val routeId = prefs.getString(KEY_ACTIVE_ROUTE, "").orEmpty()
        val orderId = prefs.getString(KEY_ACTIVE_ORDER, "").orEmpty()
        val now = Timestamp.now()
        forgetActiveRide(context)
        val payload = mapOf(
            "status" to "Livre",
            "online" to true,
            "statusOperacional" to "LIVRE",
            "emCorrida" to false,
            "corridaAtualId" to null,
            "missaoAtualId" to null,
            "pedidoAtualId" to null,
            "rotaAtualId" to null,
            "rotaAtualStatus" to null,
            "pedidoAtualStatus" to null,
            "rastreamentoAtivo" to false,
            "ocorrenciaAtiva" to false,
            "destravadoAutomaticamenteEm" to now,
            "ultimoDestravamentoMotivo" to reason,
            "ultimaMissaoEncerradaId" to missionId.ifBlank { routeId.ifBlank { orderId } },
            "atualizadoEm" to now,
            "updatedAt" to now
        )
        db.collection(profile.collectionName).document(profile.id).set(payload, SetOptions.merge())
            .addOnSuccessListener {
                if (missionId.isNotBlank() || routeId.isNotBlank() || orderId.isNotBlank()) {
                    db.collection("appEventosOperacao").add(
                        mapOf(
                            "tipo" to "DESTRAVAMENTO_ENTREGADOR",
                            "origem" to "APP_ENTREGADOR",
                            "motivo" to reason,
                            "entregadorId" to profile.id,
                            "entregadorUid" to profile.id,
                            "entregadorNome" to profile.name,
                            "missaoId" to missionId,
                            "rotaId" to routeId,
                            "pedidoId" to orderId,
                            "criadoEm" to now,
                            "createdAt" to now
                        )
                    )
                }
                onDone()
            }
            .addOnFailureListener { onError(it.message ?: "Falha ao destravar entregador.") }
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
                "origem" to "android_native_v6_10_1",
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



    private fun DocumentSnapshot.toPaymentMachine(collectionName: String): PaymentMachine {
        val status = anyString("status", "situacao", "situação").upperOrTrim()
        val activeByStatus = status.isBlank() || status in setOf("ATIVA", "ATIVO", "ACTIVE", "ONLINE", "HABILITADA")
        return PaymentMachine(
            id = id,
            collectionName = collectionName,
            name = anyString("nome", "name", "titulo", "title", "apelido").ifBlank { "Maquininha" },
            owner = anyString("dono", "owner", "responsavel", "responsável").ifBlank { "loja" },
            active = anyBoolean("ativa", "ativo", "active", "enabled", "habilitada") ?: activeByStatus,
            debitFeePercent = anyDouble("taxaDebito", "taxaDébito", "taxas.debito", "fees.debit") ?: 0.0,
            creditFeePercent = anyDouble("taxaCredito", "taxaCrédito", "taxas.credito", "taxas.crédito", "fees.credit") ?: 0.0,
            installmentFeePercent = anyDouble("taxaParcelado", "taxas.parcelado", "fees.installment") ?: 0.0,
            ticketFeePercent = anyDouble("taxaTicket", "taxas.ticket", "fees.ticket") ?: 0.0,
            anticipationFeePercent = anyDouble("taxaAntecipacao", "taxaAntecipação", "taxas.antecipacao", "taxas.antecipação") ?: 0.0,
            order = anyDouble("order", "ordem", "position", "posicao", "posição")?.toInt() ?: 999
        )
    }

    private fun DocumentSnapshot.toAppRuntimeConfig(id: String): AppRuntimeConfig {
        return AppRuntimeConfig(
            id = id,
            maintenanceActive = anyBoolean("manutencaoAtiva", "manutençãoAtiva", "maintenanceActive", "ativa") ?: false,
            maintenanceMessage = anyString("mensagemManutencao", "mensagemManutenção", "maintenanceMessage", "mensagem"),
            maintenanceReturn = anyString("previsaoRetorno", "previsãoRetorno", "maintenanceReturn", "retorno"),
            minimumVersion = anyString("versaoMinima", "versãoMinima", "minimumVersion"),
            forceUpdate = anyBoolean("forcarAtualizacao", "forçarAtualização", "forceUpdate") ?: false,
            globalAlert = anyString("alertaGlobal", "globalAlert", "avisoGlobal")
        )
    }

    private fun DocumentSnapshot.toWalletStats(): DriverStats {
        val available = anyDouble(
            "saldoDisponivel",
            "carteira.saldoDisponivel",
            "financeiro.saldoDisponivel",
            "wallet.available",
            "payout.available"
        )
        val pending = anyDouble(
            "saldoPendente",
            "carteira.saldoPendente",
            "financeiro.saldoPendente",
            "wallet.pending",
            "payout.pending"
        )
        val totalToReceive = anyDouble(
            "totalAReceber",
            "carteira.totalAReceber",
            "financeiro.totalAReceber",
            "wallet.totalToReceive",
            "payout.totalToReceive"
        )
        val recebidoPeloEntregador = anyDouble(
            "acerto.recebidoPeloEntregador",
            "financeiro.recebidoPeloEntregador",
            "carteira.recebidoPeloEntregador",
            "recebidoPeloEntregador",
            "totalRecebidoMotoboy",
            "valorRecebidoMotoboy",
            "recebidoMotoboy"
        )
        val taxaMotoboy = anyDouble(
            "acerto.taxaMotoboy",
            "financeiro.taxaMotoboy",
            "carteira.taxaMotoboy",
            "taxaMotoboy",
            "valorMotoboy",
            "ganhoMotoboy",
            "repasseEntregador"
        )
        val taxasMaquininha = anyDouble(
            "acerto.taxasMaquininha",
            "financeiro.taxasMaquininha",
            "carteira.taxasMaquininha",
            "taxasMaquininha",
            "taxaMaquininha",
            "descontoMaquininha"
        )
        val valorARepassar = anyDouble(
            "acerto.valorARepassar",
            "financeiro.valorARepassar",
            "carteira.valorARepassar",
            "valorARepassar",
            "deveRepassar",
            "repasseParaLoja",
            "repasseSistema",
            "valorLojaSistema"
        )
        val valorAReceber = anyDouble(
            "acerto.valorAReceber",
            "financeiro.valorAReceber",
            "carteira.valorAReceber",
            "valorAReceber",
            "temAReceber",
            "receberMotoboy"
        )
        val dinheiroRecebido = anyDouble("acerto.dinheiro", "financeiro.dinheiro", "carteira.dinheiro", "dinheiroRecebido", "recebidoDinheiro")
        val cartaoRecebido = anyDouble("acerto.cartao", "financeiro.cartao", "carteira.cartao", "cartaoRecebido", "maquininhaRecebido", "recebidoCartao")
        val pixRecebido = anyDouble("acerto.pix", "financeiro.pix", "carteira.pixRecebido", "pixRecebido", "recebidoPix")
        val nextMillis = anyTimestamp(
            "proximoRepasseData",
            "carteira.proximoRepasseData",
            "financeiro.proximoRepasseData",
            "proximoRepasseEm",
            "nextPayoutAt"
        )?.toDate()?.time ?: anyString(
            "proximoRepasseData",
            "carteira.proximoRepasseData",
            "financeiro.proximoRepasseData",
            "proximoRepasseEm",
            "nextPayoutAt"
        ).toFlexibleMillisOrNull()

        return DriverStats(
            saldoDisponivel = available,
            saldoPendente = pending,
            totalAReceber = totalToReceive,
            recebidoPeloEntregador = recebidoPeloEntregador,
            taxaMotoboy = taxaMotoboy,
            taxasMaquininha = taxasMaquininha,
            valorARepassar = valorARepassar,
            valorAReceber = valorAReceber,
            dinheiroRecebido = dinheiroRecebido,
            cartaoRecebido = cartaoRecebido,
            pixRecebido = pixRecebido,
            proximoRepasseLabel = nextMillis?.let { Date(it).formatHistoryLabel() } ?: anyString(
                "proximoRepasseLabel",
                "carteira.proximoRepasseLabel",
                "financeiro.proximoRepasseLabel"
            ).ifBlank { "A definir" },
            proximoRepasseDescricao = anyString(
                "proximoRepasseDescricao",
                "carteira.proximoRepasseDescricao",
                "financeiro.proximoRepasseDescricao",
                "nextPayoutDescription"
            ).ifBlank { "A definir" },
            pixKey = anyString("chavePix", "pix", "pixKey", "carteira.pixChave", "financeiro.pixChave"),
            pixVerificada = anyBoolean("pixVerificada", "carteira.pixVerificada", "financeiro.pixVerificada") ?: false,
            bankName = anyString("banco", "bank", "bankName", "carteira.banco", "financeiro.banco")
        )
    }

    private fun DocumentSnapshot.toDriverPayout(driverId: String): DriverPayout? {
        if (!matchesDriverId(driverId)) return null
        val value = anyDouble("valor", "amount", "value", "valorRepasse", "valorPago", "total")
            ?: anyString("valor", "amount", "value", "valorRepasse", "valorPago", "total").toMoneyDouble()
            ?: 0.0
        val statusRaw = anyString("status", "situacao", "situação").upperOrTrim().ifBlank { "PENDENTE" }
        val createdMillis = anyTimestamp(
            "dataPagamento",
            "pagoEm",
            "paidAt",
            "data",
            "programadoEm",
            "scheduledAt",
            "createdAt",
            "criadoEm"
        )?.toDate()?.time ?: anyString(
            "dataPagamento",
            "pagoEm",
            "paidAt",
            "data",
            "programadoEm",
            "scheduledAt",
            "createdAt",
            "criadoEm"
        ).toFlexibleMillisOrNull() ?: System.currentTimeMillis()
        return DriverPayout(
            id = id,
            value = value,
            valueLabel = formatCurrency(value),
            status = statusRaw,
            statusLabel = statusRaw.payoutStatusLabel(),
            createdAtMillis = createdMillis,
            createdLabel = Date(createdMillis).formatHistoryLabel(),
            method = anyString("metodo", "method", "forma", "tipo").ifBlank { "Pix" },
            note = anyString("observacao", "observação", "note", "descricao", "descrição")
        )
    }

    private fun DocumentSnapshot.toAppNotice(collectionName: String): AppNotice? {
        val title = anyString("title", "titulo", "título", "headline", "assunto", "nome").ifBlank { "Aviso da operação" }
        val message = anyString("message", "mensagem", "description", "descricao", "descrição", "body", "texto", "conteudo", "conteúdo")
        val category = anyString("category", "categoria", "tipo").ifBlank { "Operação" }
        val priority = anyString("priority", "prioridade", "nivel", "nível").ifBlank { "NORMAL" }
        val status = anyString("status", "situacao", "situação").upperOrTrim()
        val activeByStatus = status.isBlank() || status in setOf("ATIVO", "ACTIVE", "APROVADO", "PUBLICADO", "ONLINE", "ENVIADO")
        val active = anyBoolean("active", "ativo", "enabled", "visivel", "visível", "publicado") ?: activeByStatus
        val startsAtMillis = anyTimestamp("startsAt", "inicioEm", "começaEm", "comecaEm", "dataInicio")?.toDate()?.time
            ?: anyString("startsAt", "inicio", "inicioEm", "dataInicio").toFlexibleMillisOrNull()
        val endsAtMillis = anyTimestamp("endsAt", "fimEm", "terminaEm", "dataFim")?.toDate()?.time
            ?: anyString("endsAt", "fim", "fimEm", "dataFim").toFlexibleMillisOrNull()
        val createdAtMillis = anyTimestamp("createdAt", "criadoEm", "dataCriacao", "dataCriação", "enviadoEm", "timestamp")?.toDate()?.time
            ?: anyString("createdAt", "criadoEm", "data", "enviadoEm", "timestamp").toFlexibleMillisOrNull()
            ?: System.currentTimeMillis()
        val targetDriverId = anyString(
            "targetDriverId",
            "entregadorId",
            "entregadorUid",
            "motoboyId",
            "driverId",
            "uidEntregador",
            "destinatarioId",
            "destinatárioId"
        )
        val targetGroup = anyString("targetGroup", "grupo", "publico", "público").ifBlank { "all" }
        if (title.isBlank() && message.isBlank()) return null
        return AppNotice(
            id = id,
            collectionName = collectionName,
            title = title,
            message = message,
            category = category,
            priority = priority,
            actionType = anyString("actionType", "tipoAcao", "tipoAção", "acao", "ação").ifBlank { "none" },
            actionTarget = anyString("actionTarget", "destino", "target", "url", "link"),
            targetDriverId = targetDriverId,
            targetGroup = targetGroup,
            read = anyBoolean("read", "lido", "visualizado") ?: false,
            active = active,
            order = anyDouble("order", "ordem", "position", "posicao", "posição")?.toInt() ?: 999,
            createdAtMillis = createdAtMillis,
            createdLabel = Date(createdAtMillis).formatHistoryLabel(),
            startsAtMillis = startsAtMillis,
            endsAtMillis = endsAtMillis
        )
    }

    private fun DocumentSnapshot.toAppCarouselBanner(collectionName: String): AppCarouselBanner? {
        val title = anyString("title", "titulo", "nome", "headline")
        val imageUrl = anyString("imageUrl", "imagemUrl", "urlImagem", "image", "imagem", "bannerUrl", "fotoUrl")
        if (title.isBlank() && imageUrl.isBlank()) return null
        val status = anyString("status", "situacao", "situação").upperOrTrim()
        val activeByStatus = status.isBlank() || status in setOf("ATIVO", "ACTIVE", "APROVADO", "PUBLICADO", "ONLINE")
        val active = anyBoolean("active", "ativo", "enabled", "visivel", "visível", "publicado") ?: activeByStatus
        val startsAtMillis = anyTimestamp("startsAt", "inicioEm", "começaEm", "comecaEm")?.toDate()?.time
            ?: anyString("startsAt", "inicio", "inicioEm", "dataInicio").toFlexibleMillisOrNull()
        val endsAtMillis = anyTimestamp("endsAt", "fimEm", "terminaEm")?.toDate()?.time
            ?: anyString("endsAt", "fim", "fimEm", "dataFim").toFlexibleMillisOrNull()
        return AppCarouselBanner(
            id = id,
            collectionName = collectionName,
            title = title,
            badge = anyString("badge", "selo", "tag", "categoria"),
            description = anyString("description", "descricao", "descrição", "subtitle", "subtitulo", "texto", "mensagem"),
            buttonText = anyString("buttonText", "ctaText", "textoBotao", "textoBotão", "cta", "callToAction"),
            imageUrl = imageUrl,
            active = active,
            order = anyDouble("order", "ordem", "position", "posicao", "posição")?.toInt() ?: 999,
            actionType = anyString("actionType", "tipoAcao", "tipoAção", "acao", "ação").ifBlank { "none" },
            actionTarget = anyString("actionTarget", "destino", "target", "url", "link"),
            displayMode = anyString("displayMode", "modoExibicao", "modoExibição", "modo", "layout", "bannerMode", "tipoVisual")
                .ifBlank { if (anyBoolean("imageOnly", "somenteImagem", "soImagem", "apenasImagem", "artePronta") == true) "image_only" else "auto" },
            showBadge = anyBoolean("showBadge", "mostrarSelo", "exibirSelo"),
            showTitle = anyBoolean("showTitle", "mostrarTitulo", "mostrarTítulo", "exibirTitulo", "exibirTítulo"),
            showDescription = anyBoolean("showDescription", "mostrarDescricao", "mostrarDescrição", "exibirDescricao", "exibirDescrição"),
            showButton = anyBoolean("showButton", "mostrarBotao", "mostrarBotão", "exibirBotao", "exibirBotão"),
            startsAtMillis = startsAtMillis,
            endsAtMillis = endsAtMillis
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
            city = anyString("cidade", "city", "municipio", "município"),
            vehicle = anyString("veiculo", "veículo", "modalidade", "tipoVeiculo", "tipoVeículo"),
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

    private fun String.payoutStatusLabel(): String {
        val s = upperOrTrim()
        return when {
            s.contains("PAGO") || s.contains("PAID") -> "Pago"
            s.contains("PROCESS") || s.contains("ANDAMENTO") -> "Processando"
            s.contains("CANCEL") || s.contains("ESTORN") -> "Cancelado"
            s.contains("AGEND") || s.contains("PROGRAM") -> "Agendado"
            else -> "Pendente"
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
    val city: String = "",
    val vehicle: String = "",
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


data class PaymentMachine(
    val id: String,
    val collectionName: String = "maquininhas",
    val name: String = "Maquininha",
    val owner: String = "loja",
    val active: Boolean = true,
    val debitFeePercent: Double = 0.0,
    val creditFeePercent: Double = 0.0,
    val installmentFeePercent: Double = 0.0,
    val ticketFeePercent: Double = 0.0,
    val anticipationFeePercent: Double = 0.0,
    val order: Int = 999
)

data class PaymentSettlementInput(
    val rideId: String,
    val orderTotal: Double,
    val driverFee: Double,
    val paymentMethod: String,
    val transactionType: String = "",
    val machineId: String = "",
    val machineName: String = "",
    val manualFeePercent: Double = 0.0,
    val receivedByDriver: Boolean = false,
    val receivedBy: String = "",
    val note: String = ""
)

data class PaymentSettlementPreview(
    val grossAmount: Double = 0.0,
    val driverFee: Double = 0.0,
    val machineFee: Double = 0.0,
    val machineFeePercent: Double = 0.0,
    val liquidAmount: Double = 0.0,
    val amountToRepay: Double = 0.0,
    val amountToReceive: Double = 0.0,
    val cashAmount: Double = 0.0,
    val cardAmount: Double = 0.0,
    val pixAmount: Double = 0.0,
    val status: String = "AGUARDANDO_CONFERENCIA"
)

data class AppRuntimeConfig(
    val id: String = "entregador",
    val maintenanceActive: Boolean = false,
    val maintenanceMessage: String = "",
    val maintenanceReturn: String = "",
    val minimumVersion: String = "",
    val forceUpdate: Boolean = false,
    val globalAlert: String = ""
)

data class DriverPayout(
    val id: String,
    val value: Double = 0.0,
    val valueLabel: String = "R$ 0,00",
    val status: String = "PENDENTE",
    val statusLabel: String = "Pendente",
    val createdAtMillis: Long = 0L,
    val createdLabel: String = "Agora",
    val method: String = "Pix",
    val note: String = ""
)

data class DriverStats(
    val totalToday: Double = 0.0,
    val totalWeek: Double = 0.0,
    val totalMonth: Double = 0.0,
    val finishedCount: Int = 0,
    val ridesTodayCount: Int = 0,
    val finishedTodayCount: Int = 0,
    val score: Int = 0,
    val saldoDisponivel: Double? = null,
    val saldoPendente: Double? = null,
    val totalAReceber: Double? = null,
    val recebidoPeloEntregador: Double? = null,
    val taxaMotoboy: Double? = null,
    val taxasMaquininha: Double? = null,
    val valorARepassar: Double? = null,
    val valorAReceber: Double? = null,
    val dinheiroRecebido: Double? = null,
    val cartaoRecebido: Double? = null,
    val pixRecebido: Double? = null,
    val proximoRepasseLabel: String = "A definir",
    val proximoRepasseDescricao: String = "Estimativa será informada pela operação.",
    val pixKey: String = "",
    val pixVerificada: Boolean = false,
    val bankName: String = "",
    val payoutRows: List<DriverPayout> = emptyList()
)

data class DriverHistory(
    val id: String,
    val rideId: String,
    val action: String,
    val value: String,
    val createdAtMillis: Long,
    val createdLabel: String,
    val pickup: String = "",
    val dropoff: String = "",
    val neighborhood: String = "",
    val distance: String = "",
    val duration: String = "",
    val reason: String = "",
    val paymentMethod: String = ""
)

data class AppNotice(
    val id: String,
    val collectionName: String = "local",
    val title: String = "",
    val message: String = "",
    val category: String = "Operação",
    val priority: String = "NORMAL",
    val actionType: String = "none",
    val actionTarget: String = "",
    val targetDriverId: String = "",
    val targetGroup: String = "all",
    val read: Boolean = false,
    val active: Boolean = true,
    val order: Int = 999,
    val createdAtMillis: Long = 0L,
    val createdLabel: String = "Agora",
    val startsAtMillis: Long? = null,
    val endsAtMillis: Long? = null
) {
    fun isVisible(nowMillis: Long = System.currentTimeMillis()): Boolean {
        if (!active) return false
        if (startsAtMillis != null && nowMillis < startsAtMillis) return false
        if (endsAtMillis != null && nowMillis > endsAtMillis) return false
        return true
    }

    fun matchesDriver(driverId: String): Boolean {
        val target = targetDriverId.trim()
        if (target.isBlank()) return true
        return target == driverId
    }
}

data class AppCarouselBanner(
    val id: String,
    val collectionName: String = "local",
    val title: String = "",
    val badge: String = "",
    val description: String = "",
    val buttonText: String = "",
    val imageUrl: String = "",
    val active: Boolean = true,
    val order: Int = 999,
    val actionType: String = "none",
    val actionTarget: String = "",
    val displayMode: String = "auto",
    val showBadge: Boolean? = null,
    val showTitle: Boolean? = null,
    val showDescription: Boolean? = null,
    val showButton: Boolean? = null,
    val startsAtMillis: Long? = null,
    val endsAtMillis: Long? = null
) {
    fun isVisible(nowMillis: Long = System.currentTimeMillis()): Boolean {
        if (!active) return false
        if (startsAtMillis != null && nowMillis < startsAtMillis) return false
        if (endsAtMillis != null && nowMillis > endsAtMillis) return false
        return true
    }
}

data class RouteOrder(
    val id: String = "",
    val code: String = "",
    val customerName: String = "",
    val status: String = "",
    val paymentSummary: String = "",
    val ready: Boolean = false,
    val requiresMachine: Boolean = false,
    val requiresChange: Boolean = false,
    val changeForNumber: Double = 0.0,
    val terminal: Boolean = false
)

data class DriverOperationalPreferences(
    val hasMachine: Boolean = false,
    val acceptsDebit: Boolean = false,
    val acceptsCredit: Boolean = false,
    val acceptsInstallment: Boolean = false,
    val acceptsTicket: Boolean = false,
    val hasCashChange: Boolean = false,
    val changeAvailableNumber: Double = 0.0,
    val onlyOnlinePaid: Boolean = false,
    val blockCashAtNight: Boolean = false,
    val blockMachineAtNight: Boolean = false,
    val nightStartHour: Int = 0,
    val nightEndHour: Int = 6
) {
    fun isRestrictedHour(now: Calendar = Calendar.getInstance()): Boolean {
        val hour = now.get(Calendar.HOUR_OF_DAY)
        val start = nightStartHour.coerceIn(0, 23)
        val end = nightEndHour.coerceIn(0, 23)
        return if (start <= end) hour in start until end else hour >= start || hour < end
    }
}

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
    val paymentStatus: String = "",
    val receivedBy: String = "",
    val changeForNumber: Double = 0.0,
    val requiresMachine: Boolean = false,
    val deliveryCode: String = "",
    val offerExpiresAtMillis: Long = 0L,
    val offerCreatedAtMillis: Long = 0L,
    val offerInactive: Boolean = false,
    val offerType: String = "",
    val isRouteAddition: Boolean = false,
    val routeId: String = "",
    val routeOrderCount: Int = 1,
    val routeReadyCount: Int = 0,
    val routeReleaseCode: String = "",
    val pickupReleaseStatus: String = "",
    val pickupReleaseAllowed: Boolean = false,
    val routeLocked: Boolean = false,
    val routeOrders: List<RouteOrder> = emptyList()
) {
    fun matchesDriver(driverId: String): Boolean {
        val ids = listOf(assignedDriverId, targetDriverId).filter { it.isNotBlank() }
        return ids.contains(driverId)
    }

    fun hasExpired(nowMillis: Long = System.currentTimeMillis()): Boolean {
        return offerExpiresAtMillis > 0L && nowMillis > offerExpiresAtMillis
    }

    fun paymentKind(): String {
        val m = paymentMethod.upperOrTrim()
        val s = paymentStatus.upperOrTrim()
        return when {
            s.contains("PAGO") || m.contains("ONLINE") || m.contains("INFINITE") || m.contains("PIX ONLINE") -> "ONLINE"
            m.contains("DINHEIRO") || m.contains("CASH") -> "DINHEIRO"
            requiresMachine || m.contains("CART") || m.contains("MAQUIN") || m.contains("DÉBIT") || m.contains("DEBIT") || m.contains("CRÉDIT") || m.contains("CREDIT") -> "MAQUININHA"
            m.contains("PIX") -> "PIX"
            else -> "NAO_INFORMADO"
        }
    }

    fun isPaymentCompatible(preferences: DriverOperationalPreferences): Boolean {
        val kind = paymentKind()
        val restricted = preferences.isRestrictedHour()
        if (preferences.onlyOnlinePaid && kind != "ONLINE") return false
        if (kind == "DINHEIRO") {
            if (preferences.blockCashAtNight && restricted) return false
            if (changeForNumber > 0.0 && (!preferences.hasCashChange || preferences.changeAvailableNumber + 0.001 < changeForNumber)) return false
        }
        if (kind == "MAQUININHA") {
            if (!preferences.hasMachine) return false
            if (preferences.blockMachineAtNight && restricted) return false
        }
        return true
    }

    fun blockReason(preferences: DriverOperationalPreferences, hasActiveMission: Boolean = false, activeRouteId: String = ""): String {
        return when {
            offerInactive -> "Oferta inativa ou substituída pelo gestor."
            hasExpired() -> "Oferta expirada."
            hasActiveMission && !(isRouteAddition && routeId.isNotBlank() && routeId == activeRouteId) -> "Você já está com uma rota ativa. Nova corrida solta foi bloqueada."
            preferences.onlyOnlinePaid && paymentKind() != "ONLINE" -> "Sua preferência está como somente pedidos pagos online."
            paymentKind() == "DINHEIRO" && preferences.blockCashAtNight && preferences.isRestrictedHour() -> "Dinheiro bloqueado no seu horário de restrição."
            paymentKind() == "DINHEIRO" && changeForNumber > 0.0 && (!preferences.hasCashChange || preferences.changeAvailableNumber + 0.001 < changeForNumber) -> "Pedido exige troco maior que o valor informado disponível."
            paymentKind() == "MAQUININHA" && !preferences.hasMachine -> "Pedido exige maquininha e você informou que não possui."
            paymentKind() == "MAQUININHA" && preferences.blockMachineAtNight && preferences.isRestrictedHour() -> "Maquininha bloqueada no seu horário de restrição."
            else -> ""
        }
    }

    fun canBeOfferedTo(
        driverId: String,
        preferences: DriverOperationalPreferences = DriverOperationalPreferences(),
        hasActiveMission: Boolean = false,
        activeRouteId: String = ""
    ): Boolean {
        if (offerInactive || hasExpired()) return false
        if (rejectedDriverIds.contains(driverId) || expiredDriverIds.contains(driverId)) return false
        if (hasActiveMission && !(isRouteAddition && routeId.isNotBlank() && routeId == activeRouteId)) return false
        if (!isPaymentCompatible(preferences)) return false
        if (matchesDriver(driverId)) return true
        return broadcast || (assignedDriverId.isBlank() && targetDriverId.isBlank())
    }
}

private val TERMINAL_MISSION_STATUSES = setOf(
    "CANCELADO", "CANCELADA", "CANCELED", "CANCELLED", "CANCELAMENTO",
    "CANCELADO_PELO_GESTOR", "CANCELADA_PELO_GESTOR", "CANCELADO_GESTOR", "CANCELADA_GESTOR",
    "CANCELADO_LOJA", "CANCELADA_LOJA", "CANCELADO_CLIENTE", "CANCELADA_CLIENTE",
    "PEDIDO_CANCELADO", "ROTA_CANCELADA", "DESPACHO_CANCELADO",
    "FINALIZADO", "FINALIZADA", "CONCLUIDA", "CONCLUÍDA", "CONCLUÍDO", "CONCLUIDO",
    "ENTREGUE", "DELIVERED", "FINISHED", "ARQUIVADO", "ARQUIVADA", "ENCERRADO", "ENCERRADA"
)
private val PICKUP_RELEASED_STATUSES = setOf("LIBERADA_PARA_SAIDA", "SAIDA_LIBERADA", "RETIRADA_LIBERADA", "COM_ENTREGADOR", "A_CAMINHO_CLIENTE", "EM_ROTA", "SAIU_ENTREGA")
private val ROUTE_LOCKED_STATUSES = setOf("COM_ENTREGADOR", "A_CAMINHO_CLIENTE", "EM_ROTA", "SAIU_ENTREGA", "ENTREGADOR_NO_LOCAL", "ENTREGUE", "FINALIZADA")

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
private val ACCEPTED_STATUSES = setOf("ACEITA", "A_CAMINHO_LOJA", "AGUARDANDO_PRONTOS", "AGUARDANDO_TODOS_PRONTOS", "AGUARDANDO_COLETA", "ACCEPTED", "ACEITO", "INDO_COLETA")
private val PICKUP_STATUSES = setOf("COLETANDO", "ROTA_PRONTA_PARA_RETIRADA", "AGUARDANDO_LIBERACAO_GESTOR", "AGUARDANDO_LIBERAÇÃO_GESTOR", "AGUARDANDO_SAIDA", "LIBERADA_PARA_SAIDA", "PICKUP", "EM_COLETA", "COLETADO")
private val ARRIVED_CLIENT_STATUSES = setOf("ENTREGADOR_NO_LOCAL", "CHEGOU_CLIENTE", "CHEGOU_ENTREGA", "NO_CLIENTE", "ARRIVED_CLIENT", "ARRIVED_AT_CLIENT")
private val DELIVERING_STATUSES = setOf("COM_ENTREGADOR", "EM_ROTA", "SAIU_ENTREGA", "A_CAMINHO_CLIENTE", "DELIVERING", "EM_ENTREGA")
private val FINAL_HISTORY_STATUSES = setOf("CONCLUIDA", "ENTREGUE", "FINALIZADA", "FINISHED", "DELIVERED", "finished", "delivered")
private val OCCURRENCE_STATUSES = setOf("OCORRENCIA", "OCORRÊNCIA", "PROBLEMA", "SUPORTE", "AGUARDANDO_GESTOR", "PENDENTE_GESTOR")
private val APPROVED_STATUSES = setOf("APROVADO", "APPROVED", "LIBERADO", "ATIVO", "ACTIVE")
private val BLOCKED_STATUSES = setOf("REPROVADO", "BLOQUEADO", "BLOCKED", "SUSPENSO", "SUSPENDED", "CANCELADO")

private fun normalizeUiStatus(raw: String, collectionName: String = ""): String {
    val status = raw.upperOrTrim()
    val offerStatuses = if (collectionName == "pedidos") PEDIDO_OFFER_STATUSES else ROUTE_OFFER_STATUSES
    return when {
        status in offerStatuses -> "pending"
        status in ACCEPTED_STATUSES -> "accepted"
        status in PICKUP_STATUSES -> "pickup"
        status in ARRIVED_CLIENT_STATUSES -> "arrived_client"
        status in OCCURRENCE_STATUSES -> "occurrence"
        status in DELIVERING_STATUSES -> "delivering"
        status in FINAL_HISTORY_STATUSES -> "finished"
        else -> raw.ifBlank { "" }
    }
}

private fun DocumentSnapshot.hasTerminalMissionStatus(): Boolean {
    val statuses = listOf(
        anyString("status"),
        anyString("statusPedido"),
        anyString("statusPedidoCore"),
        anyString("statusCore"),
        anyString("statusEntrega"),
        anyString("statusRota"),
        anyString("statusOperacao"),
        anyString("statusOperação"),
        anyString("situacao"),
        anyString("situação"),
        anyString("estado"),
        anyString("state"),
        anyString("pedido.status"),
        anyString("pedido.statusPedido"),
        anyString("pedido.statusPedidoCore"),
        anyString("entrega.status"),
        anyString("delivery.status"),
        anyString("operacao.status"),
        anyString("operação.status")
    ).map { it.upperOrTrim() }
    val hasCancelTimestamp = anyTimestamp(
        "canceladoEm", "canceladaEm", "cancelamentoEm", "dataCancelamento",
        "pedidoCanceladoEm", "rotaCanceladaEm", "cancelledAt", "canceledAt"
    ) != null
    val cancelFlag = anyBoolean(
        "cancelado", "cancelada", "pedidoCancelado", "rotaCancelada", "despachoCancelado",
        "canceladoPeloGestor", "canceladaPeloGestor", "cancelled", "canceled", "arquivado"
    ) == true
    return statuses.any { it in TERMINAL_MISSION_STATUSES } || cancelFlag || hasCancelTimestamp
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
    if (hasTerminalMissionStatus()) return false
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
        "pagamento.valorPedido",
        "valores.valorPedido",
        "valores.total",
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
        "acerto.taxasMaquininha",
        "pagamento.taxaMaquininha",
        "taxaMaquininha",
        "valorTaxaMaquininha",
        "maquininhaTaxaValor",
        "financeiroEntrega.taxaMaquininha"
    ) ?: 0.0
}



private fun DocumentSnapshot.toRouteOrders(): List<RouteOrder> {
    val raw = listOf("pedidos", "orders", "routeOrders", "pedidosDaRota", "itensPedidos")
        .firstNotNullOfOrNull { key -> getDeep(key) }
        ?: return emptyList()
    val rows: List<Any?> = when (raw) {
        is List<*> -> raw
        is Map<*, *> -> raw.values.toList()
        else -> emptyList()
    }
    return rows.mapNotNull { item ->
        val map = item as? Map<*, *> ?: return@mapNotNull null
        val status = map.firstString("status", "statusPedido", "statusPedidoCore", "cozinhaStatus", "situacao", "situação", "estado", "state")
        val payment = map.firstString("pagamento", "formaPagamento", "paymentMethod", "pagamento.forma")
        val changeFor = map.firstDouble("trocoPara", "changeFor", "valorTrocoPara") ?: 0.0
        val orderTerminal = status.upperOrTrim() in TERMINAL_MISSION_STATUSES ||
            map.firstBoolean("cancelado", "cancelada", "pedidoCancelado", "canceladoPeloGestor", "cancelled", "canceled", "finalizado", "entregue", "arquivado") == true ||
            map.firstString("canceladoEm", "canceladaEm", "cancelamentoEm", "pedidoCanceladoEm", "cancelledAt", "canceledAt").isNotBlank()
        RouteOrder(
            id = map.firstString("id", "pedidoId", "orderId", "uid"),
            code = map.firstString("codigoPedido", "numeroPedido", "orderCode", "codigo"),
            customerName = map.firstString("cliente", "clienteNome", "customerName", "nome"),
            status = status.ifBlank { "Aguardando" },
            paymentSummary = payment.ifBlank { "Pagamento não informado" },
            ready = !orderTerminal && status.upperOrTrim() in setOf("PRONTO", "PRONTA", "READY", "LIBERADO", "LIBERADA"),
            requiresMachine = map.firstBoolean("precisaMaquininha", "requiresMachine", "cartaoPresencial") == true || payment.upperOrTrim().contains("CART"),
            requiresChange = changeFor > 0.0,
            changeForNumber = changeFor,
            terminal = orderTerminal
        )
    }
}

private fun DocumentSnapshot.toDriverRide(collectionName: String): DriverRide? {
    if (hasTerminalMissionStatus()) return null
    if (!deliveryReleasedToDriver(collectionName)) return null

    val rawStatus = if (collectionName == "pedidos") {
        val main = anyString("status").upperOrTrim()
        if (main in ACCEPTED_STATUSES || main in PICKUP_STATUSES || main in ARRIVED_CLIENT_STATUSES || main in OCCURRENCE_STATUSES || main in DELIVERING_STATUSES || main in FINAL_HISTORY_STATUSES) {
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
    val paymentMethod = anyString(
        "pagamento.forma", "pagamento.metodo", "pagamento.formaLabel", "pagamento.metodoLabel",
        "formaPagamento", "pagamento", "paymentMethod", "metodoPagamento"
    )
    val paymentStatus = anyString(
        "pagamento.status", "statusPagamento", "pagamentoStatus", "paymentStatus", "statusDoPagamento"
    )
    val receivedBy = anyString("pagamento.recebidoPor", "recebidoPor", "quemRecebe", "recebedor", "paymentReceiver")
    val changeFor = anyDouble("pagamento.trocoPara", "trocoPara", "troco", "valorTrocoPara", "changeFor") ?: 0.0
    val requiresMachine = anyBoolean("pagamento.precisaMaquininha", "precisaMaquininha", "maquininhaNecessaria", "requiresMachine", "cartaoPresencial") == true
    val deliveryCode = anyString("codigoEntrega", "codigoConfirmacaoEntrega", "deliveryCode", "codigoCliente", "pinEntrega", "pin", "pedido.codigoEntrega")
    val amountToCollect = anyDouble("pagamento.valorReceberCliente", "valores.valorReceberCliente", "valorReceberCliente", "valorCobrarCliente", "trocoValorCobrar", "cobrarDoCliente") ?: clientTotal
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
    val dropoff = anyAddressString()
    val km = anyDouble("kmTotal", "distanciaKm", "distanciaTotal", "distancia", "calculo.kmTotalEstimado", "calculo.kmTotal", "calculo.distanciaKm") ?: 0.0
    val minutes = anyDouble("tempoTotalMin", "tempoMin", "tempoEstimado", "tempo", "calculo.tempoTotalMin", "calculo.tempoMin") ?: 0.0
    val pickupLat = anyCoordinate("latLoja", "lojaLat", "latitudeLoja", "pickupLat", "pickupLatitude", "coletaLat", "latColeta", "origemLat")
    val pickupLng = anyCoordinate("lngLoja", "lojaLng", "longitudeLoja", "pickupLng", "pickupLongitude", "coletaLng", "lngColeta", "origemLng", "lonLoja")
    val dropoffLat = anyCoordinate("latEntrega", "entregaLat", "clienteLat", "dropoffLat", "dropoffLatitude", "destinationLat", "destinoLat") ?: nestedCoordinate("endereco", "lat", "latitude")
    val dropoffLng = anyCoordinate("lngEntrega", "entregaLng", "clienteLng", "dropoffLng", "dropoffLongitude", "destinationLng", "destinoLng", "lonEntrega") ?: nestedCoordinate("endereco", "lng", "lon", "longitude")
    val routeOrders = toRouteOrders()
    if (collectionName == "rotas_entrega" && routeOrders.isNotEmpty() && routeOrders.all { it.terminal }) return null
    val routeId = anyString("rotaId", "routeId", "rotaAtualId", "missaoAtualId").ifBlank { if (collectionName == "rotas_entrega") id else "" }
    val offerType = anyString("tipoOferta", "offerType", "tipo", "tipoDespacho", "acaoOferta")
    val expires = anyTimestamp("ofertaExpiraEm", "expiraEm", "expiresAt", "validadeOferta", "offerExpiresAt", "deadlineAt")?.toDate()?.time ?: 0L
    val created = anyTimestamp("ofertaCriadaEm", "createdAt", "criadoEm", "created_at")?.toDate()?.time ?: 0L
    val activeFlag = anyBoolean("ativa", "ativo", "active", "ofertaAtiva", "isActive")
    val activeRouteOrders = routeOrders.filterNot { it.terminal }
    val readyCount = anyDouble("pedidosProntos", "readyCount", "quantidadeProntos", "qtdProntos")?.toInt() ?: activeRouteOrders.count { it.ready }
    val orderCount = (anyDouble("quantidadePedidos", "qtdPedidos", "orderCount", "stops", "paradas")?.toInt() ?: activeRouteOrders.size.takeIf { it > 0 } ?: 1).coerceAtLeast(1)
    val releaseStatus = anyString("statusSaida", "saida.status", "retirada.status", "liberacaoSaida.status", "pickupReleaseStatus")
    val pickupReleaseAllowed = anyBoolean("saidaLiberada", "liberadaParaSaida", "liberadoParaSaida", "retiradaLiberada", "pickupReleaseAllowed") == true || releaseStatus.upperOrTrim() in PICKUP_RELEASED_STATUSES || rawStatus.upperOrTrim() in PICKUP_RELEASED_STATUSES
    val routeLocked = anyBoolean("rotaTravada", "locked", "routeLocked", "bloquearNovosPedidos") == true || rawStatus.upperOrTrim() in ROUTE_LOCKED_STATUSES
    val isRouteAddition = offerType.upperOrTrim().let { it.contains("ADICAO") || it.contains("ADIÇÃO") || it.contains("ADD_ROUTE") || it.contains("ROTA_ADICIONAL") || it.contains("PEDIDO_ADICIONADO") }

    return DriverRide(
        id = id,
        collectionName = collectionName,
        status = normalizeUiStatus(rawStatus, collectionName),
        rawStatus = rawStatus,
        value = DriverRepository.formatCurrency(number),
        valueNumber = number,
        distance = if (km > 0.0) "${String.format(Locale("pt", "BR"), "%.1f", km)} km" else anyString("distance"),
        duration = if (minutes > 0.0) "${minutes.toInt()} min" else anyString("duration", "estimatedTime"),
        pickup = pickup,
        dropoff = dropoff,
        neighborhood = anyString("bairro", "bairroEntrega", "regiao", "neighborhood"),
        assignedDriverId = assigned,
        targetDriverId = target,
        broadcast = anyBoolean("broadcast", "paraTodos", "ofertaParaTodos") ?: false,
        customerName = anyString("customerName", "clientName", "clienteNome", "nomeCliente", "nome"),
        orderCode = anyString("orderCode", "orderId", "numeroPedido", "codigoPedido").ifBlank { id.takeLast(6).uppercase() },
        stops = (anyDouble("stops", "paradas", "quantidadePedidos") ?: 1.0).toInt().coerceAtLeast(1),
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
        paymentStatus = paymentStatus,
        receivedBy = receivedBy,
        changeForNumber = changeFor,
        requiresMachine = requiresMachine,
        deliveryCode = deliveryCode.onlyDigitsLocal(),
        offerExpiresAtMillis = expires,
        offerCreatedAtMillis = created,
        offerInactive = activeFlag == false,
        offerType = offerType,
        isRouteAddition = isRouteAddition,
        routeId = routeId,
        routeOrderCount = orderCount,
        routeReadyCount = readyCount,
        routeReleaseCode = anyString("codigoRetirada", "codigoRota", "pickupCode", "codigoLiberacao", "retirada.codigo"),
        pickupReleaseStatus = releaseStatus,
        pickupReleaseAllowed = pickupReleaseAllowed,
        routeLocked = routeLocked,
        routeOrders = activeRouteOrders.ifEmpty { routeOrders.filterNot { it.terminal } }
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
    if (!path.contains('.')) return runCatching { get(path) }.getOrNull()
    var current: Any? = runCatching { data }.getOrNull()
    for (part in path.split('.')) {
        current = when (current) {
            is Map<*, *> -> current[part]
            else -> return null
        }
    }
    return current
}


private fun String.toFlexibleMillisOrNull(): Long? {
    val value = trim()
    if (value.isBlank()) return null
    value.toLongOrNull()?.let { return it }
    val candidates = buildList {
        add(value)
        if (value.contains('T') && !value.endsWith('Z')) add("${value}Z")
        if (value.count { it == ':' } == 1 && value.contains('T')) add("${value}:00Z")
        if (!value.contains('T')) add("${value}T00:00:00Z")
    }
    return candidates.firstNotNullOfOrNull { candidate ->
        runCatching { Instant.parse(candidate).toEpochMilli() }.getOrNull()
    }
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
        val value = getDeep(key)
        when (value) {
            is Timestamp -> return value
            is Date -> return Timestamp(value)
            is Number -> return Timestamp(Date(value.toLong()))
            is String -> value.toFlexibleMillisOrNull()?.let { return Timestamp(Date(it)) }
        }
        runCatching { getTimestamp(key) }.getOrNull()?.let { return it }
    }
    return null
}

private fun DocumentSnapshot.anyStringList(vararg keys: String): List<String> {
    val result = linkedSetOf<String>()
    for (key in keys) {
        val value = getDeep(key)
        when (value) {
            is List<*> -> value.mapNotNullTo(result) { it?.toString() }
            is String -> if (value.isNotBlank()) result.add(value)
            is Map<*, *> -> value.values.mapNotNullTo(result) { it?.toString() }
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
    return if (bairro.isNotBlank()) "Região: $bairro" else ""
}

private fun DocumentSnapshot.hasAnyValue(vararg keys: String): Boolean {
    return keys.any { key -> getDeep(key) != null }
}

private fun Long.isTodayMillis(): Boolean {
    val now = Calendar.getInstance()
    val date = Calendar.getInstance().apply { timeInMillis = this@isTodayMillis }
    return now.get(Calendar.YEAR) == date.get(Calendar.YEAR) &&
        now.get(Calendar.DAY_OF_YEAR) == date.get(Calendar.DAY_OF_YEAR)
}

private fun Long.isWithinLastDays(days: Int): Boolean {
    val now = System.currentTimeMillis()
    val diff = now - this
    return diff >= 0 && diff <= days * 24L * 60L * 60L * 1000L
}




private fun Map<*, *>.firstString(vararg keys: String): String {
    for (key in keys) {
        val direct = this[key]
        if (direct != null) return direct.toString().trim()
        if (key.contains('.')) {
            val parts = key.split('.')
            var current: Any? = this
            for (part in parts) current = (current as? Map<*, *>)?.get(part)
            if (current != null) return current.toString().trim()
        }
    }
    return ""
}

private fun Map<*, *>.firstDouble(vararg keys: String): Double? {
    for (key in keys) {
        val value = firstString(key)
        if (value.isNotBlank()) value.toMoneyDouble()?.let { return it }
        val direct = this[key]
        if (direct is Number) return direct.toDouble()
    }
    return null
}

private fun Map<*, *>.firstBoolean(vararg keys: String): Boolean? {
    for (key in keys) {
        when (val value = this[key]) {
            is Boolean -> return value
            is String -> {
                val normalized = value.upperOrTrim()
                if (normalized in setOf("TRUE", "SIM", "YES", "1")) return true
                if (normalized in setOf("FALSE", "NAO", "NÃO", "NO", "0")) return false
            }
        }
    }
    return null
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
