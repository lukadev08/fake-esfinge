# Confirmar preparo de entrada conhecida para o piloto

Status: draft
Type: task
Mode: HITL
Label: `wayfinder:task`
Blocked by: none


> Histórico: perguntas, dependências e pendências abaixo registram as rodadas anteriores.
> A situação vigente está na consolidação final ao término deste ticket. `Status: draft`
> continua indicando ausência de publicação, não ausência de decisão.

## Question

Como preparar e congelar a entrada conhecida na base local de testes, sem acoplar o simulador à origem? Registrar procedimento verificável e esperado independente.

## Answers

Resposta fornecida em 21/10/2026.

- É possível testar com dados extraídos de produção e mantidos em base local de testes, sem envio real ao TCE. Em 22/10/2026, o usuário confirmou que os dados conservam os valores originais e serão usados em ambiente controlado, fechado e local. São dados reais de produção em testes, não dados fictícios ou anonimizados.
- O próprio integrador possui etapas de extração e prepara os dados. O simulador deve somente recebê-los na fronteira HTTP, simular processamento/análise e devolver os estados do envio; não acessa a origem nem reproduz a extração.
- A base local de testes permite checar os registros e compará-los com os dados recebidos pelo simulador.

### Pendências para o piloto e T01

- Restrição documental revisada em 23/10 no planejamento e no `AGENTS.md`: dados originais ficam no ambiente local controlado. Essa orientação não autoriza publicar, versionar ou transferir esses dados para serviços externos. Exemplos e evidências compartilháveis continuam exigindo sanitização; credenciais do simulador serão fictícias.
- Detalhar procedimento reproduzível de preparo, seleção e congelamento do conjunto de entrada.
- Definir consultas/checagens e manifesto independente com chaves, conteúdo e contagens esperadas, sem reutilizar a lógica de montagem de lotes do integrador.
- Responsável por gerar SQL, executar e conferir definido posteriormente: o solicitante, conforme [issue 07](07-definir-cenario-e-esperado.md). Os detalhes do procedimento e as consultas ainda precisam ser fornecidos.

A disponibilidade da base de testes e das checagens foi relatada, não executada nesta tarefa. T01 permanece sem evidência de aceite.

O [roteiro do piloto](../../../docs/contrato-e0.md#piloto-e-esperado-independente)
consolida o procedimento a executar; sua documentação não comprova execução.

## Consolidação final de 23/10/2026 — plano aprovado

Situação: **parcial, vinculada ao piloto 07**.

- Cliente/alias 14 / competência 202609; uma UG por envio, sem divulgar identificação real adicional.
- Entrada será uma cópia local sem cargas, edições ou processamento concorrente durante esperado/envio; dados originais seguem restritos ao ambiente informado.
- Solicitante elaborará SQL independente cobrindo todos os campos enviados, chaves, contagens e multiplicidades, com identificação da cópia/filtros/procedimento reproduzível ainda a entregar.
- SQL/procedimento e volume real suficiente para múltiplos lotes permanecem as duas pendências E0 compartilhadas com 07, não tarefas novas duplicadas.

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
