# Evidência local — issue #17

Data: 28/10/2026. Perfil derivado 2026/V6/6.06, sem homologação oficial.
Referência: [#17](https://github.com/lukadev08/fake-esfinge/issues/17), parcelas
T09/T13/T19. Base funcional `9411e16` (PR #43), registro `7af56d5`;
branch `codex/issue-17-reset-cenario`. Somente dados fictícios em bases tmp.

## Contrato e alcance executados

POST `/__simulator/reset`, `X-Simulator-Key`, nenhuma query, JSON UTF-8 exato
`{"cenario":"cenario-ficticio-a","geracaoEsperada":0}`. Tipo string não vazio e
inteiro não negativo long, sem extras/duplicados/null/coerção/trailing tokens.
401 precede validação do alvo;400 transporte/entrada;409 alvo diferente, geração
obsoleta ou overflow;500 persistência sanitizada. Sucesso200 identifica cenário,
geração anterior/nova e contagens confirmadas de remessas/lotes/sessões removidas.

Reset remove dados operacionais e histórico/payloads somente do cenário
configurado. CAS de geração, purge, sessões e defaults MANUAL/1000ms/offset0
confirmam na mesma transação SQLite IMMEDIATE. Configuração/seed/identidades,
migrações e `sqlite_sequence` high-water permanecem; não há exclusão de arquivos,
bases ou volumes externos. Evidências necessárias devem ser guardadas antes do
comando. Reinício conserva estado; reset recria execução, sem reutilizar IDs.

Coordenação em `ProcessingService` pela porta transacional existente. Cenários
publica `AdvanceGeneration` e escreve apenas `scenarios`; autenticação publica
`InvalidateSessions` e escreve apenas suas sessões; o repositório de remessas
limpa tabelas próprias e restaura relógio/configuração. Todos usam a mesma
DataSource/conexão gerenciada. Não há `cenarios -> remessas` ou `auth -> remessas`.

Login envolve conta/UG/tempo/salvamento por porta transacional própria de auth;
abertura usa autenticação dentro da transação. Testes serializam ambas ordens de
login/abertura com reset e impedem token com offset antigo. Trabalho calculado
fora da transação revalida geração/elegibilidade ao gravar; candidato antigo não
cria efeitos após reset, nova execução ou reinício.

V1–V14/fontes/schemas/build/dependências permanecem intactos. Nenhuma migração,
fila, scheduler ou framework genérico novo foi necessário.

## TDD e validação do executor

Comandos inspecionados: Java21/Maven Wrapper3.9.9, dependências públicas/cache e
SQLite temporário, sem TCE ou dados do integrador. Runtime/cache reutilizados:

```sh
rtk proxy env JAVA_HOME=/Users/luka/.sdkman/candidates/java/current \
  MAVEN_USER_HOME=/private/tmp/fake-esfinge-wrapper-check-fixed \
  ./mvnw -B -ntp -Dmaven.repo.local=/private/tmp/fake-esfinge-m2 \
  -Dtest=ResetHttpTest test
```

- RED:1 teste HTTP/SQLite, `Status expected:<200> but was:<404>` na rota nova,
  exit1,4,309s; produção ainda intacta.
- GREEN:mesmo teste, zero falhas/erros/ignorados, exit0,4,536s. Prepara trabalho
  EM, controles14/15 e offset; reset limpa/invalida e permite novo envio/ID.
- Primeira ampliação falhou na compilação da fixture: método `received()` não
  existe em `LoteInspection`, exit1,1,860s. Corrigida só a fixture e a ordem de
  preparação para manter estados misturados; não representa falha funcional.
- Focal inicial:30 testes verdes, exit0,7,179s.
- Focal final:104 testes verdes, exit0,15,656s, incluindo regressões de
  processamento/finalização/cancelamento/expiração, autenticação e arquitetura.
- `verify`:**326 testes**, zero falhas/erros/ignorados, exit0,37,139s; log local
  `/private/tmp/fake-esfinge-issue17-verify.log`.

Novos22 casos:4 HTTP e18 JDBC (incluindo parâmetros de seis assuntos/rollback).
Cobrem entrada/auth/alvo/geração/overflow, cenário vazio, CAS concorrente,
rollback statement/COMMIT após todos participantes, seis assuntos/multiplicidade/
decimal/bytes, recebido/EM/sucesso/erro/expirado, defaults/offset/seed/identidades,
high-water900 acima de MAX em tabelas vazias, outro cenário na mesma base e outra
base com mesmo cenário, replay lógico, sessões/chaves antigas e reinício.

Corridas usam futures/barreiras, sem aguardar horas reais: preflight de abertura
antes do reset e TX depois; abertura com TX antes do reset; login nas duas ordens
com offset72h; cálculo pausado fora da TX e begin/complete/fail antigos depois do
reset/reinício. A arquitetura verifica dependências de classes e ausência de ciclos.

## Smoke no jar copiado

Jar do executor copiado para
`/private/tmp/fake-esfinge-issue17-host.ZUYwUG/app.jar`, separado de `target`:
SHA256 `0b944029956b1c226765d9b24fa73a06735f262b8f99176daeb3df8cbbd141ef`.
Configuração fixed, duas UGs fictícias0001/0002, loopback18197 e base exclusiva
`state.sqlite` no mesmo diretório. Cliente reproduzível em
[issue-17-smoke.py](../../.scratch/issue-17-smoke.py).

- `prepare`:34 verificações, exit0. Crédito efetivo, contribuinte EM, plano de
  falha por lote/recepção, bloqueio, modo/latência e offset alterados; segunda UG.
  Reset confirma2 remessas/3 lotes/2 sessões removidas, geração1, configuração
  preservada, defaults restaurados, token antigo401 e lotes antigos404.
- Serviço interrompido e mesma base reaberta com o mesmo jar/configuração.
- `resume`:29 verificações, exit0. Geração/offset persistidos, token/chave antigos
  inválidos, novos IDs, plano antigo ausente, conteúdo/zeros/decimal exatos,
  repetição obsoleta409 sem apagar dados novos e finalização efetiva correta.
- Total:**63 verificações**. `PRAGMA integrity_check` retornou `ok`;
  `foreign_key_check` não retornou violações, leitura readonly/exit0.
- Duas sessões de serviço encerradas por Ctrl+C (status1 de interrupção do
  comando); `lsof` confirmou ausência de listener18197. Jar/base/estado fictícios
  preservados em tmp; nenhum arquivo/base/volume externo foi removido.

## Supervisão, revisão e publicação

Código/testes congelados após `verify`. Supervisão `clean verify`: **326 testes**,
zero falhas/erros/ignorados, exit0,39,548s; log local
`/private/tmp/fake-esfinge-issue17-supervisor-verify.log`. Jar final SHA256
`5ca54e67495411bec2e2bdb172a81fef59df3df73c30442d9332158e792af18b`.
O reempacotamento limpo muda bytes do jar em relação à cópia usada no smoke host,
sem alteração de código/testes.

Imagem final `fake-esfinge:issue-17-local`, Linux amd64, SHA256
`32e9ae448509f7a5e8da64aaf213c79d3bdb5cd6bc46b6fe7d1fc53cdb99fab6`.
Build com base em cache, `--network=none --pull=false`; runtime bridge com
publicação somente loopback. Jar dentro da imagem confere com o hash final acima.
Base fictícia exclusiva em bindmount
`/private/tmp/fake-esfinge-issue17-supervisor.rm5pE0`, sem volume Docker criado.

Smoke da supervisão: **34 prepare +29 resume =63 verificações**, ambos exit0, com
reset/reinício/replay e comando obsoleto409. SQLite readonly íntegro e sem violações
de FK. Somente sondagens GET aguardaram prontidão; nenhum smoke falhou. Container
temporário parado/removido e ausência confirmada; bindmount/dados fictícios e
imagem preservados. Nenhuma base do usuário foi resetada.

Reviewer independente da #17 aprovou código/testes e a conferência documental
final, sem achados materiais. Publicação e merge concluídos pela supervisão no
[PR #44](https://github.com/lukadev08/fake-esfinge/pull/44), em28/10/2026:
head `d6579c9478cee42ff1a3b11e9910009fa3cc3888`, base `9411e16`, merge
`c30ccb7ce70aae82cd1c5713a6716bd5661bbb82`. GitHub confirmou MERGED e
#17 CLOSED/COMPLETED. Check rollup vazio; resultados anteriores são locais, sem CI
remoto. Checkout alinhado ao merge por fast-forward. Código/testes/
smoke/jar não mudaram após congelamento; nenhum commit/push/PR pelo executor.
Corpo publicado preservado em `.scratch/pr-issue-17.md`. Encerramento somente
documental, sem repetição de testes. Container temporário ausente, nenhum volume
criado, dados fictícios tmp preservados. Próxima #18 explorada, ainda não implementada.

## Limites

As políticas de reset/retenção são administrativas e derivadas, sem equivalência
oficial alegada. Somente bases fictícias foram resetadas. Ensaios não executam o
integrador nem aprovam #8/E1, T01/E3, CI remoto, regras fiscais ou homologação.
Inspeção/relatório #18 e painel permanecem fora desta fatia.
