# Rascunho local — issue #6

Situação: publicado e integrado com autorização específica no
[PR #33](https://github.com/lukadev08/fake-esfinge/pull/33), merge
`ad27bd88b0dc9fba2d5bd08e0a0059988f090119`; issue #6 completed.
Destino: `lukadev08/fake-esfinge`; base `main`;
head `issue-6-recepcao-saldo`. Base funcional `25dcbd6`, registro inicial `50558ee`.

## Título

Recebe saldo inicial com quatro valores monetários exatos

## Corpo exato proposto

Estende a recepção, consulta RECEBIDO e inspeção administrativa para
`saldosIniciaisCreditoTributario`, demonstrando janeiro de 2026 com referências
históricas. Valida apenas o schema fechado, sem defaults, tipos fiscais ou
restrição “somente janeiro” inferidos; recebidos continuam distintos de efetivos.

Valores passam a mapas imutáveis por registro. A tabela comum conserva campo e
valor TEXT, com chave por lote/índice/campo; V6 atribui os nomes corretos aos valores
antigos e preserva dinheiro, bytes, IDs, eventos, sessões, FKs e sequências.
Quatro campos de saldo permanecem distintos, sem soma, coerção ou arredondamento.

Validação: `clean verify` sob supervisão com **95 testes**, zero falhas/erros/ignorados;
revisão independente aprovada sem achados materiais; **729 verificações pelo jar
local macOS arm64**, incluindo upgrade V5→V6, reinício, 10.001 saldos e leitura exata
de mais de 50.000 valores. Outras 714 verificações prepararam a base V5 local.

Na imagem V6 Linux amd64 sem rede, **742 verificações** passaram: upgrade da base
V5, sessão antiga preservada com expiração confirmada, novo login, recepção de
10.001 saldos, reinício e inspeção SQLite dos campos e valores. As 714 verificações
de preparação Docker V5 são contabilizadas separadamente. O jar da imagem tem o
mesmo SHA-256 do artefato aprovado na suíte limpa.

CI remoto, integrador real, aceite
E1/T01/T16, condições e regras fiscais continuam pendentes; sem homologação oficial.

Closes #6

## Limite de publicação

Push, PR e merge realizados após autorização específica do usuário para a branch
`issue-6-recepcao-saldo` contra `main` em `lukadev08/fake-esfinge`.
Implantação e publicação de imagem não realizadas. Container temporário removido;
volume fictício `fake-esfinge-issue6-supervisao` excluído após autorização específica
posterior, com ausência confirmada no contexto `orbstack`.
