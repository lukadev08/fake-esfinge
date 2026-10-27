# Rascunho local — issue #12

Situação: pronto localmente, revisão/suíte/Docker verdes; publicação não autorizada.
Destino: `lukadev08/fake-esfinge`; base `main`; head `issue-12-descarte-assunto`.
Base funcional `0752366` (PR #38/#11), plano anterior ao código `71907a4`.
Inclui registro documental da integração/limpeza autorizada da #11 (`54265cb`).

## Título

Descarta assunto e permite reenvio mantendo chave e histórico

## Corpo exato proposto

Implementa POST de descarte por assunto com query estrita e200 vazio. Remove
atomicamente os dados ativos do assunto, preservando a chave, os outros assuntos
e o histórico de bytes/hash/controles. Reenvio idêntico cria novos lotes e a
finalização conta somente ativos. Ciclo agendado ou em execução na mesma chave
e chave finalizada retornam409 sem efeitos.

Sucesso histórico mantém PROCESSADO_SUCESSO sem diagnóstico inventado. RECEBIDO
sem trabalho pode ser removido com diagnóstico próprio, aproximação explícita de
E0. V1–V10 permanecem intactas; nenhuma migração ou dependência nova.

Validação: revisão independente aprovada; executor verify e supervisor clean
verify com **212 testes**, zero falhas/erros/ignorados; **1.524 verificações na
imagem final V10 Linux amd64 sem rede**, incluindo preservação de16 tabelas,
recuperação, reinício, conteúdo exato e agendador automático. Testes SQLite cobrem
rollback statement/COMMIT e concorrência entre instâncias. Preparação anterior
259+3 verificações é separada.

#8/E1 e T01/E3 permanecem pendentes. Resposta perdida, cancelamento/reset/
expiração, CI remoto e homologação não são concluídos por esta entrega.

Closes #12
Refs #8

## Limite de publicação

Preparado para push da branch acima, criação de PR contra `main` e merge após
autorização específica. Nenhuma mensagem externa/publicação da #12 realizada.
Não há publicação de imagem nem implantação nesta proposta.

Containers removidos. Volumes fictícios preservados no contexto `orbstack`:
`fake-esfinge-issue12-supervisao`, `fake-esfinge-issue12-supervisao-repro` e
`fake-esfinge-issue12-supervisao-auto`. Exclusão depende de autorização separada.
