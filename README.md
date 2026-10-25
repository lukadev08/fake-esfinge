# fake-esfinge

Simulador HTTP local do e-Sfinge Tributário do TCE/SC,
independente da origem dos dados e sem encaminhar chamadas ao TCE.

E0 está concluído documentalmente no recorte inicial. As fatias E1
[#1](https://github.com/lukadev08/fake-esfinge/issues/1) e
[#2](https://github.com/lukadev08/fake-esfinge/issues/2) e
[#3](https://github.com/lukadev08/fake-esfinge/issues/3) implementam autenticação
fictícia, abertura de envio, recepção de contribuintes e créditos, consulta `RECEBIDO` e
inspeção administrativa com persistência SQLite. Os outros quatro assuntos,
processamento, finalização, reset e painel ainda não estão implementados.
A saída E1 depende do aceite humano da #8; testes locais não comprovam T01.

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
anterior e não foram regeneradas nesta revisão de 23/10/2026. Consulte o Markdown
para as decisões atuais. Os comandos abaixo validam somente a fatia implementada.

Dados originais do piloto permanecem no ambiente local controlado, fora do Git
e de serviços externos. Exemplos e evidências compartilhadas exigem sanitização;
credenciais e tokens do simulador serão fictícios.

## Executar e validar as fatias #1–#3

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
permite crédito e acrescenta os valores monetários em TEXT. Schema, migração ou configuração persistida incompatíveis
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
422 `SIM_COBERTURA_AUSENTE`; não processam ou finalizam remessas nem aprovam dados.
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
nem altera parâmetros de execução ativa. O avanço administrativo de tempo fica
na #16; testes usam relógio substituível sem espera real.

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
[evidência da #3](docs/evidencias/issue-3.md) para resultados e limites.

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
