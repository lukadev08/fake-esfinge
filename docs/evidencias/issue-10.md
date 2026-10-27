# Evidência local — issue #10

Execução do executor em 26/10/2026, branch `issue-10-finalizacao`, base funcional
`eb376d0` (PR #36/#9), preparação `db0ee14`. Somente dados fictícios, SQLite
em diretórios temporários; sem TCE, integrador real, Docker ou publicação.
Agente `grupo10_executor`, GPT-6.1 Sol, esforço baixo. Revisão independente e
supervisão ainda pendentes nesta evidência inicial.

## Implementação

- POST `/v6/tributario/finalizarEnvio`, token compatível, JSON UTF-8 estrito sem
  query; chave e 17 totalizadores obrigatórios. Duplicatas, documentos adicionais,
  UTF-8 inválido ou transporte inválido retornam 400; schema inválido retorna 422.
- Seis strings de dígitos ASCII viram BigInteger no núcleo; comparação numérica
  conserva zeros iniciais e não tem overflow de inteiro fixo. Onze campos sem
  suporte exigem string exata `"0"`; positivo recebe 422 de cobertura.
- Núcleo exige pelo menos um lote e todos os lotes atuais em sucesso, inclusive
  recebidos depois do snapshot. Compara linhas efetivas por assunto/remessa/UG/
  cenário, conservando multiplicidade; não usa dinheiro ou `lotes.registros`.
  Todos os lotes atuais são ativos; descarte/recuperação não foram antecipados.
- Uma transação SQLite IMMEDIATE lê as precondições/contagens e confirma marcador,
  atividade e evento `ENVIO_FINALIZADO`. Sucesso 200 vazio; conflito 409 sem efeitos.
  Statement/COMMIT falhos revertem a finalização e permitem nova tentativa.
- Marcador no domínio e guardas em recepção, disparo, elegibilidade, seleção e
  revalidação de execução impedem mutação após fechar. Consulta histórica mantém
  estado/conteúdo sem atividade ou evento novo. Nova abertura da mesma competência
  cria chave distinta, preserva histórico e mantém unicidade da abertura vigente.
- V9 adiciona `remessas.finalizada_em TEXT` nullable e recria somente o índice
  parcial `remessa_aberta_unica`. Não reconstrói tabelas nem altera V1–V8, fontes,
  schemas, dependências ou configuração. `estado` legado não decide sozinho.

## Validação executada

Comandos inspecionados: `pom.xml`, wrapper Maven 3.9.9, migrações e testes SQLite
existentes. JDK 21, Maven local/cache público; nenhum comando acessa produção.
Prefixo efetivo em todas as execuções:

```sh
rtk proxy env JAVA_HOME=/Users/luka/.sdkman/candidates/java/current \
  MAVEN_USER_HOME=/private/tmp/fake-esfinge-wrapper-check-fixed ./mvnw -B -ntp \
  -Dmaven.repo.local=/private/tmp/fake-esfinge-m2
```

| Execução | Resultado | Tempo Maven / handle |
|---|---|---|
| RED nominal `-Dtest=FinalizacaoHttpTest test` antes da produção | 1 teste, falha esperada `Status expected:<200> but was:<422>`, exit1 | 3,844s / 92982, encerrado |
| GREEN mesmo teste nominal | 1 teste aprovado, exit0 | 4,067s / 8654, encerrado |
| HTTP ampliado | 7 testes aprovados, exit0 | 5,144s / 5562, encerrado |
| JDBC inicial | 8 testes, uma expectativa incorreta de autenticação na fixture, exit1 | 4,308s / 37782, encerrado |
| Focal/regressão inicial | 62 testes, uma contagem antiga indevidamente ajustada no teste de saldo, exit1 | 9,271s / 51315, encerrado |
| Focal/regressão corrigida | 69 testes aprovados, exit0 | 10,629s / 64734, encerrado |
| `verify` completo final | **155 testes, zero falhas/erros/ignorados**, jar empacotado, exit0 | **16,413s** / 34189, encerrado |

Os 18 novos testes são `FinalizationTest` (3), `FinalizacaoHttpTest` (7) e
`JdbcFinalizationTest` (8). Os loops HTTP verificam obrigatoriedade/tipos/formatos
em todos os 17 campos, onze constantes zero, cobertura ausente, extras, transporte,
autenticação/isolamento, seis assuntos e multiplicidade. Há casos vazia, RECEBIDO,
EM_PROCESSAMENTO, lote posterior não agendado, divergência e total enorme.

SQLite real verifica 10.001 registros repetidos, conteúdo/bytes e fechamento após
reinício; outros envios/UGs/cenários; concorrência de finalização, recepção,
abertura e última transição; guarda de efeito tardio; upgrade V8 com os seis
assuntos, trabalhos em três fases, efeitos, config/offset, sessões, dinheiro TEXT,
checksums e sequências preservados. Falha após ALTER de V9 reverte a coluna;
checksums adulterados de V8/V9 impedem abertura sem recriar dados. FKs permanecem
válidas. Fixtures pré-V9 usam SQL legado somente em testes, nas operações que
precisam semear o schema sem marcador; produção não detecta schema dinamicamente.

As duas falhas intermediárias de teste foram corrigidas: token do cenário B deve
ser usado no serviço B para provar 404 sobre chave A; contagem de oito valores de
saldo permanece oito, somente expectativas de versão migram de 8 para 9. Nenhuma
falha remanescente observada. `git diff --check` passou; código/testes congelados
após o verify. Avisos herdados de Mockito/ByteBuddy/JDK não impediram validação.

## Supervisão final

Equipe própria: explorer `grupo10_explorer` identificado GPT-6 Luna/médio,
executor `grupo10_executor` GPT-6.1 Sol/baixo e reviewer `grupo10_reviewer` no
papel configurado GPT-6.1 Sol/médio. O reviewer autodeclarou Codex/GPT-6 sem
variante/esforço expostos na sua interface. Revisão independente somente leitura
aprovou código conectado, testes e migração, sem achados materiais; nenhuma
correção de produção adicional foi necessária. Supervisor não alterou produção.

Supervisor repetiu `clean verify` após congelamento: **155 testes**, zero
falhas/erros/ignorados, BUILD SUCCESS, exit0, **17,517s**. Os 21 relatórios Surefire
confirmaram o total. Somente registros/documentação mudaram depois da suíte.
Jar `target/fake-esfinge-0.1.0.jar`, SHA-256:
`9013ce34eaf38edf6a72fd7be4ac99a2145626f67ba561d3fde90f11e92158b8`.

### Docker Linux amd64

Preparação em imagem V8 real já aprovada da #9, `fake-esfinge:issue-9-local`, ID
`sha256:9bb42803b3815e6a77569fd328d0d90135324683f78a20b05b7e4570620ae874`:
`ContainerCheck9 first` 506 e `ContainerCheck10 before` 32, total **538 verificações**,
exit0. A base contém seis assuntos, 10.001 créditos, registros efetivos, configuração
e offset persistidos; uma remessa adicional tem trabalho EM_PROCESSAMENTO,
outro agendado RECEBIDO e lote recebido depois do snapshot. Captura anterior
à migração soma 3 verificações e registra contagens/hashes das quinze tabelas V8,
sem imprimir dados/tokens. Container V8 removido com o volume preservado.

Imagem final `fake-esfinge:issue-10-local`, ID
`sha256:772dd24b909bc0616cec7cdb6d475c07d8edac1eda5821c23d62b53e3910ea3f`,
Linux amd64, Java Temurin 21.0.10+7, UID/GID 10001:10001. Jar interno tem o mesmo
SHA-256 da suíte limpa. Instância `fixed`, sem rede (`--network none`) ou portas
publicadas; `/data` no volume fictício e `/checks` somente leitura.

| Cliente na imagem final V9 | Verificações | Resultado observado |
| --- | ---: | --- |
| `MigrationSnapshot10 after` | 20 | Quinze tabelas V8 preservadas por colunas/contagens/hashes, integridade/FKs, V9 aplicada e marcador novo nulo nas remessas antigas |
| `ContainerCheck10 first` | 1.053 | Retomada das fases V8; finalização impedida durante processamento e com lote fora do snapshot; seis assuntos em ciclos, 17 campos, erros sem efeitos, totais enormes/zeros iniciais, 10.001 créditos, uma finalização/abertura vencedora nas disputas, 200 vazio e histórico imutável |
| `ContainerCheck10 restart` | 149 | Fechamento persistido, mutações repetidas 409, consulta histórica sem renovar atividade/eventos e nova chave na mesma competência após reinício |
| `SqliteCheck10` | 57 | Três fechamentos/eventos e cinco envios abertos; 21 trabalhos e 10.021 registros processados/efetivos, 10.016 valores recebidos TEXT, campos/índices/origem/decimais preservados, índice parcial, relógio e FKs íntegros |

Total: **1.279 verificações na imagem final V9**, todas exit0; as 538 da preparação
V8 e 3 do snapshot anterior são separadas. Verificações de conteúdo percorrem as
linhas recebidas/processadas/efetivas por índice, sem usar os totalizadores da
aplicação como esperado independente. Consultas históricas foram feitas após
avanço explícito de 1 ms para provar que não renovam atividade. Logs comuns tiveram
somente duas linhas `Simulador iniciado`; clientes SQL emitiram aviso SLF4J sem
provedor, sem falhar. Nenhum dado original do piloto ou chamada ao TCE.

Container temporário parado/removido e ausência confirmada. Volume fictício
`fake-esfinge-issue10-supervisao` preservado no contexto `orbstack` até autorização
específica de exclusão; nenhuma tentativa de apagá-lo. Imagem e clientes em
`/private/tmp/fake-esfinge-issue10-supervisao` preservados localmente. Push/PR/merge
da #10, publicação de imagem e implantação não realizados.

Após autorização específica posterior, o volume `fake-esfinge-issue10-supervisao`
foi excluído no contexto `orbstack`, exit0, com ausência confirmada. Nenhum outro
volume foi excluído; imagem/clientes locais preservados.

## Publicação e integração

Push da branch `issue-10-finalizacao`, criação e merge do
[PR #37](https://github.com/lukadev08/fake-esfinge/pull/37) realizados com autorização
específica. Head revisado `d47c751c593ff2f48ac423c62c04845db17851a3`, base
`eb376d07fd8efe915d2588cb69e4a0f41b1dcf75`; comparação remota confirmou três commits
preparados, sem divergência de base e sem conflitos. Merge protegido pelo SHA
esperado produziu `4de2ba5e8c799e388ecd5c6a89bcc1a33d32ff1f`. GitHub confirmou
PR merged, #10 completed, `main` no merge e #8 aberta. PR anexado à tarefa.
Checks/statuses vazios no head; nenhuma evidência de CI executado. Código não
mudou desde a suíte aprovada; não foi necessário repetir testes nesta publicação.

## Limites

Parcelas cobertas: T01/T02/T03/T10/T13/T14/T15/T18. Testes fictícios não comprovam
T01 integral, aceite humano #8/E1, piloto E3, regras fiscais ou homologação oficial.
Descarte, cancelamento, recuperação, reset, expiração, sem movimento e T17
permanecem fora da implementação #10. Recuperação futura deve integrar o filtro de
lotes ativos e excluir descartados/abortados dos totais; esses estados ainda não
existem nesta versão. CI remoto, integrador real, publicação de imagem e implantação
não executados. Integração da #10 não encerra E2 integral nem os aceites humanos.
