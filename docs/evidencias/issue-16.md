# Evidência técnica — issue #16

Data: 27/10/2026. Perfil derivado 2026/V6/leiaute6.06. Dados exclusivamente
fictícios e SQLite temporário; nenhuma chamada ao TCE ou origem do integrador.
Base funcional `df77df1` (PR #42), registro `9f58eae`, branch
`codex/issue-16-expiracao`. Implementação local; código/testes aprovados por
revisor independente sem achados materiais; conferência documental final aprovada.

## Comportamento entregue

- Token padrão de uma hora configurável existente, limite inclusivo401 antes de efeitos e
  novo login. Envio72h inclusivo com agenda durável em segundos/nanos, renovação
  atômica nas operações compatíveis bem-sucedidas; login/admin/rejeições não renovam.
- Avanço/tick usam a mesma agenda; tick manual só expira, sem conduzir lotes ou
  avançar relógio. Varredura limitada ao cenário da instância e deadline de cada
  envio; outros cenários têm seu próprio relógio/offset.
- Marcador de expiração separado, chave fechada409 e nova chave. Remoção de
  dados ativos/inativação, originais e sucessos históricos preservados; pendentes
  ABORTADO/SIM_ENVIO_EXPIRADO. Política derivada, sem conclusão fiscal implícita.
- Expiração por mutação confirma normalmente e só depois retorna409; consulta
  vencida retorna histórico200 sem touch/processamento. Abertura bloqueada 403
  não expira envio anterior. Administração vencida exige chave aberta e recusa 409
  sem efeitos, mesmo antes do marcador. Autenticação/isolamento401/404 precedem.
- V14 aditiva e backfill Java na mesma conexão/transação, sem alterar V1–V13.
  Nanos e prazo além do horizonte9999 são conservados; sem SQL de data impreciso.
  begin/complete/fail e expiração verificam deadline/marcador/geração antes de gravar.

## TDD e validação do executor

Comandos inspecionados: Maven Wrapper 3.9.9, Java 21, dependências públicas/cache
temporários e SQLite local. Comando focal inicial:

```sh
rtk proxy env JAVA_HOME=/Users/luka/.sdkman/candidates/java/current \
  MAVEN_USER_HOME=/private/tmp/fake-esfinge-wrapper-check-fixed \
  ./mvnw -B -ntp -Dmaven.repo.local=/private/tmp/fake-esfinge-m2 \
  -Dtest=ExpiracaoHttpTest test
```

- RED: um teste HTTP, esperado ABORTADO após 72h, recebido RECEBIDO; exit 1, 4,316s.
- GREEN inicial: mesmo teste, zero falhas/erros/ignorados; exit 0, 4,455s.
- Primeira regressão:128 testes,14 falhas somente de expectativas de versão13
  contra 14; sem erro funcional. Fixtures foram adaptadas e comparações de upgrade
  agora também verificam deadline exato antes de remover as colunas adicionadas.
- Focal ampliado inicial:152 testes verdes. Expansão expôs duas lacunas reais:
  repetição em chave já expirada/bloqueada devolvia403, e expiração direta com
  geração divergente era aceita. RED 6 casos; helpers preservaram409 de chave
  expirada e UPDATE ganhou guarda de geração. GREEN 6, exit 0, 3,584s.
- Focal final: **165 testes**, zero falhas/erros/ignorados, exit 0, 21,554s.
- Executor `verify`: **304 testes**, zero falhas/erros/ignorados, exit 0, 34,813s.
  Novos:25 JDBC e 4 HTTP; regressões de autenticação, fronteiras e migração incluídas.
  Logs temporários `/private/tmp/fake-esfinge-issue16-*.log` preservados.

Cobertura significativa: limite antes/no/depois com nanos; seis renovações;
token de uma hora e duração configurada existente; rejeição/login/admin sem renovação;
envio vazio; finalizada/cancelada não expiram; offset/agenda/reinício; isolamento
de cenário/UG/deadlines; statement/COMMIT rollback; backfill V13 preenchida com
atividade inválida revertendo tudo e timestamp 9999 com nanos; corrida de cálculo
pausado fora da transação; begin/complete/fail recusam gravação vencida antes do
marcador; consulta histórica e recuperação por nova chave com dados exatos.

## Supervisão

`clean verify` da supervisão: **304 testes**, zero falhas/erros/ignorados,
exit 0, 37,382s. Log `/private/tmp/fake-esfinge-issue16-supervisor-verify.log`.
Jar final SHA256:

`28d43a86093a158d4d8f45bf75b2cf8a46c6f478565b64acc1694f68e597eb3f`

Imagem final Linux amd64 `fake-esfinge:issue-16-local`:

`sha256:2d0199d53abba7656b6a878b9d71862d7fe26807b5b3a41bb8b7eabee246675b`

O hash do jar dentro da imagem corresponde ao jar final acima. Smoke Docker:
**56 verificações**, 25 prepare + 31 resume, ambos exit 0,  com reinício e avanço
no limite de 72h. SQLite somente leitura: integrity_check ok e foreign_key_check
sem violações, exit 0. Build sem rede/pullfalse; runtime bridge com publicação
loopback. Container parado/removido, ausência confirmada; nenhum volume Docker
criado. Bindmount e dados fictícios em
`/private/tmp/fake-esfinge-issue16-supervisor.3qYLsi` preservados.

Somente sondagens GET de prontidão tiveram conexão vazia durante startup; porta
efêmera foi reconferida após restart. Nenhum smoke falhou ou código mudou por
esses incidentes. Revisor independente aprovou código/testes sem achados,
conferiu 32 relatórios/304 testes e diff check. Conferência documental final também
aprovada sem achados materiais. Publicação é a próxima etapa da supervisão.

## Smoke local com reinício

Cliente [reproduzível](../../.scratch/issue-16-smoke.py), stdlib Python, loopback,
base exclusiva, relógio fixed e UGs fictícias0001/0002. Não espera horas reais.
Jar do executor copiado para tmp antes do clean da supervisão, SHA256:

`2ff0e33349985562582a29dd0aa584d0adf5537a6370cc6649b2650a8a38e74a`

- `prepare`:25 verificações, exit 0.
- Reinício da mesma base e `resume`:31 verificações, exit 0. Total **56**.
- Prova token de uma hora/novo login, consulta renovadora e deadline 1ms antes do limite,
  relógio/agenda persistidos; expiração inclusiva após reinício, trabalho pendente
  sem efeitos, sucesso histórico inativo, outra UG com deadline futuro intacta,
  consulta histórica sem novos eventos, nova chave/reenvio/decimal exato/finalização.
- Base, jar copiado e registros fictícios preservados em
  `/private/tmp/fake-esfinge-issue16-host.ArNeaR`. Serviços 18196 encerrados por
  Ctrl+C nas duas sessões (exit 1 do wrapper); lsof confirmou ausência de listener.
  Nenhum Maven concorrente ou mudança de produção/testes após congelamento.

## Limites

Parcela técnica de T02/T11/T19, sem integrador real. #8/E1 e T01/E3 continuam
pendentes. Reset #17, inspeção #18, regras fiscais/CONs, CI remoto e homologação não
são comprovados. A política de duração/renovação/terminal é derivada. Não há API
compatível nova nem TTL de envio configurável adicional.
