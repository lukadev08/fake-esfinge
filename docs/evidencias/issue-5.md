# Evidência local — issue #5

Data: 25/10/2026. Branch `issue-5-recepcao-baixas`, base documental `299b457`
sobre o merge `cc2d12d` da #4. Executor `grupo5_executor`, GPT-6.1 Sol/baixo
conforme configuração do papel; exploração por `grupo5_explorer`, GPT-6 Luna/médio.
Dados fictícios e bases SQLite temporárias; sem chamadas ao TCE ou integrador real.

## RED/GREEN observado

O primeiro teste HTTP exigiu 200 para baixa e recebeu 422 de cobertura ausente:
`BaixaHttpTest.recebeConsultaEInspecionaBaixaComDecimalEBytesExatos`.
Maven terminou com exit code 1, um teste, uma falha comportamental e zero erros,
em **3,470 segundos**. Após implementar a fatia completa, o mesmo teste passou:
exit code 0, um teste, zero falhas/erros/ignorados, em **4,414 segundos**.

Schema, persistência, migração e falhas foram acrescentados como regressões da
fatia; não se atribui RED individual a cada caso. A execução focalizada de
`BaixaHttpTest,JdbcRemessaTest,RemessaFailureHttpTest` passou com **32 testes**,
zero falhas/erros/ignorados, exit code 0, em **5,663 segundos**.

## Comportamentos exercitados

- Seis testes HTTP de baixa: recepção/consulta RECEBIDO, inspeção administrativa
  com assunto `BAIXA_CREDITO_TRIBUTARIO`, decimal grande exato, bytes UTF-8,
  identificadores com zeros e contagens recebidas sem processamento/efetivação.
- Schema documental: nove obrigatórios ausentes/null, dois opcionais
  ausentes/null, tipos sem coerção, limites Unicode, calendário, extras,
  listas vazias/nulas/mistas, chaves duplicadas e documentos JSON adicionais.
- Dinheiro `1`, `1.0`, `1.00`, exponenciais, zero e negativos aceitos sem mínimo
  fiscal inventado; frações inválidas, inclusive além da precisão binária,
  rejeitadas sem arredondamento. Inteiros matemáticos e além de 64 bits aceitos
  sem teto de Short/Long ou coerção de strings/booleanos.
- Referências de crédito e dívida ativa conservadas. Uma baixa com data anterior
  ao crédito recebido sob a mesma chave é aceita estruturalmente; referências
  inexistentes também. Esses testes demonstram regras fiscais adiadas, não
  validade de negócio ou aprovação tributária.
- Lotes separados dos quatro assuntos sob a mesma chave; lotes/registros
  idênticos não deduplicados. Outra UG/cenário não acessa chave/lote; inspeção
  exige chave administrativa própria, sem aceitar token compatível.
- SQLite real: dinheiro em `valores_recebidos.valor TEXT`, um valor por lote/índice,
  bytes originais, ordem, sessão e assunto conservados após reabertura.
  Grafias 1/1.0/1.00 mantêm bytes e equivalência numérica; FK composta impede UG cruzada.
- Upgrade de V4 populada com contribuinte, crédito, arrecadação, valores TEXT,
  sessão e eventos conserva bytes/hash/IDs e FKs. Checksum anterior adulterado
  impede migração; high-water 42/77 produz lote 43/evento 78, sem reutilizar IDs.
- Conflito estrutural durante V5 reverte cópias e estrutura, mantendo versão 4,
  valores de crédito/arrecadação, bytes, eventos e FKs. Removido o conflito, migra.
- Falhas reais de statement e COMMIT por FK diferida para os quatro assuntos:
  HTTP 500 sanitizado, dinheiro revertido e nova recepção aceita com ordem 1,
  sem confirmação parcial. Não são mocks de transação.

## Verificação completa do executor

```sh
rtk proxy env JAVA_HOME=/Users/luka/.sdkman/candidates/java/current \
  MAVEN_USER_HOME=/private/tmp/fake-esfinge-wrapper-check-fixed \
  ./mvnw -B -ntp -Dmaven.repo.local=/private/tmp/fake-esfinge-m2 verify
```

Resultado: **82 testes, zero falhas, zero erros, zero ignorados**, exit code 0,
build/jar aprovados em **8,229 segundos**. São 71 casos da base, seis HTTP de baixa,
três JDBC adicionais e dois parâmetros adicionais de falha HTTP. Verificação
arquitetural por dependências de classes aprovada. Fontes/schemas contratuais e
migrações V1–V4 intactos. Núcleo e JDBC existentes foram reutilizados sem alteração.
Avisos existentes Mockito/ByteBuddy não falharam o build.

## Supervisão independente

`grupo5_reviewer`, GPT-6.1 Sol/médio conforme configuração do papel, aprovou o
diff staged sobre `299b457`, sem achados materiais. Conferidos schema, precisão
decimal, autorização, atomicidade/rollback, migração V5, preservação de V4,
fronteiras e regressões. O reviewer não executou Maven/Docker.

O supervisor executou o comando acima com `clean verify`: **82 testes**, zero
falhas/erros/ignorados, exit code 0, build/jar aprovados em **9,079 segundos**.
Nenhum código ou teste foi alterado depois dessa execução e da revisão.

Imagem local construída com `docker build --platform linux/amd64`:
`sha256:a3b298c9903a5f62ebb835f1724d65f9cef0c6a42af4f026b7c8dde362223a43`.
Confirmados Linux amd64, usuário `10001:10001`, volume próprio e `--network none`.
UGs fictícias `0001` e `0002`; nenhuma imagem publicada.

O ensaio começou em volume novo com a imagem V4 validada da #4. Seu cliente
independente passou **771 verificações de preparação**, produzindo contribuintes,
créditos, arrecadações, valores TEXT, sessão e eventos, inclusive 10.001 registros
em um lote de arrecadação. O container foi substituído pela imagem V5,
conservando o volume. Na imagem nova:

- **34 verificações de upgrade/regressão:** sessão, consulta RECEBIDO, bytes e
  dinheiro de arrecadação preservados; nova recepção aceita após a migração.
- **714 verificações de baixa:** HTTP real com obrigatórios/opcionais, tipos,
  Unicode, calendário, dinheiro exato, referências/zeros, extras/listas e rejeições
  sem efeitos. Baixa anterior ao crédito e referência inexistente conservadas
  como dados recebidos, sem aprovação fiscal. Quatro assuntos em lotes separados,
  duplicatas, concorrência, administração protegida e **10.001 baixas** sem perda.
- **34 verificações após reinício:** sessão, baixa, bytes e valor exato
  conservados; consulta continua RECEBIDO e outra recepção funciona.
- **12 verificações SQLite somente leitura:** versão/migrações, integridade/FKs,
  tabela TEXT e ausência de tabelas temporárias da V5. Mais de **20.000 valores**
  dos três assuntos monetários são texto e equivalem exatamente a cada número do
  payload; índices/vínculos/contagens preservados, sem deduplicação. Contribuintes
  continuam sem valores monetários.

Total: **794 verificações na imagem V5**, todas aprovadas, além das 771 de
preparação em V4. Logs comuns da imagem nova continham somente duas mensagens
`Simulador iniciado`, uma por inicialização. Aviso SLF4J de ausência de provider
do cliente SQLite não falhou o ensaio nem integra os logs da aplicação.

Clientes Java `ContainerCheck2`–`ContainerCheck5` e `SqliteCheck5`, com bibliotecas
já disponíveis, ficaram em `/private/tmp/fake-esfinge-issue5-supervisao`, fora do
Git. Container e volume exclusivos do ensaio removidos após a conferência;
imagem e clientes temporários preservados. Dados fictícios, sem chamadas ao TCE.

## Limites

Implementação, revisão e supervisão locais concluídas. Publicação/merge da #5
ainda não executados. Não houve implantação ou publicação de imagem.
Não houve integrador real, CI remoto,
aceite humano #8/E1, T01 integral ou homologação oficial. Sem processamento,
finalização, demais dois assuntos, catálogos, cronologia, cálculos ou validação
fiscal de vínculos entre assuntos.
