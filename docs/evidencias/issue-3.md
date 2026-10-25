# Evidência local — issue #3

Data: 24/10/2026. Branch `issue-3-recepcao-creditos`, base documental `8431741`
sobre o merge `4b61304` da #2. Executor `grupo3_executor`, GPT-6.1 Sol/baixo
conforme configuração do papel; exploração por `grupo3_explorer`, GPT-6 Luna/médio.
Dados somente fictícios, SQLite temporário; sem integrador real ou chamadas ao TCE.

## RED/GREEN observado

O primeiro teste HTTP de crédito exigiu 200 e recebeu 422 de cobertura ausente:
`CreditoHttpTest.recebeConsultaEInspecionaCreditoSemPerderDecimalOuBytes`.
Maven terminou com exit code 1, uma falha comportamental, zero erros.
Após implementar a fatia completa recepção/consulta/inspeção/persistência, o mesmo
teste passou: exit code 0, um teste, zero falhas/erros/ignorados, em 3,401 segundos.

Os testes de schema, persistência, migração e falhas foram acrescentados como
regressões da fatia. A execução focalizada de `CreditoHttpTest,JdbcRemessaTest,
RemessaFailureHttpTest` passou com 21 testes, zero falhas/erros/ignorados,
exit code 0, em 4,500 segundos. Não se atribui RED individual a esses casos.

## Comportamentos exercitados

- Cinco testes HTTP de crédito: recepção/consulta RECEBIDO, inspeção administrativa
  com assunto correto e decimal `123456789012345678901234567890.12` exato;
  bytes UTF-8, identificadores com zeros, contagens recebidas sem efeitos.
- Schema documental independente do código: cada obrigatório ausente/null,
  opcionais ausentes/null, tipos sem coerção, limites Unicode, calendário,
  propriedades imobiliárias/transferência extras, listas vazias/nulas/mistas,
  chaves duplicadas e documentos JSON extras.
- Dinheiro: 1/1.0/1.00, zero, negativos e notação exponencial exatos aceitos;
  1.001/1.005/0.001 e fração adicional além da precisão binária rejeitados sem
  arredondar; string/boolean/null rejeitados. Inteiros além de 64 bits preservados;
  mês histórico permitido, mês 00/13/fração/negativo rejeitado.
- Contribuintes e créditos em lotes separados da mesma chave; lotes e registros
  idênticos conservam multiplicidade. Outra UG/cenário não acessa lote/chave;
  inspeção exige chave própria, sem aceitar token compatível.
- SQLite real: `typeof(valor)='text'`, valor decimal exato por lote/índice,
  BLOB original, ordem e sessão conservados na reabertura. 1/1.0/1.00 mantêm
  seus bytes e valor numérico; FK composta rejeita UG cruzada.
- Migração V2 populada: conserva lote, bytes/hash/IDs, eventos e sessão, confere
  checksum e mantém FK habilitada. High-water 42/77 gera lote 43/evento 78.
  Conflito de estrutura após a primeira cópia da V3 reverte a migração inteira,
  preservando versão 2, dados e eventos; removido o conflito, a atualização passa.
- Falhas reais de statement e COMMIT por FK diferida, para ambos os assuntos:
  HTTP 500 sanitizado, valores monetários revertidos, nenhuma confirmação parcial;
  nova recepção aceita com ordem 1. Não são mocks de transação.

## Verificação completa do executor

```sh
rtk proxy env JAVA_HOME=/Users/luka/.sdkman/candidates/java/current \
  MAVEN_USER_HOME=/private/tmp/fake-esfinge-wrapper-check-fixed \
  ./mvnw -B -ntp -Dmaven.repo.local=/private/tmp/fake-esfinge-m2 verify
```

Resultado: **61 testes, zero falhas, zero erros, zero ignorados**, exit code 0,
jar aprovado em 7,694 segundos. São 51 casos da base, cinco HTTP de crédito,
três casos JDBC adicionais e dois parâmetros adicionais de falha HTTP.
Verificação arquitetural por dependências de classes aprovada. V1, V2 e fontes
contratuais não foram alteradas; nova V3 é versionada e conferida por checksum.
Avisos existentes Mockito/ByteBuddy não falharam o build.

## Supervisão independente

`grupo3_reviewer`, GPT-6.1 Sol/médio conforme configuração do papel, aprovou o
diff staged sobre `8431741`, sem achados materiais. Conferidos contrato/schema,
precisão decimal, autorização, transação/rollback, migração, preservação de
IDs/eventos/sessões, fronteiras e regressões. O reviewer não executou Maven/Docker.

O supervisor executou o comando acima com `clean verify`: **61 testes**, zero
falhas/erros/ignorados, exit code 0, build e jar aprovados em **7,508 segundos**.
Nenhum código ou teste foi alterado depois dessa execução e da revisão.

Imagem local construída com `docker build --platform linux/amd64`:
`sha256:2c0ec56f86bc33d7772d4fbde89a070ee12cb431eab7f6b1f84e6f4e1d380f0b`.
Confirmados Linux amd64, usuário `10001:10001`, volume temporário próprio e
`--network none`. A configuração fictícia autorizou UGs `0001` e `0002`.

O ensaio começou com a imagem V2 validada da #2, em volume novo. O cliente Java
independente da #2 passou **163 verificações de preparação**, produzindo lotes,
eventos, bytes e sessão conhecidos. O container foi substituído pela imagem V3,
mantendo o volume. Somente na imagem nova foram executadas:

- **15 verificações de upgrade:** sessão, consulta RECEBIDO, bytes e eventos
  existentes conservados, com outra recepção aceita após a migração.
- **704 verificações de crédito:** HTTP real, representação decimal grande exata,
  1/1.0/1.00/exponencial/zero/negativo, inteiros além de 64 bits, mês histórico,
  todos os obrigatórios ausentes/null, opcionais, Unicode, datas e extras;
  rejeições sem efeitos, isolamento, administração protegida, concorrência,
  múltiplos lotes sem deduplicação e **10.001 registros** preservados integralmente.
- **36 verificações após reinício:** sessão, crédito, bytes e valor exato
  conservados; consulta continua RECEBIDO e nova recepção funciona.
- **12 verificações SQLite somente leitura:** versão/checksums registrados,
  integridade/FKs, coluna e valores realmente TEXT, equivalência exata de cada
  valor ao payload, índices/vínculos e contagens sem deduplicação; lotes e eventos
  da V2 preservados. A leitura não usa agregação monetária em ponto flutuante.

Total: **767 verificações na imagem V3**, todas aprovadas, além das 163 de
preparação na V2. Logs comuns da imagem nova continham somente duas mensagens
`Simulador iniciado`, uma por inicialização. O cliente SQLite emitiu aviso SLF4J
de ausência de provider, sem falhar; isso não integra os logs da aplicação.

Clientes `ContainerCheck2`, `ContainerCheck3` e `SqliteCheck3`, com bibliotecas já
disponíveis, ficaram em `/private/tmp/fake-esfinge-issue3-supervisao`, fora do Git.
O container e o volume exclusivos do ensaio foram removidos após a conferência;
imagem local e clientes temporários foram preservados. Nenhum dado real foi usado.

## Limites

Implementação, revisão e supervisão locais concluídas. Publicação e merge da #3
autorizados e executados em 25/10/2026 no [PR #30](https://github.com/lukadev08/fake-esfinge/pull/30),
head `8a08bc1`, commit de merge `e0b4852` em `main`; issue #3 encerrada.
Não houve implantação ou publicação de imagem.
Não houve integrador real, CI remoto,
aceite humano #8/E1, T01 integral ou homologação oficial. Sem processamento,
finalização, demais quatro assuntos ou validação fiscal/de vínculos entre assuntos.
