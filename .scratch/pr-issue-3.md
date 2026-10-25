# PR preparado — issue #3

Situação: publicado e integrado com autorização do usuário no
[PR #30](https://github.com/lukadev08/fake-esfinge/pull/30), commit `e0b4852` em `main`.
Implementação revisada e validada no commit `8a08bc1`.
Destino: `lukadev08/fake-esfinge`; base `main`; head `issue-3-recepcao-creditos`.
Base funcional: `4b61304`; registro inicial da tarefa: `8431741`.

## Título

Recebe créditos tributários com decimais exatos

## Corpo exato proposto

Estende a recepção, consulta RECEBIDO e inspeção administrativa para lotes de
`lancamentosCreditosTributarios`, separados dos contribuintes sob a mesma chave.
Aplica o schema `credito`, com obrigatórios/opcionais, datas, comprimentos e mês
histórico válido, sem acrescentar regras fiscais, catálogos ou vínculos de negócio.

Valores monetários passam por BigDecimal e TEXT no SQLite, sem coerção ou
arredondamento. Bytes originais, multiplicidade, contagens, vínculos e eventos
permanecem atômicos. A migração V3 preserva dados, IDs, sessões e eventos da V2;
a inspeção conserva o valor numérico exato e exige chave administrativa própria.

Validação: `clean verify` sob supervisão com **61 testes**, zero falhas/erros/ignorados;
revisão independente aprovada sem achados materiais; **767 verificações na imagem
Linux amd64**, sem rede externa, incluindo upgrade, reinício, 10.001 registros e
leitura SQLite somente leitura. Outras **163 verificações** prepararam a base V2.

Demais quatro assuntos, processamento, finalização, reset, CI remoto e aceite
humano E1/T01 permanecem pendentes. Não comprova homologação oficial.

Closes #3

## Situação das ações externas

Push de `issue-3-recepcao-creditos` para `origin`, criação deste PR contra `main`
e merge em `lukadev08/fake-esfinge` autorizados e executados em 25/10/2026. Implantação e publicação
de imagem não estão incluídas. A revisão técnica está aprovada; a conta GitHub
não permite aprovação formal em PR da própria conta, conforme verificado na #1.
