# Preparação local — issue #6

Referência: [issue #6](https://github.com/lukadev08/fake-esfinge/issues/6).
Base funcional `25dcbd6`, após integração da #5 no PR #32.
Branch `issue-6-recepcao-saldo`. Exploração concluída em leitura somente por
`grupo6_explorer`, GPT-6 Luna/médio conforme o papel.
Publicação/merge da #6 ainda não autorizados.

## Contrato verificado

- Perfil derivado 2026/V6/6.06; schema `$defs.saldo`, lista direta
  `saldosIniciaisCreditoTributario`, assunto `SALDO_INICIAL_CREDITO_TRIBUTARIO`.
  Sete propriedades, seis obrigatórias, uma opcional/nula, sem extras/misturas.
- Ano do crédito é inteiro, código é string de até16 caracteres com zeros.
  `tipoSaldoInicial` é inteiro opcional/nulo; sem catálogo/default fiscal inferido.
- Quatro números obrigatórios, cada um múltiplo exato de 0,01: `saldoPrincipal`,
  `saldoMulta`, `saldoJuros`, `saldoAtualizacaoMonetaria`. Não somar, arredondar,
  usar double/float/REAL ou substituir ausente por zero.
- Demonstrar abertura202601, recepção/consulta/admin persistida e reinício com
  referências históricas. O contrato não fixa rejeição de outros meses; não criar
  janeiro-only. Condições, defaults, tipos e regras fiscais continuam adiados.
- Cenário fictício de janeiro contribui parcialmente para T01/T02/T16/T18;
  não substitui integrador real, cenário complementar nem aceite E1/#8.

## Direção da supervisão

1. Evoluir `ReceivedContent.monetaryValues` para `List<Map<String,BigDecimal>>`,
   um mapa por registro monetário, com cópias defensivas e imutáveis. Validar
   quantidade de registros, campos exatos esperados por assunto e precisão.
   Cadastro mantém lista vazia; os três assuntos atuais têm um campo, saldo tem quatro.
2. Parser mapeia valores antigos com seus nomes e valida saldo; acrescentar enum
   e seleção da lista administrativa. Núcleo Java puro; sem JSON ou framework nele.
3. JDBC persiste campo/valor por lote/índice na mesma transação de bytes/hash,
   metadados, atividade e evento. `valores_recebidos` ganha `campo TEXT` e chave
   `(lote_id,indice,campo)`; valor continua TEXT, com vínculo composto existente.
4. V6 aditiva, mantendo V1–V5/checksums/fontes intactos: rebuild seguro de lotes,
   eventos e valores com FKs habilitadas. Campos dos valores V5 são atribuídos
   pelo assunto: crédito→`valorLancamentoCreditoTributario`, arrecadação→
   `valorCreditoTributario`, baixa→`valorOperacaoBaixa`. Preservar todos os dados,
   IDs/bytes/hash/eventos/sessões/vínculos/sequências; falha reverte tudo.
5. Atualizar todos os chamadores, fixtures legadas, testes, README, manifesto e
   evidência. Sem sobrecarga antiga não utilizada, nova dependência/config/rota,
   engine de schemas ou antecipação do prazo prescricional.

## Fatias e verificação previstas

- RED→GREEN HTTP janeiro: quatro valores diferentes, histórico/zeros, bytes,
  consulta RECEBIDO/admin e contagens sem processamento ou efetivação.
- Schema: obrigatórios/null, opcional ausente/null, tipos, Unicode, extras,
  inteiros grandes e dinheiro exato; testar cada campo monetário separadamente.
  Ausência/null não vira zero; tipo não recebe default; sem janeiro-only.
- Domínio: cardinalidade/campos por assunto, precisão e imutabilidade contra
  mutação do mapa/lista de entrada e do resultado. Atualizar regressões anteriores.
- SQLite real: quatro linhas nomeadas por registro de saldo, valores distintos,
  duplicatas/múltiplos lotes, isolamento e reabertura. Migração V5 populada com
  três assuntos monetários deve atribuir nomes corretos e manter dinheiro/IDs/FKs;
  fixtures antigas usam schema antigo, sem depender do insert novo com `campo`.
- Falha intermediária de migração e rollback real de statement/COMMIT para cinco
  assuntos, incluindo os quatro valores de saldo; nenhuma confirmação parcial.
- Reviewer próprio, correções delegadas se houver achados. Supervisor executa
  clean verify e Docker Linux amd64 sem rede, incluindo upgrade real e reinício,
  antes de preparar PR local. T01/T16/E1/homologação continuam pendentes.
