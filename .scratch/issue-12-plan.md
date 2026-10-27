# Preparação e decisões — issue #12

Referência: [issue #12](https://github.com/lukadev08/fake-esfinge/issues/12), aberta
`ready-for-agent`, sem comentários; dependências #10/#11 concluídas. Branch
`issue-12-descarte-assunto`, base funcional `0752366` (PR #38), registro `54265cb`.
E2 técnico autorizado pelo planejamento §13.1; #8/E1 pendente. Publicação/merge
da #12 não autorizados.

Explorer próprio `grupo12_explorer`, papel configurado GPT-6 Luna/médio, concluiu
somente leitura; variante efetiva não inferida. Supervisor leu contrato E0,
planejamento, arquitetura, fonte V6 incorporada e código conectado. Sem CONTEXT/
ADRs locais. Seams HTTP/admin/SQLite já confirmados em E0; integrador real pendente.

## Contrato e decisões anteriores ao código

- POST `/v6/tributario/descarteParcialPorAssunto`, `auth_token`, query exata
  `chavePacote`/`assunto`, corpo vazio;200 com corpo exatamente vazio. Parâmetros
  ausentes/vazios/duplicados/extras ou corpo não vazio:400. Assunto fora dos seis
  identificadores de `$defs.descarteQuery`:422 de cobertura, sem normalização.
  Token inválido401 antes de efeitos; chave desconhecida/de outra UG/cenário404.
- Fonte `envio-assincrono-v6.md:188–198` exige lotes processados, permite sucesso
  sem dados e conserva PROCESSADO_SUCESSO após descarte. E0 aprova limpeza derivada
  de RECEBIDO sem processamento ativo; não declarar essa exceção como oficial.
- “Durante processamento” reutiliza `active(remessa.id(), geração)` da #9/#11:
  qualquer trabalho RECEBIDO agendado ou EM_PROCESSAMENTO **da chave**, inclusive
  outro assunto, bloqueia com409. Outras chaves não bloqueiam. Não confundir lote
  recebido sem trabalho com ciclo já disparado. Decisão conservadora conforme
  “todos os lotes” da fonte, reconferida pelo explorer.
- Remessa fechada409. Assunto suportado sem dados ativos, inclusive repetição,
  retorna200 vazio, renova atividade e registra evento próprio de operação.
  Não cria garantia oficial de idempotência nem sucesso para assunto sem suporte.
- Selecionar somente lotes ativos do assunto na remessa/UG/cenário autorizados.
  Remover efetivos→processados→valores recebidos e marcar ativo0. Payload/hash/
  quantidade original e controles permanecem históricos imutáveis, conforme #11.
- Lote em sucesso conserva PROCESSADO_SUCESSO e diagnóstico ausente, mesmo inativo;
  registrar remoção dos dados sem reescrever seu histórico de processamento.
  RECEBIDO sem trabalho recebe controle terminal LOTE_DESCARTADO, código próprio
  SIM_DESCARTE_ASSUNTO, mensagem fixa sanitizada e registro null. Históricos já
  inativos (erro/aborto/sucesso) conservam estado, diagnóstico e dados originais.
- Planos antigos ficam vinculados aos IDs históricos inativos, sem aplicação ao
  reenvio. Trabalhos inativos são inertes pelas guardas existentes. Não apagar
  plano/evento histórico nem duplicar payload em tabela nova.
- Novos lotes recebem IDs/ordem próprios sob a mesma chave, mesmo com bytes/hash
  idênticos; não deduplicar nem contar históricos na finalização. Erro→descarte→
  reenvio→processamento→finalização considera somente ativos/efetivos.

## Tabela de transições

| Ação / pré-condição | Resultado / efeitos |
| --- | --- |
| Assunto suportado, chave aberta, sem ciclo ativo | Remover todo dado ativo do assunto, conservar chave/outros assuntos/histórico;200 vazio |
| RECEBIDO sem trabalho, chave sem ciclo ativo | Limpeza permitida derivadamente, controle LOTE_DESCARTADO e diagnóstico próprio |
| Qualquer trabalho RECEBIDO agendado ou EM na chave | 409, sem remoção, evento ou renovação de atividade |
| Lote PROCESSADO_SUCESSO removido | Ativo false, conjuntos ativos vazios; consulta segue sucesso sem retornoErros |
| Históricos de erro/aborto anteriores | Estado/diagnóstico/bytes e eventos preservados |
| Assunto sem dados ativos / descarte repetido | 200 vazio, só atividade/evento da operação; nenhum controle antigo reescrito |
| Reenviar mesmo conteúdo | Novo lote ativo/ID/ordem, sem plano ou deduplicação herdados do histórico |
| Finalizar após reenvio | Somente ativos em sucesso e totais efetivos; históricos não bloqueiam |
| Descartar único assunto e finalizar sem ativos | 409; sem movimento continua não implementado |
| Descartar chave fechada / de outra UG/cenário | 409 /404 sem efeitos |
| Falha statement/COMMIT durante remoção/grupo/evento | Rollback integral e500 sanitizado; repetir depois funciona |
| Reiniciar após descarte | Histórico/ativos/trabalhos/planos/efeitos conservados, sem reaplicação tardia |

## Menor implementação

1. Acrescentar operação à porta `Processamento`/`ProcessingService`, que já coordena
   trabalhos, autorização, remessa e transações, evitando ciclo em `RemessaService`.
   Controller compatível mapeia transporte/Assunto; resposta void, sem DTO do núcleo.
   Remover só esta rota do controlador indisponível; descarte explícito de lote
   e cancelamento permanecem422 de cobertura.
2. Mesma transação IMMEDIATE identifica remessa, revalida abertura/geração/atividade,
   seleciona ativos, remove/inativa/atualiza controles e grava atividade/eventos.
   Core decide precondições; SQL fica no adaptador. Reusar menor trecho de remoção
   da #11 quando útil, sem camada genérica, dependência ou nova configuração.
3. Evento de operação identifica assunto: tipo próprio
   `ASSUNTO_DESCARTADO_<ASSUNTO>`, inclusive sucesso sem dados. Para sucesso
   inativado, evento de dados removidos preserva o sucesso anterior; para RECEBIDO
   usar evento terminal LOTE_DESCARTADO. Não alterar controles já inativos.
4. V10 é suficiente; **não criar migração** sem necessidade demonstrada. Preservar
   V1–V10/checksums/schema/fontes, índices, high-water, sessões, config/offset,
   remessas fechadas e todos os dados não selecionados. Guardas existentes ativo/
   geração/estado protegem contra execução tardia; reforçar só onde necessário.
5. Política RECEBIDO é derivada e explicitada no manifesto. Falha de resposta
   perdida, cancelamento, reset, expiração e regras fiscais não entram na #12.
   Não criar rota de lote, marca de resposta perdida antecipada ou quota mensal.

## Fatias TDD e supervisão

- RED→GREEN vertical HTTP: erro intermediário→descarte por assunto→reenvio idêntico
  na mesma chave→processamento→finalização, conferindo originais/ativos/efeitos,
  status de sucesso preservado, diagnósticos anteriores e outro assunto intacto.
- Query/body/auth estritos, seis assuntos/422 sem suporte, chave fechada/404
  isolada, sem dados/repetição,200 vazio e renovação somente em sucesso.
- RECEBIDO sem trabalho permitido; recebido agendado/EM (mesmo ou outro assunto
  da chave)409; atividade/eventos/dados inalterados. Outra chave em processamento
  não bloqueia descarte autorizado. Sem ativos409 na finalização.
- SQLite real: vários lotes/10.001 registros/decimais nomeados, deleções completas
  com FK, preservação de histórico/status/planos e reenvio sem deduplicação.
- Rollback statement e COMMIT depois de exclusões e evento; concorrência entre
  descarte/recepção/disparo/última conclusão/finalização e duas instâncias, com
  resultado linearizável e nenhum dado ou efeito tardio perdido.
- Reabertura de base V10 preenchida preserva16 tabelas, schema/checksums/IDs/
  sequências e dados; reinício após remoção conserva estado. Ajustes de fixtures
  apenas se necessários em testes; sem detecção dinâmica de schema em produção.
- Executor próprio→reviewer independente→corretor separado se houver achados;
  supervisor clean verify e imagem Linux amd64 sem rede, reabertura V10 real,
  reinício e caminho automático pela mesma porta, com HTTP/SQLite fictícios.

Comandos existentes inspecionados, Java21/Maven3.9.9, dependências públicas/cache
local e SQLite temporário, sem produção/TCE. Focais `-Dtest=... test`, um verify do
executor e clean verify da supervisão, sem Maven concorrente; RTK/proxy e handles/
exit codes/diagnósticos preservados. E1/#8/T01/E3 e homologação não demonstrados.
