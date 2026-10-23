# Delimitar operações e assuntos do piloto

Status: draft
Type: grilling
Mode: HITL
Label: `wayfinder:grilling`
Blocked by: 02, 04


> Histórico: perguntas, dependências e pendências abaixo registram as rodadas anteriores.
> A situação vigente está na consolidação final ao término deste ticket. `Status: draft`
> continua indicando ausência de publicação, não ausência de decisão.

## Question

Quais operações são necessárias aos seis assuntos e como o cancelamento parcial se traduz nas rotas reais? Há dependência comprovada de webhook, relatório ou outro assunto?

## Answers

Resposta fornecida em 22/10/2026. Comportamentos do integrador são relatos do usuário; escolhas do simulador são identificadas separadamente.

### Autenticação e dependências

- O cliente depende de token de autenticação. O usuário indicou a seção `Autenticação / Autenticacao / login` do Swagger de homologação: <https://api.virtual.testing.tce.sc.gov.br/esfingeonline/swagger-ui.html?urls.primaryName=Autenticação#/Autenticacao/login>.
- A referência aponta para a interface de documentação, não estabelece sozinha método, URL do serviço, parâmetros, headers, schema ou renovação. A tentativa de leitura pela ferramenta web em 22/10/2026 não recuperou a página; esses detalhes permanecem pendentes. Isso não comprova indisponibilidade do serviço.
- Não há dependência informada de webhook, relatório ou outro assunto além dos seis já listados.

### Operações necessárias e efeitos

| Operação | Necessidade e comportamento informado |
|---|---|
| `POST /servicosGerais/cancelarEnvioParcial` | Cancela toda a remessa aberta. Cada cancelamento exige criar nova `chavePacote`; o reenvio começa desde o início com todos os dados. |
| Descarte de lote | Necessário e automático em caso de erro. A issue 06 definiu posteriormente descarte do lote com erro, aborto dos seguintes não processados do mesmo assunto e preservação dos anteriores válidos; tabela para outras opções de cancelamento ainda pendente. |
| Descarte por assunto | Necessário e selecionável no simulador. A issue 06 definiu remoção dos dados daquele assunto no envio aberto, preservando outros assuntos, mesma chave e controles históricos; seleção administrativa e detalhes HTTP permanecem pendentes. |

Os caminhos de descarte presentes no planejamento são candidatos a confirmar, não rotas do cliente comprovadas nesta resposta. Cancelar a remessa inteira nem sempre é necessário; descarte de lote/assunto deve permitir os casos de recuperação selecionados sem assumir que todo descarte cria nova chave.

### Dados no integrador versus dados no simulador

- No integrador, os dados são preservados após o cancelamento.
- Para o simulador, o usuário aceita remover os dados do envio cancelado. É uma escolha do cenário fake, não uma afirmação de equivalência com o comportamento oficial do TCE.
- Todo cancelamento integral resulta em reenviar todos os dados desde o início. Não estender essa regra automaticamente a descarte de lote ou assunto.
- A remoção deve ter seu alcance detalhado antes da implementação, distinguindo dados recebidos/processados/efetivos do envio cancelado de dados de remessas anteriores; preservar rastreabilidade e evidências do cancelamento.

### Pendências transferidas para definição de comportamento

- Contrato completo de autenticação, expiração e eventual renovação.
- Métodos, rotas, parâmetros e respostas dos descartes.
- Representação HTTP dos efeitos definidos na [issue 06](06-definir-respostas-e-erros.md), etapa exata de aplicação e casos ainda não cobertos, sem reabrir a definição do escopo por assunto.
- Como selecionar administrativamente o descarte por assunto e sua elegibilidade durante processamento; remoção/preservação já definidas na issue 06.
- Alcance detalhado da remoção no cancelamento integral, preservando controles históricos e remessas anteriores.
- Confirmação por evidência do funcionamento da remessa, da chave e dos efeitos das operações.

Consolidação em 23/10: consultar as [tabelas de comportamento](../../../docs/contrato-e0.md#comportamentos-selecionados).
O ticket continua rascunho local; respostas parciais não concluem o contrato E0.

## Consolidação final de 23/10/2026 — plano aprovado

Situação documental do recorte inicial: **concluída**.

- Descarte por assunto: POST `/v6/tributario/descarteParcialPorAssunto`, query `chavePacote` e `assunto`, header `auth_token`, corpo vazio; sucesso derivado 200 vazio.
- Valores confirmados: CADASTRO_CONTRIBUINTE, LANCAMENTO_CREDITO_TRIBUTARIO, DIARIO_ARRECADACAO, BAIXA_CREDITO_TRIBUTARIO, INICIO_PRAZO_PRESCRICIONAL e SALDO_INICIAL_CREDITO_TRIBUTARIO.
- Descarte rejeitado durante processamento; fora disso remove dados do assunto, inclusive pendentes, e preserva chave/outros assuntos/histórico. Exceção para RECEBIDO após resposta perdida é aproximação frente ao manual.
- Cancelamento integral POST em serviços gerais com chave na query, auth_token e corpo vazio: remove recebidos/processados/efetivos do envio atual, invalida trabalhos e exige nova chave/reenvio completo; preserva histórico/anteriores e dados no integrador.
- Rota explícita de descarte de lote e consulta agregada fora do inicial; descarte automático em erro permanece. Autenticação fechada no contrato e no ticket 02.

Referência atual: [contrato E0](../../../docs/contrato-e0.md) e
[pacote contratual](../../../docs/contratos/2026-v6-6.06/README.md).
Origem: respostas do solicitante e plano explicitamente aprovado nesta conversa;
sem captura HTTP, execução do integrador ou publicação no GitHub.
