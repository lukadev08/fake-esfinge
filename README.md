# fake-esfinge

Simulador HTTP local do e-Sfinge Tributário do TCE/SC,
independente da origem dos dados e sem encaminhar chamadas ao TCE.

E0 está concluído documentalmente no recorte inicial. As fatias E1
[#1](https://github.com/lukadev08/fake-esfinge/issues/1) e
[#2](https://github.com/lukadev08/fake-esfinge/issues/2) e
[#3](https://github.com/lukadev08/fake-esfinge/issues/3) e
[#4](https://github.com/lukadev08/fake-esfinge/issues/4) e
[#5](https://github.com/lukadev08/fake-esfinge/issues/5) e
[#6](https://github.com/lukadev08/fake-esfinge/issues/6) e
[#7](https://github.com/lukadev08/fake-esfinge/issues/7) implementam autenticação
fictícia, abertura de envio, recepção dos seis assuntos, consulta `RECEBIDO` e
inspeção administrativa com persistência SQLite. A fatia E2 #9 acrescenta disparo
explícito e processamento estrutural persistido. A #10 acrescenta finalização
com totalizadores efetivos e histórico preservado. Reset e painel permanecem pendentes.
A saída E1 depende do aceite humano da #8; testes locais não comprovam T01.
A continuidade técnica E2 autorizada está delimitada na
[seção 13.1 do planejamento](docs/planejamento-simulador-tributario.md#131-pendência-e1-e-continuidade-de-e2).

## Documentação

- [Planejamento de desenvolvimento](docs/planejamento-simulador-tributario.md):
  fonte principal de escopo, etapas E0–E4, invariantes e aceite T01–T19.
- [Arquitetura de código](ARCHITECTURE.md): stack adotada, portas e adaptadores,
  domínio, fluxo de dados e práticas de implementação e testes para agentes.
- [Contrato E0 consolidado](docs/contrato-e0.md): inventário relatado,
  comportamentos definidos, cobertura e pendências do piloto.
- [Mapa local E0](.scratch/wayfinder-e0/map.md): decisões e oito tickets locais;
  rascunhos sem publicação no GitHub.
- [Orientações para agentes](AGENTS.md) e configuração de
  [rastreador](docs/agents/issue-tracker.md),
  [rótulos](docs/agents/triage-labels.md) e
  [documentação de domínio](docs/agents/domain.md).

Perfil selecionado pelo usuário: 2026 / V6 / leiaute 6.06. O recorte inicial
prioriza recepção, validação estrutural, sequência, conteúdo e contagens.
Validação de vínculos entre assuntos e regras tributárias fica para evolução;
o piloto inicial não equivale ao aceite integral do MVP ou T01.

As versões HTML, PDF e DOCX do planejamento são apresentações da versão
anterior e não foram regeneradas nesta revisão de 26/10/2026. Consulte o Markdown
para as decisões atuais. Os comandos abaixo validam somente a fatia implementada.

Dados originais do piloto permanecem no ambiente local controlado, fora do Git
e de serviços externos. Exemplos e evidências compartilhadas exigem sanitização;
credenciais e tokens do simulador serão fictícios.

## Executar e validar as fatias #1–#14

Requer JDK 21. O wrapper oficial fixa Maven 3.9.9; o primeiro uso baixa Maven e
as dependências do Maven Central. O build fixa Spring Boot 3.5.10, SQLite JDBC
3.49.1.0 e ArchUnit 1.4.0. Não acessa o TCE ou bases do integrador.

```sh
./mvnw verify
java -jar target/fake-esfinge-0.1.0.jar
```

A suíte cobre núcleo, HTTP, SQLite temporário e dependências de classes. A base
padrão fica em `data/simulator.sqlite`, ignorada pelo Git. Reiniciar conserva
configuração, vínculos, sessões e lotes recebidos. A migração V2 acrescenta remessas,
lotes e eventos sem alterar a V1; versões/checksums anteriores são conferidos antes
da atualização. A V3 conserva lotes, eventos, sessões e sequências existentes,
permite crédito e acrescenta os valores monetários em TEXT. A V4 conserva esses
dados ao permitir arrecadação e reunir os valores na tabela `valores_recebidos`.
A V5 permite baixa, preservando lotes, eventos e dinheiro dos assuntos anteriores.
A V6 permite saldo inicial com quatro valores por registro e nomeia os valores
antigos pelo assunto, mantendo TEXT e todos os dados. A V7 admite início do prazo
prescricional, preservando os dados e os valores nomeados V6; prazo não gera
linhas monetárias. A V8 acrescenta trabalhos, configuração, deslocamento de tempo e
registros processados/efetivos sem reescrever V1–V7 ou o recebido. A V9 acrescenta
`remessas.finalizada_em` e atualiza o índice de abertura sem reconstruir tabelas
ou alterar V1–V8. Schema, migração ou configuração persistida incompatíveis
interrompem a inicialização sem recriar dados. Não há reset nesta
fatia. Para outra execução, use outro arquivo/volume; preserve o anterior.

Exemplo inteiramente fictício, sem corpo na requisição:

```sh
curl -i -X POST 'http://localhost:8080/autenticacao/login?codigoUg=0001&descricaoEmpresaTI=EmpresaFicticia&descritivoSoftware=SoftwareFicticio' \
  -H 'codigoAcesso: acesso-ficticio-a' -H 'senha: senha-ficticia-a'
curl -i 'http://localhost:8080/__simulator/configuracao' \
  -H 'X-Simulator-Key: admin-local-ficticio'
```

O login devolve `{"chave":"sim-..."}`; use esse valor em `auth_token`. Credenciais
inválidas retornam 401; UG não autorizada, 403; parâmetros/corpo inválidos, 400.
As rotas contratuais conhecidas ainda indisponíveis validam o token e retornam
422 `SIM_COBERTURA_AUSENTE`; não aprovam dados fiscais.
Recurso inexistente retorna 404. Erros têm `codigo`, `mensagem` sanitizada e `correlacao`.

A inspeção administrativa usa exclusivamente `X-Simulator-Key`; não aceita o
token compatível e não retorna senhas, códigos de acesso ou tokens. Mostra perfil,
cenário, seed, tempo inicial, modo do relógio, validade, geração e IDs/UGs fictícios.
Os logs padrão ficam desabilitados para evitar detalhes de frameworks/SQL; o
processo imprime somente o resultado genérico da inicialização. Erros HTTP
permanecem consultáveis com correlação. Não habilite logs de requisição ou SQL
sobre dados originais.

## Configuração local

O exemplo inicial está em [application.properties](src/main/resources/application.properties).
Sobrescreva propriedades por arquivo externo com
`--spring.config.additional-location=file:/caminho/simulador.properties` ou
argumentos Spring. Não versione arquivos locais com dados do piloto.

| Propriedade | Exemplo / efeito |
|---|---|
| `simulator.database` / `SIMULATOR_DATABASE` | Arquivo SQLite local exclusivo da execução |
| `simulator.scenario` | ID do cenário, string sem transformação |
| `simulator.profile` | Somente `2026/V6/6.06` implementado; outros são recusados |
| `simulator.seed`, `simulator.initial-time` | Contexto rastreável; instante ISO-8601 |
| `simulator.clock-mode` | `system` acompanha UTC; `fixed` conserva o instante configurado |
| `simulator.token-seconds` | Inteiro positivo; padrão 3600, decisão aproximada local |
| `simulator.admin-key` / `SIMULATOR_ADMIN_KEY` | Chave fictícia administrativa, diferente das credenciais |
| `simulator.users[0].id`, `.access-code`, `.password`, `.ugs` | Identidade fictícia e UGs separadas por vírgula, preservando zeros |

Outros usuários usam índices consecutivos `[1]`, `[2]` etc. Senhas são guardadas
com PBKDF2 e salt; tokens persistem somente por hash. Alterar configuração de um
cenário já persistido exige arquivo/volume novo; esta fatia não migra identidades
nem altera identidades da execução ativa. A #9 permite avanço de tempo pelo
controle de processamento abaixo; a #16 acrescenta a expiração de envios pelo
mesmo relógio, conforme a seção de expiração abaixo.

## Docker mínimo Linux amd64

Empacote com `./mvnw verify` antes do build. A imagem usa Java 21 fixado por digest
amd64 e usuário sem privilégios; não inclui ferramentas de desenvolvimento.

```sh
docker build --platform linux/amd64 -t fake-esfinge:0.1.0 .
docker volume create fake-esfinge-execucao
docker run --name fake-esfinge-execucao --platform linux/amd64 \
  -p 127.0.0.1:8080:8080 --mount source=fake-esfinge-execucao,target=/data \
  fake-esfinge:0.1.0
docker restart fake-esfinge-execucao
```

O volume conserva o estado entre reinícios. Compose, CI e guia operacional
completo pertencem à #21. O runtime não contém cliente/proxy para o TCE.
Consulte [cobertura atual](docs/cobertura.md),
[evidência da #1](docs/evidencias/issue-1.md) e
[evidência da #2](docs/evidencias/issue-2.md) e
[evidência da #3](docs/evidencias/issue-3.md) e
[evidência da #4](docs/evidencias/issue-4.md) e
[evidência da #5](docs/evidencias/issue-5.md) e
[evidência da #6](docs/evidencias/issue-6.md) e
[evidência da #7](docs/evidencias/issue-7.md) para resultados e limites.

## Abrir, receber e consultar contribuintes — #2

Use o token fictício do login em `auth_token`. A abertura pertence à UG do token,
a um cenário e a um mês de 2026; `cancelamentoAutomatico=NAO` é obrigatório para
esta capacidade. Ausência ou outro valor retorna 422. Parâmetros malformados
retornam 400; abertura repetida/concorrente para a mesma UG/competência retorna
409. Outras UGs, competências ou cenários têm aberturas independentes.

```sh
curl -i 'http://localhost:8080/v6/tributario/iniciarEnvio?anoMes=202601&cancelamentoAutomatico=NAO' \
  -H 'auth_token: <chave-do-login>'
curl -i -X POST 'http://localhost:8080/v6/tributario/enviarParcialLote?chavePacote=<chave-do-envio>' \
  -H 'auth_token: <chave-do-login>' -H 'Content-Type: application/json' --data-binary @- <<'JSON'
{"cadastrosContribuintes":[{"numeroMatriculaContribuinte":"0007","dataAtualizacaoCadastral":"2026-01-02","tipoContribuinte":"1","numeroDocumentoContribuinte":"00000000000","nomeCompletoContribuinte":"Pessoa Ficticia","indicativoCadastroAtivo":"S","indicativoEspolio":"N"}]}
JSON
curl -i 'http://localhost:8080/v6/tributario/consultarPorNumeroLote/<numeroLote>' \
  -H 'auth_token: <chave-do-login>'
curl -i 'http://localhost:8080/__simulator/lotes/<numeroLote>' \
  -H 'X-Simulator-Key: admin-local-ficticio'
```

O corpo tem a lista diretamente na raiz, sem wrapper `lote`. A recepção retorna
`situacao=RECEBIDO`, `numeroLote`, `dataEnvio` com offset e
`identificadorRemessaOnline`; a consulta usa o nome distinto `identificadorRemessa`.
Vários lotes são aceitos, mesmo com conteúdo idêntico: hash não deduplica, e não
há limite de 10.000 registros imposto pelo simulador.

A validação segue o schema de contribuinte: obrigatórios não admitem ausência ou
`null`; opcionais admitem ambos; extras e coerção de tipos são rejeitados. Datas
exigem calendário `YYYY-MM-DD`, comprimentos contam caracteres Unicode. O schema
não exige strings não vazias nos registros nem valida CPF, enums ou regras fiscais.
JSON malformado, chaves duplicadas, segundo documento e UTF-8 inválido recebem 400;
estrutura inválida e assuntos sem cobertura recebem 422.

Bytes originais, hash, ordem, vínculos, contagem e evento são confirmados na mesma
transação SQLite antes do HTTP 200. Falha de statement ou de commit retorna erro
sanitizado e reverte a operação. Consulta compatível registra atividade/evento,
mas não avança relógio ou processamento; dados recebidos continuam sem efeito.
Chave/lote de outra UG ou cenário retorna 404, sem mutação.

A inspeção de lote exige a chave administrativa própria e limita a leitura ao
cenário da instância. Retorna `numeroLote`, `identificadorRemessa`, `chavePacote`,
`cenario`, `codigoUg`, `anoMes`, `ordem`, `assunto`, `situacao`, `hash`,
`bytesBase64` (conteúdo original), `registros`, `recebidos`, `processados`,
`efetivos` e `eventos`. Estes últimos trazem ID, lote quando aplicável, tipo e
instante, sem credenciais ou payloads. As contagens processada/efetiva são zero
nesta fatia. A inspeção não renova atividade nem grava evento de consulta.

A suíte local exercita SQLite real, concorrência, rollback, falha real de commit,
isolamento, reabertura e migração V1→V2. A execução Docker da #2 e a revisão
independente são registradas separadamente pela supervisão; integrador real e
aceite E1/T01 permanecem pendentes.

## Receber e inspecionar créditos — #3

Use a mesma abertura, chave e rota de recepção, com lista direta
`lancamentosCreditosTributarios`. Contribuintes e créditos podem compor lotes
separados do envio; misturar listas em um lote recebe 422. Exemplo fictício:

```json
{"lancamentosCreditosTributarios":[{"anoLancamentoCreditoTributario":2020,"codigoLancamentoCreditoTributario":"000009","tipoOperacao":1,"tipoCredito":1,"numeroMatriculaContribuinte":"0007","anoMesCompetencia":202002,"dataLancamento":"2020-02-29","dataVencimento":"2020-03-01","valorLancamentoCreditoTributario":123456789012345678901234567890.12,"numeroCpfResponsavelOperacao":"00000000001","codigoEspecificacaoNaturezaReceita":1}]}
```

O schema `credito` controla obrigatórios, opcionais/nulos, comprimentos Unicode,
datas de calendário e extras. Competência do crédito admite meses históricos;
somente a abertura permanece restrita a 2026. Inteiros não recebem teto `int` ou
`long` inventado; números matematicamente integrais são permitidos sem converter
strings ou booleanos. Campos imobiliários/transferência fora do schema recebem 422.

Dinheiro é número JSON tratado com `BigDecimal`, múltiplo exato de 0,01.
`1`, `1.0` e `1.00` são aceitos; `1.001`, strings e arredondamento não são.
Cada valor é persistido como TEXT, vinculado ao lote/índice e UG/cenário, junto dos
bytes originais, metadados e evento na mesma transação. Não há passagem por ponto
flutuante na validação, persistência ou resposta administrativa. A grafia original
permanece em `bytesBase64`; a projeção numérica conserva o valor exato.

Consulta continua `RECEBIDO`, sem processamento. A inspeção existente mostra
`assunto=LANCAMENTO_CREDITO_TRIBUTARIO`, registros/contagens e conteúdo exato,
protegida pela chave administrativa. Vários lotes e registros repetidos não são
deduplicados. Cronologia, referências entre assuntos, catálogos externos e cálculos
tributários não estão implementados. Os testes locais da #3 não encerram E1/T01.

## Receber e inspecionar diário de arrecadação — #4

Use a mesma chave e rota de recepção com lista direta `diarioGeralArrecadacao`.
O schema `arrecadacao` valida oito obrigatórios e dois opcionais/nulos, tipos,
comprimentos Unicode, datas de calendário e extras. Exemplo fictício:

```json
{"diarioGeralArrecadacao":[{"anoCodigoSequencialOperacaoArrecadacao":2020,"codigoSequencialOperacaoArrecadacao":"000009","tipoArrecadacao":1,"dataOperacao":"2020-02-29","codigoEspecificacaoReceita":1,"anoLancamentoCreditoTributario":2019,"codigoLancamentoCreditoTributario":"000007","valorCreditoTributario":123456789012345678901234567890.12,"numeroParcelas":2}]}
```

`numeroParcelas` é inteiro opcional/nulo. O plural é aproximação confirmada pelo
cliente, divergente da extração original; `numeroParcela` recebe 422, sem alias.
`numeroAnoTipoCobranca` também é opcional/nulo. Não são validados catálogos,
cronologia, existência do crédito ou cálculos tributários.

`valorCreditoTributario` usa decimal exato, com a mesma política monetária dos
créditos. A V4 reúne os valores dos dois assuntos em `valores_recebidos`, por
lote/índice/UG/cenário, preservando os valores TEXT existentes na V3. Bytes,
metadados, valores e evento são confirmados atomicamente; falha de COMMIT não
confirma recepção. Contribuintes não recebem valores monetários.

Lotes separados dos três assuntos podem usar a mesma chave; listas mistas são
rejeitadas. Vários lotes e registros repetidos conservam multiplicidade, sem
limite artificial de 10.000. Consulta continua RECEBIDO, sem processamento.
A inspeção protegida apresenta assunto `DIARIO_ARRECADACAO`, valores exatos,
bytes e contagens. Reinício conserva os dados; testes locais não encerram E1/T01.

## Receber e inspecionar baixas de crédito — #5

Use a mesma chave e rota de recepção com lista direta `baixasCreditosTributarios`.
O schema `baixa` valida nove obrigatórios, dois opcionais/nulos, tipos,
comprimentos Unicode, data de calendário e extras. Exemplo fictício:

```json
{"baixasCreditosTributarios":[{"anoLancamentoCreditoTributario":2020,"codigoLancamentoCreditoTributario":"000007","codigoSequencialOperacaoBaixa":1,"tipoBaixa":1,"dataBaixa":"2019-02-28","valorOperacaoBaixa":123456789012345678901234567890.12,"indicativoEstornoBaixa":"N","motivoBaixa":"Motivo fictício","numeroCpfResponsavelOperacao":"00000000001"}]}
```

As referências de dívida ativa (`anoLancamentoCreditoTributarioDividaAtiva` e
`codigoLancamentoCreditoTributarioDividaAtiva`) admitem ausência/null. O ano é
inteiro e o código permanece string; não converter tipos nem remover zeros.
`valorOperacaoBaixa` segue a política BigDecimal/múltiplo exato de 0,01 e TEXT
por lote/índice, conservando os bytes originais e multiplicidades.

A V5 conserva dados, dinheiro, IDs, eventos, sessões, FKs e sequências da V4.
Lotes separados dos quatro assuntos podem reutilizar a mesma chave; misturas
são rejeitadas. Consulta continua RECEBIDO, sem processamento; inspeção protegida
mostra `BAIXA_CREDITO_TRIBUTARIO`, UG/cenário, registros, bytes e contagens.

Cronologia (inclusive baixa anterior ao crédito), existência do crédito/dívida,
catálogos, CPF e cálculos tributários não são validados. O exemplo demonstra
aceitação estrutural de uma data anterior ao ano do crédito, sem aprovação fiscal.
Reinício conserva o recebido; testes locais não encerram E1/T01.

## Receber e inspecionar saldo inicial em janeiro — #6

Abra o envio de uma UG fictícia com `anoMes=202601`, usando a mesma autenticação,
chave e rota de recepção. O corpo tem lista direta `saldosIniciaisCreditoTributario`.
Exemplo fictício com referência histórica e quatro valores distintos:

```json
{"saldosIniciaisCreditoTributario":[{"anoLancamentoCreditoTributario":2015,"codigoLancamentoCreditoTributario":"000007","saldoPrincipal":123456789012345678901234567890.12,"saldoMulta":2.30,"saldoJuros":4.56,"saldoAtualizacaoMonetaria":7.89}]}
```

O schema `saldo` exige ano inteiro, código string de até 16 caracteres e os quatro
números monetários. Ausência/null de qualquer um deles recebe 422; não há default
zero. `tipoSaldoInicial` é inteiro opcional/nulo e permanece ausente ou nulo quando
assim enviado. Sem catálogo de tipos, condição/default fiscal, existência de crédito
ou regra de abertura de exercício inferidos. Janeiro demonstra a recepção; não se
acrescenta proibição de outros meses sem contrato que a determine.

Todos os valores usam BigDecimal e múltiplo exato de 0,01, sem coerção ou
arredondamento. O núcleo carrega mapas imutáveis por registro, com campos exatos
por assunto. A V6 acrescenta `campo` a `valores_recebidos`, com chave
`(lote_id,indice,campo)` e `valor TEXT`: são quatro linhas nomeadas por registro de
saldo, preservando os valores escalares anteriores com o nome correto de cada
assunto. Bytes/hash, valores, vínculos, atividade e evento persistem atomicamente.
A migração conserva IDs, eventos, sessões, FKs e sequências; falha reverte a atualização.

Consulta continua RECEBIDO e a inspeção protegida mostra
`SALDO_INICIAL_CREDITO_TRIBUTARIO`, valores exatos, bytes e contagens, com
processados/efetivos zero. Reinício conserva o recebido; duplicatas não são
eliminadas. O cenário fictício de janeiro não substitui o integrador real nem
encerra T01/T16/E1. Se saldo faltar no piloto inicial, o cenário complementar
continua necessário.

## Receber e inspecionar início do prazo prescricional — #7

Use a mesma abertura, autenticação, chave e rota de recepção, com lista direta
`inicioPrazoPrescricional`. Exemplo fictício com referência histórica:

```json
{"inicioPrazoPrescricional":[{"anoLancamentoCreditoTributarioOuDividaAtiva":2015,"codigoLancamentoCreditoTributarioOuNumeroInscricaoDividaAtiva":"000007","dataInicioPrazoPrescricional":"2020-02-29","dataAtualizacaoPrazoPrescricional":"2010-01-01"}]}
```

O schema derivado `prazo` tem quatro campos obrigatórios: ano inteiro, código
string de até16 codepoints e duas datas de calendário `YYYY-MM-DD`. Ausentes/null,
extras, tipos incompatíveis, timestamps e datas impossíveis recebem422. Não há
coerção de strings em números nem limite fiscal inferido para o ano.

Os quatro campos foram aprovados a partir do exemplo; **não há DTO extraído**.
O exemplo aceita atualização anterior ao início para demonstrar ausência de
validação cronológica, sem afirmar prescrição fiscal ou vínculo válido com crédito
ou dívida ativa. Essas regras permanecem não implementadas.

Prazo usa conteúdo não monetário, com lista de valores vazia como cadastro.
Bytes/hash, referências, multiplicidade, contagens, atividade e eventos persistem
atomicamente. A V7 preserva lotes, quatro valores nomeados de saldo e demais
assuntos, IDs, sessões, eventos, FKs e sequências; falha reverte a migração.
Consulta mantém `RECEBIDO`; inspeção protegida mostra `INICIO_PRAZO_PRESCRICIONAL`,
registros e bytes, com processados/efetivos zero. Reinício conserva o recebido.
Se prazo não aparecer no piloto, cenário complementar pelo integrador real
continua necessário; esta fatia não encerra E1/T01 ou homologação oficial.


## Disparar e avançar processamento — #9

Após enviar os lotes, use POST `/v6/tributario/iniciar-processamento?chavePacote=<chave>`
com `auth_token` e sem corpo. O disparo captura todos os lotes ainda não agendados
da chave, sem escolher assunto. Retorna `situacao=RECEBIDO`, `dataEnvio` e
`identificadorRemessaOnline`. Disparo ativo, repetido ou sem elegíveis recebe409.
Lotes recebidos depois do disparo aguardam um novo disparo.

O controle interno exige `X-Simulator-Key`. GET `/__simulator/processamento`
consulta configuração e instante. PUT na mesma rota exige exatamente:

```json
{"modo":"MANUAL","latenciaMilissegundos":1000}
```

MANUAL/1000ms são padrões locais derivados. AUTOMATICO executa pela mesma porta,
em tarefas do adaptador; a configuração nova não reagenda trabalhos existentes.
POST `/__simulator/processamento/avancar` exige exatamente:

```json
{"milissegundos":1000,"passos":1}
```

`milissegundos` é inteiro não negativo; `passos` é inteiro positivo. Tipos, extras,
campos duplicados e overflow são recusados. Avanço e disparo retornam400 quando
um instante sai de `0000-01-01T00:00:00Z`–`9999-12-31T23:59:59.999999999Z`
(ano de quatro dígitos do timestamp contratual), ou exige deslocamento além do
`long` persistido. É limite técnico, sem restringir exercícios de créditos.
O disparo verifica início e conclusão de todos os lotes antes de criar trabalhos,
eventos ou alterar atividade; a latência configurada é conservada se rejeitado.
O avanço inválido não altera o relógio nem executa passos. O avanço funciona nos dois modos e
usa o mesmo relógio de negócio das sessões e remessas. O deslocamento persiste
sobre o relógio `fixed` ou `system`, inclusive após reinício. Consultas não avançam
esse relógio nem tarefas. Se o tempo real em `system` esgotar a janela técnica,
leituras do relógio e operações dependentes retornam400, sem emitir timestamp
incompatível nem alterar o deslocamento persistido, inclusive após reinício.
Cada passo executa uma transição elegível: RECEBIDO →
EM_PROCESSAMENTO → PROCESSADO_SUCESSO; início e conclusão são separados.
O avanço do relógio é confirmado antes dos passos; falha posterior de tarefa
reverte aquela transição, preservando o tempo já avançado e permitindo retomada.

A inspeção administrativa acrescenta `registrosProcessados` e `registrosEfetivos`.
Essas listas e suas contagens vêm de linhas persistidas por lote/índice, separadas
do recebido. O processamento revalida a estrutura pelo validador existente e,
sem regras fiscais nesta fatia, torna efetivos os registros estruturalmente válidos.
Duplicatas permanecem; strings, zeros e decimais exatos são conservados. Bytes
originais continuam imutáveis em `bytesBase64`. As descrições RECEBIDO/zero das
fatias #2–#7 acima representam o estado anterior ao disparo.

Consulte [evidência da #9](docs/evidencias/issue-9.md). A implementação técnica
não encerra o aceite humano E1/#8, T01, E2 completo ou homologação oficial.


## Finalizar envio — #10

Use POST `/v6/tributario/finalizarEnvio`, `auth_token`, JSON UTF-8 e nenhuma query.
O corpo exige `chavePacote` e todos os 17 totalizadores string do
[schema `finalizar`](docs/contratos/2026-v6-6.06/schemas.json).
Os seis assuntos aceitam somente dígitos ASCII, comparados numericamente sem
limite de inteiro fixo; zeros iniciais são aceitos. Os onze assuntos sem suporte
exigem string exata `"0"`; positivo retorna422 de cobertura. Campos ausentes,
nulos, tipos incorretos ou extras retornam422; JSON/mídia/query inválidos retornam400.

Há pelo menos um lote ativo e todos os lotes ativos devem estar em `PROCESSADO_SUCESSO`,
inclusive os recebidos após o último disparo. Totais contam linhas efetivas por
assunto e envio, conservando duplicatas; não somam dinheiro nem usam a contagem
de recepção. Pendência ou divergência retorna409 sem efeitos. Sucesso retorna200
com corpo vazio e confirma marcador/atividade/evento em uma transação SQLite.

Recepção, disparo e nova finalização na chave fechada retornam409. Consulta
histórica autorizada mantém estado/conteúdo, sem renovar atividade nem gerar
novo evento de consulta. Uma abertura posterior da mesma UG/competência retorna
chave nova e preserva o histórico, sem cota mensal adicional nesta fatia.
Reinício conserva o fechamento; falha de statement/COMMIT reverte a finalização.

Consulte [evidência da #10](docs/evidencias/issue-10.md). Descarte por assunto e
reenvio estão na fatia #12 abaixo; cancelamento integral está na #13. Sem movimento
e T17 seguem pendentes. Testes fictícios locais não encerram #8/E1, T01/E3 ou
homologação oficial. Publicação da #10 permanece sem autorização.


## Erro assíncrono controlado — #11

GET/PUT/DELETE `/__simulator/falhas/processamento/{numeroLote}` usam
`X-Simulator-Key` e nenhuma query. PUT exige JSON UTF-8 exato `{"registro": 0}`
ou `{"registro": null}`; índice inteiro zero-based menor que o total original.
GET/PUT retornam200 com `numeroLote` e `registro` explícito, inclusive null;
DELETE retorna200 vazio. Plano ausente/outro cenário retorna404. Configurar ou
remover exige envio aberto e lote ativo sem início (sem trabalho ou RECEBIDO);
EM_PROCESSAMENTO, sucesso, terminais e chave fechada retornam409 sem efeitos.
GET só lê; configuração não renova atividade. Plano persiste por lote/geração.

Na conclusão, o alvo fica `LOTE_DESCARTADO` com `SIM_ERRO_INJETADO`. A mesma
transação aborta seguintes não concluídos do mesmo assunto/remessa/UG/cenário,
inclusive EM_PROCESSAMENTO e recebidos após o disparo, com
`ABORTADO_ERRO_LOTE_ANTERIOR` e `SIM_ABORTADO_ERRO_LOTE_ANTERIOR`.
O índice localiza o diagnóstico; todo o lote é descartado, sem commit parcial.
Anteriores válidos, posteriores já concluídos e outros assuntos são preservados.
Consulta mantém HTTP200 e acrescenta `retornoErros.erros` apenas nos terminais,
com `codigo`, `mensagem` própria e `registro` (null presente quando não localizado).
Consultas não avançam processamento. Ambos os modos usam a mesma porta.

V10 mantém bytes/hash/quantidade original como arquivo histórico imutável,
marca o lote inativo e remove dados ativos recebidos/processados/efetivos.
Inspeção retorna `ativo`, `diagnostico`, contagens/listas ativas e
`recebidosHistoricos`/`registrosHistoricos`; `bytesBase64` sempre representa o
original histórico. Finalização conta somente ativos em sucesso e seus efetivos;
somente históricos não permitem disparo nem finalização. Reinício conserva planos,
diagnósticos e terminais; rollback reverte o grupo inteiro, inclusive exclusões.

Esta é injeção aproximada do simulador, sem regra fiscal/CON ou mensagem oficial.
Recuperação por descarte de assunto/reenvio está na #12 abaixo; cancelamento
integral está na #13. Rota explícita de lote e reset seguem pendentes. Veja
[evidência da #11](docs/evidencias/issue-11.md); #8/E1, T01/E3 e homologação seguem
pendentes. Publicação da #11 exige autorização específica.


## Descarte por assunto e reenvio — #12

`POST /v6/tributario/descarteParcialPorAssunto` exige `auth_token`, query exata
`chavePacote`/`assunto` e corpo vazio. Retorna200 com corpo vazio. `assunto` usa
um dos seis identificadores exatos de `$defs.descarteQuery` no
[schema derivado](docs/contratos/2026-v6-6.06/schemas.json), sem normalização.
Parâmetros ausentes/vazios/duplicados/extras ou corpo não vazio retornam400;
assunto fora da cobertura retorna422. Token inválido/expirado retorna401 antes
de efeitos; chave desconhecida/de outra UG ou cenário404; chave fechada409.

Qualquer trabalho RECEBIDO agendado ou EM_PROCESSAMENTO **da chave**, inclusive
de outro assunto, impede descarte com409 sem efeitos. Outra chave não bloqueia.
Sem ciclo ativo, a transação remove efetivos, processados e valores recebidos de
todos os lotes ativos do assunto, inativando-os e preservando demais assuntos,
chaves, UGs e cenários. Bytes/hash/quantidade original, planos e históricos são
conservados em V10, sem migração nova.

Sucesso histórico conserva `PROCESSADO_SUCESSO` sem diagnóstico, com evento
`DADOS_LOTE_REMOVIDOS`. RECEBIDO sem trabalho recebe `LOTE_DESCARTADO`, código
próprio `SIM_DESCARTE_ASSUNTO`, mensagem `Dados removidos por descarte do assunto`
e registro null. Essa limpeza de RECEBIDO é aproximação aprovada, divergente do
manual V6; não representa comportamento oficial nem implementa resposta perdida.
Históricos já inativos não são reescritos.

Assunto suportado sem dados ativos, inclusive repetição, retorna200 vazio e
renova atividade/registro `ASSUNTO_DESCARTADO_<ASSUNTO>`. Não há garantia oficial
de idempotência. Reenviar os mesmos bytes usa a mesma chave com novo ID/ordem,
sem deduplicação nem herança de plano antigo. Finalização considera somente
ativos em sucesso e seus efetivos; sem ativos retorna409.

Veja [evidência da #12](docs/evidencias/issue-12.md). Ensaios locais fictícios não
encerram #8/E1, T01/E3 ou homologação. Descarte explícito de lote,
resposta perdida, reset e expiração continuam fora desta fatia. Publicação/merge
da #12 autorizados e concluídos no [PR #39](https://github.com/lukadev08/fake-esfinge/pull/39).

## Cancelamento integral e nova chave — #13

`POST /servicosGerais/cancelarEnvioParcial` exige `auth_token`, query exata
`chavePacote` e corpo vazio. Retorna 200 com corpo exatamente vazio. Parâmetro
ausente/vazio/duplicado/extra ou corpo não vazio retorna 400; token inválido ou
expirado retorna 401 antes de efeitos; chave desconhecida/de outra UG/cenário,
404. Repetição e chave finalizada/cancelada retornam 409 sem efeitos.

O cancelamento alcança todos os assuntos do envio aberto, inclusive recebidos
sem trabalho, agendados e `EM_PROCESSAMENTO`. Uma transação encerra a chave,
remove dados ativos recebidos/processados/efetivos e inativa os lotes. Bytes,
hash, quantidade original, controles, planos e eventos permanecem históricos;
outras chaves, UGs, cenários e envios concluídos são preservados. Sucessos
históricos mantêm `PROCESSADO_SUCESSO` sem diagnóstico inventado. Pendentes
tornam-se `ABORTADO`, com `SIM_ENVIO_CANCELADO`, mensagem fixa
`Envio cancelado no simulador` e `registro: null`; essa política é derivada.

V11 distingue `cancelada_em` de `finalizada_em`, preservando V1–V10 e seus dados.
Trabalhos capturados antes do cancelamento revalidam a chave na gravação e não
reaplicam efeitos, inclusive após reinício. A geração de outros envios não muda.
Todas as mutações na chave cancelada retornam 409. Consulta histórica autorizada
continua 200, sem renovar atividade/eventos nem avançar processamento.

Nova abertura para a mesma UG/competência retorna chave distinta e permite
reenvio integral com novos IDs, sem deduplicação por hash ou herança de planos.
Envio aberto sem lotes também pode ser cancelado; envio já finalizado não pode.
Veja [evidência da #13](docs/evidencias/issue-13.md). Testes fictícios não encerram
#8/E1, T01/E3 ou homologação. Cancelamento automático, resposta perdida, reset,
expiração e regras fiscais ficam fora da fatia. Publicação/merge da #13 autorizados
e concluídos no [PR #40](https://github.com/lukadev08/fake-esfinge/pull/40).
Trabalho foi encerrado após a #13 a pedido do solicitante e retomado em 27/10;
[resumo do desenvolvimento](.scratch/resumo-ate-issue-13.md) registrado localmente.

## Interrupção antes da aceitação e resposta perdida após commit — #14

GET/PUT/DELETE `/__simulator/falhas/recepcao` exigem `X-Simulator-Key` e query
exata `chavePacote`/`assunto` (um dos seis nomes do descarte). PUT exige JSON UTF-8:

```json
{"tipo":"APOS_COMMIT","tentativa":2}
```

`tipo` admite `ANTES_ACEITACAO` ou `APOS_COMMIT`; `tentativa` é inteiro positivo
até `Long.MAX_VALUE`, relativo às próximas recepções elegíveis daquele envio e
assunto. Requisições inválidas/não autorizadas não contam. A saída de GET/PUT
contém `chavePacote`, `assunto`, `tipo`, `tentativa`, `tentativas`, `consumida` e
`recuperacaoPendente`. GET/DELETE não têm corpo; DELETE retorna 200 vazio.
Plano ausente retorna 404; parâmetros/corpo inválidos, 400; assunto fora da
cobertura, 422. PUT/DELETE exigem envio aberto; outro cenário retorna 404.
Administração não renova atividade, executa tarefas ou avança relógio.

Na tentativa escolhida, o servidor encerra a conexão **sem resposta HTTP**.
ANTES_ACEITACAO confirma somente o consumo do controle, sem lote, valores,
trabalho, atividade ou evento de aceitação. APOS_COMMIT confirma atomicamente
payload/valores/vínculos/evento `LOTE_RECEBIDO`, incidente e marca de recuperação;
o lote permanece `RECEBIDO`, sem processamento. Falha real de persistência
retorna 500 sanitizado e reverte também contador/marca; não simula desconexão.

Nova recepção do assunto marcado recebe 409 `SIM_ENVIO_ABERTO_SEM_PROCESSAMENTO`.
Outros assuntos e múltiplos lotes normais continuam permitidos. PUT/DELETE do
plano não contornam a marca: retornam 409 enquanto a recuperação estiver pendente.
Descarte bem-sucedido do assunto remove seus dados ativos e limpa a marca na
mesma transação; preserva plano consumido, eventos e bytes históricos. Reenvio
usa a mesma chave com IDs novos, sem deduplicação por hash. Cancelamento integral
limpa a marca e fecha a chave. Consultas não executam processamento.

V12 acrescenta somente a tabela de planos por remessa/assunto/geração, com FK
de cenário/UG; V1–V11 permanecem intactas. Reinício conserva seleção, consumo,
marca e recebido. A interrupção usa o Tomcat incluído no Spring Boot, apenas no
adaptador/composição; clientes com retry automático devem desativá-lo ao medir
a tentativa de falha. Nenhuma instrução de teste entra no JSON do integrador.

O [cliente de smoke](.scratch/issue-14-smoke.py) usa somente dados fictícios,
socket sem retry e biblioteca padrão Python. Em instância/base **vazia e exclusiva**:

```sh
python3 .scratch/issue-14-smoke.py prepare --port 8080 --state /tmp/issue-14-ficticio.json
# Reinicie a mesma instância conservando a base; depois:
python3 .scratch/issue-14-smoke.py resume --port 8080 --state /tmp/issue-14-ficticio.json
```

Veja [evidência da #14](docs/evidencias/issue-14.md). Limpar RECEBIDO por descarte
é aproximação expressa divergente do manual. T12 cobre estes dois limites;
demora/500 adicionais, reset, expiração, regras fiscais, #8/E1 e T01/E3 continuam
pendentes. Código aprovado por revisão independente;263 testes e51 verificações
Docker Linux amd64 confirmados pela supervisão, com limites na evidência.
Publicação e merge concluídos em27/10/2026 no
[PR #41](https://github.com/lukadev08/fake-esfinge/pull/41); #14 encerrada.

## Bloqueio administrativo de UG/competência — #15

GET/PUT `/__simulator/competencias/{ug}/{anoMes}` exigem `X-Simulator-Key` e
nenhuma query. A UG mantém seus zeros e deve estar configurada no cenário atual;
`anoMes` tem exatamente seis dígitos ASCII de 202601 a 202612. GET não tem corpo.
PUT exige JSON UTF-8 estrito com apenas um booleano:

```json
{"bloqueada":true}
```

GET/PUT retornam `{"ug":"0001","anoMes":202601,"bloqueada":true}`. Uma combinação
válida ainda não bloqueada retorna `false`; PUT com `false` desbloqueia. Chave
administrativa ausente/inválida, inclusive token compatível em seu lugar, recebe
401; UG não configurada, 404; competência, query, corpo ou mídia inválidos, 400.
GET não escreve e PUT altera somente o bloqueio, sem renovar atividade compatível.

As seis mutações compatíveis — abrir, receber, disparar, finalizar, descartar
assunto e cancelar — consultam o bloqueio na mesma transação de seus efeitos.
Após autenticar e resolver a chave isolada, o alvo bloqueado recebe 403
`SIM_ACESSO_NEGADO`, antes das demais guardas de estado, inclusive para uma chave
encerrada. Sem bloqueio, as guardas anteriores continuam válidas. Rejeição não
altera lote, trabalho, atividade, eventos ou tentativas de falha da #14.
UGs, competências e cenários não selecionados permanecem independentes.

Consulta compatível autorizada continua disponível e renova atividade conforme
o contrato existente, sem executar tarefas. Administração mantém suas próprias
precondições e não renova atividade. **O bloqueio é prospectivo:** trabalhos
aceitos antes dele continuam no executor existente; não são suspensos nem
cancelados. Desbloquear libera novas mutações, sem reabrir uma chave encerrada.
Essa é uma política derivada do simulador, sem ratificação fictícia na API do TCE.

V13 acrescenta somente a tabela de bloqueios por cenário/UG/competência;
V1–V12 e seus checksums permanecem intactos. Reinício conserva bloqueios e dados.
Reset e repetição com estado inicial pertencem à #17, ainda pendente.

O [smoke fictício](.scratch/issue-15-smoke.py) exige instância/base vazia e exclusiva,
relógio fixo e duas UGs: `--simulator.clock-mode=fixed` e
`--simulator.users[0].ugs=0001,0002`. Depois da prontidão:

```sh
python3 .scratch/issue-15-smoke.py prepare --port 8080 --state /tmp/issue-15-ficticio.json
# Reinicie a mesma instância conservando a base; depois:
python3 .scratch/issue-15-smoke.py resume --port 8080 --state /tmp/issue-15-ficticio.json
```

[Evidência da #15](docs/evidencias/issue-15.md): executor e supervisão confirmaram
275 testes verdes, incluindo 12 novos HTTP/SQLite; smoke do jar e da imagem final
Linux amd64 passaram 51 verificações cada, com reinício. Código aprovado por
revisão independente, sem achados; conferência documental final também aprovada.
Ensaios fictícios
não encerram #8/E1, T01/E3 ou homologação oficial.
Publicação e merge concluídos em 27/10/2026 no
[PR #42](https://github.com/lukadev08/fake-esfinge/pull/42); #15 encerrada.

## Expiração pelo relógio de negócio — #16

O token continua válido por `simulator.token-seconds` (padrão 3600 segundos):
no instante exato do prazo a chamada retorna 401 antes de efeitos. Novo login
permite repetir a operação sem perder lote/envio. Login não renova a remessa.

Envio aberto expira em `atividade + 72h`, inclusive no limite. Recepção, disparo,
consulta compatível, descarte, finalização e cancelamento bem-sucedidos atualizam
atividade e prazo na mesma transação. Rejeições e administração não renovam.
A duração e a lista renovadora são políticas derivadas, sem equivalência oficial.

Reutilize POST `/__simulator/processamento/avancar` com chave administrativa e
JSON estrito já documentados. O avanço confirma o relógio primeiro e verifica a
agenda antes dos passos; uma falha posterior de expiração não desfaz o tempo
já avançado. `tick` verifica vencimentos nos dois modos, sem avançar tempo;
somente AUTOMATICO executa passos de lote pelo tick. Em fixed, nada vence sem
avanço explícito do relógio; em system, o tempo real também pode vencer a agenda,
inclusive com processamento MANUAL. Isso não inicia processamento de lotes.

A expiração encerra a chave com `expirada_em` distinto de cancelamento/finalização,
remove os dados ativos e inativa os lotes. Bytes/hash/quantidade original,
controles e eventos permanecem históricos. Pendentes sem trabalho, agendados e
EM tornam-se ABORTADO com `SIM_ENVIO_EXPIRADO`, mensagem própria
`Envio expirado no simulador` e registro null. Sucessos anteriores conservam a
situação histórica, com processados/efetivos ativos removidos; isso não finaliza
o envio nem aprova dados da execução expirada.

Mutações da chave expirada retornam 409; nova abertura exige outra chave. Consulta
compatível autorizada pode materializar a expiração já vencida e retorna200 com
histórico, sem renovar atividade/eventos de consulta nem conduzir processamento.
Autenticação e isolamento401/404 precedem a expiração. Em operações com chave,
expiração precede bloqueio403; na nova abertura, bloqueio403 é verificado antes
de expirar o envio anterior, sem efeitos de uma abertura rejeitada.

GET administrativo continua somente leitura. Controles administrativos mutáveis
que exigem chave aberta retornam 409 quando o prazo venceu, mesmo antes de existir
o marcador; não materializam expiração nem renovam atividade. Trabalhos capturados
antes do prazo revalidam deadline/marcador/geração na transação final e não podem
reaplicar efeitos após o vencimento.

V14 acrescenta deadline em segundos epoch+nanos e marcador separado; o backfill
Java calcula `Instant.parse(atividade) + 72h` na transação da migração, preservando
nanos, dados, checksums anteriores, FKs e sequências. Reinício conserva agenda,
offset e atividade. Não se usa ponto flutuante ou comparação de strings de tempo.
O prazo interno pode ultrapassar o ano 9999 sem restringir operações antes aceitas;
o horizonte técnico do relógio/timestamps compatíveis permanece o mesmo.

Smoke reproduzível em instância/base fictícias exclusivas, relógio fixed e
`--simulator.users[0].ugs=0001,0002` (use aspas no shell):

```sh
python3 .scratch/issue-16-smoke.py prepare --port 8080 --state /tmp/issue-16-ficticio.json
# Reinicie a mesma instância conservando a base; depois:
python3 .scratch/issue-16-smoke.py resume --port 8080 --state /tmp/issue-16-ficticio.json
```

[Evidência da #16](docs/evidencias/issue-16.md): executor e supervisão confirmaram
304 testes verdes; smoke no jar copiado e na imagem final Linux amd64 passaram 56
verificações cada, com avanço virtual, reinício, expiração e reenvio. Revisão
independente do código e conferência documental final aprovadas sem achados.
Reset #17, #8/E1, integrador/T01/E3, regras fiscais, CI remoto e homologação permanecem
pendentes. Publicação é a próxima etapa, a cargo da supervisão.
