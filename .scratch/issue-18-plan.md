# Plano da issue #18 — inspeção e relatório

Data: 28/10/2026. Situação: aprovado com ajuste de eventos reais; implementação/validação/revisão independente aprovadas; clean verify343verde, imagem final45verificações verdes; conferência documental final aprovada sem achados materiais.
Base: branch `codex/issue-18-inspecao-relatorio`, HEAD `c9895f1`; #17 integrada.
Fonte: issue #18, planejamento §§3–15, contrato E0, ARCHITECTURE.md.
A exceção §13.1 permite E2 técnico; #8/E1 e T01/E3 continuam humanos/pendentes.

## Contrato administrativo proposto

Todas as rotas exigem `X-Simulator-Key`. Autorização precede validação de
query/identificadores. Nenhuma leitura expira remessa, renova atividade, avança
relógio/processamento, cria configuração, trabalho ou evento. Transação de leitura
coordena geração e projeções com reset; não usa `RemessaService.consult`.

| GET | Query aceita | Resultado |
|---|---|---|
| `/__simulator/remessas` | `cenario`, `codigoUg`, `anoMes`, `assunto`, `limite`, `cursor` | Página de metadados de remessas, sem payload |
| `/__simulator/lotes` | mesmos filtros | Página de metadados, estado/diagnóstico/ativo/contagens/hash, sem bytes/listas |
| `/__simulator/remessas/{id}` | nenhuma | Metadados, estado e totais da remessa; referências aos caminhos de lotes/eventos |
| `/__simulator/remessas/{id}/eventos` | `limite`, `cursor` | Eventos ordenados por ID persistido; referência local de correlação |
| `/__simulator/lotes/{numeroLote}/registros` | `tipo`, `limite`, `cursor` | Página de `{indice, registro}`, índice original zero-based |
| `/__simulator/relatorio` | `origem`, `formato`, `limite`, `cursor` | JSON local com página de eventos ou síntese compartilhável |

Preservar `/__simulator/configuracao` e o contrato completo existente
`/__simulator/lotes/{numeroLote}` (bytes Base64, conteúdo e histórico sob demanda).
Nenhuma nova rota compatível ou tela.

Filtros opcionais: cenário deve ser o configurado (outro retorna 404); `codigoUg`
é string opaca não vazia, preserva zeros; `anoMes` inteiro canônico 202601–202612;
`assunto` é um dos seis nomes exatos existentes. Filtro assunto em remessas usa
EXISTS sobre lotes, inclusive históricos. Filtros válidos sem correspondência
retornam página vazia. Detalhes fora do cenário retornam 404.
`tipo` obrigatório: `RECEBIDOS`, `PROCESSADOS`, `EFETIVOS`, `HISTORICOS`.
Recebidos só ativos; históricos preservam os recebidos originais inclusive ativos,
como o detalhe atual; processados/efetivos seguem tabelas correspondentes.
Estado/ativo distinguem descarte, aborto, cancelamento e expiração.

Paginação: padrão local 50, máximo local 200, sem significado de limite TCE.
Resposta `{itens, proximoCursor, geracao}`; fetch `limite+1`, sem total obrigatório.
Keyset crescente por remessa.id/lote.numero/evento.id/registro.indice. Cursor opaco
Base64 URL de estrutura estrita contém versão, rota/recurso, geração, filtros,
limite, última PK e teto de PK capturado na primeira página. Não é credencial.
Inserções acima do teto não entram na continuação; nova consulta as vê. Estados e
contagens são atuais por página, não snapshot entre requisições; descarte pode
remover itens processados/efetivos entre páginas. Não prometer snapshot imutável.
Cursor com filtros/limite/recurso alterados: 400. Reset: geração antiga 409,
inclusive quando recurso foi removido. Reinício sem reset conserva continuação.
SQL parametrizado, `LIMIT` e predicados de keyset; nunca OFFSET ou carregar todas
as listas para recortar. Bytes recebidos via stream JDBC/Jackson streaming, só
registros da página materializados; multiplicidade, null/ausente, tipos, zeros e
BigDecimal preservados. Bytes/hash originais continuam íntegros no detalhe atual.

Erros: 401 chave ausente/inválida antes de validar entrada; 400 query desconhecida,
duplicada, vazia quando inválida, enum/cursor/limite/ID inválidos e corpo em GET;
404 recurso/cenário inexistente; 409 geração antiga. Erros sanitizados seguem
padrão existente; sem segredo/valor de query refletido.

## Relatório e manifesto

`origem` obrigatória e estrita: `FICTICIA`, `SANITIZADA`, `ORIGINAL` ou
`NAO_INFORMADA`; apenas classificação DECLARADA pelo solicitante, nunca prova.
`formato` obrigatório: `LOCAL` ou `COMPARTILHAVEL`. Nenhum parâmetro caminho,
escrita no servidor ou publicação. Guia usa `curl ... -o arquivo.json` local.

LOCAL: versão de formato; artefato Maven `fake-esfinge`/versão disponível no jar;
perfil/contrato derivado 2026/V6/6.06; cenário/geração/seed/tempo/configuração não
secreta; origem declarada; totais por estado/assunto; referência aos endpoints de
remessas/lotes/eventos/registro para reconciliação paginada. Sequência/correlação:
IDs persistidos de eventos e vínculos locais remessa/lote, não UUID HTTP. Relatório
contém `sequencia` com página de eventos reais `{id, identificadorRemessa, numeroLote, tipo, instante}` ordenados por ID persistido, `proximoCursor`, `geracao` e `teto`. Continuação usa o mesmo endpoint e origem/formato/limite, com cursor vinculado. Metadados e pendências repetem a cada página. Guia permite salvar todas as páginas localmente. `limite`/`cursor` no formato COMPARTILHAVEL retornam 400. Sem copiar payloads ilimitados. Divergências `null`, comparação `NAO_COMPARADO`,
referência independente/SQL não disponível, aceite T01/E3 pendente. Não inferir
zero divergências. Sem tokens, senhas, hashes de credenciais ou SQL interno.

COMPARTILHAVEL: somente formato, artefato, perfil/contrato, cobertura/regras/limites
gerais e comparação pendente. Excluir cenário/seed/tempo da execução, identidades,
UG/competência, IDs/chaves, contagens/volumes/resultados/correlações, origem real e
payload. A síntese não copia o relatório LOCAL para então apagar campos.

Manifesto real `docs/cobertura.md` empacotado por Maven resources em caminho
específico; relatório identifica nome/hash SHA-256 do snapshot e expõe conteúdo
real com operação/assunto/regra/versão/evidência/teste/situação. Fonte congelada
ANTES de verify/clean verify/imagem. Não atualizar manifesto com resultados
posteriores à build; usar evidência/PROGRESS/README. Git/tag/digest imagem são
indisponíveis no runtime, declarados como tal; sem hash autorreferente de jar.

## Arquivos em escopo e fronteiras

- `remessas/domain`: DTOs Java de filtros/páginas/projeções/registros/relatório.
- `remessas/application/port/in`: inspeção administrativa; implementação coesa
  junto da coordenação existente, sem framework de queries/reports.
- `RemessaRepository`/`JdbcRemessaRepository`: metadados/listas/eventos/recebidos.
- `ProcessingRepository`/`JdbcProcessingRepository`: estado/contagens e páginas de
  processados/efetivos; cada adaptador permanece dono das suas tabelas.
- adaptador HTTP administrativo: validação estrita/cursor/mapeamento JSON e
  metadados empacotados; composição em `configuration` quando necessária.
- `pom.xml`: somente empacotar manifesto existente; sem dependência nova.
- testes focais novos HTTP/SQLite e pequenos ajustes de fixtures/interface.
- `docs/cobertura.md`, README, `docs/evidencias/issue-18.md`, plano,
  `.scratch/pr-issue-18.md`, PROGRESS. Sem alteração arquitetural/rotas compatíveis.

## TDD e validação

1. RED/GREEN HTTP+SQLite: proteção/entrada estrita e listagem mínima sem efeitos.
2. Keyset/filtros/limites/vazio/última página/novas inserções/mudanças de estado,
   cenário/UG e cursor adulterado/alterado/stale409 após reset/reinício.
3. Registros de seis assuntos: índices originais/multiplicidade/zeros/decimais
   altos/null/ausente; erros/descarte/cancelamento/expiração e histórico.
4. Snapshots antes/depois de DB/relógio/atividade/eventos, MANUAL para isolar
   scheduler; relatório/síntese/origem declarada/artefato real/manifesto/422 estrito.
5. Congelar manifesto; focais e `verify` via Java21/Maven local já inspecionados.
6. Congelar produção/testes/POM/manifesto e avisar supervisão antes de reviewer.
   Smoke no jar COPIADO tmp com dados fictícios, metadados/paginação/relatório/
   reset/leitura sem efeitos/reinício. Docker final cabe à supervisão.

Comando local autorizado: `rtk proxy env JAVA_HOME=/Users/luka/.sdkman/candidates/java/current MAVEN_USER_HOME=/private/tmp/fake-esfinge-wrapper-check-fixed ./mvnw -B -ntp -Dmaven.repo.local=/private/tmp/fake-esfinge-m2 -Dtest=... test`; suíte `verify` sem `-Dtest`.
Baseline informado: 326 testes, V1–V14. Conferir execução; nenhuma chamada ao TCE.

## Limites e próxima decisão

Sem UI/E4, novos controles de cenário, framework, integração real, SQL/referência
independente, aceite humano, commit/push/publicação. Contrato aprovado antes do primeiro RED/código. LOCAL inclui página de eventos reais; todos os dados continuam acessíveis nas rotas locais protegidas.

Ajuste aprovado pela supervisão em 28/10: LOCAL inclui eventos reais paginados; autorização para iniciar RED/GREEN recebida.


## Execução do executor

Focal final20testes verde; verify343/0/0/0,exit0,39,815s;17novoscasos.
Smoke do jar copiado45verificações(27+18),exit0,com reinício/reset/read-only.
[Detalhes e hashes](../docs/evidencias/issue-18.md). Produção/testes/POM/manifesto
congelados; resumo dos XML em `.scratch/issue-18-validation-summary.json`.
Interrupção por quota foi operacional, sem mudança funcional ou nova execução
Maven na retomada. Revisão de código e supervisão da imagem concluídas;
próximo: publicação pela supervisão, dentro da autorização vigente.

Imagem finalLinuxamd64 validada pela supervisão com45verificações adicionais,
jar/manifesto conferidos e container removido; nenhuma mudança nos inputs
congelados. Próximo passo: publicação pela supervisão, dentro da autorização vigente.
