# Obter inventário das chamadas reais do integrador

Status: draft
Type: task
Mode: HITL
Label: `wayfinder:task`
Blocked by: none


> Histórico: perguntas, dependências e pendências abaixo registram as rodadas anteriores.
> A situação vigente está na consolidação final ao término deste ticket. `Status: draft`
> continua indicando ausência de publicação, não ausência de decisão.

## Question

Quais métodos, rotas, ordem, opções e respostas o integrador realmente usa? Obter inventário e exemplos sanitizados de quem o mantém, incluindo cancelamento parcial.

## Answers

Respostas fornecidas em 21/10/2026. Inventário relatado pelo usuário; não validado por captura HTTP ou execução do integrador.

### Operações informadas

| Método | Caminho | Parâmetros e observações |
|---|---|---|
| GET | `/v6/tributario/iniciarEnvio` | Abre a remessa; `cancelamentoAutomatico` informado como parâmetro com valor `"NAO"`. |
| POST | `/v6/tributario/enviarParcialLote` | Envia os lotes de cada assunto. |
| POST | `/v6/tributario/iniciar-processamento` | Chamado após todos os lotes de cada assunto, dentro da mesma remessa. |
| GET | `/v6/tributario/consultarPorNumeroLote` | Número do lote enviado como parâmetro; nome e localização exatos ainda não informados. Grafia corrigida pelo usuário para `consultar`. |
| POST | `/v6/tributario/finalizarEnvio` | Finaliza depois dos ciclos dos assuntos. |
| POST | `/servicosGerais/cancelarEnvioParcial` | Parâmetro `chavePacote`; cancela toda a remessa aberta, conforme relato. |

URL original de cancelamento informada, sem chave real:

`https://api.virtual.testing.tce.sc.gov.br/api-gateway/ms-esfingeonline-servicos-gerais/servicosGerais/cancelarEnvioParcial?chavePacote=<chavePacote>`

### Sequência real relatada

`iniciarEnvio → enviar todos os lotes de um assunto → iniciar-processamento → consultar até terminar → repetir envio/processamento/consulta para os assuntos seguintes → finalizarEnvio`

Ordem dos assuntos:

1. Saldo inicial, quando a remessa for de janeiro.
2. Contribuintes.
3. Lançamento de crédito.
4. Início do prazo prescricional, quando houver dados; caso contrário, omite-se esse envio.
5. Diário de arrecadação.
6. Baixa de crédito.

### Opções e respostas

- `cancelamentoAutomatico` é enviado como parâmetro com valor `"NAO"`. Esta confirmação substitui a resposta inicial `false`; não tratar como booleano literal no contrato HTTP.
- A princípio, o integrador utiliza apenas retorno HTTP OK. A análise de campos, estados e erros da resposta permanece **pendente de revisão posterior**, conforme solicitado pelo usuário. Não está confirmado que o status HTTP, isoladamente, determine conclusão do processamento.
- Apesar do nome `cancelarEnvioParcial`, a operação cancela toda a remessa aberta, conforme relato. Efeitos sobre dados previamente processados ainda não detalhados.

### Semântica de remessa pendente de confirmação

O usuário confirmou que a mesma `chavePacote` é reutilizada para todos os assuntos, com ciclos de envio/processamento/consulta e uma finalização ao término. Segundo seu relato, a chave reflete `anoMes + cliente`, e os dados enviados ficam vinculados a ela. Essa descrição não estabelece um algoritmo de geração da chave nem confirma que “cliente” seja equivalente a UG.

Em 22/10/2026, o usuário esclareceu que “1 assunto por remessa” era a premissa antiga a retirar. O relato atual é de vários assuntos vinculados à mesma chave no envio, em lotes separados por assunto. Não foi relatado lote com assuntos misturados. Mantém-se, a pedido do usuário, pendência de confirmar o funcionamento de remessa e os disparos sucessivos por evidência do cliente/contrato; a ambiguidade textual está resolvida.

### Lacunas remanescentes

- Nome/localização exatos do parâmetro de número do lote e localização de `cancelamentoAutomatico`.
- Schemas, headers, autenticação e exemplos completos de requisição/resposta.
- Revisão dos campos/status de resposta usados pelo integrador para avançar, concluir ou detectar erros.
- Alcance detalhado da remoção no cancelamento integral. A [issue 05](05-delimitar-operacoes-e-assuntos.md) registra nova chave/reenvio completo e a escolha de remover dados no simulador; não reabrir essas decisões.

### Consolidação em 23/10/2026

O [contrato E0](../../../docs/contrato-e0.md#inventário-de-operações) reúne este
inventário, a autenticação da issue 05 e a recuperação da issue 06. O
planejamento e o `AGENTS.md` retiraram a premissa de um assunto por remessa.
A ambiguidade textual está resolvida; a comprovação do ciclo e os detalhes HTTP
continuam pendentes. Não deduzir schema ou conclusão a partir de HTTP OK.

## Consolidação final de 23/10/2026 — plano aprovado

Situação documental do recorte inicial: **concluída por relato e contrato derivado**.

- Login POST `/autenticacao/login`, headers `codigoAcesso`/`senha`, query `codigoUg`/`descricaoEmpresaTI`/`descritivoSoftware`, resposta `chave`; demais operações usam header `auth_token`.
- Abertura GET com `anoMes` inteiro e `cancelamentoAutomatico=NAO` na query; chave vem da resposta `chavePacote`, sem geração/transformação local. Um envio corresponde a uma UG/competência; códigos de cliente e UG não são presumidos iguais.
- Envio POST com chave na query e uma lista não vazia do assunto no JSON, omitindo demais. Tipos numéricos sem aspas; arrecadação usa `numeroParcelas`. As estruturas imobiliárias/transferência não são enviadas.
- Disparo POST com chave na query e corpo vazio; consulta GET com número no caminho. JSON usa `situacao`; `situacao_lote` é propriedade interna.
- RECEBIDO/EM_PROCESSAMENTO mantêm consulta, sucesso em todos os lotes libera próximo assunto; erro/aborto para com diagnóstico. Substitui o relato anterior de uso apenas de HTTP OK.
- Finalização POST leva chave e os 17 totalizadores string no corpo, zero para ausentes; HTTP 200 basta para concluir. Retornos de envio/disparo seguem campos do manual registrados no contrato.
- Integrador Java 21 / Spring Boot 3.5.10. Plataforma inicial Docker Linux amd64, instância/volume local por execução. Sem captura ou execução independente.

Referência atual: [contrato E0](../../../docs/contrato-e0.md) e
[pacote contratual](../../../docs/contratos/2026-v6-6.06/README.md).
Origem: respostas do solicitante e plano explicitamente aprovado nesta conversa;
sem captura HTTP, execução do integrador ou publicação no GitHub.
