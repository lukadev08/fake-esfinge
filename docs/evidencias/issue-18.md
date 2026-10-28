# Evidência local — issue #18

Data: 28/10/2026. Referência: [issue #18](https://github.com/lukadev08/fake-esfinge/issues/18).
Situação: implementação, validação e revisão independente aprovadas; clean verify
da supervisão e imagem final concluídos; conferência documental final aprovada sem achados materiais.

## Entrega e contrato

Inspeção administrativa protegida lista remessas/lotes com filtros exatos de
cenário/UG/competência/assunto; detalhe da remessa e páginas de eventos/registros
completam a reconciliação local. Listas não selecionam payloads. Keyset ordenado,
teto de PK e geração conservam a continuação no reinício; reset invalida com409.
Índices originais, duplicatas, tipos, zeros, null/ausente e decimais permanecem
rastreáveis. Leitura não expira, renova atividade ou avança trabalhos/relógio.

O relatório LOCAL inclui eventos reais paginados com referência persistida e
vínculos remessa/lote, configuração não secreta, contagens e origem declarada.
COMPARTILHAVEL é construído separadamente e omite dados da execução. Ambos incluem
o manifesto real empacotado e seu hash; divergências são null/NAO_COMPARADO.
Guia e contratos: [inspeção administrativa](../inspecao-administrativa.md).
Sem migração/dependência nova; Maven resources empacota `docs/cobertura.md`.

## TDD e suíte

Comandos inspecionados, somente base temporária e dados fictícios. Java21:

```sh
rtk proxy env JAVA_HOME=/Users/luka/.sdkman/candidates/java/current \
  MAVEN_USER_HOME=/private/tmp/fake-esfinge-wrapper-check-fixed \
  ./mvnw -B -ntp -Dmaven.repo.local=/private/tmp/fake-esfinge-m2 \
  -Dtest=InspectionHttpTest,JdbcInspectionTest,ArchitectureTest test
# Mesmo prefixo e opções, sem -Dtest, para verify.
```

- RED1: esperava401 na nova listagem sem chave, recebeu404; exit1,4,229s.
- GREEN1: 1teste, zero falhas/erros/ignorados; exit0,4,310s.
- Ampliação HTTP:7testes verdes; exit0,5,332s.
- Focal HTTP/SQLite/arquitetura:19testes verdes; exit0,7,549s.
- Primeira suíte:342testes verdes; exit0,40,097s.
- Supervisão pediu prova explícita contra multiplicação das contagens por joins.
  Focal final: **20testes**,8HTTP+9SQLite+3arquitetura; exit0,7,549s.
- `verify` final: **343testes**,zero falhas/erros/ignorados; exit0,**39,815s**.
  Acrescenta17casos ao baseline326. Não houve novo Maven após congelamento.

As provas novas cobrem limites50/200, vazio/fronteira/continuação, novas inserções,
filtros alterados, cenário/UG/competência/assunto, reinício, descarte/erro/aborto/
cancelamento/expiração, reset/stale409, leitura sem efeitos inclusive com trabalho
pendente e prazo vencido, seis assuntos e conteúdo exato, diagnóstico/histórico,
origem estrita, síntese sem segredos/contexto e manifesto real. Saldo com payloads
duplicados,4valores monetários/registro e vários eventos confirma2lotes/4registros,
sem produto cartesiano. A contagem continua correta após descarte/reset.
422 de cobertura ausente continua explícito. Contribui a T01/T02/T18/T19, sem
concluir os aceites humanos ou acrescentar regras fiscais.

Uma ampliação de fixture tentou excluir processado antes de efetivo dependente;
SQLite recusou pela FK. Corrigida apenas a ordem da preparação do teste, que
verifica índice original sem reindexação. Nenhuma falha funcional após GREEN.
Avisos herdados Mockito/ByteBuddy/JDK não impediram testes.

A saída Maven foi capturada no transcript da ferramenta, sem redirecionamento
para `/private/tmp/fake-esfinge-issue18-verify.log`; esse arquivo não existe.
Resultados primários por teste: XML/TXT de Surefire, preservados pela supervisão
antes de clean verify em
`/private/tmp/fake-esfinge-issue18-supervisor.EpqKpf/executor-reports/`.
O diretório `target/surefire-reports` pode ser recriado pela supervisão.
`.scratch/issue-18-validation-summary.json` consolida a leitura posterior dos XML,
sem nova execução:343/0/0/0. A duração39,815s vem do transcript, não da soma dos XML.

## Artefato congelado e smoke do jar copiado

Produção/testes/POM/manifesto congelados antes da revisão. Inventário:
`.scratch/issue-18-freeze.sha256`, hash SHA-256
`6ca9178d5f00b042a6805d352735c568c851b91edc4b332daf8f1524d06a3bec`.

- Jar do executor, copiado antes de clean verify: SHA-256
  `416b01397c2b25b5f1e24f1136d92aa7fede75c5bf9a5a022efbaa9610157c91`.
- Manifesto fonte e recurso `BOOT-INF/classes/evidencias/cobertura.md` idênticos,
 30.266bytes, SHA-256
 `3704770cffc1229600e7fc2b10adc61aaffdcb5c2afd6b1768e2a6f4a3f5eeb6`.
- Manifest do jar informa `Implementation-Version: 0.1.0`. HTTP do jar confirmou
 essa versão, conteúdo/hash reais e Git/tag/digest de imagem indisponíveis.
- Cópia exata e base fictícia:
 `/private/tmp/esfinge-issue18-d653ogis/`; porta temporária18198.
 `.scratch/issue-18-run.json` contém os caminhos e é estado operacional local
 não versionado, preservado; não integra a entrega pública portátil. Nenhum dado
 original foi usado. Inventário freeze e validation-summary contêm somente hashes,
 caminhos de fontes/testes e totais sanitizados; nenhum payload, SQL interno,
 credencial real ou dado original foi incluído.

Smoke `.scratch/issue-18-smoke.py` com fases `prepare`/`resume`:

```sh
rtk proxy python3 .scratch/issue-18-smoke.py prepare --port 18198 \
  --state /private/tmp/esfinge-issue18-d653ogis/smoke-state.json \
  --database /private/tmp/esfinge-issue18-d653ogis/state.sqlite
# Encerrar e reiniciar o mesmo jar/base; repetir com fase resume.
```

**45verificações**,27antes+18após reinício, ambas exit0. Cobrem metadados0.1.0,
manifesto exato, filtros/páginas, ausência de payload em listas, eventos reais,
origem declarada, síntese sanitizada, zeros/decimal exatos, snapshots SQLite
antes/depois das leituras, continuação após reinício, reset/stale409, relatório
pós-reset vazio, IDs novos e SQLite íntegro/sem violações de FK.

Houve interrupção operacional por quota entre a inicialização do host e o smoke;
a retomada conferiu hashes, inventário, run.json, listener e processo existente
antes de agir, sem duplicar host ou repetir Maven. Nenhum diagnóstico funcional
foi atribuído à interrupção. TERM dos processos temporários exigiu escalada do
sandbox, autorizada. Host18198 encerrado após resume; base fictícia preservada.

## Limites

Revisão independente, clean verify e Docker final concluídos conforme a seção
final abaixo. CI remoto não executado. Sem publicação/commit/push pelo
executor. Nenhum TCE, SQL/referência independente, integrador real, dado original,
aceite #8/E1 ou T01/E3, UI/E4, regra fiscal ou homologação executados.
Paginação limita itens, não bytes de um registro individual; os registros seguem
os limites já aceitos na recepção. Driver SQLite pode manter o BLOB em memória;
a aplicação percorre streaming e materializa somente a página.


## Conferência da supervisão antes da imagem

Reviewer exclusivo da #18 aprovou código/testes sem achados materiais; supervisão
informou conferência de36XML/343testes e manifesto/jar do executor. `clean verify`
da supervisão:343testes,zero falhas/erros/ignorados,exit0,42,346s; log real
`/private/tmp/fake-esfinge-issue18-supervisor-verify.log` conferido pelo executor.
Jar final reconstruído SHA-256
`45eedfed5f1cb239a16bb906298e34f5259eaea4c52cf746f94f76b11228fcd6`,
conferido no disco; bytes do manifesto extraído continuam iguais à fonte congelada.
O smoke45 descrito acima usou a cópia do jar do executor416b0139..., antes desta
reconstrução. Build/smoke Docker final foram executados separadamente pela
supervisão, conforme a seção seguinte; não foram atribuídos a este smoke host. Nenhuma mudança em produção/testes/POM/manifesto/smoke.


## Imagem final validada pela supervisão

Supervisão informou **45verificações** adicionais na imagem final,27prepare+
18resume, ambas exit0, sem falhas: inspeção/relatório real, filtros/páginas,
reinício/reset e leitura sem efeitos. Este total é separado das45verificações
host no jar copiado anterior. Imagem Linux amd64, tag `fake-esfinge:issue-18-local`,
ID `sha256:4bf6cf1acdf7819389a5428f1a9a11d92c7f58cfe4f2ec1fbd709049089cc3bb`.
Jar interno SHA-256
`45eedfed5f1cb239a16bb906298e34f5259eaea4c52cf746f94f76b11228fcd6`,
igual ao jar final reconstruído. Manifesto via HTTP e recurso extraído idênticos
à fonte congelada SHA-256
`3704770cffc1229600e7fc2b10adc61aaffdcb5c2afd6b1768e2a6f4a3f5eeb6`;
comparação de bytes exit0. SQLite readonly íntegro, sem violações de FK.

Build sem rede/pullfalse; runtime bridge com publicação em loopback. Container
parado/removido e ausência confirmada; nenhum volume criado. Bindmount fictício
`/private/tmp/fake-esfinge-issue18-supervisor.EpqKpf` preservado, com relatório do
ensaio local. Nenhum dado original, TCE ou serviço externo de dados usado.
Resultados Docker são da supervisão, sem nova execução pelo executor.
Conferência documental final aprovada sem achados materiais; produção/testes/POM/manifesto/
smoke/jar não foram alterados nesta consolidação.

Reviewer exclusivo aprovou também a documentação final sem achados materiais.
Publicação é o próximo passo da supervisão; executor não realizou commit/push.
