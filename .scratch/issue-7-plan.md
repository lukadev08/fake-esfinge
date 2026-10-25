# Preparação local — issue #7

Referência: [issue #7](https://github.com/lukadev08/fake-esfinge/issues/7).
Base funcional `ad27bd8`, após integração da #6 no PR #33.
Branch `issue-7-recepcao-prazo`. Exploração concluída somente leitura por
`grupo7_explorer`, identificado como GPT-6 Luna/médio. Publicação/merge da #7
ainda não autorizados. E2 aguarda aceite humano #8 após esta fatia.
Implementação e revisão concluídas por `grupo7_executor`, GPT-6.1 Sol/baixo, e
`grupo7_reviewer`, GPT-6.1 Sol/médio, ambos identificados. Suíte limpa de 105 testes
e 678 verificações Docker Linux amd64 aprovadas. Nenhuma correção obrigatória.
[Corpo exato de PR](pr-issue-7.md) preparado; container removido e volume fictício
preservado até autorização específica de exclusão.

## Contrato e limite

- Perfil derivado 2026/V6/6.06, schema `$defs.prazo`, lista direta
  `inicioPrazoPrescricional`, assunto `INICIO_PRAZO_PRESCRICIONAL`.
- Quatro campos obrigatórios, sem extras: `anoLancamentoCreditoTributarioOuDividaAtiva`
  inteiro; `codigoLancamentoCreditoTributarioOuNumeroInscricaoDividaAtiva` string
  de até16 codepoints, preservando zeros; `dataInicioPrazoPrescricional` e
  `dataAtualizacaoPrazoPrescricional` strings de calendário `YYYY-MM-DD`.
- Sem coerção, catálogo, teto/ano fiscal ou cronologia inferida. O schema foi
  aprovado a partir do exemplo, sem DTO extraído; não reabrir decisões E0.
  Prescrição fiscal e relação com crédito/dívida ativa continuam não implementadas.
- Recepção/consulta/inspeção/reinício preservam bytes, referências e contagens;
  não processar ou efetivar. Caso ausente no piloto, cenário complementar segue
  pendente. Contribuição parcial a T01/T02/T18, sem encerramento E1/T01.

## Implementação mínima e validação

1. Fatia RED→GREEN HTTP para prazo hoje rejeitado com422: parser estrutural,
   enum, seleção administrativa e `ReceivedContent` não monetário com lista vazia.
   Retirar tratamento de assunto sem suporte agora que os seis estão cobertos.
2. Persistência existente mantém atomicidade, sem nova tabela/linha de dinheiro
   para prazo. V7 aditiva reconstrói tabelas vinculadas para admitir o sexto assunto,
   preservando V1–V6/checksums, bytes/IDs/sessões/eventos/campos/valores/FKs/sequências.
   Fixture anterior contém somente os cinco assuntos admitidos na V6; prazo entra
   depois da migração. Falha intermediária deve reverter integralmente.
3. Cobrir todos os obrigatórios ausentes/null, tipos sem coerção, limites Unicode,
   inteiro matemático/grande, ambas as datas e calendário (bissexto/inválido),
   extras/listas vazias/nulas/mistas e histórico sem regras fiscais inventadas.
4. Provar seis assuntos separados na mesma remessa, duplicatas, vínculos/isolamento,
   bytes/contagens após reabertura, ausência de valores de prazo e quatro valores
   de saldo preservados. Falhas reais de statement/COMMIT para os seis assuntos.
5. Atualizar chamadores/fixtures/expectativas de versão e cobertura ausente, README,
   manifesto e evidência. Sem dependência/config/rota/infra nova ou antecipação E2.
6. Executor próprio implementa em TDD e executa suíte. Reviewer independente revisa
   o diff; correções delegadas se necessárias. Supervisor confere `clean verify`
   e Docker Linux amd64 sem rede, upgrade de V6 preenchida e reinício antes do PR.

## Comando conhecido

`pom.xml`, wrapper e Dockerfile conferidos; testes usam SQLite local e credenciais
fictícias, sem acesso ao TCE ou produção. Comando da base, adaptando seleção focalizada:

```sh
rtk proxy env JAVA_HOME=/Users/luka/.sdkman/candidates/java/current \
  MAVEN_USER_HOME=/private/tmp/fake-esfinge-wrapper-check-fixed \
  ./mvnw -B -ntp -Dmaven.repo.local=/private/tmp/fake-esfinge-m2 verify
```

Cache Maven e artefatos fora do workspace podem exigir escalada do sandbox;
preservar diagnósticos/exit codes. Sem publicação ou merge da #7 nesta preparação.
