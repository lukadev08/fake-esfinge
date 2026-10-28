# Inspeção administrativa e relatórios locais

Contrato exclusivo do simulador 0.1.0, perfil derivado 2026/V6/6.06. Nenhuma rota
abaixo é uma capacidade oficial do TCE. Chave `X-Simulator-Key` obrigatória; use a
chave administrativa fictícia configurada. Todas são GET sem corpo e sem efeitos
sobre atividade, relógio, processamento, expiração ou eventos.

| Caminho sob `/__simulator` | Query aceita |
|---|---|
| `/remessas` e `/lotes` | `cenario`, `codigoUg`, `anoMes`, `assunto`, `limite`, `cursor` opcionais |
| `/remessas/{id}` | Nenhuma |
| `/remessas/{id}/eventos` | `limite`, `cursor` opcionais |
| `/lotes/{numeroLote}/registros` | `tipo` obrigatório; `limite`, `cursor` opcionais |
| `/relatorio` | `origem`, `formato` obrigatórios; `limite`, `cursor` só em LOCAL |

A configuração atual continua em `/configuracao`. O detalhe existente
`/lotes/{numeroLote}` conserva conteúdo completo, bytes Base64/hash e histórico;
use sob demanda para conferir os bytes originais. Essa rota preexistente não é
paginada. Listas novas selecionam apenas metadados; nenhum payload entra nelas.
Detalhe de remessa traz datas de abertura/atividade/fechamento/cancelamento/
expiração e contagens por assunto/estado, sem consultar a API compatível.

Filtros são exatos: `cenario` só aceita o configurado; `codigoUg` é string opaca
não vazia (não converter `0001` em número); `anoMes` inteiro ASCII canônico
202601–202612; `assunto` usa um dos seis nomes de `Assunto`. O filtro assunto de
remessas considera também lotes históricos. Sem correspondência, página vazia.

`tipo` aceita `RECEBIDOS`, `PROCESSADOS`, `EFETIVOS`, `HISTORICOS`. Recebidos mostram
somente lotes ativos; históricos mostram os recebidos originais, inclusive
ativos, conforme o detalhe existente. Processados/efetivos refletem as linhas
que ainda existem. Cada item contém `indice` original, zero-based, e `registro`;
não há reindexação ou deduplicação. Campos ausentes/nulos, tipos, zeros e valores
decimais exatos são preservados. Estado e `ativo` distinguem sucesso histórico,
descarte e aborto; eventos documentam cancelamento/expiração e suas sequências.

## Paginação e alterações entre leituras

Páginas têm `itens`, `proximoCursor` (null ao terminar), `geracao`, `teto`.
Padrão **50**, máximo **200**: limites administrativos locais, sem limite TCE
implícito. IDs persistidos crescentes ordenam remessas/lotes/eventos; registros
usam índice original crescente. A primeira página captura o teto da PK; inserções
acima dele ficam para nova consulta. SQL usa keyset/LIMIT, sem OFFSET. Recebidos
são percorridos por streaming do BLOB; somente a página é materializada pela
aplicação (o driver SQLite pode manter o BLOB internamente).

Envie o cursor retornado com a mesma rota/recurso, filtros e limite; no relatório,
mesma origem e formato. O cursor é opaco, versionado, sem segredo ou poder de
autorização. Não edite seu conteúdo. Não há snapshot entre requisições: estados,
contagens e registros são atuais por página; remoção de efetivos/processados pode
encurtar uma continuação. O teto evita incluir novas PKs, não congela resultados.
Reinício sem reset conserva continuação. Reset muda a geração e invalida cursores
anteriores com 409, inclusive se o recurso foi removido. Nova consulta após reset
não retorna dados da geração antiga.

Chave ausente/inválida: **401 antes de validar entrada**. Query extra, duplicada,
vazia/inválida, corpo, IDs/enums/limites/cursor inválidos, filtro/recurso/limite
alterado na continuação: **400**. Outro cenário/recurso: **404**. Cursor de geração
anterior: **409**. Erros usam envelope sanitizado existente; nenhum segredo ou
valor de query é refletido.

## Relatório e origem declarada

`origem`: `FICTICIA`, `SANITIZADA`, `ORIGINAL`, `NAO_INFORMADA`. É uma declaração
do solicitante, sem detecção, verificação ou garantia de mascaramento.
`formato`: `LOCAL` ou `COMPARTILHAVEL`. Não existe parâmetro caminho nem escrita
no servidor. O cliente salva o JSON localmente com ferramentas usuais.

LOCAL contém configuração não secreta, seed/perfil/geração, modo/latência/tempo,
contagens por assunto/estado e `sequencia` com página de eventos reais ordenados:
`id`, `identificadorRemessa`, `numeroLote` (null nos eventos da remessa), `tipo`,
`instante`. Correlação é **referência LOCAL persistida** em `remessa_eventos.id`
e seus vínculos; não representa UUID HTTP. Cada página repete metadados/corte e
pendências; siga `sequencia.proximoCursor` no próprio `/relatorio` até null.
Dados completos continuam nas páginas de inspeção e no detalhe original.

Exemplo com dados fictícios; salve cada continuação em arquivo diferente:

```sh
curl --fail-with-body --get 'http://localhost:8080/__simulator/relatorio' \
  -H 'X-Simulator-Key: admin-local-ficticio' \
  --data-urlencode 'origem=FICTICIA' --data-urlencode 'formato=LOCAL' \
  --data-urlencode 'limite=50' -o relatorio-local-001.json
# Copie sequencia.proximoCursor do arquivo; se for null, terminou.
curl --fail-with-body --get 'http://localhost:8080/__simulator/relatorio' \
  -H 'X-Simulator-Key: admin-local-ficticio' \
  --data-urlencode 'origem=FICTICIA' --data-urlencode 'formato=LOCAL' \
  --data-urlencode 'limite=50' --data-urlencode "cursor=$CURSOR" \
  -o relatorio-local-002.json
curl --fail-with-body --get 'http://localhost:8080/__simulator/lotes' \
  -H 'X-Simulator-Key: admin-local-ficticio' \
  --data-urlencode 'codigoUg=0001' --data-urlencode 'anoMes=202601' \
  --data-urlencode 'assunto=CADASTRO_CONTRIBUINTE' -o lotes-local.json
```

COMPARTILHAVEL é construído separadamente: artefato, perfil/contrato, manifesto,
regras/limites gerais e comparação pendente. Exclui origem real, contexto/seed/
tempo/identidades, UG/competência, IDs/chaves, volumes/resultados/correlações da
execução e payloads. `limite`/`cursor` nesse formato recebem400. O manifesto pode
referir volumes de testes públicos fictícios; não contém resultados da execução.
Não publique relatórios LOCAL de dados originais; mantenha-os no ambiente local
controlado e fora do versionamento/serviços externos.

Ambos declaram `comparacao=NAO_COMPARADO` e `divergencias=null`. Não há comparação
com SQL/esperado independente nem aceite #8/E1 ou T01/E3 implícito.

## Identificação reproduzível

O jar informa `Implementation-Version` (0.1.0); fora de jar, versão pode aparecer
`INDISPONIVEL_FORA_DO_JAR`. Git/tag/digest da imagem ficam `INDISPONIVEL`: não são
inferidos do checkout. O relatório inclui o conteúdo real de `docs/cobertura.md`
empacotado por Maven em `evidencias/cobertura.md`, com SHA-256 desse snapshot.
Não inclui hash autorreferente do jar/imagem. O manifesto é input do artefato:
congele-o antes de build/imagem; mudança funcional exige reconstrução/revalidação.
Resultados posteriores à build ficam na evidência/PROGRESS, sem mudar o snapshot.
