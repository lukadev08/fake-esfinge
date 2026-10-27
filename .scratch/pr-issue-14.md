## Alteração

Permite selecionar uma tentativa de recepção para interromper o transporte antes
da aceitação ou após o commit. O segundo caso conserva o lote em RECEBIDO e
bloqueia novas recepções daquele assunto com409 até descarte; reenvio sob a mesma
chave cria novos IDs, conserva histórico e finaliza pelos registros efetivos.

Controle administrativo protegido e persistido por contexto/geração, migração
V12 aditiva e interrupção real de conexão no adaptador Tomcat. Nenhum parâmetro
de teste no JSON compatível, deduplicação por hash ou regra fiscal nova.

## Validação

- Executor verify e supervisão clean verify:263 testes, zero falhas/erros/ignorados, incluindo20 novos
  casos socket/SQLite; rollback statement/COMMIT, reinício, concorrência,
  migração preenchida, seis assuntos, bytes/identificadores/decimais exatos.
- Smoke no jar local:51 verificações; na imagem final Docker Linux amd64, outras51,
  com interrupções sem resposta HTTP, reinício e recuperação/finalização; somente
  dados fictícios. Integridade SQLite e FKs conferidas na base final.
- Revisão independente do código e conferência documental final aprovadas,
  sem achados materiais.
- Evidência e limites em `docs/evidencias/issue-14.md`; T12 parcial, sem demora/
  HTTP500 adicionais. Descarte de RECEBIDO permanece aproximação divergente do
  manual. #8/E1, T01/E3, CI remoto e homologação não demonstrados.

Closes #14
