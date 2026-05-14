# Rodrigues Entregador Nativo — V4 Operacional Premium

Projeto Android 100% nativo em Kotlin + Jetpack Compose.

## O que esta versão traz

- Tema escuro premium inspirado na referência visual aprovada
- Home operacional com status ONLINE/OFFLINE
- Simulação de nova corrida em tela urgente
- Oferta de corrida com valor, distância, tempo e paradas
- Fluxo por estados: aguardando, oferta, coleta e entrega
- Mapa visual mockado em Compose para não depender ainda de API externa
- Tela de coleta com botão de navegação
- Tela de entrega/rota em andamento
- Ganhos, histórico, conta e menu Mais
- Foreground service para manter operação online
- Firebase Messaging preparado para push urgente
- Notificação full screen preparada
- GitHub Actions para gerar APK sem Android Studio

## Importante

Esta versão é uma base nativa operacional. O mapa ainda é visual/mockado dentro do app.
A navegação real abre o Google Maps/padrão do celular pelo botão Mapa/GPS.

## Build

Suba os arquivos no GitHub e rode:

Actions > Build APK Nativo Entregador

## Arquivos importantes

- app/google-services.json
- app/src/main/res/raw/alerta.wav
- app/src/main/java/com/rodriguesacai/entregador/ui/DriverHomeScreen.kt
- app/src/main/java/com/rodriguesacai/entregador/ui/UrgentRideScreen.kt
