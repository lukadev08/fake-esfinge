# Evidência local — issue #13

Data: 27/10/2026. Perfil: 2026/V6/6.06. Base funcional `f2d7e58`, plano `b296dc5`,
branch `issue-13-cancelamento-envio`. Dados exclusivamente fictícios em SQLite
temporário; sem integrador, produção ou TCE. Publicação da #13 não autorizada.

## Implementação

POST `/servicosGerais/cancelarEnvioParcial`, `auth_token`, query exata
`chavePacote`, corpo vazio e 200 vazio. Transporte inválido400, autenticação401,
chave isolada404, finalizada/cancelada409. Cancelamento integral permite ciclos
agendados ou EM; não reutiliza a guarda de descarte por assunto.

Porta Processamento, serviço e JDBC existentes coordenam a mesma transação
IMMEDIATE: marcador de cancelamento distinto da finalização, remoção de dados
ativos/inativação e eventos. Pendentes RECEBIDO sem trabalho/agendado/EM tornam-se
ABORTADO/SIM_ENVIO_CANCELADO, mensagem fixa e registro null; sucesso histórico
mantém estado/diagnóstico ausente. Bytes/hash/quantidade original, planos e
históricos já inativos ficam preservados; outros envios/UGs/cenários não mudam.

V11 adiciona `cancelada_em`, ajusta a unicidade da abertura e reconstrói somente
os trabalhos para ABORTADO, copiando todas as colunas antigas, inclusive
diagnósticos/nulls/IDs/seq. V1–V10/fontes/schemas/build/dependências intactos.
Guardas de recepção/disparo/next/current/planos/finalização/consulta incluem o
cancelamento; begin/complete/fail antigos não gravam após encerramento.
Não há incremento de geração global ou aplicação de plano antigo ao reenvio.

Nova abertura na mesma UG/competência tem chave distinta e reenvio integral com
novos IDs. Chave antiga rejeita mutações; consulta histórica não toca atividade,
gera eventos ou conduz processamento. Cancelar aberto sem lotes é permitido;
cancelar concluído é rejeitado. Resposta/diagnóstico/situação são escolhas do
simulador, sem equivalência oficial comprovada.

## TDD e validação do executor

Comando inspecionado: Maven Wrapper3.9.9, Java21, dependências públicas/cache e
SQLite temporário. Nenhum Docker ou Git de mutação executado pelo executor.

```sh
rtk proxy env JAVA_HOME=/Users/luka/.sdkman/candidates/java/current \
  MAVEN_USER_HOME=/private/tmp/fake-esfinge-wrapper-check-fixed \
  ./mvnw -B -ntp -Dmaven.repo.local=/private/tmp/fake-esfinge-m2 \
  -Dtest=CancelamentoHttpTest#cancelaPendenteEReenviaIntegralmenteComNovaChave test
```

- Preparação inicial: helper de teste referenciou método inexistente, compilação
  exit1,1,724s. Corrigida somente fixture; não constitui RED comportamental.
- RED: um teste, falha `Status expected:<200> but was:<422>`, exit1,3,823s.
- GREEN: mesmo teste, zero falhas/erros/ignorados, exit0,4,052s.
- Ampliação HTTP:12 testes, zero falhas/erros/ignorados, exit0,6,148s.
- Ampliação JDBC inicial:15 testes,6 erros de fixture INVALID_STRUCTURE, exit1,
  4,276s. Totais precisam dos seis assuntos; corrigida preparação e ordem dos
  ciclos para manter o alvo EM, sem mudar produção.
- JDBC após ajuste:15 testes verdes, exit0,4,641s. Ampliação para18 encontrou
  expectativa incorreta na corrida cancelar/cancelar quando a segunda instância
  venceu; corrigido somente rótulo do resultado, exit1,5,670s anterior registrado.
- Caso focado de high-water77 mesmo sem trabalhos restantes passou sem alteração
  de produção:1 teste, exit0,2,763s.
- Focais novos e regressões de migração/recuperação:115 testes em7 classes, zero
  falhas/erros/ignorados, exit0,17,917s. Novos:12 HTTP +19 JDBC.

Suíte final do executor usou o mesmo comando sem `-Dtest`, com `verify`:
**243 testes**, zero falhas/erros/ignorados, exit0,27,730s. Código/testes congelados;
supervisor `clean verify`: **243 testes**, zero falhas/erros/ignorados, exit0,
29,569s,26 relatórios Surefire conferidos. Nenhum código/teste mudou após a suíte.
Avisos herdados de Mockito/ByteBuddy/JDK não impediram os focais.

## Revisão e supervisão final

Equipe própria: `grupo13_explorer` (configurado GPT-6 Luna/médio),
`grupo13_executor` (GPT-6.1 Sol/baixo) e `grupo13_reviewer` (GPT-6.1 Sol/médio).
Papéis configurados distinguem-se da variante efetiva de runtime, não exposta.
Explorer reconferiu o plano antes do código sem conflito material. Reviewer
independente aprovou diff/código conectado, sem achados materiais; não executou
Maven/Docker. Primeira tentativa de revisão interrompida por limite de uso;
consulta fresca indicou uso normal disponível e a retomada concluiu, sem troca
de modelo ou consumo de reset. Nenhuma correção de produção após a revisão.
O mesmo reviewer aprovou a consolidação documental sem achados materiais,
reconferindo26 relatórios/243 testes, hash do jar e88 links locais; sem novas
execuções Maven/Docker ou edições.

Jar `target/fake-esfinge-0.1.0.jar`, SHA-256
`f8eddafce576521d06ed9bfed1b11096e8aef1c457b4e1f6e99d0e8bd7332150`.
Imagem local `fake-esfinge:issue-13-local`, ID
`sha256:a6478f79b169a4df2ad26f497d96eaf4db72ab62bc6087a92f0933ef04bffa80`,
Linux amd64, Temurin21.0.10+7, usuário10001:10001, rede `none`, volume `/data`
gravável e clientes temporários em `/checks` somente leitura. Hash do jar no
container igual ao artefato da suíte. Sem publicação de imagem ou implantação.

Preparação independente com imagem #12/V10: **270 verificações** e **3 de
snapshot anterior**, exit0, separadas da imagem final. Incluiu remessa concluída,
seis assuntos, erro/descarte/abortos e planos históricos, sucesso inativado pelo
descarte da #12, RECEBIDO sem trabalho, outro agendado e outra UG. Dois lotes
distintos com10.001 registros permitiram comparar preservação e remoção integral.
High-water de trabalhos V10 foi preparado acima do maior ID para provar o upgrade.

| Cliente temporário / etapa na imagem final | Verificações | Resultado |
| --- | ---: | --- |
| `MigrationSnapshot13 after` — V10→V11,16 tabelas/colunas antigas | 22 | exit0 |
| `ContainerCheck13 first` — cancelamento integral, nova chave e reenvio | 838 | exit0 |
| `ContainerCheck13 restart` — estado/histórico após reinício | 648 | exit0 |
| `SqliteCheck13` — conteúdo/vínculos/decimais/FKs somente leitura | 112 | exit0 |
| `ContainerCheck13 auto` — cancelamento explícito com agendador automático | 187 | exit0 |
| **Total da imagem final** | **1.807** | **Todos verdes** |

HTTP confirmou rejeições estritas, isolamento,200 vazio e409 sem efeitos em
repetição/finalizada/todas as mutações na chave cancelada. Foram cancelados envio
vazio, lote agendado, lote grande EM com plano ainda não aplicado e seis assuntos
RECEBIDO sem trabalho. Outros envios/UGs/concluídos ficaram intactos. Nova chave
na mesma competência aceitou reenvio integral, processamento e finalização; plano
antigo não foi herdado. Consulta histórica não renovou atividade ou eventos.

Após reinício:33 lotes,18 ativos e15 inativos;20.034 registros originais
arquivados e32 trabalhos terminais. SQLite confirmou10.017 processados/efetivos,
10.021 valores monetários TEXT exatos e nenhum dado ativo residual em cancelados.
Três remessas concluídas, quatro canceladas com finalizada_em null e uma aberta
de outra UG; três sucessos históricos, nove ABORTADO e antigos diagnósticos/
planos/bytes preservados. FKs/integridade/high-water e relógio persistidos.
No automático, cancelamento explícito após EM e espera além do vencimento real
não recriaram efeitos; nova chave concluiu só pelo agendador, sem avanço manual.

Cliente temporário corrigido antes da contagem final: assinatura de helper na
compilação e comparação que confundia quantidade de trabalhos com maior ID.
A primeira execução completou o fluxo, mas falhou nessa última asserção; leitura
direta confirmou high-water117/novo trabalho119 e integridade/contagens corretas.
Repetidos preparação/snapshot/ensaio em volume novo com cliente corrigido, sem
mudar aplicação/testes. Primeira execução e sondagens de diagnóstico não somam
ao total; só a repetição integral acima e o automático verde são contados.
Avisos SLF4J dos clientes não impediram validação. Logs dos containers finais
continham apenas inicialização, sem erro ou dados recebidos.

Containers removidos, ausência confirmada. Três volumes fictícios preservados
no contexto `orbstack`: `fake-esfinge-issue13-supervisao`,
`fake-esfinge-issue13-supervisao-repro` e `fake-esfinge-issue13-supervisao-auto`.
Exclusão depende de autorização específica. Clientes/logs permanecem temporários
locais; nenhum token, payload, SQL ou dado original foi adicionado à evidência.

## Cobertura e limites

HTTP prova query/corpo/token estritos, cancelamento de pendente, reenvio e
finalização em nova chave, seis assuntos, diagnóstico/null, token expirado/
renovação, chave vazia, 404 isolado e 409 encerrada/repetida. Triggers reais de
statement e COMMIT após fechamento/exclusões/terminal/evento demonstram rollback
integral e 500 sanitizado, sem token/chave/SQL/stack trace no corpo ou logs.

SQLite real prova seis assuntos, múltiplos registros, originais exatos, histórico
de sucesso/erro/aborto, plano antigo preservado e demais UGs/cenários/concluídos
intactos. Corridas de duas instâncias cobrem receber/disparar/concluir/finalizar/
abrir/cancelar; cálculo pausado fora da transação retorna sem efeitos depois do
cancelamento. Trabalhos antigos begin/complete/fail são inertes após reinício.
10.001 registros cancelados conservam bytes/hash/multiplicidade no novo envio.

Upgrade de V10 preenchida conserva16 tabelas, todas as colunas antigas,
diagnósticos, planos, bytes, sessões, configuração, relógio, sequências/FKs;
high-water acima de MAX(id), inclusive tabela vazia, é mantido. Falha no meio de
V11 reverte marcador/índice/reconstrução/versão integralmente; checksum inválido
não recria nem perde a base. Fixtures de versões anteriores continuam reais.

Matriz: parcelas T08/T10/T11/T13/T14/T15/T18/T19 e contagens sintéticas de T01.
#8/E1 e T01/E3 continuam pendentes. Sem integrador real, aceite humano, CI remoto
ou homologação. Cancelamento automático, resposta perdida, reset, expiração e
regras fiscais não são implementados nesta fatia. Docker/revisão foram concluídos
pela supervisão nas etapas acima. #13 aberta, sem comentários; publicação/merge
aguardam autorização específica. Próxima fatia identificada: #14, resposta perdida.
