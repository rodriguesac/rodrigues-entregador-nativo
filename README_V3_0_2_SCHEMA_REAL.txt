V3.0.2 - Schema real do sistema Rodrigues

Correcoes aplicadas:
- Login nativo agora procura na colecao real: entregadores.
- Aceita documento por CPF limpo, CPF com mascara, telefone/celular/whatsapp.
- Respeita statusCadastro/statusAprovacao do painel gestor.
- Online/offline salva em entregadores com status Livre/Offline.
- Escuta ofertas reais em rotas_entrega e pedidos.
- Aceitar/rejeitar/expirar/finalizar grava nos campos usados pelo PainelUP.
- Historico usa historicoEntregador.

Como aplicar:
1. Extraia este ZIP dentro de C:\RSITE\rodrigues-entregador-nativo
2. Rode CORRIGIR_V3_SCHEMA_REAL_WINDOWS.bat
3. Abra GitHub Actions e baixe o novo APK.
