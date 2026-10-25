# Evidência local — issue #4

Data: 25/10/2026. Branch `issue-4-recepcao-arrecadacao`, base documental
`54525d5` sobre o merge `e0b4852` da #3. Executor `grupo4_executor`, GPT-6.1
Sol/baixo conforme configuração do papel; exploração por `grupo4_explorer`,
GPT-6 Luna/médio. Dados fictícios e bases SQLite temporárias; sem chamadas ao TCE.

## RED/GREEN observado

O primeiro teste HTTP exigiu 200 para arrecadação e recebeu 422 de cobertura
ausente: `ArrecadacaoHttpTest.recebeConsultaEInspecionaArrecadacaoComDecimalEBytesExatos`.
Maven terminou com exit code 1, um teste, uma falha comportamental e zero erros,
em 3,372 segundos. Após implementar a fatia completa, o mesmo teste passou:
exit code 0, um teste, zero falhas/erros/ignorados, em 3,351 segundos.

Schema, persistência, migração e falhas foram acrescentados como regressões da
fatia; não se atribui RED individual a cada caso. A primeira execução focalizada
detectou que a atualização de números de versão em um teste também alterara
indevidamente a expectativa de três conflitos concorrentes para quatro. A
expectativa foi restaurada, sem alterar produção. A execução focalizada final
passou com **26 testes**, zero falhas/erros/ignorados, exit code 0, em 4,984 segundos.

## Comportamentos exercitados

- Cinco testes HTTP de arrecadação: recepção e consulta RECEBIDO, assunto
  `DIARIO_ARRECADACAO`, inspeção administrativa protegida, decimal grande exato,
  bytes UTF-8, identificadores com zeros e contagens sem processamento.
- Schema documental: oito obrigatórios ausentes/null, dois opcionais
  ausentes/null, tipos sem coerção, limites Unicode, calendário, extras,
  listas vazias/nulas/mistas, chaves duplicadas e documentos JSON adicionais.
- `numeroParcelas` é inteiro opcional/nulo; `numeroParcela` recebe 422 tanto
  sozinho quanto junto do plural. O nome adotado é aproximação relatada pelo
  cliente, distinta da extração original preservada.
- Valores `1`, `1.0`, `1.00`, exponenciais, zero e negativos aceitos sem mínimo
  fiscal inventado; dinheiro grande exato e frações inválidas, inclusive além
  da precisão binária, rejeitadas sem arredondamento. Inteiros matemáticos,
  inclusive além de 64 bits, preservados sem coerção de string/booleano.
- Lotes separados de contribuinte, crédito e arrecadação sob a mesma chave;
  lotes/registros idênticos não deduplicados. Consultar repetidamente não
  processa; outra UG/cenário não acessa chave/lote. Administração exige chave própria.
- SQLite real: `typeof(valor)='text'`, dinheiro exato por lote/índice,
  BLOB original, ordem, sessão e assunto conservados na reabertura. Grafias
  1/1.0/1.00 mantêm bytes originais e equivalência numérica. FK composta impede
  alteração cruzada de UG nos valores e nos lotes.
- V4 preserva V1–V3 e checksums, amplia o assunto dos lotes e reúne valores
  em `valores_recebidos`, mantendo créditos existentes. Upgrade V3 populada
  conserva contribuinte, crédito, valor TEXT, bytes/hash/IDs, eventos e sessão;
  high-water 42/77 produz lote 43/evento 78, sem reutilização de IDs.
- Conflito estrutural durante V4 reverte cópias e estrutura, mantendo versão 3,
  dinheiro na tabela original, bytes, eventos e FKs. Removido o conflito, migra.
- Falhas reais de statement e COMMIT por FK diferida para os três assuntos:
  HTTP 500 sanitizado e rollback incluindo dinheiro; nova recepção aceita com
  ordem 1, sem confirmação parcial. Não são mocks de transação.

## Verificação completa do executor

```sh
rtk proxy env JAVA_HOME=/Users/luka/.sdkman/candidates/java/current \
  MAVEN_USER_HOME=/private/tmp/fake-esfinge-wrapper-check-fixed \
  ./mvnw -B -ntp -Dmaven.repo.local=/private/tmp/fake-esfinge-m2 verify
```

Resultado: **71 testes, zero falhas, zero erros, zero ignorados**, exit code 0,
build e jar aprovados em **7,313 segundos**. São 61 casos da base, cinco HTTP
de arrecadação, três JDBC adicionais e dois parâmetros adicionais de falha HTTP.
Verificação arquitetural por dependências de classes aprovada. Fontes/schemas
contratuais e migrações V1–V3 não foram alterados. Avisos existentes de
Mockito/ByteBuddy não falharam o build.

## Supervisão independente

`grupo4_reviewer`, GPT-6.1 Sol/médio conforme configuração do papel, aprovou o
diff staged sobre `54525d5`, sem achados materiais. Conferidos schema/plural,
precisão decimal, autorização, atomicidade/rollback, migração V4, preservação dos
dados V3, concorrência, fronteiras e regressões. O reviewer não executou Maven/Docker.

O supervisor executou o comando acima com `clean verify`: **71 testes**, zero
falhas/erros/ignorados, exit code 0, build/jar aprovados em **8,349 segundos**.
Nenhum código ou teste foi alterado depois dessa execução e da revisão.

Imagem local construída com `docker build --platform linux/amd64`:
`sha256:31c54ea6f2dffe9022dec37dad0e35e7d9e8dacb2fabb85c2c334db03ce7ef4a`.
Confirmados Linux amd64, usuário `10001:10001`, volume próprio e `--network none`.
UGs fictícias `0001` e `0002`; nenhuma imagem foi publicada.

O ensaio começou em volume novo com a imagem V3 validada da #3. Seu cliente
independente passou **704 verificações de preparação**, produzindo contribuintes,
créditos, valores TEXT, sessão e eventos, inclusive um lote de 10.001 créditos.
O container foi substituído pela imagem V4, conservando o volume. Na imagem nova:

- **36 verificações de upgrade/regressão de crédito:** sessão, consulta RECEBIDO,
  bytes e dinheiro grande exato preservados; nova recepção de crédito aceita.
- **771 verificações de arrecadação:** HTTP real com oito obrigatórios,
  opcionais, `numeroParcelas` e rejeição do singular, tipos sem coerção, inteiros
  grandes, Unicode, calendário, dinheiro exato, listas/extras, isolamento e
  administração protegida. Rejeições não alteram eventos; lotes idênticos e
  concorrentes mantêm multiplicidade/ordem. Lote com **10.001 arrecadações**
  conserva bytes, registros, valores e contagens integrais.
- **34 verificações após reinício:** sessão, arrecadação, bytes e valor exato
  conservados; consulta continua RECEBIDO e outra recepção funciona.
- **12 verificações SQLite somente leitura:** versão 4, registro de migrações,
  integridade/FKs, tabela comum TEXT e ausência da antiga `valores_creditos`.
  Mais de **20.000 valores**, dos dois assuntos, são texto e equivalem exatamente
  a cada número do payload; índices/vínculos e contagens preservados, sem
  deduplicação. Contribuintes continuam sem registros monetários.

Total: **853 verificações na imagem V4**, todas aprovadas, além das 704 de
preparação em V3. Logs comuns da imagem nova continham somente duas mensagens
`Simulador iniciado`, uma por inicialização. O cliente SQLite emitiu aviso SLF4J
de ausência de provider, sem falhar; não integra os logs da aplicação.

Clientes Java `ContainerCheck2`, `ContainerCheck3`, `ContainerCheck4` e
`SqliteCheck4`, com bibliotecas já disponíveis, ficaram em
`/private/tmp/fake-esfinge-issue4-supervisao`, fora do Git. Container e volume
exclusivos do ensaio foram removidos; imagem e clientes temporários preservados.
Todos os dados são fictícios; não houve chamadas ao TCE.

## Limites

Implementação, revisão e supervisão locais concluídas. Publicação/merge da #4
autorizados e executados em 25/10/2026 no [PR #31](https://github.com/lukadev08/fake-esfinge/pull/31),
head `4e3f8df`, commit de merge `cc2d12d` em `main`; issue #4 encerrada.
Não houve implantação ou publicação de imagem.
Não houve integrador real, CI remoto,
aceite humano #8/E1, T01 integral ou homologação oficial. Sem processamento,
finalização, demais três assuntos, catálogos, cronologia, cálculos ou validação
fiscal da relação entre arrecadação e crédito.
