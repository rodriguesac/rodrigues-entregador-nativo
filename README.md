# Rodrigues Entregador — V5.2 Produto Nativo

Pacote preparado para ser aplicado no repositório `rodrigues-entregador-nativo` e compilado pelo GitHub Actions.

## Direção do pacote

- App 100% nativo em Kotlin + Jetpack Compose.
- Sem WebView, sem Capacitor, sem app híbrido.
- Visual escuro/profissional inspirado no padrão PainelUP/Up Entregas.
- Fonte Montserrat via Google Fonts Android.
- Roxo apenas como acento; sem texto preto sobre fundo roxo.
- Verde usado para status/ações positivas.
- Firebase no padrão real do gestor: `entregadores`, `rotas_entrega`, `pedidos`, `historicoEntregador`.

## Fluxo incluído

- Login por CPF/telefone + senha.
- Entregador antigo sem senha entra uma vez e cria senha.
- Cadastro pelo app com status pendente.
- Sessão salva no aparelho.
- Status online/offline salvo no Firestore.
- Token FCM salvo no entregador logado.
- Oferta real de corrida do Firestore.
- Som, vibração e tela urgente por Full-Screen Intent.
- Checklist de permissões: notificações, localização, tela urgente e bateria.
- Aceitar, recusar com motivo opcional, expirar e finalizar corrida.
- Mapa real nativo com osmdroid + rota/geocoding TomTom.
- Botão de navegação externa para Google Maps/Waze/padrão do celular.
- Histórico e ganhos básicos.
- Perfil, Pix/banco e solicitação de alteração de dados ao gestor.

## Financeiro

O valor mostrado para o motoboy usa repasse da frota/piloto, não taxa do cliente.

Campos priorizados:

- `repasseFrota`
- `repassePiloto`
- `valorRepasseFrota`
- `valorRepassePiloto`
- `financeiroEntrega.repasseFrota`
- `financeiroEntrega.repassePiloto`
- `valores.repasseFrota`
- `valores.repassePiloto`
- `logistica.repasseFrota`
- `logistica.repassePiloto`
- `calculo.valorTotalMotoboy`
- `valorTotalMotoboy`
- `valorMotoboy`
- `valorEntregador`
- `valorRepasseMotoboy`
- `valorCorrida`
- `valorRota`

A taxa do cliente (`taxaEntrega`) não deve ser usada como ganho do entregador.

## Como aplicar

Cole o conteúdo deste ZIP dentro de:

```bat
C:\RSITE\rodrigues-entregador-nativo
```

Depois execute:

```bat
APLICAR_V5_2_PRODUTO_NATIVO_WINDOWS.bat
```

O GitHub Actions vai gerar o APK em Artifacts.

## Observação honesta

Eu não compilei localmente neste ambiente porque não há Gradle/Android SDK local disponível. O pacote foi preparado para o GitHub Actions compilar. Se o Actions apontar erro, corrija em cima desta V5.2, sem voltar para versão híbrida ou versão feia.
