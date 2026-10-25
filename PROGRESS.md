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
