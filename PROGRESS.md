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
