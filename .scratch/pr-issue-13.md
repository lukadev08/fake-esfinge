# Rascunho local — issue #13

Situação: publicado e integrado com autorização específica no
[PR #40](https://github.com/lukadev08/fake-esfinge/pull/40), head `c3714b9`, merge
`566afda0c3ac9c6f1f623d752407616978b5cd93`; #13 completed, #8/E1 pendente.
Destino: `lukadev08/fake-esfinge`; base `main`; head `issue-13-cancelamento-envio`.
Base funcional `f2d7e58` (PR #39/#12), plano anterior ao código `b296dc5`.
Inclui registro documental da integração/limpeza autorizada da #12 (`15da126`).

## Título

Cancela envio aberto e permite reenvio integral com nova chave

## Corpo exato proposto

Implementa POST de cancelamento integral com query estrita e200 vazio, inclusive
com trabalhos agendados ou em execução. Fecha a chave e remove atomicamente
dados ativos dos seis assuntos, preservando originais, controles, outros envios
e sucessos históricos. Pendentes passam a ABORTADO com diagnóstico próprio;
resultados tardios não recriam efeitos.

V11 distingue cancelamento de finalização e preserva V1–V10, dados, diagnósticos
e sequências. Nova abertura na mesma competência retorna chave distinta para
reenvio integral. Cancelamento repetido/de concluído e mutações na chave cancelada
retornam409; consultas históricas não renovam atividade nem avançam trabalhos.

Validação: revisão independente aprovada; executor verify e supervisor clean
verify com **243 testes**, zero falhas/erros/ignorados; **1.807 verificações na
imagem final V11 Linux amd64 sem rede**, incluindo upgrade V10/16 tabelas,
cancelamento/nova chave/reenvio, reinício, conteúdo exato e agendador automático.
SQLite real cobre rollback statement/COMMIT, corridas entre instâncias e cálculo
capturado antes do cancelamento. Preparação anterior270+3 verificações separada.

Diagnóstico/terminal de cancelamento são políticas derivadas do simulador.
#8/E1 e T01/E3 permanecem pendentes. Cancelamento automático, resposta perdida,
reset/expiração, CI remoto e homologação não são concluídos por esta entrega.

Closes #13
Refs #8

## Limite de publicação

Push da branch acima, PR contra `main` e merge realizados após autorização
específica. GitHub confirmou head revisado, ausência de conflitos, merge,
`main` no merge e #13 completed. Check-runs/statuses vazios no head, sem CI
executado. Corpo publicado acima preservado; nenhuma imagem publicada ou implantação.
Pedido posterior do solicitante: parar após a #13 e relatar o que foi feito.
A indicação anterior de seguir para #14 foi substituída; #14 não iniciada.

Containers removidos. Volumes fictícios excluídos com autorização no contexto `orbstack`:
`fake-esfinge-issue13-supervisao`, `fake-esfinge-issue13-supervisao-repro` e
`fake-esfinge-issue13-supervisao-auto`. Exit0 e ausência confirmada; nenhum outro
volume excluído. [Resumo do encerramento](resumo-ate-issue-13.md) salvo localmente.
