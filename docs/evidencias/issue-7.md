# Evidência local — issue #7

Data: 25/10/2026. Branch `issue-7-recepcao-prazo`, base documental `27e7980`
sobre o merge `ad27bd8` da #6. Executor `grupo7_executor`, identificado como
GPT-6.1 Sol/baixo conforme o papel. Dados fictícios e SQLite temporário;
sem chamadas ao TCE ou integrador real.

## Contrato e comportamento

Schema derivado `$defs.prazo` do perfil 2026/V6/6.06, aprovado a partir do exemplo,
**sem DTO extraído**. Quatro campos obrigatórios, sem extras: ano inteiro
`anoLancamentoCreditoTributarioOuDividaAtiva`, código string até16 codepoints
`codigoLancamentoCreditoTributarioOuNumeroInscricaoDividaAtiva` e duas datas reais
`dataInicioPrazoPrescricional`/`dataAtualizacaoPrazoPrescricional` em `YYYY-MM-DD`.
Sem coerção, teto fiscal para o ano, cronologia ou relação com crédito/dívida ativa.

- Cinco testes HTTP de prazo exercitam recepção, consulta RECEBIDO e inspeção
  protegida: bytes/referências/zeros, ano histórico, data bissexta e atualização
  anterior ao início sem aprovação fiscal, contagens recebidas e efetivos/processados zero.
- Todos os obrigatórios ausentes/null, tipos incompatíveis, fração no ano e
  extras são rejeitados sem efeitos. Datas em ambos os campos validam formato
  e calendário, incluindo 1900/2000, datas impossíveis, timestamp e dígitos não ASCII.
- Código Unicode respeita16 codepoints; código vazio não recebe mínimo inventado.
  Inteiro grande, negativo, zero e grafias inteiras `1.0`/`1e2` conservam valor,
  sem coerção de string. Listas vazias/nulas/mistas, JSON duplicado/adicional
  são rejeitados. Duplicatas permanecem distintas e os seis assuntos coexistem
  em lotes separados; outra UG ou token inválido não acessa os lotes.
- Núcleo aceita lista monetária vazia para prazo e cadastro, rejeitando mapas
  monetários e até mapas vazios por registro. Valores de saldo continuam imutáveis.
- SQLite real confirma prazo sem linhas monetárias, multiplicidade, bytes/hash,
  eventos e sessão após reabertura, outra UG/cenário e FKs protegidos.
- V7 preserva V1–V6/checksums/fontes. Fixture V6 preenchida com os cinco assuntos
  conserva todos os metadados/bytes/IDs, eventos, sessões, campos e valores TEXT.
  High-water42/77 produz lote43/evento78 após migrar; prazo entra somente na V7
  e não altera as sete linhas monetárias antigas, incluindo quatro campos de saldo.
- Conflito intermediário na V7 reverte estrutura, versão6, migrações, dinheiro
  nomeado, bytes/eventos/sessões e FKs; removido o conflito, migra e recebe prazo.
- Falhas reais de statement e COMMIT por FK diferida agora cobrem os seis assuntos:
  HTTP500 sanitizado, nenhuma confirmação parcial, valores antigos conservados
  e nova recepção com ordem1. Sem mock de transação.

## RED/GREEN e regressões observadas

Primeiro teste `PrazoHttpTest.recebeConsultaEInspecionaPrazoSemValidacaoFiscal`:
200 esperado/422 recebido, **1 teste, 1 falha, 0 erros/ignorados**, exit1,
**3,995s**. Após implementar a fatia, o mesmo teste passou: **1 teste**, zero
falhas/erros/ignorados, exit0, **3,524s**. Os demais casos foram acrescentados como
regressões; não se atribui RED individual a cada caso.

Primeira focalizada teve **43 testes, 1 falha**, exit1/6,424s: um novo
`assertEquals(Lote,Lote)` comparava a identidade de `byte[]` do record, apesar de
metadados e conteúdo iguais. Corrigida somente a asserção para metadados e
`assertArrayEquals`; nenhum código da aplicação mudou por essa falha.
Reexecução de `PrazoHttpTest,ReceivedContentTest,JdbcRemessaTest,RemessaFailureHttpTest`:
**43 testes**, zero falhas/erros/ignorados, exit0, **6,325s**.

## Verificação completa

Comando inspecionado, sem acesso a produção; dependências públicas e SQLite local:

```sh
rtk proxy env JAVA_HOME=/Users/luka/.sdkman/candidates/java/current \
  MAVEN_USER_HOME=/private/tmp/fake-esfinge-wrapper-check-fixed \
  ./mvnw -B -ntp -Dmaven.repo.local=/private/tmp/fake-esfinge-m2 verify
```

Resultado: **105 testes, zero falhas, zero erros, zero ignorados**, exit code0,
build/jar aprovados em **9,613s**. São os95 casos da base, cinco HTTP de prazo,
três JDBC adicionais e dois parâmetros de falha HTTP do sexto assunto.
Verificação arquitetural por dependências de classes aprovada. V1–V6, fontes
contratuais e dependências intactas; `git diff --check` sem erros. Avisos existentes
Mockito/ByteBuddy não falharam o build. O verify foi iniciado uma vez e concluído,
sem handle pendente.

## Revisão e supervisão independentes

`grupo7_explorer`, identificado como GPT-6 Luna/médio, explorou somente leitura.
`grupo7_reviewer`, identificado como GPT-6.1 Sol/médio, aprovou o diff da #7 sobre
`27e7980`, incluindo os três arquivos novos, sem achados materiais. Conferiu
schema/calendário/tipos, isolamento, conteúdo não monetário, preservação de saldo,
V7 transacional, checksums/FKs/sequências, rollback e limites documentados.
Não executou testes ou Docker; nenhuma correção obrigatória foi identificada.

Supervisor executou `clean verify` com o mesmo ambiente/comando acima:
**105 testes, zero falhas/erros/ignorados**, exit0, **10,957s**. Código e testes
permaneceram congelados após revisão e suíte; somente registros foram atualizados.
Jar `target/fake-esfinge-0.1.0.jar`, SHA-256
`0d54ec70afea3c1f7f2a87db0e172683795000913e4c4668576643b17c5d5957`.

### Docker Linux amd64

Preparação na imagem V6 já validada `fake-esfinge:issue-6-local`
(`46d3d07e84c0`): **714 verificações de baixa + 636 de saldo = 1.350**, exit0,
criando cinco assuntos e mais de 50.000 valores monetários fictícios. Captura
somente leitura registrou contagens e hashes ordenados de dez tabelas: cenário,
identidades/vínculos/sessões, remessas/lotes/eventos/valores, sequências e os seis
registros anteriores de migração. Dados/tokens não foram impressos. Container V6
parado/removido, preservando o volume para upgrade.

Imagem nova `fake-esfinge:issue-7-local`, ID
`sha256:bcbc05ff4d10596e4b651e16adef205e6e4c7696ed0108d61f98a90f48a13c99`,
Linux amd64, Java Temurin **21.0.10+7**, UID/GID `10001:10001`. Executada sem rede
(`--network none`) ou portas publicadas, com o mesmo volume em `/data` e clientes
somente leitura em `/checks`. O hash de `/opt/simulator/app.jar` coincidiu com o
jar aprovado na suíte limpa; nenhum acesso ao TCE ou dados originais do piloto.

| Cliente na imagem V7 | Verificações | Resultado observado |
| --- | ---: | --- |
| `MigrationSnapshot7 after` | 13 | Versão7, integridade/FKs e hashes/contagens das dez tabelas iguais aos anteriores, antes de novas operações |
| `ContainerCheck5 restart` | 34 | Sessão/bytes/dinheiro V6 preservados e nova recepção de baixa |
| `ContainerCheck6 restart` | 46 | Quatro valores nomeados de saldo, bytes/contagens e sessão preservados |
| `ContainerCheck7 first` | 529 | Quatro campos, calendário, obrigatórios/tipos/extras, Unicode, histórico sem cronologia fiscal, isolamento, seis assuntos separados, duplicatas, concorrência e 10.001 prazos |
| `ContainerCheck7 restart` | 42 | Sessão, bytes/referências/datas/contagens e nova recepção após reinício, sem processamento pela consulta |
| `SqliteCheck7` | 14 | Versão/migrações/integridade/FKs, mais de 50.000 valores TEXT exatos por campo/índice/vínculo e ausência de dinheiro para prazo/cadastro |

Total: **678 verificações na imagem V7**, todas exit0, contabilizadas separadamente
das 1.350 de preparação V6. Mais de 10.001 registros de prazo persistem com os
quatro campos, sem linhas monetárias. Nenhuma falha remanescente no ensaio V7.
Reinício concluiu normalmente; logs comuns contiveram somente duas linhas
`Simulador iniciado`. Avisos SLF4J dos clientes SQL não falharam os ensaios.

Container temporário parado e removido. Volume fictício
`fake-esfinge-issue7-supervisao` preservado até autorização específica de exclusão,
sem tentar apagá-lo. Imagem e clientes em `/private/tmp/fake-esfinge-issue7-supervisao`
preservados localmente; nenhuma publicação de imagem.

## Limites

Recepção estrutural não valida prescrição, cronologia ou vínculo fiscal ao
crédito/dívida ativa. Sem DTO extraído, captura independente do cliente/TCE,
integrador real, CI remoto ou aceite humano #8/E1/T01. Se prazo faltar no piloto,
cenário complementar continua necessário. Processamento, finalização e demais
capacidades E2 permanecem não implementadas. Sem publicação/merge desta fatia.
