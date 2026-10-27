# Evidência local — issue #14

Data: 27/10/2026. Perfil derivado 2026/V6/6.06; artefato 0.1.0.
Origem: exclusivamente fixtures fictícias. Sem acesso ao TCE, origem do integrador,
SQL interno ou dados originais. Esta evidência contribui para T12, sem encerrar
#8/E1 ou T01/E3.

## Entrega e limites do contrato

O [plano local](../../.scratch/issue-14-plan.md) fixa as transições e o contrato
administrativo antes da produção; o [README](../../README.md#interrupção-antes-da-aceitação-e-resposta-perdida-após-commit--14)
documenta uso/rejeições. GET/PUT/DELETE administrativos usam chave própria e query
exata chave/assunto. Plano é relativo às próximas recepções válidas daquele
contexto; inválidos e acesso recusado não consomem tentativa.

ANTES_ACEITACAO conserva somente contador/consumo do controle, sem lote, valores,
trabalho, atividade ou evento de aceitação. APOS_COMMIT confirma lote, original,
valores, vínculos, aceitação e marca do assunto na mesma transação; permanece
RECEBIDO. Somente após retorno da transação o núcleo emite a interrupção Java;
o adaptador retorna sem corpo e a Valve Tomcat encerra a conexão antes dos
headers. Socket real confirmou **zero bytes de resposta HTTP** nos dois casos.

Marca pendente produz 409 `SIM_ENVIO_ABERTO_SEM_PROCESSAMENTO`; PUT/DELETE do plano
retornam 409 e não a removem. Descarte do assunto limpa a marca junto com seus
dados ativos, conservando originais/eventos/plano consumido. Cancelamento integral
limpa a marca e fecha a chave; novo envio é independente. Não há deduplicação,
nem bloqueio normal por mera presença de RECEBIDO. Reconfiguração/remoção de
plano sem marca pendente é permitida; os eventos e lotes históricos permanecem.

V12 cria somente `falhas_recepcao`, com FK remessa/cenário/UG e seleção por
assunto/geração. Migrações V1–V11, build, dependências e fontes/schemas contratuais
permanecem intactos. Testes antigos receberam somente ajustes de versão/tabela
aditiva e implementação da nova porta na fixture de schema legado.

## TDD e execução do executor

Comando disponível inspecionado: wrapper Maven 3.9.9, JDK21, `pom.xml` sem
chamadas ao TCE; testes usam bases SQLite temporárias e servidor HTTP local.
No ambiente restrito, o comando foi executado com caches já existentes em tmp:

```sh
rtk proxy env JAVA_HOME=/Users/luka/.sdkman/candidates/java/current \
  MAVEN_USER_HOME=/private/tmp/fake-esfinge-wrapper-check-fixed \
  ./mvnw -B -ntp -Dmaven.repo.local=/private/tmp/fake-esfinge-m2 verify
```

| Execução | Resultado | Duração Maven |
|---|---|---|
| RED `ReceptionTransportTest`, antes de produção | 1 teste falhou: PUT do plano esperado200 recebeu404; exit1 | 4,797s |
| GREEN inicial do mesmo teste | 1 teste, zero falhas/erros/ignorados; exit0 | 4,726s |
| Transporte completo | 4 testes, zero falhas/erros/ignorados; exit0 | 5,361s |
| Focal `ReceptionTransportTest,JdbcReceptionFailureTest` | 20 testes, zero falhas/erros/ignorados; exit0 | 6,989s |
| Suíte `verify` | **263 testes**, zero falhas/erros/ignorados; exit0 | 31,344s |

Logs locais: `/private/tmp/fake-esfinge-issue14-red.log`, `-green1.log`,
`-transport.log`, `-focal.log` e `-verify.log` com o mesmo prefixo
`fake-esfinge-issue14`. O primeiro comando sem cache tmp foi recusado ao tentar
criar `/Users/luka/.m2/wrapper`; falha de preparação, não RED de comportamento.
Avisos herdados Mockito/ByteBuddy/JDK/SLF4J não impediram validação.

Os 20 casos novos cobrem:

- Transporte real sem retry, zero confirmação HTTP, limites antes/depois do
  commit, recebido preservado e nenhuma execução implícita.
- Administração estrita, token compatível recusado, inválidos sem consumo,
  marca409 e impossibilidade de bypass via DELETE/PUT.
- Seis assuntos, reabertura/reinício, bytes e valores TEXT, multiplicidade,
  identificadores/decimais exatos, recuperação, histórico e finalização.
- Duas instâncias SQLite disputando a tentativa selecionada: um consumo,
  resultado linearizado e FK íntegra; isolamento UG/chave/assunto/cenário/geração.
- Rollback real de statement e COMMIT em ambos os limites e no descarte;
  falha de COMMIT devolve500 sanitizado em HTTP e reverte plano/lote/marca.
- Upgrade V11 preenchida preservando todas as tabelas/checksums/sequências;
  colisão na V12 reverte schema/versão e dados integralmente.

## Smoke reproduzível e reinício no host

O [cliente Python](../../.scratch/issue-14-smoke.py) usa biblioteca padrão e
socket sem retry. Executado no jar do `verify` acima, porta local18194 e base
fictícia exclusiva em `/private/tmp/fake-esfinge-issue14-smoke-host/state.sqlite`:

```sh
python3 .scratch/issue-14-smoke.py prepare --port 18194 --state /private/tmp/fake-esfinge-issue14-smoke-host/fixture.json
# Reinício do mesmo jar/base.
python3 .scratch/issue-14-smoke.py resume --port 18194 --state /private/tmp/fake-esfinge-issue14-smoke-host/fixture.json
```

Preparação:22 verificações, exit0. Após reinício:29 verificações, exit0.
Total: **51**; nenhuma sondagem/falha intermediária contabilizada. Valida os dois
aborts, consumo uma vez, marca persistida, recuperação, originais históricos,
dois créditos idênticos com novos IDs e decimal grande exato, processamento e
finalização de um cadastro mais dois créditos efetivos. Serviço temporário
encerrado; listener18194 ausente. Base/fixture fictícias preservadas em tmp.

SHA256 do jar do executor:
`f0e17fbd48f6ffce8e4db45b2f24180ffec743248a182ed1e365bbdfcaf321e0`.
Não confundir esse jar do host com futura imagem final da supervisão.

## Revisão e supervisão

Supervisão executou `clean verify`: **263 testes**, zero falhas/erros/ignorados,
exit0,32,779s. Log `/private/tmp/fake-esfinge-issue14-supervisor-verify.log`.
SHA256 do jar final recompilado:
`0bef0ddd0a5f966cf6be025be4a3c2077df462609d51bc366687cf5483e004be`.
O repackage limpo altera o hash em relação ao jar do executor; esse é o artefato
final usado para construir a imagem. Revisor exclusivo da #14 aprovou o código
sem achados materiais e aprovou a conferência documental final, também sem achados.
Produção/testes congelados após o verify do executor; documentos/cliente de
smoke foram consolidados depois.

Imagem final `fake-esfinge:issue-14-local`, Linux amd64:
`sha256:ef0dc4cbff813dddeff7a6dc3eff8a7d90c2242d06b542cff0c77e38c68ab71d`.
Build com `--network=none --pull=false`, usando a base já disponível. Runtime
usou bridge e porta efêmera publicada somente em127.0.0.1; **não foi execução
com rede desabilitada**. O hash do jar dentro da imagem conferiu com o jar final
acima. Nenhum encaminhamento ao TCE existe na aplicação.

Supervisão executou o mesmo cliente em duas fases na imagem: **22 verificações
na preparação e29 após reinício**, ambas exit0. Conservou container/base entre
as fases; a marca pendente sobreviveu ao reinício, sem processamento implícito,
e a recuperação/finalização manteve os totais e o decimal grande exato.
SQLite somente leitura: `integrity_check=ok` e `foreign_key_check` sem violações,
exit0. Não somar essas sondagens aos51 checks do cliente.

Container `fake-esfinge-issue14-supervisao` parado/removido e ausência confirmada.
Não foi criado volume Docker; bindmount fictício em
`/private/tmp/fake-esfinge-issue14-supervisor.oUHCFG/data`, preservado junto com
os registros em tmp. Não houve exclusão de dados externos.

Incidentes do harness, sem alteração de aplicação: primeiro prepare ocorreu
antes da prontidão e recebeu desconexão no login, sem aceitação; a execução
contabilizada começou após GET de prontidão bem-sucedido. No reinício, Docker
remapeou a porta32768 para32769; sondagem na antiga falhou exit7, consulta da
porta publicada corrigiu o destino e a fase resume passou. Readiness e tentativas
anteriores não entram nas51 verificações. Nenhuma falha funcional remanescente.

## Cobertura não demonstrada

Demora e HTTP500 adicionais são evolução, sem serem contados como T12 completo.
Descarte de RECEBIDO diverge do manual e permanece aproximação expressa.
Transporte foi exercitado no Tomcat instalado; troca de servidor exige nova
validação do adaptador. Cliente que repete POST automaticamente pode mascarar
a primeira desconexão; desative retry ao medir o cenário.
Reset #17, bloqueio #15, expiração #16, integrador real/aceites humanos, CI remoto,
regras tributárias e homologação não foram executados ou aprovados por esta fatia.
