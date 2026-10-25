# Preparação local — issue #5

Referência: [issue #5](https://github.com/lukadev08/fake-esfinge/issues/5).
Base funcional `cc2d12d`, após integração da #4 no PR #31.
Branch `issue-5-recepcao-baixas`. Exploração concluída em leitura somente pelo
`grupo5_explorer`, GPT-6 Luna/médio conforme o papel; identidade de caminho
fornecida pela configuração da orquestração, não introspecção do modelo.
Publicação/merge da #5 ainda não autorizados.

## Contrato verificado

- Perfil derivado 2026/V6/6.06; schema `$defs.baixa`: lista direta
  `baixasCreditosTributarios`, assunto `BAIXA_CREDITO_TRIBUTARIO`, onze propriedades,
  nove obrigatórias e duas opcionais/nulas. Exatamente uma lista não vazia por lote.
- Inteiros obrigatórios: `anoLancamentoCreditoTributario`,
  `codigoSequencialOperacaoBaixa`, `tipoBaixa`. Sem coerção ou faixas/enums fiscais
  inventados; números matematicamente integrais e grandes devem ser conservados.
- Strings obrigatórias: `codigoLancamentoCreditoTributario` (máximo16),
  `indicativoEstornoBaixa` (1), `motivoBaixa` (512), `numeroCpfResponsavelOperacao` (11).
  `dataBaixa` é data de calendário estrita YYYY-MM-DD. Limites Unicode do schema,
  sem mínimo inventado, validação fiscal de CPF ou normalização de zeros.
- Opcionais/nulas: `anoLancamentoCreditoTributarioDividaAtiva` (inteiro) e
  `codigoLancamentoCreditoTributarioDividaAtiva` (string, máximo16).
- Único dinheiro: `valorOperacaoBaixa`, número JSON múltiplo exato de 0,01.
  Preservar BigDecimal/TEXT/bytes; 1/1.0/1.00 equivalentes, sem arredondamento.
- Não validar crédito existente, dívida ativa, cronologia (inclusive baixa anterior
  ao crédito), catálogo ou cálculo fiscal. Recepção/consulta mantêm RECEBIDO.

## Direção da supervisão

1. Acrescentar assunto, parser estrito e projeção administrativa. Reutilizar
   abertura, autenticação, transação, consulta e inspeção; sem nova rota/config/dependência.
2. `ReceivedContent.monetaryValues` e JDBC já suportam um valor por registro;
   reutilizar `valores_recebidos` sem novos campos/tabelas/abstrações. Conservar
   bytes, multiplicidade, ordem, contagens, vínculos e evento atômicos.
3. V5 aditiva amplia CHECK de assunto; reconstruir lotes, eventos e valores
   com FKs habilitadas, copiando todos os dados/IDs/bytes/hash e sequências em
   transação única. V1–V4/checksums/fontes intactos. Falha reverte atualização inteira.
4. Atualizar README, manifesto e evidência própria com limites fiscais e de aceite.

## Fatias e verificação previstas

- Primeiro RED→GREEN HTTP: baixa recebida, consultada e inspecionada com assunto,
  zeros, bytes, decimal e contagens exatos; sem processamento/efetivação.
- Todos os obrigatórios ausentes/null, opcionais ausentes/null, tipos sem coerção,
  datas, Unicode, extras/misturas, inteiros grandes e dinheiro inválido. Demonstrar
  referências inexistentes e data anterior ao crédito aceitas apenas estruturalmente.
- SQLite real: TEXT, vários lotes/registros idênticos sem dedupe, isolamento,
  reabertura, migração V4 populada com dinheiro de crédito/arrecadação, high-water,
  checksum e falha reversível. Rollback real de statement/COMMIT para quatro assuntos.
- Executor/reviewer próprios; eventual correção delegada. Supervisor confere
  clean verify e Docker Linux amd64 sem rede, com upgrade real e reinício, antes
  de preparar PR local. T01 integral/E1/#8 e homologação permanecem pendentes.
