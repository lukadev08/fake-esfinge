# Rascunho local — issue #13

Situação: pronto localmente, revisão/suíte/Docker verdes; publicação não autorizada.
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

Preparado para push da branch acima, PR contra `main` e merge após autorização
específica. Nenhuma mensagem externa/publicação da #13 realizada. Imagem/implantação
fora da proposta. Depois da integração, seguir para a #14 dentro de suas dependências.

Containers removidos. Volumes fictícios preservados no contexto `orbstack`:
`fake-esfinge-issue13-supervisao`, `fake-esfinge-issue13-supervisao-repro` e
`fake-esfinge-issue13-supervisao-auto`. Exclusão depende de autorização separada.
