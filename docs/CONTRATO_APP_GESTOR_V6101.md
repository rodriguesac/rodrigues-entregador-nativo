# Contrato App Entregador ↔ Gestor V6.10.1

## Gestor fala, app escuta

- `app_carousel_banners`: banners/carrossel da Home.
- `app_notifications`: avisos internos do app.
- `maquininhas`: maquininhas e taxas de débito, crédito, parcelado e ticket.
- `entregadores/{uid}.acerto`: resumo do acerto financeiro do motoboy.
- `entregadores/{uid}.financeiro`: valores financeiros consolidados.
- `repassesEntregadores`: histórico de repasses/acertos.
- `ofertasEntregador`, `corridasAtivas`, `rotas_entrega`, `pedidos`: ofertas e corridas.
- `app_config/entregador`, `app_config/global`, `app_config/manutencao`: manutenção, versão mínima e alertas.

## App fala, gestor escuta

- `entregadores/{uid}`: status, localização, token FCM, corrida atual e acerto atual.
- `historicoEntregador`: histórico por corrida, sem registrar etapa como corrida nova.
- `acertosEntregadores`: acerto enviado pelo app na finalização/registro de pagamento.
- `solicitacoesEntregadores`: alterações de Pix, banco, telefone e dados.
- `ocorrenciasEntrega`: problemas durante entrega.

## Regra

Sem dado real, o app mostra vazio correto. Nenhum botão deve ficar sem ação quando aparecer.
