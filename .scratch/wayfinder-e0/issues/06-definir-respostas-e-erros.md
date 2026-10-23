# Definir respostas, erros e repetição prioritários

Status: draft
Type: grilling
Mode: HITL
Label: `wayfinder:grilling`
Blocked by: 01, 04, 05


> Histórico: perguntas, dependências e pendências abaixo registram as rodadas anteriores.
> A situação vigente está na consolidação final ao término deste ticket. `Status: draft`
> continua indicando ausência de publicação, não ausência de decisão.

## Question

Quais schemas, resultados e falhas dos casos prioritários devem ser fixados em E0? Incluir autenticação, ausente versus nulo, erros assíncronos, repetição, descarte e cancelamento automático conforme evidência.

## Answers

Resposta fornecida em 23/10/2026. Decisões de comportamento para o simulador; não constituem comprovação do contrato oficial.

### Erro assíncrono e descarte

- O lote com erro é descartado.
- Os lotes seguintes ainda não processados **do mesmo assunto** recebem o estado `abortado_erro_lote_anterior`. A grafia foi fornecida pelo usuário; não está comprovada como enum oficial nem foi definida sua representação no schema HTTP.
- Os dados dos lotes anteriores processados com sucesso permanecem até o descarte explícito. No exemplo confirmado, o lote 1 permanece válido; a partir do lote 2 com erro, os dados são descartados e os lotes seguintes não processados do mesmo assunto ficam em `abortado_erro_lote_anterior`.
- Nos lotes descartados, permanecem registros de controle, como número e ID; esses registros históricos não contam como duplicados em nova integração.
- O descarte por assunto remove todos os dados daquele assunto na remessa aberta, preservando os demais assuntos e a mesma `chavePacote`, conforme confirmado pelo usuário.
- O cliente informa `cancelamentoAutomatico=NAO` (issue 02). A tabela completa de comportamento para campo omitido ou outros valores ainda não foi definida.

### Falhas prioritárias

1. Token inválido ou expirado.
2. Payload inválido.
3. Erro tributário assíncrono.
4. Resposta perdida após o recebimento do lote.

Demora e HTTP 500 não foram selecionados como prioritários nesta resposta; isso não representa retirada automática da matriz geral do planejamento.

### Respostas derivadas

O usuário autorizou definir respostas sem exemplo disponível por contrato derivado, identificadas como aproximações e sujeitas a revisão. Essa decisão não autoriza inventar códigos de CON, mensagens oficiais ou afirmar compatibilidade exata. Schemas e resultados concretos ainda precisam ser especificados com as evidências disponíveis.

### Resposta perdida e repetição

- O cenário aceita e persiste o lote e perde somente a resposta; o lote fica em `RECEBIDO` sem processamento.
- O usuário esclareceu o “reconhecimento do anterior”: quando um assunto possui lotes nessa condição, uma nova tentativa de envio retorna erro indicando envio aberto com lotes sem processamento. Não se trata de deduplicação por conteúdo nem de retorno de sucesso do lote anterior.
- Não foi informado identificador de idempotência; não inventar comparação por payload para reconhecer repetição.
- Para novo envio, executar o **descarte por assunto**, mantendo a mesma `chavePacote`, e descartar os lotes pendentes daquele assunto. Preservar os registros históricos de controle, sem considerá-los duplicados na nova integração.
- Condição de bloqueio, estado `RECEBIDO` e recuperação acima são comportamentos solicitados para o simulador; não comprovam o contrato oficial. Código HTTP, schema e texto exato do erro ainda não foram definidos.

### Campos ausentes e nulos

- Para campos obrigatórios, rejeitar tanto ausência quanto `null`, conforme aprovado pelo usuário.
- Para campos opcionais, seguir o manual; quando não houver definição, registrar a lacuna como pendente.
- A política é uma decisão do contrato derivado onde faltar evidência oficial; códigos HTTP e corpos de erro ainda não foram fixados.

### Pendências para concluir a definição

- Fixar schema/código HTTP do erro de envio aberto com lotes sem processamento; bloqueio e recuperação via descarte por assunto já definidos pelo usuário.
- Completar regras de campos opcionais onde o manual não esclarecer ausência versus `null`.
- Fixar schemas e respostas de autenticação, expiração, payload inválido e erro assíncrono; interpretação dos campos pelo integrador continua pendente de revisão (issue 02).
- Confirmar por evidência o ciclo da remessa (issue 02); escopo dos lotes abortados definido pelo usuário como o mesmo assunto.

### Consolidação em 23/10/2026

Decisões incorporadas ao planejamento e às [tabelas do contrato E0](../../../docs/contrato-e0.md#comportamentos-selecionados).
O manifesto registra aproximações planejadas e ausência de implementação.
Schemas, códigos HTTP e casos pendentes acima continuam abertos; documentar a
recuperação não equivale a executá-la nem a comprovar regra tributária oficial.

## Consolidação final de 23/10/2026 — plano aprovado

Situação documental do recorte inicial: **concluída como contrato derivado**.

- Token padrão de uma hora configurável; HTTP 401 provoca login/retentativa. Envio: 72h de inatividade renovadas por operações bem-sucedidas sobre a chave, inclusive consulta, sem avanço de processamento.
- Erro intermediário descarta lote e aborta seguintes não processados do mesmo assunto; anteriores válidos permanecem até descarte. Diagnóstico no envelope `retornoErros.erros[]`; código próprio SIM_, sem CON inventada.
- Finalizar após recuperação considera lotes ativos em sucesso e totais efetivos corretos, ignorando controles históricos; sem lotes ativos não há sucesso. Descarte durante processamento retorna 409.
- Nova tentativa após resposta perdida retorna 409 `SIM_ENVIO_ABERTO_SEM_PROCESSAMENTO`; limpar assunto e reenviar na mesma chave. Não aplicar esse bloqueio à mera existência normal de lotes recebidos antes do disparo.
- Repetições/estados inválidos retornam 409 sem efeitos: abertura com envio aberto, disparo ativo/sem elegíveis, finalizar/cancelar repetidos e mutação em chave encerrada; sem promessa de idempotência.
- Tabela HTTP aprovada: 200, 400, 401, 403, 404, 409, 422 e 500 conforme contrato. Corpo de erro síncrono: codigo, mensagem, correlacao; textos não são mensagens oficiais. Sucessos finais/controle vazios são derivados.
- Opcionais admitem ausência/null; obrigatórios rejeitam ambos; extras rejeitados no modo estrito. Condicionais tributárias adiadas.
- Apenas cancelamentoAutomatico=NAO; omissão/SIM/outros recebem 422. T05, sem movimento e contagem mensal T17 adiados explicitamente, sem ampliar o aceite inicial.

Referência atual: [contrato E0](../../../docs/contrato-e0.md) e
[pacote contratual](../../../docs/contratos/2026-v6-6.06/README.md).
Origem: respostas do solicitante e plano explicitamente aprovado nesta conversa;
sem captura HTTP, execução do integrador ou publicação no GitHub.
