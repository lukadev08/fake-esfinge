# Preparação local — issue #4

Referência: [issue #4](https://github.com/lukadev08/fake-esfinge/issues/4).
Base funcional: `e0b4852`, após integração da #3 no PR #30.
Branch: `issue-4-recepcao-arrecadacao`. Exploração concluída por
`grupo4_explorer`, GPT-6 Luna/médio conforme o papel, somente leitura.
Publicação/merge da #4 ainda não autorizados.

## Contrato verificado

- Perfil derivado 2026/V6/6.06, schema `$defs.arrecadacao`: lista direta
  `diarioGeralArrecadacao`, assunto `DIARIO_ARRECADACAO`, dez propriedades,
  oito obrigatórias e duas opcionais/nulas. Lista não vazia, um assunto por lote.
- Obrigatórias: ano/código da operação, tipo, data de operação, código de receita,
  ano/código do crédito e `valorCreditoTributario`. Strings com limites do schema;
  datas de calendário, inteiros matemáticos sem coerção ou teto fiscal inventado.
- Opcionais/nulas: `numeroAnoTipoCobranca` (string, máximo 50) e `numeroParcelas`
  (inteiro). Plural é aproximação relatada pelo cliente; singular `numeroParcela`
  deve receber 422, sem alias ou conversão silenciosa.
- Um valor monetário por registro: número JSON decimal exato, múltiplo de 0,01.
  Preservar BigDecimal, dinheiro TEXT, bytes/zeros/tipos e multiplicidades.
- Sem validar crédito existente, cronologia, enum/catálogo ou cálculo tributário.
  Recepção e consulta mantêm RECEBIDO, sem processamento.

## Direção da supervisão

1. Acrescentar terceiro assunto em `Assunto`, parser e projeção administrativa;
   reutilizar autenticação, abertura, transação, recepção e consulta existentes.
   Preservar regressões de contribuintes e créditos.
2. Renomear a lista `creditValues` para `monetaryValues`, atualizando todos os
   chamadores/testes. Os dois assuntos monetários existentes têm exatamente um
   valor por registro; contribuinte não tem valor monetário.
3. Usar uma tabela estreita comum `valores_recebidos`, por lote/índice/UG/cenário,
   no lugar de duplicar a mesma estrutura física para arrecadação. É reutilização
   pelos dois assuntos desta entrega, sem metadados genéricos de campos monetários.
   Valores continuam TEXT via BigDecimal; sem double/float/REAL/arredondamento.
4. V4 aditiva: V1–V3 e fontes intactas. Reconstruir lotes, eventos e valores
   existentes com FK habilitada e uma transação; copiar valores de crédito para
   a tabela comum, preservando bytes/hash/IDs/sessões/eventos/sequências/vínculos.
   Testar reversão completa de falha e upgrade de V3 populada com dinheiro.
5. Atualizar README, manifesto e evidência própria com limites de cobertura.
   Não criar dependência, configuração ou rota adicional.

## Fatias e verificação previstas

- Primeiro comportamento HTTP RED→GREEN: arrecadação aceita, consultada RECEBIDO
  e inspecionada com decimal exato, zeros, bytes e contagens.
- Schema positivo/negativo, opcionais ausentes/null, tipos, limites Unicode,
  datas, singular/plural, extras e listas mistas. 1/1.0/1.00 e valores grandes;
  frações não múltiplas de 0,01 rejeitadas sem arredondamento.
- SQLite real: TEXT, múltiplos lotes sem dedupe, vínculos/isolamento, reinício,
  upgrade V3, high-water e rollback real de statement/COMMIT incluindo valores.
- Executor e reviewer próprios; correções delegadas se necessárias. Supervisor
  confere `clean verify` e Docker Linux amd64 sem rede, incluindo upgrade real,
  antes de preparar PR local. T01/E1/#8 e homologação continuam pendentes.
