# Evidência local — issue #6

Data: 25/10/2026. Branch `issue-6-recepcao-saldo`, base documental `50558ee`
sobre o merge `25dcbd6` da #5. Executor `grupo6_executor`, GPT-6.1 Sol/baixo
conforme configuração do papel; exploração por `grupo6_explorer`, GPT-6 Luna/médio.
Dados fictícios e SQLite temporário; sem chamadas ao TCE ou integrador real.

## RED/GREEN observado

O primeiro teste HTTP exigiu 200 para saldo e recebeu 422 de cobertura ausente:
`SaldoHttpTest.janeiroRecebeConsultaEInspecionaQuatroValoresExatos`.
Maven terminou com exit code 1, um teste, uma falha comportamental e zero erros,
em **3,533 segundos**. Após implementar a fatia completa, o mesmo teste passou:
exit code 0, um teste, zero falhas/erros/ignorados, em **3,535 segundos**.

Os testes de schema, domínio, SQLite/migração e falhas foram acrescentados como
regressões da fatia; não se atribui RED individual a cada caso. Execução focalizada
`SaldoHttpTest,ReceivedContentTest,JdbcRemessaTest,RemessaFailureHttpTest` aprovada:
**39 testes**, zero falhas/erros/ignorados, exit code 0, em **6,329 segundos**.

A execução foi interrompida por limite de uso após os focalizados. Na retomada,
os handles anteriores já estavam concluídos; nenhum verify completo havia sido
iniciado. A árvore e os resultados foram preservados, sem reiniciar suíte pendente.

## Comportamentos exercitados

- Seis testes HTTP de saldo: abertura de janeiro 202601, referência histórica,
  quatro valores distintos incluindo decimal grande, consulta RECEBIDO e inspeção
  protegida com bytes, identificadores/zeros e recebidos sem processamento/efetivação.
- Schema documental: seis obrigatórios ausentes/null, tipo opcional ausente/null
  sem default, tipos sem coerção, limite Unicode, extras/listas vazias/nulas/mistas,
  chaves duplicadas e documentos adicionais. Saldo em outro mês não é proibido por
  regra inferida; janeiro é o cenário demonstrado, sem aceite fiscal de abertura.
- Cada campo monetário admite 1/1.0/1.00, exponencial, zero e negativo sem mínimo
  inventado; frações inválidas, inclusive além da precisão binária, são rejeitadas
  sem arredondamento. Inteiros matemáticos/grandes não recebem teto ou catálogo.
- Dois testes de domínio: bytes/lista/mapas de entrada e saída não podem alterar
  o conteúdo construído; campos exatos, cardinalidade e precisão são exigidos por
  assunto. Cadastro tem lista monetária vazia; os três assuntos anteriores têm
  um campo por registro, saldo tem quatro. Sem JSON/framework no núcleo.
- SQLite real: quatro linhas TEXT nomeadas por registro de saldo, oito para dois
  registros idênticos; valores distintos, índices, multiplicidades e vínculo
  composto conservados. Reabertura preserva bytes/assunto/sessão/ordem; outra
  UG/cenário não acessa os dados. Grafias 1/1.0/1.00 conservam bytes e valor numérico.
- V6 preserva V1–V5/checksums/fontes. Migração V5 populada com crédito, arrecadação
  e baixa atribui o campo correto a cada valor escalar antigo, conservando dinheiro,
  bytes/hash/IDs, eventos e sessão. High-water 42/77 produz lote 43/evento 78.
  Fixtures antigas escrevem o schema escalar antigo, sem usar o insert novo com campo.
- Conflito durante V6 reverte a estrutura e dados, conservando versão5 e a tabela
  escalar sem coluna campo. Removido o conflito, migra. FKs ficam habilitadas.
- Falhas reais de statement e COMMIT por FK diferida para cinco assuntos incluem
  os quatro valores do saldo: HTTP500 sanitizado e rollback completo, seguido de
  nova recepção com ordem1. Não são mocks de transação.

## Verificação completa do executor

```sh
rtk proxy env JAVA_HOME=/Users/luka/.sdkman/candidates/java/current \
  MAVEN_USER_HOME=/private/tmp/fake-esfinge-wrapper-check-fixed \
  ./mvnw -B -ntp -Dmaven.repo.local=/private/tmp/fake-esfinge-m2 verify
```

Resultado: **95 testes, zero falhas, zero erros, zero ignorados**, exit code 0,
build/jar aprovados em **14,398 segundos**. São 82 casos da base, seis HTTP de saldo,
dois de domínio, três JDBC adicionais e dois parâmetros adicionais de falha HTTP.
Verificação arquitetural por dependências de classes aprovada. V1–V5 e fontes
contratuais intactos. Avisos existentes Mockito/ByteBuddy não falharam o build.
O verify foi iniciado uma única vez após a retomada; seu handle concluiu exit0.

## Supervisão independente

`grupo6_reviewer`, GPT-6.1 Sol/médio conforme configuração do papel, aprovou o
diff staged sobre `50558ee`, sem achados materiais. Conferidos schema, mapas
imutáveis, precisão decimal, autorização, atomicidade/rollback, migração V6,
preservação dos valores V5, fronteiras e regressões. Não executou Maven/Docker.

O supervisor executou `clean verify`: **95 testes**, zero falhas/erros/ignorados,
exit code 0, build/jar aprovados em **15,182 segundos**. Nenhum código ou teste
foi alterado depois dessa execução e da revisão.

Jar verificado: `target/fake-esfinge-0.1.0.jar`, SHA-256
`6d5a6913163b2081d74defcaa11fc69d6b145ea9edbe665b1a5c67f6640517ce`.
Runtime local: macOS arm64, Corretto Java **21.0.2**. Serviço vinculado somente
a `127.0.0.1`, porta temporária 64259, base SQLite temporária e UGs fictícias
`0001`/`0002`; sem chamadas ao TCE. Esta é evidência de execução pelo jar local.

### Upgrade e reinício pelo HTTP local

Exportado o código V5 de `25dcbd6` para diretório temporário. O jar de preparação
foi construído offline com `-DskipTests package`, sem alterar a árvore da #6;
essa construção não foi contabilizada como execução de testes. O cliente V5
passou **714 verificações de preparação**, produzindo contribuintes, créditos,
arrecadações, baixas, valores e eventos, incluindo lote de 10.001 baixas.

Após encerrar esse servidor, o jar V6 abriu a mesma base. Foram aprovadas:

- **34 verificações de upgrade/regressão:** sessão, consulta RECEBIDO, bytes e
  dinheiro de baixa preservados; nova recepção aceita após a migração.
- **636 verificações de saldo:** janeiro de 2026 (`202601`), referência histórica/zeros,
  quatro valores distintos e exatos, ausência/null de tipo sem default, tipos
  grandes, Unicode, schema/rejeições sem efeitos, isolamento, administração,
  duplicatas, concorrência e **10.001 registros** conservados. Outros meses não
  recebem restrição fiscal inventada. Os cinco assuntos coexistem em lotes separados.
- **46 verificações após reinício:** sessão, saldo, bytes e quatro valores
  preservados, consulta sem processamento e nova recepção funcionando.
- **13 verificações SQLite somente leitura:** versão/migrações, integridade/FKs,
  campo compondo a chave e dinheiro realmente TEXT. Mais de **50.000 valores**
  conservam nome, índice, vínculo e equivalência exata ao payload; quatro campos
  por saldo e um para cada assunto monetário anterior, sem soma ou deduplicação.

Total: **729 verificações no jar V6**, todas com exit code 0, além das 714 de
preparação V5 local. Os servidores temporários foram encerrados após a conferência;
logs comuns mostraram apenas `Simulador iniciado`. Aviso SLF4J do cliente SQLite
não falhou o ensaio. Clientes em `/private/tmp/fake-esfinge-issue6-supervisao`,
base em `/private/tmp/fake-esfinge-issue6-host-state` e fonte V5 em
`/private/tmp/fake-esfinge-issue6-v5-fixture`, preservados fora do Git.

### Docker V6 e recuperação do ambiente

Antes da interrupção por quota, a imagem Docker V5 passou 714 verificações de
preparação em volume exclusivo da #6. Na retomada, Docker não respondeu ao CLI
nem ao `/_ping` no socket local, com timeout de cinco segundos. `orbctl status`
informou Running. Dentro do sandbox, o macOS recusou abrir o app com
`kLSNoExecutableErr`; conferência posterior mostrou que o executável existe.
A abertura fora do sandbox teve exit0, sem reinstalação, restart ou mudança de
configuração. O diagnóstico somente leitura também apontou CLI Homebrew, sem fix aplicado.

Depois da resposta do usuário sobre restaurar Docker, `/_ping` respondeu `OK` e
o inventário confirmou o container da #6 na imagem V5, ativo havia cinco horas,
com volume e imagem anteriores preservados. A parada normal e a parada com
timeout1 não concluíram, mesmo fora do sandbox; a consulta seguinte também ficou
sem resposta. Clientes CLI foram cancelados, sem remover o volume. A inspeção
da interface informou **Mac bloqueado**, sem conseguir desbloqueio automático;
solicitado desbloqueio manual para continuar o diagnóstico. A causa da instabilidade
da API permanece não confirmada; bloqueio de tela é o impedimento da inspeção visual.

Após confirmação de desbloqueio manual pelo usuário, a interface do OrbStack
mostrou somente o container temporário da #6 em execução. Sua parada pela
interface concluiu com status Exited (143), preservando o volume V5. A API voltou
a responder; o container antigo foi removido e a imagem V6 construída usando o
mesmo jar aprovado. Não houve restart global, reinstalação ou alteração de configuração.
A causa da instabilidade permanece não confirmada.

Imagem local `fake-esfinge:issue-6-local`, Linux amd64, Java Temurin **21.0.10+7**,
ID `sha256:46d3d07e84c02bd53d44e82d2933d1d35fc38c90b62ba94ee2c31fc106690a3c`.
O SHA-256 de `/opt/simulator/app.jar` coincidiu com o jar acima. Instância V6
executada como `10001:10001`, `--network none`, volume V5 em `/data` e clientes
montados somente leitura em `/checks`; nenhuma porta publicada ou chamada ao TCE.

A primeira tentativa de `ContainerCheck5 restart` terminou exit1 com
`AssertionError: Baixa após reinício HTTP=401`: o checkpoint V5 tinha mais de
cinco horas, além da validade configurada de uma hora. A inspeção administrativa
dos dados havia passado. O cliente auxiliar `UpgradeCheck6` confirmou somente
por leitura SQLite a sessão antiga preservada, UG e duração originais, expiração
anterior ao instante atual e rejeição HTTP401 sanitizada. Fez novo login e
prosseguiu com a mesma remessa; não alterou a aplicação, relógio ou sessão antiga.

Resultados finais na imagem V6, cada comando com exit code 0:

| Cliente executado com `docker exec` | Verificações | Evidência |
| --- | ---: | --- |
| `UpgradeCheck6` | 47 | Sessão V5 preservada e expirada, novo login, bytes/dinheiro/estado RECEBIDO e nova recepção após upgrade |
| `ContainerCheck6 first` | 636 | Janeiro, quatro campos exatos, schema/rejeições, isolamento, concorrência, cinco assuntos separados e 10.001 saldos |
| `ContainerCheck6 restart` | 46 | Sessão recente, bytes/saldos/contagens após reinício, sem processamento pela consulta |
| `SqliteCheck6` | 13 | Versão6, integridade/FKs, chave com campo, mais de 50.000 valores TEXT exatos por nome/índice/vínculo |

Total: **742 verificações na imagem V6**, separado das 729 no jar macOS e das
714 de preparação Docker V5. O reinício do container concluiu normalmente.
Logs comuns contiveram somente duas linhas `Simulador iniciado`; o aviso SLF4J
veio dos clientes auxiliares SQL e não falhou os ensaios.

Container temporário parado e removido após as verificações. A revisão automática
bloqueou a remoção do volume `fake-esfinge-issue6-supervisao`, exigindo autorização
específica para apagar os dados; volume fictício preservado. Imagem e clientes
auxiliares também preservados localmente, sem publicação.

## Limites

Implementação, revisão, suíte limpa e ensaios pelo jar local e Docker V6 concluídos.
Sem push, PR, merge, implantação ou publicação de imagem da #6.
Sem integrador real, CI remoto, aceite humano
#8/E1, T01/T16 integral ou homologação oficial. Prazo, processamento, finalização,
condições/defaults/catálogos/cálculos e validação fiscal permanecem não implementados.
Se saldo faltar no piloto, o cenário complementar continua necessário.
