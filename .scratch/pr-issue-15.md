## Alteração

Permite bloquear/desbloquear uma UG e competência pelo controle administrativo
protegido. As seis mutações compatíveis retornam 403 sem efeitos ou consumo do
plano de falhas; consultas autorizadas continuam disponíveis. Trabalhos aceitos
antes do bloqueio continuam, conforme política prospectiva derivada.

V13 aditiva conserva dados e controles anteriores; reinício conserva bloqueios.
API administrativa tem UG opaca, competência anual e JSON booleano estritos.

## Validação

- Executor `verify` e supervisão `clean verify`: 275 testes, zero falhas/erros/
  ignorados, incluindo 12 novos HTTP/SQLite: seis mutações, entradas/auth,
  isolamento, rollback statement/COMMIT, corrida, upgrade preenchido, reinício
  e trabalho previamente aceito.
- Smoke no jar local e na imagem final Linux amd64: 51 verificações em cada,
  com reinício, bloqueio/desbloqueio, recuperação do fluxo e decimal exato;
  integridade/FKs SQLite e hash do jar conferidos, somente dados fictícios.
- Revisão independente do código e conferência documental final aprovadas,
  sem achados materiais.
- Evidência em `docs/evidencias/issue-15.md`. Reset #17, #8/E1, T01/E3, CI remoto,
  regras fiscais e homologação permanecem pendentes.

Closes #15
