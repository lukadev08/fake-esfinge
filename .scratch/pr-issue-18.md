## Alteração

Completa a inspeção administrativa com filtros/paginação estável de remessas e
lotes, detalhe de remessa, eventos e registros por índice original. Leituras não
alteram atividade, relógio ou processamento; reset invalida cursores anteriores.
Relatório LOCAL inclui eventos reais paginados e origem declarada; síntese
COMPARTILHAVEL exclui dados da execução. O jar empacota o manifesto real e informa
seu hash, sem inferir Git/imagem ou comparação independente.

## Validação

- Focal20testes; `verify`:343testes,zero falhas/erros/ignorados.
- Smoke do jar copiado:45verificações com reinício/reset, conteúdo/paginação,
  versão/manifesto e snapshots SQLite sem efeitos de leitura.
- [Contrato administrativo](docs/inspecao-administrativa.md) e
  [evidência local](docs/evidencias/issue-18.md).

Revisão independente aprovada sem achados; supervisão clean verify343verde.
Imagem final Linux amd64: outras45verificações verdes, jar/manifesto conferidos,
SQLite íntegro/FKs válidas; container removido. Conferência documental final aprovada sem achados materiais.
Não conclui #8/E1/T01/E3, regras fiscais ou homologação. Nenhum dado original,
SQL interno, credencial real ou resultado de produção incluído.

Closes #18
