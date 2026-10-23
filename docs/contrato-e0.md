# Contrato E0 — recorte inicial consolidado

Atualizado em 23/10/2026. **E0 concluído documentalmente para o recorte inicial; aceite do piloto pendente.**
Não há aplicação, teste de aplicação executado, OpenAPI exportado ou compatibilidade
comprovada. A definição documental não autoriza antecipar E1–E4.

O [planejamento](planejamento-simulador-tributario.md) define escopo e aceite;
a [arquitetura](../ARCHITECTURE.md) define a implementação futura. O
[pacote 2026/V6/6.06](contratos/2026-v6-6.06/README.md) contém fontes preservadas,
hashes, schemas e exemplos fictícios. O [mapa e oito tickets locais](../.scratch/wayfinder-e0/map.md)
preservam o histórico de 21–23/10, sem publicação no GitHub.

## Perfil e evidências

| Item | Definição e situação | Origem |
|---|---|---|
| Exercício / API / leiaute | 2026 / V6 / 6.06, sem execução independente | Relato do usuário em 22/10, ticket 04 |
| CONs | 6.9 continua referência proposta; catálogo e regras fora da validação inicial | Planejamento e ticket 08 |
| Precedência | Manual do perfil; diferenças do cliente ou escolhas locais são aproximações explícitas | Ticket 04 e respostas de 23/10 |
| Fontes reproduzíveis | Quatro manuais e extração de DTOs incorporados, bytes/hashes iguais aos registrados em 21/10 | [Manifesto das fontes](contratos/2026-v6-6.06/fontes.json), ticket 01 |
| Datas e autenticidade | Data original de obtenção e versão da extração desconhecidas; fornecimento em 21/10, incorporação/verificação em 23/10. Hash não autentica origem oficial | Fontes fornecidas pelo usuário |
| Inventário e consumo do cliente | Métodos, parâmetros, envelopes e critérios de avanço confirmados por pergunta/resposta em 23/10; sem captura HTTP | Tickets 02, 05, 06 e plano aprovado nesta conversa |
| Contrato derivado | Schemas, HTTP sem exemplo completo, erros, repetições e recuperação aprovados como definições locais | Respostas e aprovação do plano em 23/10 |
| Plataforma | Integrador Java 21 / Spring Boot 3.5.10, por relato; Docker Linux amd64, uma instância e volume local por execução | Respostas de 23/10 |
| Código bruto | Indisponível por restrição informada; não é insumo pendente | Ticket 01 |

Este fechamento substitui as pendências HTTP documentadas anteriormente. A
comprovação independente do cliente/TCE permanece trabalho de validação futura,
sem ser confundida com fechamento documental de E0. A revisão de critério
autorizada em 23/10 está registrada na condição de saída ao final deste documento.
Mudanças de contrato posteriores exigem revisão explícita do pacote.

## Inventário de operações

Rotas relativas à URL base configurada do simulador, nunca à página Swagger.
JSON UTF-8, `Content-Type: application/json` nas requisições com corpo; respostas
com corpo usam JSON. GETs e POSTs declarados sem corpo não carregam payload.
A configuração de URL do destino não pressupõe prefixo de gateway do TCE.

Todas as operações, exceto login, exigem header `auth_token`. Login não usa token.
Headers são nomes HTTP; schemas de headers/query representam apenas sua projeção
contratual decodificada, sem rejeitar headers de transporte comuns. Parâmetros
obrigatórios ausentes/malformados produzem 400, exceto a política de cancelamento:
valor fora de `NAO`, inclusive omissão, usa 422 de cobertura, assim como assunto
sem suporte. Query é texto no transporte; o adaptador valida e decodifica `anoMes` como
inteiro, mantendo chaves, códigos de UG e tokens opacos.

| Operação | Método e caminho | Entrada | HTTP 200 / schema em `$defs` |
|---|---|---|---|
| Autenticar | `POST /autenticacao/login` | Headers `codigoAcesso`, `senha`; query `codigoUg`, `descricaoEmpresaTI`, `descritivoSoftware`; corpo vazio | `loginResposta`: `{"chave":"<token>"}` |
| Abrir envio | `GET /v6/tributario/iniciarEnvio` | Query `anoMes` inteiro de 2026, `cancelamentoAutomatico=NAO`; UG vem do token | `abrirResposta`: `{"chavePacote":"<chave>"}` |
| Enviar lote | `POST /v6/tributario/enviarParcialLote` | Query `chavePacote`; corpo `lote`, uma lista não vazia | `recepcaoResposta`: `situacao=RECEBIDO`, `numeroLote`, `dataEnvio`, `identificadorRemessaOnline` |
| Disparar | `POST /v6/tributario/iniciar-processamento` | Query `chavePacote`; corpo vazio | `processamentoResposta`: `situacao=RECEBIDO`, `dataEnvio`, `identificadorRemessaOnline` |
| Consultar lote | `GET /v6/tributario/consultarPorNumeroLote/{numeroLote}` | Número no caminho; header identifica UG/cenário autorizado | `consultaResposta`: `situacao`, `identificadorRemessa`, `numeroLote`, `dataEnvio`; diagnóstico nos terminais sem sucesso |
| Finalizar | `POST /v6/tributario/finalizarEnvio` | Corpo `finalizar` com `chavePacote` e 17 totalizadores string; chave não vai na query | 200 suficiente ao cliente; corpo vazio como decisão derivada |
| Descartar assunto | `POST /v6/tributario/descarteParcialPorAssunto` | Query `chavePacote`, `assunto`; corpo vazio | 200 e corpo vazio, decisão derivada |
| Cancelar envio aberto | `POST /servicosGerais/cancelarEnvioParcial` | Query `chavePacote`; corpo vazio | 200 e corpo vazio, decisão derivada |

Schemas completos: [schemas.json](contratos/2026-v6-6.06/schemas.json).
Exemplos: [exemplos.json](contratos/2026-v6-6.06/exemplos.json).
Os corpos vazios de sucesso não alegam equivalência com resposta oficial não capturada.

Fora da superfície inicial: operação explícita `/v6/tributario/descartar-lote`,
consulta agregada por chave, ratificação via API, cancelamento de envio concluído,
webhooks e relatórios oficiais. Descarte automático em erro continua incluído.
Uma chamada a capacidade conhecida fora do recorte recebe 422 de cobertura,
sem aprovação silenciosa; recurso inexistente usa 404.

### Autenticação, identidade e relógio

- `codigoAcesso` e `senha` são credenciais fictícias; `codigoUg` identifica a UG
  autorizada. Resposta usa **`chave`**, não `auth_token`; o cliente envia seu valor
  no header **`auth_token`** das demais chamadas.
- Token tem duração padrão de uma hora configurável por cenário, controlada por
  relógio virtual. Token inválido/expirado retorna 401 antes de efeitos; cliente
  autentica novamente e repete a chamada. Duração é escolha local, não regra oficial.
- Cliente selecionado corresponde a uma UG por envio e a uma competência. A chave
  vem da abertura, é reutilizada sem transformação e não é gerada por `anoMes + cliente`.
  O algoritmo interno da chave não pertence ao contrato externo.
- Envio aberto expira após 72 horas de inatividade. Operações compatíveis
  bem-sucedidas sobre a chave do envio aberto, inclusive consulta de lote, renovam atividade;
  login e chamadas rejeitadas não renovam. Consulta não avança processamento.
  Expiração encerra a chave, invalida trabalhos e preserva histórico; exige nova
  chave; consulta histórica não reabre nem renova envio encerrado. Não atribuir
  conclusão aos dados de execução expirada. A regra de
  operações renovadoras é aproximação aprovada, sem validação oficial.

## Ciclo relatado

`abrir → enviar todos os lotes de um assunto → disparar → consultar todos até sucesso → repetir por assunto com a mesma chave → finalizar`

Ordem relatada: saldo inicial em janeiro; contribuintes; crédito; prazo
prescricional quando houver dados; arrecadação; baixa. As referências entre
assuntos são conservadas, sem validação tributária na etapa inicial.

O cliente consulta `situacao` no JSON; `situacao_lote` é propriedade interna,
não nome de campo externo. `RECEBIDO` e `EM_PROCESSAMENTO` mantêm a consulta;
`PROCESSADO_SUCESSO` permite avançar. Qualquer terminal de erro/descarte/aborto
interrompe a consulta com diagnóstico. Todos os lotes do assunto devem concluir
com sucesso antes do seguinte. HTTP 200 isolado não encerra processamento;
somente na finalização o 200 basta para conclusão do cliente.

Retornos preservam os nomes distintos do manual: `identificadorRemessaOnline`
em recepção/disparo e `identificadorRemessa` em consulta. Identificadores numéricos
são números JSON; chave e token são strings opacas; `dataEnvio` é timestamp
ISO-8601 com offset. Datas dos registros são `YYYY-MM-DD`.

## Assuntos e schemas estruturais

| Lista JSON | Identificador de `assunto` no descarte | Schema do registro | Totalizador |
|---|---|---|---|
| `cadastrosContribuintes` | `CADASTRO_CONTRIBUINTE` | `contribuinte` | `quantidadeCadastroContribuinte` |
| `lancamentosCreditosTributarios` | `LANCAMENTO_CREDITO_TRIBUTARIO` | `credito` | `quantidadeLancamentoCreditosTributarios` |
| `diarioGeralArrecadacao` | `DIARIO_ARRECADACAO` | `arrecadacao` | `quantidadeDiarioGeralArrecadacao` |
| `baixasCreditosTributarios` | `BAIXA_CREDITO_TRIBUTARIO` | `baixa` | `quantidadeBaixaCreditosTributarios` |
| `saldosIniciaisCreditoTributario` | `SALDO_INICIAL_CREDITO_TRIBUTARIO` | `saldo` | `quantidadeSaldoInicialCreditosTributarios` |
| `inicioPrazoPrescricional` | `INICIO_PRAZO_PRESCRICIONAL` | `prazo` | `quantidadeInicioPrazoPrescricional` |

Obrigatórios incondicionais do leiaute estão em `required`; ausência e `null`
são rejeitados. Opcionais admitem ambos. Campos condicionais são opcionais na
etapa inicial; suas condições e defaults tributários ficam não implementados.
Rejeitar campos extras, listas nulas/vazias, nenhuma lista ou mais de uma lista.
Não converter tipos ou normalizar identificadores; bytes originais são preservados.

Tipos vêm da extração de DTOs com ajustes relatados: **`numeroParcelas`** substitui
`numeroParcela`; prazo prescricional tem quatro campos aprovados a partir do
exemplo, sem DTO extraído; propriedades de inscrição imobiliária/transferência
não enviadas pelo cliente ficam fora do schema inicial. A lista de divergências,
limites estruturais e rastreabilidade está no [pacote](contratos/2026-v6-6.06/README.md#decisões-de-schema).
Catálogos externos, validade fiscal de documentos, cronologia, relações e cálculos
continuam não implementados, mesmo quando o schema aceita o registro.

Totalizadores da finalização são strings de dígitos, sem sinal ou fração.
Todas as propriedades do exemplo aprovado são obrigatórias; as fora dos seis
assuntos devem ser `"0"`. Valor positivo para cobertura ausente é 422.
Comparar os seis totais numericamente com os registros efetivos do envio após
recuperação, preservando multiplicidade e excluindo histórico, descartados e
abortados. Divergência é 409; não arredondar ou converter dados recebidos para
forçar coincidência. Os lotes de até 10.000 registros são comportamento do
integrador, não limite obrigatório do simulador ou do TCE.

## Comportamentos selecionados

As regras abaixo são decisões do simulador, sem comprovação de equivalência
oficial. Operações rejeitadas não alteram dados nem renovam atividade.

| Condição/ação | Efeito e resultado observável | Histórico / testes previstos |
|---|---|---|
| Receber lote | Persistir bytes, vínculos e metadados antes de confirmar; não processar | T01, T02; contagens derivadas dos registros |
| Consultar | Não avançar processamento; 200 com situação atual, inclusive erro assíncrono; renovar atividade se autorizado e bem-sucedido | T02, T19 |
| Erro assíncrono intermediário | Descartar lote com erro (`LOTE_DESCARTADO` com diagnóstico); abortar seguintes ainda não processados do mesmo assunto (`ABORTADO_ERRO_LOTE_ANTERIOR`) | Preservar anteriores válidos até descarte explícito e controles históricos; T04, T06 |
| Descartar assunto | Remover dados recebidos/processados/efetivos daquele assunto no envio aberto, inclusive pendentes; preservar outros assuntos e chave | Controles históricos não contam como duplicatas; T07, T08 |
| Descarte durante processamento | 409, sem remoção; aguardar término | T08, T15 |
| Finalizar após recuperação | Todos os lotes ativos em sucesso, ao menos um lote ativo, totais corretos; históricos descartados/abortados não bloqueiam | Finalização prematura ou divergência: 409; T03, T06–T08 |
| Cancelar envio aberto | Remover seus dados recebidos/processados/efetivos, invalidar trabalhos e encerrar chave; nova chave e reenvio integral | Preservar controles/evidências e envios anteriores; dados do integrador permanecem; T08, T13 |
| Falha antes da aceitação | Não confirmar nem persistir a operação | T12; distinta da resposta perdida |
| Resposta perdida após aceitação | Persistir em `RECEBIDO`, perder somente resposta, sem iniciar processamento; marcar recuperação daquele assunto no cenário | Nova tentativa: 409 `SIM_ENVIO_ABERTO_SEM_PROCESSAMENTO`; descartar assunto e reenviar na mesma chave; T12 |
| Abertura simultânea ou repetida com envio aberto na UG/competência | Uma abertura vence, demais 409 sem efeitos | Não confundir com reabrir após encerramento com nova chave; T14 |
| Disparo sem elegíveis, repetido/ativo; finalização/cancelamento repetidos ou operação mutável em chave encerrada | 409 sem efeitos, sem promessa de idempotência | T03, T15; consulta histórica autorizada pode continuar disponível, sem reabrir chave |
| Envio expirado | Encerrar chave e invalidar trabalhos sem concluir processamento; preservar evidências | 72 horas virtuais; T19 |
| Competência bloqueada | 403 nas operações mutáveis de envio; consulta autorizada continua permitida | Cenário administrativo, sem rota fictícia de ratificação; T09 |
| Assunto/campo/operação sem suporte | 422 e registro de cobertura ausente | Modo estrito, T18 |

A marca de recuperação por resposta perdida é efeito do plano de falhas do
cenário; não usar a mera existência de um lote `RECEBIDO` para bloquear os demais
lotes normais do assunto, nem hash para reconhecer repetição. Essa distinção
preserva o envio de múltiplos lotes antes do disparo.

**Aproximação expressa:** a cópia V6 impede descarte por assunto com lotes
`RECEBIDO`. A recuperação escolhida permite limpá-los após perda de resposta,
sem processamento ativo. Não declarar essa exceção como comportamento oficial.

`PROCESSADO_ERRO_NEGOCIO`, `PROCESSADO_ERRO_INTERNO` e `ABORTADO` permanecem no
vocabulário de consulta para diagnóstico dos cenários; no erro intermediário
selecionado, o resultado terminal do lote descartado é `LOTE_DESCARTADO`.
O estado de espera de descarte de lote misto não é gerado pelo recorte inicial.

### Códigos HTTP e erros derivados

| HTTP | Condição | Código próprio representativo |
|---|---|---|
| 200 | Operação bem-sucedida ou consulta válida, mesmo com erro assíncrono no lote | Sucesso transporta o schema da operação |
| 400 | JSON, parâmetro ou caminho numérico malformado/obrigatório ausente | `SIM_REQUISICAO_INVALIDA` |
| 401 | Credenciais/token inválidos ou token expirado | `SIM_AUTENTICACAO_INVALIDA` |
| 403 | UG de login não autorizada ou competência bloqueada para mutação | `SIM_ACESSO_NEGADO` |
| 404 | Chave/lote inexistente ou pertencente a outra UG/cenário | `SIM_RECURSO_NAO_ENCONTRADO` |
| 409 | Estado, sequência, disparo, repetição ou totalizadores incompatíveis | `SIM_ESTADO_INVALIDO`, `SIM_TOTALIZADORES_DIVERGENTES`, `SIM_ENVIO_ABERTO_SEM_PROCESSAMENTO` |
| 422 | Schema do corpo inválido ou capacidade conhecida sem cobertura | `SIM_ESTRUTURA_INVALIDA`, `SIM_COBERTURA_AUSENTE` |
| 500 | Falha interna, sem expor SQL, stack trace ou segredos | `SIM_ERRO_INTERNO` |

Erro HTTP: `{"codigo":"SIM_ESTADO_INVALIDO","mensagem":"Operação incompatível com o estado atual","correlacao":"sim-evento-1"}`.
Código identifica a condição; texto não é mensagem oficial nem critério de
comparação literal do cliente. Consulta com erro mantém 200 e usa `situacao`
e `retornoErros.erros[]`, com `codigo`, `mensagem` sanitizada e `registro`
(índice zero-based ou `null` quando não localizado). Nenhum número de CON é
inventado. Estes corpos mínimos são decisões derivadas aceitas pelo cliente.

### Exclusões explícitas do aceite inicial

- `cancelamentoAutomatico=SIM`, omitido ou outro valor: 422 de cobertura; não
  implementar cancelamento automático por suposição. T05 fica para evolução.
- Envio sem movimento: não aceitar finalização sem lotes ativos; parcela de T16
  adiada. Janeiro continua cenário complementar, com regras fiscais adiadas.
- Contagem mensal 64/65: T17 adiado; não impor cota mensal. Mantém-se uma abertura
  por UG/competência. Compatibilidade com limites mensais não é alegada.
- Descarte explícito de lote, consulta agregada, lotes mistos, outros assuntos,
  tabelas externas e regras tributárias: fora do contrato inicial. Demora/500
  permanecem na matriz geral sem prioridade inicial.

## Piloto e esperado independente

Cliente/alias **14**, competência **202609**, uma UG, base local de testes com
valores originais de produção. O alias não é credencial nem comprova que o código
real de UG seja 14. O solicitante prepara a referência independente, executa
e confere o piloto localmente quando o simulador estiver testável (E3).
Usará cópia local sem novas cargas, edições ou processamento concorrente entre
extração do esperado e envio. SQL, estrutura interna do banco, identificação
da cópia e resultados detalhados permanecem na empresa, conforme suas regras
de compliance; seu fornecimento ao agente ou repositório não é requisito de E0.
O roteiro abaixo define a conferência; sua execução e o preparo concreto das
consultas são responsabilidade do solicitante no piloto.

1. No ambiente interno de E3, identificar a cópia e registrar o procedimento reproduzível de seleção de
   cliente 14 / 202609, filtros e joins do esperado. O simulador não acessa a origem.
2. Quando o simulador estiver testável, antes do envio do piloto, executar SQL
   independente da montagem de lotes e obter todos
   os campos efetivamente enviados em cada DTO, chaves, contagens e multiplicidades
   por assunto. Preservar strings/zeros e decimal exato. Hashes e identificação da
   versão da entrada podem ser registrados localmente; não transferir resultados.
3. Volume confirmado pelo solicitante em 23/10/2026: **21.770 lançamentos de
   crédito tributário** para cliente 14 / 202609. Com partições de até 10.000,
   prever **3 lotes: dois de 10.000 e um de 1.770**. O tamanho foi corrigido pelo
   solicitante de 1.000 para 10.000, por lembrança de outro cliente com cerca
   de 240 mil registros em mais de 20 lotes; não houve verificação da configuração. A pendência documental de volume
   está resolvida por relato, sem consulta independente ao banco. Na execução,
   conferir esse fracionamento e a união dos 21.770 registros, sem perdas ou
   duplicações. A contagem deverá corresponder à cópia estável identificada no
   esperado SQL; eventual diferença exige reconciliação, não ajuste silencioso.
4. Após E0 e implementação, configurar cenário/credenciais fictícias, executar o
   integrador real, transmitir todos os lotes de cada assunto, disparar, acompanhar
   todos e finalizar com os totais efetivos.
5. Comparar esperado e recebidos por todos os campos, chaves e multiplicidades;
   comparar efetivos conforme os efeitos do cenário, explicando descartes e
   abortos. Conferir conclusão no cliente e falhas prioritárias exercitadas.
6. Repetir após reset quando implementado e guardar evidências detalhadas
   internamente. Compartilhar somente conclusão e divergências sanitizadas,
   quando permitido pela empresa, identificando contrato, cenário e limitações.
   Dados originais, estrutura do banco, SQL e resultados não vão para Git,
   serviços externos ou evidências compartilhadas.

Saldo inicial e prazo prescricional podem faltar no primeiro ensaio, exigindo
cenários adicionais. Piloto parcial não encerra T01 integral, validação de negócio,
MVP completo ou homologação oficial. Nenhuma etapa de execução foi realizada aqui.

## Manifesto inicial de cobertura

**Tudo não implementado.** Schemas e exemplos são documentos, não testes da
aplicação. Para cada linha: perfil 2026/V6/6.06; fontes/relatos de 23/10 no pacote
e tickets. “Aproximação” qualifica a definição planejada, não software entregue.

| Operação/assunto | Regra e evidência | Teste previsto | Situação e definição |
|---|---|---|---|
| Autenticação | Headers/query, `chave`, `auth_token`; token 1h/401/renovação; tickets 02, 05 | T10, T11 | Não implementada; contrato definido por relato, duração aproximada |
| Abrir/receber/disparar/consultar/finalizar | Schemas e ciclo desta revisão; tickets 02, 06 | T01–T03, T14, T15 | Não implementada; consumo relatado, erros/respostas sem captura são derivados |
| Cadastro de contribuinte | Schema `contribuinte`, tipos DTO e obrigatórios do leiaute; 01, 08 | Parcela de T01; estrutura | Não implementada; condições fiscais adiadas |
| Lançamento de crédito tributário | Schema `credito`; campos imobiliários/transferência fora do recorte; 01, 08 | Parcela de T01; estrutura | Não implementada; referências/cálculos adiados |
| Diário de arrecadação | Schema `arrecadacao`, override `numeroParcelas`; 01, 02, 08 | Parcela de T01; estrutura | Não implementada; aproximação de campo e condições adiadas |
| Baixa de crédito tributário | Schema `baixa`, DTO e leiaute; 01, 08 | Parcela de T01; estrutura | Não implementada; cronologia e vínculos adiados |
| Saldo inicial | Schema `saldo`, DTO e leiaute; 01, 07, 08 | T01/T16, cenário adicional | Não implementada; condições/defaults tributários adiados |
| Início do prazo prescricional | Schema `prazo`, exemplo e aprovação de 23/10; 07, 08 | T01, cenário adicional | Não implementada; schema derivado sem DTO extraído |
| Descarte/cancelamento/recuperação | Tabela de efeitos, 05–06 | T04, T06–T08, T12 | Não implementada; aproximação de descarte de `RECEBIDO` explícita |
| Persistência/reset/isolamento | Invariantes e arquitetura | T09, T10, T13, T14 | Não implementada; nenhum ensaio real |
| Expiração de envio | 72h, renovação inclusive consulta, relógio virtual | T19 | Não implementada; política aproximada |
| Campo/operação/assunto sem suporte | Rejeição estrita 422 | T18 | Não implementada; sem aprovação silenciosa |
| Regras fiscais e relações entre assuntos | Validação adiada, CONs sem versão adotada | Evolução; injeção em T04 não prova regra | Não implementada; fora da validação inicial |
| SIM/omitido, sem movimento, mensal e descarte explícito de lote | Exclusões aprovadas nesta revisão | T05, parcela de T08/T16, T17 | Não implementada; fora do aceite inicial |

Expandir resultados reais por regra/teste quando existir software. Todas as
propriedades dos seis registros estão enumeradas nos schemas; exigências
condicionais/enums e demais regras não cobertas estão identificadas no pacote.

## Pendências e condição de saída de E0

### Revisão autorizada em 23/10/2026 — referência interna por compliance

O solicitante autorizou revisar o critério anterior, que exigia preparar SQL
antes de fechar E0. **E0 exige o roteiro, responsáveis, recorte e critérios da
conferência independente; a preparação concreta da referência e sua comparação
ocorrem localmente em E3, antes do aceite do piloto.** Não é necessário fornecer
SQL, estrutura interna do banco ou resultados sensíveis ao agente/repositório.
As decisões anteriores permanecem no histórico dos tickets e em PROGRESS.

| Elemento exigido em E0 | Definição registrada |
|---|---|
| Responsável | Solicitante prepara a referência, executa o integrador e confere o resultado no ambiente interno |
| Recorte | Cliente/alias 14, UG correspondente, 202609, cópia local sem alterações entre referência e envio |
| Referência independente | SQL interno, preparado no piloto antes do envio, sem reutilizar a montagem de lotes do integrador; comparação não pode depender apenas do payload enviado |
| Conteúdo comparado | Todos os campos enviados por assunto, chaves, contagens e multiplicidades, preservando strings/zeros, nulos e decimal exato |
| Conferência do fluxo | Inspecionar recebidos/processados/efetivos; explicar efeitos de recuperação; confirmar conclusão no integrador e fracionamento observado |
| Volume de referência | 21.770 lançamentos por relato; previsão de 3 lotes (10.000 + 10.000 + 1.770), sujeita à cópia estável e tamanho efetivo configurado |
| Aprovação | Nenhuma perda, duplicação indevida ou divergência de conteúdo sem explicação; totais efetivos coerentes com o cenário; falhas exercitadas e cobertura ausente identificadas |
| Registro | Detalhes e SQL ficam internos; compartilhar apenas conclusão e divergências sanitizadas quando permitido pela empresa; reportar limites de acesso sem afirmar revisão independente pelo agente |

O roteiro está na seção de piloto e os elementos acima estão definidos. **E0
está concluído documentalmente no recorte aprovado.** A execução de E3 e o
aceite T01 continuam pendentes; saldo/prazo ausentes e regras adiadas não recebem
aprovação automática. O fechamento não comprova compatibilidade, implementa E1
ou autoriza publicação externa.

| Pendência transferida explicitamente para o piloto E3 | Responsável | Condição de aceite |
|---|---|---|
| Preparar referência e realizar conferência independente local | Solicitante | Antes do envio, preparar referência sobre a cópia estável; comparar campos/chaves/contagens/multiplicidades com recebidos e efetivos; registrar conclusão, divergências e limitações internamente |

A execução pode revelar diferenças de seleção ou transformação e exigir
retrabalho no contrato ou simulador. Somente contar registros ou comparar o
simulador com o próprio payload de envio oferece cobertura insuficiente para
o aceite ponta a ponta. Se a conferência não ocorrer, o aceite permanece pendente;
o agente não revisará nem certificará o conteúdo inacessível por compliance.
