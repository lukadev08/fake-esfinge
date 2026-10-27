# Evidência local — issue #11

Execução em 26/10/2026, branch `issue-11-erro-assincrono`, base funcional
`4de2ba5` (PR #37/#10), preparação documental HEAD `a7beb04`. Dados exclusivamente
fictícios, SQLite temporário; nenhum integrador real, TCE ou dado de produção.
Executor `grupo11_executor`, papel configurado GPT-6.1 Sol/esforço baixo;
modelo/esforço efetivos não expostos pelo runtime, sem inferência.

## Entrega e decisões

API administrativa GET/PUT/DELETE de plano por lote/geração, JSON estrito com
índice inteiro ou null, autenticação própria e imutabilidade após início.
Erro acontece na conclusão: descarte integral do alvo e aborto atômico de todos
os seguintes não concluídos do mesmo assunto/remessa/UG/cenário. Inclui trabalhos
EM_PROCESSAMENTO e lotes recebidos fora do snapshot. Anteriores válidos,
posteriores já concluídos e outros assuntos/envios/UGs/cenários permanecem.
Diagnósticos próprios fixos, sem CON ou mensagem oficial; consulta HTTP 200 inclui
`retornoErros.erros` somente nos terminais, preservando `registro:null` explícito.

V10 acrescenta `ativo` aos lotes e reconstrói somente trabalhos para estados e
diagnóstico, preservando IDs/sequências/índice/tempos/latência/geração. Plano tem
FK composta com lote/remessa/cenário/UG. Payload/hash/quantidade original são
históricos imutáveis; exclusões ativas seguem efetivos→processados→valores.
Inspeção distingue ativo, diagnóstico e registros/contagens históricos.
Finalização exclui inativos e exige ao menos um ativo em sucesso. Terminais não
voltam à execução; conclusão revalida trabalho/geração/abertura/atividade do lote.

## TDD e validação

Comando inspecionado: Maven Wrapper 3.9.9, Java21, dependências existentes, testes
locais em bases temporárias e empacotamento local. Sem Docker ou publicação pelo
executor. Todos os comandos utilizaram RTK/proxy, mantendo diagnósticos/exit codes.

```sh
rtk proxy env JAVA_HOME=/Users/luka/.sdkman/candidates/java/current \
  MAVEN_USER_HOME=/private/tmp/fake-esfinge-wrapper-check-fixed \
  ./mvnw -B -ntp -Dmaven.repo.local=/private/tmp/fake-esfinge-m2 verify
```

| Execução | Resultado |
|---|---|
| RED vertical `ProcessamentoHttpTest#erroIntermediarioDescartaInteiroEAbortaSeguintesSemAtingirOutroAssunto` | 1 teste/1 falha esperada: PUT administrativo retornou404 em vez de200; exit1; 3,804s |
| GREEN do mesmo teste | 1 teste, zero falhas/erros/ignorados; exit0; 4,010s |
| JDBC focal inicial, incluindo regressões | 65 testes verdes; exit0; 9,351s |
| HTTP/JDBC ampliados | 33 testes verdes; exit0; 8,392s |
| JDBC de falha final | 21 testes verdes; exit0; 5,243s |
| HTTP final | 23 testes verdes; exit0; 7,662s |
| `verify` completo único do executor | **186 testes**, zero falhas/erros/ignorados; exit0; **19,947s** |

Foram conferidos 22 relatórios Surefire: total186/failures0/errors0/skipped0.
Avisos herdados Mockito/ByteBuddy/JDK sobre autoanexação de agente não falharam
validação. Todos os handles de comandos foram encerrados; nenhum build pendente.
Jar gerado: `target/fake-esfinge-0.1.0.jar`, SHA-256
`288616233e1bbe9b62b093b6451535370f2b9cc0c5ad2c97b2521bc399fafbe1`.

Os 31 novos casos (21 JDBC e 10 HTTP com MockMvc/Spring/SQLite real) cobrem:

- Primeiro/intermediário/último lote; seis assuntos; índice 1 em lote com dois
  registros e null sem localização; preservação de bytes/hash/zeros/decimais.
- Aborto de seguintes RECEBIDO/EM_PROCESSAMENTO/fora do snapshot, preservando
  anteriores/outros assuntos/UGs/cenários/envios e posterior já concluído.
- Finalização só dos ativos, conflito sem ativos, reinício antes/depois,
  isolamento de geração e conflitos administrativos após início/sucesso/fechamento.
- JSON/tipos/overflow/campos extras/duplicatas/trailing tokens, autenticação
  própria, GET sem escrita/avanço, atividade não renovada, DELETE200 vazio.
- Falhas reais de statement e COMMIT após exclusões no alvo e na propagação;
  rollback do grupo e retomada. Plano/evento também atômicos; HTTP500 sanitizado
  sem token/chave/senha/SQL/stack trace no corpo ou logs.
- Concorrência entre instâncias manual/automático e configuração/início;
  conclusão calculada antes do aborto rejeitada, sem efeito tardio/duplicado.
- Migração V9 preenchida com seis assuntos/trabalhos em fases distintas/remessa
  fechada/configuração/offset/sequências; comparação integral das tabelas antigas,
  rollback V10, checksum incompatível, FKs e high-water sem trabalhos remanescentes.

Fixtures antigas só ajustaram expectativas de versão e comparação dos campos
aditivos; nenhuma detecção dinâmica de schema foi adicionada à produção.
V1–V9, fontes, schemas, build/dependências e arquivos `.DS_Store` preservados.
`git diff --check` passou. Nenhuma mutação Git realizada pelo executor.

## Limites e próximo passo

Injeção é aproximação de T04/T06, sem validação tributária T04. Não implementa
recuperação/descarte explícito/cancelamento/reset, nem conclui #8/E1, T01/E3 ou
homologação oficial. MockMvc verifica transporte/serviços/persistência conectados;
HTTP de rede no jar/imagem e supervisão Linux ficam para o supervisor.
Na entrega inicial, revisão independente e supervisão final ainda estavam pendentes;
a consolidação final consta abaixo. Publicação/merge da #11 não autorizados.
Registros PROGRESS/.scratch pertencem ao supervisor; executor não os alterou.

## Correção P2 — fronteira dos DTOs HTTP

Corretor separado `grupo11_correcao`; modelo/esforço efetivos não expostos pelo
runtime. Revisão independente identificou objetos do núcleo nas respostas HTTP,
incompatíveis com `ARCHITECTURE.md` §3. GET/PUT do plano agora retornam
`ProcessingController.FailurePlanResponse`; consulta e inspeção incorporam
`DiagnosticResponse`, record do adaptador compartilhado. Conversões explícitas
nos três controllers preservam nomes/tipos, `registro:null` e ausência de
`retornoErros` nos estados bons. Núcleo, SQL, portas e testes não foram alterados.

Verificação estática das assinaturas confirmou ausência de `Processing.FailurePlan`
e `Processing.Diagnostic` nos DTOs/retornos HTTP. Testes comportamentais existentes
foram reutilizados, sem teste estrutural que apenas espelhe os records:

| Execução após correção | Resultado |
|---|---|
| `-Dtest=ProcessamentoHttpTest,FinalizacaoHttpTest test` | 30 testes, zero falhas/erros/ignorados; exit0; 9,337s |
| `verify` completo único do corretor | 186 testes, zero falhas/erros/ignorados; exit0; 19,833s |

Mesmo comando Java21/RTK/proxy/cache documentado acima. Handles98202 e74729
encerrados com exit0; sem build concorrente ou pendente. Avisos herdados de
autoanexação Mockito/ByteBuddy permaneceram sem falha. Correção congelada após
validação; revisão pelo mesmo reviewer, clean verify e Docker finais pertencem
ao supervisor. #8/E1/T01/E3 e publicação da #11 continuam pendentes.

## Supervisão final — consolidação em 27/10/2026

Equipe própria: `grupo11_explorer`, `grupo11_executor`, `grupo11_reviewer` e
`grupo11_correcao`. Papéis configurados respectivamente GPT-6 Luna/médio,
GPT-6.1 Sol/baixo, GPT-6.1 Sol/médio e GPT-6.1 Sol/baixo; identificação de runtime
não foi inferida quando indisponível. O mesmo reviewer reconferiu os mapeamentos
e consumidores após a correção, aprovando sem achados materiais remanescentes.
Supervisor não alterou produção, núcleo, SQL ou testes.

Supervisor repetiu `clean verify` nas fontes finais congeladas: **186 testes**,
zero falhas/erros/ignorados, BUILD SUCCESS, exit0, **21,608s**; os 22 relatórios
Surefire confirmaram os totais. Somente documentação/registros mudaram depois.
Jar final `target/fake-esfinge-0.1.0.jar`, SHA-256:
`aadc64093bce472c8f868196550e081a6f0bc3a6fd828569f56df2592136d836`.
Hashes anteriores acima pertencem às execuções anteriores à suíte limpa final.

### Docker Linux amd64

Preparação em imagem V9 real aprovada, `fake-esfinge:issue-10-local`, ID
`sha256:772dd24b909bc0616cec7cdb6d475c07d8edac1eda5821c23d62b53e3910ea3f`:
`Prepare11` 105 verificações, exit0, criando remessa fechada com seis assuntos e
10.001 créditos, remessa aberta com trabalho EM_PROCESSAMENTO, outros RECEBIDO,
lote fora do snapshot e config/offset persistidos. Snapshot anterior soma 3
verificações e captura contagens/hashes das quinze tabelas V9 sem imprimir dados
ou tokens. Container V9 removido, preservando volume para upgrade.

Imagem final `fake-esfinge:issue-11-local`, ID
`sha256:bdff7c1be40cdd45c3ac5e2105225839ad0903abff27c9704bf00138c7a0bae3`,
Linux amd64, Java Temurin 21.0.10+7, UID/GID 10001:10001. Hash interno do jar igual
ao da suíte limpa. Duas instâncias sem rede (`--network none`) ou portas publicadas,
`/checks` somente leitura e volumes fictícios separados. Manual usa `fixed`;
automático usa `system` e a mesma porta de processamento.

| Cliente na imagem final V10 | Verificações | Resultado observado |
| --- | ---: | --- |
| `MigrationSnapshot11 after` | 20 | Quinze tabelas V9 preservadas por colunas/contagens/hashes, FKs/integridade e todos os lotes antigos ativos; remessa fechada e fases anteriores conservadas |
| `ContainerCheck11 configure` | 193 | API administrativa de rede, rejeições sem efeitos, índice 1/null/tipos/limites, edição/remoção antes do início e configuração sem renovar atividade |
| `ContainerCheck11 first` | 537 | Plano persistido após reinício, aborto de lote fora do snapshot, preservação de anteriores/outro assunto, primeiro/intermediário/último, bytes/histórico/decimais, diagnóstico 200 com null, finalização só de ativos e 409 sem ativos |
| `ContainerCheck11 restart` | 384 | Terminais, diagnósticos, planos e arquivo histórico persistem; nenhum efeito ou trabalho reaplicado; remessa V9 fechada preservada |
| `SqliteCheck11` | 89 | 26 controles, 16 ativos/10 inativos, 10.027 registros originais/11 inativos, 10.016 processados e efetivos, 10.015 valores TEXT exatos, cinco erros/cinco abortos, 16 sucessos, vínculos/FKs/índices/high-water e offset 1735 |
| `ContainerCheck11 auto` | 332 | Progresso automático verificado por leitura SQL sem consultas compatíveis ou avanço; outros assuntos preservados; disputa com 12 avanços manuais não duplica eventos/efeitos nem conserva dinheiro inativo |

Total na **imagem final: 1.555 verificações**, todas exit0. As 105 de preparação V9
e 3 do snapshot anterior são contabilizadas separadamente. Esperados de conteúdo
vêm dos payloads fictícios conhecidos e índices, sem usar contagem calculada pela
aplicação como oráculo. Comparações percorrem cada campo/registro/valor monetário.
Logs comuns somaram quatro linhas `Simulador iniciado` entre instâncias/reinícios;
avisos SLF4J dos clientes SQL não falharam ensaios. Nenhum dado original ou TCE.

Containers parados/removidos e ausência confirmada. Preservados no contexto
`orbstack` os volumes fictícios `fake-esfinge-issue11-supervisao` e
`fake-esfinge-issue11-supervisao-auto`, até autorização específica de exclusão.
Imagem/clientes em `/private/tmp/fake-esfinge-issue11-supervisao` preservados
localmente. Push/PR/merge, publicação de imagem e implantação da #11 não realizados.

Implementação técnica pronta para publicação específica. Injeção continua aproximação,
sem regra fiscal T04; #8/E1/T01/E3, recuperação integral, CI remoto e homologação
oficial permanecem pendentes. A próxima recuperação por assunto pertence à #12.
