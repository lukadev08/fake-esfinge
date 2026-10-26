# Publicação do roteiro — apoio à issue #8

Destino autorizado: `lukadev08/fake-esfinge`, branch `issue-8-aceite-e1` contra `main`.
Somente documentação e registros; a issue #8 continua aberta e E1 não está aprovado.
Publicado e integrado no [PR #35](https://github.com/lukadev08/fake-esfinge/pull/35),
head `2bc1444`, merge `ddf84875a5afd5a336537ca631afe04ef9ebfd1c`.

## Título

Documenta roteiro para o aceite humano de E1

## Corpo exato proposto

Disponibiliza o roteiro para o solicitante comprovar recepção pelo integrador real
e preservação de bytes, vínculos e contagens após reabrir a instância no ambiente
interno controlado. Inclui referência do artefato, cuidados com dados internos e
modelo de conclusão sanitizada, com todos os critérios de execução e aceite pendentes.

A consulta e a inspeção podem ocorrer após o disparo rejeitado com HTTP422.
A comparação obrigatória usa inspeções anteriores/posteriores ao reinício; o
corpo enviado é uma referência adicional quando disponível, sem exigir nova
instrumentação do integrador. Atualiza também os registros da integração da #7.

Validação: revisão documental independente aprovada após correções de escopo;
três links locais e whitespace conferidos; `./mvnw verify` supervisionado com
**105 testes, zero falhas/erros/ignorados**, exit0 em 11,123s. Código, testes,
dependências e Dockerfile permanecem iguais a `53814c6`.

Este PR publica somente a preparação. Não comprova execução real, não aprova a
saída E1 e não encerra a #8. E2 permanece aguardando o aceite ou uma decisão
expressa do solicitante sobre alterar essa sequência. Sem novo ensaio Docker,
integrador real, CI remoto ou aceite T01/E3 integral.

Refs #8

## Autorização e limites

O usuário autorizou o próximo push/PR/merge com testes verdes e a continuação sob
supervisão. A publicação documental foi concluída com corpo exato acima, revisão
aprovada e testes verdes. Execução e aceite E1 não foram demonstrados; a decisão
sobre antecipar E2 foi solicitada separadamente e continua pendente de resposta.
