# PR preparado — issue #5

Situação: local; implementação, revisão e supervisão concluídas. Sem push ou PR.
Destino: `lukadev08/fake-esfinge`; base `main`; head `issue-5-recepcao-baixas`.
Base funcional: `cc2d12d`; registro inicial da tarefa: `299b457`.

## Título

Recebe baixas de crédito com conteúdo e decimais exatos

## Corpo exato proposto

Estende a recepção, consulta RECEBIDO e inspeção administrativa para lotes de
`baixasCreditosTributarios`, aplicando o schema `baixa` sem coerção ou extras.
Conserva referências, datas, tipos, zeros, bytes e multiplicidade; cronologia,
existência de crédito/dívida e regras tributárias permanecem explicitamente adiadas.

Reutiliza núcleo, JDBC e `valores_recebidos` com BigDecimal/TEXT, sem arredondamento.
A migração V5 amplia o assunto dos lotes e preserva dinheiro, bytes, IDs, sessões,
eventos, FKs e sequências de V4. Consulta não processa e a inspeção exige chave
administrativa própria, com números JSON exatos e recebidos distintos de efetivos.

Validação: `clean verify` sob supervisão com **82 testes**, zero falhas/erros/ignorados;
revisão independente aprovada sem achados materiais; **794 verificações na imagem
Linux amd64**, sem rede externa, incluindo upgrade, reinício, 10.001 baixas e leitura
SQLite exata de mais de 20.000 valores dos três assuntos monetários. Outras **771
verificações** prepararam a base V4.

Demais dois assuntos, processamento, finalização, reset, CI remoto e aceite
humano E1/T01 permanecem pendentes. Não comprova homologação oficial.

Closes #5

## Ações externas a autorizar

Push de `issue-5-recepcao-baixas` para `origin`, criação deste PR contra `main`
e merge da mudança revisada em `lukadev08/fake-esfinge`. Implantação e publicação
de imagem não estão incluídas. Revisão técnica aprovada; a conta GitHub não permite
aprovação formal de PR próprio, conforme já verificado na #1.
