package com.rodriguesacai.entregador

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.Space
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.messaging.FirebaseMessaging
import java.net.URL
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.Executors
import kotlin.math.roundToInt

private const val GREEN = 0xFF15A05F.toInt()
private const val GREEN_DARK = 0xFF087A45.toInt()
private const val GREEN_SOFT = 0xFFE9F8EF.toInt()
private const val BG = 0xFFF6F8F7.toInt()
private const val CARD = 0xFFFFFFFF.toInt()
private const val TEXT = 0xFF111827.toInt()
private const val MUTED = 0xFF6B7280.toInt()
private const val LINE = 0xFFE5E7EB.toInt()
private const val RED = 0xFFDC2626.toInt()
private const val ORANGE = 0xFFF59E0B.toInt()

class MainActivity : Activity() {
    private val db by lazy { FirebaseFirestore.getInstance() }
    private val prefs by lazy { getSharedPreferences("up_driver", MODE_PRIVATE) }
    private val money = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
    private val executor = Executors.newSingleThreadExecutor()

    private lateinit var root: LinearLayout
    private var driver: Driver? = null
    private var driverCollection: String = "entregadores"
    private var activeTab: Tab = Tab.HOME
    private var rides: MutableList<Ride> = mutableListOf()
    private var banners: MutableList<Banner> = mutableListOf()
    private var notices: MutableList<Notice> = mutableListOf()
    private var updateInfo: UpdateInfo? = null
    private var loading = false
    private var valuesHidden = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = Color.WHITE
        window.navigationBarColor = Color.WHITE
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
        ensureNotificationPermission()
        createRoot()
        restoreSession()
    }

    override fun onDestroy() {
        super.onDestroy()
        executor.shutdownNow()
    }

    private fun createRoot() {
        root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(BG)
            layoutParams = LinearLayout.LayoutParams(-1, -1)
        }
        setContentView(root)
    }

    private fun restoreSession() {
        val id = prefs.getString("driverId", null)
        val collection = prefs.getString("driverCollection", null)
        valuesHidden = prefs.getBoolean("valuesHidden", false)
        if (id.isNullOrBlank() || collection.isNullOrBlank()) {
            renderLogin()
            return
        }
        loading = true
        showLoading("Carregando sua conta...")
        db.collection(collection).document(id).get()
            .addOnSuccessListener { doc ->
                loading = false
                if (doc.exists()) {
                    driver = doc.toDriver(collection)
                    driverCollection = collection
                    loadAllRealData { renderShell() }
                } else {
                    prefs.edit().clear().apply()
                    renderLogin()
                }
            }
            .addOnFailureListener {
                loading = false
                renderLogin("Não foi possível carregar sua conta agora.")
            }
    }

    private fun renderLogin(error: String? = null) {
        activeTab = Tab.HOME
        root.removeAllViews()
        val scroll = ScrollView(this)
        val content = vertical().apply { setPadding(dp(22), dp(26), dp(22), dp(22)) }
        scroll.addView(content)
        root.addView(scroll, LinearLayout.LayoutParams(-1, -1))

        content.addView(text("up", 42, GREEN, true).apply { gravity = Gravity.CENTER })
        content.addView(text("entregas", 16, GREEN_DARK, true).apply { gravity = Gravity.CENTER })
        content.addView(space(30))
        content.addView(text("Bem-vindo", 30, TEXT, true))
        content.addView(text("Acesse sua conta para receber corridas reais da operação.", 15, MUTED, false))
        content.addView(space(20))
        content.addView(card().apply {
            addView(text("Entrar no app", 20, TEXT, true))
            addView(text("Use CPF, telefone ou e-mail cadastrado no painel.", 14, MUTED, false))
            addView(space(16))
            val idField = input("CPF, telefone ou e-mail")
            val passField = input("Senha")
            passField.inputType = 0x00000081
            addView(idField)
            addView(space(10))
            addView(passField)
            addView(space(6))
            addView(text("Esqueci minha senha", 13, GREEN_DARK, true).apply {
                gravity = Gravity.END
                setOnClickListener { toast("Solicite uma nova senha ao gestor da operação.") }
            })
            error?.let {
                addView(space(8))
                addView(text(it, 13, RED, true))
            }
            addView(space(16))
            addView(primaryButton("Entrar") {
                hideKeyboard(passField)
                val identifier = idField.text.toString().trim()
                val password = passField.text.toString().trim()
                if (identifier.isBlank() || password.isBlank()) {
                    toast("Preencha CPF/telefone/e-mail e senha.")
                } else {
                    login(identifier, password)
                }
            })
            addView(space(10))
            addView(outlineButton("Solicitar cadastro") { renderSignup() })
        })
        content.addView(space(16))
        content.addView(text("Cadastro sujeito à aprovação do gestor.", 13, MUTED, false).apply { gravity = Gravity.CENTER })
    }

    private fun renderSignup() {
        root.removeAllViews()
        val scroll = ScrollView(this)
        val content = vertical().apply { setPadding(dp(22), dp(24), dp(22), dp(22)) }
        scroll.addView(content)
        root.addView(scroll, LinearLayout.LayoutParams(-1, -1))
        content.addView(backHeader("Solicitar cadastro") { renderLogin() })
        content.addView(card().apply {
            val nome = input("Nome completo")
            val cpf = input("CPF")
            val telefone = input("Telefone")
            val email = input("E-mail")
            val pix = input("Chave Pix")
            val senha = input("Senha desejada")
            senha.inputType = 0x00000081
            addView(text("Dados do entregador", 19, TEXT, true))
            addView(text("Sua solicitação ficará pendente até aprovação.", 14, MUTED, false))
            listOf(nome, cpf, telefone, email, pix, senha).forEachIndexed { index, field ->
                addView(space(if (index == 0) 16 else 10))
                addView(field)
            }
            addView(space(18))
            addView(primaryButton("Enviar solicitação") {
                val data = hashMapOf<String, Any>(
                    "nome" to nome.text.toString().trim(),
                    "cpf" to cpf.text.toString().trim(),
                    "telefone" to telefone.text.toString().trim(),
                    "email" to email.text.toString().trim(),
                    "pix" to pix.text.toString().trim(),
                    "senha" to senha.text.toString().trim(),
                    "status" to "PENDENTE",
                    "aprovado" to false,
                    "criadoEm" to FieldValue.serverTimestamp()
                )
                if (data["nome"].toString().isBlank() || data["telefone"].toString().isBlank() || data["senha"].toString().isBlank()) {
                    toast("Nome, telefone e senha são obrigatórios.")
                    return@primaryButton
                }
                db.collection("entregadores_pendentes").add(data)
                    .addOnSuccessListener {
                        toast("Solicitação enviada.")
                        renderPending()
                    }
                    .addOnFailureListener { toast("Não foi possível enviar agora.") }
            })
        })
    }

    private fun renderPending() {
        root.removeAllViews()
        val content = vertical().apply { setPadding(dp(24), dp(60), dp(24), dp(24)); gravity = Gravity.CENTER_HORIZONTAL }
        root.addView(content, LinearLayout.LayoutParams(-1, -1))
        content.addView(text("Cadastro em análise", 26, TEXT, true).apply { gravity = Gravity.CENTER })
        content.addView(text("O gestor precisa aprovar seu acesso antes de você receber corridas.", 15, MUTED, false).apply { gravity = Gravity.CENTER })
        content.addView(space(24))
        content.addView(primaryButton("Voltar para login") { renderLogin() })
    }

    private fun login(identifier: String, password: String) {
        showLoading("Validando acesso...")
        val collections = listOf("entregadores", "motoboys", "drivers")
        val fields = listOf("cpf", "telefone", "email", "login")
        fun tryNext(ci: Int, fi: Int) {
            if (ci >= collections.size) {
                renderLogin("Entregador não encontrado ou senha inválida.")
                return
            }
            val collection = collections[ci]
            val field = fields[fi]
            db.collection(collection).whereEqualTo(field, identifier).limit(1).get()
                .addOnSuccessListener { qs ->
                    if (!qs.isEmpty) {
                        val doc = qs.documents.first()
                        val savedPass = doc.getString("senha") ?: doc.getString("password") ?: doc.getString("senhaApp") ?: ""
                        val approved = doc.getBoolean("aprovado") ?: (doc.getString("status")?.uppercase() !in listOf("PENDENTE", "BLOQUEADO", "REPROVADO"))
                        if (savedPass == password || savedPass.isBlank()) {
                            if (!approved) {
                                renderPending()
                                return@addOnSuccessListener
                            }
                            driver = doc.toDriver(collection)
                            driverCollection = collection
                            prefs.edit().putString("driverId", doc.id).putString("driverCollection", collection).apply()
                            saveFcmToken()
                            loadAllRealData { renderShell() }
                        } else {
                            renderLogin("Senha inválida.")
                        }
                    } else {
                        val nextField = fi + 1
                        if (nextField < fields.size) tryNext(ci, nextField) else tryNext(ci + 1, 0)
                    }
                }
                .addOnFailureListener {
                    val nextField = fi + 1
                    if (nextField < fields.size) tryNext(ci, nextField) else tryNext(ci + 1, 0)
                }
        }
        tryNext(0, 0)
    }

    private fun loadAllRealData(done: () -> Unit) {
        loading = true
        rides.clear(); banners.clear(); notices.clear(); updateInfo = null
        loadRides { loadBanners { loadNotices { loadUpdateInfo { loading = false; done() } } } }
    }

    private fun renderShell() {
        root.removeAllViews()
        val main = FrameLayout(this)
        root.addView(main, LinearLayout.LayoutParams(-1, 0, 1f))
        val scroll = ScrollView(this)
        val content = vertical().apply { setPadding(dp(18), dp(18), dp(18), dp(18)) }
        scroll.addView(content)
        main.addView(scroll)
        when (activeTab) {
            Tab.HOME -> screenHome(content)
            Tab.RIDES -> screenRides(content)
            Tab.WALLET -> screenWallet(content)
            Tab.NOTICES -> screenNotices(content)
            Tab.MORE -> screenMore(content)
        }
        root.addView(bottomNav(), LinearLayout.LayoutParams(-1, dp(72)))
    }

    private fun screenHome(content: LinearLayout) {
        val d = driver
        content.addView(header())
        content.addView(space(12))
        content.addView(statusButton())
        content.addView(space(12))
        content.addView(earningsCard())
        content.addView(space(14))
        content.addView(bannerCard())
        content.addView(space(14))
        content.addView(quickGrid())
        content.addView(space(16))
        val active = rides.filter { it.isMine(d?.id) && !it.isFinished() }
        content.addView(text("Corrida em andamento", 18, TEXT, true))
        content.addView(space(8))
        if (active.isEmpty()) {
            content.addView(emptyCard("Nenhuma corrida em andamento.", "Quando uma corrida real for aceita, ela aparecerá aqui."))
        } else {
            active.forEach { content.addView(rideCard(it, compact = true)) }
        }
    }

    private fun header(): View {
        val d = driver
        return horizontal().apply {
            gravity = Gravity.CENTER_VERTICAL
            addView(avatar(d?.fotoUrl, d?.nome ?: "Entregador"), LinearLayout.LayoutParams(dp(54), dp(54)))
            addView(vertical().apply {
                setPadding(dp(12), 0, 0, 0)
                addView(text("Olá, ${firstName(d?.nome)}", 22, TEXT, true))
                addView(text(if (d?.statusDisponivel == true) "Pronto para receber corridas" else "Ative disponibilidade para operar", 13, MUTED, false))
            }, LinearLayout.LayoutParams(0, -2, 1f))
            addView(circleIcon("🔔") { activeTab = Tab.NOTICES; renderShell() })
            addView(space(8))
            addView(circleIcon("💬") { toast("Chat operacional será aberto quando houver conversa real vinculada.") })
        }
    }

    private fun statusButton(): View {
        val d = driver
        val restricted = isRestricted()
        val available = d?.statusDisponivel == true && !restricted
        val label = when {
            restricted -> "Restrição"
            available -> "Disponível"
            else -> "Indisponível"
        }
        val color = when {
            restricted -> RED
            available -> GREEN
            else -> 0xFF374151.toInt()
        }
        return Button(this).apply {
            text = "●  $label    ˅"
            textSize = 17f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.WHITE)
            background = rounded(color, dp(18))
            minHeight = dp(58)
            setOnClickListener {
                if (restricted) {
                    renderRestrictions()
                } else {
                    updateAvailability(!available)
                }
            }
        }
    }

    private fun earningsCard(): View {
        val finalized = rides.filter { it.isMine(driver?.id) && it.isFinished() }
        val total = finalized.sumOf { it.valor }
        return card().apply {
            val row = horizontal().apply { gravity = Gravity.CENTER_VERTICAL }
            row.addView(vertical().apply {
                addView(text("Ganhos de hoje", 14, MUTED, true))
                addView(text(if (valuesHidden) "R$ •••••" else money.format(total), 28, TEXT, true))
            }, LinearLayout.LayoutParams(0, -2, 1f))
            row.addView(Button(context).apply {
                text = if (valuesHidden) "Mostrar" else "Ocultar"
                textSize = 12f
                setTextColor(GREEN_DARK)
                background = rounded(GREEN_SOFT, dp(14))
                setOnClickListener {
                    valuesHidden = !valuesHidden
                    prefs.edit().putBoolean("valuesHidden", valuesHidden).apply()
                    renderShell()
                }
            })
            addView(row)
            addView(space(12))
            addView(horizontal().apply {
                addView(metric("Corridas", rides.count { it.isMine(driver?.id) }.toString()), LinearLayout.LayoutParams(0, -2, 1f))
                addView(metric("Finalizadas", finalized.size.toString()), LinearLayout.LayoutParams(0, -2, 1f))
            })
        }
    }

    private fun bannerCard(): View {
        val banner = banners.firstOrNull()
        return card(bgColor = GREEN_DARK).apply {
            if (banner == null) {
                addView(text("Carrossel do App", 13, 0xFFD1FAE5.toInt(), true))
                addView(text("Nenhum banner cadastrado", 22, Color.WHITE, true))
                addView(text("Cadastre banners reais no painel gestor para aparecer aqui.", 14, 0xFFE5FFF0.toInt(), false))
            } else {
                addView(text(banner.tag.ifBlank { "NOVIDADES" }, 12, 0xFFD1FAE5.toInt(), true))
                addView(text(banner.titulo, 22, Color.WHITE, true))
                addView(text(banner.descricao, 14, 0xFFE5FFF0.toInt(), false))
                if (banner.acao.isNotBlank()) {
                    addView(space(10))
                    addView(outlineLightButton("Abrir") { openUrl(banner.acao) })
                }
            }
        }
    }

    private fun quickGrid(): View {
        return vertical().apply {
            addView(horizontal().apply {
                addView(quick("Histórico", "Ver corridas") { renderHistory() }, LinearLayout.LayoutParams(0, dp(96), 1f))
                addView(space(10))
                addView(quick("Ganhos", "Resumo financeiro") { activeTab = Tab.WALLET; renderShell() }, LinearLayout.LayoutParams(0, dp(96), 1f))
            })
            addView(space(10))
            addView(horizontal().apply {
                addView(quick("Mapa", "Abrir região") { openMaps() }, LinearLayout.LayoutParams(0, dp(96), 1f))
                addView(space(10))
                addView(quick("Suporte", "Fale conosco") { openWhatsApp() }, LinearLayout.LayoutParams(0, dp(96), 1f))
            })
        }
    }

    private fun screenRides(content: LinearLayout) {
        content.addView(topTitle("Corridas", "Atualizadas com dados reais do Firebase."))
        content.addView(space(10))
        content.addView(primaryButton("Atualizar corridas") { loadAllRealData { renderShell() } })
        content.addView(space(16))
        val available = rides.filter { it.isAvailableFor(driver?.id) }
        val mine = rides.filter { it.isMine(driver?.id) && !it.isFinished() }
        content.addView(text("Disponíveis", 18, TEXT, true))
        content.addView(space(8))
        if (available.isEmpty()) content.addView(emptyCard("Nenhuma corrida disponível.", "O app não cria corridas falsas.")) else available.forEach { content.addView(rideCard(it, compact = false)) }
        content.addView(space(16))
        content.addView(text("Minhas corridas", 18, TEXT, true))
        content.addView(space(8))
        if (mine.isEmpty()) content.addView(emptyCard("Você não tem corrida ativa.", "Aceite uma corrida real para ver o fluxo.")) else mine.forEach { content.addView(rideCard(it, compact = false)) }
    }

    private fun rideCard(ride: Ride, compact: Boolean): View {
        return card().apply {
            addView(horizontal().apply {
                addView(text(ride.numero.ifBlank { ride.id.take(8) }, 17, TEXT, true), LinearLayout.LayoutParams(0, -2, 1f))
                addView(chip(humanStatus(ride.status), if (ride.isFinished()) GREEN else ORANGE))
            })
            addView(space(8))
            addView(text("Coleta: ${ride.coleta.ifBlank { "não informado" }}", 14, TEXT, false))
            addView(text("Entrega: ${ride.entrega.ifBlank { "endereço liberado conforme etapa" }}", 14, MUTED, false))
            addView(space(8))
            addView(horizontal().apply {
                addView(metric("Valor", if (valuesHidden) "R$ •••" else money.format(ride.valor)), LinearLayout.LayoutParams(0, -2, 1f))
                addView(metric("Distância", ride.distancia.ifBlank { "—" }), LinearLayout.LayoutParams(0, -2, 1f))
            })
            if (!compact) {
                addView(space(12))
                addView(actionsForRide(ride))
            }
        }
    }

    private fun actionsForRide(ride: Ride): View {
        return vertical().apply {
            when {
                ride.isAvailableFor(driver?.id) -> {
                    addView(primaryButton("Aceitar corrida") { updateRide(ride, "ACEITA") })
                    addView(space(8))
                    addView(outlineButton("Recusar") { updateRide(ride, "RECUSADA") })
                }
                ride.isMine(driver?.id) && !ride.isFinished() -> {
                    val next = nextStatus(ride.status)
                    addView(primaryButton(next.second) { updateRide(ride, next.first) })
                    addView(space(8))
                    addView(outlineButton("Registrar ocorrência") { renderOccurrence(ride) })
                    addView(space(8))
                    addView(outlineButton("Abrir navegação") { openMaps(ride.entrega.ifBlank { ride.coleta }) })
                }
                else -> addView(text("Sem ações disponíveis para esta corrida.", 13, MUTED, false))
            }
        }
    }

    private fun screenWallet(content: LinearLayout) {
        val my = rides.filter { it.isMine(driver?.id) }
        val finalized = my.filter { it.isFinished() }
        val total = finalized.sumOf { it.valor }
        content.addView(topTitle("Carteira", "Resumo financeiro real das suas corridas."))
        content.addView(space(12))
        content.addView(card().apply {
            addView(text("Saldo calculado", 14, MUTED, true))
            addView(text(if (valuesHidden) "R$ •••••" else money.format(total), 32, TEXT, true))
            addView(space(12))
            addView(horizontal().apply {
                addView(metric("Finalizadas", finalized.size.toString()), LinearLayout.LayoutParams(0, -2, 1f))
                addView(metric("Em aberto", my.count { !it.isFinished() }.toString()), LinearLayout.LayoutParams(0, -2, 1f))
            })
        })
        content.addView(space(12))
        content.addView(card().apply {
            addView(text("Recebimento", 18, TEXT, true))
            addView(text("Pix: ${driver?.pix?.ifBlank { "não cadastrado" } ?: "não cadastrado"}", 14, MUTED, false))
            addView(text("Banco: ${driver?.banco?.ifBlank { "não cadastrado" } ?: "não cadastrado"}", 14, MUTED, false))
            addView(space(12))
            addView(outlineButton("Solicitar alteração") { renderPaymentEdit() })
        })
    }

    private fun screenNotices(content: LinearLayout) {
        content.addView(topTitle("Notificações", "Avisos reais enviados pela operação."))
        content.addView(space(10))
        content.addView(primaryButton("Atualizar avisos") { loadNotices { renderShell() } })
        content.addView(space(12))
        if (notices.isEmpty()) {
            content.addView(emptyCard("Nenhuma notificação real.", "Quando o gestor enviar avisos, eles aparecerão aqui."))
        } else {
            notices.forEach { n ->
                content.addView(card().apply {
                    addView(text(n.titulo, 17, TEXT, true))
                    addView(text(n.mensagem, 14, MUTED, false))
                    if (n.data.isNotBlank()) addView(text(n.data, 12, MUTED, false))
                })
            }
        }
    }

    private fun screenMore(content: LinearLayout) {
        content.addView(topTitle("Mais", "Conta, permissões e operação."))
        content.addView(space(12))
        content.addView(menuItem("Perfil e conta") { renderProfile() })
        content.addView(menuItem("Permissões do app") { renderRestrictions() })
        content.addView(menuItem("Navegação padrão") { renderNavigationSettings() })
        content.addView(menuItem("Atualização do app") { renderUpdateScreen() })
        content.addView(menuItem("Histórico completo") { renderHistory() })
        content.addView(menuItem("Sair") { logout() })
    }

    private fun renderProfile() {
        root.removeAllViews()
        val content = page("Perfil") { renderShell() }
        content.addView(card().apply {
            val d = driver
            addView(horizontal().apply {
                addView(avatar(d?.fotoUrl, d?.nome ?: "Entregador"), LinearLayout.LayoutParams(dp(62), dp(62)))
                addView(vertical().apply {
                    setPadding(dp(12), 0, 0, 0)
                    addView(text(d?.nome ?: "Entregador", 20, TEXT, true))
                    addView(text("Status: ${if (d?.statusDisponivel == true) "Disponível" else "Indisponível"}", 14, MUTED, false))
                }, LinearLayout.LayoutParams(0, -2, 1f))
            })
            addView(space(14))
            addView(text("Telefone: ${d?.telefone.orEmpty().ifBlank { "não informado" }}", 14, MUTED, false))
            addView(text("E-mail: ${d?.email.orEmpty().ifBlank { "não informado" }}", 14, MUTED, false))
            addView(space(12))
            addView(outlineButton("Solicitar alteração de dados") { renderDataChange() })
        })
    }

    private fun renderDataChange() {
        val content = page("Alterar dados") { renderProfile() }
        content.addView(card().apply {
            val telefone = input("Novo telefone")
            val email = input("Novo e-mail")
            addView(text("Solicitação ao gestor", 18, TEXT, true))
            addView(telefone); addView(space(10)); addView(email); addView(space(14))
            addView(primaryButton("Enviar solicitação") {
                val d = driver ?: return@primaryButton
                db.collection("solicitacoes_entregadores").add(mapOf(
                    "entregadorUid" to d.id,
                    "tipo" to "DADOS_PESSOAIS",
                    "telefone" to telefone.text.toString().trim(),
                    "email" to email.text.toString().trim(),
                    "status" to "PENDENTE",
                    "criadoEm" to FieldValue.serverTimestamp()
                )).addOnSuccessListener { toast("Solicitação enviada."); renderProfile() }
            })
        })
    }

    private fun renderPaymentEdit() {
        val content = page("Recebimento") { activeTab = Tab.WALLET; renderShell() }
        content.addView(card().apply {
            val pix = input("Chave Pix").also { it.setText(driver?.pix.orEmpty()) }
            val banco = input("Banco").also { it.setText(driver?.banco.orEmpty()) }
            addView(text("Dados de repasse", 18, TEXT, true))
            addView(text("A conta precisa estar no nome do titular.", 13, MUTED, false))
            addView(space(12)); addView(pix); addView(space(10)); addView(banco); addView(space(14))
            addView(primaryButton("Salvar e enviar para aprovação") {
                val d = driver ?: return@primaryButton
                val data = mapOf(
                    "entregadorUid" to d.id,
                    "tipo" to "RECEBIMENTO",
                    "pix" to pix.text.toString().trim(),
                    "banco" to banco.text.toString().trim(),
                    "status" to "PENDENTE",
                    "criadoEm" to FieldValue.serverTimestamp()
                )
                db.collection("solicitacoes_entregadores").add(data)
                    .addOnSuccessListener { toast("Solicitação enviada."); activeTab = Tab.WALLET; renderShell() }
            })
        })
    }

    private fun renderRestrictions() {
        val content = page("Permissões") { renderShell() }
        content.addView(card().apply {
            addView(text("Checklist operacional", 20, TEXT, true))
            addView(checkLine("Localização", hasLocationPermission()))
            addView(checkLine("GPS ativo", isGpsOn()))
            addView(checkLine("Notificações", hasNotificationPermission()))
            addView(checkLine("Bateria acima de 10%", batteryPercent() > 10))
            addView(space(12))
            addView(primaryButton("Abrir ajustes do app") { startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:$packageName"))) })
        })
    }

    private fun renderNavigationSettings() {
        val content = page("Navegação") { renderShell() }
        val current = prefs.getString("nav", "padrao") ?: "padrao"
        content.addView(card().apply {
            addView(text("App padrão de rota", 20, TEXT, true))
            listOf("padrao" to "Padrão do celular", "google" to "Google Maps", "waze" to "Waze").forEach { (key, label) ->
                addView(menuItem(if (current == key) "✓ $label" else label) {
                    prefs.edit().putString("nav", key).apply()
                    toast("Navegação alterada.")
                    renderNavigationSettings()
                })
            }
        })
    }

    private fun renderUpdateScreen() {
        val content = page("Atualização do app") { renderShell() }
        content.addView(card().apply {
            addView(text("Versão instalada", 14, MUTED, true))
            addView(text("6.0.0-nativo-limpo", 24, TEXT, true))
            addView(space(12))
            val info = updateInfo
            if (info == null) {
                addView(text("Nenhuma configuração real de atualização encontrada no Firebase.", 14, MUTED, false))
            } else {
                addView(text("Última versão: ${info.latestVersion.ifBlank { "não informada" }}", 16, TEXT, true))
                addView(text(info.message.ifBlank { "Sem mensagem adicional." }, 14, MUTED, false))
                if (info.required) addView(text("Atualização obrigatória", 13, RED, true))
                if (info.url.isNotBlank()) {
                    addView(space(12))
                    addView(primaryButton("Baixar atualização") { openUrl(info.url) })
                }
            }
        })
        content.addView(space(10))
        content.addView(primaryButton("Verificar agora") { loadUpdateInfo { renderUpdateScreen() } })
    }

    private fun renderHistory() {
        val content = page("Histórico") { renderShell() }
        val my = rides.filter { it.isMine(driver?.id) || it.status.uppercase() in listOf("RECUSADA", "EXPIRADA") }
        if (my.isEmpty()) content.addView(emptyCard("Histórico vazio.", "Corridas reais finalizadas, recusadas ou expiradas aparecerão aqui."))
        my.sortedByDescending { it.dataMillis }.forEach { content.addView(rideCard(it, compact = true)) }
    }

    private fun renderOccurrence(ride: Ride) {
        val content = page("Ocorrência") { activeTab = Tab.RIDES; renderShell() }
        val reasons = listOf("Cliente não atende", "Endereço divergente", "Local inseguro", "Pagamento pendente", "Pedido danificado", "Cliente ausente", "Aguardando cliente", "Outro motivo")
        content.addView(card().apply {
            addView(text("Registrar ocorrência no local", 18, TEXT, true))
            reasons.forEach { reason ->
                addView(menuItem(reason) {
                    ride.refCollection?.let { col ->
                        db.collection(col).document(ride.id).update(mapOf(
                            "ocorrencia" to reason,
                            "statusOcorrencia" to "PENDENTE_SOLUCAO",
                            "ocorrenciaEm" to FieldValue.serverTimestamp()
                        )).addOnSuccessListener { toast("Ocorrência registrada."); loadAllRealData { activeTab = Tab.RIDES; renderShell() } }
                    }
                })
            }
        })
    }

    private fun page(title: String, back: () -> Unit): LinearLayout {
        root.removeAllViews()
        val scroll = ScrollView(this)
        val content = vertical().apply { setPadding(dp(18), dp(18), dp(18), dp(22)) }
        scroll.addView(content)
        root.addView(scroll, LinearLayout.LayoutParams(-1, -1))
        content.addView(backHeader(title, back))
        content.addView(space(12))
        return content
    }

    private fun loadRides(done: () -> Unit) {
        val collections = listOf("corridas", "pedidos", "rotas", "orders")
        val all = mutableListOf<Ride>()
        fun next(index: Int) {
            if (index >= collections.size) {
                rides = all.distinctBy { it.refCollection + it.id }.toMutableList()
                done()
                return
            }
            val col = collections[index]
            db.collection(col).limit(80).get()
                .addOnSuccessListener { qs ->
                    qs.documents.mapNotNullTo(all) { it.toRide(col) }
                    next(index + 1)
                }
                .addOnFailureListener { next(index + 1) }
        }
        next(0)
    }

    private fun loadBanners(done: () -> Unit) {
        val collections = listOf("carrosselApp", "appBanners", "banners")
        val all = mutableListOf<Banner>()
        fun next(index: Int) {
            if (index >= collections.size) { banners = all.filter { it.ativo }.sortedBy { it.ordem }.toMutableList(); done(); return }
            db.collection(collections[index]).limit(20).get()
                .addOnSuccessListener { qs -> qs.documents.mapNotNullTo(all) { it.toBanner() }; next(index + 1) }
                .addOnFailureListener { next(index + 1) }
        }
        next(0)
    }

    private fun loadNotices(done: () -> Unit) {
        val all = mutableListOf<Notice>()
        val collections = listOf("notificacoes", "avisos")
        fun next(index: Int) {
            if (index >= collections.size) { notices = all.toMutableList(); done(); return }
            db.collection(collections[index]).limit(40).get()
                .addOnSuccessListener { qs -> qs.documents.mapNotNullTo(all) { it.toNotice() }; next(index + 1) }
                .addOnFailureListener { next(index + 1) }
        }
        next(0)
    }

    private fun loadUpdateInfo(done: () -> Unit) {
        db.collection("configuracoes").document("appEntregador").get()
            .addOnSuccessListener { doc -> updateInfo = if (doc.exists()) doc.toUpdateInfo() else null; done() }
            .addOnFailureListener { updateInfo = null; done() }
    }

    private fun updateRide(ride: Ride, status: String) {
        val d = driver ?: return
        val col = ride.refCollection ?: return
        val data = mutableMapOf<String, Any>(
            "status" to status,
            "statusPedidoCore" to status,
            "statusAtualizadoEm" to FieldValue.serverTimestamp()
        )
        if (status == "ACEITA") {
            data["entregadorUid"] = d.id
            data["entregadorNome"] = d.nome
            data["aceitaEm"] = FieldValue.serverTimestamp()
        }
        if (status == "FINALIZADA") data["finalizadaEm"] = FieldValue.serverTimestamp()
        db.collection(col).document(ride.id).update(data)
            .addOnSuccessListener { toast("Corrida atualizada."); loadAllRealData { activeTab = Tab.RIDES; renderShell() } }
            .addOnFailureListener { toast("Não foi possível atualizar a corrida.") }
    }

    private fun updateAvailability(enable: Boolean) {
        val d = driver ?: return
        val data = mapOf(
            "online" to enable,
            "disponivel" to enable,
            "statusOperacional" to if (enable) "DISPONIVEL" else "INDISPONIVEL",
            "atualizadoEm" to FieldValue.serverTimestamp()
        )
        db.collection(driverCollection).document(d.id).update(data)
            .addOnSuccessListener { driver = d.copy(statusDisponivel = enable); renderShell() }
            .addOnFailureListener { toast("Não foi possível alterar status.") }
    }

    private fun saveFcmToken() {
        val d = driver ?: return
        FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
            db.collection(driverCollection).document(d.id).update(mapOf("fcmToken" to token, "tokenAtualizadoEm" to FieldValue.serverTimestamp()))
        }
    }

    private fun logout() {
        prefs.edit().clear().apply()
        driver = null
        renderLogin()
    }

    private fun ensureNotificationPermission() {
        if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 55)
        }
    }

    private fun hasNotificationPermission(): Boolean = Build.VERSION.SDK_INT < 33 || ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
    private fun hasLocationPermission(): Boolean = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
    private fun isGpsOn(): Boolean = try { (getSystemService(LOCATION_SERVICE) as LocationManager).isProviderEnabled(LocationManager.GPS_PROVIDER) } catch (_: Exception) { false }
    private fun batteryPercent(): Int = try { val bm = getSystemService(BATTERY_SERVICE) as android.os.BatteryManager; bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) } catch (_: Exception) { 100 }
    private fun isRestricted(): Boolean = batteryPercent() <= 10 || !hasLocationPermission() || !isGpsOn() || !hasNotificationPermission()

    private fun openMaps(query: String = "") {
        val nav = prefs.getString("nav", "padrao") ?: "padrao"
        val encoded = Uri.encode(query.ifBlank { "Rodrigues Açaí e Cia" })
        val uri = when (nav) {
            "waze" -> Uri.parse("https://waze.com/ul?q=$encoded&navigate=yes")
            "google" -> Uri.parse("google.navigation:q=$encoded")
            else -> Uri.parse("geo:0,0?q=$encoded")
        }
        runCatching { startActivity(Intent(Intent.ACTION_VIEW, uri)) }.onFailure { toast("Nenhum app de navegação encontrado.") }
    }

    private fun openWhatsApp() { openUrl("https://wa.me/") }
    private fun openUrl(url: String) { runCatching { startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) }.onFailure { toast("Não foi possível abrir o link.") } }

    private fun showLoading(message: String) {
        root.removeAllViews()
        val box = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; gravity = Gravity.CENTER; setBackgroundColor(BG) }
        box.addView(ProgressBar(this))
        box.addView(space(16))
        box.addView(text(message, 15, MUTED, false).apply { gravity = Gravity.CENTER })
        root.addView(box, LinearLayout.LayoutParams(-1, -1))
    }

    private fun bottomNav(): View = horizontal().apply {
        gravity = Gravity.CENTER
        setPadding(dp(8), dp(6), dp(8), dp(8))
        setBackgroundColor(Color.WHITE)
        Tab.values().forEach { tab ->
            addView(Button(context).apply {
                text = tab.label
                textSize = 11f
                isAllCaps = false
                setTextColor(if (activeTab == tab) GREEN else MUTED)
                background = rounded(if (activeTab == tab) GREEN_SOFT else Color.TRANSPARENT, dp(16))
                setOnClickListener { activeTab = tab; renderShell() }
            }, LinearLayout.LayoutParams(0, -1, 1f))
        }
    }

    private fun topTitle(title: String, subtitle: String): View = vertical().apply { addView(text(title, 28, TEXT, true)); addView(text(subtitle, 14, MUTED, false)) }
    private fun backHeader(title: String, back: () -> Unit): View = horizontal().apply { gravity = Gravity.CENTER_VERTICAL; addView(circleIcon("‹") { back() }); addView(text(title, 22, TEXT, true).apply { setPadding(dp(12), 0, 0, 0) }, LinearLayout.LayoutParams(0, -2, 1f)) }
    private fun checkLine(label: String, ok: Boolean): View = text("${if (ok) "✓" else "!"}  $label", 15, if (ok) GREEN_DARK else RED, true).apply { setPadding(0, dp(8), 0, dp(2)) }
    private fun menuItem(label: String, onClick: () -> Unit): View = card().apply { setPadding(dp(16), dp(14), dp(16), dp(14)); addView(text(label, 16, TEXT, true)); setOnClickListener { onClick() } }
    private fun metric(label: String, value: String): View = vertical().apply { addView(text(value, 18, TEXT, true)); addView(text(label, 12, MUTED, false)) }
    private fun quick(title: String, sub: String, click: () -> Unit): View = card().apply { addView(text(title, 17, TEXT, true)); addView(text(sub, 13, MUTED, false)); setOnClickListener { click() } }
    private fun emptyCard(title: String, sub: String): View = card().apply { addView(text(title, 17, TEXT, true)); addView(text(sub, 13, MUTED, false)) }
    private fun chip(label: String, color: Int): View = TextView(this).apply { text = label; textSize = 12f; setTextColor(color); typeface = Typeface.DEFAULT_BOLD; setPadding(dp(10), dp(5), dp(10), dp(5)); background = rounded(0xFFF9FAFB.toInt(), dp(20), color) }

    private fun vertical(): LinearLayout = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
    private fun horizontal(): LinearLayout = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
    private fun card(bgColor: Int = CARD): LinearLayout = vertical().apply { setPadding(dp(16), dp(16), dp(16), dp(16)); background = rounded(bgColor, dp(22)); elevation = dp(2).toFloat(); layoutParams = LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(10) } }
    private fun text(value: String, sp: Int, color: Int, bold: Boolean): TextView = TextView(this).apply { text = value; textSize = sp.toFloat(); setTextColor(color); includeFontPadding = true; if (bold) typeface = Typeface.DEFAULT_BOLD }
    private fun input(hint: String): EditText = EditText(this).apply { this.hint = hint; textSize = 16f; setSingleLine(true); setPadding(dp(14), 0, dp(14), 0); background = rounded(Color.WHITE, dp(16), LINE); minHeight = dp(54) }
    private fun primaryButton(label: String, action: () -> Unit): Button = Button(this).apply { text = label; textSize = 15f; isAllCaps = false; typeface = Typeface.DEFAULT_BOLD; setTextColor(Color.WHITE); background = rounded(GREEN, dp(18)); minHeight = dp(52); setOnClickListener { action() } }
    private fun outlineButton(label: String, action: () -> Unit): Button = Button(this).apply { text = label; textSize = 15f; isAllCaps = false; typeface = Typeface.DEFAULT_BOLD; setTextColor(GREEN_DARK); background = rounded(Color.WHITE, dp(18), GREEN); minHeight = dp(52); setOnClickListener { action() } }
    private fun outlineLightButton(label: String, action: () -> Unit): Button = Button(this).apply { text = label; textSize = 14f; isAllCaps = false; setTextColor(Color.WHITE); background = rounded(Color.TRANSPARENT, dp(18), Color.WHITE); setOnClickListener { action() } }
    private fun circleIcon(label: String, click: () -> Unit): TextView = TextView(this).apply { text = label; textSize = 22f; gravity = Gravity.CENTER; setTextColor(TEXT); background = rounded(Color.WHITE, dp(28), LINE); setOnClickListener { click() }; layoutParams = LinearLayout.LayoutParams(dp(46), dp(46)) }
    private fun avatar(url: String?, name: String): ImageView = ImageView(this).apply { scaleType = ImageView.ScaleType.CENTER_CROP; background = rounded(GREEN_SOFT, dp(40), GREEN); setImageBitmap(null); if (!url.isNullOrBlank()) loadImage(this, url) else setImageDrawable(null); contentDescription = name }
    private fun space(h: Int): Space = Space(this).apply { layoutParams = LinearLayout.LayoutParams(1, dp(h)) }
    private fun rounded(color: Int, radius: Int, strokeColor: Int? = null): GradientDrawable = GradientDrawable().apply { setColor(color); cornerRadius = radius.toFloat(); strokeColor?.let { setStroke(dp(1), it) } }
    private fun dp(v: Int): Int = (v * resources.displayMetrics.density).roundToInt()
    private fun toast(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    private fun hideKeyboard(view: View) { (getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager).hideSoftInputFromWindow(view.windowToken, 0) }
    private fun firstName(name: String?): String = name?.trim()?.split(" ")?.firstOrNull()?.ifBlank { "Entregador" } ?: "Entregador"

    private fun loadImage(target: ImageView, url: String) {
        executor.execute {
            runCatching { BitmapFactory.decodeStream(URL(url).openStream()) }.onSuccess { bmp -> runOnUiThread { target.setImageBitmap(bmp) } }
        }
    }
}

enum class Tab(val label: String) { HOME("Início"), RIDES("Corridas"), WALLET("Carteira"), NOTICES("Avisos"), MORE("Mais") }

data class Driver(val id: String, val collection: String, val nome: String, val telefone: String, val email: String, val fotoUrl: String, val pix: String, val banco: String, val statusDisponivel: Boolean)
data class Ride(val id: String, val refCollection: String?, val numero: String, val status: String, val coleta: String, val entrega: String, val valor: Double, val distancia: String, val entregadorUid: String, val dataMillis: Long) {
    fun isMine(uid: String?): Boolean = uid != null && entregadorUid == uid
    fun isFinished(): Boolean = status.uppercase() in listOf("FINALIZADA", "ENTREGUE", "CONCLUIDA", "CONCLUÍDA", "CANCELADA")
    fun isAvailableFor(uid: String?): Boolean {
        val s = status.uppercase()
        val free = entregadorUid.isBlank() || entregadorUid == uid
        return free && s in listOf("DISPONIVEL", "DISPONÍVEL", "AGUARDANDO_ENTREGADOR", "PENDENTE_ENTREGADOR", "CRIADA", "NOVO", "NOVA")
    }
}
data class Banner(val titulo: String, val descricao: String, val tag: String, val acao: String, val ativo: Boolean, val ordem: Int)
data class Notice(val titulo: String, val mensagem: String, val data: String)
data class UpdateInfo(val latestVersion: String, val url: String, val message: String, val required: Boolean)

private fun DocumentSnapshot.toDriver(collection: String): Driver = Driver(
    id = id,
    collection = collection,
    nome = getStringAny("nome", "name", "entregadorNome") ?: "Entregador",
    telefone = getStringAny("telefone", "phone", "celular") ?: "",
    email = getStringAny("email") ?: "",
    fotoUrl = getStringAny("fotoUrl", "photoUrl", "avatar", "imagem") ?: "",
    pix = getStringAny("pix", "chavePix") ?: "",
    banco = getStringAny("banco", "bank") ?: "",
    statusDisponivel = getBoolean("disponivel") ?: getBoolean("online") ?: (getString("statusOperacional")?.uppercase() == "DISPONIVEL")
)

private fun DocumentSnapshot.toRide(collection: String): Ride? {
    val status = getStringAny("status", "statusPedidoCore", "statusEntrega", "situacao") ?: ""
    val hidden = status.uppercase() in listOf("EXCLUIDO", "DELETADO", "RASCUNHO")
    if (hidden) return null
    return Ride(
        id = id,
        refCollection = collection,
        numero = getStringAny("numero", "pedidoNumero", "codigo", "shortId") ?: id.take(8),
        status = status.ifBlank { "NOVO" },
        coleta = getStringAny("coleta", "enderecoLoja", "lojaEndereco", "origem") ?: "",
        entrega = getStringAny("entrega", "enderecoEntrega", "clienteEndereco", "destino", "bairro") ?: "",
        valor = getDoubleAny("valorEntrega", "taxaEntrega", "valorCorrida", "valor", "total") ?: 0.0,
        distancia = getStringAny("distancia", "km", "distanciaTexto") ?: "",
        entregadorUid = getStringAny("entregadorUid", "driverId", "motoboyUid", "entregadorId") ?: "",
        dataMillis = getTimestamp("criadoEm")?.toDate()?.time ?: getTimestamp("createdAt")?.toDate()?.time ?: 0L
    )
}

private fun DocumentSnapshot.toBanner(): Banner? {
    val titulo = getStringAny("titulo", "title") ?: return null
    return Banner(
        titulo = titulo,
        descricao = getStringAny("descricao", "description", "texto") ?: "",
        tag = getStringAny("tag", "etiqueta") ?: "",
        acao = getStringAny("acao", "url", "link", "destino") ?: "",
        ativo = getBoolean("ativo") ?: getBoolean("active") ?: true,
        ordem = (getLong("ordem") ?: getLong("order") ?: 0L).toInt()
    )
}

private fun DocumentSnapshot.toNotice(): Notice? {
    val titulo = getStringAny("titulo", "title") ?: return null
    val millis = getTimestamp("criadoEm")?.toDate()?.time ?: getTimestamp("createdAt")?.toDate()?.time
    val formatted = millis?.let { SimpleDateFormat("dd/MM • HH:mm", Locale("pt", "BR")).format(Date(it)) } ?: ""
    return Notice(titulo, getStringAny("mensagem", "message", "texto") ?: "", formatted)
}

private fun DocumentSnapshot.toUpdateInfo(): UpdateInfo = UpdateInfo(
    latestVersion = getStringAny("latestVersion", "versao", "versionName") ?: "",
    url = getStringAny("apkUrl", "url", "downloadUrl") ?: "",
    message = getStringAny("message", "mensagem", "descricao") ?: "",
    required = getBoolean("required") ?: getBoolean("obrigatorio") ?: false
)

private fun DocumentSnapshot.getStringAny(vararg keys: String): String? {
    for (k in keys) {
        val v = get(k)
        if (v is String && v.isNotBlank()) return v
        if (v is Number) return v.toString()
    }
    return null
}
private fun DocumentSnapshot.getDoubleAny(vararg keys: String): Double? {
    for (k in keys) {
        val v = get(k)
        if (v is Number) return v.toDouble()
        if (v is String) v.replace(",", ".").toDoubleOrNull()?.let { return it }
    }
    return null
}
private fun humanStatus(status: String): String = when (status.uppercase()) {
    "ACEITA", "ACEITO" -> "Aceita"
    "NA_COLETA", "INDO_COLETA" -> "Na coleta"
    "RETIRADO", "PEDIDO_RETIRADO" -> "Retirado"
    "EM_ROTA", "SAIU_ENTREGA", "A_CAMINHO_CLIENTE" -> "Em rota"
    "ENTREGADOR_NO_LOCAL" -> "No cliente"
    "FINALIZADA", "ENTREGUE", "CONCLUIDA", "CONCLUÍDA" -> "Finalizada"
    "RECUSADA" -> "Recusada"
    "EXPIRADA" -> "Expirada"
    else -> status.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale("pt", "BR")) else it.toString() }
}
private fun nextStatus(status: String): Pair<String, String> = when (status.uppercase()) {
    "ACEITA", "ACEITO" -> "NA_COLETA" to "Indo para coleta"
    "NA_COLETA", "INDO_COLETA" -> "CHEGUEI_COLETA" to "Cheguei na coleta"
    "CHEGUEI_COLETA" -> "RETIRADO" to "Pedido retirado"
    "RETIRADO", "PEDIDO_RETIRADO" -> "EM_ROTA" to "Indo para entrega"
    "EM_ROTA", "SAIU_ENTREGA", "A_CAMINHO_CLIENTE" -> "ENTREGADOR_NO_LOCAL" to "Cheguei no cliente"
    "ENTREGADOR_NO_LOCAL" -> "FINALIZADA" to "Finalizar entrega"
    else -> "ACEITA" to "Aceitar corrida"
}
