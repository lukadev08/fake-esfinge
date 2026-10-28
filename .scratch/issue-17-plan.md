# Plano local — issue #17

Referência: [#17](https://github.com/lukadev08/fake-esfinge/issues/17), T09/T13/T19,
contrato derivado 2026/V6/6.06. Base funcional `9411e16` (PR #43), registro
`7af56d5`; branch `codex/issue-17-reset-cenario`. Baseline:304 testes. #8/E1 e
T01/E3 continuam pendentes; E2 técnico autorizado pelo planejamento §13.1.

## Contrato administrativo

POST `/__simulator/reset`, header exclusivo `X-Simulator-Key`, nenhuma query.
Corpo JSON UTF-8 estrito com exatamente `cenario` string não vazia e
`geracaoEsperada` inteiro não negativo de até `Long.MAX_VALUE`. Sem coerção,
campos duplicados/extras, nulls ou conteúdo após o objeto.

Autenticação administrativa precede a validação do alvo. Chave ausente/inválida
ou token compatível em seu lugar:401. Transporte/corpo inválido:400. Cenário
diferente da instância, geração obsoleta ou impossibilidade de incrementar
`Long.MAX_VALUE`:409 `SIM_ESTADO_INVALIDO`. Erro de persistência:500 sanitizado.
Sucesso:200 com `cenario`, `geracaoAnterior`, `geracao`, `remessasRemovidas`,
`lotesRemovidos` e `sessoesInvalidadas`, calculados da unidade confirmada.

O campo cenário confirma o alvo; não permite operar outro cenário. Repetir o
mesmo comando com geração antiga dá409, sem apagar uma execução posterior.

## Alcance e retenção

Reset remove somente o estado operacional e o histórico do cenário configurado:
bytes/payloads, dados recebidos/processados/efetivos, lotes, remessas, eventos,
trabalhos, planos de falha, marca de recuperação, bloqueios e sessões. Portanto,
evidências necessárias devem ser guardadas localmente antes do comando. A remoção
ocorre no SQLite; não há exclusão de arquivos, bases ou volumes externos.

Preservar configuração em `scenarios` (perfil/seed/tempo inicial/modo do relógio/
validade do token), identidades em `users`/`user_ugs`, `schema_migrations` e todos
os high-water de `sqlite_sequence`. Incrementar geração monotonicamente; repor
`processamento_config` em MANUAL/1000ms e `relogio_negocio.deslocamento_ms` em0.
No relógio fixed, o instante volta ao inicial; system continua ligado ao tempo
real com offset0. Nada de nova migração, fila ou dependência: V1–V14 intactas.

## Decisões e atomicidade

| Condição / ação | Resultado e efeito |
| --- | --- |
| Auth/corpo/alvo/geração inválidos | Erro antes de qualquer limpeza ou mudança de geração |
| Reset válido | Geração+1, purge e defaults no mesmo commit IMMEDIATE |
| Falha statement/COMMIT | Reverte toda geração/limpeza/defaults;500 sem sucesso parcial |
| Trabalho capturado antes do reset | `next/current/begin/complete/fail` já verificam geração/elegibilidade; resultado tardio não escreve após purge |
| Sessão existente no commit do reset | Invalidada; token antigo401; nova autenticação necessária |
| Abertura passa preflight antes do reset, mas entra na TX depois | Autenticação dentro da TX vê sessão apagada; nenhuma remessa nova |
| Login possui a TX antes do reset | Busca/tempo/salvamento confirmam juntos; reset posterior invalida sua sessão |
| Reset possui a TX antes do login | Login usa relógio reiniciado e TTL normal; não herda offset antigo |
| Reinício | Conserva geração/defaults e resultado já confirmado, sem reaplicar trabalho antigo |
| Repetição do cenário após reset | Mesmo resultado lógico de conteúdo/contagens/estados, com novas chaves/IDs/tokens |

O UPDATE condicional de geração confirma exatamente uma linha antes do purge.
Apagar filhos em ordem FK, sempre por cenário: `registros_efetivos`,
`registros_processados`, `valores_recebidos`, `falhas_processamento`,
`processamento_trabalhos`, `falhas_recepcao`, `remessa_eventos`, `lotes`, `remessas`,
`sessions` e `competencias_bloqueadas`. Os contadores da resposta vêm das remoções.
Outros cenários/bases permanecem intactos. Não tocar `sqlite_sequence` evita alias
de IDs antigos, inclusive quando uma tabela fica vazia ou high-water supera MAX.

## Menor alteração e chamadores

- Acrescentar `reset` em `Processamento`/`ProcessingService`, resultado no domínio
  de remessas e controller administrativo no adaptador de entrada de remessas.
  `ProcessingService` já coordena autenticação e cenários; sua porta `Transactions`
  existente define a unidade inteira. Não criar `cenarios -> remessas`.
- `cenarios` publica a porta de entrada `AdvanceGeneration`, implementada por
  `ScenarioService`. Ela autoriza a chave e faz CAS da geração do cenário
  configurado por `ScenarioRepository`/`JdbcScenarioRepository`. Essa capacidade
  escreve somente `scenarios`, sem conhecer tabelas de remessas ou sessões.
- `autenticacao` publica `InvalidateSessions`, implementada por
  `AuthenticationService`, que remove e conta sessões somente do cenário já
  configurado no serviço, por `AuthenticationRepository`/JDBC. Não recebe um alvo
  arbitrário do HTTP e não lê tabelas de remessas.
- `ProcessingRepository`/`JdbcProcessingRepository` limpam somente tabelas de
  remessas/processamento/controles próprios e restauram seu relógio/configuração.
  Retornam contagens de remessas/lotes; a contagem de sessões vem da porta de auth.
  CAS, remoções, defaults e contagens participam da mesma conexão gerenciada pelo
  `DataSource` compartilhado. Falha em qualquer participante reverte tudo.
- `AuthenticationService.login` passa a executar conta/UG/tempo/salvamento por
  porta transacional própria de autenticação, implementada no adaptador JDBC.
  Não importar `remessas.Transactions`: criaria ciclo de capacidades. Atualizar
  Composition, testes unitários e fixtures que constroem AuthenticationService.
- `RemessaService.open` autentica dentro da TX e usa aquela sessão; controller
  pode conservar preflight. Demais mutações já resolvem chave/lote dentro da TX.
- Guardas tardias existentes são reutilizadas e testadas, sem novo scheduler.
- `Composition` mantém os mesmos serviços concretos e expõe suas novas portas;
  o DAG continua `configuration -> remessas -> cenarios -> autenticacao`, com
  `remessas -> autenticacao`. Nenhuma dependência inversa é introduzida.
- Escopo de arquivos: `Processamento`, `ProcessingService`, domínio/controller e
  repositório JDBC de remessas; portas/serviço/repositório de cenários para CAS;
  auth service/port/JDBC para sessões e transação; `RemessaService.open`,
  `Composition`, testes/fixtures afetados, README/cobertura/evidência17/PR/PROGRESS.
  #18/UI fora do escopo.

## Retomada e revisão do desenho — 28/10/2026

O executor anterior foi interrompido por limite de uso depois de preparar este
plano; nenhum código ou teste da #17 foi escrito. A reconferência independente
identificou ownership incorreta no desenho anterior: `ScenarioRepository` não
deve purgar tabelas de outras capacidades. O plano corrigido mantém a coordenação
em remessas e usa portas públicas para geração e sessões. Contrato HTTP, alcance,
retenção e testes previstos permanecem os mesmos. Implementação aguarda aprovação
do desenho corrigido pela supervisão. Docker estava indisponível na retomada;
isso não é evidência de ausência de containers nem impede TDD/Maven/SQLite local.

## Validação

Inspecionados Maven Wrapper3.9.9/pom/JDK21 e testes atuais; SQLite temporário,
nenhuma chamada ao TCE ou dado original. Usar cache/wrapper temporários existentes.
Primeiro RED/GREEN HTTP vertical, depois focais SQLite e `verify`; supervisão
executará `clean verify` e smoke Docker Linux amd64 no jar final.

Cobrir auth/JSON/alvo/geração/overflow; rollback statement/COMMIT integral;
seis assuntos e estados recebidos/agendados/EM/sucesso/erro/expirado; planos,
bloqueios, offset/defaults; sessões antigas, nova autenticação e TTL; corrida de
abertura/login/reset e cálculo pausado; reinício, high-water e outro cenário/base;
repetição lógica após reset. Smoke usa somente base tmp fictícia e reset explícito.
Não executar reset em `data/simulator.sqlite` ou em base do usuário.

## Implementação local — 28/10/2026

Desenho corrigido aprovado pela supervisão antes de produção. TDD RED404/GREEN
comprovado; executor concluiu326 testes verdes (22 novos HTTP/SQLite), focal104
verde e smoke em jar copiado com63 verificações após reset/reinício. Rollback
statement/COMMIT e corridas de login/abertura/cálculo antigo estão demonstrados;
V1–V14/fontes/schemas/build/dependências intactos. Código/testes/smoke congelados.
Supervisão confirmou326 testes verdes em `clean verify` (39,548s) e63 verificações
na imagem final Linux amd64, com hash do jar/integridade SQLite conferidos.
Revisão independente de código/testes e conferência documental final aprovadas
sem achados materiais. Publicação é o próximo passo. Evidência detalhada em
[issue-17.md](../docs/evidencias/issue-17.md). Nenhum commit/push pelo executor.

## Publicação e encerramento — 28/10/2026

Supervisão publicou head `d6579c9478cee42ff1a3b11e9910009fa3cc3888` sobre base
`9411e16` e integrou no [PR #44](https://github.com/lukadev08/fake-esfinge/pull/44),
merge `c30ccb7ce70aae82cd1c5713a6716bd5661bbb82`. GitHub confirmou MERGED e
#17 CLOSED/COMPLETED; checkout alinhado ao merge por fast-forward. Check rollup
vazio, sem CI remoto. Código/testes/jar não mudaram, sem repetição de testes.
Corpo publicado do PR preservado, container ausente, nenhum volume criado e tmp
fictício preservado. #18 explorada, sem implementação; aceites humanos pendentes.
