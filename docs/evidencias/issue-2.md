# Evidência local — issue #2

Data: 24/10/2026. Branch `issue-2-recepcao-contribuintes`, base `a36c7bf` e
registro documental inicial `ce1217c`. Executor `grupo2_executor`, GPT-6.1 Sol,
raciocínio baixo; exploração por `grupo2_explorer`, GPT-6 Luna/médio.
Revisão por `grupo2_reviewer`, GPT-6.1 Sol/médio conforme configuração do papel,
aprovada sem achados materiais no diff sobre `ce1217c`.
Todos os dados são fictícios. Não houve chamada ao TCE ou uso do integrador real.

## Fatias e RED/GREEN observados

| Fatia | Falha observada | Resultado posterior |
|---|---|---|
| Abrir envio | HTTP esperado 200, recebido 422 da operação indisponível | 200 com chave opaca, SQLite e evento |
| Política de cancelamento | Valor vazio recebeu 400, esperado 422 | Ausência/SIM/vazio/outro recebem 422 |
| Receber contribuinte | HTTP esperado 200, recebido 422 | RECEBIDO com IDs/timestamp contratuais |
| Consultar/inspecionar | Consulta esperava 200, recebeu422 | Consulta RECEBIDO e inspeção protegida com bytes exatos |
| Evento sem lote | Esperado `null`, obtido 0 em evento de abertura | `wasNull` capturado antes da próxima leitura JDBC |
| Content-Type JSON válido | Charset UTF-8 entre aspas recebeu 400 | MediaType padrão aceita sintaxe válida |
| Documento vazio | Esperado400, obtido 422 | Ausência de documento tratada como requisição malformada |

Comandos focalizados usaram `-Dtest=RemessaHttpTest#<metodo> test`; as falhas foram
comportamentais. O teste de reabertura inicialmente comparou arrays pela igualdade
padrão do record; a asserção foi corrigida para bytes/metadados, sem alterar produção.

## Verificação final do executor

Comando executado:

```sh
rtk proxy env JAVA_HOME=/Users/luka/.sdkman/candidates/java/current \
  MAVEN_USER_HOME=/private/tmp/fake-esfinge-wrapper-check-fixed \
  ./mvnw -B -ntp -Dmaven.repo.local=/private/tmp/fake-esfinge-m2 verify
```

Resultado: **51 testes, zero falhas, zero erros, zero ignorados**, build e jar
aprovados em 6,725 segundos. Distribuição: 30 testes da base e 21 da #2:

- `JdbcRemessaTest`: 9; reabertura/bytes/hash/ordem, outro cenário com token válido,
  outra UG, FK de lote/evento, rollback de statement, falha real de COMMIT por FK
  diferida, contenção real e migração V1→V2 com sessão preservada/checksum prévio.
- `RemessaHttpTest`: 9; abertura/recepção/consulta/admin, schema documental com
  obrigatórios, nulos/ausentes, tipos, extras, calendário, limites Unicode,
  documentos duplicados/extra, UTF-8 inválido, assuntos sem cobertura, múltiplos
  lotes idênticos, 10.001 registros sem limite artificial e negações de acesso.
- `RemessaExpiryHttpTest`: 1; relógio substituível demonstra 401 antes de abertura,
  recepção e consulta, renovação por login e ausência de eventos/ordem extras.
- `RemessaFailureHttpTest`: 2; falha SQLite em statement ou COMMIT produz 500
  sanitizado, não confirma a recepção e permite nova primeira recepção correta.

A concorrência usa quatro aberturas simultâneas: uma vence, três retornam conflito;
quatro recepções simultâneas conservam todos os lotes com ordens 1–4. Não há mocks
para transação/commit: `DataSourceTransactionManager` reverte inclusive falha de
commit real. As falhas são montadas em bases temporárias, fora da API compatível.
Os testes HTTP usam MockMvc com SQLite real; socket/container pertence à supervisão.

O processo Maven iniciado antes da interrupção por quota terminou com exit code 0.
Na retomada, o mesmo handle foi consultado e sua conclusão preservada; a suíte não
foi reiniciada sem mudança. Documentação foi concluída depois, sem alterar código.
Avisos Mockito/ByteBuddy existentes não falharam a suíte; logs da aplicação seguem
sanitizados/desabilitados. V1, fontes e schemas contratuais não foram alterados.

## Supervisão independente

O supervisor executou o mesmo comando com `clean verify`: **51 testes**, zero
falhas/erros/ignorados e jar aprovado em 7,222 segundos. Nenhuma alteração de
código ou teste ocorreu depois dessa execução e da revisão independente.

Imagem construída com `docker build --platform linux/amd64`:
`sha256:f5a50b623a9724fce4c119f3eb1ecbdd48846912caf0187c420351269f4ad9a0`.
Confirmados `linux/amd64`, usuário `10001:10001`, volume exclusivo do ensaio e
`--network none`. A configuração fictícia autorizou UGs `0001` e `0002`.

Um cliente Java temporário independente executou **163 verificações** no fluxo
inicial e **15 após reinício**, todas aprovadas. Exercitou quatro aberturas
concorrentes (uma aceita/três conflitos), outra competência e outra UG, payloads
idênticos sem deduplicação, consulta RECEBIDO, nomes de IDs contratuais, bytes
UTF-8 exatos, zeros/nulos e inspeção administrativa protegida. Rejeições de
JSON/schema, limites Unicode 512/513 e lote com **10.001 registros** também foram
verificados; a inspeção desse lote conservou seus bytes e sua contagem integral.
Após reinício, a sessão, o lote inspecionado e seus bytes permaneceram disponíveis,
e outra recepção foi aceita. Logs comuns continham somente `Simulador iniciado`.

O cliente de checagem e os jars de apoio ficaram em diretório temporário, fora da
aplicação e do versionamento. As evidências são de dados fictícios e execução local.

## Limites de aceite

Revisão e supervisão locais da #2 concluídas. Publicação autorizada e executada no
[PR #29](https://github.com/lukadev08/fake-esfinge/pull/29), com implementação
revisada no commit `02e036f`; merge ainda não autorizado ou executado.
Não há outros assuntos, processamento, finalização, descarte, reset, expiração
administrativa de envio, painel ou CI remoto nesta entrega. As evidências contribuem
parcialmente para T01/T02/T10/T11/T13/T14/T18; não comprovam integrador real,
aceite humano #8/E1, T01 integral ou homologação oficial.
