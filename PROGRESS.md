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

## 2026-10-24 — Publicar a #2 no PR #29

Referência: [issue #2](https://github.com/lukadev08/fake-esfinge/issues/2) e
autorização do usuário: “autorizada a publicação”.
Situação: publicação concluída; integração pendente de autorização específica.

### O que foi feito

- Confirmados branch, destino remoto, ausência de PR anterior e `main` em
  `a36c7bf`. Árvore revisada no commit `02e036f`, sem alterações de código após
  revisão e validação: 51 testes e 178 verificações Docker aprovadas.
- Enviada `issue-2-recepcao-contribuintes` para `origin` e aberto o
  [PR #29](https://github.com/lukadev08/fake-esfinge/pull/29) contra `main`,
  com título/corpo preparados; PR anexado à tarefa.
- Atualizados o registro do PR e a evidência local. Nenhuma alteração de código
  ou teste nesta publicação; `.DS_Store` permanecem fora do versionamento.

### O que deu errado

Nenhuma falha na publicação. Não foram executados merge, implantação, publicação
de imagem, CI remoto ou integrador real. A autorização recebida cobre publicação;
o merge permanece sujeito ao limite específico do `AGENTS.md`.

### Próximo passo

Obter autorização específica para integrar o PR #29 em `main`; conferir seu head
e condições de integração antes do merge. #3–#7 dependem da #2 integrada;
aceite humano #8/E1 e T01 integral continuam pendentes.

## 2026-10-24 — Explorar a #3 enquanto a integração da #2 aguarda autorização

Referência: [issue #3](https://github.com/lukadev08/fake-esfinge/issues/3) e
solicitação do usuário: “siga para a próxima tarefa”.
Situação: preparação local concluída; implementação dependente da integração da #2.

### O que foi feito

- Conferidos PR #29 aberto, head `46c67f2`, integração possível informada pelo
  GitHub e ausência de status de CI publicados. Código/testes permanecem iguais
  ao commit revisado/validado `02e036f`; somente três documentos foram atualizados.
- Confirmada #3 aberta, `ready-for-agent`, sem comentários e bloqueada pela #2.
  Iniciado explorer exclusivo `grupo3_explorer`, GPT-6 Luna/médio conforme o papel,
  com identificação e exploração somente leitura.
- Supervisão e explorer conferiram planejamento, arquitetura, contrato, schema
  `credito` e pontos de integração. Preparado [plano local](.scratch/issue-3-plan.md)
  com escopo, persistência decimal exata, migração e fatias TDD previstas.

### O que deu errado

Não há impedimento técnico identificado na exploração. O PR #29 ainda não foi
integrado; a autorização anterior cobriu publicação. Solicitada autorização
específica de merge conforme `AGENTS.md`, mantendo trabalho independente em leitura.
Nenhuma implementação, teste novo, mensagem externa ou publicação da #3 executada.

### Próximo passo

Após autorização, integrar PR #29 com conferência do head, sincronizar a base da
#3 e iniciar executor próprio. Depois, revisão independente, eventual correção
delegada e verificação do supervisor. Nenhum critério E1/T01 foi encerrado.

## 2026-10-24 — Integrar a #2 e iniciar implementação da #3

Referência: [PR #29](https://github.com/lukadev08/fake-esfinge/pull/29),
[issue #3](https://github.com/lukadev08/fake-esfinge/issues/3) e autorização do
usuário para integrar o PR e prosseguir para a próxima tarefa.
Situação: integração da #2 concluída; implementação local da #3 iniciada.

### O que foi feito

- Reconfirmados head `46c67f2`, revisão aprovada e ausência de mudanças de código
  desde a validação `02e036f`. Integrado PR #29 com SHA esperado e método merge,
  gerando `4b61304929b0435bc6a499911d3025808bc36ce7` em `main`.
- Conferidos PR merged e issue #2 encerrada como completed. Atualizado `main`
  local por fast-forward e criada `issue-3-recepcao-creditos`, conservando a
  preparação local e os arquivos `.DS_Store` fora do versionamento.
- Atualizados registros da integração e plano da #3. Exploração concluída no
  grupo3; próxima etapa executada por executor próprio, sob supervisão.

### O que deu errado

Nenhuma falha na integração. Não houve implantação, publicação de imagem ou
nova tentativa de aprovação formal na própria conta, restrição já comprovada
na #1. CI remoto, integrador real e aceite E1/T01 continuam pendentes.

### Próximo passo

Implementar #3 por fatias TDD, revisar com agente independente e delegar eventual
correção. Supervisor verificar testes e Docker antes de preparar PR. A autorização
recebida não publica nem integra automaticamente futuras mudanças da #3.

## 2026-10-24 — Concluir implementação e supervisão locais da #3

Referência: [issue #3](https://github.com/lukadev08/fake-esfinge/issues/3), após
integração autorizada da #2 no PR #29.
Situação: implementação, revisão e validação locais concluídas; publicação pendente.

### O que foi feito

- `grupo3_executor`, GPT-6.1 Sol/baixo conforme papel, implementou recepção de
  créditos pelo fluxo existente, sem processamento. Parser `LotePayload`, assunto
  explícito, obrigatórios/opcionais/tipos/datas/mês histórico e decimal exato;
  preservados contribuintes, bytes, multiplicidade, isolamento e contagens.
- Persistidos valores BigDecimal como TEXT por lote/índice, junto de bytes,
  vínculos e evento na mesma transação. V3 preserva V1/V2/checksums, dados,
  sessões, IDs/eventos/FKs e sequências. Inspeção usa números JSON exatos.
- RED inicial observou 422 onde crédito exigia 200; a fatia completa passou.
  Regressões ampliaram schema/SQLite/migração/falhas; executor concluiu `verify`
  com 61 testes verdes, zero falhas/erros/ignorados, exit0, 7,694 segundos.
- `grupo3_reviewer`, GPT-6.1 Sol/médio conforme papel, aprovou diff staged sobre
  `8431741` sem achados materiais. Nenhuma correção obrigatória ou agente de
  correção necessário. Supervisor executou `clean verify`: 61 testes verdes,
  zero falhas/erros/ignorados, exit0, 7,508 segundos.
- Imagem Linux amd64, usuário 10001, volume próprio e `--network none`: base V2
  preparada com 163 verificações, preservada no upgrade. Na imagem V3 passaram
  15 verificações de upgrade, 704 de crédito/HTTP, 36 após reinício e 12 de SQLite
  somente leitura: **767 no total**, incluindo 10.001 registros e valores TEXT
  equivalentes exatamente a cada registro recebido. Logs comuns só de início.
- Atualizados README, manifesto, [evidência completa](docs/evidencias/issue-3.md),
  plano e [PR local](.scratch/pr-issue-3.md). Código/testes congelados após a
  revisão e validação; somente documentação foi concluída depois.
- Removidos somente container/volume temporários do ensaio. Imagem e clientes
  temporários preservados; dados exclusivamente fictícios, sem chamadas ao TCE.

### O que deu errado

Nenhuma falha material remanescente. A compilação isolada de um cliente auxiliar
precisou incluir seu diretório de classes no classpath; corrigida antes da execução.
O aviso SLF4J do cliente SQLite não falhou o ensaio nem integra os logs da aplicação.
Não houve publicação da #3, integrador real, CI remoto ou aceite E1/T01.

### Próximo passo

Solicitante decidir sobre push da branch `issue-3-recepcao-creditos`, criação de
PR em `lukadev08/fake-esfinge` contra `main` e integração da mudança revisada.
#4 é a próxima tarefa na sequência; E1 continua sujeito ao aceite humano #8.

## 2026-10-25 — Publicar/integrar a #3 e iniciar a #4

Referência: [issue #3](https://github.com/lukadev08/fake-esfinge/issues/3),
[PR #30](https://github.com/lukadev08/fake-esfinge/pull/30) e autorização do usuário
para publicação/merge, mantendo a sequência de tarefas da meta.
Situação: #3 integrada; exploração da #4 iniciada em grupo independente.

### O que foi feito

- Reconfirmados head local `8a08bc1`, árvore limpa exceto `.DS_Store`, ausência de
  branch/PR remotos da #3 e `main` em `4b61304`. Publicada a branch revisada e
  aberto PR #30 com título/corpo preparados; PR anexado à tarefa.
- Conferidos head e integração possível. Merge executado com SHA esperado,
  sem contorno de proteção, gerando `e0b485276e5841eb0785217edb8f4dd0dbae8bda`.
  Verificados PR merged e issue #3 encerrada como completed. Não havia status
  de CI publicados; evidência permanece local: 61 testes e 767 verificações V3.
- Atualizado `main` local por fast-forward e criada `issue-4-recepcao-arrecadacao`.
  #4 aberta, `ready-for-agent`, sem comentários, dependência #2 já concluída.
  Iniciado `grupo4_explorer`, exclusivo da tarefa e somente leitura, com pedido
  de identificação nome/modelo/raciocínio.
- Atualizados registros da #3. Nenhum código, teste ou fonte contratual alterado
  nesta integração; arquivos `.DS_Store` preservados fora do versionamento.

### O que deu errado

Nenhuma falha na publicação ou integração. A aprovação técnica independente foi
preservada; não se repetiu tentativa de aprovação formal da própria conta.
Sem implantação, publicação de imagem, integrador real, CI remoto ou aceite E1/T01.

### Próximo passo

Concluir desenho da #4 a partir do schema `arrecadacao`, implementar por executor
próprio em TDD e revisar com reviewer independente. Delegar eventual correção;
supervisor verificar testes/Docker antes de preparar PR. Publicação/merge da #4
continuam sujeitos à autorização específica. Aceite humano #8/E1 permanece pendente.

## 2026-10-25 — Concluir implementação e supervisão locais da #4

Referência: [issue #4](https://github.com/lukadev08/fake-esfinge/issues/4), após
integração da #3 no PR #30, dentro da sequência da meta.
Situação: implementação, revisão e validação locais concluídas; publicação pendente.

### O que foi feito

- Grupo4 exclusivo: exploração por GPT-6 Luna/médio, execução por GPT-6.1
  Sol/baixo e revisão por GPT-6.1 Sol/médio, conforme os papéis, com identificação.
  Executor implementou arrecadação no fluxo existente, sem processamento.
- Schema `arrecadacao`, opcionais/nulos, `numeroParcelas` plural, datas, Unicode,
  inteiros e dinheiro exatos; singular, extras e misturas rejeitados. Preservados
  contribuintes/créditos, bytes, multiplicidade, contagens e isolamento.
- `monetaryValues` e tabela comum `valores_recebidos` preservam dinheiro em TEXT
  para créditos/arrecadação, na transação de lote/evento/atividade. V4 mantém
  V1–V3/checksums/fontes, dinheiro existente, IDs/eventos/sessões/FKs e high-water.
- RED inicial: 200 esperado, 422 recebido, exit1/3,372s; a mesma fatia passou,
  exit0/3,351s. Regressões focalizadas: 26 testes verdes/4,984s. Executor concluiu
  `verify`: 71 testes, zero falhas/erros/ignorados, exit0/7,313s.
- Reviewer aprovou diff staged sobre `54525d5`, sem achados materiais; nenhuma
  correção obrigatória. Supervisor executou `clean verify`: 71 testes verdes,
  zero falhas/erros/ignorados, exit0/8,349s. Código/testes congelados após validação.
- Imagem Linux amd64, usuário10001 e `--network none`: V3 preparada com 704
  verificações, preservada no upgrade. Na V4 passaram 36 verificações de crédito,
  771 de arrecadação/HTTP, 34 após reinício e 12 de SQLite somente leitura:
  **853 no total**, com 10.001 arrecadações e mais de 20.000 valores TEXT exatos
  dos dois assuntos. Logs comuns somente de inicialização.
- Atualizados README, manifesto, [evidência completa](docs/evidencias/issue-4.md),
  plano e [PR local](.scratch/pr-issue-4.md). Removidos somente container/volume
  temporários do ensaio; imagem e clientes preservados. Dados somente fictícios.

### O que deu errado

Uma substituição inicial de versão nos testes alterou a expectativa de conflitos
concorrentes de três para quatro; detectada/restaurada pelo executor, sem mudar
produção. Não há falha material remanescente. Aviso SLF4J do cliente SQLite não
falhou o ensaio nem integra logs da aplicação. Sem publicação da #4, integrador
real, CI remoto ou aceite E1/T01.

### Próximo passo

Solicitante decidir sobre push de `issue-4-recepcao-arrecadacao`, criação do PR
em `lukadev08/fake-esfinge` contra `main` e integração da mudança revisada.
#5 é a próxima tarefa; E1 continua sujeito ao aceite humano #8.

## 2026-10-25 — Publicar/integrar a #4 e iniciar a #5

Referência: [issue #4](https://github.com/lukadev08/fake-esfinge/issues/4),
[PR #31](https://github.com/lukadev08/fake-esfinge/pull/31) e autorização do usuário
para publicação/merge, seguindo a sequência de tarefas da meta.
Situação: #4 integrada; exploração da #5 iniciada em grupo independente.

### O que foi feito

- Reconfirmados head `4e3f8df`, árvore limpa exceto `.DS_Store`, destino remoto,
  ausência de branch/PR anteriores e `main` em `e0b4852`. Publicada a branch e
  aberto PR #31 com título/corpo preparados; PR anexado à tarefa.
- Conferidos head e integração possível. Merge autorizado com SHA esperado
  gerou `cc2d12d3b3351f4edc8446d0366e4355119c6a7b`, sem contorno de proteção.
  Confirmados PR merged e issue #4 completed. Não havia status de CI publicados;
  evidência continua local: 71 testes e 853 verificações na imagem V4.
- Atualizado `main` local por fast-forward e criada `issue-5-recepcao-baixas`.
  #5 aberta, `ready-for-agent`, sem comentários, dependência #2 já concluída.
  Iniciado `grupo5_explorer`, exclusivo e somente leitura, com pedido de
  identificação nome/modelo/raciocínio; papel GPT-6 Luna/médio.
- Atualizados registros da #4; código/testes/fontes sem alterações nesta integração.

### O que deu errado

Nenhuma falha de publicação/integração. Não houve nova tentativa de aprovação
formal da própria conta, implantação ou publicação de imagem. Integrador real,
CI remoto e aceite E1/T01 continuam pendentes.

### Próximo passo

Concluir desenho da #5 a partir do schema `baixa`; implementar por executor
próprio em TDD, revisar com reviewer independente e delegar eventual correção.
Supervisor verificar testes/Docker antes do PR. Publicação/merge da #5 exigem
autorização específica; saída E1 continua sujeita ao aceite humano #8.

## 2026-10-25 — Concluir implementação e supervisão locais da #5

Referência: [issue #5](https://github.com/lukadev08/fake-esfinge/issues/5), após
integração da #4 no PR #31, dentro da sequência da meta.
Situação: implementação, revisão e validação locais concluídas; publicação pendente.

### O que foi feito

- Grupo5 exclusivo: exploração por GPT-6 Luna/médio, execução por GPT-6.1 Sol/baixo,
  revisão por GPT-6.1 Sol/médio conforme papéis, com identificação solicitada.
  Executor acrescentou schema/assunto/admin de baixa e V5, reutilizando núcleo,
  `ReceivedContent`, JDBC e tabela monetária sem alterações.
- Conservados obrigatórios/opcionais, tipos/zeros, datas, bytes, decimal exato,
  multiplicidade/contagens e isolamento. Baixa anterior ao crédito e referências
  inexistentes são aceitas só estruturalmente; relações fiscais continuam adiadas.
- V5 preserva V1–V4/checksums/fontes, dados de contribuinte/crédito/arrecadação,
  dinheiro TEXT, IDs/eventos/sessões/FKs e high-water. Falha de migração reverte
  tudo; rollback real de statement/COMMIT exercitado para quatro assuntos.
- RED inicial: 200 esperado/422 recebido, exit1/3,470s; mesma fatia GREEN,
  exit0/4,414s. Regressões focalizadas: 32 testes verdes/5,663s. Executor concluiu
  `verify`: 82 testes, zero falhas/erros/ignorados, exit0/8,229s.
- Reviewer aprovou diff staged sobre `299b457`, sem achados materiais; nenhuma
  correção obrigatória. Supervisor executou `clean verify`: 82 testes verdes,
  zero falhas/erros/ignorados, exit0/9,079s. Código/testes congelados após validação.
- Imagem Linux amd64, usuário10001 e `--network none`: V4 preparada com 771
  verificações, conservada no upgrade. Na V5 passaram 34 verificações de regressão,
  714 de baixa/HTTP, 34 após reinício e 12 de SQLite somente leitura: **794 no total**,
  incluindo 10.001 baixas e mais de 20.000 valores TEXT exatos dos três assuntos
  monetários. Logs comuns somente de inicialização.
- Atualizados README, manifesto, [evidência completa](docs/evidencias/issue-5.md),
  plano e [PR local](.scratch/pr-issue-5.md). Removidos somente container/volume
  temporários do ensaio; imagem e clientes preservados. Dados somente fictícios.

### O que deu errado

Nenhuma falha material remanescente na revisão ou supervisão. O RED inicial foi
o comportamento esperado antes da implementação. Aviso SLF4J do cliente SQLite
não falhou o ensaio nem integra logs da aplicação. Não houve publicação da #5,
integrador real, CI remoto ou aceite E1/T01.

### Próximo passo

Solicitante decidir sobre push de `issue-5-recepcao-baixas`, criação do PR em
`lukadev08/fake-esfinge` contra `main` e integração da mudança revisada.
#6 é a próxima tarefa; E1 continua sujeito ao aceite humano #8.

## 2026-10-25 — Publicar/integrar a #5 e iniciar a #6

Referência: [issue #5](https://github.com/lukadev08/fake-esfinge/issues/5),
[PR #32](https://github.com/lukadev08/fake-esfinge/pull/32) e autorização do usuário
para publicação/merge, seguindo a sequência da meta.
Situação: #5 integrada; exploração da #6 iniciada em grupo independente.

### O que foi feito

- Reconfirmados head `b7aa32b`, árvore limpa exceto `.DS_Store`, destino remoto,
  ausência de branch/PR anteriores e `main` em `cc2d12d`. Publicada a branch e
  aberto PR #32 com título/corpo preparados; PR anexado à tarefa.
- Conferidos head e integração possível. Merge autorizado com SHA esperado
  gerou `25dcbd662c3bcfb91253554a644503929a31fa0b`, sem contorno de proteção.
  Confirmados PR merged e issue #5 completed. Não havia status de CI publicados;
  evidência continua local: 82 testes e 794 verificações na imagem V5.
- Atualizado `main` local por fast-forward e criada `issue-6-recepcao-saldo`.
  #6 aberta, `ready-for-agent`, sem comentários, dependência #2 concluída.
  Iniciado `grupo6_explorer`, exclusivo e somente leitura, com identificação
  solicitada nome/modelo/raciocínio; papel GPT-6 Luna/médio.
- Atualizados registros da #5. Nenhum código/teste/fonte alterado nesta integração.

### O que deu errado

Nenhuma falha na publicação/integração. Sem nova tentativa de aprovação formal
da própria conta, implantação, publicação de imagem, integrador real ou CI remoto.
Aceite E1/T01 permanece pendente.

### Próximo passo

Concluir desenho da #6 com quatro valores monetários nomeados por registro;
implementar por executor próprio em TDD e revisar com reviewer independente.
Delegar eventual correção e supervisor verificar testes/Docker antes do PR.
Publicação/merge da #6 exigem autorização específica; cenário janeiro não encerra T16/E1/T01.

## 2026-10-25 — Retomar e validar localmente a #6; Docker pendente

Referência: [issue #6](https://github.com/lukadev08/fake-esfinge/issues/6) e pedido
do usuário “retome de onde parou”, após interrupção do executor por quota.
Situação: implementação/revisão/suíte e jar local aprovados; Docker V6 pendente.

### O que foi feito

- Grupo6 exclusivo: explorer GPT-6 Luna/médio, executor GPT-6.1 Sol/baixo,
  reviewer GPT-6.1 Sol/médio conforme papéis, com identificação solicitada.
  Executor implementou saldo estrutural, mapas nomeados imutáveis e V6 com
  campo/valor TEXT, preservando valores V5, bytes/IDs/eventos/sessões/FKs/high-water.
- RED inicial 200/422, exit1/3,533s; mesma fatia GREEN, exit0/3,535s. Regressões
  focalizadas: 39 verdes/6,329s. Quota interrompeu antes dos documentos/verify.
  Retomado o mesmo executor/árvore após uso normal confirmado, sem consumir reset.
- Executor confirmou ausência de verify pendente, terminou documentos e iniciou
  uma única execução completa, handle83479: 95 testes, zero falhas/erros/ignorados,
  exit0/14,398s. Reviewer aprovou diff staged sobre `50558ee` sem achados materiais;
  nenhuma correção obrigatória. Supervisor `clean verify`: 95 verdes, exit0/15,182s.
- Sem Docker responsivo, preparado ensaio independente pelo jar em macOS arm64,
  Corretto 21.0.2, vinculado a 127.0.0.1 e SQLite temporário fictício. Código V5
  exportado de `25dcbd6` e construído offline só para fixture; 714 verificações
  prepararam seus dados. Na V6 passaram 34 de upgrade/regressão, 636 de saldo,
  46 após reinício e 13 de SQLite somente leitura: **729 no total**, incluindo
  10.001 saldos e mais de 50.000 valores TEXT, com campos distintos e exatos.
- Encerrados somente os servidores locais do ensaio; fontes, base e clientes
  temporários preservados fora do Git. Código/testes congelados após revisão e
  validação; atualizados README, manifesto, [evidência](docs/evidencias/issue-6.md),
  plano e [rascunho de PR](.scratch/pr-issue-6.md), com Docker pendente explícito.

### O que deu errado

- Interrupção por quota resolvida na retomada. Um import ausente no cliente SQL
  auxiliar foi corrigido antes de sua execução, sem alterar a aplicação.
- API Docker não responde, inclusive fora do sandbox e com CLI do OrbStack;
  `/_ping` no socket local expirou após 5s. OrbStack informou Running, mas o macOS
  não abriu o app (`kLSNoExecutableErr`). Diagnóstico somente leitura apontou
  CLI Homebrew; não houve fix/reinstalação/restart global. Consultas presas canceladas.
- V5 Docker havia passado 714 verificações de preparação antes da interrupção;
  container/volume `fake-esfinge-issue6-supervisao` não puderam ser conferidos ou
  limpos depois. Nenhuma imagem V6 construída/validada. Aviso SLF4J do cliente
  SQLite não falhou o ensaio local. Sem publicação da #6 ou aceite E1/T01/T16.

### Próximo passo

Solicitante restaurar Docker/OrbStack ou decidir explicitamente manter somente
a validação local. Executar ensaio V6 quando disponível e limpar apenas os
recursos temporários da #6. Depois decidir publicação/merge com autorização
específica; #7 segue na sequência, antes do aceite humano #8.

## 2026-10-25 — Conferir restauração do Docker da #6

Referência: issue #6 e resposta do usuário “Restaurar Docker e avisar”.
Situação: implementação/revisão/validação local preservadas; Docker V6 pendente.

### O que foi feito

- Salvo commit local `9853f08`, com 95 testes e 729 verificações pelo jar aprovados,
  sem publicação. Executável do OrbStack confirmado no disco; o erro de abertura
  observado dentro do sandbox não comprovava ausência física. App abriu fora do
  sandbox com exit0, sem restart, reinstalação ou mudança de configuração.
- API respondeu `OK` e inventário confirmou o container temporário da #6 ainda
  na imagem V5, ativo havia cinco horas, com volume e imagem conhecidos preservados.
  Mantido contexto explícito `orbstack`; somente recursos da #6 foram selecionados.

### O que deu errado

A parada do container, inclusive com timeout curto fora do sandbox, não concluiu;
a consulta seguinte também ficou sem resposta. Clientes CLI cancelados. Não houve
remoção do volume ou construção da imagem V6. A inspeção da interface retornou
Mac bloqueado e exigiu desbloqueio manual; causa da instabilidade Docker não confirmada.

### Próximo passo

Solicitante desbloquear o Mac e confirmar a área de trabalho acessível. Retomar
diagnóstico/ensaio Docker sem repetir testes verdes nem alterar o código revisado;
conferir estado do container antes de agir. Publicação da #6 continua não autorizada.

## 2026-10-25 — Concluir ensaio Docker V6 da #6

Referência: [issue #6](https://github.com/lukadev08/fake-esfinge/issues/6) e resposta
do usuário “Mac desbloqueado”, dando continuidade à recuperação do Docker.
Situação: implementação, revisão, 95 testes e ensaios macOS/Docker aprovados;
publicação da #6 e remoção do volume temporário aguardam autorização específica.

### O que foi feito

- Após desbloqueio manual, interface do OrbStack confirmou apenas nossa instância
  temporária em execução. Parada pela interface concluiu com Exited143, preservando
  volume V5; API respondeu novamente. Removido container V5 e construída imagem
  local V6 `46d3d07e84c0`, Linux amd64/Temurin21.0.10+7, com o mesmo jar aprovado.
- V6 executada no volume V5 como UID10001, sem rede/portas publicadas e com clientes
  somente leitura. Passaram 47 verificações de upgrade/expiração/novo login, 636
  de saldo, 46 após reinício e 13 de SQLite: **742**, todas exit0, além de 714 de
  preparação Docker V5. Conservados 10.001 saldos e mais de 50.000 valores TEXT
  exatos por campo/índice/vínculo; logs comuns somente de inicialização.
- Mantidas evidências separadas: 95 testes da suíte limpa e 729 verificações no
  jar macOS continuam aprovados, sem mudança de código/testes após revisão.
  Atualizados [evidência](docs/evidencias/issue-6.md), manifesto, plano e
  [corpo exato de PR local](.scratch/pr-issue-6.md).
- Container V6 parado e removido. Volume fictício, imagem e clientes preservados.
  GitHub reconferido: #6 aberta sem comentários, nenhum PR dessa branch e `main`
  ainda em `25dcbd6`. Nenhuma publicação da #6 realizada.

### O que deu errado

- O primeiro cliente de upgrade esperava sessão V5 ainda válida; recebeu401
  após mais de cinco horas de interrupção ambiental. Conferência SQLite confirmou
  sessão/UG/validade de uma hora preservadas e expiração anterior ao instante atual.
  Cliente auxiliar testou a rejeição e fez novo login; sem mudar aplicação, relógio
  ou dados antigos. O ensaio corrigido passou; a falha inicial fica registrada.
- Causa da instabilidade Docker não confirmada. Sem restart global/reinstalação
  ou alteração de configuração. Avisos SLF4J dos clientes não falharam o ensaio.
- Revisão automática rejeitou excluir o volume temporário por exigir autorização
  específica para destruir seus dados. Nenhuma tentativa alternativa de exclusão;
  volume `fake-esfinge-issue6-supervisao` mantido. Sem CI remoto/integrador real ou
  aceite humano #8/E1/T01/T16 integral.

### Próximo passo

Solicitante decidir sobre push de `issue-6-recepcao-saldo`, criação de PR contra
`main` e merge em `lukadev08/fake-esfinge`; decidir separadamente sobre exclusão do
volume fictício temporário. Após integração autorizada, #7 segue na sequência,
antes do aceite humano #8.

## 2026-10-25 — Publicar/integrar a #6 e iniciar a #7

Referência: [issue #6](https://github.com/lukadev08/fake-esfinge/issues/6),
[PR #33](https://github.com/lukadev08/fake-esfinge/pull/33) e autorizações específicas
do usuário para publicação/merge e exclusão do volume fictício temporário.
Situação: #6 integrada; exploração da #7 iniciada em grupo independente.

### O que foi feito

- Reconfirmados head `7e5709b`, árvore limpa exceto `.DS_Store`, destino remoto,
  ausência de PR anterior e `main` em `25dcbd6`. Push e PR #33 realizados com corpo
  preparado; PR anexado à tarefa. Conferidos head e merge possível.
- Merge com SHA esperado gerou `ad27bd88b0dc9fba2d5bd08e0a0059988f090119`, sem
  contorno de proteção. Confirmados PR merged e #6 completed. Nenhum status de CI
  publicado; evidência permanece local: 95 testes, 729 verificações macOS e 742 Docker.
- Após autorização específica, removido somente o volume fictício
  `fake-esfinge-issue6-supervisao` no contexto `orbstack`, exit0 e ausência confirmada.
  Imagem e clientes preservados. Atualizados registros da #6 sem mudar seu código/testes.
- `main` atualizado por fast-forward e criada branch `issue-7-recepcao-prazo`.
  #7 aberta `ready-for-agent`, sem comentários, dependência #2 concluída. Iniciado
  `grupo7_explorer`, exclusivo/somente leitura, com identificação solicitada
  nome/modelo/raciocínio; papel GPT-6 Luna/médio.

### O que deu errado

Nenhuma falha na publicação/integração ou limpeza autorizada. Não houve nova
tentativa de aprovação formal da própria conta, implantação, publicação de imagem
ou CI remoto. Aceite E1/T01 permanece pendente, sujeito à #8.

### Próximo passo

Concluir exploração da #7, implementar schema derivado de quatro campos em fatia
TDD por executor próprio e revisar por reviewer independente. Delegar correções
se necessárias; supervisor verificar suíte/Docker antes de preparar PR.
Publicação/merge da #7 exigem autorização específica; E2 aguarda o aceite humano #8.

## 2026-10-25 — Implementar, revisar e supervisionar a #7

Referência: [issue #7](https://github.com/lukadev08/fake-esfinge/issues/7) e meta
autorizada de seguir tarefas em ordem, com equipe independente por issue.
Situação: implementação, revisão, suíte limpa e Docker V7 aprovados; somente local.

### O que foi feito

- Grupo7 identificado: explorer GPT-6 Luna/médio, executor GPT-6.1 Sol/baixo e
  reviewer GPT-6.1 Sol/médio. Contrato derivado dos quatro campos mantido, sem DTO
  extraído, teto fiscal, cronologia ou vínculo tributário inventado.
- Prazo recebido/consultado/inspecionado em RECEBIDO, não monetário como cadastro.
  V7 preserva dados V6/campos/valores/IDs/eventos/sessões/FKs/high-water; rollback
  e falhas reais de statement/COMMIT cobrem o sexto assunto. Fontes/V1–V6 intactos.
- RED HTTP200/422: exit1/3,995s; mesma fatia GREEN: exit0/3,524s. Focalizada final:
  43 verdes/6,325s. Executor verify: 105 testes, zero falhas/erros/ignorados,
  exit0/9,613s; nenhum processo pendente. Reviewer aprovou diff sobre `27e7980`,
  incluindo novos arquivos, sem achados materiais ou correções obrigatórias.
- Supervisor clean verify: **105 verdes**, exit0/10,957s. Docker V7 `bcbc05ff4d10`,
  Linux amd64/Temurin21.0.10+7, mesmo jar aprovado, UID10001, sem rede/portas.
  **678 verificações**, todas exit0: 13 de captura comparada antes/depois de dez
  tabelas, 34 de baixa, 46 de saldo, 529 de prazo, 42 após reinício e 14 de SQLite.
  Incluídos 10.001 prazos, mais de 50.000 valores antigos TEXT exatos e ausência
  de dinheiro para prazo/cadastro. Preparação V6: 1.350 checks, separados.
- Código/testes congelados após revisão/validação. Atualizados README, manifesto,
  [evidência](docs/evidencias/issue-7.md), plano e [PR local](.scratch/pr-issue-7.md).
  Container temporário removido; volume fictício, imagem e clientes preservados.

### O que deu errado

Primeira focalizada: 43 testes/1 falha, exit1/6,424s, por comparação de identidade
de `byte[]` em nova asserção de teste. Corrigida somente a asserção para campos e
`assertArrayEquals`; aplicação não mudou por essa falha. Reexecução verde.
Nenhuma falha material remanescente na revisão/supervisão. Avisos existentes
Mockito/ByteBuddy e SLF4J auxiliar não falharam os ensaios. Sem push/PR/merge da #7,
CI remoto, integrador real ou aceite humano E1/T01.

### Próximo passo

Solicitante decidir publicação/merge da #7 contra `main` em `lukadev08/fake-esfinge`
e, separadamente, exclusão do volume fictício `fake-esfinge-issue7-supervisao`.
Depois da integração, #8 exige recepção de payload real no ambiente local
controlado e aprovação expressa da saída E1; não iniciar E2 antes desse aceite.

## 2026-10-25 — Publicar/integrar a #7 e preparar o aceite humano #8

Referência: [issue #7](https://github.com/lukadev08/fake-esfinge/issues/7),
[PR #34](https://github.com/lukadev08/fake-esfinge/pull/34) e autorizações específicas
do usuário para publicação/merge e exclusão do volume fictício temporário.
Situação: #7 integrada; preparação documental da #8 iniciada; aceite humano pendente.

### O que foi feito

- Reconfirmados head `07c3951`, árvore limpa exceto `.DS_Store`, destino remoto,
  ausência de PR anterior e `main` em `ad27bd8`. Push e PR #34 com corpo preparado,
  anexado à tarefa; conferidos head e merge possível.
- Merge com SHA esperado gerou `53814c6d2b93e6a3ef37e0022f1993ab1c6c9a36`, sem
  contorno de proteção. Confirmados PR merged e #7 completed. Nenhum status CI
  publicado; evidência permanece local: 105 testes e 678 verificações Docker V7.
- Excluído somente o volume fictício `fake-esfinge-issue7-supervisao` no contexto
  `orbstack`, exit0 e ausência confirmada, após autorização específica. Imagem e
  clientes preservados. Atualizados registros da #7 sem alterar código/testes.
- `main` atualizado por fast-forward; criada branch local `issue-8-aceite-e1`.
  #8 aberta `ready-for-human`, sem comentários: exige payload real e aprovação
  expressa do solicitante, sem SQL interno ou aceite E3 completo nesta etapa.
  Iniciado `grupo8_explorer`, exclusivo/somente leitura, identificado como
  GPT-6 Luna/médio. Solicitada situação do ensaio em pergunta assíncrona, sem dados sensíveis.

### O que deu errado

Nenhuma falha na publicação, integração ou limpeza autorizada. Sem aprovação
formal da própria conta, implantação ou publicação de imagem. A preparação da #8
não comprova execução real, não encerra E1/T01 e não libera E2.

### Próximo passo

Preparar roteiro local mínimo para o solicitante executar no ambiente controlado;
revisar em grupo próprio e registrar lacunas. Receber somente situação/conclusão
sanitizada quando permitida, preservando payloads, SQL e resultados detalhados
internamente. #8 permanece aberta até critérios demonstrados e aceite expresso.

## 2026-10-25 — Retomar e revisar o roteiro local do aceite #8

Referência: [issue #8](https://github.com/lukadev08/fake-esfinge/issues/8), pedido
“retome de onde parou” e confirmação do solicitante: “Vou executar no ambiente
interno controlado”. Situação: preparação documental concluída e revisada;
execução real e aceite humano E1 pendentes.

### O que foi feito

- Reconfirmados branch `issue-8-aceite-e1`, base documental `e17baa5`, ausência
  inicial de roteiro e árvore limpa exceto `.DS_Store`. PR #34 continua integrado
  em `53814c6`; #8 aberta `ready-for-human`, sem comentários. Código, testes,
  dependências e Dockerfile permanecem iguais à versão funcional integrada.
- Retomado o mesmo `grupo8_executor` após uso normal restabelecido, sem consumir
  reset. Grupo identificado: explorer GPT-6 Luna/médio, executor GPT-6.1 Sol/baixo,
  reviewer GPT-6.1 Sol/médio e agente de correção GPT-6.1 Sol/baixo.
- Criado [roteiro local](.scratch/issue-8-roteiro.md) para execução pelo solicitante
  no ambiente interno controlado: artefato/configuração/volume exclusivos,
  recepção pelo integrador real, inspeção privada antes/depois da reabertura,
  autenticação e modelo de conclusão sanitizada. Todos os critérios de execução
  e aceite continuam PENDENTES; payloads, SQL e detalhes permanecem internos.
- Imagem local V7 `bcbc05ff4d10` e SHA do jar `0d54ec70afea3c1f7f2a87db0e172683795000913e4c4668576643b17c5d5957`
  reconferidos neste Mac. Disponibilidade/execução na empresa não comprovadas;
  construção interna deve identificar seu próprio artefato. Os 105 testes e 678
  verificações da #7 permanecem evidência anterior, sem nova execução nesta tarefa.
- Reviewer encontrou duas ampliações indevidas do gate: exigir captura do corpo
  no integrador e observar recepção antes do422. Correção delegada a agente próprio:
  inspeções anteriores/posteriores são a referência obrigatória; comparação com
  corpo enviado é adicional quando disponível. Consulta/admin após disparo
  rejeitado são permitidos, pois não houve processamento. Rechecagem aprovou o
  roteiro sem achados materiais remanescentes.
- Verificação estática: rotas/headers/campos/configuração conferidos nas fontes,
  três links locais existentes, whitespace/final de arquivo e diff sem erros.
  Nenhum build, suíte, container de aplicação ou integrador real iniciado para a #8.

### O que deu errado

O primeiro turno do executor foi interrompido por limite de uso, antes de criar
o arquivo; retomada preservou a mesma tarefa/árvore. A revisão documental apontou
dois P2 de escopo, corrigidos e rechecados. Nenhum código foi alterado, nenhum
dado real acessado e nenhuma publicação externa da #8 realizada. Revisão do
roteiro não comprova execução real nem aprovação da saída E1.

### Próximo passo

Solicitante executar o roteiro no ambiente interno controlado, conferir os
critérios e reconciliar divergências. Quando permitido pela empresa, retornar
somente versão do artefato/contrato, conclusão sanitizada, lacunas e decisão
expressa sobre E1. Manter #8 aberta e E2 bloqueada até esse aceite; não declarar
T01/E3 integral ou homologação oficial a partir desta preparação.

## 2026-10-25 — Autorizar e validar a publicação documental do roteiro E1

Referência: solicitação do usuário “autorizo o próximo merge e publicação com
testes verdes” e continuação da implementação sob supervisão; apoio à
[issue #8](https://github.com/lukadev08/fake-esfinge/issues/8).
Situação: publicação do roteiro autorizada e validada; execução/aceite E1 pendentes.

### O que foi feito

- Reconfirmados branch `issue-8-aceite-e1`, head `28036ed`, árvore limpa exceto
  `.DS_Store`, ausência de PR anterior e `main` remoto em `53814c6`. #8 aberta,
  `ready-for-human`, sem comentários ou conclusão de ensaio fornecida.
- Mantida a revisão documental aprovada do grupo8, incluindo as duas correções
  de escopo já rechecadas. Supervisor reconferiu três links locais, whitespace,
  cinco critérios PENDENTES e ausência de alterações no código/testes/build/Dockerfile.
- Comando Maven inspecionado novamente. Novo `verify` supervisionado: **105 testes,
  zero falhas/erros/ignorados**, exit0 em **11,123s**, com build/jar aprovados.
  SQLite temporário e credenciais fictícias; nenhum acesso ao integrador/TCE ou
  dados reais. Não houve novo ensaio Docker; imagem #7 permanece referência anterior.
- Preparado [corpo exato de PR](.scratch/pr-issue-8-roteiro.md), sem fechamento
  automático da #8 e sem declaração de saída E1. Autorização permite publicar e
  integrar esta documentação contra `main` em `lukadev08/fake-esfinge`.
- O mesmo reviewer do grupo8, identificado como GPT-6.1 Sol/médio, aprovou também
  a preparação da publicação, corpo com `Refs #8` e registro de autorização,
  sem achados materiais ou inferência de execução/aceite E1.

### O que deu errado

Nenhuma falha na validação. A nova autorização de publicação/continuação não
comprova o ensaio real nem esclarece se a dependência E1→E2 pode ser adiada.
Solicitada definição assíncrona específica sobre antecipar E2 com #8 pendente,
pois o planejamento e `AGENTS.md` exigem a sequência. Sem resposta, o marco é mantido.
Avisos existentes Mockito/ByteBuddy não falharam os testes. Sem CI remoto ou
nova evidência de execução interna do solicitante.

### Próximo passo

Publicar e integrar o PR documental autorizado com head verificado, mantendo
#8 aberta. A implementação E2 aguarda execução/aceite E1 ou decisão expressa
do solicitante permitindo antecipá-la; não inferir aceite nem encerrar #8.

## 2026-10-25 — Publicar e integrar o roteiro de apoio ao aceite E1

Referência: autorização do usuário para o próximo merge/publicação com testes
verdes, [PR #35](https://github.com/lukadev08/fake-esfinge/pull/35) e issue #8.
Situação: publicação documental concluída; #8 aberta; decisão sobre antecipar E2 pendente.

### O que foi feito

- Commit local `2bc1444` salvou o corpo exato de PR e a validação supervisionada.
  Publicada `issue-8-aceite-e1` e criado PR #35 contra `main`, com `Refs #8`, sem
  fechamento automático ou afirmação de saída E1. PR anexado à tarefa.
- Conferidos head esperado, base e merge possível. Merge normal autorizado
  gerou `ddf84875a5afd5a336537ca631afe04ef9ebfd1c`; PR merged e `main` remoto
  reconfirmados. #8 continua aberta, sem comentários ou resultado real fornecido.
- Nenhum status de CI publicado; verdes locais: 105 testes no novo verify
  supervisionado/11,123s e verificações documentais. Fonte/testes/build/Dockerfile
  permanecem iguais a `53814c6`. Não foi repetido o ensaio Docker da #7.
- Branch local sincronizada por fast-forward com `origin/main`. Atualizado o
  registro do PR, mantendo o roteiro e seus cinco critérios PENDENTES.

### O que deu errado

Nenhuma falha na publicação ou integração, sem contorno de proteção. A autorização
de continuação foi recebida, mas o ensaio/aceite real E1 segue sem evidência e a
pergunta específica sobre adiar a dependência E1→E2 permanece sem resposta.
Não iniciada implementação E2, não fechado o aceite #8, sem implantação ou
publicação de imagem. Este registro posterior ao merge permanece local.

### Próximo passo

Receber conclusão sanitizada e aceite E1 após execução interna, ou decisão
expressa permitindo antecipar E2 com #8 pendente. Na segunda hipótese, registrar
a exceção de sequência e retomar a próxima implementação em equipe própria,
preservando #8/T01/E3 como pendentes e supervisão/revisão/testes por tarefa.

## 2026-10-26 — Manter E2 aguardando o aceite humano E1

Referência: resposta expressa do solicitante “Manter E2 aguardando o aceite E1”
à definição sobre antecipar E2 com a [issue #8](https://github.com/lukadev08/fake-esfinge/issues/8) pendente.
Situação: sequência E1→E2 mantida; execução interna e aceite humano E1 pendentes.

### O que foi feito

- Registrada a decisão de não antecipar E2. #8 reconfirmada aberta; nenhuma
  conclusão do ensaio real ou aprovação de saída E1 foi fornecida.
- Atualizado somente o registro local da autorização. Roteiro revisado permanece
  disponível em [.scratch/issue-8-roteiro.md](.scratch/issue-8-roteiro.md), com os
  critérios de execução e aceite pendentes; corpo publicado do PR #35 preservado.

### O que deu errado

Nenhuma falha. Não há impedimento técnico novo; a implementação aguarda o marco
humano mantido pelo solicitante. Sem mudanças na aplicação, novos testes,
publicação ou início de E2 nesta decisão.

### Próximo passo

Solicitante executar e conferir o ensaio no ambiente interno controlado. Quando
permitido, retornar somente versão do artefato/contrato, conclusão sanitizada,
lacunas e decisão expressa sobre E1. Retomar E2 após o aceite, mantendo detalhes
e dados originais internos e sem declarar T01/E3 integral por esse marco.

## 2026-10-26 — Registrar pendência E1 e autorizar continuidade técnica E2

Referência: solicitação do usuário para registrar a validação interna E1 ainda
não executada e direcionar E2, alterando apenas a documentação relacionada.
Situação: #8/E1 pendente; início técnico de E2 autorizado; documentação revisada.

### O que foi feito

- A nova decisão substitui a orientação anterior, registrada na mesma data, de
  manter E2 aguardando o aceite E1. A exceção de sequência está centralizada na
  [seção 13.1 do planejamento](docs/planejamento-simulador-tributario.md#131-pendência-e1-e-continuidade-de-e2),
  revisão 1.7. E1 conserva critérios, responsável solicitante e execução no
  ambiente interno controlado, sem data definida ou aprovação presumida.
- Direcionada a próxima implementação para a #9, começando pelas tabelas de
  transição, com equipe própria, revisão, correções delegadas e testes verificados.
  Mantidos os demais critérios e dependências de aceite E1/T01/E3/E4/E5.
- Alinhados ponteiros em AGENTS, README, manifesto e roteiro da #8; atualizado
  somente o registro atual fora do corpo publicado do PR #35. Histórico anterior,
  referência do artefato E1 e cinco critérios PENDENTES do roteiro preservados.
- Grupo8 retomado para esta atualização documental: explorer GPT-6 Luna/médio,
  executor GPT-6.1 Sol/baixo e reviewer GPT-6.1 Sol/médio, identificados. Revisão
  aprovada sem achados materiais. Aplicado `writing-for-agents` ao ajuste de
  AGENTS, com ponteiro curto para a decisão central.
- Verificações estáticas aprovadas: sete referências válidas à seção 13.1,
  versão/data do planejamento, whitespace, cinco critérios E1 PENDENTES e corpo
  publicado do PR #35 idêntico ao anterior. Diff restrito aos sete documentos.
- GitHub consultado somente para leitura: #8 aberta `ready-for-human`; #9 aberta
  `ready-for-agent`, sem comentários e ainda com `Blocked by: #8`. A exceção
  documental autoriza seu início técnico; nenhuma dependência remota foi removida.

### O que deu errado

Nenhuma falha de execução informada; o ensaio E1 segue sem validação real por
indisponibilidade do solicitante. A liberação de desenvolvimento E2 não demonstra
compatibilidade nem encerra E1/T01/E3. Nenhum código, teste, build, dado do piloto,
arquivo de apresentação ou estado do rastreador foi alterado nesta tarefa.

### Próximo passo

Prosseguir em execução posterior com a implementação técnica da #9, dentro do
escopo e ciclo de supervisão autorizados. O solicitante realizará a pendência
#8 internamente e retornará, quando permitido, versão do artefato/contrato,
conclusão sanitizada, lacunas e decisão expressa sobre E1. Reconciliar divergências
do ensaio antes de declarar esse aceite; os demais marcos continuam obrigatórios.

## 2026-10-26 — Iniciar implementação técnica da #9

Referência: pedido do usuário “Dê continuidade a implementação”,
[issue #9](https://github.com/lukadev08/fake-esfinge/issues/9) e exceção de sequência
da seção 13.1 do planejamento. Situação: exploração concluída; transições/desenho
registrados antes do código; #8/E1 permanece pendente.

### O que foi feito

- Preservada em commit local `abad882` a documentação já revisada que autoriza
  E2 técnico com E1 pendente; criada branch `issue-9-processamento-assuntos`.
  `main` remoto permanece `ddf8487`; nenhum push/PR da #9 realizado.
- Issue #9 reconfirmada aberta `ready-for-agent`, sem comentários. Dependência
  remota #8 preservada; exceção do solicitante autoriza este início técnico.
- Novo grupo exclusivo: `grupo9_explorer`, identificado GPT-6 Luna/médio,
  concluiu somente leitura. Supervisor leu arquitetura/contrato/código e inspecionou
  comandos de build/testes; não há CONTEXT.md/ADRs locais nesta árvore, sem recriá-los.
- [Plano da #9](.scratch/issue-9-plan.md) registra tabelas de transição, interfaces
  e fatias TDD. Disparo usa snapshot dos pendentes da chave conforme fonte V6,
  sem seleção implícita de assunto; recebidos posteriores aguardam novo disparo.
  Reutilizados os seams já confirmados em E0 e vocabulário de `codebase-design`.

### O que deu errado

Uma busca inicial citou arquivo de operações inexistente; o pacote foi inventariado
e as rotas/schemas reais consultados, sem criar artefato substituto. Nenhum build,
teste, aplicação ou alteração de código realizado na preparação. Ensaio interno
E1 permanece não executado; não há aceite presumido ou homologação oficial.

### Próximo passo

Executor próprio implementar a #9 em fatias RED→GREEN, sem antecipar finalização,
recuperação/reset/expiração ou regras fiscais. Reviewer independente avaliar,
correções delegadas se necessárias; supervisor conferir suíte limpa e Docker
antes de preparar publicação específica. #8/E1 continua pendente do solicitante.

## 2026-10-26 — Concluir implementação técnica local da #9

Referência: [issue #9](https://github.com/lukadev08/fake-esfinge/issues/9), pedido
“Dê continuidade a implementação” e planejamento §13.1. Situação: implementação
local revisada, testes e Docker finais verdes; publicação específica pendente.

### O que foi feito

- Disparo explícito, snapshot estável de elegíveis, estados/transições persistidos,
  processamento estrutural dos seis assuntos e conjuntos recebidos/processados/
  efetivos distinguíveis. Controle administrativo manual/automático usa a mesma
  porta, Clock compartilhado e configuração persistida. V8 aditiva conserva V1–V7.
- Executor próprio `grupo9_executor` GPT-6.1 Sol/baixo concluiu em TDD, após
  exploração GPT-6 Luna/médio. Reviewer próprio GPT-6.1 Sol/médio encontrou limites
  temporais; correções delegadas a `grupo9_correcao` GPT-6.1 Sol/baixo e depois
  `grupo9_correcao_final`, identificado Codex/GPT-6 sem variante/esforço expostos.
  Reavaliação final aprovou tecnicamente a #9, sem achados materiais remanescentes.
- Supervisor confirmou **137 testes**, zero falhas/erros/ignorados, `clean verify`
  exit0,15.930s. Jar final e imagem Linux amd64 identificados na
  [evidência sanitizada](docs/evidencias/issue-9.md); hash interno do jar conferido.
- **1.094 verificações na imagem final V8**, todas exit0: upgrade real V7 com
  hashes/contagens de dez tabelas preservados, reinício, 10.001 créditos, dados
  exatos/índices/origem, disputa manual/automático e limite de tempo system após
  reabertura. Preparação V7:1.879 verificações +3 do snapshot anterior, separadas.
- README, manifesto, plano e [rascunho do PR](.scratch/pr-issue-9.md) atualizados.
  GitHub #9 reconfirmada aberta `ready-for-agent`, sem comentários; #8 e dependência
  remota preservadas. Nenhum push/PR/merge ou publicação de imagem da #9 realizado.
- Containers temporários removidos, ausência confirmada; três volumes fictícios
  `fake-esfinge-issue9-supervisao`, `fake-esfinge-issue9-supervisao-final` e
  `fake-esfinge-issue9-supervisao-auto` preservados no contexto `orbstack`.

### O que deu errado

- Revisão detectou conclusão temporal inalcançável e avanço aceitando timestamps
  fora do contrato; três testes RED reproduziram os dois P2. Validação do snapshot
  inteiro e do avanço corrigiu ambos. Nova revisão encontrou leitura system que
  podia ultrapassar ano9999 após avanço da base; um RED determinístico comprovou
  o terceiro P2, corrigido pela guarda em cada leitura sem mutar o deslocamento.
- Limite de uso interrompeu a última rodada do primeiro corretor; novo agente
  delegado concluiu a correção, sem perder alterações ou processos. Os ensaios
  preliminares foram excluídos do total final; refeitos upgrade e Docker com jar
  final após nova suíte limpa. Nenhuma falha remanescente observada.
- Ensaio interno E1/#8 e integrador real continuam sem execução/aceite informado.
  A implementação técnica da #9 não conclui E2 integral, E1/T01/E3 ou homologação.

### Próximo passo

Obter autorização específica para publicar/integrar a #9 com o rascunho concreto;
exclusão dos três volumes requer decisão própria. Após integração, prosseguir à
#10 e às demais fatias E2 dentro de suas dependências. Solicitante mantém o ensaio
e aceite humano #8/E1 pendentes, sem data presumida.

## 2026-10-26 — Publicar e integrar #9; limpar ensaios autorizados

Referência: respostas expressas “Autorizar publicação e merge” da #9 e
“Autorizar exclusão dos três volumes”. Situação: PR #36 integrado; #9 completed;
continuidade técnica para #10, com #8/E1 pendente.

### O que foi feito

- Push de `issue-9-processamento-assuntos`, head revisado `5e9e8a4`, criação do
  [PR #36](https://github.com/lukadev08/fake-esfinge/pull/36) com corpo preparado e
  anexo à tarefa. Base remota reconfirmada `ddf8487`; comparação sem divergência
  e PR sem conflitos. Merge com SHA esperado produziu
  `eb376d07fd8efe915d2588cb69e4a0f41b1dcf75`.
- GitHub confirmou PR merged, `main` no merge, #9 closed/completed e #8 aberta.
  Checks/statuses do head vazios, sem CI executado; 137 testes e 1.094 verificações
  Docker verdes continuam evidência local do artefato revisado.
- Após conferir nomes e ausência de containers usuários, excluídos somente os
  volumes `fake-esfinge-issue9-supervisao`, `fake-esfinge-issue9-supervisao-final`
  e `fake-esfinge-issue9-supervisao-auto` no contexto `orbstack`, exit0; ausência
  confirmada. Imagem/clientes preservados. Evidência, manifesto e registro do PR
  atualizados sem alterar o corpo publicado.
- Criada branch local `issue-10-finalizacao` sobre `origin/main` integrado.
  #10 reconfirmada aberta `ready-for-agent`, sem comentários, dependente apenas
  da #9 agora concluída. Iniciada exploração própria para finalização/totalizadores.

### O que deu errado

Nenhuma falha de publicação, merge ou exclusão. Nenhum código mudou desde a suíte
final; testes não repetidos nesta operação documental. Ensaio real e aceite
humano #8/E1, T01/E3 e CI remoto seguem pendentes; merge não comprova esses marcos.

### Próximo passo

Concluir exploração e plano de transições da #10, implementar em equipe própria
com TDD/revisão/supervisão, mantendo publicação da nova fatia sob autorização
específica. Solicitante conserva a pendência interna #8/E1.

## 2026-10-26 — Preparar implementação da #10

Referência: continuação técnica E2 autorizada e
[issue #10](https://github.com/lukadev08/fake-esfinge/issues/10), após merge da #9.
Situação: exploração/plano concluídos antes do código; implementação em seguida.

### O que foi feito

- `grupo10_explorer`, identificado GPT-6 Luna/médio, concluiu leitura do contrato,
  arquitetura, chamadas e persistência. Supervisor leu fontes pertinentes e
  reconfirmou #10 aberta, sem comentários, com #9 completed e #8/E1 pendente.
- [Plano da #10](.scratch/issue-10-plan.md) define17 totalizadores, precondições,
  fechamento/transições, histórico, migração V9 e fatias TDD. Escolhido marcador
  `finalizada_em` com índice parcial, preservando dados V8 e substituindo guardas
  que dependiam só do estado legado. Sem reconstrução desnecessária de tabelas.
- Planejados comparação numérica exata com linhas efetivas, erro409 sem efeitos,
  sucesso200 vazio, consulta histórica sem renovar atividade e nova abertura
  sem cota mensal. Recuperação e operações indisponíveis seguem explicitamente fora.
- Comandos herdados inspecionados; nenhuma dependência/configuração nova necessária.
  .DS_Store preexistentes preservados; nenhuma publicação da #10 autorizada.

### O que deu errado

Nenhuma falha executada. Estado ABERTO restrito pela V2 exigiu definir marcador
persistido e revisar todas as guardas; não foi tratado como impedimento externo.
Nenhum build/teste/código da #10 executado nesta preparação. E1 real permanece pendente.

### Próximo passo

Executor próprio implementar TDD da #10; revisão independente e correções delegadas,
seguindo supervisão de suíte limpa e Docker no artefato final antes da publicação.

## 2026-10-26 — Concluir implementação técnica local da #10

Referência: continuação E2 autorizada e
[issue #10](https://github.com/lukadev08/fake-esfinge/issues/10). Situação: pronta
localmente, revisão/suíte/Docker aprovados; publicação específica pendente.

### O que foi feito

- Executor próprio `grupo10_executor`, identificado GPT-6.1 Sol/baixo, implementou
  finalização com 17 totalizadores estritos, comparação BigInteger com linhas
  efetivas por origem e fechamento/evento/atividade atômicos. V9 aditiva e guardas
  conservam histórico, recusam mutações fechadas e liberam nova chave por competência.
  Recuperação/descartes não foram antecipados.
- Reviewer próprio `grupo10_reviewer` concluiu leitura independente de produção,
  testes/fixtures e migração, sem achados materiais. Papel configurado reviewer
  GPT-6.1 Sol/médio; autodeclarou Codex/GPT-6 sem variante/esforço expostos. Nenhuma
  correção adicional de produção foi necessária; supervisor não editou código.
- TDD nominal comprovou RED 200/422 e GREEN. Executor verify 155 testes verdes,
  exit0,16,413s. Supervisor repetiu `clean verify`: **155 testes**, zero falhas/
  erros/ignorados, exit0,17,517s; 21 relatórios Surefire conferidos e jar identificado
  na [evidência](docs/evidencias/issue-10.md).
- **1.279 verificações na imagem final V9**, todas exit0: quinze tabelas V8
  preservadas, estados em fases distintas retomados, rejeições sem efeitos,
  seis assuntos, 17 campos, 10.001 créditos, concorrência, 200 vazio, consulta histórica
  sem renovar atividade e nova chave após reinício. SQLite confirmou três
  fechamentos/eventos, 21 trabalhos, 10.021 registros/efeitos e10.016 valores TEXT
  com conteúdo/origem exatos. Preparação V8: 538 verificações +3 de snapshot, separadas.
- README, manifesto, plano e [rascunho de PR](.scratch/pr-issue-10.md) atualizados.
  V1–V8/fontes/schemas/build/dependências intactos; .DS_Store preexistentes preservados.
  Container removido e ausência confirmada; volume fictício
  `fake-esfinge-issue10-supervisao` preservado no contexto `orbstack`.

### O que deu errado

- Duas expectativas intermediárias de teste foram corrigidas: token do cenário B
  deve autenticar no serviço B para provar 404 sobre chave A; contagem monetária
  de saldo continuava oito, enquanto a versão de schema avançava para 9.
  Fixtures antigas passaram a usar SQL legado somente em testes para semear
  schemas V2–V8 sem marcador; produção não detecta schema dinamicamente.
- Nenhuma falha remanescente na revisão, suíte ou imagem final. Avisos herdados
  de Mockito/ByteBuddy/JDK e SLF4J dos clientes não falharam validação. Nenhum
  integrador real, aceite #8/E1/T01/E3 ou CI remoto executado; conclusão técnica
  da #10 não encerra E2 integral ou homologação oficial.

### Próximo passo

Obter autorização específica para push/PR/merge da #10 e decisão separada sobre
exclusão do volume fictício. Depois de integrar, prosseguir à #11 dentro das
dependências e supervisão autorizadas. Solicitante mantém #8/E1 pendente.

## 2026-10-26 — Publicar e integrar #10; excluir ensaio autorizado

Referência: autorização expressa de publicação/merge da #10 e exclusão de
`fake-esfinge-issue10-supervisao`. Situação: PR #37 integrado, #10 completed;
continuidade técnica para #11, com #8/E1 pendente.

### O que foi feito

- Push de `issue-10-finalizacao`, head revisado `d47c751`, criação do
  [PR #37](https://github.com/lukadev08/fake-esfinge/pull/37) com corpo preparado
  e anexo à tarefa. Base remota `eb376d0` reconfirmada; comparação de três commits
  sem divergência e PR sem conflitos. Merge com SHA esperado produziu
  `4de2ba5e8c799e388ecd5c6a89bcc1a33d32ff1f`.
- GitHub confirmou PR merged, `main` no merge, #10 closed/completed e #8 aberta.
  Checks/statuses vazios no head, sem CI executado. Evidências locais continuam
  155 testes e 1.279 verificações Docker verdes, sem mudança de código após revisão.
- Conferidos nome exato e ausência de containers usuários; excluído somente o
  volume `fake-esfinge-issue10-supervisao` no contexto `orbstack`, exit0, com
  ausência confirmada. Imagem/clientes preservados. Manifesto, evidência e registro
  do PR atualizados; corpo publicado preservado.
- Criada branch local `issue-11-erro-assincrono` sobre `origin/main` integrado.
  #11 reconfirmada aberta `ready-for-agent`, sem comentários, dependente da #9
  já concluída. Explorer próprio iniciado para erro assíncrono controlado.

### O que deu errado

Nenhuma falha de publicação, merge ou exclusão. Um trecho duplicado durante a
edição documental foi removido antes de validar/commitar. Código não mudou;
testes não repetidos nesta operação. Ensaio real #8/E1/T01/E3 e CI remoto pendentes.

### Próximo passo

Concluir exploração e tabelas de decisão da #11; implementar com equipe própria,
TDD, revisão independente e supervisão do artefato final. Publicação da nova fatia
continua exigindo autorização específica; #8/E1 permanece com o solicitante.

## 2026-10-26 — Preparar implementação da #11

Referência: continuação técnica E2 autorizada e
[issue #11](https://github.com/lukadev08/fake-esfinge/issues/11). Situação: exploração
e tabelas de decisão concluídas antes do código; implementação em seguida.

### O que foi feito

- Explorer próprio `grupo11_explorer`, papel configurado GPT-6 Luna/médio, leu
  contrato/arquitetura/código. Corrigiu variante inicial inferida sem evidência.
  Supervisor definiu [plano da #11](.scratch/issue-11-plan.md) com falha administrativa
  por lote/índice, diagnóstico contratual e propagação atômica do mesmo assunto.
- Decisões derivadas: falhar na conclusão sem commit parcial por registro;
  conservar payload/hash como histórico, remover dados ativos e marcar lote inativo;
  abortar seguintes não concluídos, inclusive EM_PROCESSAMENTO/fora do snapshot.
  Outros assuntos/anteriores válidos permanecem; finalização considera só ativos.
- Plano administrativo por lote/geração, imutável após início, separado do payload.
  V10 preservará dados V9; reconstrução necessária restrita à tabela de trabalhos.
  Reforçada ordem de exclusão das FKs e revalidação contra conclusão tardia.
- Comandos existentes inspecionados, sem dependência/configuração novas. #11 aberta,
  sem comentários; #9 concluída. #8/E1/T01/E3 e publicação da #11 pendentes.

### O que deu errado

Nenhuma execução de código/build/testes nesta preparação. Lacunas de API própria,
arquivo histórico e ciclo do plano foram resolvidas por decisões documentadas do
simulador, sem atribuir equivalência oficial. Identificação incorreta inicial do
explorer foi corrigida; nenhuma alteração de fonte original ou dado real.

### Próximo passo

Executor próprio implementar em fatias TDD, seguido de revisão independente,
correções delegadas e supervisão de suíte limpa/Docker sobre artefato final.

## 2026-10-27 — Concluir implementação técnica local da #11

Referência: continuação E2 autorizada e
[issue #11](https://github.com/lukadev08/fake-esfinge/issues/11). Situação: pronta
localmente, revisão/suíte/Docker finais verdes; publicação específica pendente.

### O que foi feito

- `grupo11_executor` implementou plano por lote/índice/geração, imutável após início,
  falha/descarte do alvo e aborto atômico dos seguintes do mesmo assunto, inclusive
  EM_PROCESSAMENTO/fora do snapshot. Inspeção e diagnóstico distinguem histórico
  e dados ativos removidos; finalização usa só ativos. V10 conserva V9/bytes/FKs.
- Equipe própria com explorer, executor, reviewer e corretor separados. Papéis
  configurados GPT-6 Luna/médio, GPT-6.1 Sol/baixo, GPT-6.1 Sol/médio e GPT-6.1
  Sol/baixo; runtime não inferido quando indisponível. Reviewer apontou P2 de
  fronteira DTO HTTP/núcleo; `grupo11_correcao` mapeou explicitamente respostas.
  Mesmo reviewer aprovou depois, sem achados materiais remanescentes.
- TDD comprovou RED 404/200 e GREEN. Executor verify 186 verde (19,947s); corretor
  verify 186 verde (19,833s). Supervisor clean verify: **186 testes**, zero falhas/
  erros/ignorados, exit0,21,608s; 22 relatórios conferidos. Jar/imagem identificados
  na [evidência sanitizada](docs/evidencias/issue-11.md).
- **1.555 verificações na imagem final V10**, todas exit0: upgrade V9 real com
  quinze tabelas preservadas, plano e terminais após reinícios, primeiro/intermediário/
  último, diagnóstico 200/null, finais só de ativos, conteúdo/histórico exato e
  disputa manual/automático sem duplicação. SQLite confirmou 26 controles, 16 ativos,
  dez inativos, 10.016 processados/efetivos e 10.015 valores TEXT, com origem/FKs/
  sequências preservadas. Preparação V9: 105 verificações + 3 de snapshot, separadas.
- README, manifesto, plano e [rascunho do PR](.scratch/pr-issue-11.md) atualizados.
  V1–V9/fontes/schemas/build/dependências intactos, .DS_Store preservados. Nenhum
  código mudou após a suíte final; supervisor escreveu só registros e clientes
  temporários de ensaio, sem alterar produção/testes.
- Containers removidos, ausência confirmada; dois volumes fictícios
  `fake-esfinge-issue11-supervisao` e `fake-esfinge-issue11-supervisao-auto`
  preservados no contexto `orbstack`. #11 aberta sem comentários; #8 aberta
  `ready-for-human`. #12 aberta, dependente de #10 concluída e #11 ainda pendente.

### O que deu errado

- Revisão encontrou objetos do núcleo diretamente nos DTOs de plano/diagnóstico.
  Correção delegada preservou JSON e null explícito; testes comportamentais
  existentes foram reutilizados, sem novo teste estrutural espelhando records.
- Nenhuma falha funcional remanescente na suíte ou imagem final. Avisos herdados
  de Mockito/ByteBuddy/JDK e SLF4J dos clientes não impediram validação. Injeção
  não comprova regra fiscal T04; #8/E1/T01/E3 e CI remoto não executados.

### Próximo passo

Obter autorização específica para publicar/integrar a #11 e decisão separada sobre
exclusão dos dois volumes fictícios. Após integração, prosseguir à #12 com equipe
própria, dependências e supervisão. Solicitante mantém validação interna #8/E1.

## 2026-10-27 — Publicar e integrar #11; excluir ensaios autorizados

Referência: autorização expressa de publicação/merge da #11 e exclusão dos volumes
`fake-esfinge-issue11-supervisao`/`fake-esfinge-issue11-supervisao-auto`.
Situação: PR #38 integrado, #11 completed; continuidade técnica para #12.

### O que foi feito

- Push de `issue-11-erro-assincrono`, head revisado `1b435cf`, criação do
  [PR #38](https://github.com/lukadev08/fake-esfinge/pull/38) com corpo preparado e
  anexo à tarefa. Base `4de2ba5` reconfirmada; comparação de três commits sem
  divergência e PR sem conflitos. Merge protegido pelo SHA esperado produziu
  `075236636077093b4493952af0c4951824689433`.
- GitHub confirmou PR merged, `main` no merge, #11 closed/completed e #8 aberta.
  Checks/statuses vazios no head: resultados verdes continuam evidência local
  (186 testes, 1.555 verificações Docker), sem CI executado.
- Após conferir nomes exatos e ausência de containers usuários, excluídos somente
  os dois volumes autorizados no contexto `orbstack`, exit0, com ausência
  confirmada. Imagem/clientes preservados. Manifesto, evidência e registro de PR
  atualizados sem alterar corpo publicado.
- Criada branch local `issue-12-descarte-assunto` sobre `origin/main` integrado.
  #12 aberta `ready-for-agent`, sem comentários, dependências #10/#11 concluídas.
  Explorer próprio iniciado para descarte de assunto e reenvio na mesma chave.

### O que deu errado

Nenhuma falha de publicação, merge ou exclusão. Código não mudou desde a suíte
final; testes não repetidos nesta operação documental. #8/E1/T01/E3 e CI remoto
seguem pendentes; integração da #11 não conclui E2 integral ou homologação.

### Próximo passo

Concluir exploração e tabelas de decisão da #12; implementar em equipe própria
com TDD/revisão/supervisão e correções delegadas. Publicação da nova fatia continua
sob autorização específica; solicitante mantém pendência interna #8/E1.

## 2026-10-27 — Preparar implementação da #12

Referência: continuação técnica E2 autorizada e
[issue #12](https://github.com/lukadev08/fake-esfinge/issues/12). Situação: exploração
e decisões/transições concluídas antes do código; implementação em seguida.

### O que foi feito

- `grupo12_explorer`, papel configurado GPT-6 Luna/médio, concluiu somente leitura.
  Supervisor leu contrato/fonte V6/arquitetura/código e registrou
  [plano da #12](.scratch/issue-12-plan.md), reconferido pelo explorer sem conflito.
- Decisões: guarda global de ciclo ativo na chave (RECEBIDO agendado/EM), exceção
  derivada para RECEBIDO sem trabalho, sucesso preservado quando inativado e
  históricos anteriores intactos. Sucesso vazio/repetição de assunto suportado
  segue fonte, com atividade/evento próprios e sem promessa de idempotência oficial.
- Operação permanece no módulo remessas via Processamento, reutilizando V10 e
  menor remoção JDBC, sem migração/infraestrutura novas. Reenvio conserva chave
  com IDs próprios; finalização só de ativos. Cancelamento/rota de lote/falha de
  resposta perdida continuam fora do escopo.
- #12 aberta sem comentários, dependências #10/#11 concluídas, #8/E1 pendente.
  Comandos de build/teste herdados inspecionados; nenhuma publicação da #12 autorizada.

### O que deu errado

Nenhuma execução de código/build/teste nesta preparação. A chave de schema foi
inventariada como `descarteQuery`, evitando supor outra definição. Lacunas de
RECEBIDO/atividade/estado histórico foram resolvidas a partir da fonte/E0 e
marcadas como decisões derivadas do simulador, sem equivalência oficial.

### Próximo passo

Executor próprio implementar em fatias TDD, revisão independente, correções
delegadas e supervisão de suíte limpa/imagem final antes da publicação específica.

## 2026-10-27 — Concluir implementação técnica local da #12

Referência: continuação técnica E2 autorizada e
[issue #12](https://github.com/lukadev08/fake-esfinge/issues/12). Situação: pronta
localmente, revisão/suíte/Docker finais verdes; publicação específica pendente.

### O que foi feito

- Equipe própria com `grupo12_explorer`, `grupo12_executor` e
  `grupo12_reviewer`: papéis configurados GPT-6 Luna/médio, GPT-6.1 Sol/baixo e
  GPT-6.1 Sol/médio; variante efetiva não inferida quando indisponível. Plano e
  transições registrados antes do código; revisão independente aprovada sem
  achados materiais. Não houve correção de produção após revisão.
- POST de descarte por assunto, query/corpo estritos e200 vazio. Transação
  IMMEDIATE remove só dados ativos selecionados, preserva chave/outros assuntos/
  originais/controles, mantém sucesso histórico e permite reenvio com novos IDs.
  Guarda global da chave rejeita agendado/EM, inclusive de outro assunto. Limpeza
  de RECEBIDO sem trabalho é aproximação explícita de E0, sem resposta perdida.
- TDD RED200/422 e GREEN comprovados. Executor verify:212 testes verdes, exit0,
  26,814s; supervisor clean verify: **212 testes**, zero falhas/erros/ignorados,
  exit0,25,328s,24 relatórios conferidos. HTTP/JDBC cobrem seis assuntos, rollback
  statement/COMMIT, isolamento, concorrência entre instâncias e reinício.
- **1.524 verificações na imagem final V10**, todas exit0:21 de preservação V10,
  850 de recuperação,393 após reinício,99 SQLite somente leitura e161 automáticas.
  Preparação com imagem #11:259+3 separadas. SQLite confirmou28 lotes/16 ativos/
  12 inativos,10.015 processados/efetivos e10.025 valores TEXT exatos, conteúdo/
  origem/FKs/high-water íntegros. Jar/imagem identificados na
  [evidência sanitizada](docs/evidencias/issue-12.md).
- README, manifesto, plano e [rascunho do PR](.scratch/pr-issue-12.md) atualizados.
  V1–V10/fontes/schemas/build/dependências intactos; .DS_Store preservados. Nenhum
  código/teste mudou após a suíte final. Containers removidos, ausência confirmada;
  três volumes fictícios preservados em `orbstack`, sem exclusão autorizada.
- `grupo12_docreviewer`, configurado GPT-6.1 Sol/médio, aprovou evidência,
  manifesto e rascunho sem achados materiais; reconferiu relatórios/hash local,
  sem Maven/Docker ou edições. Variante efetiva não exposta.
- GitHub reconfirmou PR #38 merged e #12 aberta `ready-for-agent`, sem comentários,
  dependências #10/#11 concluídas. #8/E1 continua pendente com o solicitante.

### O que deu errado

- Fixtures iniciais corrigidas pelo executor: inspeção inclui eventos da remessa
  inteira e V10 tem16 tabelas incluindo sqlite_sequence. Sem mudança dinâmica
  em produção. RED e falhas intermediárias documentados na evidência.
- Saída da primeira execução Docker não ficou integralmente disponível; checkpoint
  confirmou conclusão. Comparador anterior reaplicado à base já modificada acusou
  diferença esperada. Repetidos preparação/snapshot/ensaio em volume independente,
  com saída completa e sem mudar aplicação; só a repetição entra no total final.
- Nenhuma falha funcional remanescente na suíte/imagem final. Avisos herdados
  Mockito/ByteBuddy/JDK/SLF4J não impediram validação. Integrador real, #8/E1,
  T01/E3, CI remoto e homologação não executados.

### Próximo passo

Obter autorização específica para push/PR/merge da #12 e decisão separada sobre
exclusão dos três volumes fictícios. Após integração, ler dependências/escopo da
próxima issue e continuar com equipe própria/supervisão. Solicitante mantém o
ensaio e aceite humano #8/E1; E2 completo não é concluído por esta fatia.

## 2026-10-27 — Publicar e integrar #12; excluir ensaios autorizados

Referência: autorização expressa de publicação/merge da #12 e exclusão dos três
volumes fictícios identificados na pergunta. Situação: PR #39 integrado,
#12 completed; continuidade técnica para #13.

### O que foi feito

- Push de `issue-12-descarte-assunto`, head revisado `a863140`, criação do
  [PR #39](https://github.com/lukadev08/fake-esfinge/pull/39) com corpo preparado e
  anexo à tarefa. Base `0752366` reconfirmada; comparação de três commits sem
  divergência, PR sem conflitos e merge protegido pelo head esperado.
  Merge: `f2d7e58aaaf1fd1021c6b05f9471560046963baa`.
- GitHub confirmou PR merged, `main` no merge, #12 closed/completed e #8 aberta.
  Check-runs/statuses vazios no head: resultados verdes continuam evidência local
  (212 testes, 1.524 verificações Docker), sem CI executado.
- Após conferir nomes exatos e ausência de containers usuários, excluídos somente
  `fake-esfinge-issue12-supervisao`, `fake-esfinge-issue12-supervisao-repro` e
  `fake-esfinge-issue12-supervisao-auto` no contexto `orbstack`, exit0 e ausência
  confirmada. Imagem/clientes preservados; corpo publicado não alterado.
- Branch local `issue-13-cancelamento-envio` criada sobre `origin/main` integrado.
  #13 aberta `ready-for-agent`, sem comentários, dependências #10/#12 concluídas.
  Explorer próprio iniciado para cancelamento integral/nova chave.

### O que deu errado

Nenhuma falha de publicação, merge ou exclusão. Código não mudou desde a suíte
final; testes não repetidos nesta operação documental. #8/E1/T01/E3 e CI remoto
seguem pendentes; integração da #12 não conclui E2 integral ou homologação.

### Próximo passo

Concluir exploração/tabelas de transição da #13 e implementar com equipe própria,
TDD/revisão/supervisão e correções delegadas. Publicação da nova fatia exige
autorização específica; solicitante mantém a pendência interna #8/E1.

## 2026-10-27 — Preparar implementação da #13

Referência: continuação técnica E2 autorizada e
[issue #13](https://github.com/lukadev08/fake-esfinge/issues/13). Situação: exploração
e decisões/transições concluídas antes do código; implementação em seguida.

### O que foi feito

- `grupo13_explorer`, configurado GPT-6 Luna/médio, concluiu somente leitura,
  variante efetiva não exposta. Supervisor leu contrato/planejamento/arquitetura/
  schema/código conectado e definiu [plano da #13](.scratch/issue-13-plan.md).
- Cancelamento integral permite trabalho agendado/EM e invalida resultados
  tardios; conserva sucesso histórico e usa ABORTADO/diagnóstico próprio para
  pendentes. Cancelada difere de finalizada por timestamp próprio, sem atribuir
  conclusão aos dados removidos. Nova chave na mesma competência é independente.
- V11 necessária: coluna aditiva de cancelamento/índice aberto e reconstrução
  restrita dos trabalhos para ABORTADO, preservando todas as colunas/diagnósticos/
  IDs/sequências/FKs. V1–V10/fontes/schemas intactos; sem infraestrutura nova.
- Caso de uso permanece em Processamento, reutiliza remoção/terminal/transações
  e audita todos os chamadores/guardas. Não altera geração global do cenário.
  #13 aberta, sem comentários, dependências #10/#12 concluídas; #8/E1 pendente.

### O que deu errado

Nenhuma execução de código/build/teste nesta preparação. Estado legado ABERTO
e ausência de ABORTADO no schema exigiram decisão explícita. Reutilizar
finalizada_em para cancelamento confundiria conclusão; definida migração mínima
com marcador distinto, sem reconstrução de remessas ou enum externo novo.

### Próximo passo

Executor próprio implementar por fatias TDD, reviewer independente e correções
delegadas quando necessárias; supervisão de suíte limpa e Docker sobre artefato
final. Publicação da #13 continua sob autorização específica.

## 2026-10-27 — Concluir implementação técnica local da #13

Referência: continuação técnica E2 autorizada e
[issue #13](https://github.com/lukadev08/fake-esfinge/issues/13). Situação: pronta
localmente, revisão/suíte/Docker finais verdes; publicação específica pendente.

### O que foi feito

- Equipe própria com `grupo13_explorer`, `grupo13_executor` e
  `grupo13_reviewer`: configurados GPT-6 Luna/médio, GPT-6.1 Sol/baixo e
  GPT-6.1 Sol/médio; variante efetiva não inferida quando indisponível. Plano
  anterior ao código reconferido pelo explorer; revisão independente aprovada
  sem achados materiais, sem correção de produção posterior.
- POST de cancelamento integral, query/corpo estritos e200 vazio, inclusive
  agendado/EM. Transação fecha chave e remove/inativa dados ativos; sucesso
  histórico permanece e pendentes viram ABORTADO/diagnóstico próprio/null.
  Nova chave na mesma competência permite reenvio integral. Guardas de todos
  os chamadores e gravação tardia verificam cancelamento sem mudar geração global.
- V11 aditiva em remessas/índice e reconstrução restrita dos trabalhos preserva
  todas as colunas/diagnósticos/IDs/seq/FKs. Finalização distinta de cancelamento;
  históricos e demais contextos intactos. V1–V10/fontes/schemas/build/deps intactos.
- TDD RED200/422 e GREEN comprovados.115 focais verdes; executor verify243,
  exit0,27,730s. Supervisor clean verify: **243 testes**, zero falhas/erros/
  ignorados, exit0,29,569s;26 relatórios conferidos. HTTP/JDBC cobrem seis
  assuntos, rollback statement/COMMIT, concorrência e cálculo pausado fora da
  transação, além de high-water acima de MAX mesmo em tabela vazia.
- **1.807 verificações na imagem final V11**, todas exit0:22 upgrade V10,
  838 cancelamento/reenvio,648 após reinício,112 SQLite somente leitura e187
  automáticas. Preparação V10 com imagem #12:270+3 separadas. SQLite confirmou
  33 lotes/18 ativos/15 inativos,10.017 processados/efetivos e10.021 valores
  TEXT exatos; originais/diagnósticos/planos/FKs/high-water preservados.
  Jar/imagem identificados na [evidência sanitizada](docs/evidencias/issue-13.md).
- README, manifesto, plano e [rascunho do PR](.scratch/pr-issue-13.md) atualizados.
  Nenhum código/teste mudou após a suíte final. Containers removidos, ausência
  confirmada; três volumes fictícios preservados em `orbstack`, sem exclusão autorizada.
- Mesmo reviewer aprovou consolidação documental sem achados materiais;
  reconferiu26 relatórios/243 testes, hash do jar e88 links locais, sem Maven/
  Docker ou edições. Resultados finais e limites consistentes.
- #13 aberta sem comentários, dependências #10/#12 concluídas. Próxima fatia #14
  aberta, sem comentários, depende da #12 concluída; #8/E1 continua pendente.

### O que deu errado

- Executor corrigiu somente fixtures: helper inexistente antes do RED, totais
  incompletos/ordem dos ciclos e expectativa na corrida com segundo cancelamento
  vencedor. Falhas intermediárias constam na evidência; produção permaneceu a
  mesma desde GREEN inicial.
- Primeira revisão interrompida por limite de uso; consulta fresca indicou uso
  normal disponível e retomada concluiu aprovada, sem trocar modelo ou usar reset.
- Cliente Docker temporário confundiu quantidade de trabalhos com maior ID;
  fluxo concluído, asserção final incorreta. Leitura direta confirmou high-water117,
  novo119 e SQLite íntegro. Corrigido só cliente; preparação/snapshot/ensaio
  repetidos em volume novo, sem mudança de aplicação/testes. Primeira execução
  e sondagens não entram no total final. Compilação do cliente também ajustou
  assinatura de helper, sem efeito no artefato.
- Nenhuma falha funcional remanescente na suíte/imagem final. Avisos herdados
  Mockito/ByteBuddy/JDK/SLF4J não impediram validação. #8/E1, integrador T01/E3,
  CI remoto e homologação não executados.

### Próximo passo

Obter autorização específica para push/PR/merge da #13 e decisão separada sobre
exclusão dos três volumes fictícios. Após integrar, prosseguir para #14 com equipe
própria/supervisão e suas dependências; solicitante mantém o ensaio/aceite #8/E1.
E2 integral não é concluído por esta fatia.

## 2026-10-27 — Integrar #13, excluir ensaios e encerrar a pedido

Referência: autorização expressa de push/PR/merge da #13, exclusão dos três
volumes fictícios e orientação para parar após a #13 e resumir o trabalho.
Situação: PR #40 integrado, #13 completed; implementação encerrada, #14 não iniciada.

### O que foi feito

- Push de `issue-13-cancelamento-envio`, head revisado `c3714b9`, criação do
  [PR #40](https://github.com/lukadev08/fake-esfinge/pull/40) com corpo preparado e
  anexo à tarefa. Base `f2d7e58` reconfirmada; comparação de três commits sem
  divergência, PR sem conflitos e merge protegido pelo head esperado.
  Merge: `566afda0c3ac9c6f1f623d752407616978b5cd93`.
- GitHub confirmou PR merged, `main` no merge e #13 closed/completed. Inventário
  confirmou #1–#7/#9–#13 encerradas; #8 e #14–#27 abertas. Check-runs/statuses
  vazios no head:243 testes/1.807 verificações Docker são evidência local, sem CI.
- Conferidos nomes exatos e ausência de containers usuários; excluídos somente
  `fake-esfinge-issue13-supervisao`, `fake-esfinge-issue13-supervisao-repro` e
  `fake-esfinge-issue13-supervisao-auto` no contexto `orbstack`, exit0 e ausência
  confirmada. Imagem/clientes preservados; corpo publicado não alterado.
- Checkout alinhado ao merge por fast-forward, sem branch/tarefa #14. Registros
  de publicação/limpeza atualizados e [resumo](.scratch/resumo-ate-issue-13.md)
  preparado localmente, sem nova publicação documental. Nenhum código/teste mudou.
- Orientação mais recente de parar após a #13 substitui a continuação anterior
  para #14; aprovação de publicação da #13 não foi interpretada como revogação
  dessa parada. Nenhuma implementação da #14 foi iniciada.
- Verificação documental local:94 links sem destino ausente, whitespace válido
  e corpo publicado do PR preservado. Resumo e registros finais não publicados.

### O que deu errado

Nenhuma falha de publicação, merge ou exclusão. Não foram repetidos testes para
o encerramento documental; código validado permanece o mesmo. #8/E1/T01/E3 e
CI remoto continuam pendentes; não há homologação ou conclusão integral do MVP.

### Próximo passo

Entregar o resumo e parar. Aguardar nova orientação antes de retomar implementação.
O solicitante mantém o ensaio/aceite interno #8/E1 pendente, sem data estabelecida.
