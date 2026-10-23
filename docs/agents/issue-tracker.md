# Rastreador de issues: GitHub

Issues e especificações deste repositório ficam no GitHub Issues de `lukadev08/fake-esfinge`. Execute `gh` a partir do clone para identificar o repositório pelo remoto.

## Operações

- Criar: `gh issue create --title "..." --body-file <arquivo>`.
- Ler corpo, comentários e rótulos: `gh issue view <número> --comments`.
- Listar: `gh issue list --state open --json number,title,body,labels,comments`; filtre por estado ou rótulo conforme a tarefa.
- Comentar: `gh issue comment <número> --body-file <arquivo>`.
- Aplicar ou remover rótulo: `gh issue edit <número> --add-label "..."` ou `--remove-label "..."`.
- Fechar: `gh issue close <número> --comment "..."`.

Quando uma publicação no rastreador estiver autorizada para a ação e o alvo,
crie uma issue. Uma instrução da skill, isoladamente, não autoriza publicação.
Para buscar um ticket publicado, leia a issue e seus comentários.

## Rascunhos locais E0

O [mapa E0](../../.scratch/wayfinder-e0/map.md) e seus oito tickets são rascunhos
locais, sem IDs do GitHub. Os números `01`–`08` e `Blocked by` identificam arquivos
e dependências locais; não equivalem a issues ou dependências nativas publicadas.
`Status: draft` indica ausência de publicação, não ausência de respostas.
Consulte o mapa para decisões já registradas e pendências reais. Preservar esses
rascunhos não altera a escolha de GitHub Issues como rastreador canônico.

## Pull requests como pedidos

**PRs as a request surface: no.** Altere para `yes` se PRs externos também entrarem na triagem.

Se ativado, leia o PR e o diff com `gh pr view <número> --comments` e `gh pr diff <número>`. Na descoberta, considere apenas autores externos (`CONTRIBUTOR`, `FIRST_TIME_CONTRIBUTOR` ou `NONE`). Para um `#número` ambíguo, tente `gh pr view` e depois `gh issue view`.

## Wayfinder

- Mapa: uma issue com rótulo `wayfinder:map`.
- Ticket filho: sub-issue do mapa; se indisponível, use uma lista de tarefas no mapa e `Part of #<mapa>` no corpo do ticket.
- Bloqueios: dependências nativas do GitHub; se indisponíveis, registre `Blocked by: #<número>` no corpo.
- Próximo ticket: primeiro filho aberto, sem responsável e sem bloqueios abertos, na ordem do mapa.
- Reivindicar: `gh issue edit <número> --add-assignee @me`.
- Resolver: registre a resposta, feche o ticket e acrescente ao mapa um ponteiro para a decisão.
