# Plano local — issue #15

Referência: [#15](https://github.com/lukadev08/fake-esfinge/issues/15), T09/T10,
contrato derivado 2026/V6/6.06. Base funcional `f4ba836` (PR #41), registro
`fe5919d`; branch `codex/issue-15-bloqueio-competencia`. #8/E1 permanece pendente,
com desenvolvimento técnico E2 autorizado pelo planejamento §13.1.

## Contrato administrativo

GET/PUT `/__simulator/competencias/{ug}/{anoMes}` exigem `X-Simulator-Key`, nenhuma
query e UG fictícia configurada no cenário atual. `ug` é string opaca, sem
normalizar zeros; `anoMes` tem exatamente seis dígitos ASCII de 202601 a 202612.
GET exige corpo vazio. PUT exige JSON UTF-8 estrito, exatamente
`{"bloqueada":true}` ou `{"bloqueada":false}`, sem coerção, duplicados ou extras.
Ambos retornam 200 com `ug`, `anoMes` numérico e `bloqueada` booleano. Sem linha
de bloqueio, uma combinação válida retorna `bloqueada:false`.

Chave ausente/inválida ou token compatível no lugar da chave: 401. UG não
configurada: 404. Query, corpo, mídia, UTF-8, JSON ou competência inválidos: 400.
Autorização administrativa vem antes da validação do alvo. Não há seleção de
outro cenário no transporte. GET não escreve; PUT altera somente o bloqueio,
sem renovar atividade compatível ou criar eventos de remessa artificiais.

## Decisões e transações

| Condição / ação | Resultado e efeito |
| --- | --- |
| UG/mês sem bloqueio | Fluxo compatível existente |
| Nova abertura em UG/mês bloqueado | 403 `SIM_ACESSO_NEGADO`, nenhuma remessa/evento |
| Receber, disparar, finalizar, descartar ou cancelar envio do alvo bloqueado | 403 antes de efeitos, atividade, planos ou trabalhos |
| Recepção bloqueada com plano da #14 | Não conta/consome tentativa nem cria marca |
| Token inválido ou chave de outro contexto | 401/404 existentes, sem revelar bloqueio alheio |
| Consulta compatível autorizada | Continua disponível; renova atividade segundo contrato existente, sem processar |
| Administração durante bloqueio | Mantém acesso e precondições próprias; não renova atividade |
| Trabalho aceito antes do bloqueio | Continua pelo executor existente, inclusive conclusão; não é cancelado/suspenso |
| PUT false | Remove bloqueio; mutações posteriores voltam às regras existentes |
| Falha de statement/COMMIT no PUT | Rollback integral, 500 sanitizado |

Bloqueio é prospectivo sobre novas mutações compatíveis. Essa política derivada
não inventa ratificação na API do integrador nem altera trabalhos já aceitos.
Cada guarda roda na mesma transação SQLite IMMEDIATE dos seus efeitos. PUT e
mutação concorrentes são serializados: chamada aceita antes do bloqueio conclui;
depois do bloqueio dá 403, inclusive em chave encerrada; sem bloqueio, a guarda
própria de chave fechada continua válida.

## Menor alteração

- V13 aditiva cria `competencias_bloqueadas(cenario,ug,competencia)`, PK composta,
  FK de cenário e CHECK do perfil anual. Linha significa bloqueada; PUT false
  exclui a linha. V1–V12/fontes/schemas/build/deps permanecem intactos.
- Reutilizar `Remessas`/`RemessaService` para consultar/configurar e
  `RemessaRepository`/JDBC para ler/gravar estado; um record de domínio representa
  o resultado. A inspeção autenticada fornece cenário e UGs configuradas.
- Guardas em `RemessaService.open/receive/finish` e
  `ProcessingService.start/discard/cancel`, com leitura por `RemessaRepository`.
  `receive` verifica antes de `receptionAttempt()`. Não alterar tick/step/complete,
  login, consulta ou controles administrativos da #14.
- Controller administrativo próprio no padrão estrito existente; categoria
  explícita de bloqueio em `RemessaFailure`/`RemessaErrors` traduz para 403 próprio.
- Fixture `LegacyRemessaRepository` retorna sem bloqueio nos schemas anteriores;
  testes de upgrade são ajustados somente para a nova tabela/versão aditivas.

## Verificação

Comandos inspecionados: Maven Wrapper 3.9.9/JDK 21, SQLite temporário, sem TCE ou
dados originais. Runtime/cache temporários já existentes. Primeiro RED/GREEN
vertical HTTP, depois focais HTTP/JDBC e `verify`; supervisão executará
`clean verify` e smoke da imagem final Linux amd64.

Cobrir as seis mutações, desbloqueio, GET sem efeitos, consultas sem processamento,
duas UGs/dois meses/cenários, plano da #14 intacto, reinício, upgrade V12 preenchida,
rollback statement/COMMIT, corrida bloqueio/recepção e conclusão de trabalho aceito.
README, cobertura, evidência, rascunho do PR e PROGRESS registrarão resultados reais.
Reset/repetição pertence à #17; aceites humanos/T01/E3 e regras fiscais continuam
pendentes. Nenhum commit, push ou publicação será feito pelo executor.

## Execução técnica concluída — 27/10/2026

Desenho aprovado pela supervisão antes do código. RED/GREEN vertical, seis
testes HTTP e seis JDBC novos; focal 119 e executor `verify` 275 verdes.
Supervisor `clean verify` confirmou 275 testes, zero falhas/erros/ignorados;
jar final `088f5a171fdde9886db840ede5dfc568e8b8e7deebc77f111bce96d5f8c5a912`.
Smoke no jar local e na imagem final Linux amd64: 51 verificações em cada,
33 antes e 18 após reinício. SQLite íntegro/FKs sem violações, hash do jar
na imagem igual ao final. Código aprovado pelo reviewer próprio sem achados;
conferência documental final também aprovada, sem achados materiais. Nenhuma produção/teste/smoke mudou
depois da suíte; detalhes e incidentes operacionais na
[evidência](../docs/evidencias/issue-15.md). Container temporário ausente,
nenhum volume Docker criado, bindmount/base fictícios tmp preservados.
