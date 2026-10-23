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
