# Selecionar regras mínimas dos seis assuntos no piloto

Status: draft
Type: grilling
Mode: HITL
Label: `wayfinder:grilling`
Blocked by: 04, 07


> Histórico: perguntas, dependências e pendências abaixo registram as rodadas anteriores.
> A situação vigente está na consolidação final ao término deste ticket. `Status: draft`
> continua indicando ausência de publicação, não ausência de decisão.

## Question

Quais regras de domínio terão resultado verificável no piloto para cada assunto, incluindo saldo inicial e início do prazo prescricional, e quais ficarão explicitamente pendentes?

## Answers

Resposta fornecida em 23/10/2026.

### Prioridade inicial confirmada

- O objetivo inicial é receber os dados do envio e devolver a confirmação de recebimento dos lotes. Regras tributárias mais complexas não são prioridade nesta etapa.
- O usuário aceitou como base validação estrutural (formato, tipos e campos obrigatórios), sequência de operações e conferência de conteúdo e contagens. Esta conferência não implica validação de vínculos nem cálculo de efeitos tributários.
- Manter os comportamentos de estado, falhas e recuperação já definidos nas issues anteriores; esta resposta prioriza a profundidade de validação, sem cancelar silenciosamente aquelas decisões.

### Vínculos entre assuntos

- Os assuntos possuem relações entre si, e as referências estão nos dados enviados.
- Não validar esses vínculos na primeira etapa: o usuário considera seu desenvolvimento inadequado à prioridade inicial pelo tempo necessário.
- Registrar como **pendência de evolução** a validação das relações entre contribuinte, crédito, arrecadação, baixa e demais assuntos aplicáveis.
- A ausência dessa validação deve constar no manifesto como não implementada; recebimento bem-sucedido não comprova integridade referencial ou validade tributária.

### Saldo inicial e início do prazo prescricional

- Manter recepção e validação estrutural inicialmente.
- Regras tributárias específicas e cenários de validação de domínio ficam pendentes para evolução posterior.
- A ausência desses assuntos no piloto inicial é admitida conforme issue 07; isso não comprova sua cobertura funcional completa.

### Regras e CONs

A seleção de regras tributárias e CONs concretas permanece **pendente**, conforme solicitado. Não inventar códigos, mensagens oficiais ou aprovação de regras não executadas.

### Relação com o planejamento e aceite

Este recorte inicial é mais restrito que o MVP completo e a validação mínima de domínio originalmente propostos no planejamento. A diferença foi incorporada ao planejamento, `AGENTS.md` e [manifesto inicial](../../../docs/contrato-e0.md#manifesto-inicial-de-cobertura) em 23/10. Recepção/confirmação dos lotes e validação estrutural não equivalem ao aceite integral T01, à implementação de regras de domínio ou à homologação oficial.

Antes da implementação, completar os schemas estruturais e as lacunas do contrato
E0. A seleção de regras tributárias continua adiada para evolução; a consolidação
documental não transforma essas regras em implementadas nem retira os demais
critérios do MVP completo.

## Consolidação final de 23/10/2026 — plano aprovado

Situação documental do recorte inicial: **concluída; regras fiscais adiadas**.

- Seis schemas estruturais no pacote: obrigatórios incondicionais do leiaute e tipos DTO, com overrides relatados. Opcionais/condicionais admitem ausência/null, extras rejeitados no estrito; condições fiscais e defaults não são executados.
- Arrecadação usa numeroParcelas; prazo prescricional tem quatro campos derivados aprovados (ano inteiro, código string, duas datas). Campos imobiliários/transferência fora do schema inicial. Fontes originais permanecem intactas.
- Totalizadores de finalização são strings e contam efetivos; fora do recorte, zero obrigatório. Dinheiro decimal exato e identificadores preservados; enums externos e validações tributárias não são inferidos.
- Quatro pontos de teste confirmados pelo solicitante: HTTP compatível, administração separada, integrador real/SQL independente e núcleo/SQLite.
- Manifesto permanece inteiramente não implementado. Testes de schema são validação documental; não validam aplicação, regras fiscais ou aceite T01.

Referência atual: [contrato E0](../../../docs/contrato-e0.md) e
[pacote contratual](../../../docs/contratos/2026-v6-6.06/README.md).
Origem: respostas do solicitante e plano explicitamente aprovado nesta conversa;
sem captura HTTP, execução do integrador ou publicação no GitHub.
