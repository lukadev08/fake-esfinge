# Progresso das tarefas

Histórico de execução dos agentes. Issues e especificações continuam no
[rastreador configurado](docs/agents/issue-tracker.md); este arquivo registra
o andamento, as falhas e a continuidade de cada tarefa.

## Como registrar

- Acrescente entradas ao final, preservando as anteriores. Retomadas da mesma
  tarefa geram nova entrada com referência à anterior.
- Identifique cada entrada por data (`AAAA-MM-DD`), título da tarefa, referência
  à issue/ticket local ou solicitação e situação: concluída, parcial ou bloqueada.
- Use sempre as três seções: **O que foi feito**, **O que deu errado** e
  **Próximo passo**.
- Em **O que foi feito**, registre entregas, arquivos e verificações realmente
  executadas, com resultado e evidência quando disponível.
- Em **O que deu errado**, registre falhas, bloqueios e limitações, distinguindo
  o que foi corrigido do que permanece aberto. Se não houve falha, informe isso;
  verificações não executadas não contam como sucesso.
- Em **Próximo passo**, descreva a ação concreta, a dependência e o responsável
  quando conhecidos. Se não houver continuidade necessária, registre “Nenhum
  para esta tarefa”. Esse campo não autoriza executar trabalho fora do escopo.

Use conteúdo sanitizado, sem credenciais, payloads originais ou dados pessoais.
O histórico começa nesta criação; tarefas anteriores não foram reconstruídas.

## 2026-10-23 — Criar rastreamento de tarefas dos agentes

Referência: solicitação do usuário nesta conversa, sem issue vinculada.
Situação: concluída.

### O que foi feito

- Criado este arquivo com formato de registro por tarefa.
- Atualizado [AGENTS.md](AGENTS.md) para leitura do histórico pertinente e
  registro ao concluir, interromper ou bloquear uma tarefa, incluindo entregas
  parciais e referência ao pedido de origem.
- Validados os links locais, as três seções do registro, a unicidade da orientação
  e o whitespace dos dois arquivos por checagem Python; `git diff --check` sem erros.

### O que deu errado

Nenhuma falha observada na edição. Não houve execução da aplicação; a mudança
é documental.

### Próximo passo

O agente responsável pela próxima tarefa deverá consultar o histórico pertinente
e acrescentar sua entrada ao encerrar ou interromper a execução.

## 2026-10-23 — Sintetizar PRD do simulador tributário

Referência: solicitação do usuário com a skill `to-prd`, a partir do planejamento
e documentos contratuais do projeto, sem issue vinculada.
Situação: parcial.

### O que foi feito

- Criado [PRD local](docs/prd-simulador-tributario.md), não publicado, com problema,
  solução, 52 histórias, decisões de implementação/teste, exclusões e pendências.
- Sintetizados planejamento v1.1, contrato E0, arquitetura adotada, mapa e oito
  tickets locais; preservadas as diferenças entre entrega inicial, piloto parcial
  e MVP completo. A stack adotada atualiza as propostas técnicas antigas, sem
  fechar E0 ou declarar compatibilidade.
- Propostos pontos de teste em HTTP compatível, controle administrativo,
  integrador real com esperado independente e portas/adaptadores reais.
- Conferidos links locais, estrutura, histórias numeradas, referências T01–T19
  e whitespace por validação documental local; sem testes de aplicação.

### O que deu errado

- Nenhuma falha documental remanescente. E0 continua incompleto e não há aplicação,
  build ou testes para executar. Fontes externas referenciadas não foram relidas
  ou verificadas contra o TCE; o PRD usa somente a documentação local atual.
- A skill exige confirmar os pontos de teste com o solicitante. Publicação em
  GitHub Issues requer autorização específica conforme orientação do repositório;
  nenhum rótulo ou issue foi criado.

### Próximo passo

O solicitante deve confirmar os pontos de teste e autorizar, se desejar, a
publicação do PRD em `lukadev08/fake-esfinge`. Após autorização, o agente deverá
verificar o rastreador e publicar o conteúdo confirmado com `ready-for-agent`,
preservando os bloqueios E0; não iniciar implementação contratual pendente.

## 2026-10-23 — Consolidar respostas e fechamento parcial de E0

Referência: plano “Consolidação das respostas e fechamento parcial de E0”,
explicitamente aprovado pelo solicitante nesta conversa. Retoma a entrada de
criação do PRD acima, com pontos de teste agora confirmados e publicação proibida.
Situação: concluída para a consolidação documental; E0 permanece parcial.

### O que foi feito

- Atualizados contrato E0, planejamento v1.2, PRD, arquitetura e guia de agentes
  com os relatos HTTP, tipos/envelopes, recuperação, códigos/erros derivados,
  expiração, exclusões, Spring Boot 3.5.10 e Docker Linux amd64.
- Criado [pacote 2026/V6/6.06](docs/contratos/2026-v6-6.06/README.md), com cinco
  insumos copiados byte a byte e manifesto de origem/datas/limites. Os cinco
  hashes conferem com os registros de 21/10. Datas originais desconhecidas não
  foram inventadas; não houve validação da autenticidade oficial.
- Especificados schemas dos seis assuntos, entradas/retornos das operações e
  20 exemplos fictícios. Registradas diferenças de `numeroParcelas`, prazo sem
  DTO extraído, campos não enviados e descarte de RECEBIDO após resposta perdida.
- Atualizados mapa e oito tickets locais, acrescentando consolidação final sem
  apagar respostas anteriores. `Status: draft` continua significando não publicado.
- Preservado manifesto inteiramente não implementado. Restam apenas esperado
  SQL/procedimento e volume real acima de 1.000 registros em um assunto do piloto
  cliente/alias 14 / 202609, ambos do solicitante. Os 3.000 citados eram exemplo.
- Validação documental: JSON Schema 2020-12 válido; 307 verificações positivas e
  negativas, incluindo os 20 exemplos, obrigatórios/nulos, tipos, datas,
  decimais exatos, extras, totalizadores, erro assíncrono e lote com 1.001 registros
  sem limite de tamanho inventado. Conferidos cinco hashes, links locais dos
  documentos editados, 52 histórias do PRD, T01–T19, whitespace e `git diff --check`.

### O que deu errado

- Validador JSON Schema não estava disponível nos runtimes Python consultados;
  usada ferramenta temporária em `/private/tmp`, sem dependência adicionada ao
  projeto. A validação com essa ferramenta passou; não houve erro remanescente.
- SQL e volume real ainda não foram fornecidos, mantendo E0 aberto conforme
  decisão expressa. Não houve aplicação, build, teste de aplicação, Docker,
  consulta a banco do piloto, chamada ao TCE ou comprovação de compatibilidade.
- HTML/PDF/DOCX não foram regenerados e continuam versões de apresentação
  anteriores. Nenhuma issue, rótulo, commit ou publicação foi criado.

### Próximo passo

O solicitante deve preparar consultas/esperado e procedimento reproduzível da
cópia estável, com todos os campos enviados, chaves, contagens e multiplicidades;
também deve confirmar contagem real acima de 1.000 registros em ao menos um
assunto. Após os dois insumos, revisar coerência do pacote e registrar fechamento
de E0, sem confundir isso com execução do piloto/T01 ou autorizar E1 automaticamente.

## 2026-10-23 — Registrar volume confirmado do piloto

Referência: confirmação do solicitante nesta conversa de 21.770 lançamentos de
crédito do cliente 14 / 202609; continuidade da consolidação E0 acima.
Situação: concluída para o registro; E0 permanece parcial.

### O que foi feito

- Registrada a contagem por relato, encerrando a pendência documental de volume.
- Calculada a previsão de 22 lotes: 21 de 1.000 e um de 770, com partições do
  integrador de até 1.000 registros; não declarada como execução comprovada.
- Atualizados contrato, planejamento v1.3, PRD, mapa, guia de agentes, arquitetura
  e resumo do pacote; acrescentadas atualizações aos tickets 03/07, preservando
  histórico. Os schemas e insumos originais não foram alterados.
- Conferidos cálculo, pendência única de E0, links locais e whitespace dos
  documentos alterados; `git diff --check` sem erros.

### O que deu errado

Nenhuma falha observada. Não houve consulta independente à base, execução do
integrador ou teste de aplicação. O esperado SQL/procedimento ainda não foi
entregue; a contagem confirmada não comprova fracionamento, conteúdo ou T01.

### Próximo passo

O solicitante deve preparar o esperado SQL e o procedimento reproduzível da
cópia estável, cobrindo campos enviados, chaves, contagens e multiplicidades.
Após esse insumo, revisar e registrar fechamento de E0; a execução futura deve
conferir os 22 lotes previstos e os 21.770 registros sem perdas ou duplicações.

## 2026-10-23 — Corrigir tamanho de lote e previsão do piloto

Referência: correção do solicitante nesta conversa, de 1.000 para 10.000 registros
por lote, baseada na lembrança de outro cliente com cerca de 240 mil registros.
Situação: concluída para a correção documental; E0 permanece parcial.

### O que foi feito

- Atualizado o tamanho de referência do cliente para até 10.000 registros por
  lote; mantido como relato, sem impor limite ao simulador ou ao TCE.
- Recalculada a previsão para 21.770 lançamentos do cliente 14 / 202609:
  3 lotes, dois de 10.000 e um de 1.770, substituindo a previsão anterior de 22.
- Atualizados contrato, planejamento v1.4, PRD, arquitetura, mapa e resumo do
  pacote; acrescentadas correções nos tickets 03/07 sem apagar histórico.
- Conferidos cálculo, links locais, whitespace e ausência das previsões antigas
  nos documentos vigentes; `git diff --check` sem erros. Fontes/schemas intactos.

### O que deu errado

O tamanho anterior estava incorreto segundo a revisão do solicitante. A correção
é relato baseado em lembrança, sem inspeção da configuração ou ensaio real.
Não houve consulta à base, execução do integrador ou teste de aplicação.

### Próximo passo

O solicitante deve preparar o esperado SQL/procedimento reproduzível, única
pendência documental restante de E0. No piloto, confirmar o tamanho efetivo das
partições e a previsão de 3 lotes, reconciliando os 21.770 registros sem perdas
ou duplicações. A contagem suficiente não comprova execução ou aceite T01.

## 2026-10-23 — Revisar critério E0 para conferência interna por compliance

Referência: solicitação “Certo, então registre isso no E0”, autorizando a revisão
proposta nesta conversa após a restrição de fornecimento do SQL de referência.
Situação: concluída documentalmente; E0 concluído no recorte inicial revisado.

### O que foi feito

- Registrada a mudança expressa do critério: E0 exige roteiro, responsável,
  recorte, campos e critérios definidos; a preparação concreta da referência e
  sua comparação ficam em E3, antes do aceite, no ambiente interno da empresa.
- SQL, estrutura do banco e resultados detalhados não precisam ser fornecidos
  ao agente/repositório. Compartilhar somente conclusão/divergências sanitizadas
  quando permitido pela empresa, sem declarar revisão independente do agente.
- Preservada referência independente anterior ao envio, com todos os campos,
  chaves, contagens/multiplicidades e cópia estável; contagens isoladas ou o
  próprio payload não substituem a comparação. Aceite E3/T01 permanece pendente.
- Atualizados contrato, planejamento v1.5, PRD, arquitetura, guia, mapa e resumo
  do pacote; acrescentada revisão aos tickets 03/07, sem apagar histórico.
- Conferidos links, coerência do estado E0/E3, registro das três seções e
  whitespace; `git diff --check` sem erros. Fontes e schemas não foram alterados.

### O que deu errado

Nenhuma falha de edição observada. A conferência tardia pode revelar diferenças
de seleção/transformação e exigir retrabalho. Não houve acesso ao SQL/base,
execução do piloto ou teste de aplicação; compatibilidade não foi comprovada.

### Próximo passo

Executar a próxima fatia de implementação somente quando solicitada, respeitando
o contrato e a sequência. Em E3, o solicitante prepara a referência antes do
envio e realiza a comparação local; sem essa conferência, manter aceite pendente.

## 2026-10-23 — Versionar alterações por tema e propósito

Referência: solicitação do usuário para realizar commits da worktree atual.
Situação: concluída.

### O que foi feito

- Organizadas as alterações em três grupos: configuração de agentes e orientações;
  contrato derivado E0 e fontes/schemas/exemplos; planejamento, arquitetura, PRD,
  rascunhos locais e histórico. Commits locais, sem publicação externa.
- Conferidas sintaxe dos três TOMLs e dos JSONs do pacote e whitespace do diff.
- Excluídos dos commits os dois arquivos `.DS_Store`, preservados na worktree.

### O que deu errado

A escrita inicial no índice Git foi bloqueada pelo sandbox; a repetição com
permissão ampliada permitiu criar os commits. A checagem de whitespace apontou
espaços finais nas fontes originais, preservados para manter seus bytes/hashes;
a checagem dos demais arquivos passou. O README ainda apresenta E0 aberto,
em divergência com o fechamento documental registrado nos documentos atuais;
conteúdo preservado nesta tarefa de versionamento. Não houve teste de aplicação
ou reexecução da validação semântica de schemas.

### Próximo passo

Corrigir o estado E0 no README em tarefa própria. Push depende de autorização
específica; não foi realizado.

## 2026-10-23 — Incluir primeira fatia do painel web após o backend

Referência: solicitação do usuário para adicionar a primeira fatia funcional da
interface ao escopo, com implementação somente após o backend concluído.
Situação: concluída para a atualização documental.

### O que foi feito

- Incluído RF13 no planejamento v1.6, com cenário/latência/falhas suportados,
  avanço manual/reset confirmado, remessas/lotes, estados/erros/contagens e
  inspeção paginada. Definidos limites e critérios de aceite da fatia.
- Acrescentada E5, bloqueada até saídas E1–E4 do backend no recorte contratado
  e consultas administrativas necessárias validadas. Mantidos E3/T01 e regras
  tributárias adiadas com seus limites, sem antecipar aceite.
- Alinhados arquitetura e PRD, com histórias 53–55: página na mesma aplicação e
  Docker, API administrativa com chave própria e consultas sem processamento.
- Conferidos links locais, âncora da seção 4.4, sequência de 55 histórias,
  referências RF13/E5 e whitespace; `git diff --check` sem erros.

### O que deu errado

Nenhuma falha documental observada. Faixa adicional de 2–4 dias é preliminar,
sem protótipo, a reestimar após o backend. Não houve implementação, testes de
aplicação ou regeneração das apresentações HTML/PDF/DOCX; nenhuma publicação.

### Próximo passo

Concluir e validar o backend no recorte contratado antes de iniciar E5. Na
preparação de E5, reestimar com as consultas administrativas disponíveis e
executar o aceite específico da seção 4.4; esta tarefa não inicia implementação.

## 2026-10-23 — Preparar tickets de implementação a partir do PRD

Referência: solicitação do usuário com a skill `to-issues`, usando PRD,
planejamento e documentos complementares do simulador.
Situação: parcial; divisão aguardando aprovação antes de publicar.

### O que foi feito

- Preparados [27 rascunhos completos](.scratch/implementation-issues/README.md)
  e [dados para publicação](.scratch/implementation-issues/issues.json), com
  títulos, critérios de aceite, rótulos previstos, dependências e histórias.
- Consultados planejamento v1.6, PRD com 55 histórias, contrato E0, arquitetura,
  pacote contratual, orientações do rastreador e mapa histórico E0. Preservados
  os documentos e rascunhos anteriores, inclusive alterações já na worktree.
- Separados 21 tickets AFK e seis HITL: cinco aceites humanos e fechamento das
  pendências de evolução. E1 exige payload real; E3 mantém conferência interna
  independente; E4 exige reprodução por outra pessoa; E5 depende dessas saídas.
- Verificados remoto e repositório público `lukadev08/fake-esfinge`. A consulta
  de issues não retornou tickets existentes. Os rótulos `ready-for-agent`,
  `ready-for-human` e `needs-info` ainda não existem; nenhuma mutação no GitHub.
- Validados 27 IDs/títulos únicos, template, critérios, referências e dependências
  sem ciclos em ordem de publicação; mapeadas H01–H55 e T01–T19. Registrados hashes
  de seis fontes para detectar mudanças antes da publicação. Mapeamento indica
  contribuição planejada, sem declarar histórias ou cenários implementados.

### O que deu errado

Nenhuma falha na geração dos rascunhos. A skill exige aprovação da divisão antes
da publicação; nenhuma issue, rótulo ou dependência externa foi criada. Regras
fiscais e demais casos adiados não têm contrato suficiente para tickets AFK;
ficaram em decisão HITL, sem bloquear E5 após aceite do recorte. Não há aplicação
para build/testes de runtime; a validação desta tarefa é documental.

### Próximo passo

O solicitante deve revisar granularidade, dependências e classificação AFK/HITL
e aprovar ou ajustar a divisão. Após aprovação, conferir alterações nas fontes
e duplicatas no rastreador, criar somente os rótulos necessários e publicar os
tickets aprovados em ordem de dependência, substituindo IDs locais por issues
reais em todas as referências. Esta tarefa não autoriza implementar o simulador.

## 2026-10-23 — Publicar os tickets de implementação aprovados

Referência: aprovação explícita do solicitante à divisão em 27 tickets da skill
`to-issues`, em continuidade à entrada anterior.
Situação: concluída.

### O que foi feito

- Conferidos os hashes das seis fontes: sem alteração desde a proposta aprovada.
  Consulta prévia por conector e CLI confirmou ausência de issues no rastreador.
- Criados e aplicados somente os rótulos necessários: `ready-for-agent`,
  `ready-for-human` e `needs-info`.
- Publicadas as [issues #1–#27](https://github.com/lukadev08/fake-esfinge/issues),
  em ordem de dependência: 21 AFK, cinco aceites HITL e uma decisão HITL de evolução.
  Bloqueios documentados no corpo com IDs reais, conforme proposta aprovada;
  não foram criadas dependências nativas, issue pai ou alterações nos tickets E0.
- Substituídas também as referências cruzadas futuras nos critérios de #1,
  #10 e #15. Releitura remota das 27 issues confirmou títulos, corpos, rótulos,
  estado aberto e ausência de referências locais pendentes/metadados do piloto.
- Criados [índice publicado](.scratch/implementation-issues/PUBLISHED.md) e
  [registro dos corpos finais](.scratch/implementation-issues/publication.json).
  Atualizados índice e payload originais com os IDs reais, preservando a versão
  aprovada anterior à publicação e identificando a sanitização de #19.

### O que deu errado

A revisão automática de aprovação rejeitou a tentativa inicial de publicar I19
por conter metadados internos de cliente, competência e volumes em repositório
público. A tentativa não criou issue. Publicada alternativa sanitizada em #19,
sem esses detalhes e mantendo referência independente, comparação de conteúdo/
multiplicidades e demonstração de múltiplos lotes. SQL, estrutura do banco,
payloads originais e resultados internos não foram enviados. Não restou bloqueio
de publicação. Não houve implementação, build, teste de aplicação, piloto,
commit ou push de código.

### Próximo passo

Nenhum para a publicação dos tickets. Quando solicitada a implementação, a
primeira fatia sem bloqueios é [#1](https://github.com/lukadev08/fake-esfinge/issues/1).
Preservar os aceites humanos e as saídas E1–E4 antes de iniciar E5; #27 acompanha
evolução não contratada para execução AFK e não bloqueia o painel no recorte.

## 2026-10-23 — Segunda rodada de commits e envio à branch remota

Referência: solicitação do usuário para agrupar alterações e realizar push.
Situação: parcial; commits e envio em execução.

### O que foi feito

- Inspecionadas alterações, histórico e remoto origin de lukadev08/fake-esfinge;
  branch main acompanha origin/main, com três commits anteriores ainda locais.
- Separados os temas: remoção dos agentes locais, escopo RF13/E5 e registros
  da divisão/publicação de 27 tickets. Arquivos .DS_Store excluídos.
- Conferida sintaxe dos dois JSONs novos; validação documental, sem aplicação.

### O que deu errado

Nenhuma falha observada até este registro. Não houve build ou teste de aplicação.
A divergência anterior do README sobre E0 permanece fora desta tarefa.

### Próximo passo

Concluir os commits, enviar main para origin/main e verificar sincronização;
registrar o resultado no relato final.

## 2026-10-23 — Confirmar envio da segunda rodada

Referência: continuidade da solicitação de commits e push acima.
Situação: concluída.

### O que foi feito

- Criados 2ae1706 (remoção de agentes), fb61a25 (RF13/E5) e eb1c544
  (tickets e histórico), com checagem de whitespace sem erros.
- Push de main para origin/main concluído com exit code 0: 4fe7d6e..eb1c544,
  incluindo os três commits anteriores.

### O que deu errado

Nenhuma falha no envio. Dois .DS_Store permanecem fora do versionamento.
Sem testes de aplicação; permanecem os limites documentais registrados acima.

### Próximo passo

Enviar este registro de conclusão e verificar sincronização. Nenhuma
implementação foi iniciada.

## 2026-10-24 — Implementar autenticação fictícia da issue #1

Referência: [issue #1](https://github.com/lukadev08/fake-esfinge/issues/1) e solicitação
de orquestração com explorer → executor → reviewer.
Situação: parcial; implementação local pronta, revisão e repetição final Docker pendentes.

### O que foi feito

- Criado monomódulo Maven/Java21/SpringBoot3.5.10, wrapper oficial com Maven3.9.9
  e checksum, autenticação por portas/adaptadores e inspeção administrativa separada.
- Persistidos contexto, identidades/UGs fictícias e sessões SQLite com migração
  SQL versionada, checksum, FK por conexão e reabertura sem recriar dados incompatíveis.
- Implementados login derivado, negações, token vinculado ao contexto e validade
  configurável com relógio substituível; senha PBKDF2/salt e token persistido por hash.
- Criados Dockerfile mínimo Linux amd64/non-root, guia real de execução,
  [manifesto atual](docs/cobertura.md) e [evidência TDD](docs/evidencias/issue-1.md).
- Verificação do executor: `./mvnw verify`, 30 testes aprovados, zero falhas/erros/
  ignorados; jar empacotado. Testes cobrem núcleo, HTTP/SQLite real, configuração,
  sanitização de falha SQLite e dependências de classes; `git diff --check` sem erros.
- Supervisor informou Docker Linux amd64/non-root com rede desabilitada e 93
  assertions HTTP/reinício aprovadas na imagem inicial identificada na evidência.
  Após correção da leitura de identidades por porta pública da autenticação,
  repetirá a checagem no jar final antes do PR.

### O que deu errado

- Maven não estava instalado; usada distribuição temporária validada e criado
  wrapper oficial versionado, sem instalação global. JDK21 existe no SDKMAN,
  embora não apareça em `java_home` do macOS; build executado com caminho explícito.
- Ciclos RED falharam pelos comportamentos ausentes esperados e passaram após
  implementação. Avisos de autoanexação Mockito/ByteBuddy não falharam a suíte.
- Download limpo do wrapper revelou `sha256sum -c` incompatível com stdin no
  macOS; corrigido para `sha256sum -c -`, checksum e download limpo aprovados.
- Docker, revisão independente e integrador real não foram validados pelo executor.
  Esta fatia não conclui E1/T01 ou as capacidades posteriores.

### Próximo passo

Supervisor validar imagem Linux amd64, persistência e HTTP em execução real;
reviewer independente confrontar #1 e corrigir achados por agente próprio antes
de permitir PR. Permanecem as dependências #1 → #2 → #3–#7 → aceite humano #8.

## 2026-10-24 — Revisar e validar a fatia #1 sob supervisão

Referência: [issue #1](https://github.com/lukadev08/fake-esfinge/issues/1), continuidade
da implementação acima e objetivo de orquestração solicitado pelo usuário.
Situação: concluída para implementação, revisão e validação locais; publicação do PR em preparação.

### O que foi feito

- Grupo exclusivo da #1: explorer GPT-6 Luna/médio, executor GPT-6.1 Sol/baixo e
  reviewer GPT-6.1 Sol/médio, identificados ao iniciar. Ciclo explorar → implementar
  → revisar concluído; reviewer aprovou sem achados materiais ou correções obrigatórias.
- Supervisor executou `clean verify`: 30 testes, zero falhas/erros/ignorados, build
  aprovado em 5,387 segundos. Conferidos efeitos locais dos comandos e testes.
- Construída a imagem final Linux amd64 por digest, como usuário `10001:10001`.
  Ensaio com volume exclusivo e `--network none`: 85 verificações HTTP e oito
  após reinício aprovadas; sessão e contexto persistidos. TTL malformado impediu
  inicialização com exit code 1 e mensagem sanitizada. Resultados e imagem em
  [evidência da #1](docs/evidencias/issue-1.md); manifesto atualizado.
- Reconsultada a issue: aberta, sem alteração de critérios; nenhum PR duplicado
  para a branch `issue-1-autenticacao`.

### O que deu errado

- Escritas de branch/index e cache Docker exigiram execução fora do sandbox;
  as operações locais foram autorizadas pela revisão automática e concluídas.
  OrbStack estava parado e foi iniciado para o ensaio; Maven ficou em cache temporário.
- Na autoinspeção, o executor encontrou acesso de cenários à tabela de identidades
  de outra capacidade; corrigiu por porta pública antes da revisão. Suíte limpa
  e imagem final foram verificadas após a correção.
- Não houve integrador real, T01, aceite da saída E1 ou CI remoto. Nenhuma dessas
  verificações é inferida dos testes locais. Não houve merge ou implantação.

### Próximo passo

Criar o PR da #1 com os testes verificados e registrar seu estado. A #2 depende
da integração da #1; cada issue seguinte terá grupo independente. Seguir #1 →
#2 → #3–#7 → aceite humano #8; não iniciar E2 sem essa saída de E1.

## 2026-10-24 — Preparar PR da #1 e registrar bloqueio de publicação

Referência: [issue #1](https://github.com/lukadev08/fake-esfinge/issues/1), continuidade
da revisão e validação sob supervisão acima.
Situação: bloqueada para publicação; implementação local revisada e verificada.

### O que foi feito

- Criado commit local `c7b2bd5` na branch `issue-1-autenticacao`, com 41 arquivos
  restritos à #1. Fontes e schemas contratuais intactos; `.DS_Store` fora do commit.
- Preparado [rascunho exato do PR](.scratch/pr-issue-1.md), com título, corpo,
  destino `lukadev08/fake-esfinge`, base `main`, branch e validações comprovadas.
- Mantidos os limites da sequência: #2 depende da #1; os demais grupos ainda não
  foram iniciados. O objetivo de orquestração permanece ativo.
- `git ls-remote` confirmou ausência da branch de trabalho no remoto. Removidos
  somente o container e o volume temporários criados para o ensaio da #1;
  a imagem local e os arquivos de checagem temporários foram preservados.

### O que deu errado

A revisão automática de aprovação rejeitou `git push -u origin issue-1-autenticacao`
antes da execução: publicar código em destino externo exige autorização confiável
e específica para a ação e o repositório. Nenhum PR foi criado. A operação não foi
repetida por outro caminho. Testes e revisão locais continuam válidos; não houve
merge, implantação ou publicação de imagem.

### Próximo passo

O solicitante deve autorizar explicitamente push de `issue-1-autenticacao` para
`origin` e criação do PR preparado em `lukadev08/fake-esfinge`, base `main`.
Após autorização, verificar branch/remoto e critérios, publicar e registrar o PR.
Merge e aceites humanos continuam sujeitos aos seus limites específicos.

## 2026-10-24 — Revalidar autorização pendente para publicar a #1

Referência: objetivo ativo de orquestração e bloqueio de publicação registrado acima.
Situação: bloqueada para publicação; segunda constatação consecutiva do impedimento.

### O que foi feito

- Revalidados branch e histórico locais: `issue-1-autenticacao`, commits
  `c7b2bd5` e `fa22e25`; somente os dois `.DS_Store` estavam fora do versionamento.
- Consulta remota confirmou `main` em `a757dd5`, ausência da branch de trabalho
  e de PR correspondente. A issue #1 continua sem comentários; não apareceu
  autorização humana nova nesta retomada automática.
- Conferido o rascunho exato do PR, preservado em `.scratch/pr-issue-1.md`.
  Nenhum teste foi repetido: o código não mudou desde a validação registrada.

### O que deu errado

Permanece a rejeição automática do push por ausência de autorização específica
para publicar no destino externo. A retomada do objetivo não responde à pergunta
de autorização pendente. Não houve nova tentativa de push, criação de PR ou
início da #2. Não há processo de publicação ativo a aguardar.

### Próximo passo

O solicitante deve responder à autorização já solicitada: push de
`issue-1-autenticacao` para `origin` e criação do PR em
`lukadev08/fake-esfinge`, base `main`. Até lá, preservar os commits locais e as
dependências da sequência; merge e aceites humanos não estão autorizados.

## 2026-10-24 — Confirmar bloqueio persistente da orquestração

Referência: objetivo de orquestração e duas constatações anteriores do bloqueio
de publicação da #1.
Situação: bloqueada; terceira constatação consecutiva do mesmo impedimento.

### O que foi feito

- Revalidados checkout e remoto: código da #1 preservado; somente este histórico
  mudou desde o último commit. `main` remoto segue em `a757dd5`; branch da #1
  e PR correspondente continuam ausentes.
- A retomada anterior não avançou a implementação ou publicação; confirmou a
  mesma pendência de autorização. Não existe processo ativo de publicação.
- Conferido que nenhuma correção obrigatória da #1 resta após revisão e testes;
  a #2 permanece dependente da integração da #1. Não há próxima ação executável
  nesta sequência sem resolver o limite externo.

### O que deu errado

Não houve resposta humana à autorização de push e criação do PR. A rejeição
automática anterior continua válida e não será contornada. A continuação
automática do objetivo não fornece essa autorização.

### Próximo passo

Solicitante autorizar explicitamente push de `issue-1-autenticacao` para
`origin` e criação do PR preparado em `lukadev08/fake-esfinge`, base `main`.
Retomar a orquestração após essa decisão, preservando os limites de merge,
implantação e aceites humanos. Objetivo não concluído.

## 2026-10-24 — Retomar publicação autorizada da #1

Referência: resposta explícita do solicitante “autorizo a publicação, continue a
meta”, em resposta ao pedido de push e criação do PR da #1.
Situação: parcial; autorização recebida, publicação em preparação.

### O que foi feito

- Registrada autorização específica para enviar `issue-1-autenticacao` ao
  `origin` e criar o PR em `lukadev08/fake-esfinge`, base `main`.
- Conferidos os commits e o diff: somente registros documentais mudaram desde
  a implementação revisada `c7b2bd5`; as evidências de 30 testes e 93 verificações
  Docker continuam associadas ao mesmo código. Testes não repetidos sem mudança.
- Atualizado o rascunho do PR com a autorização; histórico do bloqueio preservado.

### O que deu errado

O bloqueio anterior de publicação foi resolvido pela autorização humana. Nenhuma
nova falha local observada nesta retomada. Merge, implantação e publicação de
imagem continuam fora desta autorização.

### Próximo passo

Commitar os registros, enviar a branch, criar e anexar o PR; verificar estado
remoto e registrar a revisão da #1 antes de avançar na sequência de dependências.

## 2026-10-24 — Publicar PR #28 e conferir conteúdo remoto

Referência: [issue #1](https://github.com/lukadev08/fake-esfinge/issues/1) e
autorização explícita de publicação registrada acima.
Situação: concluída para publicação; aprovação formal e integração pendentes.

### O que foi feito

- Criado commit documental `aff307b` e enviado `issue-1-autenticacao` para
  `origin`, com exit code 0. Criado [PR #28](https://github.com/lukadev08/fake-esfinge/pull/28)
  aberto contra `main` (`a757dd5`) e anexado à tarefa. Não é rascunho; conector
  confirmou ausência de conflitos.
- `grupo1_reviewer` manteve a aprovação técnica no head publicado `aff307b`:
  comparação remota com `c7b2bd5` mostrou somente os documentos de progresso e
  do PR, com código/testes/dependências/wrapper/Docker iguais aos revisados.
  Nenhum achado material novo. Não houve repetição desnecessária da suíte.
- Consulta de status remoto não retornou statuses; CI remoto continua sem
  comprovação. Validação local preservada: 30 testes e 93 verificações Docker.
- Preparado o parecer formal no registro local do PR. A #2 continua dependente
  da integração da #1, sem iniciar grupo novo ou antecipar seu escopo.

### O que deu errado

A revisão automática de autorização rejeitou a tentativa de submeter `APPROVE`
no PR #28: classificou a aprovação formal como mensagem externa distinta da
publicação autorizada. Nenhum review foi publicado; não foi tentado comentário
ou outro caminho alternativo. Merge não foi executado e continua fora da
autorização recebida para push/criação do PR.

### Próximo passo

Solicitante decidir se autoriza submissão da aprovação formal e merge do PR #28
em `main`. Após autorização, conferir o head atual e os critérios antes das ações;
registrar eventual limitação do GitHub sem contorná-la e avançar à #2 somente
após resolver sua dependência. Sem integrador real ou aceite E1/T01 nesta fatia.

## 2026-10-24 — Integrar PR #28 e liberar a #2

Referência: autorização explícita do solicitante “Autorizar aprovação e merge”
para [PR #28](https://github.com/lukadev08/fake-esfinge/pull/28).
Situação: concluída para integração da #1; #2 iniciada em exploração.

### O que foi feito

- Conferido head `63ec6a4`, com código igual ao revisado/testado. Submetido merge
  com esse SHA esperado; GitHub confirmou `merged=true`, commit `a36c7bf`.
  Releitura confirmou PR integrado e issue #1 fechada como concluída.
- Sincronizado `main` local por fast-forward e criada branch independente
  `issue-2-recepcao-contribuintes` a partir de `a36c7bf`. Nenhum dado de usuário
  alterado; os dois `.DS_Store` continuam fora do versionamento.
- Reconsultada #2: aberta, `ready-for-agent`, sem comentários e dependência #1
  encerrada. Iniciado `grupo2_explorer`, exclusivo da #2, com pedido de identidade
  e exploração somente leitura antes do desenho/implementação.

### O que deu errado

Após autorização, a submissão de aprovação formal chegou ao GitHub, mas foi
recusada com HTTP 422: `Review Can not approve your own pull request`. Nenhuma
aprovação formal ou comentário alternativo foi publicado. A revisão técnica
independente permaneceu aprovada; o GitHub permitiu o merge autorizado.
Não houve contorno de proteção, implantação ou publicação de imagem.

### Próximo passo

Executar #2 com grupo próprio: explorar → implementar por TDD → revisar, com
correção delegada se necessária. Recorte: abertura, recepção de contribuintes,
consulta RECEBIDO e inspeção administrativa persistida. Outros assuntos ficam
em #3–#7; processamento em E2, após aceite humano #8. Publicação e merge de PRs
futuros exigem seus limites de autorização; E1/T01 continuam pendentes.

## 2026-10-24 — Implementar recepção persistida de contribuintes da #2

Referência: [issue #2](https://github.com/lukadev08/fake-esfinge/issues/2), após
integração da dependência #1 no PR #28; continuação do grupo2 exclusivo.
Situação: parcial; implementação/testes locais concluídos, revisão e Docker pendentes.

### O que foi feito

- `grupo2_executor` identificado como GPT-6.1 Sol/raciocínio baixo. Implementadas
  abertura, recepção de contribuintes, consulta RECEBIDO e inspeção administrativa
  por portas, sem processamento ou finalização. Corrigido entendimento do envelope:
  lista diretamente na raiz, sem wrapper `lote`, conforme schema/exemplos.
- Acrescentada migração V2 sem alterar V1: remessas/lotes/eventos, índices únicos,
  FK de vínculos, BLOB/hash/ordem/contagem e transações IMMEDIATE com rollback
  inclusive em falha de commit. Verificados checksums anteriores antes do upgrade.
- Validador Jackson específico segue o schema, sem nova dependência: tipos sem
  coerção, nulos/ausentes, extras, calendário, Unicode e UTF-8; recusa JSON duplicado
  ou documento extra. Não inventa strings não vazias, regras fiscais ou limite de 10.000.
- `verify` concluído: 51 testes aprovados, zero falhas/erros/ignorados, jar em
  6,725 segundos. Inclui SQLite real/HTTP, concorrência, contenção, falha de statement
  e COMMIT, isolamento entre UGs/cenários, reabertura e V1→V2. Atualizados README,
  manifesto e [evidência da #2](docs/evidencias/issue-2.md).

### O que deu errado

- A execução do agente foi interrompida por limite de uso após iniciar o `verify`;
  retomada no mesmo grupo/árvore. O mesmo handle confirmou término exit0 e os 51
  testes; não houve reinício ou repetição da suíte sem mudança.
- Ciclos RED observaram capacidades ausentes e erros de mapeamento (política vazia,
  evento sem lote, Content-Type válido e documento vazio); corrigidos com testes
  verdes. Um teste de record com array foi corrigido para comparar conteúdo.
- Revisão independente, Docker da #2, integrador real e aceite E1/T01 não foram
  executados pelo executor. Não houve commit, push, PR, merge ou publicação de imagem.

### Próximo passo

Supervisor executar revisão independente do grupo2 e validação final limpa/Docker;
se houver achados, delegar correção a agente próprio. Só permitir PR com testes
verdes verificados. #3–#7 dependem da #2; saída E1 continua exigindo aceite humano #8.

## 2026-10-24 — Revisar e validar a #2 sob supervisão

Referência: [issue #2](https://github.com/lukadev08/fake-esfinge/issues/2) e retomada
solicitada pelo usuário após interrupção por limite de uso.
Situação: concluída para implementação, revisão e validação locais; publicação pendente.

### O que foi feito

- Retomado o mesmo executor do grupo2 após confirmação de término por erro de
  limite de uso; preservados arquivos e resultado do processo Maven anterior.
- `grupo2_reviewer`, GPT-6.1 Sol/médio conforme configuração do papel, aprovou o
  diff staged sobre `ce1217c` sem achados materiais. Conferidos contrato, isolamento,
  bytes/contagens/eventos, transações, migração e fronteiras; nenhuma correção obrigatória.
- Supervisor executou `clean verify`: 51 testes, zero falhas/erros/ignorados,
  build aprovado em 7,222 segundos. V1 e fontes/schemas contratuais intactos.
- Construída imagem Linux amd64, usuário `10001:10001`, volume exclusivo e
  `--network none`; 163 verificações no fluxo inicial e 15 após reinício, todas
  aprovadas. Incluem concorrência, isolamento UG, bytes UTF-8, nulos/zeros,
  consulta sem processamento, Unicode e 10.001 registros sem perda/truncamento.
  Sessão/lote/bytes conservados após reinício; logs somente de inicialização.
- Atualizados manifesto e [evidência completa](docs/evidencias/issue-2.md).
  Preparado [PR local da #2](.scratch/pr-issue-2.md), com título/corpo e escopo.
- Removidos somente o container e o volume temporários criados para o ensaio da
  #2; imagem local e cliente de checagem temporário preservados.

### O que deu errado

Nenhuma falha remanescente na revisão ou nas verificações finais. A interrupção
por quota foi resolvida na retomada e não exigiu refazer a implementação.
Não houve integrador real, aceite E1/T01 ou CI remoto. A autorização anterior de
publicação/merge referia-se ao PR #28; nenhuma publicação da #2 foi executada.

### Próximo passo

Solicitante decidir sobre push de `issue-2-recepcao-contribuintes`, criação do PR
em `lukadev08/fake-esfinge` contra `main` e merge da mudança revisada. Após resolver
essa dependência, iniciar grupo independente da #3 e seguir #4–#7 antes do aceite #8.
