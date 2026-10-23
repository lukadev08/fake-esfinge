# PRD — Simulador e-Sfinge Tributário

Data: 23/10/2026. Status: **rascunho local, não publicado**.
Pontos de teste confirmados pelo solicitante em 23/10/2026 conforme `to-prd`.
Revisão após pergunta/resposta e aprovação do plano de consolidação, sem publicação.
Este PRD especifica o produto e distingue a entrega inicial do MVP completo;
registra E0 concluído documentalmente no recorte inicial, com preparo da referência
e conferência independente local em E3 por revisão autorizada de compliance.

## Problema

A equipe precisa testar repetidamente o integrador tributário com os lotes que
ele realmente produz, inclusive quando competências municipais estão bloqueadas
no ambiente do TCE. Os ensaios precisam revelar perdas, duplicações, alterações
de conteúdo e falhas de recuperação, sem depender da disponibilidade do destino
ou encaminhar dados a ele.

Uma resposta HTTP de sucesso, isoladamente, não demonstra conclusão assíncrona,
integridade dos lotes ou correspondência com a entrada. Um fake que sempre aprova
também mascara operações e regras sem cobertura. A equipe precisa observar os
dados recebidos, processados e efetivos, além dos estados e respostas do fluxo.

O contrato derivado inicial está definido documentalmente, a partir de relatos,
manuais e escolhas locais. Não há captura independente nem software executado;
ausência dessa prova deve continuar visível para não produzir falsa declaração
de compatibilidade. E0 define o roteiro de comparação; sua execução e aceite continuam pendentes de E3.

## Solução

Fornecer um simulador HTTP local, com estado persistente, executado em Docker e
configurável como destino do integrador. O integrador continua obtendo,
transformando e dividindo os dados; o simulador recebe os payloads, aplica o
subconjunto contratado e permite inspecionar conteúdo, contagens e recuperação.

O perfil selecionado é **2026 / V6 / leiaute 6.06**, confirmado pelo usuário,
sem comprovação independente do cliente. O manual desse perfil é a referência;
diferenças necessárias ao cliente são aproximações declaradas. CONs 6.9 seguem
como referência proposta, sem versão ou regras concretas confirmadas.

O fluxo relatado abre um envio, transmite todos os lotes de um assunto, dispara
processamento e consulta até terminar; repete por assunto sob a mesma
`chavePacote` e finaliza ao término. Cada lote contém um assunto. A correspondência
remessa/envio/chave e os ciclos sucessivos foram definidos por relato: uma
UG/competência por envio, com chave retornada pela abertura, sem geração local.
A prova de execução pertence ao piloto futuro.

### Entrega inicial e evolução

| Camada | Compromisso de produto | Limite de aceite |
|---|---|---|
| Entrega inicial aprovada | Recepção dos seis assuntos; validação estrutural, sequência, conteúdo e contagens; estados, persistência, falhas e recuperação definidos | Não comprova regras tributárias ou integridade de negócio entre assuntos |
| Piloto inicial | Cliente/alias 14, 202609, cópia local sem alterações e esperado SQL independente a preparar | Pode não conter saldo inicial ou prazo prescricional; cobertura ausente permanece pendente |
| MVP completo do planejamento | Fluxo dos seis assuntos, múltiplos lotes, matriz aplicável e regras de domínio selecionadas e evidenciadas | Depende de novos fechamentos de contrato e seleção das regras adiadas; não é homologação oficial |

Os seis assuntos são cadastro de contribuinte, lançamento de crédito tributário,
diário de arrecadação, baixa de crédito tributário, saldo inicial e início do
prazo prescricional. Preservar suas referências desde a recepção; validar os
vínculos de negócio e cálculos tributários fica para evolução.

## Histórias de usuário

1. Como responsável pelo integrador, quero configurar URL e credenciais do simulador, para executar o fluxo normal contra um destino controlado.
2. Como responsável pelo integrador, quero enviar os lotes produzidos pelo cliente real, para verificar sua montagem sem duplicar essa lógica no destino.
3. Como responsável pelo contrato, quero identificar exercício, API e leiaute em cada execução, para evitar mistura de versões.
4. Como responsável pelo contrato, quero rastrear métodos, schemas, respostas e exemplos até sua fonte, para distinguir evidência oficial de contrato derivado.
5. Como responsável pelo contrato, quero registrar divergências entre manual e cliente como aproximações, para revisar compatibilidade sem ocultar diferenças.
6. Como operador de testes, quero autenticar usuários e UGs fictícios, para testar acesso sem credenciais reais.
7. Como operador de testes, quero controlar expiração e invalidade de tokens, para observar a reação do integrador durante o fluxo.
8. Como operador de testes, quero negar acesso por token, UG ou chave incompatíveis, para impedir leitura e alteração de outra execução.
9. Como responsável pelo integrador, quero abrir um envio e receber sua chave conforme o contrato, para vincular os lotes e acompanhar o ciclo.
10. Como responsável pelo integrador, quero reutilizar a chave nos ciclos dos assuntos, para reproduzir a sequência relatada pelo cliente.
11. Como responsável pelo integrador, quero transmitir múltiplos lotes do mesmo assunto antes do disparo, para testar fracionamento sem perdas ou duplicações.
12. Como operador de testes, quero receber cadastro de contribuinte, para conferir estrutura e conteúdo desse assunto.
13. Como operador de testes, quero receber lançamento de crédito tributário, para conferir estrutura e conteúdo desse assunto.
14. Como operador de testes, quero receber diário de arrecadação, para conferir estrutura e conteúdo desse assunto.
15. Como operador de testes, quero receber baixa de crédito tributário, para conferir estrutura e conteúdo desse assunto.
16. Como operador de testes, quero receber saldo inicial, para incluir cenários adicionais de abertura do exercício.
17. Como operador de testes, quero receber início do prazo prescricional, para incluir esse assunto quando houver dados no cenário.
18. Como responsável pelo contrato, quero rejeitar campos obrigatórios ausentes ou nulos e tipos ou formatos inválidos, para detectar payloads incompatíveis com os schemas fechados.
19. Como responsável pelo contrato, quero explicitar a política de campos opcionais e desconhecidos, para evitar descarte silencioso ou aceitação sem evidência.
20. Como operador de testes, quero inspecionar os bytes originais, identificadores e valores decimais, para detectar alterações introduzidas na recepção.
21. Como responsável pelo integrador, quero receber confirmação somente após persistência, para não tratar um lote perdido como aceito.
22. Como responsável pelo integrador, quero disparar processamento explicitamente, para reproduzir a separação entre recepção e processamento da V6.
23. Como operador de testes, quero consultar repetidamente sem avançar processamento, para observar o estado sem alterar o resultado.
24. Como operador de testes, quero execução automática para exploração e avanço manual para testes, para reproduzir resultados sem esperas reais.
25. Como responsável pelo integrador, quero finalizar somente quando as pré-condições contratadas forem atendidas, para evitar conclusão prematura.
26. Como operador de testes, quero distinguir recebidos, processados, efetivos e históricos, para explicar contagens e efeitos de cada recuperação.
27. Como operador de testes, quero injetar erro assíncrono em lote intermediário, para verificar descarte e aborto dos seguintes ainda não processados do mesmo assunto.
28. Como responsável pelo integrador, quero preservar lotes anteriores válidos até descarte explícito, para recuperar um assunto sem perder dados indevidamente.
29. Como responsável pelo integrador, quero descartar um assunto no envio aberto mantendo chave e outros assuntos, para corrigir e reenviar apenas seu conteúdo.
30. Como operador de testes, quero manter controles históricos após descarte, para rastrear a recuperação sem contar esses controles como duplicatas ativas.
31. Como responsável pelo integrador, quero cancelar integralmente o envio aberto e recomeçar com nova chave, para testar o reenvio completo preservando os dados no cliente.
32. Como operador de testes, quero simular resposta perdida após persistência sem processar o lote, para reproduzir aceitação sem confirmação de transporte.
33. Como responsável pelo integrador, quero observar o erro de envio aberto na nova tentativa após resposta perdida, para executar descarte do assunto e reenvio sob a mesma chave.
34. Como operador de testes, quero distinguir falha anterior à aceitação de resposta perdida após commit, para conferir seus efeitos persistidos.
35. Como operador de testes, quero bloquear uma competência pelo controle administrativo, para reproduzir os impedimentos definidos sem criar uma API fictícia de ratificação.
36. Como operador de testes, quero reiniciar o processo preservando lotes e trabalhos, para verificar retomada sem reaplicar efeitos confirmados.
37. Como operador de testes, quero resetar a execução invalidando trabalhos antigos, para impedir contaminação do cenário novo.
38. Como operador de testes, quero repetir entradas, seed e comandos com o mesmo resultado lógico, para reproduzir falhas e regressões.
39. Como operador de testes, quero controlar o tempo de expiração, para testar durações longas sem aguardar horas reais.
40. Como operador de testes, quero verificar aberturas concorrentes e disparos inválidos com resultados previamente definidos, para detectar inconsistências de sequência.
41. Como solicitante do piloto, quero congelar um cliente e competência na base local e gerar o esperado por SQL antes do envio, para comparar resultados de forma independente.
42. Como solicitante do piloto, quero comparar identificadores, campos e contagens, para reconciliar perdas, duplicações e descartes além dos status HTTP.
43. Como solicitante do piloto, quero demonstrar ao menos dois lotes de um mesmo assunto e a conclusão no integrador, para comprovar fracionamento e acompanhamento.
44. Como responsável pelo aceite, quero registrar assuntos e regras ausentes no piloto, para não confundir cobertura parcial com T01 completo.
45. Como responsável pelo aceite, quero consultar um manifesto por operação, assunto e regra, para identificar o que foi implementado, aproximado ou permanece pendente.
46. Como responsável pelo aceite, quero falha identificável para operação ou assunto sem suporte no modo estrito, para impedir aprovação silenciosa.
47. Como operador de CI, quero uma instância e volume por execução, para reproduzir e limpar os testes com isolamento.
48. Como operador de testes, quero instalar, executar, inspecionar e resetar por instruções verificadas, para reproduzir o ambiente sem conhecimento interno.
49. Como responsável pelos dados, quero manter payloads originais e resultados SQL apenas no ambiente local controlado, para não transferir dados de produção a serviços externos.
50. Como responsável pelos dados, quero logs e relatórios compartilháveis sanitizados, para investigar falhas sem divulgar senhas, tokens ou dados pessoais.
51. Como operador de testes, quero executar sem acesso ao TCE após baixar os artefatos, para ensaiar sem chamadas ao destino real.
52. Como responsável pelo aceite do MVP completo, quero cenários adicionais de janeiro, mês sem movimento e sequência mensal com expectativas fechadas, para cobrir os casos não demonstrados pelo primeiro piloto.

## Decisões de implementação

As escolhas abaixo são documentais e orientam implementação futura. Nenhuma
capacidade está implementada nesta revisão.

### Arquitetura adotada

- Monólito modular com arquitetura hexagonal, organizado por autenticação,
  remessas/lotes, processamento, cenários e evidências. São capacidades de um
  único contexto, sem serviços distribuídos.
- Java 21, Spring Boot 3.5, Spring MVC e Maven em um módulo. Jackson e Bean
  Validation ficam na fronteira HTTP; JUnit 5 é a base dos testes. Versões exatas
  serão fixadas quando o build existir. O integrador foi confirmado por relato
  em Spring Boot 3.5.10, separadamente da escolha corretiva para o simulador.
- Domínio e aplicação usam Java puro e portas pequenas. Não dependem de Spring,
  JDBC, Jackson ou tipos HTTP. Adaptadores mapeiam DTOs, resultados e persistência;
  configuração compõe os objetos. Capacidades usam portas públicas, sem acessar
  adaptadores ou tabelas de outra capacidade diretamente.
- SQLite com SQLite JDBC, Spring JDBC e SQL explícito parametrizado, sem ORM.
  Bytes de payload, metadados, resultados, eventos e fila ficam na mesma base.
  Essa decisão atualiza a proposta anterior de diretório separado de payloads.
- Imagem Docker Linux amd64 e volume local, uma instância por execução local/CI.
  arm64 e uso compartilhado ficam fora da operação inicial.
- Não criar camadas, interfaces ou infraestrutura sem uma fronteira concreta.

### Contrato e fronteiras

- O simulador começa no HTTP do destino: não consulta a origem, não extrai dados
  nem monta lotes e não compartilha dependências internas com o integrador.
- Cada operação habilitada exige método, caminho, parâmetros/localização, headers,
  content type, entrada, sucesso, erro, datas, tipos, opcionais e exemplos
  sanitizados, com origem, versão, data e hash. Ausência de OpenAPI oficial exige
  identificação explícita de contrato derivado.
- Contrato inicial: login POST com credenciais nos headers e UG/descrições na
  query; resposta `chave`, enviada nas demais chamadas pelo header `auth_token`.
  Token dura uma hora configurável; 401 provoca novo login e repetição sem efeitos
  anteriores. Não confundir essa escolha com duração oficial.
- Abertura GET com `anoMes` inteiro e `cancelamentoAutomatico=NAO` na query;
  envio e disparo POST com chave na query; consulta GET com número do lote no
  caminho. Descarte por assunto POST com chave/assunto na query e corpo vazio.
- Cada lote contém uma lista reconhecida não vazia, omitindo outras listas.
  Tipos seguem DTOs com ajustes relatados: `numeroParcelas` no plural e schema
  derivado dos quatro campos de prazo prescricional. Campos imobiliários e
  transferência não enviados ficam fora do schema inicial. Não impor o limite
  de 10.000 do particionamento do cliente como limite do destino.
- O cliente lê `situacao` no JSON; `situacao_lote` é interno. RECEBIDO e
  EM_PROCESSAMENTO mantêm consulta; todos os lotes do assunto devem atingir
  PROCESSADO_SUCESSO antes do próximo. Erro/aborto interrompe com diagnóstico.
- Finalização POST leva chave e 17 totalizadores string no corpo; assuntos
  ausentes têm `"0"`. Comparar com registros efetivos após recuperação,
  preservando multiplicidades. HTTP 200 é suficiente para conclusão no cliente.
- Cancelamento integral em serviços gerais usa chave na query e invalida o envio
  aberto. Rota explícita de descarte de lote e consulta agregada ficam fora do
  contrato inicial; o descarte automático em erro continua incluído.
- `cancelamentoAutomatico=NAO` é texto; omissão/SIM/outros valores recebem 422
  de cobertura, fora do aceite inicial. Sem movimento e limite mensal T17 ficam
  adiados expressamente, sem retirar esses objetivos do MVP futuro.
- Obrigatórios ausentes/nulos são rejeitados; opcionais admitem ambos. Extras
  são rejeitados no modo estrito. Obrigatoriedade condicional, enums externos,
  defaults fiscais e relações continuam não implementados.
- Não inventar algoritmo externo de chave, códigos de CON, mensagens oficiais
  ou idempotência. Hash serve à rastreabilidade. Erros síncronos usam `codigo`
  próprio `SIM_...`, `mensagem`, `correlacao`; assíncronos usam `situacao` e
  `retornoErros.erros[]` em consulta HTTP 200. Tabela derivada: 400 requisição,
  401 autenticação, 403 acesso/competência, 404 recurso inacessível/inexistente,
  409 sequência/estado/totais, 422 estrutura/cobertura e 500 falha interna.

- Controle administrativo sob `/__simulator`, com chave própria, separado da
  autenticação compatível. Configura cenário, falhas, relógio, reset e evidências
  por portas de entrada. Parâmetros de teste não entram no payload do integrador.
- Token válido não concede acesso a outra UG ou cenário. Credenciais e tokens
  são fictícios e exclusivos do simulador. Erros técnicos não expõem SQL,
  stack trace ou segredos; o mapeamento HTTP segue o contrato por operação.

### Estado, persistência e execução

- Preservar vínculos lote/remessa/UG/cenário, ordem de chegada e geração de reset.
  Chaves são opacas e identificadores conservam tipos e zeros.
- Receber não inicia processamento. Consultar não o inicia nem avança. O disparo
  explícito torna elegíveis trabalhos persistidos.
- Persistir payload, vínculos, transição, evento e trabalho pertinente em unidade
  atômica; responder confirmação após commit. Chaves estrangeiras ficam ativas
  em cada conexão. Falha de persistência nunca vira sucesso.
- Aplicação define unidade transacional por porta; adaptador JDBC executa-a na
  mesma conexão. Essa atomicidade interna não é garantia oficial do TCE sobre
  aceitação por registro ou lote.
- Um executor por instância usa a mesma porta para avanço automático e manual;
  serializa acionamentos e ordena trabalhos por instante e sequência persistida.
  Tempo de negócio vem de relógio substituível, sem espera real no modo manual.
- Reinício conserva estado e retoma trabalho não concluído sem reaplicar efeitos
  confirmados. Reset muda geração atomicamente; gravação de efeitos revalida
  geração e elegibilidade na transação. Trabalho antigo não altera cenário novo.
- Recebidos, processados, efetivos e históricos são distinguíveis. Contagens
  derivam do conteúdo e dos efeitos; sucesso estrutural não atesta validade fiscal.
- Dinheiro usa `BigDecimal` e texto decimal no SQLite, sem ponto flutuante ou
  agregação que converta valores. Escala e arredondamento dependem do contrato.
- Evoluir schema com SQL versionado e versão aplicada. Reinício não recria base
  silenciosamente; incompatibilidade produz erro explícito.

### Recuperação selecionada

| Situação | Comportamento definido para o simulador | Classificação |
|---|---|---|
| Erro assíncrono intermediário | Lote com erro descartado com diagnóstico; seguintes não processados do mesmo assunto abortados; anteriores válidos permanecem até descarte | Escolha derivada; sem CON inventada |
| Descarte de assunto | Remove recebidos/processados/efetivos do assunto no envio aberto; mantém chave, outros assuntos e controles históricos; durante processamento retorna 409 | Exceção para RECEBIDO após resposta perdida diverge do manual |
| Finalização após recuperação | Exige lotes ativos em sucesso, ao menos um ativo e totais efetivos corretos; histórico não bloqueia | Derivada; rejeição 409 |
| Cancelamento integral | Remove dados do envio atual, invalida trabalhos e exige nova chave/reenvio; preserva histórico e envios anteriores | Alcance aprovado; sem alterar dados no integrador |
| Resposta perdida | Commit conserva lote em RECEBIDO, perde somente resposta, sem processar; nova tentativa retorna 409 `SIM_ENVIO_ABERTO_SEM_PROCESSAMENTO` | Marca do cenário, sem deduplicação por hash e sem bloquear lotes normais |
| Repetições/concorrência | Segunda abertura com envio aberto, disparo ativo/sem elegíveis, repetição de finalizar/cancelar e mutação em chave encerrada retornam 409 sem efeitos | Sem promessa de idempotência |
| Expiração | 72 horas de inatividade; operações bem-sucedidas sobre chave, inclusive consulta, renovam atividade; expirar encerra chave/invalida trabalhos e conserva histórico | Política derivada; consulta não avança processamento |

Falhas prioritárias: token inválido/expirado, payload inválido, erro assíncrono
controlado e resposta perdida após recebimento. Erro tributário injetado não
implementa regra fiscal. Demora e HTTP 500 permanecem no planejamento geral,
sem prioridade inicial nem retirada automática da matriz.

### Evidências e proteção dos dados

- Manifesto por operação, assunto, regra, versão, evidência, teste e situação:
  implementada, aproximada, não implementada ou pendente. Hoje tudo está não
  implementado; definições e aproximações são apenas planejadas.
- Relatório identifica imagem/contrato, seed/configuração, classificação da origem,
  sequência, correlação, estados, contagens, divergências, regras e limitações.
- Piloto admite dados originais de produção somente na base local de testes e
  ambiente controlado informado. Payloads e SQL ficam fora do Git e de serviços
  externos, em volume restrito, com limpeza vinculada à execução.
- Exemplos e evidências compartilhados são fictícios ou sanitizados. Logs comuns
  não contêm senha, token ou dados pessoais. O serviço funciona sem encaminhar
  chamadas ao TCE.

## Decisões de teste

### Pontos de teste confirmados em 23/10/2026

1. **Fronteira HTTP compatível:** exercitar requisições e observar respostas,
   estados, conteúdo e contagens pelas interfaces do produto. É o ponto principal
   de teste do comportamento contratado com o integrador.
2. **Controle administrativo separado:** preparar cenário, injetar falhas,
   avançar tarefas/tempo e consultar evidências, sem parâmetros exclusivos no
   payload compatível. Consultas não avançam processamento; renovação da
   atividade segue a política derivada de expiração.
3. **Integrador real e esperado independente:** executar obtenção, montagem,
   envio, acompanhamento e conclusão contra o simulador; comparar conteúdo e
   contagens com SQL anterior ao envio e independente da montagem de lotes.
4. **Portas do núcleo e adaptadores reais:** complementar os testes externos com
   invariantes em Java puro, casos de uso com relógio controlado e SQLite real
   temporário para atomicidade, reinício e reset. Esses testes não substituem
   o aceite pelo integrador.

Esses pontos derivam das fronteiras já documentadas. Ainda não existem portas
implementadas, suíte, fixtures executáveis, build ou comandos de validação.
O precedente disponível é documental: invariantes, tabelas de recuperação,
matriz T01–T19 e estratégia arquitetural. Não há testes existentes a reutilizar.

### Qualidade dos testes e níveis

- Testar comportamento observável, pré-condições e efeitos sobre dados; evitar
  assertivas sobre organização interna, quantidade de chamadas ou detalhes privados.
- Em cada fatia, escrever teste comportamental que falha pelo motivo esperado,
  implementar o mínimo e refatorar com testes verdes, respeitando os pontos
  de verificação aprovados na especificação.
- Domínio: transições, invariantes e recuperação sem Spring. Aplicação: casos de
  uso pelas portas e relógio controlado; memória não comprova transação real.
- Persistência: SQLite real temporário para bytes, decimal exato, vínculos,
  commit/rollback, contenção, reinício e geração de reset. Interromper antes e
  depois do commit para conferir retomada e ausência de reaplicação de efeitos.
- HTTP: schemas, headers, autenticação, rejeição estrutural, isolamento,
  respostas e perda de resposta após commit. Casos pendentes precisam de esperado
  definido antes de serem contados como compatibilidade.
- Arquitetura: verificar dependências de classes, ausência de frameworks no
  núcleo, direção domínio/aplicação/portas/adaptadores e ciclos entre capacidades;
  não limitar a verificação a imports textuais.
- Operação: executar imagem em Linux/CI e verificar persistência, configuração,
  saúde, reset e reprodução por outra pessoa, após existirem artefatos e comandos.

### Rastreabilidade da matriz mínima

| Cenário | Verificação e condição de aceite |
|---|---|
| T01 | Integrador real, seis assuntos, múltiplos lotes em ao menos um assunto, conteúdo/contagens e referências aplicáveis; conclusão no cliente. Piloto inicial contribui parcialmente; validação de negócio adiada não pode ser contada como concluída |
| T02 | Receber/consultar antes do disparo não avança processamento |
| T03 | Finalização durante processamento é impedida conforme resposta contratada |
| T04 | Erro assíncrono intermediário identifica registro e efeitos; injeção não comprova regra tributária |
| T05 | SIM/omitido/outros: 422 de cobertura; efeitos automáticos fora do aceite inicial |
| T06 | Recuperação selecionada com cancelamento desativado conserva anteriores válidos até descarte explícito |
| T07 | Descarte/reenvio de assunto preserva chave e demais assuntos |
| T08 | Descarte automático/por assunto e cancelamento integral com alcance definido; rejeitar descarte durante processamento; rota explícita de lote fora do inicial |
| T09 | Competência bloqueada impede operações cobertas; reset permite repetir |
| T10 | UG/token/chave incompatíveis não leem nem alteram outro contexto |
| T11 | 401 antes de efeitos, novo login e repetição da chamada; token padrão de uma hora configurável |
| T12 | Falhas antes da aceitação e resposta perdida após commit têm efeitos distintos; recuperação selecionada é observável; demora segue matriz geral |
| T13 | Reinício preserva dados/trabalhos; reset invalida geração anterior, inclusive trabalho concorrente |
| T14 | Uma abertura aceita na UG/competência, demais 409 sem efeitos |
| T15 | Ausência de elegíveis e disparo repetido/ativo retornam 409 sem efeitos |
| T16 | Janeiro complementar; sem movimento adiado, sem finalização vazia bem-sucedida |
| T17 | Limite mensal 64/65 adiado expressamente; não impor cota no recorte inicial |
| T18 | Campo, operação ou assunto sem suporte não recebe aprovação silenciosa no modo estrito |
| T19 | 72 horas virtuais de inatividade, renovadas por operações bem-sucedidas inclusive consulta, sem avanço de processamento |

Acrescentar verificações de bytes originais, identificadores com zeros, dinheiro
decimal exato e vínculos técnicos lote/remessa/UG/cenário. Expiração do envio
(72 horas de inatividade) e duração do token (uma hora padrão) são distintas;
ambas usam relógio controlado, com política derivada declarada.

### Piloto e critérios de conclusão

O solicitante selecionou cliente/alias 14, competência 202609, e cópia local sem
alterações. Em E3, antes do envio, elaborará a referência independente e
executará/conferirá o integrador localmente, com SQL, estrutura do banco e
resultados internos à empresa. Compartilhará só conclusão e divergências
sanitizadas quando permitido; E0 não exige fornecer o material interno. O
cliente usa até 10.000 registros por lote. O solicitante confirmou em 23/10
**21.770 lançamentos de crédito**, com previsão de **3 lotes: dois de 10.000 e um
de 1.770**. A contagem é relato do usuário; fracionamento e conteúdo ainda serão
conferidos na execução. Todos os campos
enviados, chaves, contagens e multiplicidades serão comparados, com diferenças
entre recebidos, processados e efetivos explicadas; conferir conclusão no
integrador e registrar falhas efetivamente exercitadas. Repetir após reset quando
implementado. Ausência de saldo inicial, prazo prescricional ou fracionamento
demonstrado deixa cobertura pendente; não encerra T01 ou MVP.

Aceite integral exige instalação e reset reproduzíveis por outra pessoa;
integrador apontado por configuração; conteúdo, vínculos e contagens demonstrados;
matriz mínima aplicável executada, com pendências resolvidas ou retirada de escopo
acordada antes do aceite; falhas reproduzíveis; modo estrito sem aprovação
silenciosa; contrato, manifesto e evidências com aproximações identificadas.
Regras de domínio adiadas exigem seleção e evidência próprias para o MVP completo.

## Fora do escopo

- Captura, extração, transformação, saneamento, migração ou montagem de lotes da
  origem; eventual substituto de origem é artefato separado.
- Portal TCE Virtual, SGI, ratificação oficial, assinatura, certificados ou
  permissões reais. Bloqueio de competência é cenário administrativo local.
- Todas as CONs e exceções fiscais; na entrega inicial, também ficam adiados
  cálculos, cronologia e validação de vínculos de negócio entre assuntos.
- NFS-e e demais assuntos fora dos seis selecionados, salvo dependência comprovada
  e reavaliação explícita de escopo; não aceitar silenciosamente.
- Lotes com assuntos misturados e cancelamento de envio já concluído.
- Outros módulos, SOAP, compatibilidade legada e múltiplos exercícios simultâneos.
- Espelhamento de produção, importação automática de dados municipais, proxy
  ou encaminhamento de chamadas ao TCE.
- Portal administrativo completo, Kafka, Redis, microsserviços, ORM, cluster e
  operação pública ou compartilhada sem necessidade confirmada.
- Webhooks e relatórios oficiais sem dependência demonstrada pelo inventário.
- Nesta solicitação documental: implementação da aplicação, execução do piloto,
  criação de build/testes de aplicação/Docker e publicação externa. A consolidação
  dos tickets locais foi autorizada; seus registros anteriores são preservados.

## Notas adicionais

### Fontes locais e precedência

| Fonte | Papel nesta síntese |
|---|---|
| [Planejamento v1.5](planejamento-simulador-tributario.md) | Autoridade de escopo, invariantes, RF01–RF12, E0–E4, T01–T19 e critérios do MVP |
| [Contrato E0](contrato-e0.md) | Inventário relatado, comportamento selecionado, manifesto e lacunas |
| [Arquitetura adotada](../ARCHITECTURE.md) | Stack, portas/adaptadores, transações, SQLite, relógio e testes |
| [Mapa E0 e oito tickets locais](../.scratch/wayfinder-e0/map.md) | Origem das respostas de 21–23/10, restrições e escolhas do usuário |
| [Rastreador](agents/issue-tracker.md) e [rótulos](agents/triage-labels.md) | GitHub Issues e vocabulário de publicação futura |
| [Orientação de domínio](agents/domain.md) | Layout previsto; não existem glossário CONTEXT ou ADRs no inventário atual |

HTML, PDF e DOCX são versões de apresentação anteriores e não orientam este PRD.
Os cinco insumos foram incorporados ao [pacote contratual](contratos/2026-v6-6.06/README.md),
com hashes iguais aos registrados em 21/10, schemas e exemplos fictícios. Data
original e versão da extração são desconhecidas e declaradas. Não houve consulta
ao TCE nem execução independente; leitura local não autentica fonte oficial.

A arquitetura adotada em 23/10 atualiza as propostas técnicas anteriores do
planejamento: payloads ficam no SQLite e instância/volume por execução é o padrão.
Essa atualização não fecha lacunas funcionais nem transforma relato em contrato
oficial. Divergências restantes devem ser resolvidas antes da implementação afetada.

### Revisão da condição de saída de E0 e pendências de E3
+
+Revisão autorizada pelo solicitante em 23/10/2026 por compliance: **E0 exige
+roteiro, responsável, recorte, campos e critérios de conferência; não exige
+receber SQL, estrutura interna do banco ou resultados sensíveis.** Esses elementos
+estão definidos no contrato: solicitante, cliente 14 / 202609, cópia estável,
+todos os campos enviados/chaves/contagens/multiplicidades e comparação independente
+da montagem de lotes, com decimais e identificadores preservados.
+
+E0 está concluído documentalmente no recorte inicial. O solicitante prepara a
+referência antes do envio do piloto em E3 e compara localmente conteúdo recebido,
+efetivos, perdas/duplicações, estados e recuperação. Evidências detalhadas ficam
+internas; conclusão/divergências sanitizadas são compartilhadas quando permitido.
+Aceite requer reconciliar diferenças e registrar limitações; somente totais ou
+o próprio payload não substituem a referência independente.
+
+| Pendência de E3 | Responsável | Aceite |
+|---|---|---|
+| Preparar referência e executar comparação local | Solicitante | Referência anterior ao envio, comparação de todos os campos/chaves/contagens/multiplicidades e relatório interno; registro sanitizado quando permitido |
+
+Volume de 21.770 créditos foi confirmado por relato; 3 lotes continuam previsão.
+Sem essa conferência, T01/piloto não são aprovados. O agente não certifica
+material inacessível; diferenças de seleção/transformação descobertas em E3
+podem exigir revisão do contrato e retrabalho.
+

### Etapas e limites de prontidão

| Etapa | Saída exigida |
|---|---|
| E0 | Contrato prioritário e roteiro do piloto definidos, com responsável/recorte/campos/critérios, sem entrega do SQL interno; concluída documentalmente no recorte |
| E1 | Autenticação, recepção, lotes, consultas e evidências persistidos; receber payload real do cliente e reabrir instância sem perda |
| E2 | Executor, finalização, descarte/cancelamento, relógio e regras do recorte; tabelas de decisão e testes dos efeitos |
| E3 | Referência preparada internamente antes do envio e comparação local pelo solicitante; integrador real e cenários com evidências internas; distinguir piloto parcial de T01 integral |
| E4 | Imagem, Compose, CI, guia, manifesto e relatório; reprodução de instalação, teste e reset por outra pessoa |

E0 está concluído documentalmente no recorte inicial; E1–E4 não implementados.
Não houve build, teste de aplicação ou ensaio de compatibilidade. O SQL interno
não é bloqueio de implementação; a conferência independente permanece bloqueante
do aceite E3/T01. Esta atualização não inicia implementação nem publica o PRD.
A publicação futura pela skill prevê `ready-for-agent`; o rótulo não substitui
especificações por fatia, pontos de verificação e escopo autorizado.

As estimativas de 15–25 dias úteis pertencem ao MVP original e dependem dos
insumos; reestimar após E0 para o recorte atual. Inicialização em 30 segundos,
suíte pequena em dois minutos e ensaio opcional de 50 mil registros/lote são
metas propostas, a calibrar no hardware; não são limites oficiais nem resultados.

Os pontos de teste estão confirmados. Esta consolidação atualiza documentos e
tickets locais, preservando seu histórico; não cria issues ou rótulos. Publicar
em `lukadev08/fake-esfinge` exige autorização específica, não concedida nesta tarefa.
