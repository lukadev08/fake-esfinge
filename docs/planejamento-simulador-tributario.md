---
title: "Simulador e-Sfinge Tributário"
subtitle: "Planejamento do escopo de desenvolvimento"
date: "20 de outubro de 2026 · versão 1.0"
lang: pt-BR
---

# 1. Recomendação e resultado esperado

Desenvolver um simulador HTTP com estado, portável e reiniciável, que ocupe o lugar da API do TCE/SC no ambiente de testes do integrador. O simulador deverá receber os lotes efetivamente produzidos pelo integrador, conservar seus conteúdos, controlar remessas, executar processamento assíncrono controlado e devolver respostas compatíveis com o contrato selecionado.

O resultado esperado é poder repetir o fluxo de integração quantas vezes forem necessárias, inclusive quando os períodos das cidades clientes estiverem ratificados no TCE. O ensaio deverá demonstrar que os registros esperados chegaram ao destino, que não foram perdidos na divisão em lotes e que o integrador reagiu corretamente a sucesso, rejeição, demora e interrupção.

Este documento entrega o planejamento de desenvolvimento. Não contém uma implementação nem uma declaração de conformidade integral com o TCE. As decisões de arquitetura são propostas; comportamentos oficiais e lacunas de pesquisa estão identificados separadamente.

**Restrições confirmadas pelo usuário:** integrador em Java 21 com Spring 3.5, URL base e credenciais configuráveis, operações atuais de processamento interno, envio, consulta de lote e cancelamento; simulador executado em Docker. Os DTOs seguem o Swagger informado na conversa. Exemplos completos de requisições/respostas e erros poderão ser fornecidos futuramente; não foram analisados nesta etapa. A nomenclatura “Spring 3.5” será confirmada no inventário do projeto, presumindo-se Spring Boot 3.5.

**Recorte funcional confirmado:** cadastro de contribuinte, lançamento de crédito tributário, diário de arrecadação, baixa de crédito tributário, saldo inicial e início do prazo prescricional. O cancelamento utilizado atua sobre lotes/assuntos de um envio em andamento e inclui cancelamento parcial. Cada remessa contém um único assunto, com um ou mais lotes; o integrador chama o início remoto de processamento depois de transmitir todos os lotes dessa remessa. O fluxo é denominado atualmente “envio-lote”, cujo nome será ajustado.

**Base inicial recomendada:** exercício 2026, família de endpoints V6, leiaute 6.06 e catálogo de CONs 6.9 como referências a congelar na etapa inicial. A versão efetivamente usada pelo integrador ainda precisa ser confirmada. As publicações de 2027 não devem ser misturadas ao perfil 2026. [F06](https://jira-tcesc.atlassian.net/wiki/spaces/SD/pages/2154922045), [F07](https://jira-tcesc.atlassian.net/wiki/spaces/SD/pages/1517223937), [F16](https://jira-tcesc.atlassian.net/wiki/spaces/SD/pages/2310539061).

# 2. Problema e evidências verificadas

## 2.1 Homologação e produção

O aviso oficial de 21/08/2023 informa que os dados tributários de testes passaram a acompanhar a cópia de produção da semana anterior, com mascaramento de determinados campos. Isso caracteriza uma base copiada; a fonte não demonstra que ambos os ambientes utilizem uma única base transacional. Também não oferece, nesse aviso, um mecanismo de reset por empresa ou município. [F01](https://jira-tcesc.atlassian.net/wiki/spaces/SD/pages/132579359).

A tabela oficial de comportamento após ratificação marca o Tributário como impedido para novos envios e cancelamentos no período ratificado. A combinação entre essa regra e a cópia de produção é uma explicação consistente para os bloqueios relatados. O diagnóstico exato de cada erro continua dependendo da requisição, competência, UG e resposta concreta. Não foi verificado que todo erro de ausência de lotes decorra da ratificação. [F02](https://jira-tcesc.atlassian.net/wiki/spaces/SD/pages/13882932).

O catálogo de endpoints de outubro de 2025 aponta os seguintes endereços de documentação, diferentes do endereço antigo informado na conversa: [F03](https://jira-tcesc.atlassian.net/wiki/spaces/SD/pages/1560969264).

| Ambiente | Swagger indicado atualmente pela documentação |
|---|---|
| Homologação/Sandbox | [Swagger em api.virtual.testing.tce.sc.gov.br](https://api.virtual.testing.tce.sc.gov.br/esfingeonline/swagger-ui.html) |
| Produção | [Swagger em api.virtual.tce.sc.gov.br](https://api.virtual.tce.sc.gov.br/esfingeonline/swagger-ui.html) |

Esses endereços apontam para a interface Swagger. A composição da URL base de serviço deverá ser extraída da especificação; não se deve configurar o cliente usando a URL da página HTML como base.

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

NFS-e é um serviço próprio dentro do módulo, documentado como dependente da conclusão do Tributário. Não está entre os seis assuntos informados. Sua inclusão futura exigirá escopo próprio, com schemas e casos; não basta aceitá-lo como mais uma lista genérica. [F10](https://jira-tcesc.atlassian.net/wiki/spaces/SD/pages/2154923972).

Um assunto sem suporte deverá provocar um erro identificável no relatório de teste. No modo estrito, não poderá finalizar com sucesso. Um modo permissivo poderá existir para investigação, mas sua execução deverá ser marcada como cobertura parcial, sem contar para o aceite.

## 4.3 Fora do MVP

- Captura de dados, migração, saneamento ou montagem de lotes do serviço independente.
- Reprodução do portal TCE Virtual, SGI, assinatura eletrônica, certificados e permissões reais.
- Implementação integral de todas as CONs, cálculos e exceções do TCE.
- Demais módulos do e-Sfinge, SOAP e compatibilidade com vários exercícios simultaneamente.
- Cancelamento de remessa já concluída, que não faz parte do uso informado nesta etapa.
- Recuperação de lotes com assuntos misturados; o fluxo confirmado usa um assunto por remessa.
- Espelhamento de produção, importação automática de dados municipais ou encaminhamento de requisições ao TCE.
- Portal administrativo completo, infraestrutura de mensageria externa e operação de serviço público.
- Webhooks e relatórios oficiais, salvo se o inventário do integrador demonstrar dependência; nesse caso, reavaliar antes de construir o MVP.

# 5. Contratos e superfície de API

## 5.1 Inventário inicial de rotas

Os caminhos abaixo constam no manual V6; são relativos à base de serviço. Não constituem um OpenAPI validado. Métodos, parâmetros e corpos serão fixados no pacote de contrato da etapa E0. Para reconstruir cada caminho, concatenar o prefixo e o sufixo: nas sete primeiras linhas, o prefixo é `/v6/tributario/`; na última linha, é `/servicosGerais/`. [F04](https://jira-tcesc.atlassian.net/wiki/spaces/SD/pages/1582465028).

| Operação | Sufixo do caminho documentado |
|--------------------------|--------------------------------------------------------------------------|
| Abrir remessa | `iniciarEnvio` |
| Receber lote | `enviarParcialLote` |
| Disparar processamento | `iniciar-processamento` |
| Consultar lote | `consultarPorNumeroLote/{numeroLote}` |
| Finalizar remessa | `finalizarEnvio` |
| Descartar assunto | `descarteParcialPorAssunto` |
| Descartar lote | `descartar-lote` |
| Consultar lotes da remessa | `consultarStatusLotePorChavePacote/{chavePacote}` |

Autenticação e a correspondência exata do cancelamento parcial do integrador precisam ser inventariadas. Estão confirmados descarte de lotes/assuntos em andamento e início remoto de processamento. O cancelamento parcial deve ser mapeado às operações reais, sem criar um endpoint adicional com base apenas no nome usado pela aplicação. Cancelamento de remessa concluída fica fora do recorte atual.

A sequência informada pelo usuário é: abrir remessa de um assunto, transmitir todos os seus lotes, iniciar o processamento remoto, acompanhar e finalizar. O simulador deverá testar exatamente essa sequência e revelar chamadas sobrepostas ou fora da ordem contratada. A alteração do nome interno “envio-lote” pertence ao integrador e não exige criar uma rota alternativa no simulador.

## 5.2 Pacote de contrato necessário antes da implementação compatível

Registrar, para cada operação efetivamente usada: método HTTP; caminho; parâmetros e localização; headers; content type; schema de entrada; schema de sucesso e erro; campos opcionais; comportamento de campo ausente versus nulo; tipos numéricos; representação das datas; identificadores; exemplos sanitizados e referências de origem.

A referência de transporte oficial usa REST/JSON, UTF-8 e datas ISO-8601. O versionamento anual se aplica ao envio, enquanto autenticação e serviços gerais permanecem sem esse prefixo anual. Essas orientações não resolvem os detalhes de cada endpoint. [F12](https://jira-tcesc.atlassian.net/wiki/spaces/SD/pages/13877935), [F13](https://jira-tcesc.atlassian.net/wiki/spaces/SD/pages/758677529).

O pacote deverá ser armazenado com data de obtenção, hash e origem. Se não for possível exportar OpenAPI, produzir um contrato derivado dos manuais e dos exemplos do integrador, com lacunas marcadas. Ele não deverá ser apresentado como especificação oficial exportada.

## 5.3 Autenticação e erros

O manual de autenticação descreve os parâmetros `codigoAcesso`, `codigoUg` e `senha`, e o retorno `auth_token`. O simulador usará valores fictícios e emitirá tokens válidos somente nele. O endpoint e os headers exatos ainda precisam ser confirmados. [F14](https://jira-tcesc.atlassian.net/wiki/spaces/SD/pages/13867166).

A página de uso de tokens contém referências conflitantes a duração de uma hora e seis horas. Por isso, o planejamento propõe duração configurável e um cenário determinístico de expiração; não fixa nenhuma dessas durações como regra atual do TCE. [F17](https://jira-tcesc.atlassian.net/wiki/spaces/SD/pages/13861444).

O catálogo genérico de retornos associa 200/201 a sucesso, 400 a negócio, 401 à autenticação, 422 à formação/obrigatoriedade e 500 a erro de servidor. A correspondência por operação deverá ser conferida; erro de negócio detectado no processamento assíncrono também precisa ser representado na consulta de lote. Não se deve trocar indiscriminadamente todo erro tributário por HTTP 400. [F19](https://jira-tcesc.atlassian.net/wiki/spaces/SD/pages/13870220).

Mensagens e códigos oficiais só serão reproduzidos quando houver evidência. Uma regra exclusiva do simulador terá identificação própria no controle administrativo; não receberá um número de CON inventado.

# 6. Arquitetura de referência

Recomenda-se um único serviço com adaptador HTTP, motor de domínio, armazenamento e executor de tarefas, implementado em Java 21 e Spring Boot 3.5 para aproveitar a plataforma já conhecida pela equipe. Essa escolha não cria dependência de código do integrador ou do serviço de origem. SQLite e diretório local de payloads são suficientes para a primeira versão de uso individual/CI; em Java, pode-se usar acesso SQL explícito para evitar uma camada ORM desnecessária no MVP.

| Componente | Responsabilidade |
|---|---|
| Adaptador compatível | Implementar o contrato TCE selecionado sem parâmetros exclusivos de teste no payload |
| Autenticação local | Vincular token a usuário/UG fictícios e controlar expiração |
| Motor de remessas | Aplicar precondições e coordenar lotes, dados aceitos e finalização |
| Executor assíncrono | Consumir trabalhos persistidos e produzir transições observáveis |
| Validador por perfil | Aplicar somente regras cadastradas e expor a cobertura |
| Armazenamento | Guardar requisições, dados, vínculos, resultados e agenda de tarefas |
| Controle administrativo | Preparar, inspecionar, avançar tempo, injetar falhas e reiniciar cenários |

O controle administrativo deverá usar porta separada ou prefixo reservado, por exemplo `/__simulator`, com chave própria. Os nomes dessa interface são proposta interna. O integrador não dependerá dela para funcionar: o executor de testes a utiliza antes e depois do fluxo normal.

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

O cenário principal requer referências entre os registros efetivos das remessas já finalizadas dos seis assuntos. Atomicidade por registro/lote, visibilidade de remessas ainda em andamento e comportamento de reenvio precisam de casos observados. Não se deve escolher uma transação de banco conveniente e anunciá-la como garantia do TCE.

# 8. Comportamento e processamento

## 8.1 Fluxo nominal proposto

O executor de teste prepara a competência e as credenciais fictícias. Para cada assunto, o integrador abre a remessa, transmite todos os seus lotes e solicita processamento; o simulador passa a executar os trabalhos. O integrador acompanha as consultas e conclui o envio. Repete-se o ciclo nos demais assuntos, na ordem de dependência do cenário. Ao fim, o executor compara os registros esperados com os dados efetivos das remessas no simulador e verifica a conclusão no próprio integrador.

Deverão existir dois modos de execução: automático, com latência configurada, para uso exploratório; e manual, com avanço explícito de tarefas/tempo, para CI. A mesma sequência de entrada, seed e comandos deverá produzir o mesmo resultado lógico. A passagem do tempo real não poderá ser necessária para testar expiração longa.

## 8.2 Situações externas e recuperação

Vocabulário documentado no fluxo assíncrono: `RECEBIDO`, `EM_PROCESSAMENTO`, `PROCESSADO_SUCESSO`, `PROCESSADO_ERRO_NEGOCIO`, `PROCESSADO_ERRO_INTERNO`, `ABORTADO_ERRO_LOTE_ANTERIOR`, `ABORTADO_AGUARDANDO_DESCARTE_PARCIAL` e `ABORTADO`. O cancelamento automático define se a recuperação conserva dados anteriores; o fluxo de descarte deve respeitar dependências. [F11](https://jira-tcesc.atlassian.net/wiki/spaces/SD/pages/759267332).

Para V6, acrescentar `LOTE_DESCARTADO` e preservar as exceções do caso de um assunto. A documentação também distingue lotes mistos, cuja recuperação fica para ampliação futura. Ela prevê timeout de 72 horas, renovado pelas operações indicadas. Os casos incluídos serão transformados em tabelas de decisão antes de codificar. [F04](https://jira-tcesc.atlassian.net/wiki/spaces/SD/pages/1582465028).

Há pontos que exigem fechamento de contrato: a relação entre finalização e lotes descartados/abortados após recuperação, o comportamento de descarte com cancelamento automático desativado e o resultado quando não há lotes elegíveis. O simulador não deve assumir que uma lista vazia significa processamento bem-sucedido.

## 8.3 Retransmissão e resposta perdida

Guardar o hash do payload ajuda a investigar repetição, mas não cria uma garantia oficial de idempotência. O modo compatível deverá reproduzir o comportamento confirmado do TCE para repetição de abertura, envio, processamento e finalização.

Incluir duas falhas diferentes: falha antes de aceitar a operação, sem efeito persistido; e falha depois de aceitar, com resposta perdida. O segundo caso testa se o integrador consulta e reconcilia o estado antes de reenviar, evitando duplicidades. Qualquer política temporária enquanto a semântica oficial estiver desconhecida deverá constar no manifesto como aproximação.

# 9. Validação e fidelidade

| Nível | O que verifica | Aceite no MVP |
|---|---|---|
| Contrato | Tipos, campos, enums, formatos e estruturas do perfil | Obrigatório para as operações e assuntos habilitados |
| Fluxo | Ordem das operações, vínculo de chaves, concorrência e bloqueio de competência | Obrigatório |
| Domínio selecionado | Referências cadastrais, cronologia e regras dos casos do piloto | Lista fechada de regras com origem e testes |
| Catálogo completo | Todas as CONs e exceções anuais | Fora do MVP |

Lista inicial proposta para escolha com a equipe: referência cadastral ausente; chave de negócio repetida; data de movimentação incompatível; baixa anterior ao crédito; totalizadores divergentes; tentativa em competência bloqueada. Cada regra deve indicar a etapa de aplicação: recepção, processamento ou finalização. Estas são candidatas, não uma afirmação de que todas correspondem a CONs já analisadas.

Para os assuntos sem validação profunda, o manifesto separará “formato compatível” de “regra de negócio implementada”. Campos extras e desconhecidos seguirão a política confirmada no contrato; não serão descartados silenciosamente.

O manifesto de cobertura deverá conter: operação, assunto, regra, versão de referência, evidência, teste associado e situação — implementada, aproximada, não implementada ou pendente. Ele acompanhará todo relatório de execução.

# 10. Cenário fictício mínimo e prova do fluxo completo

Propor uma UG fictícia A, uma UG B para isolamento, competência 202601 e relógio fixo. Janeiro permite incluir saldo inicial no cenário principal, cobrindo os seis assuntos confirmados. Uma variante em fevereiro testa continuidade de estado; outra cobre mês sem movimento. O leiaute exige atenção específica a saldo inicial e permite pacote sem movimentação, observadas suas condições. [F09](https://jira-tcesc.atlassian.net/wiki/spaces/SD/pages/2154923885).

Conjunto lógico de exemplo, a adaptar aos campos e referências efetivamente usados:

| Dado sintético | Quantidade | Relação pretendida |
|---|---|---|
| Contribuintes | 3 | Identificadores estáveis, nomes fictícios |
| Créditos | 4 | Vínculos e datas definidos no manifesto de entrada |
| Arrecadações | 2 | Vinculadas aos créditos escolhidos |
| Baixa | 1 | Caso nominal ou data inválida em variante negativa |
| Saldo inicial | 1 | Saldo de abertura com referência de exercício anterior conforme DTO |
| Início do prazo prescricional | 1 | Vinculado ao crédito compatível com as pré-condições do perfil |

Essas quantidades são propostas de teste, não prescrições do TCE. A equipe fornecerá os atributos e o estado anterior que tornem o conjunto válido no perfil adotado, inclusive referências para saldo inicial e prazo prescricional. A preparação pode semear pré-condições históricas pelo controle administrativo; elas serão distinguidas dos registros realmente enviados pelo integrador. Não usar cópias reais de contribuintes para preencher lacunas.

Roteiro de aceite ponta a ponta:

1. Preparar esse conjunto no ambiente de origem de teste, por procedimento externo ao simulador, e gerar um manifesto independente com chaves e contagens esperadas.
2. Carregar somente a configuração do destino: credenciais fictícias, UG, competência aberta e plano de processamento.
3. Configurar o integrador real para apontar ao simulador e acionar seu fluxo normal de obtenção, transformação, agrupamento e envio, com remessas separadas para os seis assuntos.
4. Em pelo menos uma remessa, fracionar o assunto em dois ou mais lotes. Usar tamanho de lote reduzido no integrador se ele permitir. Verificar que o início de processamento remoto ocorre após o envio de todos esses lotes.
5. Antes do processamento, provar que os payloads foram conservados e ainda não equivalem aos dados efetivos finais.
6. Executar o processamento, acompanhar consultas e finalizar pelo integrador. Repetir nos demais assuntos, conservando referências válidas entre remessas concluídas.
7. Comparar o manifesto de entrada com a união dos registros efetivos por chave de negócio; conferir conteúdo selecionado, contagens, duplicatas e perdas. Gerar o esperado sem reutilizar o algoritmo de montagem de lotes que está sendo testado.
8. Conferir o estado de conclusão no integrador e exportar evidências. Repetir após reset com o mesmo resultado lógico.

Sem uma maneira de preparar entrada conhecida no serviço independente, ainda é possível testar a interface de destino; permanece limitada a comprovação da obtenção e transformação anterior aos lotes. Essa dependência deve aparecer no aceite, sem acoplar o simulador à origem.

# 11. Matriz mínima de cenários e aceite

| ID | Exercício | Resultado observável esperado |
|---|---|---|
| T01 | Seis assuntos em remessas separadas, uma delas com vários lotes | Referências entre remessas, conteúdo e contagens corretos; integrador conclui |
| T02 | Receber e consultar antes do disparo | Não avançar processamento pela simples consulta |
| T03 | Finalizar durante processamento | Resposta de impedimento compatível; sem conclusão prematura |
| T04 | Regra de domínio em lote intermediário | Erro com referência ao registro e efeitos coerentes nos demais lotes |
| T05 | Cancelamento automático ativado/omitido | Efeitos de cancelamento conferidos nos dados, além dos status |
| T06 | Recuperação com cancelamento desativado | Corrigir e reenviar sem perder dados anteriores válidos |
| T07 | Corrigir e reprocessar remessa de um assunto | Conservar efeitos válidos e aplicar somente os efeitos esperados do reenvio |
| T08 | Descartar lote/assunto e cancelar parcialmente | Cobrir o uso real do integrador, elegibilidade e efeitos rastreáveis |
| T09 | Competência ratificada | Impedir operações cobertas; reset permite repetir o teste |
| T10 | UG/token/chave incompatíveis | Negar acesso e evitar alteração de outro cenário |
| T11 | Token expira no meio do fluxo | Integrador renova ou apresenta falha controlada, sem perder evidência |
| T12 | Demora e resposta perdida após aceitação | Reconciliação observável; repetição segue política confirmada |
| T13 | Reinício e reset com trabalhos pendentes | Reinício conserva estado; reset impede trabalho antigo no novo cenário |
| T14 | Duas aberturas simultâneas | Resultado consistente para a mesma UG/competência |
| T15 | Processar sem lotes elegíveis e repetir disparo ativo | Respostas contratadas para chamadas inválidas, sem sucesso inventado |
| T16 | Pacote sem movimento e janeiro | Casos distintos, validados conforme pré-condições do perfil |
| T17 | Sequência de remessas e limite mensal | Verificar fronteira 64/65; política de contagem deve ser confirmada |
| T18 | Campo/assunto sem suporte | Não produzir aprovação silenciosa no modo estrito |
| T19 | Expiração longa com relógio virtual | Resultado reprodutível sem espera de 72 horas reais |

T01 só passa com o integrador real. Os demais podem começar como testes do simulador e ganhar execução pelo integrador conforme suas capacidades. Os resultados ainda pendentes de contrato deverão ser definidos em E0/E2 antes de afirmar compatibilidade.

# 12. Portabilidade, operação e evidências

Docker é requisito confirmado. Entrega operacional proposta: imagem OCI/Docker versionada com runtime Java 21; arquivo Compose para instância única; configuração de exemplo sem segredos; volume local; instruções de início/reset; verificação de saúde; comando de execução da suíte; manifesto de compatibilidade. A imagem deverá executar em Linux no CI. Suporte simultâneo a amd64/arm64 será confirmado com os equipamentos da equipe.

O ambiente deverá funcionar sem acesso ao TCE após baixar os artefatos. Não haverá modo proxy com encaminhamento ao destino real. Cada execução de CI poderá usar instância, porta e volume próprios, sem exigir um header de teste adicional nas chamadas do integrador.

Para uso compartilhado, isolamento entre equipes e autenticação administrativa passam a ser requisitos de operação, com validação específica. O padrão inicial é instância por execução, mais simples de reproduzir e limpar.

Cada relatório deverá conter: versão da imagem e contrato; configuração/seed; origem fictícia dos dados; sequência de operações; IDs de correlação; estados observados; contagens recebidas/aceitas/removidas; divergências; regras executadas e limitações. Logs comuns não devem expor senha, token ou dados pessoais. O armazenamento detalhado de payload fica restrito ao cenário de teste e tem limpeza junto com a execução.

Metas de engenharia propostas, a calibrar com o hardware real: inicialização em até 30 segundos; suíte funcional pequena em até dois minutos; teste opcional de 50 mil registros por lote sem perda. São metas do projeto, não promessas de desempenho ou limites do TCE. Medir uso de memória e duração e fixar orçamento na etapa do piloto.

# 13. Plano de desenvolvimento e entregáveis

| Etapa | Entrega | Condição de saída | Ordem de grandeza proposta |
|---|---|---|---|
| E0 — Fechar contrato e piloto | Inventário do cliente, OpenAPI ou contrato derivado, exemplos, assuntos e decisões | Rotas/métodos/schemas e resultados dos casos prioritários definidos | 2–4 dias úteis |
| E1 — Recepção e estado | Autenticação local, abertura, lotes persistidos, consultas e evidências | Receber payload real do cliente e reabrir a instância sem perdê-lo | 3–5 dias úteis |
| E2 — Ciclo e recuperação | Executor, finalização, cancelamento/descarte, relógio e regras selecionadas | Tabelas de decisão e testes dos efeitos sobre os dados | 5–8 dias úteis |
| E3 — Piloto ponta a ponta | Execução do integrador com entrada conhecida e cenários de falha | T01 e casos prioritários demonstrados com evidências | 3–5 dias úteis |
| E4 — Empacotar e documentar | Imagem, Compose, CI, guia, cobertura e relatório de aceite | Outra pessoa reproduz instalação, teste e reset | 2–3 dias úteis |

Estimativa preliminar total: 15–25 dias úteis de trabalho técnico, para uma pessoa com apoio de QA e acesso ao responsável pelo integrador, após disponibilidade dos insumos. Trata-se de faixa de planejamento baseada nas entregas propostas, sem análise do código do cliente. Prazo de calendário dependerá da equipe e dos tempos de resposta.

Reestimar ao concluir E0. NFS-e, compatibilidade legada, autenticação adicional, interface visual, grandes volumes e regras tributárias extensas são itens que podem ampliar materialmente essa faixa. Bloqueios de contrato não devem ser consumidos como implementação baseada em suposição.

Responsabilidades sugeridas: desenvolvimento cuida do simulador e contratos; responsável pelo integrador informa chamadas e comportamento do cliente; especialista tributário seleciona as regras; QA define o esperado independente e verifica o fluxo; responsável pela origem viabiliza o conjunto sintético sem transferir essa responsabilidade ao simulador.

# 14. Critérios de conclusão do MVP

1. O integrador aponta ao simulador por configuração, sem alterar sua lógica de agrupamento e envio. Eventual ajuste necessário para separar URL e credenciais é identificado e concluído no cliente.
2. A pessoa responsável consegue instalar, carregar cenário, executar, inspecionar e reiniciar a partir do guia, em máquina/CI definidos no piloto.
3. O fluxo completo prova conteúdo, vínculos e contagens, incluindo múltiplos lotes; não se limita a verificar strings de status.
4. A matriz mínima aplicável ao contrato é executada, com os casos pendentes encerrados ou explicitamente retirados do escopo acordado antes do aceite.
5. Falhas, recuperação e reset são reprodutíveis, sem dependência de calendário municipal ou disponibilidade do TCE.
6. Nenhuma execução aprovada no modo estrito contém operação ou assunto ignorado silenciosamente.
7. Pacote de contrato, manifesto de cobertura, testes e evidências identificam as aproximações. Conformidade com o subconjunto não é apresentada como homologação oficial.

A homologação oficial continuará útil para diferenças de contrato e validações não cobertas, quando houver condições disponíveis. O benefício do simulador é permitir regressão frequente e investigação controlada entre essas oportunidades.

# 15. Decisões pendentes e perguntas para implementação

Todas as perguntas encaminhadas durante a elaboração foram respondidas e incorporadas: plataforma, troca de configuração, Docker, seis assuntos, cancelamento parcial e processamento remoto após todos os lotes de uma remessa de um assunto. Os itens abaixo são detalhamentos para a implementação e não impedem a entrega deste planejamento.

| ID | Informação necessária | Premissa de planejamento enquanto pendente | Consequência |
|---|---|---|---|
| D01 | Há dependências adicionais dos seis assuntos que exijam ampliar o piloto? | Seis assuntos confirmados; NFS-e fora do recorte | Verificar dependências sem ampliar o produto silenciosamente |
| D02 | Confirmar nomenclatura Spring Boot 3.5, sistemas operacionais e arquiteturas de máquina | Java 21/Spring 3.5, URL/credenciais configuráveis e Docker já confirmados | Fecha detalhes do empacotamento |
| D03 | Completar contrato de DTOs, respostas e erros; selecionar falhas prioritárias | DTOs seguem o Swagger informado; usuário poderá prover insumos futuramente | Necessário para afirmar compatibilidade exata |
| D04 | Como preparar entrada conhecida no ambiente do serviço independente? | Procedimento externo ao simulador | Necessário para T01 ponta a ponta |
| D05 | Qual exercício/versão é efetivamente enviado? | Perfil 2026, referências 6.06/6.9 | Evita mistura com 2025/2027 |
| D06 | O cliente depende também de webhook ou relatórios? | Consulta de lote confirmada; cancelamento de remessa concluída fora do recorte | Dependências adicionais entram antes de E1 |
| D07 | Qual valor de cancelamento automático o cliente envia: SIM, NAO ou omitido? | Um assunto por remessa já confirmado; lotes mistos fora do MVP | Define o comportamento padrão e a prioridade de recuperação |
| D08 | Precisa de uso simultâneo por equipes? Quais volumes e sistemas operacionais? | Instância por execução e persistência local | Pode mudar banco e operação |
| D09 | Quais regras de domínio dos seis assuntos são essenciais no piloto? | Conjunto mínimo da seção 9; abertura e prazo prescricional incluídos | Fecha compromisso de fidelidade |
| D10 | Qual é a semântica observada de repetição, descarte, lotes vazios e contagem mensal? | Lacunas explícitas, sem respostas inventadas | Fecha as tabelas de decisão |
| D11 | Qual rota e payload correspondem ao cancelamento parcial do cliente? | Ordem de processamento confirmada; falta somente mapear os detalhes do cancelamento às operações oficiais | Fecha o contrato de cancelamento |

Não são necessários dados pessoais ou credenciais reais para responder a essas questões. Exemplos de payload devem preservar estrutura, tipos e relações relevantes, substituindo seus conteúdos sensíveis.

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

