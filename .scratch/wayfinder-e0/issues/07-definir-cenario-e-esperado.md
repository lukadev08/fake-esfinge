# Definir cenário piloto e esperado independente

Status: draft
Type: grilling
Mode: HITL
Label: `wayfinder:grilling`
Blocked by: 03, 05


> Histórico: perguntas, dependências e pendências abaixo registram as rodadas anteriores.
> A situação vigente está na consolidação final ao término deste ticket. `Status: draft`
> continua indicando ausência de publicação, não ausência de decisão.

## Question

Qual cliente/competência fora de janeiro será usado no piloto local, como demonstrar vários lotes sob a chave compartilhada e como gerar esperado SQL independente por identificador, conteúdo e contagem? Registrar a cobertura ausente para o T01 completo.

## Answers

Resposta fornecida em 23/10/2026.

### Recorte inicial do piloto

- Selecionar um cliente e um mês **diferente de janeiro**, no perfil 2026. Cliente e mês concretos ainda não definidos.
- Saldo inicial envolve condições que tornam o primeiro teste menos prático; não será exigido nesse cenário inicial.
- Os seis assuntos devem estar disponíveis para recepção no simulador, mas não são todos obrigatórios em cada execução. Pode não haver saldo inicial ou início do prazo prescricional; sua ausência não impede o piloto inicial por si só.
- O piloto usará a base local de testes com dados originais de produção, conforme issue 03, e o fluxo por assunto com chave compartilhada relatado na issue 02. A pergunta foi atualizada em 23/10 para substituir a formulação anterior sobre cenário sintético e remessas separadas.

### Vários lotes

O cenário depende do volume do cliente. O usuário considera que um cliente com menos registros pode facilitar o ensaio, mas ainda verificará se o integrador permite reduzir o tamanho dos lotes. **Pendência do solicitante:** averiguar essa configuração e selecionar um conjunto que produza pelo menos dois lotes de um mesmo assunto. Não afirmar cobertura de fracionamento antes dessa confirmação e execução.

### Esperado independente e responsável

- O solicitante confirmou que consegue gerar consultas SQL na base local, antes do envio, com identificadores, campos relevantes e contagens esperadas por assunto, para comparar com o simulador.
- O próprio solicitante da implementação executará o piloto e conferirá a comparação.
- As consultas e seus resultados ainda não foram fornecidos nem executados nesta tarefa. O esperado deve ser independente da lógica de montagem de lotes do integrador.

### Limites de aceite e pendências

- Definir cliente, competência, conjunto de entrada estável, consultas e campos de comparação.
- Verificar possibilidade de reduzir lotes no integrador e demonstrar fracionamento sem perdas/duplicações.
- Planejar cenários adicionais para saldo inicial e início do prazo prescricional quando ausentes no primeiro piloto. Suporte de recepção não equivale a validação ou aceite desses assuntos.
- O piloto inicial fora de janeiro e possivelmente sem os seis assuntos não comprova sozinho o T01 completo descrito no planejamento. A cobertura ausente permanece explícita; não houve retirada automática desses requisitos do MVP.
- Manter dados originais no ambiente local controlado informado pelo usuário; exemplos e evidências compartilhados devem ser sanitizados.

Consolidação em 23/10: planejamento, seção 10.1, e [roteiro do piloto](../../../docs/contrato-e0.md#piloto-e-esperado-independente)
registram o aceite parcial e o procedimento a executar. Seleção de entrada,
consultas e execução continuam pendentes; o solicitante já foi identificado
como responsável, sem nova pendência de nomeação nesta etapa.

## Consolidação final de 23/10/2026 — plano aprovado

Situação: **parcial; duas pendências bloqueantes de E0**.

- Cliente/alias 14, competência 202609; cópia local sem alterações. O solicitante elaborará SQL, executará o integrador e conferirá resultado.
- Esperado cobre todos os campos enviados, chaves, contagens e multiplicidades por assunto, com strings/zeros e decimal exato, independente da montagem de lotes.
- Lote do integrador é partição de até 1.000 registros de um assunto; três lotes/3.000 foi exemplo, não volume real confirmado. Não impor esse tamanho como limite do simulador/TCE.
- **Falta 1:** consultas SQL e procedimento reproduzível com identificação da cópia, filtros/joins e campos do esperado, mantidos localmente.
- **Falta 2:** contagem real acima de 1.000 em ao menos um assunto do recorte; selecionar esse assunto para demonstrar vários lotes na execução futura.
- O solicitante decidiu manter E0 aberto nesses pontos. Não transferir pendências para E3 nem declarar execução, T01 integral ou MVP concluídos. Saldo inicial e prazo podem faltar no primeiro piloto, com cobertura complementar necessária.

Referência atual: [contrato E0](../../../docs/contrato-e0.md) e
[pacote contratual](../../../docs/contratos/2026-v6-6.06/README.md).
Origem: respostas do solicitante e plano explicitamente aprovado nesta conversa;
sem captura HTTP, execução do integrador ou publicação no GitHub.

## Atualização de 23/10/2026 — volume confirmado

O solicitante confirmou **21.770 lançamentos de crédito tributário** no cliente
14 / 202609. Considerando partições de até 1.000 registros, são esperados
**22 lotes: 21 de 1.000 e um de 770**. Contagem por relato, sem consulta independente
ao banco ou execução do integrador. Encerra a pendência documental de volume;
a demonstração do particionamento e da integridade dos registros pertence ao piloto.

Esta atualização substitui as indicações anteriores de volume não confirmado.
Resta somente **esperado SQL/procedimento reproduzível**, a elaborar pelo
solicitante sobre a cópia estável; E0 continua aberto. A contagem esperada deve
ser reconciliada com essa cópia, sem tratar mudança do recorte como sucesso.

## Correção de 23/10/2026 — tamanho de referência dos lotes

O solicitante corrigiu o relato anterior: **10.000 registros por lote**, em vez
de 1.000, com base na lembrança de outro cliente com cerca de 240 mil registros
em mais de 20 lotes. Para os **21.770 lançamentos** do cliente 14 / 202609,
a previsão passa a **3 lotes: dois de 10.000 e um de 1.770**.

Esta correção prevalece sobre as previsões anteriores de 22 lotes. Trata-se de
relato do solicitante, sem conferência independente da configuração ou execução
do fracionamento. A pendência documental de volume continua resolvida, pois
21.770 supera 10.000; resta o esperado SQL/procedimento. O piloto deverá conferir
o tamanho efetivo das partições e reconciliar todos os registros.

## Revisão autorizada de 23/10/2026 — referência interna por compliance

O solicitante autorizou substituir a preparação/entrega do SQL como bloqueio de
E0 pelo roteiro de conferência: responsável, recorte, campos e critérios estão
definidos no contrato. **Decisão documental E0 concluída no recorte inicial.**
As proibições anteriores de transferir a preparação para E3 são históricas e
foram substituídas expressamente nesta revisão.

SQL, estrutura do banco e resultados detalhados permanecem internos à empresa.
Quando o simulador estiver testável, em E3, o solicitante prepara a referência
antes do envio sobre a cópia estável e compara todos os campos enviados, chaves,
contagens e multiplicidades com recebidos/efetivos, sem reutilizar a montagem de
lotes. Compartilha apenas conclusão/divergências sanitizadas quando permitido.
O agente não exige nem certifica material ao qual não tem acesso.

Aceite do piloto/T01 continua pendente dessa execução e da cobertura aplicável;
contagens isoladas ou comparação com o próprio payload não bastam. Possíveis
diferenças descobertas em E3 podem exigir revisão/retrabalho. Mantidos cliente
14 / 202609, 21.770 créditos por relato e previsão de 3 lotes, ainda sem ensaio.
`Status: draft` permanece: nenhuma publicação ou implementação foi realizada.
