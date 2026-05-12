# Rodrigues Entregador Nativo

App Android 100% nativo em Kotlin + Jetpack Compose, sem Capacitor/WebView.

## Como usar no GitHub

1. Crie um repositório novo.
2. Envie todos estes arquivos.
3. Coloque seu `google-services.json` dentro de `app/`.
4. Substitua `app/src/main/res/raw/alerta.wav` pelo áudio real.
5. Faça push para `main`.
6. Vá em Actions > Build APK Nativo Entregador.
7. Baixe o APK em Artifacts.

## Push esperado para nova corrida

Envie FCM data message com alta prioridade:

```json
{
  "type": "NEW_RIDE",
  "rideId": "123",
  "value": "R$ 12,50",
  "distance": "3.2 km"
}
```

## Próximas etapas

- Salvar token FCM do entregador no Firebase.
- Salvar online/offline no Firebase.
- Adicionar localização em background.
- Criar fluxo aceitar/rejeitar corrida.
- Integrar navegação Google Maps/Waze.
