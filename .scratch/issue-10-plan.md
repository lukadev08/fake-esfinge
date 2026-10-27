# Preparação e transições — issue #10

Referência: [issue #10](https://github.com/lukadev08/fake-esfinge/issues/10), aberta
`ready-for-agent`, sem comentários, dependência #9 concluída. Branch
`issue-10-finalizacao`, base funcional merge `eb376d0` (PR #36), registro local
`f4e48b4`. #8/E1 pendente; continuidade técnica autorizada pelo planejamento §13.1.
Publicação/merge da #10 não autorizados.

Explorer próprio `grupo10_explorer` identificado GPT-6 Luna/médio, somente leitura.
Planejamento §§5/7/8/10–14, contrato E0 e arquitetura continuam fontes. Não há
CONTEXT.md/ADRs locais; não recriar. Fontes/schemas e V1–V8 permanecem imutáveis.

## Contrato e validação

`POST /v6/tributario/finalizarEnvio`, `auth_token`, JSON UTF-8, sem query. Corpo
exato com `chavePacote` não vazia e os 17 totalizadores obrigatórios; não aceitar
headers administrativos como autenticação compatível. Sucesso200, corpo vazio.
JSON/mídia/query malformados:400; schema/campos/tipos inválidos:422, conforme
padrão da recepção. Duplicatas/documentos extras/UTF-8 inválido:400.

| Totalizador dos seis assuntos | Assunto interno |
| --- | --- |
| quantidadeCadastroContribuinte | CADASTRO_CONTRIBUINTE |
| quantidadeLancamentoCreditosTributarios | LANCAMENTO_CREDITO_TRIBUTARIO |
| quantidadeDiarioGeralArrecadacao | DIARIO_ARRECADACAO |
| quantidadeBaixaCreditosTributarios | BAIXA_CREDITO_TRIBUTARIO |
| quantidadeSaldoInicialCreditosTributarios | SALDO_INICIAL_CREDITO_TRIBUTARIO |
| quantidadeInicioPrazoPrescricional | INICIO_PRAZO_PRESCRICIONAL |

Os seis aceitam strings ASCII `[0-9]+`, comparadas numericamente sem overflow
(BigInteger), sinal/fração/coerção ou limite inventado. Zeros iniciais não mudam a
comparação. Os onze restantes exigem string exata `"0"`, conforme `const` do schema:
quantidadeCadastroImobiliario, quantidadeCadastroPropriedadeImobiliaria,
quantidadeRevisaoValorLancamentoCreditos,
quantidadeEstornoReceitaDiarioGeralArrecadacao, quantidadeCertidaoDividaAtiva,
quantidadeCobrancaDividaAtiva, quantidadeSituacaoCobrancaDividaAtiva,
quantidadeNovoNumeroInscricaoImobiliaria,
quantidadeLancamentoCreditosTributariosOriginal,
quantidadeNumeroCadastroImobiliarioLancamento,
quantidadeRevisaoTipoCreditoTributario. Positivo sem suporte:422 de cobertura;
outras violações:422 estrutural. Não alterar o schema para aceitar `"00"` nestes onze.

## Transições anteriores à implementação

| Pré-condição / ação | Resultado e efeitos |
| --- | --- |
| Token inválido ou expirado | 401 antes de efeitos |
| Chave inexistente ou de outra UG/cenário | 404 sem efeitos |
| Chave finalizada, nova finalização/recepção/disparo | 409 sem efeitos |
| Aberta sem lote | 409, sem movimento não implementado |
| Aberta com qualquer lote RECEBIDO/EM_PROCESSAMENTO, incluindo recebido depois do snapshot | 409, sem avanço de tarefa/relógio ou atividade |
| Todos os lotes em sucesso, total divergente | 409, preservar chave aberta e conteúdo |
| Todos em sucesso, ao menos um lote, seis totais efetivos corretos e onze zeros | Fechar chave, registrar instante/evento e liberar competência atomicamente;200 vazio |
| Consultar lote histórico autorizado após fechar | 200 com conteúdo/estado preservados; não renovar atividade nem reabrir chave; leitura sem novo evento de consulta |
| Abrir mesma UG/competência após fechar | Nova chave distinta; histórico preservado; uma única abertura concorrente vence |
| Falha de statement/COMMIT na finalização | Rollback integral de marcador/evento/atividade; sem200 parcial; retomada possível |
| Reiniciar | Preservar fechamento, originais, processados/efetivos, totais e histórico; repetição continua409 |

## Menor desenho completo

1. Finalização fica em `remessas`, estendendo porta/caso de uso existentes quando
   suficiente. Adaptador HTTP valida transporte e passa seis totais tipados ao
   núcleo Java puro; reutilizar erros/transações/validação existentes, sem dependências.
2. Uma transação SQLite IMMEDIATE autentica/identifica a chave autorizada,
   revalida abertura, verifica que há lotes e todos concluíram, agrega registros
   efetivos por assunto/origem, compara e grava fechamento/evento. Contagens são
   linhas efetivas, sem DISTINCT, soma monetária, `lotes.registros` ou totais globais.
   A semântica de precondição/comparação fica no núcleo; SQL fica no adaptador.
3. V9 aditiva: `remessas.finalizada_em TEXT` nullable, NULL nas remessas antigas.
   Recriar `remessa_aberta_unica` para considerar somente remessas sem marcador
   (e estado legado ABERTO). Evitar reconstruir remessa e seis tabelas filhas.
   `estado` da V2 permanece legado, sem constituir sozinho a verdade da abertura.
   Domínio carrega marcador; `find` retorna também finalizadas para distinguir409
   de404, com guardas obrigatórias nos mutadores.
4. Atualizar todas as consultas/guardas de abertura em recepção, disparo,
   elegibilidade, seleção/revalidação do executor e touch. Cálculo fora de
   transação continua revalidando marcador antes de confirmar efeitos. Consulta
   histórica é somente leitura da execução encerrada.
5. Nesta versão todo lote existente é ativo; não há descarte/aborto ou controles
   históricos de lote. Não antecipar esse modelo. Recuperação #12–#14 integrará
   seu filtro e os totais; histórico de outros envios já fica fora pelo vínculo.
   Cancelamento/descarte ainda indisponíveis mantêm422 de cobertura, sem simular409
   como se a capacidade estivesse implementada.
6. Preservar IDs/bytes/hashes/valores TEXT/sessões/eventos/trabalhos/resultados,
   índices/FKs, high-water marks e checksums antigos na migração/reabertura.
   Testar falha intermediária de V9 e checksum incompatível sem recriar dados.

## Fatias TDD e supervisão

Seams confirmados em E0: HTTP compatível, administração separada, núcleo/SQLite
real e integrador real/esperado independente (último permanece pendente).

- RED→GREEN nominal HTTP: receber/processar, finalizar com17 totais,200 vazio;
  conferir conteúdo e marker/evento, repetição409 e nova chave na mesma competência.
- Parser estrito e seis assuntos em ciclos/múltiplos lotes/duplicatas; zeros
  iniciais e total enorme divergente sem overflow;11 ausentes/positivos/tipos/extras.
- Vazia, RECEBIDO, EM_PROCESSAMENTO, lote novo não agendado, divergência e outros
  envios/UGs/cenários; rejeições preservam dados/atividade/eventos/relógio.
- Concorrência de finalização/recepção/abertura/processamento; uma conclusão/evento,
  sem lote perdido ou efeito tardio. Consulta histórica sem renovar atividade.
- SQLite real: V8 preenchida com todos os assuntos, trabalhos em fases distintas,
  config/offset/efeitos, rollback statement/COMMIT, falha V9, reinício e unicidade.
- Executor próprio implementa; reviewer independente revisa; correções delegadas.
  Supervisor repete clean verify e imagem Linux amd64 isolada, upgrade V8 e
  reinício com HTTP e leitura SQLite. Somente fictícios, sem TCE/produção.

Comandos inspecionados: pom/wrapper/Dockerfile herdados, sem alteração desde #9.
Java21/Maven3.9.9, dependências públicas/cache local, testes SQLite temporários.
Focal com `-Dtest=FinalizacaoHttpTest,JdbcFinalizationTest test`, um `verify` do
executor e `clean verify` da supervisão. Sem execuções concorrentes de Maven.
Usar RTK/proxy e preservar saídas, exit codes e handles. Sem publicação da #10,
reset, sem movimento, T17, regras fiscais, E1/T01 integral ou homologação.
