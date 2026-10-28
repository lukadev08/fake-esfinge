## Alteração

Permite resetar explicitamente o cenário confirmado por nome e geração. A mesma
transação avança a geração, apaga dados/histórico/sessões/controles da execução e
restaura relógio/processamento, preservando configuração, identidades e sequências
de IDs. Comando obsoleto não apaga a execução nova; reinício conserva o resultado.

O fluxo coordenador usa portas públicas de cada capacidade. Login e abertura
serializam autenticação/tempo/gravação com reset, e trabalho antigo não reaplica
efeitos. Nenhuma migração ou dependência nova.

## Validação

- Executor `verify` e supervisão `clean verify`:326 testes, zero falhas/erros/ignorados, incluindo22 novos
  HTTP/SQLite; rollback statement/COMMIT, concorrência de login/abertura/reset,
  cálculo pausado, reinício, high-water e isolamento/replay dos seis assuntos.
- Smoke no jar copiado e na imagem final Linux amd64:63 verificações em cada,
  com reset, reinício e reenvio/finalização; conteúdo/decimais exatos e SQLite
  íntegro, somente dados fictícios. Hash do jar na imagem conferido.
- Revisão independente de código/testes e conferência documental final aprovadas,
  sem achados materiais.
- Evidência em `docs/evidencias/issue-17.md`. O reset remove deliberadamente o
  histórico do cenário confirmado. #18, #8/E1, T01/E3, CI remoto, regras fiscais e
  homologação permanecem pendentes.

Closes #17
