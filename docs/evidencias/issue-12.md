# Evidência local — issue #12

Data: 27/10/2026. Perfil: 2026/V6/6.06. Base funcional `0752366`, preparação
`71907a4`, branch `issue-12-descarte-assunto`. Dados exclusivamente fictícios em
SQLite temporário; sem acesso ao integrador, produção ou TCE. Publicação/merge
autorizados e concluídos no [PR #39](https://github.com/lukadev08/fake-esfinge/pull/39).

## Implementação

POST `/v6/tributario/descarteParcialPorAssunto`, `auth_token`, query exata
`chavePacote`/`assunto`, corpo vazio;200 vazio. Transporte inválido400, assunto
sem cobertura422, autenticação401, chave isolada404, fechada/ciclo ativo409.
Guarda global reutiliza `active(remessa,geração)`; RECEBIDO agendado e EM de
qualquer assunto da chave impedem remoção. Outras chaves não bloqueiam.

Caso de uso na porta `Processamento`, serviço e adaptador JDBC existentes.
Transação SQLite IMMEDIATE remove efetivos→processados→valores recebidos e
inativa todos os ativos do assunto, conservando bytes/hash/quantidade original,
planos, outros assuntos e históricos. V1–V10 intactas; nenhuma migração nova.
PROCESSADO_SUCESSO conserva estado/diagnóstico ausente e ganha evento de dados
removidos. RECEBIDO sem trabalho vira LOTE_DESCARTADO/SIM_DESCARTE_ASSUNTO,
mensagem fixa sanitizada e registro null. Esse comportamento é aproximação
expressa de E0, divergente da restrição V6; não comprova perda de resposta.

Sucesso sem dados/repetição gera atividade/evento por assunto. Reenvio conserva
chave e usa ID/ordem próprios, mesmo hash/bytes, sem plano herdado. Finalização
conta somente ativos/efetivos, exigindo pelo menos um ativo. Controlador retorna
void; cancelamento e descarte explícito de lote continuam422.

## TDD e validação do executor

Comando inspecionado: Maven Wrapper3.9.9, Java21, dependências públicas/cache e
SQLite temporário. Nenhum comando Docker ou Git de mutação executado.

```sh
rtk proxy env JAVA_HOME=/Users/luka/.sdkman/candidates/java/current \
  MAVEN_USER_HOME=/private/tmp/fake-esfinge-wrapper-check-fixed \
  ./mvnw -B -ntp -Dmaven.repo.local=/private/tmp/fake-esfinge-m2 \
  -Dtest=DescarteAssuntoHttpTest#descarteHttpRemoveDadosPreservaSucessoEPermiteReenvio test
```

- RED:1 teste, falha `Status expected:<200> but was:<422>`, exit1,3.960s.
- GREEN: mesmo teste após mudança mínima,1 teste sem falha/erro/skipped,
  exit0,4.002s.
- Ampliação inicial HTTP:5 testes,1 falha de expectativa de fixture, exit1,4.691s.
  Inspeção inclui eventos de toda a remessa; corrigida comparação dos campos e
  preservação dos eventos anteriores, sem alterar produção.
- Ampliação HTTP/JDBC:19 testes,6 falhas na contagem de tabelas da fixture,
  exit1,7.293s. V10 tem16 tabelas incluindo sqlite_sequence,15 sem ela; ajuste
  somente no teste, sem schema dinâmico em produção.
- Focal HTTP/JDBC/autenticação:28 testes, zero falhas/erros/skipped, exit0,7.474s.
  Depois foram incluídos seis casos HTTP por enum e simplificados imports.

Suíte final do executor: mesmo comando sem `-Dtest`, com `verify`: **212 testes**,
zero falhas/erros/ignorados, exit0,26,814s. Supervisão independente com `clean
verify`: **212 testes**, zero falhas/erros/ignorados, exit0,25,328s; 24 relatórios
Surefire conferidos. Nenhum código ou teste mudou após essa suíte.

## Cobertura dos testes novos

`DescarteAssuntoHttpTest`:200 vazio, sucesso histórico e bytes preservados;
erro→descarte→reenvio→processamento→finalização, diagnóstico antigo e demais
assuntos; seis identificadores; parâmetros/corpo estritos,401 antes de efeitos,
token expirado e renovação,404 isolado,409 fechado,422 sem suporte. Falhas reais
statement/COMMIT após exclusões e evento retornam500 sanitizado, rollback integral
e retomada; logs e corpo sem senha/token/chave/SQL/stack trace.

`JdbcSubjectDiscardTest`: seis assuntos, múltiplos lotes/registros, decimal exato,
bytes/hash/quantidade originais, planos históricos sem reaplicação, isolamento
UG/cenário/envio, reabertura de V10 preenchida conservando16 tabelas/schema/
checksums/IDs/sequências/dados. 10.001 registros preservam multiplicidade no
reenvio/finalização. Sem ativos409; sem dados/repetição renovam atividade sem
reescrever controles. Agendado/EM de outro assunto bloqueiam a chave inteira;
outra chave não bloqueia. Duas instâncias exercitam descarte concorrente com
recepção/disparo/última conclusão/finalização/outro descarte, preservando resultado
linearizável, FK e nenhuma escrita tardia após reinício.

Matriz: parcelas T06/T07/T08/T10/T11/T13/T15/T18/T19 e contagens sintéticas de T01;
T01 completo exige integrador real e esperado independente.

## Revisão e supervisão final

Equipe própria: `grupo12_explorer` (configurado GPT-6 Luna/médio),
`grupo12_executor` (GPT-6.1 Sol/baixo) e `grupo12_reviewer` (GPT-6.1 Sol/médio).
Papéis configurados distinguem-se da variante efetiva de runtime, não exposta.
Revisão independente do diff e código conectado aprovada, sem achados materiais;
reviewer não executou Maven/Docker. Não houve correção de produção após revisão.
`grupo12_docreviewer` (configurado GPT-6.1 Sol/médio, variante efetiva não exposta)
aprovou também a consolidação documental sem achados materiais; conferiu os24
relatórios/212 testes e hash do jar, sem executar Maven/Docker ou editar arquivos.

Jar `target/fake-esfinge-0.1.0.jar`, SHA-256
`f4839cce252e5f74c2a100e4d47cc209e8e694072e9b15000e79997c4419cc38`.
Imagem local `fake-esfinge:issue-12-local`, ID
`sha256:d3e185d37540618abc5a79d442ac104aca7a17ebbe49f91d3b18d77eb29fc83c`,
Linux amd64, Temurin21.0.10+7, usuário10001:10001, rede `none`, volume `/data`
gravável e clientes temporários em `/checks` somente leitura. Hash do jar no
container igual ao artefato da suíte. Nenhuma imagem publicada ou implantação.

Preparação independente com a imagem anterior #11 em V10: **259 verificações**
e **3 de snapshot anterior**, exit0, separadas da contagem da imagem final.
Incluiu remessa finalizada com 10.001 registros em um lote, erro intermediário/
aborto, plano histórico, seis assuntos RECEBIDO sem trabalho, ciclo agendado e
outra UG. Comparação após reabrir a mesma base com a imagem final preservou bytes,
colunas, linhas e sequências das16 tabelas, sessões, configuração e tempo.

| Cliente temporário / etapa na imagem final | Verificações | Resultado |
| --- | ---: | --- |
| `MigrationSnapshot12 after` — reabertura V10 | 21 | exit0 |
| `ContainerCheck12 first` — remoção, reenvio e finalização | 850 | exit0 |
| `ContainerCheck12 restart` — estado e histórico após reinício | 393 | exit0 |
| `SqliteCheck12` — conteúdo/vínculos/decimais/FKs somente leitura | 99 | exit0 |
| `ContainerCheck12 auto` — agendador real sem avanço manual | 161 | exit0 |
| **Total da imagem final** | **1.524** | **Todos verdes** |

HTTP confirmou query/corpo/autenticação estritos,422 sem suporte,404 isolado,
409 fechado e guarda global para agendado/EM de outro assunto da chave. Outra
chave EM não bloqueou o descarte. Repetição e assunto vazio retornaram200 vazio,
renovando atividade/evento sem reescrever controles. Sucesso histórico continuou
sucesso sem diagnóstico; RECEBIDO removido ganhou diagnóstico próprio/null.
Reenvio idêntico recebeu IDs próprios, preservou multiplicidade e não herdou plano.

Após reinício:28 controles de lote,16 ativos e12 inativos,10.029 registros
originais arquivados,27 trabalhos terminais. SQLite confirmou10.015 processados/
efetivos e10.025 valores monetários TEXT exatos, sem linhas ativas residuais em
lotes inativos; quatro remessas finalizadas, outra UG aberta e intacta, planos/
eventos/high-water e integridade/FKs preservados. Duas remoções mantiveram sucesso
histórico; sete RECEBIDO foram descartados com SIM_DESCARTE_ASSUNTO. Nenhuma
reaplicação tardia. Automático concluiu por agendador e consulta SQL somente leitura.

Na primeira execução, a saída do cliente final não ficou disponível integralmente.
O checkpoint final confirmou sua conclusão; reaplicar o comparador anterior já
sobre a base modificada acusou diferença esperada. Supervisão repetiu preparação,
snapshot e ensaio em volume novo; somente a repetição integral acima entra no
total. Nenhuma alteração de produção/teste foi necessária. Avisos SLF4J dos
clientes e Mockito/ByteBuddy/JDK da suíte não impediram validação. Logs dos dois
containers finais continham apenas inicialização, sem erro ou dados recebidos.

Containers temporários removidos, ausência confirmada. Três volumes fictícios
excluídos com autorização específica no contexto `orbstack`: `fake-esfinge-issue12-supervisao`,
`fake-esfinge-issue12-supervisao-repro` e `fake-esfinge-issue12-supervisao-auto`.
Exit0 e ausência confirmada; nenhum outro volume excluído. Clientes/logs permanecem temporários
locais; nenhum token, payload, SQL ou dado original foi adicionado à evidência.

## Limites

#8/E1 e T01/E3 pendentes. Sem homologação oficial, ensaio do cliente ou CI remoto.
Docker foi executado pela supervisão. Resposta perdida, cancelamento, reset, expiração e regras
fiscais não implementados nesta fatia. Repetição200 não cria garantia oficial de
idempotência. PR #39 merged, head `a863140`, merge
`f2d7e58aaaf1fd1021c6b05f9471560046963baa`; #12 closed/completed, #8 aberta.
GitHub sem check-runs/statuses no head; resultados verdes são evidência local.
