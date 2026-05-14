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

## V1.4 Firebase Real — incluído nesta revisão

Esta revisão deixa de depender somente da simulação visual e passa a usar Firestore como base operacional mínima.

### Coleções usadas

`drivers/{driverId}`
- `online`: true/false
- `status`: available/offline
- `fcmToken`: token para push
- `updatedAt`: última atualização

`rides/{rideId}`
- `status`: pending, accepted, delivering ou finished
- `value`: exemplo `R$ 12,50`
- `distance`: exemplo `3,2 km`
- `duration`: exemplo `22 min`
- `pickup`: endereço/nome da coleta
- `dropoff`: endereço/nome da entrega
- `assignedDriverId`: vazio para qualquer entregador ou igual ao driverId do aparelho
- `driverId`: preenchido quando o entregador aceita
- `customerName`: nome do cliente

`driverHistory`
- registra accepted, rejected, delivering e finished.

### Como testar corrida real

No Firebase Console, crie um documento em `rides` com:

```json
{
  "status": "pending",
  "value": "R$ 12,50",
  "distance": "3,2 km",
  "duration": "22 min",
  "pickup": "Rodrigues Açaí e Cia",
  "dropoff": "Cliente próximo ao Centro",
  "assignedDriverId": "",
  "customerName": "Cliente teste"
}
```

Com o app online, a oferta aparece na tela inicial. Ao aceitar, o documento muda para `accepted`; ao avançar, muda para `delivering`; ao finalizar, muda para `finished`.
