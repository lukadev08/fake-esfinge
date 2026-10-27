## Alteração

Envios abertos expiram após 72h de inatividade pelo relógio de negócio, com prazo
inclusivo e agenda exata persistida. Operações compatíveis bem-sucedidas renovam
atividade e prazo; login, rejeições e administração não renovam. Chave expirada
fecha, pendentes são abortados e dados ativos tornam-se históricos; nova abertura
usa outra chave, sem perder originais ou atribuir conclusão ao envio expirado.

V14 aditiva faz backfill transacional com nanos. Reutiliza avanço/executor atuais,
protege gravações tardias e distingue expiração de cancelamento/finalização.

## Validação

- Executor `verify` e supervisão `clean verify`:304 testes, zero falhas/erros/
  ignorados;29 novos HTTP/SQLite. Limites token/prazo/nanos, renovação, isolamento,
  reinício, upgrade preenchido, rollback statement/COMMIT e cálculo concorrente.
- Smoke no jar copiado e na imagem final Linux amd64:56 verificações em cada,
  com avanço virtual, reinício, expiração, consulta histórica, nova chave,
  reenvio e decimal exato; integridade/FKs e hash do jar conferidos.
- Revisão independente do código e conferência documental final aprovadas,
  sem achados materiais.
- Evidência em `docs/evidencias/issue-16.md`. Durações, renovação e diagnóstico são
  políticas derivadas. Reset #17, #8/E1, T01/E3, regras fiscais, CI remoto e
  homologação permanecem pendentes.

Closes #16
