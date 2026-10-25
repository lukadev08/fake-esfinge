# PR preparado — issue #4

Situação: local; implementação, revisão e supervisão concluídas. Sem push ou PR.
Destino: `lukadev08/fake-esfinge`; base `main`; head `issue-4-recepcao-arrecadacao`.
Base funcional: `e0b4852`; registro inicial da tarefa: `54525d5`.

## Título

Recebe diário de arrecadação com valores decimais exatos

## Corpo exato proposto

Estende a recepção, consulta RECEBIDO e inspeção administrativa para lotes de
`diarioGeralArrecadacao`, preservando bytes, identificadores, contagens e
multiplicidade. Aplica o schema `arrecadacao`: `numeroParcelas` é inteiro opcional
e o singular `numeroParcela` recebe 422, conforme aproximação relatada pelo cliente.
Não valida vínculo fiscal ao crédito, cronologia, catálogos ou cálculos tributários.

Créditos e arrecadações usam BigDecimal e TEXT na tabela comum `valores_recebidos`,
na mesma transação de lote/evento/atividade, sem coerção ou arredondamento.
A migração V4 preserva dinheiro, bytes, IDs, sessões, eventos, FKs e sequências
existentes. Administração mantém chave própria e números JSON exatos.

Validação: `clean verify` sob supervisão com **71 testes**, zero falhas/erros/ignorados;
revisão independente aprovada sem achados materiais; **853 verificações na imagem
Linux amd64**, sem rede externa, incluindo upgrade, reinício, 10.001 arrecadações e
leitura SQLite exata de mais de 20.000 valores dos dois assuntos. Outras **704
verificações** prepararam a base V3.

Demais três assuntos, processamento, finalização, reset, CI remoto e aceite
humano E1/T01 permanecem pendentes. Não comprova homologação oficial.

Closes #4

## Ações externas a autorizar

Push de `issue-4-recepcao-arrecadacao` para `origin`, criação deste PR contra `main`
e merge da mudança revisada em `lukadev08/fake-esfinge`. Implantação e publicação
de imagem não estão incluídas. Revisão técnica aprovada; a conta GitHub não permite
aprovação formal de PR próprio, conforme já verificado na #1.
