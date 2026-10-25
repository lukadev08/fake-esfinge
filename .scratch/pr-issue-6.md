# Rascunho local — issue #6

Situação: implementação/revisão/suíte e jar local aprovados; Docker V6 pendente.
Sem push ou PR. Destino: `lukadev08/fake-esfinge`; base `main`;
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

**Docker V6 pendente:** a API do OrbStack não responde e o macOS não abre o app.
Nenhuma imagem V6 foi construída ou validada. CI remoto, integrador real, aceite
E1/T01/T16, condições e regras fiscais continuam pendentes; sem homologação oficial.

Closes #6

## Limite de publicação

Rascunho local. Concluir Docker V6 ou obter decisão explícita sobre seu adiamento;
depois solicitar autorização específica para push, criação do PR e merge em
`lukadev08/fake-esfinge`. Implantação e publicação de imagem não estão incluídas.
