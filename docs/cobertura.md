# Cobertura implementada — perfil 2026/V6/6.06

Artefato 0.1.0, issues [#1](https://github.com/lukadev08/fake-esfinge/issues/1) e
[#2](https://github.com/lukadev08/fake-esfinge/issues/2) e
[#3](https://github.com/lukadev08/fake-esfinge/issues/3) e
[#4](https://github.com/lukadev08/fake-esfinge/issues/4) e
[#5](https://github.com/lukadev08/fake-esfinge/issues/5) e
[#6](https://github.com/lukadev08/fake-esfinge/issues/6) e
[#7](https://github.com/lukadev08/fake-esfinge/issues/7) e
[#9](https://github.com/lukadev08/fake-esfinge/issues/9) e
[#10](https://github.com/lukadev08/fake-esfinge/issues/10) e
[#11](https://github.com/lukadev08/fake-esfinge/issues/11) e
[#12](https://github.com/lukadev08/fake-esfinge/issues/12) e
[#13](https://github.com/lukadev08/fake-esfinge/issues/13) e
[#14](https://github.com/lukadev08/fake-esfinge/issues/14) e
[#15](https://github.com/lukadev08/fake-esfinge/issues/15) e
[#16](https://github.com/lukadev08/fake-esfinge/issues/16) e
[#17](https://github.com/lukadev08/fake-esfinge/issues/17).
Fontes: [contrato E0](contrato-e0.md), [pacote derivado](contratos/2026-v6-6.06/README.md)
e [hashes das fontes](contratos/2026-v6-6.06/fontes.json), consolidados em 23/10/2026.
Não há OpenAPI oficial nem ensaio independente do integrador/TCE. O manifesto
inicial dentro de E0 conserva o estado documental daquela entrega; este arquivo
registra a evolução implementada.

| Operação / assunto | Regra | Evidência / teste | Situação |
|---|---|---|---|
| Login | POST sem corpo, headers/query, `chave`, 400/401/403 | Contrato E0; `AuthenticationHttpTest`, `AuthenticationServiceTest` | Implementada no contrato derivado |
| Token | Vínculo usuário/UG/cenário, opaco, inválido/expirado 401 antes de efeito | `AuthenticationServiceTest`, `AuthenticationHttpTest`; parcela T10/T11 | Implementada |
| Validade de token | 3600 segundos configuráveis, relógio substituível | Contrato E0; testes de limite e duração customizada | Aproximada; duração local aprovada, sem equivalência oficial |
| Administração | GET `/__simulator/configuracao`, chave própria, inspeção sem segredos | `AuthenticationHttpTest`, `SimulatorSettingsTest` | Implementada; API exclusiva do simulador |
| Bloqueio de competência | GET/PUT `/__simulator/competencias/{ug}/{anoMes}`, chave própria, UG opaca configurada, mês ASCII 2026 e JSON booleano estrito; V13 aditiva, reinício e rollback | `CompetenciaHttpTest`, `JdbcCompetenciaTest`; parcelas T09/T10 | Implementada no contrato administrativo derivado |
| Mutações em competência bloqueada | Abrir/receber/disparar/finalizar/descartar/cancelar retornam 403 `SIM_ACESSO_NEGADO` dentro da transação, após autenticação/resolução isolada, sem efeitos ou consumo da tentativa #14 | `CompetenciaHttpTest`, `JdbcCompetenciaTest`; ausência de efeitos, corrida/isolamento/reinício | Implementada no contrato derivado; consultas autorizadas permanecem disponíveis |
| Bloqueio prospectivo | Trabalhos previamente aceitos continuam; administração mantém suas precondições; desbloqueio libera novas mutações | E0/T09, interpretação documentada no plano #15; `CompetenciaHttpTest`, `JdbcCompetenciaTest` | Aproximada; política local sem suspender/cancelar trabalho ou inventar ratificação compatível |
| Persistência | SQLite real, migrações V1–V14 com checksum/version, FK por conexão, reabertura, incompatibilidade sem recriar; V14 faz backfill Java transacional exato de atividade+72h | `JdbcAuthenticationRepositoryTest`, `JdbcRemessaTest`, `JdbcCancellationTest`, `JdbcReceptionFailureTest`, `JdbcExpirationTest` | Implementada para contexto/identidades/sessões/remessas/lotes/eventos/planos/agenda; parcela de T13 |
| Erros e logs | Envelope próprio sanitizado, sem senha/token/SQL/stack trace | `InternalFailureHttpTest`, testes HTTP/configuração | Implementada; logs padrão desabilitados |
| Fronteiras | Núcleo Java puro, domínio sem aplicação, capacidades sem ciclos | `ArchitectureTest`, análise de dependências de classes | Implementada na fatia atual |
| Operações ainda indisponíveis | Token válido recebe 422 de cobertura, inválido 401; inexistente 404 | `AuthenticationHttpTest`, `RemessaHttpTest`; parcela de T18 | Cobertura ausente explícita; sem aprovação fiscal |
| Abrir envio | UG/cenário do token, mês de 2026, NAO; restrição única para abertura concorrente | `RemessaHttpTest`, `JdbcRemessaTest`; parcelas T10/T14 | Implementada no contrato derivado; SIM/omissão recebem 422 |
| Receber contribuinte | Lista direta na raiz; schema, bytes/hash/ordem/contagem/evento atômicos; sem dedupe/limite de 10.000 | `RemessaHttpTest`, `JdbcRemessaTest`, `RemessaFailureHttpTest`; parcelas T01/T18 | Implementada estruturalmente; sem validação fiscal |
| Receber crédito | Lista direta, schema `credito`, mês histórico válido, dinheiro BigDecimal/múltiplo exato de 0,01 sem coerção/arredondamento; TEXT por registro e bytes originais | `CreditoHttpTest`, `JdbcRemessaTest`, `RemessaFailureHttpTest`; parcelas T01/T02/T18 | Implementada estruturalmente; referências, cronologia, catálogos e cálculos tributários não implementados |
| Receber arrecadação | Lista direta `diarioGeralArrecadacao`, schema `arrecadacao`, decimal exato em TEXT e bytes/contagens/vínculos atômicos | `ArrecadacaoHttpTest`, `JdbcRemessaTest`, `RemessaFailureHttpTest`; parcelas T01/T02/T18 | Implementada estruturalmente; sem cronologia, catálogos, referência fiscal ao crédito ou cálculos |
| Receber baixa | Lista direta `baixasCreditosTributarios`, schema `baixa`, obrigatórios/opcionais e decimal exato em TEXT; bytes/contagens/vínculos atômicos | `BaixaHttpTest`, `JdbcRemessaTest`, `RemessaFailureHttpTest`; parcelas T01/T02/T18 | Implementada estruturalmente; baixa anterior ao crédito, existência de crédito/dívida ativa, catálogos e cálculos não implementados |
| Receber saldo inicial | Lista direta, schema `saldo`, quatro valores nomeados BigDecimal/TEXT, ano histórico e opcional sem default | `SaldoHttpTest`, `ReceivedContentTest`, `JdbcRemessaTest`, `RemessaFailureHttpTest`; parcelas T01/T02/T16/T18 | Implementada estruturalmente em cenário fictício janeiro; sem condições/defaults/catálogo fiscal ou validação de abertura de exercício |
| Receber início do prazo prescricional | Lista direta `inicioPrazoPrescricional`, quatro obrigatórios, ano inteiro/código string/datas de calendário, conteúdo não monetário | `PrazoHttpTest`, `ReceivedContentTest`, `JdbcRemessaTest`, `RemessaFailureHttpTest`; parcelas T01/T02/T18 | Implementada estruturalmente no schema derivado aprovado sem DTO extraído; sem cronologia, prescrição fiscal ou vínculo ao crédito/dívida ativa |
| Valores recebidos | Mapas imutáveis por registro e campo exato por assunto; V6 atribui nomes ao dinheiro antigo | `ReceivedContentTest`, `JdbcRemessaTest`; campos/precisão/cardinalidade/imutabilidade/migração | Implementada; quatro linhas por registro de saldo, uma nos três assuntos monetários anteriores |
| `numeroParcelas` | Inteiro opcional/nulo no plural; singular `numeroParcela` rejeitado | Pacote derivado/relato do cliente; `ArrecadacaoHttpTest` | Aproximada; divergência da extração preservada explicitamente |
| Consultar lote | Estado persistido, nomes contratuais, isolamento e atividade/evento sem avançar processamento | `RemessaHttpTest`, `JdbcRemessaTest`, `RemessaExpiryHttpTest`; parcelas T02/T10/T13 | Implementada; recebidos não são efetivos |
| Inspeção de lote | Chave administrativa, cenário atual, bytesBase64/registros/contagens/eventos e listas processadas/efetivas | `RemessaHttpTest`, `CreditoHttpTest`, `ArrecadacaoHttpTest`, `BaixaHttpTest`, `SaldoHttpTest`, `PrazoHttpTest`, `RemessaFailureHttpTest` | Implementada para os seis assuntos com números exatos sob `/__simulator/lotes/{numeroLote}` |
| Falha de persistência | Rollback de statement/commit SQLite real, 500 sanitizado e nenhuma confirmação parcial | `JdbcRemessaTest`, `RemessaFailureHttpTest` | Implementada para abertura/recepção/consulta da fatia |
| Disparo | Snapshot de todos os lotes elegíveis da chave, conflitos409, lotes posteriores aguardam novo disparo | `ProcessamentoHttpTest`, `JdbcProcessingTest`; parcelas T02/T10/T13/T14 | Implementada no contrato derivado |
| Processamento estrutural | RECEBIDO→EM_PROCESSAMENTO→PROCESSADO_SUCESSO, revalidação existente, registros processados/efetivos por origem/índice, sem deduplicação | `ProcessamentoHttpTest`, `JdbcProcessingTest`; parcelas T02/T13/T18 | Implementada para seis assuntos; sem regras fiscais |
| Controle de processamento | MANUAL/AUTOMATICO, latência persistida, avanço explícito do relógio compartilhado e passos, JSON estrito, chave própria e horizonte técnico dos timestamps/deslocamento validado antes dos efeitos e em cada leitura, inclusive após reinício | `ProcessamentoHttpTest`, `JdbcProcessingTest`; parcelas T10/T11/T13 | Aproximada; API interna e padrões MANUAL/1000ms derivados, sem equivalência oficial |
| Persistência de processamento | V8 aditiva, upgrade V7 preenchida, reinício, ordem, concorrência, guarda de geração e rollback statement/COMMIT | `JdbcProcessingTest`, `ProcessamentoHttpTest`; parcela T13 | Implementada; avanço de tempo precede transições individualmente atômicas |
| Finalização | POST JSON estrito com chave/17 totais, seis BigInteger versus linhas efetivas por assunto, onze strings zero; todos os lotes ativos em sucesso e pelo menos um ativo; divergência/pendência409 sem efeitos, sucesso200 vazio | `FinalizationTest`, `FinalizacaoHttpTest`, `JdbcFinalizationTest`; parcelas T01/T03/T10/T13/T14/T15/T18 | Implementada no contrato derivado; exclui históricos inativos; recuperação por assunto na #12 |
| Fechamento e histórico | Marcador V9 aditivo, unicidade apenas da chave aberta por competência; mutações409, consulta histórica sem atividade/evento, reabertura sem cota, concorrência, rollback statement/COMMIT e preservação V8 | `FinalizacaoHttpTest`, `JdbcFinalizationTest`; parcelas T02/T03/T13/T14/T15 | Implementada; estado legado não determina sozinho a abertura |
| Erro assíncrono injetado | Plano administrativo estrito por lote/geração, índice/null, imutável após início; descarte integral do alvo e aborto atômico dos seguintes pendentes do mesmo assunto/remessa/UG/cenário; consulta200 com diagnóstico, sem avançar | `ProcessamentoHttpTest`, `JdbcProcessingFailureTest`; parcelas T02/T04/T06/T10/T13/T15 | Aproximada; injeção própria, sem validar regra fiscal T04 |
| Histórico de descartes/abortos | V10 aditiva em lotes, reconstrução restrita aos trabalhos preserva IDs/seq/tempos; bytes/hash originais imutáveis, linhas ativas removidas, inspeção histórica explícita e finalização só de ativos | `JdbcProcessingFailureTest`, `JdbcFinalizationTest`, `ProcessamentoHttpTest`; rollback statement/COMMIT, concorrência, guardas tardias, reinício/migração | Implementada para erro controlado; sem recuperação integral |
| Descartar assunto / seis assuntos | POST com query estrita,200 vazio; guarda global da chave contra agendado/EM,404 isolado/409 fechado; remoção e inativação atômicas, sucesso histórico preservado, demais dados intactos, evento/atividade inclusive sem dados/repetição | E0 e fonte V6:188–198; `DescarteAssuntoHttpTest`, `JdbcSubjectDiscardTest`; parcelas T06/T07/T08/T10/T11/T13/T15/T18/T19 | Implementada no contrato derivado; sem garantia oficial de idempotência |
| Limpeza de RECEBIDO sem trabalho | LOTE_DESCARTADO/SIM_DESCARTE_ASSUNTO/registro null, originais e plano históricos preservados; reenvio novo ID/ordem sem dedupe nem plano herdado, finalização somente dos ativos/efetivos | E0 aproximação expressa; `DescarteAssuntoHttpTest`, `JdbcSubjectDiscardTest`, `JdbcReceptionFailureTest`; parcelas T06/T07/T08/T12 | Aproximada; diverge da restrição V6, recuperação de resposta perdida incluída na #14 |
| Cancelar envio aberto | POST query estrita/corpo vazio, 200 vazio; remove/inativa todo dado ativo, inclusive agendado/EM, preserva originais/sucessos/erros anteriores e outros contextos; nova chave na mesma competência; mutações repetidas/encerradas409 e consulta histórica sem atividade | `CancelamentoHttpTest`, `JdbcCancellationTest`; parcelas T08/T10/T11/T13/T14/T15/T18/T19 | Implementada no contrato derivado; não cancela concluído |
| Controle de cancelamento | Pendentes ABORTADO/SIM_ENVIO_CANCELADO/registro null, sucesso histórico preservado; V11 distingue cancelamento/finalização, copia V10 inclusive diagnósticos/sequências e revalida resultado tardio sem mudar geração global | `JdbcCancellationTest`, `CancelamentoHttpTest`; rollback statement/COMMIT, corrida/pausa fora da transação, reinício/upgrade | Aproximada na situação/diagnóstico próprios do simulador, sem equivalência oficial |
| Falhas de recepção antes/depois do commit | Plano administrativo por tentativa elegível e remessa/assunto/geração; socket real recebe zero bytes HTTP; antes confirma só consumo do controle sem lote/atividade/aceitação, depois conserva RECEBIDO/original/valores/evento e marca | `ReceptionTransportTest`, `JdbcReceptionFailureTest`; parcela T12, rollback statement/COMMIT, reabertura e duas instâncias | Aproximada; injeção própria do simulador, sem demora/500 adicionais |
| Recuperar resposta perdida | Retry409 SIM_ENVIO_ABERTO_SEM_PROCESSAMENTO somente no assunto/contexto marcado; PUT/DELETE409 não contornam recuperação; descarte limpa marca e dados ativos atomicamente, reenvio novos IDs sem dedupe e finalização efetiva | `ReceptionTransportTest`, `JdbcReceptionFailureTest`; seis assuntos, bytes/decimais/multiplicidades/histórico e isolamento | Implementada no contrato derivado; descarte de RECEBIDO permanece aproximação divergente do manual |
| Expiração de envio | Agenda72h inclusiva em segundos/nanos, renovação atômica nas seis operações, fechamento distinto/remoção de ativos/histórico, nova chave; token1h/novo login e prioridade401/404/409 | `JdbcExpirationTest`, `ExpiracaoHttpTest`, `RemessaExpiryHttpTest`, `AuthenticationServiceTest`; parcelas T02/T11/T19 | Aproximada; prazo e operações renovadoras são políticas derivadas |
| Execução da expiração | Advance/tick usam a mesma agenda, manual não processa por tick, cenário/deadline isolados, reinício, rollback statement/COMMIT e geração; begin/complete/fail tardios não gravam | `JdbcExpirationTest`, `ExpiracaoHttpTest`; corrida pausada e guardas antes do marcador, upgrade V13/nanos/horizonte | Implementada tecnicamente; diagnósticos ABORTADO/SIM_ENVIO_EXPIRADO próprios |
| Administração de envio vencido | GET sem efeitos, controles que exigem chave aberta recusam409 antes de materializar marcador; nova abertura bloqueada403 sem expirar; consulta compatível vencida pode expirar e retorna histórico sem conduzir processamento | Plano16; `JdbcExpirationTest`, `ExpiracaoHttpTest` | Aproximada; sem rota compatível nova |
| Reset administrativo do cenário | POST protegido com alvo/geração esperados, geração+1/purge de dados e histórico/defaults/sessões em uma TX, sem apagar arquivos; configuração/identidades/seq preservadas | `ResetHttpTest`, `JdbcResetTest`; parcelas T09/T13/T19, rollback statement/COMMIT, outra base/cenário, seis assuntos e replay | Implementada tecnicamente; retenção e API administrativas derivadas |
| Barreira de reset | Trabalho antigo inelegível após purge/geração, abertura reautentica dentroTX e login serializa conta/tempo/save; sem token com offset antigo ou ID reutilizado | `JdbcResetTest`, `ArchitectureTest`; cálculo pausado, duas ordens login/abertura, high-water900, reinício | Implementada; sem ciclo entre capacidades ou migração nova |
| Descarte explícito de lote | Rota explícita | Exclusão do contrato inicial | Não implementada |
| Regras fiscais / vínculos entre assuntos / CONs | Evolução sem catálogo aprovado | Issue #27 | Não implementada |
| Aceites humanos / T01 / E1–E5 | Integrador real, referência independente e reprodução | Issues #8, #19, #20, #22, #26 | Pendente |
| Docker Linux amd64 | Imagem mínima, volume local e runtime Java21 | Dockerfile; supervisão da #2 com 178 verificações HTTP/reinício sem rede, imagem identificada na evidência | Implementada e exercitada localmente no jar da #2 |

A pendência de aceite E1 e a continuidade técnica E2 autorizada estão delimitadas
na [seção 13.1 do planejamento](planejamento-simulador-tributario.md#131-pendência-e1-e-continuidade-de-e2).

Evidência da #2: [execução local](evidencias/issue-2.md), 51 testes aprovados no total,
178 verificações Docker e revisão independente sem achados materiais.

Evidência da #3: [execução local](evidencias/issue-3.md), 61 testes aprovados no total,
revisão independente sem achados materiais e 767 verificações na imagem V3,
incluindo upgrade de base V2 preenchida, HTTP, reinício e leitura decimal em TEXT.
As 163 verificações de preparação na imagem anterior são contabilizadas separadamente.

Evidência da #4: [execução local](evidencias/issue-4.md), 71 testes aprovados no total,
revisão independente sem achados materiais e 853 verificações na imagem V4,
incluindo upgrade de V3 preenchida, HTTP, reinício e leitura exata da tabela comum
TEXT. As 704 verificações de preparação na imagem V3 são contabilizadas separadamente.

Evidência da #5: [execução local](evidencias/issue-5.md), 82 testes aprovados no total,
revisão independente sem achados materiais e 794 verificações na imagem V5,
incluindo upgrade de V4 preenchida, HTTP, reinício e valores TEXT exatos dos três
assuntos monetários. As 771 verificações de preparação em V4 são contabilizadas separadamente.

Evidência da #6: [execução local](evidencias/issue-6.md), 95 testes aprovados no total,
revisão independente sem achados materiais e 729 verificações no jar macOS arm64,
incluindo upgrade, reinício e quatro valores nomeados exatos por saldo. Após
recuperação do ambiente, **742 verificações na imagem V6 Linux amd64** passaram,
incluindo upgrade real de V5, expiração da sessão antiga, novo login, reinício e
mais de 50.000 valores TEXT exatos; preparação V5 contabilizada separadamente.
Janeiro é demonstração estrutural; T01/T16 integral, cenário complementar pelo
integrador real e saída E1 permanecem pendentes.

Evidência da #7: [execução local](evidencias/issue-7.md), **105 testes aprovados**,
revisão independente sem achados materiais e **678 verificações na imagem V7
Linux amd64**, incluindo upgrade real de V6 preenchida, hashes/contagens de dez
tabelas preservados, reinício, 10.001 prazos e ausência de valores monetários para
prazo. As 1.350 verificações de preparação V6 são contabilizadas separadamente.
Ausência de DTO extraído permanece limite de evidência;
prescrição fiscal, relação com crédito e cenário complementar pelo integrador
quando ausente no piloto continuam pendentes.

A suíte automatizada destas fatias não conclui integralmente T10/T11 nem substitui
renovação pelo integrador real. Recepção, processamento e regras ausentes não
recebem aprovação fiscal ou homologação implícita.

Evidência da #9: [execução local](evidencias/issue-9.md), **137 testes aprovados**
na suíte limpa da supervisão, incluindo 32 novos casos HTTP/SQLite; revisão final
aprovada após correções temporais. **1.094 verificações na imagem final V8 Linux
amd64 sem rede**, incluindo migração real V7, reinício, 10.001 créditos processados,
conteúdo/precisão/origem, concorrência manual/automático e limite temporal system.
As 1.879 verificações de preparação V7 e 3 do snapshot anterior são separadas.
Continuidade técnica E2 com #8 pendente; sem aceite humano E1/T01 ou validação
fiscal implícitos. Publicação/merge autorizados e concluídos no
[PR #36](https://github.com/lukadev08/fake-esfinge/pull/36), head `5e9e8a4`, merge
`eb376d07fd8efe915d2588cb69e4a0f41b1dcf75`; #9 completed, #8 aberta.


Evidência da #10: [execução local](evidencias/issue-10.md), **155 testes** na suíte
limpa da supervisão e revisão independente aprovada, sem achados materiais.
**1.279 verificações na imagem final V9 Linux amd64 sem rede**, com upgrade real V8,
quinze tabelas preservadas, reinício, 17 totalizadores, 10.001 créditos, conteúdo
efetivo exato, disputas e fechamento/histórico persistidos. As 538 verificações da
preparação V8 e 3 do snapshot anterior são separadas. Sem regras fiscais,
recuperação, sem movimento, T17 ou aceite humano implícitos; #8/E1 e T01/E3
pendentes. Publicação/merge autorizados e concluídos no
[PR #37](https://github.com/lukadev08/fake-esfinge/pull/37), head `d47c751`, merge
`4de2ba5e8c799e388ecd5c6a89bcc1a33d32ff1f`; #10 completed, #8 aberta.


Evidência da #11: [execução local](evidencias/issue-11.md), **186 testes** na suíte
limpa do supervisor e revisão final aprovada após correção dos DTOs HTTP.
**1.555 verificações na imagem final V10 Linux amd64 sem rede**, incluindo upgrade
V9 real, quinze tabelas preservadas, plano/terminais após reinícios, primeiro/
intermediário/último, diagnóstico, ativos/histórico, finalização e execução manual/
automática concorrente. As 105 verificações de preparação V9 e3 de snapshot são
separadas. Injeção aproximada; regra fiscal T04, integrador T01/E3, aceite #8/E1,
recuperação integral, CI remoto e homologação não comprovados. Publicação/merge
da #11 autorizados e concluídos no
[PR #38](https://github.com/lukadev08/fake-esfinge/pull/38), head `1b435cf`, merge
`075236636077093b4493952af0c4951824689433`; #11 completed, #8 aberta.


Evidência da #12: [execução local](evidencias/issue-12.md). Sem migração nova:
V1–V10 preservadas; SQLite real cobre exclusões com FK, rollback statement/COMMIT,
reinício, reenvio com multiplicidade, seis assuntos e concorrência entre instâncias.
Revisão independente aprovada; executor verify e supervisor clean verify com
**212 testes**, zero falhas/erros/ignorados. **1.524 verificações na imagem final
V10 Linux amd64 sem rede**, incluindo reabertura de16 tabelas, descarte/reenvio,
reinício, conteúdo exato e agendador automático. Preparação anterior259+3 separada.
Limpeza de RECEBIDO é aproximação expressa. Integrador real, #8/E1, T01/E3, CI
remoto e homologação não demonstrados. Publicação/merge autorizados e concluídos
no [PR #39](https://github.com/lukadev08/fake-esfinge/pull/39), head `a863140`, merge
`f2d7e58aaaf1fd1021c6b05f9471560046963baa`; #12 completed, #8 aberta.

Evidência da #13: [execução local](evidencias/issue-13.md). V11 acrescenta marcador
de cancelamento separado da finalização e amplia o terminal ABORTADO, preservando
V1–V10, dados, diagnósticos, planos e sequências históricas. HTTP/SQLite cobrem
remoção integral, seis assuntos, nova chave/reenvio, rollback statement/COMMIT,
concorrência e resultado de cálculo capturado antes do cancelamento. Integrador
real, #8/E1, T01/E3, CI remoto e homologação não demonstrados. Revisão independente
aprovada; executor verify e supervisor clean verify com **243 testes**, zero
falhas/erros/ignorados, exit0. **1.807 verificações na imagem final V11 Linux amd64
sem rede**, incluindo upgrade V10/16 tabelas, cancelamento/nova chave/reenvio,
reinício, conteúdo exato e agendador automático. Preparação anterior270+3 separada.
Publicação/merge autorizados e concluídos no
[PR #40](https://github.com/lukadev08/fake-esfinge/pull/40), head `c3714b9`, merge
`566afda0c3ac9c6f1f623d752407616978b5cd93`; #13 completed, #8 aberta.
Execução foi encerrada após a #13 a pedido do solicitante e retomada em 27/10.

Evidência da #14: [execução local](evidencias/issue-14.md). V12 aditiva preserva
V1–V11, dados e sequências. Executor verify e supervisão clean verify: **263 testes**,
zero falhas/erros/ignorados, exit0; 20 casos novos de transporte real/JDBC.
**51 verificações de smoke** no jar local e outras **51 na imagem final Linux
amd64**:22 de preparação e29 de recuperação/finalização por execução, com reinício.
Revisão independente do código e conferência documental final aprovadas sem
achados materiais. SQLite da imagem íntegro e sem violações de FK; imagem
e jar identificados na evidência. Sem aceite humano #8/E1, T01/E3, CI remoto,
regras fiscais ou homologação implícitos.

Publicação/merge da #14 concluídos em27/10/2026 no
[PR #41](https://github.com/lukadev08/fake-esfinge/pull/41), head `32882ce`,
merge `f4ba83693ce9d12900ff140248b6dc3661cb193e`; #14 completed.
Checks remotos vazios; resultados acima são locais.

Evidência da #15: [execução local](evidencias/issue-15.md). V13 aditiva preserva
V1–V12, dados, planos, checksums e sequências; upgrade de V12 preenchida, falha
de migração, reinício, rollback statement/COMMIT e corrida verificam o bloqueio.
Executor `verify` e supervisão `clean verify`: **275 testes**, zero falhas/erros/
ignorados, exit0; 12 novos HTTP/SQLite. Smoke do jar local: **51 verificações**,
33 antes e 18 após reinício, com seis mutações 403, isolamento, trabalho anterior,
desbloqueio e decimal exato. Na imagem final Linux amd64, outras **51 verificações**
passaram com reinício, integridade SQLite e hash do jar conferidos. Código aprovado
por revisão independente sem achados; conferência documental final também
aprovada. Publicação/merge concluídos no
[PR #42](https://github.com/lukadev08/fake-esfinge/pull/42), head `70ee641`, merge
`df77df16a131c5a419648ce472915a237a6a3c70`; #15 completed. Check rollup vazio,
sem CI remoto; encerramento documental não alterou código ou repetiu testes.
Próxima #16 explorada, ainda não implementada. Reset #17,
#8/E1, T01/E3, CI remoto, regras fiscais e homologação não demonstrados.

Evidência da #16: [execução local](evidencias/issue-16.md). V14 aditiva, backfill
Java transacional exato e agenda por envio. Novos 25 casos JDBC e 4 HTTP cobrem
nanos/limite inclusivo, renovação/ausência de renovação, token, histórico,
isolamento, reinício, upgrade preenchido, rollback statement/COMMIT e cálculo
concorrente. Executor `verify` e supervisão `clean verify`: **304 testes**, zero
falhas/erros/ignorados. Smoke no jar copiado e na imagem final Linux amd64:
**56 verificações em cada**, com avanço virtual/reinício/expiração/nova chave e
decimal exato. Jar interno conferido, SQLite íntegro/FKs sem violações.
Código/testes e conferência documental final aprovados por revisão independente
sem achados. Reset #17, inspeção #18, #8/E1, integrador/T01/E3, CI remoto, regras
fiscais e homologação continuam pendentes. Publicação e merge concluídos no
[PR #43](https://github.com/lukadev08/fake-esfinge/pull/43), head `24287a8`, merge
`9411e16fb095170a405f57b3d9d66e287b88f232`; #16 completed. Check rollup vazio,
sem CI remoto. Encerramento documental não alterou código ou repetiu testes;
próxima #17 explorada, ainda não implementada.

Evidência da #17: [execução local](evidencias/issue-17.md). Sem migração nova:
V1–V14 permanecem intactas. Reset coordenado em remessas usa portas públicas de
cenários e autenticação; cada adaptador escreve apenas tabelas da sua capacidade.
Executor `verify` e supervisão `clean verify`: **326 testes**, zero falhas/erros/ignorados, incluindo22 novos
HTTP/SQLite. Focal104 verde. Smoke do jar copiado: **63 verificações**,34 antes e29
após reinício, com reset explícito, invalidação, defaults, IDs novos, decimal
exato e finalização. Outras **63 verificações** na imagem final Linux amd64
passaram com reinício, integridade/FKs SQLite e hash do jar conferidos. Código e
testes e conferência documental final aprovados por revisão independente, sem
achados materiais. Publicação pendente.
Remoção de histórico/payloads é deliberada e restrita ao cenário confirmado;
fixtures não aprovam #8/E1, integrador/T01/E3 ou homologação. #18/CI remoto e
regras fiscais continuam pendentes.
