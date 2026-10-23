# Fixar perfil anual e precedência das fontes de contrato

Status: draft
Type: grilling
Mode: HITL
Label: `wayfinder:grilling`
Blocked by: 01, 02


> Histórico: perguntas, dependências e pendências abaixo registram as rodadas anteriores.
> A situação vigente está na consolidação final ao término deste ticket. `Status: draft`
> continua indicando ausência de publicação, não ausência de decisão.

## Question

Qual exercício e versão o integrador envia? Qual fonte prevalece quando export, manual e amostras divergem, e quais lacunas serão marcadas como contrato derivado?

## Answers

Resposta fornecida em 22/10/2026.

- Perfil confirmado pelo usuário: exercício **2026**, endpoints **V6**, leiaute **6.06**. Não misturar contratos de outros exercícios.
- Precedência aceita: o manual do perfil selecionado é a referência. Diferenças necessárias ao comportamento observado do cliente devem ser documentadas como aproximações explícitas, sem apresentá-las como comportamento oficial. Não foi fornecido export OpenAPI; eventual conflito com um export futuro requer confronto de versão e origem, sem substituição silenciosa.
- O contrato reconstruído dos manuais e da extração de DTOs será classificado como **derivado**, com lacunas identificadas. Não equivale a uma especificação oficial exportada.
- Permanecem as lacunas das issues 01 e 02, incluindo schemas completos, autenticação/headers, parâmetros exatos, respostas e erros, interpretação dos estados pelo cliente e confirmação do funcionamento da remessa.
- A versão do catálogo de CONs não foi confirmada nesta resposta; 6.9 continua referência proposta no planejamento, não versão validada pelo usuário.

Confirmação baseada no relato do usuário, sem execução do integrador ou validação independente de compatibilidade.

Consolidação em 23/10: perfil e precedência incorporados ao planejamento,
`AGENTS.md` e [contrato E0](../../../docs/contrato-e0.md#perfil-e-evidências).
A confirmação de 2026/V6/6.06 não está mais pendente de resposta do usuário;
permanecem os limites de evidência e as lacunas acima, inclusive CONs.

## Consolidação final de 23/10/2026 — plano aprovado

Situação documental do recorte inicial: **concluída**.

- Mantido perfil 2026/V6/6.06 e precedência do manual; pacote derivado incorpora fontes com hashes e declara limites de autenticidade/data.
- HTTP e schemas prioritários consolidados por relato e escolhas derivadas aprovadas. Divergências estão registradas no pacote; não misturar enum/fluxo de outro exercício.
- CONs 6.9 e regras fiscais permanecem evolução fora da validação inicial, sem constituir nova pendência de saída de E0.

Referência atual: [contrato E0](../../../docs/contrato-e0.md) e
[pacote contratual](../../../docs/contratos/2026-v6-6.06/README.md).
Origem: respostas do solicitante e plano explicitamente aprovado nesta conversa;
sem captura HTTP, execução do integrador ou publicação no GitHub.
