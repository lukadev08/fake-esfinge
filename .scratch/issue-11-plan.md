# Preparação e decisões — issue #11

Referência: [issue #11](https://github.com/lukadev08/fake-esfinge/issues/11), aberta
`ready-for-agent`, sem comentários; #9 concluída. Branch `issue-11-erro-assincrono`,
base funcional `4de2ba5` (PR #37/#10), registro documental `1a371ed`.
E2 técnico autorizado, #8/E1 pendente conforme planejamento §13.1.
Publicação/merge da #11 não autorizados.

Explorer próprio `grupo11_explorer`, papel configurado GPT-6 Luna/médio, concluiu
somente leitura. Corrigiu identificação inicial de modelo que havia inferido sem
evidência. Supervisor leu planejamento §§7–8/13.1, arquitetura de executor/SQLite,
contrato E0, schemas de consulta/erro e implementação conectada. Seams HTTP,
administração e SQLite real já confirmados em E0; integrador real permanece pendente.

## Escolhas derivadas do simulador

- Falha controlada ocorre na conclusão do lote-alvo, depois de EM_PROCESSAMENTO.
  O índice é localização do diagnóstico, não autorização de commit parcial dos
  registros anteriores. Todo o lote com erro é descartado atomicamente, coerente
  com a conclusão por lote existente. Não implementar CON ou validação tributária.
- “Seguintes” usa `lotes.ordem` maior, filtrada por mesma remessa/UG/cenário/assunto.
  Não reordenar o motor global por catálogo fiscal. Preservar anteriores válidos,
  quaisquer posteriores já concluídos e outros assuntos/envios. Abortam seguintes
  ainda não concluídos RECEBIDO/EM_PROCESSAMENTO, inclusive sem trabalho agendado
  por terem chegado depois do snapshot.
- Uma transação revalida estado/geração/abertura e aplica erro do alvo e todos
  os abortos selecionados, diagnósticos, dados ativos e eventos. Não confirmar o
  alvo e deixar a propagação para outro commit. Revalidar depois de cálculo fora
  da transação; trabalho abortado não pode concluir tardiamente.

## Plano administrativo mínimo

GET/PUT/DELETE `/__simulator/falhas/processamento/{numeroLote}`, protegido por
`X-Simulator-Key` do cenário atual, sem `auth_token` como substituto. GET sem corpo
ou query; PUT JSON UTF-8 exato `{"registro": 1}` ou `{"registro": null}`; DELETE
sem corpo/query. Respostas GET/PUT:200 com `numeroLote` e `registro` (null explícito);
DELETE200 vazio. API própria derivada, sem parâmetros no payload do integrador.

Alvo explícito positivo e existente; outro cenário ou plano inexistente:404.
Índice localizado exige inteiro exato >=0 e menor que a quantidade original do
lote; ausente/tipo/overflow/extra/transporte inválidos:400, sem coerção. `null`
significa diagnóstico sem localização, não ausência do campo.

PUT/DELETE só enquanto remessa aberta e lote ativo não iniciado (sem trabalho ou
trabalho RECEBIDO). EM_PROCESSAMENTO/sucesso/terminal/chave fechada:409 sem efeitos.
PUT permite corrigir índice antes de iniciar; DELETE remove plano ainda não usado,
404 se ausente. Configuração não renova atividade do envio. Plano e seu evento
persistem atomicamente por lote/geração; GET apenas lê. Início e alteração do plano
disputam a mesma transação/guardas, tornando o plano imutável quando o lote começa.
Reinício preserva configuração; geração anterior não aplica plano à atual.
Sem mensagens arbitrárias, regex de payload ou sorteio/infraestrutura novos.

## Tabela de transições anterior ao código

| Condição / ação | Resultado persistido e observável |
| --- | --- |
| Alvo válido configurado, ainda sem disparo | Plano persistido, lote RECEBIDO; nada processa |
| Disparo/etapa inicial | Mesmo agendamento existente; alvo chega a EM_PROCESSAMENTO |
| Conclusão do alvo com plano da geração | LOTE_DESCARTADO, ativo0, diagnóstico SIM_ERRO_INJETADO, registro configurado/null; remover dados ativos e registrar evento |
| Seguintes não concluídos do mesmo assunto | ABORTADO_ERRO_LOTE_ANTERIOR, ativo0, diagnóstico próprio SIM_ABORTADO_ERRO_LOTE_ANTERIOR com registro null; dados ativos removidos, controle/evento conservados |
| Anteriores válidos / posteriores já concluídos / outros assuntos | Conteúdo/estado/efeitos preservados; pendentes de outros assuntos seguem o motor |
| Consultar terminais | HTTP 200, quatro campos contratuais mais retornoErros.erros com codigo/mensagem/registro; nenhum avanço de tarefa/relógio |
| Consultar estados bons | Exatamente o envelope atual, sem retornoErros nem campo null extra |
| Inspecionar removido | Ativo false; contagens/listas ativas vazias; originais históricos, hash/bytes/diagnóstico/eventos distinguíveis |
| Disparar só históricos / finalizar sem ativos | 409 sem sucesso vazio |
| Finalizar com ativos válidos restantes | Comparar somente ativos em sucesso e efetivos; históricos descartados/abortados não bloqueiam |
| Reiniciar antes/depois de erro | Retoma fase persistida; terminais não reexecutam, efeitos não reaparecem |
| Falhar statement/COMMIT no erro ou propagação | Rollback de todo o grupo, incluindo exclusões/estado/eventos; retomada possível |

Mensagens são próprias, fixas e sanitizadas. `retornoErros.erros` respeita
`$defs.erroLote`: registro zero-based ou null **presente**, sem campos extras.
Não adicionar esse envelope às respostas RECEBIDO/EM_PROCESSAMENTO/PROCESSADO_SUCESSO.
Consulta histórica de remessa aberta mantém política existente de atividade/evento;
remessa fechada continua sem renovar atividade. Consulta nunca avança processamento.

## Persistência, inspeção e fronteiras

1. Manter a capacidade no módulo `remessas`; domínio/casos de uso Java puro,
   portas para plano/transição e adaptadores HTTP/JDBC. Agendador só aciona porta.
   Serviço decide que a falha configurada termina o lote; SQL aplica operação
   estreita de descarte/aborto com o escopo definido, sem lógica fiscal.
2. V10 aditiva em `lotes`: marcador `ativo` default1, check0/1. Preservar payload,
   hash, quantidade original e vínculos como arquivo histórico imutável. Remover
   linhas ativas na ordem FK: registros_efetivos → registros_processados →
   valores_recebidos. Não apagar lote, sessão, remessa, evento ou bytes originais.
3. V10 pode reconstruir **somente processamento_trabalhos**, ampliando CHECK para
   os dois novos terminais e persistindo diagnóstico. Nenhuma FK aponta ao seu id;
   preservar IDs, ordem, instante/latência, geração, high-water e índice devido.
   Abortado sem trabalho recebe controle terminal sem executar processamento.
   Plano por lote/geração em tabela própria, com vínculo composto e índice único.
4. Não duplicar payload em outra tabela nem reconstruir todas as tabelas filhas.
   Inspeção ativa mantém campos atuais; inativa usa recebidos0/registros vazios e
   campos explícitos `recebidosHistoricos`/`registrosHistoricos` com original completo.
   `bytesBase64`/hash continuam originais, documentados como arquivo histórico.
   `ativo` e diagnóstico explicam a diferença; contagens processadas/efetivas são
   linhas reais. Nenhuma soma monetária aproximada ou deduplicação.
5. Atualizar active/eligible/next/current, conclusão e finalização para filtrar
   ativo e estados pendentes; terminais são inertes. Finalização conta somente
   lotes ativos e seus efetivos, sem controles históricos. Não implementar rotas
   de descarte por assunto/cancelamento/recuperação/reset, nem bloqueio tributário.
6. V1–V9/fontes/schemas/build/dependências intactos. Preservar dados V9, inclusive
   remessas fechadas, sessões, config/offset, resultados e trabalhos em fases
   distintas. Falha V10 reverte integralmente e checksum incompatível é recusado.
   Ajustar fixtures antigas somente em testes, com menor semeadura SQL legada
   necessária; nunca detectar schema dinamicamente em produção.

## Fatias TDD e supervisão

- Primeiro RED→GREEN HTTP vertical: configurar alvo intermediário por administração,
  disparar/avançar, consultar200 com descarte/diagnóstico e aborto seguinte;
  conferir dados ativos versus arquivo histórico, anteriores e outro assunto.
- Plano estrito: auth, cenário, índice0/último/null/fora/negativo/tipo/duplicatas,
  PUT/DELETE antes do início e 409 depois, reinício e geração. Nenhuma alteração
  de payload, atividade ou dados em rejeições; GET não avança nem grava evento.
- Primeiro/intermediário/último lote, vários assuntos/UGs/envios, lotes posteriores
  fora do snapshot ou EM_PROCESSAMENTO, precedentes/sucessos preservados; índice
  nãozero em lote com múltiplos registros e valores exatos. Repetibilidade lógica.
- SQLite real: atomicidade do grupo, falhas de statement e COMMIT após exclusões,
  concorrência manual/automático/duas instâncias e guardas contra efeito tardio;
  migração V9 preenchida, rollback V10, FKs/sequências/checksums e reabertura.
- Regressões de seis assuntos, finalização só de ativos e sem ativos409; inspeção
  mantém bytes/identificadores/decimais históricos sem expor segredos em erro/log.
- Executor próprio → reviewer independente → correções delegadas se necessárias.
  Supervisor clean verify e imagem Linux amd64 sem rede, upgrade real V9/reinício,
  HTTP/SQLite e caminho automático pela mesma porta. Apenas dados fictícios.

Comandos herdados inspecionados (pom/wrapper/Dockerfile), Java21/Maven3.9.9 sem
dependências novas. Cache Maven local, SQLite temporário, sem produção/TCE.
Focais `-Dtest=... test`; um verify do executor e clean verify do supervisor,
sem Maven concorrente; RTK/proxy, exit codes/diagnósticos/handles preservados.
Manifesto marca injeção como aproximação e T04 fiscal não implementado. Sem aceite
humano #8/E1, T01/E3, recuperação integral ou homologação por estes ensaios.

## Resultado local

Implementação concluída. Reviewer encontrou P2 de objetos do núcleo usados em
DTOs HTTP; corretor separado criou mapeamentos explícitos no adaptador. Mesmo
reviewer aprovou a correção e o conjunto, sem achados materiais remanescentes.
Supervisor confirmou 186 testes no clean verify (21,608s) e 1.555 verificações na
imagem final V10, incluindo upgrade V9 real, reinícios, conteúdo/ativos/histórico
e execução automática/disputa manual. Preparação V9: 105 verificações + 3 de snapshot,
separadas. [Evidência completa](../docs/evidencias/issue-11.md) com hashes e limites.

Containers removidos; dois volumes fictícios preservados até autorização específica.
[Rascunho do PR](pr-issue-11.md) pronto; nenhum push/PR/merge da #11. #8/E1/T01/E3
seguem pendentes. Após integração, #12 depende de #10 e #11 para recuperação por
descarte de assunto/reenvio; não implementada nesta fatia.
