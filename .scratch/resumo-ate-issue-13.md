# Resumo do desenvolvimento até a issue #13

Data: 27/10/2026. Repositório: `lukadev08/fake-esfinge`.
Encerramento solicitado após a #13; **#14 não iniciada**. Este resumo e os
registros finais de encerramento são locais, sem nova publicação documental.

## Situação confirmada

- #13 concluída e integrada em `main` pelo
  [PR #40](https://github.com/lukadev08/fake-esfinge/pull/40).
  Head validado: `c3714b9dff30229f931d24e74ccb73c68f4362d6`;
  merge: `566afda0c3ac9c6f1f623d752407616978b5cd93`.
- GitHub confirmou 12 das 27 issues encerradas: #1–#7 e #9–#13.
  #8 e #14–#27 continuam abertas. Essa contagem não mede percentual de conclusão
  do MVP: tarefas têm escopos e aceites diferentes.
- E0 fechado documentalmente. E1 tem recepção técnica implementada, mas seu
  aceite humano #8 permanece pendente. Desenvolvimento técnico E2 autorizado
  separadamente e implementado até a #13, sem concluir E2 integral, E3 ou o MVP.

## O que foi entregue

| Issues | Capacidade implementada e integrada |
| --- | --- |
| #1–#2 | Autenticação fictícia por usuário/UG, tokens e controle administrativo separado; abertura de envio, recepção de contribuintes, persistência e inspeção local |
| #3–#7 | Recepção/inspeção de crédito tributário, diário de arrecadação, baixa, saldo inicial e início do prazo prescricional, completando os seis assuntos do recorte |
| #9 | Disparo explícito e processamento persistido nos modos manual/automático; consultas não avançam tarefas |
| #10 | Finalização com totalizadores derivados dos registros efetivos, guardas de sequência e rejeição de divergências |
| #11 | Injeção administrativa de erro assíncrono, diagnóstico, descarte do lote alvo e aborto rastreável dos seguintes do mesmo assunto |
| #12 | Descarte de assunto e reenvio sob a mesma chave, conservando outros assuntos e controles históricos |
| #13 | Cancelamento integral do envio aberto, inclusive trabalhos agendados/EM; nova chave para reenvio integral e proteção contra gravações tardias |

O backend usa Java 21, Spring Boot 3.5.10, Maven, SQLite e Docker Linux amd64,
com portas/adaptadores e perfil derivado 2026/V6/6.06. Preserva bytes/hash,
identificadores/zeros, decimais exatos e vínculos lote/remessa/UG/cenário.
Recebidos, processados, efetivos e históricos inativos são distinguíveis.

V11 acrescenta cancelamento distinto de finalização e terminal ABORTADO sem
alterar V1–V10. Originais, diagnósticos, planos e sequências são conservados;
trabalhos antigos não recriam efeitos após cancelamento/reinício. As políticas
derivadas e aproximações estão no [manifesto](../docs/cobertura.md).

## Validação mais recente

- Na #13, equipe própria de exploração, execução e revisão independente;
  aprovada sem achados materiais. TDD comportamental e SQLite real.
- Executor e supervisor confirmaram **243 testes**, zero falhas/erros/ignorados;
  supervisor executou `clean verify` e conferiu 26 relatórios.
- **1.807 verificações na imagem final da #13**, todas exit0: upgrade V10→V11,
  cancelamento/reenvio, reinício, conteúdo/decimais/FKs e agendador automático.
  Preparação 270 + 3 separada. Não somar ensaios de issues anteriores como cobertura
  adicional: parte dos cenários é repetida entre entregas.
- Falhas intermediárias de fixtures/clientes foram corrigidas e registradas;
  não há falha funcional remanescente identificada na suíte/imagem final.
- Containers temporários removidos. Os três volumes da #13 foram excluídos com
  autorização específica e ausência confirmada. Imagens/clientes locais preservados.

Detalhes, hashes e limites: [evidência #13](../docs/evidencias/issue-13.md).
Publicação/merge não alteraram o código validado; testes não repetidos para o
registro documental final. Check-runs/statuses do head estavam vazios: **sem CI
remoto executado**, embora testes e Docker locais estejam verdes.

## O que permanece pendente

- **#8/E1:** solicitante executar e aceitar o ensaio com payload efetivamente
  produzido pelo integrador no ambiente interno controlado, sem data estabelecida.
- #14–#18: resposta perdida após commit, bloqueio de competência, expiração
  completa, reset e ampliação de inspeção/evidências. Bases parciais existentes
  não encerram esses tickets.
- #19–#20: piloto do integrador real, referência independente e aceite da matriz E3.
- #21–#22: Compose/CI Linux amd64 e comprovação de reprodução por outra pessoa.
- #23–#26: painel local E5 e aceite humano, após seus pré-requisitos.
- #27: fechamento do contrato dos casos adiados para o MVP completo.

Não houve chamada autenticada ao TCE, execução do integrador real, homologação oficial,
publicação de imagem ou implantação. Dados de ensaio são fictícios; payloads,
SQL e resultados internos do cliente continuam fora do Git/serviços externos.

Nenhuma continuação automática para #14 será feita neste trabalho. Uma retomada
depende de nova orientação do solicitante.
