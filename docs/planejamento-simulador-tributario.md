---
title: "Simulador e-Sfinge Tributário"
subtitle: "Planejamento do escopo de desenvolvimento"
date: "23 de outubro de 2026 · versão 1.6"
lang: pt-BR
---

# 1. Recomendação e resultado esperado

Desenvolver um simulador HTTP com estado, portável e reiniciável, que ocupe o lugar da API do TCE/SC no ambiente de testes do integrador. O simulador deverá receber os lotes efetivamente produzidos pelo integrador, conservar seus conteúdos, controlar remessas, executar processamento assíncrono controlado e devolver respostas compatíveis com o contrato selecionado.

O resultado esperado é poder repetir o fluxo de integração quantas vezes forem necessárias, inclusive quando os períodos das cidades clientes estiverem ratificados no TCE. O ensaio deverá demonstrar que os registros esperados chegaram ao destino, que não foram perdidos na divisão em lotes e que o integrador reagiu corretamente a sucesso, rejeição, demora e interrupção.

Este documento entrega o planejamento de desenvolvimento. Não contém uma implementação nem uma declaração de conformidade integral com o TCE. A arquitetura adotada está em [ARCHITECTURE.md](../ARCHITECTURE.md); comportamentos oficiais, escolhas derivadas e limites de evidência estão identificados separadamente.

**Revisão documental de 23/10/2026, versão 1.5:** incorpora a rodada de pergunta/resposta e o plano de consolidação aprovado pelo usuário. O [contrato E0](contrato-e0.md) e o [pacote contratual](contratos/2026-v6-6.06/README.md) definem o contrato derivado inicial com fontes, schemas, exemplos e aproximações. Os [tickets locais](../.scratch/wayfinder-e0/map.md) preservam as respostas anteriores e a atualização. O solicitante confirmou 21.770 lançamentos de crédito no cliente 14 / 202609, encerrando a pendência documental de volume; o critério de E0 foi revisado com autorização do solicitante: roteiro e critérios ficam definidos em E0, com preparação da referência e conferência local em E3 por compliance, sem exigir fornecimento de SQL/estrutura do banco/resultados. E0 está concluído documentalmente para o recorte; aceite do piloto permanece pendente. O tamanho de referência foi corrigido pelo solicitante de 1.000 para 10.000 por lote, por lembrança de outro cliente com cerca de 240 mil registros; a previsão de 3 lotes ainda exige execução. HTML, PDF e DOCX não foram regenerados e representam versão anterior.

**Restrições confirmadas pelo usuário:** integrador em Java 21 e **Spring Boot 3.5.10**, URL e credenciais configuráveis; operação inicial do simulador em Docker **Linux amd64**, uma instância e volume local por execução. Não há aplicação nem ensaio de compatibilidade. As confirmações são por relato, não inspeção independente do código do cliente.

**Recorte funcional confirmado:** seis assuntos, em lotes com uma única lista não vazia, omitindo as demais. Um envio pertence a uma UG/competência; a UG vem da autenticação e a `chavePacote` é retornada pela abertura, sem geração/transformação no cliente. Envia todos os lotes do assunto, dispara e consulta cada um até `PROCESSADO_SUCESSO` antes do próximo assunto; finaliza uma vez ao término. `situacao_lote` é campo interno; o JSON usa `situacao`. A premissa de um assunto por remessa foi retirada em 22/10. A equivalência operacional remessa/envio/chave está definida para este contrato derivado por relato em 23/10; comprovação independente pertence aos ensaios futuros. Cancelamento integral do envio aberto exige nova chave; descarte de assunto preserva chave e outros assuntos. Descarte explícito de lote fica fora do contrato inicial, preservado o descarte automático em erro.

**Perfil selecionado:** exercício 2026, endpoints V6 e leiaute 6.06 foram confirmados pelo usuário em 22/10, sem execução independente do integrador. CONs 6.9 continuam referência proposta, ainda sem confirmação. O manual do perfil selecionado prevalece; diferenças necessárias ao cliente serão aproximações explícitas. O contrato será derivado enquanto não houver export oficial. As publicações de 2027 não devem ser misturadas ao perfil 2026. [F06](https://jira-tcesc.atlassian.net/wiki/spaces/SD/pages/2154922045), [F07](https://jira-tcesc.atlassian.net/wiki/spaces/SD/pages/1517223937), [F16](https://jira-tcesc.atlassian.net/wiki/spaces/SD/pages/2310539061).

**Revisão de escopo de 23/10/2026, versão 1.6:** incluída a primeira fatia do
painel web local (RF13, seção 4.4 e E5), por solicitação do usuário. Sua
implementação depende da conclusão do backend no recorte contratado, com saídas
E1–E4 comprovadas. A inclusão é documental; não inicia implementação.

# 2. Problema e evidências verificadas

## 2.1 Homologação e produção

O aviso oficial de 21/08/2023 informa que os dados tributários de testes passaram a acompanhar a cópia de produção da semana anterior, com mascaramento de determinados campos. Isso caracteriza uma base copiada; a fonte não demonstra que ambos os ambientes utilizem uma única base transacional. Também não oferece, nesse aviso, um mecanismo de reset por empresa ou município. [F01](https://jira-tcesc.atlassian.net/wiki/spaces/SD/pages/132579359).

A tabela oficial de comportamento após ratificação marca o Tributário como impedido para novos envios e cancelamentos no período ratificado. A combinação entre essa regra e a cópia de produção é uma explicação consistente para os bloqueios relatados. O diagnóstico exato de cada erro continua dependendo da requisição, competência, UG e resposta concreta. Não foi verificado que todo erro de ausência de lotes decorra da ratificação. [F02](https://jira-tcesc.atlassian.net/wiki/spaces/SD/pages/13882932).

O catálogo de endpoints de outubro de 2025 aponta os seguintes endereços de documentação, diferentes do endereço antigo informado na conversa: [F03](https://jira-tcesc.atlassian.net/wiki/spaces/SD/pages/1560969264).

| Ambiente | Swagger indicado atualmente pela documentação |
|---|---|
| Homologação/Sandbox | [Swagger em api.virtual.testing.tce.sc.gov.br](https://api.virtual.testing.tce.sc.gov.br/esfingeonline/swagger-ui.html) |
| Produção | [Swagger em api.virtual.tce.sc.gov.br](https://api.virtual.tce.sc.gov.br/esfingeonline/swagger-ui.html) |

Esses endereços apontam para a interface Swagger. Não se deve usar a URL HTML como base. O contrato derivado inicial usa os caminhos relativos da seção 5 sob a URL local configurada do simulador, sem presumir prefixos de gateway do TCE.

## 2.2 Distinções que orientam o escopo

Remessa é o agrupamento lógico; lote é uma parcela transmitida assincronamente. A orientação para 2026 admite até 64 remessas por UG/mês, sem estabelecer esse mesmo limite para a quantidade de lotes. Também impede abrir duas remessas simultaneamente para a mesma UG/competência. As faixas sugeridas de 10 mil a 50 mil registros por lote e de até 500 mil por remessa são recomendações de dimensionamento, não limites obrigatórios a inventar no simulador. [F05](https://jira-tcesc.atlassian.net/wiki/spaces/SD/pages/2154921985).

A V6 introduz o início explícito do processamento, entre a recepção dos lotes e a finalização. O perfil anterior tem fluxo diferente; reproduzir apenas a descrição antiga produziria testes enganosos. [F04](https://jira-tcesc.atlassian.net/wiki/spaces/SD/pages/1582465028), [F11](https://jira-tcesc.atlassian.net/wiki/spaces/SD/pages/759267332).

A ratificação V6.06 é documentada como exclusiva do TCE Virtual/e-Sfinge Web. No MVP, o bloqueio de competência será semeado ou acionado pelo controle administrativo do simulador. Não será criada uma rota fictícia de ratificação dentro da superfície compatível com o TCE. [F08](https://jira-tcesc.atlassian.net/wiki/spaces/SD/pages/2154923870).

## 2.3 Limites da pesquisa realizada

- A conversa referenciada foi lida pela ferramenta de chats. Suas duas mensagens estão disponíveis; as duas respostas anteriores aparecem somente como marcadores internos, sem conteúdo. Não foi possível recuperar ou auditar a lista exata de fontes citadas nessas respostas. A pesquisa deste documento foi refeita em fontes oficiais.
- O Swagger antigo não pôde ser lido pela ferramenta de pesquisa. O endereço atual retornou a interface sem conteúdo textual da especificação. Uma tentativa de obter `/v2/api-docs` também não forneceu contrato legível. Isso não prova indisponibilidade do serviço para seus usuários.
- As rotas da seção 5 foram verificadas no manual oficial V6. Métodos HTTP, schemas completos, headers e respostas por operação ainda não foram confirmados por exportação OpenAPI.
- A página oficial de CONs lista a versão 6.9 de 17/09/2026. O arquivo vinculado no SharePoint não pôde ser recuperado nesta pesquisa; não se afirma ter analisado suas regras individualmente.
- O FAQ atual e o material histórico de 2020 foram localizados. Servem como contexto e esclarecimento, sem substituir o contrato anual em uso. [F15](https://jira-tcesc.atlassian.net/wiki/spaces/SD/pages/13912693), [F18](https://www.tcesc.tc.br/sites/default/files/Perguntas%20e%20Respostas_tributario_Esfinge2020_v1.06.pdf).
- Nenhuma chamada autenticada, transmissão, cancelamento ou ratificação foi realizada no TCE. Não foi solicitado acesso a contas municipais.

# 3. Fronteira do produto e independência da origem

O serviço independente continua responsável pela captura e disponibilização dos registros. O integrador da empresa continua responsável por obter esses registros, transformá-los no formato de envio, montar remessas e lotes e conduzir o diálogo com o destino. O simulador começa na fronteira HTTP que hoje aponta para o TCE.

```text
Serviço independente de dados
            |
            v
Integrador real da empresa
  obtém -> transforma -> organiza remessas/lotes -> envia -> acompanha
                                                     |
                                                     v
                                          Simulador local do TCE
                                          recebe e conserva payloads
                                          processa e valida subconjunto
                                          expõe consultas e resultados

Executor de testes ----------------------> Controle administrativo
  prepara cenário, avança relógio, consulta evidências e reinicia estado
```

**Contrato de independência:** o simulador não acessará banco, API, fila, arquivos internos ou credenciais do fornecedor de dados. Não conhecerá classes ou modelos de domínio do serviço de captura. Sua entrada serão as requisições no formato do TCE; sua configuração adicional será exclusivamente de cenários de teste.

Para comprovar também a parte anterior ao envio, o teste deverá alimentar o integrador com um conjunto conhecido no ambiente do serviço independente ou em um substituto de teste já disponível para ele. Se for necessário construir esse substituto, ele será um artefato separado, fora do MVP do simulador. Reproduzir a lógica de montagem de lotes dentro do simulador eliminaria justamente a parte que se quer verificar.

Um cliente simples e arquivos JSON poderão acompanhar o simulador para validar seu próprio funcionamento. Eles não substituem os ensaios que executam o integrador real desde a obtenção dos registros.

# 4. Escopo funcional proposto

## 4.1 Incluído no MVP

| ID | Capacidade | Entrega verificável |
|---|---|---|
| RF01 | Perfil de contrato anual | Rotas e schemas selecionados, versão registrada e manifesto de cobertura |
| RF02 | Autenticação simulada | Usuário/UG fictícios, emissão e expiração controladas, rejeição de token inválido |
| RF03 | Remessas e lotes | Abertura, recepção de payload real, identificadores, vínculo entre entidades e consultas |
| RF04 | Processamento controlado | Executor assíncrono, fila persistida e avanço manual para testes determinísticos |
| RF05 | Validação mínima | Formato do contrato, sequência de operações e regras tributárias explicitamente selecionadas |
| RF06 | Finalização | Verificação de resultados e totalizadores, resultado coerente com os dados aceitos |
| RF07 | Cancelamento e recuperação de erros | Descarte de lote/assunto, cancelamento parcial em andamento e recuperação do perfil, conforme casos confirmados |
| RF08 | Competência bloqueada | Cenário com ratificação simulada e restauração do estado inicial |
| RF09 | Falhas de transporte | Demora, erro HTTP, perda de resposta e falha interna selecionados por cenário |
| RF10 | Evidências de execução | Diário de chamadas, conteúdos sanitizados, contagens e resultados por lote/remessa |
| RF11 | Portabilidade | Imagem, configuração por arquivo/variáveis, persistência local e execução em CI |
| RF12 | Reset e isolamento | Recriação repetível, proteção contra tarefas antigas e instância independente por teste |

O MVP deve implementar o fluxo completo para os seis assuntos confirmados, antes de ampliar a profundidade de validação tributária. Receber todos os assuntos silenciosamente sem validar nada não atende ao objetivo.

**Recorte inicial aprovado em 23/10:** recepção e confirmação de lotes para os seis assuntos, validação estrutural (formato, tipos e obrigatoriedade), sequência, conteúdo e contagens. Preservam-se os comportamentos de estado, falhas e recuperação definidos nos tickets. Validação de vínculos entre assuntos, cálculos e regras tributárias fica para evolução, com cobertura não implementada. Isso não dispensa vínculos técnicos lote/remessa/UG/cenário, persistência, rastreabilidade ou reset. O recorte inicial é uma entrega parcial, não o aceite integral do MVP descrito abaixo.

## 4.2 Assuntos e níveis de cobertura

O leiaute V6.06 relaciona cadastros, créditos, saldo inicial, dívida ativa, revisões, baixas, arrecadação e estornos. O suporte inicial seguirá os seis assuntos informados pelo usuário, com profundidade explícita no manifesto. [F09](https://jira-tcesc.atlassian.net/wiki/spaces/SD/pages/2154923885).

| Grupo | Cobertura proposta para o MVP | Evolução |
|---|---|---|
| Cadastro de contribuinte | Recepção, formato e referências do cenário mínimo | Histórico cadastral e demais exceções |
| Lançamento de crédito tributário | Recepção, formato e regras selecionadas de vínculo/data | Mais combinações e consistências financeiras |
| Diário de arrecadação | Recepção, formato e vínculos com os créditos do cenário | Exceções por natureza e tipo de arrecadação |
| Baixa de crédito tributário | Recepção, formato, vínculo e cronologia selecionada | Tipos e exceções adicionais |
| Saldo inicial | Recepção, formato e cenário de abertura de exercício | Reintegrações e demais tipos conforme prioridade |
| Início do prazo prescricional | Recepção, formato, vínculo e datas do cenário | Exceções e histórico adicional |
| Imóvel, propriedade, revisões, estornos, cobrança, CDA e situação de cobrança | Fora do MVP, salvo dependência comprovada dos seis assuntos | Pacotes separados de ampliação |
| Nota Fiscal de Serviço | Fora do recorte informado para o MVP | Pacote de trabalho separado |

A coluna de cobertura do MVP conserva o objetivo completo. Na primeira etapa, os seis assuntos terão recepção e validação estrutural; referências, cronologia e regras de domínio dessa tabela permanecem como evolução. Saldo inicial e prazo prescricional podem estar ausentes no piloto inicial, sem que isso comprove sua cobertura funcional.

NFS-e é um serviço próprio dentro do módulo, documentado como dependente da conclusão do Tributário. Não está entre os seis assuntos informados. Sua inclusão futura exigirá escopo próprio, com schemas e casos; não basta aceitá-lo como mais uma lista genérica. [F10](https://jira-tcesc.atlassian.net/wiki/spaces/SD/pages/2154923972).

Um assunto sem suporte deverá provocar um erro identificável no relatório de teste. No modo estrito, não poderá finalizar com sucesso. Um modo permissivo poderá existir para investigação, mas sua execução deverá ser marcada como cobertura parcial, sem contar para o aceite.

## 4.3 Fora do MVP

- Captura de dados, migração, saneamento ou montagem de lotes do serviço independente.
- Reprodução do portal TCE Virtual, SGI, assinatura eletrônica, certificados e permissões reais.
- Implementação integral de todas as CONs, cálculos e exceções do TCE.
- Demais módulos do e-Sfinge, SOAP e compatibilidade com vários exercícios simultaneamente.
- Cancelamento de remessa já concluída, que não faz parte do uso informado nesta etapa.
- Lotes com assuntos misturados e sua recuperação. O relato de vários assuntos sob uma chave não inclui mistura dentro de um lote.
- Espelhamento de produção, importação automática de dados municipais ou encaminhamento de requisições ao TCE.
- Portal administrativo completo, infraestrutura de mensageria externa e operação de serviço público.
- Webhooks e relatórios oficiais, salvo se o inventário do integrador demonstrar dependência; nesse caso, reavaliar antes de construir o MVP.

## 4.4 Painel web local — primeira fatia após o backend

**RF13 — incluído no escopo do projeto, para E5.** Página local servida pelo
próprio simulador, no mesmo Docker, consumindo exclusivamente o controle
administrativo sob `/__simulator`. Não integra o aceite do backend E1–E4 nem
substitui o integrador real ou a conferência independente em E3.

| Área | Escopo da primeira fatia |
|---|---|
| Cenário | Consultar configuração e selecionar cenário, latência e falhas já suportados pelo backend, respeitando suas condições de alteração |
| Execução | Avançar tarefas e relógio no modo manual; resetar com confirmação explícita do cenário e dos efeitos |
| Acompanhamento | Listar remessas e lotes, com filtros por cenário, UG, competência e assunto; mostrar estados e erros |
| Resultados | Exibir contagens recebidas, processadas, descartadas e efetivas, distinguindo histórico e cobertura não implementada |
| Inspeção | Consultar detalhes e registros de um lote com paginação, preservando identificadores e decimais exatos |

Reutilizar casos de uso e regras do backend, sem acesso direto ao SQLite,
duplicação de validações de negócio ou alteração da API compatível. Consultas
administrativas de listagem/detalhe necessárias devem estar disponíveis e
validadas antes de iniciar a tela; não são novas rotas compatíveis do TCE.
Atualização periódica é suficiente; abrir ou atualizar a página não dispara
processamento nem avança relógio. Comandos exigem ação explícita e exibem seu
resultado ou erro, sem sucesso aparente quando rejeitados pelo backend.

O acesso usa a chave administrativa própria; ela não pode estar embutida nos
arquivos da página. Dados originais permanecem no ambiente local controlado,
sem recursos externos que os transmitam. O painel evidencia que sucesso
estrutural não comprova validação tributária ou homologação.

Ficam fora desta fatia: portal completo, gestão multiusuário, edição genérica de
dados, montagem/envio de lotes em lugar do integrador, relatórios elaborados,
WebSocket e aplicação frontend com implantação separada.

Aceite E5: preparar um cenário suportado pela tela, observar um envio do
integrador, conferir estados/erros/contagens/detalhes contra a API administrativa,
executar avanço manual e reset confirmado. Verificar consultas sem efeitos,
negação sem chave administrativa, falha de comando visível, paginação,
preservação de identificadores/decimais, uso por teclado e rótulos dos controles.
Documentar a reprodução local e manter os testes do backend aprovados.

# 5. Contratos e superfície de API

## 5.1 Inventário de operações do contrato inicial

O [contrato E0](contrato-e0.md#inventário-de-operações) é a referência dos parâmetros, headers, corpos e respostas. Caminhos relativos ao simulador, sem prefixo de gateway presumido. JSON UTF-8 para corpos; `auth_token` nas chamadas após login. Definições por relato do cliente e decisões derivadas aprovadas em 23/10, sem OpenAPI oficial ou captura independente.

| Operação | Método e caminho | Entrada principal |
|---|---|---|
| Login | `POST /autenticacao/login` | Headers `codigoAcesso`, `senha`; query `codigoUg`, `descricaoEmpresaTI`, `descritivoSoftware`; retorno `chave` |
| Abrir | `GET /v6/tributario/iniciarEnvio` | Query `anoMes` inteiro, `cancelamentoAutomatico=NAO`; retorno `chavePacote` |
| Receber lote | `POST /v6/tributario/enviarParcialLote` | Chave na query e uma lista de assunto no corpo |
| Disparar | `POST /v6/tributario/iniciar-processamento` | Chave na query, corpo vazio |
| Consultar lote | `GET /v6/tributario/consultarPorNumeroLote/{numeroLote}` | Número no caminho; retorno com `situacao` |
| Finalizar | `POST /v6/tributario/finalizarEnvio` | Chave e 17 totalizadores string no corpo; ausentes têm `"0"` |
| Descartar assunto | `POST /v6/tributario/descarteParcialPorAssunto` | Query `chavePacote`, `assunto`; corpo vazio |
| Cancelar envio aberto | `POST /servicosGerais/cancelarEnvioParcial` | Chave na query, corpo vazio |

Consulta agregada por chave e descarte explícito de lote não integram o contrato inicial. Não criar rotas alternativas para o nome interno “envio-lote” do integrador.

## 5.2 Pacote de contrato derivado

O [pacote 2026/V6/6.06](contratos/2026-v6-6.06/README.md) reúne cinco fontes incorporadas byte a byte, hashes, origem, datas conhecidas, [schemas JSON](contratos/2026-v6-6.06/schemas.json) e [exemplos fictícios](contratos/2026-v6-6.06/exemplos.json). Os hashes conferem com 21/10; a data original de obtenção e versão da extração são desconhecidas e permanecem declaradas. O material anterior à V6 é apenas histórico. Nenhum dado original do piloto integra o pacote.

Schemas usam DTOs e obrigatórios incondicionais do leiaute, com diferenças do cliente explícitas: `numeroParcelas` no plural, prazo prescricional derivado e campos imobiliários/transferência não enviados fora do schema. Tipos numéricos saem sem aspas; exceção confirmada são os totalizadores da finalização. Obrigatórios ausentes/nulos são rejeitados; opcionais admitem ausência/null; desconhecidos são rejeitados no modo estrito. Condições, defaults e regras tributárias permanecem não implementados.

As decisões prioritárias estão definidas documentalmente; datas desconhecidas e ausência de captura independente são limites de evidência, sem apresentação como fatos comprovados. A seção 15 registra o fechamento documental de E0 e a revisão autorizada: preparo concreto da referência e conferência independente local em E3, sem fornecimento de SQL interno. Código bruto do integrador é restrição informada, não pendência.

## 5.3 Autenticação e erros

Login usa `POST /autenticacao/login`, os headers e query da tabela; resposta JSON `{"chave":"<token>"}`. Cliente transmite o valor em `auth_token`. HTTP 401 provoca novo login e repetição da chamada rejeitada antes de efeitos. O simulador usa somente credenciais fictícias.

Token tem validade padrão de uma hora, configurável por cenário e relógio virtual. As fontes registram durações conflitantes; a duração escolhida é local, sem alegação oficial. Envio tem 72 horas de inatividade; operações compatíveis bem-sucedidas sobre a chave, inclusive consulta, renovam atividade sem avançar processamento, como aproximação aprovada.

Tabela HTTP derivada: 200 sucesso/consulta válida; 400 JSON/parâmetros malformados; 401 autenticação; 403 UG não autorizada no login ou competência bloqueada para mutação; 404 recurso inexistente/inacessível; 409 sequência/estado/totalizadores; 422 schema/cobertura; 500 falha interna. Os erros usam `codigo` próprio `SIM_...`, `mensagem` e `correlacao`, sem códigos de CON ou textos oficiais inventados.

Erro assíncrono permanece na consulta HTTP 200 com `situacao` e `retornoErros.erros[]`. `RECEBIDO`/`EM_PROCESSAMENTO` mantêm consultas; sucesso de todos os lotes libera o próximo assunto; qualquer terminal de erro/aborto interrompe com diagnóstico. Na finalização, HTTP 200 basta ao cliente.

# 6. Arquitetura de referência

A [arquitetura adotada](../ARCHITECTURE.md) define Java 21, Spring Boot 3.5, Spring MVC, Maven, SQLite/JDBC sem ORM e testes JUnit 5, em um monólito modular com portas e adaptadores. Payloads originais ficam como bytes no SQLite junto dos metadados, substituindo a proposta anterior de diretório separado. Domínio e aplicação usam Java puro. A versão 3.5.10 confirmada do integrador não fixa automaticamente a versão corretiva do futuro build do simulador.

| Componente | Responsabilidade |
|---|---|
| Adaptador compatível | Implementar o contrato TCE selecionado sem parâmetros exclusivos de teste no payload |
| Autenticação local | Vincular token a usuário/UG fictícios e controlar expiração |
| Motor de remessas | Aplicar precondições e coordenar lotes, dados aceitos e finalização |
| Executor assíncrono | Consumir trabalhos persistidos e produzir transições observáveis |
| Validador por perfil | Aplicar somente regras cadastradas e expor a cobertura |
| Armazenamento | Guardar requisições, dados, vínculos, resultados e agenda de tarefas |
| Controle administrativo | Preparar, inspecionar, avançar tempo, injetar falhas e reiniciar cenários |

O controle administrativo adotado usa `/__simulator`, com chave própria e portas de entrada; operações concretas serão especificadas nas fatias de implementação, sem alterar o HTTP compatível. O integrador não dependerá dela para funcionar: o executor de testes a utiliza antes e depois do fluxo normal.

Não é necessário Kafka, Redis ou um cluster para o MVP. Caso uso compartilhado e grande concorrência sejam requisitos confirmados, reavaliar persistência e operação antes de escolher o banco definitivo.

# 7. Modelo de dados e invariantes

| Entidade proposta | Dados mínimos |
|---|---|
| Execução/cenário | ID, geração de reset, perfil, seed, instante inicial e plano de falhas |
| UG e usuário | Identificadores fictícios, vínculo e permissões simuladas |
| Competência | UG, ano/mês e condição aberta/ratificada |
| Remessa | Chave opaca, UG, competência, etiqueta, política de cancelamento e fase interna |
| Lote | Número, remessa, sequência de chegada, situação, payload/hash, contagens e erros |
| Registro recebido | Assunto, chave de negócio e localização no payload original |
| Registro efetivo | Conteúdo aceito, versão e origem em lote/remessa, com trilha de remoção |
| Trabalho | Remessa/lote, momento agendado, condição de execução e geração do cenário |
| Evento | Chamada, resposta sanitizada, transição, instante e correlação |

As fases internas sugeridas para remessa — aberta, em processamento, recuperável, finalizada, cancelada e expirada — são conceitos do simulador. Não serão publicadas como enums oficiais do TCE.

Invariantes de projeto:

1. Consultar não deve, por si só, alterar o resultado do processamento. O avanço decorrerá do executor ou de comando administrativo explícito.
2. Cada lote conserva o vínculo com sua remessa, UG e cenário; uma chave de outra UG não dá acesso a ele.
3. Persistir o lote e registrar o trabalho correspondente deve ser consistente. Um reinício não pode esquecer o lote já reconhecido ao cliente.
4. Reiniciar o cenário invalida os trabalhos da geração anterior; eles não podem modificar o novo estado.
5. Dados recebidos, dados processados e dados efetivos são conjuntos distintos. O relatório deve permitir explicar sua diferença.
6. Remover dados de um assunto não equivale a reescrever todo o histórico de status. O histórico e o conjunto efetivo precisam ser rastreáveis separadamente.
7. Contagens e valores são calculados a partir dos payloads e de seus efeitos; nenhum resultado é gerado apenas porque o cenário se chama “sucesso”.
8. Valores monetários usam representação decimal exata; identificadores preservam os zeros e tipos exigidos pelo contrato.

O objetivo completo inclui referências de negócio entre os seis assuntos; sua validação foi adiada na etapa inicial. O fluxo deve conservar as referências recebidas e os vínculos técnicos, mesmo sem validar integridade entre assuntos. Atomicidade por registro/lote e visibilidade de dados em andamento ainda precisam de casos observados. A recuperação selecionada consta na seção 8; não se deve anunciar uma transação de banco conveniente como garantia do TCE.

# 8. Comportamento e processamento

## 8.1 Fluxo nominal proposto

O executor de teste prepara a competência e as credenciais fictícias. No fluxo relatado, o integrador abre o envio uma vez e reutiliza a `chavePacote`: transmite todos os lotes de um assunto, solicita processamento e consulta até terminar; repete esse ciclo nos demais assuntos e finaliza ao término. A ordem informada é saldo inicial em janeiro, contribuintes, lançamento de crédito, prazo prescricional quando houver dados, diário de arrecadação e baixa. A sequência e os critérios de avanço foram confirmados por relato em 23/10; sua comprovação pelo integrador pertence à validação futura. Ao fim, comparar conteúdo e contagens com o esperado independente e verificar a conclusão no integrador.

Deverão existir dois modos de execução: automático, com latência configurada, para uso exploratório; e manual, com avanço explícito de tarefas/tempo, para CI. A mesma sequência de entrada, seed e comandos deverá produzir o mesmo resultado lógico. A passagem do tempo real não poderá ser necessária para testar expiração longa.

## 8.2 Situações externas e recuperação

Vocabulário documentado no fluxo assíncrono: `RECEBIDO`, `EM_PROCESSAMENTO`, `PROCESSADO_SUCESSO`, `PROCESSADO_ERRO_NEGOCIO`, `PROCESSADO_ERRO_INTERNO`, `ABORTADO_ERRO_LOTE_ANTERIOR`, `ABORTADO_AGUARDANDO_DESCARTE_PARCIAL` e `ABORTADO`. O cancelamento automático define se a recuperação conserva dados anteriores; o fluxo de descarte deve respeitar dependências. [F11](https://jira-tcesc.atlassian.net/wiki/spaces/SD/pages/759267332).

Para V6, acrescentar `LOTE_DESCARTADO` e preservar as exceções do caso de um assunto. A documentação também distingue lotes mistos, cuja recuperação fica para ampliação futura. Ela prevê timeout de 72 horas, renovado pelas operações indicadas. Os casos incluídos serão transformados em tabelas de decisão antes de codificar. [F04](https://jira-tcesc.atlassian.net/wiki/spaces/SD/pages/1582465028).

As tabelas do [contrato E0](contrato-e0.md#comportamentos-selecionados) fecham o recorte inicial: erro intermediário descarta o lote, aborta seguintes ainda não processados do mesmo assunto e preserva anteriores válidos até descarte. Descarte por assunto remove seus dados no envio aberto e mantém chave, outros assuntos e controles históricos; durante processamento é rejeitado com 409.

Finalização após recuperação considera somente lotes ativos em sucesso, ao menos um lote ativo e totalizadores efetivos corretos; históricos descartados/abortados não bloqueiam. Cancelamento integral remove recebidos/processados/efetivos do envio atual, invalida seus trabalhos e exige nova chave/reenvio integral, preservando histórico e envios anteriores.

Segunda abertura para UG/competência com envio aberto, disparo ativo/sem elegíveis, repetição de finalização/cancelamento e mutação em chave encerrada retornam 409 sem efeitos. Só `cancelamentoAutomatico=NAO` é suportado; outros valores/omissão retornam 422. T05, sem movimento e T17 são exclusões expressas do aceite inicial, sem retirada do objetivo futuro. A lista de lotes vazia nunca equivale a sucesso de processamento ou finalização.

## 8.3 Retransmissão e resposta perdida

Guardar o hash do payload ajuda a investigar repetição, mas não cria uma garantia oficial de idempotência. O modo compatível deverá reproduzir o comportamento confirmado do TCE para repetição de abertura, envio, processamento e finalização.

Incluir duas falhas diferentes: falha antes de aceitar a operação, sem efeito persistido; e falha depois de aceitar, com resposta perdida. O segundo caso testa se o integrador consulta e reconcilia o estado antes de reenviar, evitando duplicidades. Qualquer política temporária enquanto a semântica oficial estiver desconhecida deverá constar no manifesto como aproximação.

No caso prioritário aprovado, o lote é persistido em `RECEBIDO` e a resposta se perde, sem iniciar processamento. Nova tentativa de envio para aquele assunto retorna erro de envio aberto com lotes sem processamento; a recuperação é descartar o assunto e reenviar sob a mesma chave. Controles históricos permanecem. Não há deduplicação por hash nem identificador de idempotência informado. A nova tentativa retorna 409 com `SIM_ENVIO_ABERTO_SEM_PROCESSAMENTO` e envelope derivado aprovado. A marca desse cenário não bloqueia a recepção normal de múltiplos lotes. Permitir descarte de `RECEBIDO` nessa recuperação diverge do manual local e permanece aproximação explícita.

# 9. Validação e fidelidade

| Nível | O que verifica | Aceite no MVP |
|---|---|---|
| Contrato | Tipos, campos, enums, formatos e estruturas do perfil | Obrigatório para as operações e assuntos habilitados |
| Fluxo | Ordem das operações, vínculo de chaves, concorrência e bloqueio de competência | Obrigatório |
| Domínio selecionado | Referências cadastrais, cronologia e regras dos casos do piloto | Lista fechada de regras com origem e testes |
| Catálogo completo | Todas as CONs e exceções anuais | Fora do MVP |

Na entrega inicial, somente contrato estrutural e fluxo são priorizados, além da conferência de conteúdo e contagens. O nível de domínio selecionado permanece como evolução, sem regras ou CONs concretas escolhidas. Rejeitar ausência e `null` em campos obrigatórios é decisão do contrato derivado; opcionais aceitam ausência e `null`; condições tributárias e catálogos externos ficam explicitamente adiados. Nenhuma aprovação estrutural comprova validade tributária.

Lista inicial proposta para escolha com a equipe: referência cadastral ausente; chave de negócio repetida; data de movimentação incompatível; baixa anterior ao crédito; totalizadores divergentes; tentativa em competência bloqueada. Cada regra deve indicar a etapa de aplicação: recepção, processamento ou finalização. Estas são candidatas, não uma afirmação de que todas correspondem a CONs já analisadas.

Para os assuntos sem validação profunda, o manifesto separará “formato compatível” de “regra de negócio implementada”. Campos extras e desconhecidos seguirão a política confirmada no contrato; não serão descartados silenciosamente.

O manifesto de cobertura deverá conter: operação, assunto, regra, versão de referência, evidência, teste associado e situação — implementada, aproximada, não implementada ou pendente. Ele acompanhará todo relatório de execução.

O [manifesto documental inicial](contrato-e0.md#manifesto-inicial-de-cobertura) registra apenas planejamento. Como ainda não há aplicação nem testes, nenhuma operação está implementada ou validada.

# 10. Piloto inicial e prova do fluxo completo

## 10.1 Piloto inicial selecionado

O solicitante executará e conferirá o piloto do cliente/alias **14**, competência **202609**, sobre cópia local sem alterações, cargas ou processamento concorrente. Dados conservam valores originais de produção. O solicitante preparará a referência antes do envio do piloto, quando o simulador estiver testável; consultas, estrutura do banco e resultados permanecem internos por compliance. E0 registra o roteiro e os critérios; não exige receber esse material. O simulador recebe somente HTTP; seleção e consultas pertencem ao ambiente do integrador/origem. Dados originais ficam no ambiente local controlado, fora do Git e de serviços externos; exemplos e evidências compartilhados serão sanitizados. Credenciais do simulador continuam fictícias.

O esperado independente será gerado antes do envio por SQL, com todos os campos enviados, chaves, contagens e multiplicidades por assunto, preservando identificadores e decimal exato, sem reutilizar a montagem de lotes. O cliente particiona cada assunto em até **10.000 registros por lote**; o solicitante confirmou **21.770 lançamentos de crédito** em 23/10/2026 para cliente 14 / 202609. A previsão é **3 lotes: dois de 10.000 e um de 1.770**, sem execução ou consulta independente ao banco nesta tarefa. O particionamento e a união dos registros ainda precisam ser demonstrados no piloto; esse tamanho não é limite obrigatório do simulador. O roteiro e as pendências estão no [contrato E0](contrato-e0.md#piloto-e-esperado-independente).

Saldo inicial e prazo prescricional podem estar ausentes nessa execução; os seis assuntos continuam previstos para recepção. O aceite parcial exige conteúdo e contagens reconciliados, sequência e estados observáveis, sem perdas ou duplicações, e registro das falhas prioritárias exercitadas. Ausência de fracionamento demonstrado, assuntos ou regras deixa cobertura pendente. Esse piloto não encerra T01 ou o MVP completo.

## 10.2 Cenário sintético complementar proposto

Propor uma UG fictícia A, uma UG B para isolamento, competência 202601 e relógio fixo. Janeiro permite incluir saldo inicial nesse cenário complementar, cobrindo os seis assuntos confirmados. Uma variante em fevereiro testa continuidade de estado; mês sem movimento fica para evolução, fora do aceite inicial. O leiaute exige atenção específica a saldo inicial e permite pacote sem movimentação, observadas suas condições. [F09](https://jira-tcesc.atlassian.net/wiki/spaces/SD/pages/2154923885).

Conjunto lógico de exemplo, a adaptar aos campos e referências efetivamente usados:

| Dado sintético | Quantidade | Relação pretendida |
|---|---|---|
| Contribuintes | 3 | Identificadores estáveis, nomes fictícios |
| Créditos | 4 | Vínculos e datas definidos no manifesto de entrada |
| Arrecadações | 2 | Vinculadas aos créditos escolhidos |
| Baixa | 1 | Caso nominal ou data inválida em variante negativa |
| Saldo inicial | 1 | Saldo de abertura com referência de exercício anterior conforme DTO |
| Início do prazo prescricional | 1 | Vinculado ao crédito compatível com as pré-condições do perfil |

Essas quantidades são propostas de teste complementar, não prescrições do TCE nem o recorte do piloto inicial. A equipe fornecerá os atributos e o estado anterior para o conjunto sintético, inclusive saldo inicial e prazo prescricional. A preparação pode semear pré-condições históricas pelo controle administrativo, distinguindo-as dos registros enviados pelo integrador. Exemplos sintéticos e compartilháveis usam dados fictícios; dados originais permanecem restritos ao piloto local da seção 10.1.

Roteiro de aceite ponta a ponta:

1. Preparar esse conjunto no ambiente de origem de teste, por procedimento externo ao simulador, e gerar um manifesto independente com chaves e contagens esperadas.
2. Carregar somente a configuração do destino: credenciais fictícias, UG, competência aberta e plano de processamento.
3. Configurar o integrador real para apontar ao simulador e acionar seu fluxo normal, com lotes separados por assunto e ciclos sob a mesma chave conforme o contrato derivado confirmado por relato em 23/10.
4. Em pelo menos uma remessa, fracionar o assunto em dois ou mais lotes, com mais de 10.000 registros do assunto no recorte e partições de até 10.000 no cliente. Verificar que o início de processamento remoto ocorre após o envio de todos esses lotes.
5. Antes do processamento, provar que os payloads foram conservados e ainda não equivalem aos dados efetivos finais.
6. Executar processamento e consultas por assunto sob a mesma chave e finalizar após todos os ciclos. Conservar referências entre assuntos e, nos cenários de continuidade, entre envios anteriores.
7. Comparar o manifesto de entrada com a união dos registros efetivos por chave de negócio; conferir conteúdo selecionado, contagens, duplicatas e perdas. Gerar o esperado sem reutilizar o algoritmo de montagem de lotes que está sendo testado.
8. Conferir o estado de conclusão no integrador e exportar evidências. Repetir após reset com o mesmo resultado lógico.

Sem uma maneira de preparar entrada conhecida no serviço independente, ainda é possível testar a interface de destino; permanece limitada a comprovação da obtenção e transformação anterior aos lotes. Essa dependência deve aparecer no aceite, sem acoplar o simulador à origem.

# 11. Matriz mínima de cenários e aceite

| ID | Exercício | Resultado observável esperado |
|---|---|---|
| T01 | Seis assuntos, um deles com vários lotes; ciclos sob a mesma chave definidos por relato no contrato derivado | Referências entre assuntos e envios aplicáveis, conteúdo e contagens corretos; integrador conclui |
| T02 | Receber e consultar antes do disparo | Não avançar processamento pela simples consulta |
| T03 | Finalizar durante processamento | Resposta de impedimento compatível; sem conclusão prematura |
| T04 | Regra de domínio em lote intermediário | Erro com referência ao registro e efeitos coerentes nos demais lotes |
| T05 | Cancelamento automático ativado/omitido | Fora do aceite inicial; retornar 422 de cobertura, sem implementar os efeitos automáticos |
| T06 | Recuperação com cancelamento desativado | Corrigir e reenviar sem perder dados anteriores válidos |
| T07 | Corrigir e reprocessar um assunto no envio aberto | Conservar outros assuntos e aplicar os efeitos esperados do descarte/reenvio sob a mesma chave |
| T08 | Descarte automático de lote, descarte por assunto e cancelamento integral aberto | Conferir efeitos e rejeitar descarte durante processamento; rota explícita de lote fora do recorte |
| T09 | Competência ratificada | Impedir operações cobertas; reset permite repetir o teste |
| T10 | UG/token/chave incompatíveis | Negar acesso e evitar alteração de outro cenário |
| T11 | Token expira no meio do fluxo | 401 antes de efeitos; integrador renova token e repete chamada, sem perder evidência |
| T12 | Demora e resposta perdida após aceitação | Reconciliação observável; repetição segue política confirmada |
| T13 | Reinício e reset com trabalhos pendentes | Reinício conserva estado; reset impede trabalho antigo no novo cenário |
| T14 | Duas aberturas simultâneas | Uma abertura aceita; outra 409, sem efeitos na mesma UG/competência |
| T15 | Processar sem lotes elegíveis e repetir disparo ativo | 409 sem efeitos, sem sucesso inventado |
| T16 | Pacote sem movimento e janeiro | Sem movimento adiado; janeiro complementar com recepção estrutural, sem regras fiscais adiadas contadas como aprovadas |
| T17 | Sequência de remessas e limite mensal | Fora do aceite inicial; fronteira 64/65 exige contrato futuro; não impor cota mensal agora |
| T18 | Campo/assunto sem suporte | Não produzir aprovação silenciosa no modo estrito |
| T19 | Expiração longa com relógio virtual | 72h de inatividade; operações bem-sucedidas sobre a chave, inclusive consulta, renovam atividade sem avançar processamento; regra derivada |

T01 só passa com o integrador real. Os demais podem começar como testes do simulador e ganhar execução pelo integrador conforme suas capacidades. Os resultados do recorte inicial estão definidos no contrato derivado; exclusões acima não contam como aceite do MVP completo. Os quatro pontos de teste foram confirmados: HTTP compatível, administração separada, integrador real/SQL independente e núcleo/SQLite.

Falhas prioritárias selecionadas em 23/10: token inválido/expirado, payload inválido, erro tributário assíncrono e resposta perdida após recebimento. O erro tributário será um cenário controlado, sem alegar regra fiscal implementada. Demora e HTTP 500 não foram priorizados, mas permanecem na matriz geral. O piloto inicial pode contribuir parcialmente para T01 e demais casos; não substitui seus critérios completos. Nada foi executado nesta revisão documental.

# 12. Portabilidade, operação e evidências

Docker é requisito confirmado. Entrega operacional proposta: imagem OCI/Docker versionada com runtime Java 21; arquivo Compose para instância única; configuração de exemplo sem segredos; volume local; instruções de início/reset; verificação de saúde; comando de execução da suíte; manifesto de compatibilidade. A imagem deverá executar em Linux no CI. Plataforma inicial confirmada: **Linux amd64**. arm64 e uso compartilhado ficam fora da operação inicial, sem promessa de suporte.

O ambiente deverá funcionar sem acesso ao TCE após baixar os artefatos. Não haverá modo proxy com encaminhamento ao destino real. Cada execução de CI poderá usar instância, porta e volume próprios, sem exigir um header de teste adicional nas chamadas do integrador.

Para uso compartilhado, isolamento entre equipes e autenticação administrativa passam a ser requisitos de operação, com validação específica. O padrão inicial é instância por execução, mais simples de reproduzir e limpar.

Cada relatório deverá conter: versão da imagem e contrato; configuração/seed; classificação da origem dos dados (sintética ou produção em base local de testes), sem conteúdo sensível; sequência de operações; IDs de correlação; estados observados; contagens recebidas/aceitas/removidas; divergências; regras executadas e limitações. Logs comuns não devem expor senha, token ou dados pessoais. Payloads originais e resultados SQL ficam no ambiente local controlado, fora do versionamento e de serviços externos, com limpeza junto com a execução. Relatórios compartilhados exigem sanitização.

Metas de engenharia propostas, a calibrar com o hardware real: inicialização em até 30 segundos; suíte funcional pequena em até dois minutos; teste opcional de 50 mil registros por lote sem perda. São metas do projeto, não promessas de desempenho ou limites do TCE. Medir uso de memória e duração e fixar orçamento na etapa do piloto.

# 13. Plano de desenvolvimento e entregáveis

| Etapa | Entrega | Condição de saída | Ordem de grandeza proposta |
|---|---|---|---|
| E0 — Fechar contrato e piloto | Inventário do cliente, contrato derivado, exemplos, assuntos e decisões | Contrato prioritário e roteiro do piloto definidos: responsável, recorte, campos, critérios e registro; sem exigir entrega do SQL interno. Concluída documentalmente no recorte | 2–4 dias úteis |
| E1 — Recepção e estado | Autenticação local, abertura, lotes persistidos, consultas e evidências | Receber payload real do cliente e reabrir a instância sem perdê-lo | 3–5 dias úteis |
| E2 — Ciclo e recuperação | Executor, finalização, cancelamento/descarte, relógio e regras selecionadas | Tabelas de decisão e testes dos efeitos sobre os dados | 5–8 dias úteis |
| E3 — Piloto ponta a ponta | Referência independente preparada internamente antes do envio; execução e conferência local pelo solicitante, sem compartilhar SQL/estrutura/resultados | T01 e casos prioritários demonstrados com evidências | 3–5 dias úteis |
| E4 — Empacotar e documentar | Imagem, Compose, CI, guia, cobertura e relatório de aceite | Outra pessoa reproduz instalação, teste e reset | 2–3 dias úteis |
| E5 — Painel web local | Primeira fatia RF13, conforme seção 4.4 | E1–E4 concluídas no recorte contratado e consultas administrativas necessárias validadas antes de iniciar; aceite da seção 4.4 demonstrado ao concluir | Reestimar após o backend |

Estimativa preliminar original de E0–E4: 15–25 dias úteis de trabalho técnico, para uma pessoa com apoio de QA e acesso ao responsável pelo integrador, após disponibilidade dos insumos. Trata-se de faixa de planejamento baseada nas entregas propostas, sem análise do código do cliente. Prazo de calendário dependerá da equipe e dos tempos de resposta.

Reestimar ao concluir E0. NFS-e, compatibilidade legada, autenticação adicional, interface visual, grandes volumes e regras tributárias extensas são itens que podem ampliar materialmente essa faixa. Bloqueios de contrato não devem ser consumidos como implementação baseada em suposição.

E5 é posterior ao backend e não entra na faixa original de 15–25 dias. A faixa
preliminar de 2–4 dias úteis adicionais para RF13 é julgamento sem protótipo,
condicionado à prontidão das consultas administrativas; reestimar com o backend
concluído. Evoluções tributárias explicitamente adiadas não bloqueiam E5, desde
que não sejam requisitos do recorte contratado e estejam identificadas como
não implementadas. E3 pendente ou saída E4 não demonstrada mantém E5 bloqueada.

O solicitante confirmou que gerará o esperado por SQL, executará o piloto e conferirá os resultados. Permanecem sugeridas: desenvolvimento cuida do simulador e contrato; responsável pelo integrador esclarece chamadas; especialista tributário seleciona regras na evolução. O preparo da entrada continua externo ao simulador. As estimativas acima são do MVP completo original e devem ser revistas após fechar E0 para o recorte inicial; não são prazo atualizado desta revisão.

Situação atual: contrato derivado inicial consolidado, fontes/schemas reproduzíveis e nenhum ensaio executado. E0 está concluído documentalmente no recorte aprovado após revisão autorizada do critério de saída; preparação concreta da referência e conferência local continuam pendentes de E3. E1–E4 não foram implementados. Guias operacionais, imagem/Compose, CI e relatórios reais serão produzidos nas etapas correspondentes, a partir dos comandos e artefatos que existirem.

RF13/E5 está incluído e não implementado; sua dependência de conclusão do backend
permanece aberta. O painel não antecipa nem encerra aceite E3/T01.

# 14. Critérios de conclusão do MVP

1. O integrador aponta ao simulador por configuração, sem alterar sua lógica de agrupamento e envio. Eventual ajuste necessário para separar URL e credenciais é identificado e concluído no cliente.
2. A pessoa responsável consegue instalar, carregar cenário, executar, inspecionar e reiniciar a partir do guia, em máquina/CI definidos no piloto.
3. O fluxo completo prova conteúdo, vínculos e contagens, incluindo múltiplos lotes; não se limita a verificar strings de status.
4. A matriz mínima aplicável ao contrato é executada, com os casos pendentes encerrados ou explicitamente retirados do escopo acordado antes do aceite.
5. Falhas, recuperação e reset são reprodutíveis, sem dependência de calendário municipal ou disponibilidade do TCE.
6. Nenhuma execução aprovada no modo estrito contém operação ou assunto ignorado silenciosamente.
7. Pacote de contrato, manifesto de cobertura, testes e evidências identificam as aproximações. Conformidade com o subconjunto não é apresentada como homologação oficial.

A homologação oficial continuará útil para diferenças de contrato e validações não cobertas, quando houver condições disponíveis. O benefício do simulador é permitir regressão frequente e investigação controlada entre essas oportunidades.

# 15. Situação das decisões e pendências para implementação

Consolidação de 23/10/2026 após pergunta/resposta e aprovação do plano. As versões anteriores estão no histórico dos tickets. Relato do cliente e decisão derivada não comprovam comportamento oficial. O [contrato E0](contrato-e0.md#pendências-e-condição-de-saída-de-e0) registra revisão autorizada por compliance: E0 exige roteiro, responsável, recorte e critérios, definidos documentalmente; preparar referência e comparar localmente ficam em E3. SQL, estrutura interna e resultados não precisam ser fornecidos. E0 documental concluído no recorte, sem aceite do piloto. Volume confirmado por relato de 21.770 lançamentos, ainda sem ensaio.

| ID | Tema | Definido e origem | Situação remanescente |
|---|---|---|---|
| D01 | Assuntos e dependências | Seis assuntos, uma lista não vazia por lote; sem outra dependência informada | Novos assuntos exigem escopo próprio |
| D02 | Plataforma | Java 21 / Spring Boot 3.5.10 no integrador por relato; stack do simulador adotada na arquitetura | Build futuro fixa versões corretivas, sem confundir relato com execução |
| D03 | Contrato e falhas | Fontes, schemas, exemplos e erros derivados; quatro falhas prioritárias | Sem ensaio ou captura independente; validação futura |
| D04 | Entrada e esperado | Cliente 14 / 202609, cópia local sem alterações; solicitante gera SQL e executa/confere | Definição E0 concluída; referência interna e comparação local pendentes de E3; 21.770 créditos por relato |
| D05 | Perfil e fontes | 2026/V6/6.06, manual prevalece, cinco fontes incorporadas com hashes | Data original desconhecida declarada; CONs ficam na evolução |
| D06 | Webhook e relatórios | Sem dependência; fora do contrato inicial | Reavaliar somente com evidência de necessidade |
| D07 | Cancelamento automático | `NAO` na query; SIM, omitido e outros recebem 422 | T05 fora do aceite inicial |
| D08 | Operação e volume | Linux amd64, instância/volume local por execução; cliente particiona até 10.000 | Volume documental confirmado: 21.770 créditos, previsão de 3 lotes; desempenho/execução ainda não medidos; arm64 fora do inicial |
| D09 | Profundidade inicial | Estrutura, sequência, conteúdo, contagens; opcionais nulos/ausentes e extras rejeitados | Regras tributárias, condicionais, catálogos e relações adiados |
| D10 | Recuperação e repetição | Estados, HTTP, totais efetivos, finalização após recuperação e conflitos definidos | Aproximações explícitas; sem movimento/T17 fora do aceite inicial |
| D11 | Cancelamento e descarte | Remoção do envio atual, preservação de histórico/anteriores, nova chave; descarte por assunto preserva chave | Rota explícita de lote fora do inicial; recuperação de RECEBIDO diverge do manual |
| D12 | Remessa/envio/chave | Uma UG/competência, chave opaca retornada pelo destino; ciclos e leitura de `situacao` definidos por relato | Sem captura independente; prova do fluxo pertence ao piloto |

Por solicitação expressa de 23/10/2026, substitui-se o bloqueio anterior de SQL em E0 pelo roteiro e critérios de conferência já definidos no contrato. O solicitante preparará a referência independente antes do envio e fará a comparação em E3, mantendo SQL, estrutura do banco e resultados internamente. Compartilha somente conclusão e divergências sanitizadas quando permitido pela empresa. Esse limite impede revisão direta do material pelo agente e pode adiar a descoberta de diferenças, mas não elimina a comparação obrigatória nem comprova T01. E0 está concluído documentalmente no recorte; não houve implementação ou publicação.

# 16. Riscos e medidas de projeto

| Risco concreto | Efeito | Medida proposta |
|---|---|---|
| Contrato incompleto | Cliente funciona no fake e falha no TCE | Congelar contrato e confrontar amostras; não aceitar lacunas como conformidade |
| Regras fiscais insuficientes | Falso resultado de aprovação dos dados | Manifesto por regra e revisão pelo especialista tributário |
| Mistura de versões | Fluxo errado ou campos incompatíveis | Perfil anual explícito e suíte própria |
| Resposta perdida e repetição | Duplicidade ou envio inconclusivo | Ensaios com falha antes/depois da aceitação e reconciliação |
| Origem sem dados controlados | Teste cobre só o destino | Procedimento externo de fixture e esperado independente |
| Assunto ignorado | Sucesso artificial | Modo estrito e erro de cobertura |
| Reset concorrente | Contaminação de uma execução nova | Geração de cenário e invalidação de tarefas antigas |

# 17. Fontes, situação de acesso e rastreabilidade

Pesquisa e leitura em 20/10/2026. “Lido” significa conteúdo recuperado por ferramenta de pesquisa; não atesta comportamento de uma chamada autenticada. Os links curtos do Confluence preservam o identificador da página oficial.

| Ref. | Fonte | Uso e situação |
|---|---|---|
| F01 | [Aviso 21/08/2023 — Dados tributários em testes](https://jira-tcesc.atlassian.net/wiki/spaces/SD/pages/132579359) | Lido; cópia de produção e mascaramento |
| F02 | [Extrato e relação com ratificação](https://jira-tcesc.atlassian.net/wiki/spaces/SD/pages/13882932) | Lido; bloqueios por assunto |
| F03 | [Endpoints — API](https://jira-tcesc.atlassian.net/wiki/spaces/SD/pages/1560969264) | Lido; endereços atualizados |
| F04 | [Tributário V6 — Envio assíncrono](https://jira-tcesc.atlassian.net/wiki/spaces/SD/pages/1582465028) | Lido; fluxo, caminhos e particularidades V6; OpenAPI não exportado |
| F05 | [Tributário — Múltiplos pacotes](https://jira-tcesc.atlassian.net/wiki/spaces/SD/pages/2154921985) | Lido; remessa/lote e dimensionamento |
| F06 | [Versão 6.06 de 10/06/2026](https://jira-tcesc.atlassian.net/wiki/spaces/SD/pages/2154922045) | Lido; referência anual e erratas |
| F07 | [CONs 2026](https://jira-tcesc.atlassian.net/wiki/spaces/SD/pages/1517223937) | Índice lido; arquivo 6.9 vinculado não recuperado |
| F08 | [V6.06 — Ratificação Tributário](https://jira-tcesc.atlassian.net/wiki/spaces/SD/pages/2154923870) | Lido; operação exclusiva no portal |
| F09 | [V6.06 — Tributário](https://jira-tcesc.atlassian.net/wiki/spaces/SD/pages/2154923885) | Conteúdo recuperado parcialmente; lista de assuntos e regras gerais; detalhamento completo não auditado |
| F10 | [V6.06 — Nota Fiscal de Serviço](https://jira-tcesc.atlassian.net/wiki/spaces/SD/pages/2154923972) | Lido; separação e dependência de serviço |
| F11 | [Tributário — Envio assíncrono anterior à V6](https://jira-tcesc.atlassian.net/wiki/spaces/SD/pages/759267332) | Lido; comparação histórica, sem substituir V6 |
| F12 | [Tecnologias e Padrões](https://jira-tcesc.atlassian.net/wiki/spaces/SD/pages/13877935) | Lido; formato e transporte |
| F13 | [Mudança no versionamento da API](https://jira-tcesc.atlassian.net/wiki/spaces/SD/pages/758677529) | Lido; prefixo anual e exceções |
| F14 | [Autenticação](https://jira-tcesc.atlassian.net/wiki/spaces/SD/pages/13867166) | Conteúdo recuperado por busca; parâmetros e token |
| F15 | [FAQ — Módulo Tributário](https://jira-tcesc.atlassian.net/wiki/spaces/SD/pages/13912693) | Trechos recuperados; contexto histórico e esclarecimentos |
| F16 | [V7.00 — Tributário / ramo 2027](https://jira-tcesc.atlassian.net/wiki/spaces/SD/pages/2310539061) | Lido; referência futura, não adotada no MVP 2026 |
| F17 | [Utilização das APIs](https://jira-tcesc.atlassian.net/wiki/spaces/SD/pages/13861444) | Lido; inconsistência de duração de token identificada |
| F18 | [Perguntas e Respostas Tributário 2020, v1.06](https://www.tcesc.tc.br/sites/default/files/Perguntas%20e%20Respostas_tributario_Esfinge2020_v1.06.pdf) | Localizado; referência histórica, não validada para perfil V6 |
| F19 | [Códigos de Retorno](https://jira-tcesc.atlassian.net/wiki/spaces/SD/pages/13870220) | Lido; orientação genérica, não matriz por endpoint |
| F20 | [Portal oficial e-Sfinge](https://manualesfinge.tcesc.tc.br/) | Lido; navegação e contatos técnicos |
| F21 | [Swagger atual de homologação](https://api.virtual.testing.tce.sc.gov.br/esfingeonline/swagger-ui.html) | Interface localizada, especificação não extraída |
| F22 | [Swagger informado na conversa](https://virtual.testing.tce.sc.gov.br/esfingeonline/swagger-ui.html) | Não acessível pela ferramenta de pesquisa |

