# Plano local — issue #16

Referência: [#16](https://github.com/lukadev08/fake-esfinge/issues/16), T02/T11/T19,
contrato derivado 2026/V6/6.06. Base funcional `df77df1` (PR #42), registro
`9f58eae`; branch `codex/issue-16-expiracao`. #8/E1 continua pendente, com E2
técnico autorizado pelo planejamento §13.1. Produção e testes ainda intactos.

## Contrato e decisões

- Reutilizar POST `/__simulator/processamento/avancar` e o relógio de negócio
  existentes. Nenhuma API, parâmetro compatível ou configuração de TTL nova.
  Token mantém duração configurável existente, padrão de uma hora; autenticação
  inválida/expirada retorna 401 antes de efeitos. Novo login não renova remessa.
- Envio aberto vence no instante `atividade + 72h`, inclusive no limite. A agenda
  durável fica no próprio envio, em segundos epoch e nanos inteiros, sem REAL,
  comparação de strings de Instant ou arredondamento. O prazo interno pode
  ultrapassar o ano 9999; isso não restringe timestamps antes aceitos.
- Cada operação compatível bem-sucedida sobre a chave aberta, inclusive consulta,
  atualiza atividade e agenda na mesma transação dos efeitos/evento. Login,
  rejeição, inspeção e controles administrativos não renovam atividade.
- Expiração usa marcador `expirada_em` separado de finalização/cancelamento.
  Remove dados ativos recebidos/processados/efetivos e inativa lotes; conserva
  bytes/hash/quantidade original, controles e eventos. Sucessos anteriores ficam
  históricos sem novo diagnóstico; pendentes sem trabalho, agendados e EM ficam
  ABORTADO, `SIM_ENVIO_EXPIRADO`, mensagem própria `Envio expirado no simulador`,
  registro null. Esta política é derivada, sem equivalência oficial alegada.
- Chave expirada não reabre; mutação retorna 409. Consulta histórica autorizada
  retorna 200 sem atividade/evento novo nem processamento. Nova abertura na mesma
  UG/competência usa chave nova, inclusive após expirar um envio vazio.
- A verificação de autenticação/isolamento 401/404 precede expiração por chamada;
  prazo vencido precede bloqueio 403. As guardas restantes permanecem iguais.
  Controles administrativos conservam suas precondições de chave aberta, mas
  consultas administrativas não executam expiração nem processamento.

## Transações e ordenação

| Entrada / condição | Unidade e resultado |
| --- | --- |
| Token expirado / chave de outro contexto | 401/404, sem touch nem expiração provocada pela chamada |
| Chave aberta antes do prazo, chamada válida | Efeitos, atividade, agenda e evento confirmam juntos |
| Chave aberta antes do prazo, chamada rejeitada | Nenhuma renovação ou efeito parcial |
| Mutação compatível encontra envio vencido | Confirma apenas expiração na transação; retorna outcome normal; lança 409 somente depois do commit |
| Consulta compatível encontra envio vencido | Confirma apenas expiração, lê estado histórico e responde 200; sem touch/processamento |
| Nova abertura bloqueada | 403 antes de expirar o envio anterior, sem efeitos; executor/outro acesso autorizado tratará o vencimento |
| Nova abertura permitida encontra envio anterior vencido | Expiração anterior e abertura nova confirmam na mesma transação |
| Controle administrativo mutável exige envio aberto, mas prazo venceu | 409 sem expirar/touch, mesmo antes de materializar expirada_em; GET administrativo continua somente leitura |
| Avanço manual de tempo | Tempo confirma primeiro como já existente; expira todos os envios devidos antes dos passos de processamento |
| Tick automático | Expira devidos e só depois processa um passo; modo manual não executa passos de processamento pelo tick |
| Trabalho capturado antes do limite | begin/complete/fail revalidam marcador, deadline e geração com now lido na transação final; vencido nunca grava efeito |
| Falha statement/COMMIT | Reverte integralmente aquela expiração/renovação; não devolve sucesso ou 409 de expiração não confirmada |

O tick pode verificar a agenda nos dois modos, sem avançar relógio; em relógio
fixed nada vence sem comando de tempo. Em system, passagem real pode vencer a
agenda mesmo com processamento MANUAL, como política de expiração temporal.
Isso não inicia processamento de lotes nem renova atividade.

## Menor alteração e alcance

- V14 aditiva acrescenta deadline e marcador, atualiza índice de envio aberto
  e registra versão/checksum. V1–V13/fontes/schemas/build/dependências intactos.
  Após o SQL e antes de registrar/commitar a versão, Java lê `atividade` via
  `Instant.parse`, soma 72h e preenche segundos/nanos na mesma conexão da migração.
  Falha no backfill reverte schema/versão/dados; nada de strftime/julianday.
- `Remessa` representa prazo/expiração e guardas. `RemessaRepository` expõe
  busca de envio aberto e agenda devida; JDBC persiste/renova prazo exatamente.
- Reutilizar `ProcessingRepository`/JDBC para a limpeza e terminais de expiração,
  reaproveitando remoção de dados/diagnóstico/eventos de cancelamento. Injetar essa
  porta também em `RemessaService`; atualizar Composition e chamadores de fixtures.
  Nenhum novo serviço/porta genérica ou ciclo entre capacidades.
- `RemessaService.open/receive/finish/consult` e
  `ProcessingService.start/discard/cancel/advance/tick/step` coordenam os outcomes.
  Guardas SQL de receber/finalizar/touch/agendar/next/current/configurar falhas
  incluem expiração. `begin` recebe now explícito para não confundir horário
  agendado com relógio da gravação; complete/fail usam o now final já existente.
- Fixtures anteriores ao schema atual usam SQL legado explícito; adaptar somente
  assinaturas, versão/checksum e colunas novas. Não simular upgrade atualizando
  artificialmente o schema antigo. Auditoria de todos os chamadores realizada.

## Verificação

Inspecionados pom, wrapper, README e testes existentes: JDK21/Maven3.9.9, SQLite
temporário, sem TCE ou dados originais. Usar wrapper/cache temporários existentes.
TDD RED/GREEN vertical HTTP, focais HTTP/JDBC e `verify`; supervisão fará
`clean verify` e Docker Linux amd64 no jar final, sem Maven concorrente.

Cobrir token imediatamente antes/no/depois e duração configurada; 72h antes/no/
depois com nanos; renovação nas operações autorizadas; rejeição/admin/login sem
renovação; envio vazio; finalizada/cancelada não expiram; reinício/offset/agenda;
upgrade V13 preenchida e backfill exato/rollback; statement/COMMIT; duas instâncias
e cálculo pausado fora da transação; guardas begin/complete/fail; nova chave e
isolamento UG/cenário, bytes/zeros/decimais/multiplicidade e regressões #14/#15.

Smoke fictício usará avanço virtual e reinício, sem aguardar horas reais. README,
cobertura, evidência, PR local e PROGRESS separarão execução comprovada e limites.
Reset #17, inspeção #18, regras fiscais, integrador/T01/E3 e homologação ficam fora.

## Execução local

Desenho aprovado pela supervisão antes do código, com precedência da abertura
bloqueada e controles administrativos vencidos explicitados. RED/GREEN HTTP,
ampliação JDBC/HTTP e correções de precedência/geração concluídas antes da revisão.
Executor verify 304 e supervisor clean verify 304 verdes; novos 29 casos. Smoke do
jar copiado e da imagem final Linux amd64 passaram 56 verificações cada, com
reinício/expiração/recuperação, SQLite íntegro e hash do jar final conferido.

Código/testes/smoke/jar congelados; revisão independente do código aprovada sem
achados materiais e conferência documental final aprovada. Containers/processos
temporários encerrados, bases fictícias
tmp preservadas; nenhum volume Docker criado. Evidência completa em
[issue-16.md](../docs/evidencias/issue-16.md). Sem commit/push pelo executor.
