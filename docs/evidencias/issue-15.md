# Evidência local — issue #15

Data: 27/10/2026. Perfil derivado 2026/V6/leiaute 6.06, artefato 0.1.0.
Referência: [issue #15](https://github.com/lukadev08/fake-esfinge/issues/15), T09/T10.
Situação: código/documentos aprovados e validação final verde; publicação pendente.
Dados inteiramente fictícios; sem execução do integrador ou chamada ao TCE.

## Contrato e alcance

GET/PUT `/__simulator/competencias/{ug}/{anoMes}` usam `X-Simulator-Key`, nenhuma
query e UG opaca configurada no cenário atual. Competência é texto de seis
dígitos ASCII de 202601 a 202612; GET tem corpo vazio e PUT exige JSON UTF-8
com exatamente `bloqueada` booleano. Saída 200: UG string, anoMes número e bloqueada
booleano. Ausência de linha significa false; PUT false remove somente o bloqueio.
Entradas inválidas recebem 400, chave administrativa ausente/inválida 401 e UG
desconhecida 404. Não há seletor de outro cenário no transporte administrativo.

Abrir, receber, disparar, finalizar, descartar e cancelar verificam bloqueio na
mesma transação SQLite IMMEDIATE dos efeitos. Autenticação e resolução isolada
da chave precedem o bloqueio (401/404); bloqueio dá 403 `SIM_ACESSO_NEGADO` antes
das demais guardas de estado, inclusive em chave encerrada. Rejeição não cria
nem altera lotes, trabalhos, atividade, eventos ou tentativas da falha #14.
Consulta compatível permanece disponível e renova atividade conforme o contrato
existente, sem executar processamento; administração não renova essa atividade.

Política prospectiva derivada: trabalho previamente aceito continua, inclusive
se o bloqueio for aplicado durante cálculo fora da transação. Bloqueio não é
cancelamento/suspensão, nem ratificação por rota compatível. O desbloqueio não
reabre chave encerrada. Outras UGs, meses e cenários conservam independência.
V13 cria somente `competencias_bloqueadas`, com PK de cenário/UG/mês, FK de cenário
e CHECK anual. V1–V12/fontes/schemas/build/dependências permanecem intactos.

## TDD e suíte do executor

Comandos inspecionados: Java 21, Maven Wrapper 3.9.9, dependências públicas/cache
temporário e SQLite temporário; sem TCE ou dados internos. Exemplo focal:

```sh
rtk proxy env JAVA_HOME=/Users/luka/.sdkman/candidates/java/current \
  MAVEN_USER_HOME=/private/tmp/fake-esfinge-wrapper-check-fixed \
  ./mvnw -B -ntp -Dmaven.repo.local=/private/tmp/fake-esfinge-m2 \
  -Dtest=CompetenciaHttpTest test
```

- RED: teste vertical recebeu 404 na rota ainda ausente, esperado 200; um teste,
  uma falha, exit1, 4,680s. Log `/private/tmp/fake-esfinge-issue15-red.log`.
- GREEN inicial: mesmo teste, zero falhas/erros/ignorados, exit0, 4,279s.
- Ampliação HTTP: seis testes verdes, exit0, 5,130s. Tipos/extras/duplicados,
  mídia, UTF-8, competência/UG, autorização, precedência, consultas, seis mutações,
  plano #14, jobs anteriores e erro 500 sanitizado por statement/COMMIT.
- Focais de oito classes: 119 testes verdes, exit0, 17,541s. Seis casos novos
  JDBC cobrem isolamento/reinício, corrida de duas instâncias, bloqueio durante
  cálculo, rollback de desbloqueio e upgrade V12 preenchida/falha da V13.
- Executor `verify`: **275 testes**, zero falhas/erros/ignorados, exit0, 33,539s;
  12 novos sobre baseline 263. Log `/private/tmp/fake-esfinge-issue15-verify.log`.
  Produção e testes congelados após essa suíte; ajustes de fixtures anteriores
  restringem-se à nova versão/tabela/checksum e ao contrato do repositório legado.

Snapshot SQLite inclui tabelas, blobs originais, valores TEXT, planos/diagnósticos,
IDs/sequências, eventos e checksums. Upgrade remove somente a nova tabela/linha
de migração da comparação; conteúdo anterior permanece igual. A corrida aceita
recepção completa anterior ao bloqueio ou 403 sem recepção, sem efeito parcial.

## Smoke no jar local com reinício

Cliente [.scratch/issue-15-smoke.py](../../.scratch/issue-15-smoke.py), biblioteca
padrão Python, instância/base vazia e exclusiva. JDK 21 Corretto 21.0.2 em macOS;
relógio fixed e UGs fictícias 0001/0002. Jar copiado para tmp antes da supervisão,
para não disputar o artefato de `clean verify`:

`00db5babfb8cc5354c0ed2d0eaa97b731e8f4c7a95a9ed2052bc8a7f8f1ab420`.

- `prepare`: **33 verificações**, exit0: chave própria, GET inicial, bloqueio,
  seis mutações 403, plano #14 não consumido, consulta RECEBIDO e isolamento UG/mês.
- Reinício da mesma base e `resume`: **18 verificações**, exit0: bloqueio
  persistido, consultas sem conduzir tarefas, conclusão de trabalho já aceito,
  finalização 403, desbloqueio, nova recepção e finalização com multiplicidade e
  decimal grande exato. Total 51, sem somar sondagens de prontidão.
- Base, jar e estado fictícios preservados em
  `/private/tmp/fake-esfinge-issue15-host.O0x6lq`; listener 18195 ausente ao concluir.
  PID 98331 inicial e PID 2575 do reinício encerrados por SIGTERM autorizado;
  sessões encerradas com 143, esperado para término por sinal.

## Supervisão e imagem final

Supervisor `clean verify`: **275 testes**, zero falhas/erros/ignorados, exit0,
34,867s. Log `/private/tmp/fake-esfinge-issue15-supervisor-verify.log`. Jar final:

`088f5a171fdde9886db840ede5dfc568e8b8e7deebc77f111bce96d5f8c5a912`.

Imagem `fake-esfinge:issue-15-local`, Linux amd64:

`sha256:b62c844a61198fc5d917c8ba89f505071780c1f46d2ecab95c2e0ad418f54120`.

- Build sem rede e `--pull=false`; runtime bridge publicado somente em loopback.
  Não se declara execução Docker sem rede. Hash do jar dentro do container
  conferido e igual ao artefato final da supervisão.
- Smoke final **51 verificações**, prepare 33 e resume 18, ambos exit0 após
  reinício da mesma base; nenhum smoke Docker falhou. Bloqueio persiste e
  recuperação do fluxo/finalização conserva decimal e multiplicidade.
- SQLite somente leitura: `integrity_check` ok e `foreign_key_check` sem linhas,
  exit0. Container parado/removido, ausência confirmada; nenhum volume Docker
  criado. Bindmount fictício em
  `/private/tmp/fake-esfinge-issue15-supervisor.CNFWQd/data` preservado.
- Revisor exclusivo da #15 aprovou código/testes sem achados materiais.
  Conferência documental final também aprovada, sem achados; produção/testes/smoke/jar
  não mudaram durante a consolidação documental.

## Incidentes operacionais e limites

O sandbox negou SIGTERM local. A primeira revisão automática da execução
escalonada falhou por limite de uso, sem executar a ação e sem julgamento de
insegurança. A tarefa foi interrompida; após retomada autorizada e consulta fresca
de uso pela supervisão, o processo ainda ativo foi conferido e encerrado pela
ação escalonada. Não houve reset de quota ou troca de modelo. A revisão de código
retomada foi concluída depois, sem contar a interrupção como revisão completa.
`pgrep` não pôde consultar sysmond no sandbox; sua falha não foi tratada como
ausência de processo. Uma tentativa de iniciar o reinício antes da parada falhou
sem alterar dados; somente o reinício posterior e as verificações passadas contam.

No Docker, o primeiro comando foi recusado pelo glob do zsh em `users[0]`, antes
de executar Docker; aspas corrigiram o argumento. Sondagens GET aguardaram a
prontidão e a porta efêmera foi reconferida após reinício. O cliente do smoke não
repete mutações. Esses incidentes não representam falha funcional ou RED adicional.

Publicação permanece pendente. Repetição após reset
pertence à #17. Aceite humano #8/E1, integrador T01/E3, CI remoto, regras fiscais
e homologação oficial continuam pendentes.
