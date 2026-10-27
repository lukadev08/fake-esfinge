# Evidência local — #9 processamento estrutural

Data: 26/10/2026. Perfil 2026/V6/leiaute6.06. Branch
`issue-9-processamento-assuntos`, base documental `1fc0128`.
Continuidade técnica autorizada conforme planejamento §13.1; #8 e aceite humano
E1 permanecem pendentes. Nenhum dado original do integrador foi usado aqui.

## Comportamento implementado

POST compatível `iniciar-processamento`, sem corpo e somente `chavePacote`,
captura todos os lotes RECEBIDO não agendados da chave. Não escolhe assunto por
catálogo. Disparo ativo, repetido ou sem elegíveis recebe409. Recepção posterior
é permitida, mas aguarda disparo futuro. Resposta mantém os três campos do schema
contratual. Autenticação e isolamento UG/cenário precedem efeitos.

Cada trabalho persiste geração, vínculo composto ao lote/remessa/UG/cenário,
instante de execução, sequência e latência capturada. Início e conclusão são
transições separadas: RECEBIDO→EM_PROCESSAMENTO→PROCESSADO_SUCESSO. Ordenação
usa instante numérico (segundos/nanos) e sequência; consultas não executam tarefas.
A conclusão revalida geração, estado e prazo antes do commit, inclusive após a
validação estrutural fora da transação. Mutex por instância e transações SQLite
IMMEDIATE serializam disputas; testes também usam duas instâncias concorrentes.

O adaptador reaproveita o validador estrutural existente sobre os bytes recebidos.
Cada registro processado e efetivo ocupa linha própria por lote/índice/origem;
contagens derivam dessas linhas, sem deduplicação. Inspeção distingue recebido,
`registrosProcessados` e `registrosEfetivos`; strings/zeros e decimais exatos são
preservados. O payload original não é reescrito. Ausência de regras fiscais é
explícita: sucesso significa processamento estrutural, sem homologação.

Controle administrativo separado com `X-Simulator-Key`:
GET/PUT `/__simulator/processamento` e POST
`/__simulator/processamento/avancar`. Padrões locais derivados MANUAL/1000ms;
AUTOMATICO chama a mesma porta por adaptador agendado. Alterar modo/latência não
reagenda trabalhos. JSON exige campos exatos, tipos sem coerção, UTF-8 válido e
sem duplicatas/documentos extras. Overflow é recusado antes da mutação pertinente.

Avanço explícito usa o mesmo Clock Java das sessões/remessas, com deslocamento
persistido sobre fixed/system. A transação do avanço de tempo confirma antes dos
passos; falha posterior de tarefa reverte sua transição, conservando tempo já
avançado e permitindo retomada. Cada transição tem transação curta independente.
Não há promessa de atomicidade de todos os passos de uma chamada.

V8 é aditiva. V1–V7 permanecem intactas e com checksum conferido. Upgrade de V7
preenchida preserva bytes/hash, seis assuntos, dinheiro TEXT nomeado, sessões,
eventos, IDs, sequências e FKs. Não cria processamento retroativo. Reinício
conserva configuração, relógio, estágios e efeitos sem reaplicação.

## Validação executada

JDK21 e wrapper Maven3.9.9 existentes, sem nova dependência. Comando base:

```sh
env JAVA_HOME=/Users/luka/.sdkman/candidates/java/current \
  MAVEN_USER_HOME=/private/tmp/fake-esfinge-wrapper-check-fixed \
  ./mvnw -B -ntp -Dmaven.repo.local=/private/tmp/fake-esfinge-m2 test
```

- RED da primeira fatia HTTP:1 teste,1 falha esperada (HTTP422 antes da
  implementação, esperado200), exit1,3.693s.
- GREEN da mesma fatia:1 teste, zero falhas/erros/skips, exit0,3.968s.
- Regressão HTTP inicial:7 testes verdes, exit0,4.967s.
- SQLite inicial:15 testes verdes, exit0,3.768s.
- Focal final `-Dtest=ProcessamentoHttpTest,JdbcProcessingTest,JdbcRemessaTest,AuthenticationHttpTest,ArchitectureTest`:
  **64 testes**, zero falhas/erros/skips, exit0,11.323s.
- `verify` completo: **134 testes**, zero falhas/erros/skips, BUILD SUCCESS,
  exit0,13.476s; jar empacotado.

Correção temporal após revisão independente (dois P2):

- RED: dois testes JDBC reproduziram avanço extremo e snapshot com conclusão
  inválida aceitos; teste HTTP reproduziu200 em vez de400 para avançoMAX.
  Três testes, três falhas esperadas, exit1,4.019s.
- A porta `BusinessTime` valida janela técnica de timestamps com ano de quatro
  dígitos (`0000`–`9999`) e horizonte alcançável pelo deslocamento `long` sobre
  o relógio base. O serviço verifica todos os inícios/conclusões antes do disparo;
  o adaptador do relógio valida o candidato antes de atualizar o deslocamento.
  Rejeições retornam400, sem trabalhos/eventos/atividade ou avanço parcial.
  A configuração de latência permanece; não se impõe limite fiscal a payloads.
- Regressões verificamMAX, latência cujo primeiro prazo cabe mas a segunda
  conclusão não cabe, conservação da configuração/tempo/dados e disparo posterior
  com latênciazero. HTTP alcança `9999-12-31T23:59:59.999Z`, rejeita1ms adicional
  e confirma `dataEnvio` de quatro dígitos no disparo permitido.
- Focal `-Dtest=ProcessamentoHttpTest,JdbcProcessingTest`:31 testes, zero
  falhas/erros/skips, exit0,7.953s.
- `verify` completo após correção: **136 testes**, zero falhas/erros/skips,
  BUILD SUCCESS, exit0,15.166s; jar empacotado. Nenhum processo pendente.

Correção temporal final após nova revisão (P2 do modo `system`):

- RED determinístico com relógio base ajustável: deslocamento válido até
  `9999-12-31T23:59:59.999Z`, seguido de2ms na base, permitia leitura com ano10000.
  Esperada `RemessaFailure`, nenhuma exceção lançada;1 teste,1 falha, exit1,2.628s.
- `JdbcBusinessClock.instant()` agora valida o instante calculado antes de
  retorná-lo. Esgotamento da janela técnica pelo tempo real interrompe leituras e
  operações dependentes com erro400, sem timestamp incompatível, ajuste automático
  ou mutação do deslocamento. A mesma proteção vale após reabertura da base.
- O teste confirma rejeição na instância original e na reabertura com o mesmo
  deslocamento persistido, conservando a linha do relógio integralmente.
- Focal `-Dtest=JdbcProcessingTest,ProcessamentoHttpTest`:32 testes, zero
  falhas/erros/skips, exit0,7.801s.
- `verify` completo final: **137 testes**, zero falhas/erros/skips,
  BUILD SUCCESS, exit0,13.619s; jar empacotado. Nenhum processo pendente.

As fatias HTTP/SQLite cobrem seis assuntos, múltiplos lotes e registros duplicados,
conteúdo efetivo, isolamento, conflitos, ordem, modo automático real, avanço manual,
reinício e expiração de token pelo relógio compartilhado. Falhas reais de statement
e COMMIT são injetadas em disparo/início/conclusão, verificando rollback, retomada
e erro/log sanitizados. Migração inclui rollback V8 e rejeição de checksum antigo
incompatível. Guardas de geração são verificadas antes da execução e durante a
validação antes do commit. ArchUnit preserva a direção hexagonal e núcleo Java puro.

## Supervisão final

Equipe própria da #9: explorer `grupo9_explorer` (GPT-6 Luna/médio), executor
`grupo9_executor` (GPT-6.1 Sol/baixo) e reviewer `grupo9_reviewer` (GPT-6.1
Sol/médio). Correções delegadas ao `grupo9_correcao` (GPT-6.1 Sol/baixo); após
limite de uso impedir a rodada seguinte, `grupo9_correcao_final` concluiu a guarda
de leitura. Este último identificou Codex/GPT-6, sem variante/esforço expostos.
Reviewer reapreciou as correções e aprovou tecnicamente toda a #9, sem achados
materiais remanescentes. Supervisor não alterou código de produção.

Supervisor repetiu `clean verify` sobre as fontes finais congeladas, com o mesmo
JDK/cache e comando base acima: **137 testes**, zero falhas/erros/ignorados,
BUILD SUCCESS, exit0, **15.930s**. Conferência dos 18 relatórios Surefire confirmou
os totais. Jar `target/fake-esfinge-0.1.0.jar`, SHA-256:
`0f389da344b94cb8838bbbf79bc12c398a51aecf13791fb4f53ca09bdfd554b4`.
Somente documentação/registros foram editados depois dessa suíte.

### Docker Linux amd64

Preparação independente na imagem V7 já validada `fake-esfinge:issue-7-local`,
ID `sha256:bcbc05ff4d10596e4b651e16adef205e6e4c7696ed0108d61f98a90f48a13c99`:
714 verificações de baixa + 636 de saldo + 529 de prazo = **1.879**, todas exit0.
Snapshot anterior à migração somou 3 verificações. Capturou contagens/hashes de dez
tabelas, incluindo sessões, originais, valores TEXT, eventos, sequências e histórico
V1–V7, sem imprimir payloads/tokens. Container V7 removido, volume preservado.

Imagem final `fake-esfinge:issue-9-local`, ID
`sha256:9bb42803b3815e6a77569fd328d0d90135324683f78a20b05b7e4570620ae874`,
Linux amd64, Java Temurin 21.0.10+7, UID/GID 10001:10001. Hash do jar interno igual
ao da suíte limpa. Instâncias sem rede (`--network none`) ou portas publicadas;
`/checks` somente leitura e `/data` em volumes fictícios exclusivos da #9.
Primeira instância usa `fixed`, a segunda usa `system`.

| Cliente na imagem final V8 | Verificações | Resultado observado |
| --- | ---: | --- |
| `MigrationSnapshot9 after` | 13 | Versão8, integridade/FKs, contagens/hashes das dez tabelas preservados antes de novas operações; sem agendamento retroativo |
| `ContainerCheck5 restart` | 34 | Sessão, bytes e valores antigos de baixa preservados |
| `ContainerCheck6 restart` | 46 | Quatro valores nomeados por saldo e recepção após upgrade |
| `ContainerCheck7 restart` | 42 | Prazo, referências, datas, bytes e nova recepção preservados |
| `ContainerCheck9 first` | 506 | HTTP real manual, rejeições administrativas, snapshot temporal inviável sem efeitos, ciclo válido posterior, seis assuntos/multiplicidade, novos lotes fora do snapshot, estados/ordem/latência, 10.001 créditos e token expirado pelo Clock compartilhado |
| `ContainerCheck9 restart` | 161 | Tempo/configuração/conteúdo persistidos, sem reaplicar conclusão e com novo ciclo após reinício |
| `SqliteCheck9` | 24 | Doze trabalhos, 10.012 registros processados e 10.012 efetivos, um evento terminal por trabalho, precisão/campos/índices/origem, mais de 50.000 valores recebidos TEXT e FKs íntegras |
| `ContainerCheck9 auto` | 228 | Scheduler não agenda antes do disparo; conclui sete lotes sem consultas/avanço; disputa com doze chamadas manuais não duplica o oitavo efeito |
| `ContainerCheck9 horizon` | 24 | Avanço system válido para ano 9999, passagem real ao limite e respostas 400 sanitizadas em administração/login, sem alterar deslocamento |
| `ContainerCheck9 horizon-restart` | 16 | Mesma rejeição após reinício, deslocamento preservado e nenhum timestamp incompatível |

Total na **imagem final: 1.094 verificações**, todas exit0. As 1.879 verificações de
preparação V7 e 3 do snapshot anterior são separadas. Ensaios preliminares com
artefatos anteriores às correções não integram esse total. Clientes SQL emitiram
aviso SLF4J sem provedor, sem falhar; logs conferidos da instância manual tiveram
somente duas linhas `Simulador iniciado`.

Containers finais parados/removidos e ausência confirmada. Preservados três volumes
no contexto `orbstack`: `fake-esfinge-issue9-supervisao` (ensaio preliminar),
`fake-esfinge-issue9-supervisao-final` (upgrade/reinício finais) e
`fake-esfinge-issue9-supervisao-auto` (automático/horizonte). Este último conserva
propositalmente o horizonte técnico esgotado; não serve para prosseguir operações
temporais. Exclusão depende de autorização específica. Imagem/clientes temporários
em `/private/tmp/fake-esfinge-issue9-supervisao` preservados localmente.

## Limites

Esta evidência não executa integrador real, aceite E1/#8 ou T01 completo.
Finalização, falhas de negócio, descarte, cancelamento, reset, expiração72h,
controle global #16, painel e regras fiscais continuam fora da #9.
CI remoto, publicação de imagem, push, PR e merge da #9 não realizados. Implementação
técnica local pronta para publicação específica; #8 permanece aberta, inclusive a
dependência remota registrada na #9. A exceção de sequência não comprova E1/E2
integral, T01/E3 nem homologação oficial.
