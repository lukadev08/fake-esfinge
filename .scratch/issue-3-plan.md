# Preparação local — issue #3

Referência: [issue #3](https://github.com/lukadev08/fake-esfinge/issues/3).
Situação: implementação, revisão e supervisão locais concluídas após integração
da dependência #2 no [PR #29](https://github.com/lukadev08/fake-esfinge/pull/29),
commit `4b61304` em `main`.
Grupo próprio: `grupo3_explorer`, GPT-6 Luna/médio conforme o papel.
Branch local `issue-3-recepcao-creditos`; executor `grupo3_executor`, GPT-6.1 Sol/baixo,
e reviewer `grupo3_reviewer`, GPT-6.1 Sol/médio conforme os papéis, concluídos.
Publicação e merge da #3 ainda não autorizados.

## Contrato verificado

- Perfil derivado 2026/V6/6.06; schema `$defs.credito` e decisões no README do
  pacote contratual. Lista direta `lancamentosCreditosTributarios`, assunto interno
  `LANCAMENTO_CREDITO_TRIBUTARIO`; exatamente uma lista não vazia por lote.
- Onze propriedades obrigatórias, três opcionais/nulas, sem extras; preservar
  referências, tipos, zeros, multiplicidade e bytes. Datas de calendário estritas;
  competência do crédito com mês válido, incluindo créditos históricos.
- `valorLancamentoCreditoTributario` é número JSON, múltiplo exato de 0,01.
  `1`, `1.0` e `1.00` são equivalentes; não impor grafia fixa, sinal, teto fiscal ou
  limites numéricos ausentes do schema. Strings não substituem números.
- Propriedades imobiliárias/transferência ficam fora do schema. Cronologia,
  catálogos externos, referências de negócio e cálculos não serão validados.

## Direção da supervisão

1. Reutilizar abertura, autenticação, transação, consulta e inspeção da #2.
   Evoluir o parser existente para os dois assuntos sem framework de schemas ou
   nova dependência. Carregar assunto no conteúdo recebido e no lote persistido.
2. Validar dinheiro com `BigDecimal`, sem passagem por `double`/`float`.
   Manter os bytes originais em BLOB e persistir cada valor em TEXT associado ao
   lote e índice do registro, na mesma transação. Layout físico é escolha local;
   não há lacuna contratual que exija consulta ao usuário para definir a tabela.
3. Acrescentar V3 preservando V1/V2 e checksums. A restrição atual do assunto e a
   FK de eventos exigem migração com preservação de IDs, bytes, contagens,
   eventos, vínculos e sessão; falha deve reverter a migração inteira.
4. Inspeção deve escolher o assunto/lista corretos e serializar números exatos,
   sem `DoubleNode`. Núcleo continua Java puro; JSON/JDBC ficam nos adaptadores.

## Fatias TDD e validação previstas

- Recepção/consulta/inspeção de crédito válido e regressão de contribuintes.
- Schema: obrigatórios, opcionais, tipos, Unicode, datas, mês histórico, extras,
  mistura, lista vazia e dinheiro inválido; nenhuma coerção/arredondamento.
- Persistência: valores grandes, 1/1.0/1.00, TEXT comprovado por SQLite real,
  bytes exatos, vários lotes sem deduplicação, isolamento e reinício.
- Migração V2 com lotes/eventos existentes, IDs/FKs preservados; rollback real
  de statement/COMMIT abrangendo também valores monetários.
- Executor por fatia RED→GREEN; reviewer independente; correção delegada se houver
  achados. Supervisor confere testes verdes e Docker antes de preparar PR.
- Atualizar README, manifesto, evidência e PROGRESS com resultados observados.
  T01 integral, integrador real, aceite E1/#8 e homologação continuam pendentes.

## Autorização e sequência atual

O usuário autorizou a integração do PR #29 e a continuação da próxima tarefa.
Merge executado com head esperado `46c67f2`; PR merged e #2 encerrada conferidos.
Executada a #3 localmente pelo grupo próprio: 61 testes verdes, revisão sem achados
materiais e 767 verificações na imagem V3, além de 163 de preparação em V2.
Preparado PR local; publicação e integração exigem autorização específica.
