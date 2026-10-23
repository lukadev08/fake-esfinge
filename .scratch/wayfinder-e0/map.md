# Mapa E0 — decidir contrato e piloto do simulador tributário

Status: rascunho local, consolidado em 23/10/2026. Este mapa e seus tickets não foram publicados; o rastreador canônico continua sendo GitHub Issues. `Status: draft` nos tickets indica ausência de publicação, não ausência de respostas.

## Destination

Fechar as decisões necessárias para executar E0: perfil anual e fontes de contrato, chamadas reais e operações do piloto, resultados prioritários, cenário de entrada conhecida, esperado independente e regras mínimas dos seis assuntos. Cada lacuna restante deve ter responsável e evidência necessária identificados.

## Notes

Fonte de escopo: [planejamento do simulador](../../docs/planejamento-simulador-tributario.md), sobretudo seções 3–5, 10, 13 e 15. Ler [AGENTS.md](../../AGENTS.md) e [documentação de domínio](../../docs/agents/domain.md); usar `grilling` e `domain-modeling` nas decisões.

Este mapa acompanha E0. O [contrato E0 consolidado](../../docs/contrato-e0.md) define o recorte inicial derivado; E0 está concluído documentalmente após revisão autorizada por compliance: roteiro definido, referência e conferência internas pendentes em E3; volume confirmado por relato de 21.770 créditos, sem execução. Dados originais do piloto ficam apenas no ambiente local controlado; arquivos versionados, exemplos e evidências compartilhadas usam conteúdo fictício ou sanitizado. Credenciais do simulador serão fictícias. Registrar origem, data e distinção entre contrato oficial e derivado.

Há referências a manuais e extração de DTOs, hashes registrados e inventário relatado pelo usuário. Os cinco insumos foram incorporados ao [pacote contratual](../../docs/contratos/2026-v6-6.06/README.md), com bytes/hashes preservados; o código bruto do integrador não está disponível por restrição informada. Todos os tickets permanecem HITL; as decisões abaixo não equivalem a testes ou comprovação do contrato oficial.

## Decisions so far

| Ticket | Consolidação vigente de 23/10, sem apagar histórico | Situação documental |
|---|---|---|
| [01 — Fontes](issues/01-disponibilizar-fontes-e-amostras.md) | Cinco fontes incorporadas, hashes iguais aos de 21/10; datas desconhecidas declaradas | Concluída no recorte derivado |
| [02 — Chamadas](issues/02-obter-inventario-das-chamadas.md) | HTTP, envelopes, UG/chave, leitura de situacao, totalizadores e plataforma confirmados por relato | Concluída no recorte derivado |
| [03 — Entrada](issues/03-confirmar-entrada-conhecida.md) | Cópia local sem alterações, cliente 14 / 202609; SQL do solicitante | Definição E0 concluída; preparação/conferência em E3 |
| [04 — Perfil](issues/04-fixar-perfil-e-fontes.md) | 2026/V6/6.06, precedência e aproximações rastreadas | Concluída no recorte; CONs são evolução |
| [05 — Operações](issues/05-delimitar-operacoes-e-assuntos.md) | Autenticação, descarte por assunto e alcance integral definidos; rota explícita de lote fora do inicial | Concluída no recorte derivado |
| [06 — Recuperação](issues/06-definir-respostas-e-erros.md) | HTTP/erros, retomada, expiração, repetições, finalização e exclusões aprovadas | Concluída no recorte derivado |
| [07 — Piloto](issues/07-definir-cenario-e-esperado.md) | Cliente 14 / 202609, 21.770 créditos confirmados; previsão de 3 lotes; roteiro definido, referência interna/comparação em E3 | Definição E0 concluída; aceite do piloto pendente |
| [08 — Validação](issues/08-selecionar-regras-minimas.md) | Seis schemas, ajustes do cliente, manifesto e quatro pontos de teste confirmados | Concluída documentalmente; domínio adiado |

Todos os tickets continuam `Status: draft`, não publicados; “concluída” acima
qualifica a decisão documental, sem comprovar software. Os tickets preservam
respostas anteriores e acrescentam a consolidação final, que prevalece sobre
pendências e dependências históricas. Nenhum fechamento externo foi executado.

## Situação atual e conferência pendente
+
+Critério de E0 revisado por autorização expressa do solicitante em 23/10/2026:
+o roteiro, responsável, recorte, campos e critérios estão definidos no contrato.
+**E0 concluído documentalmente no recorte inicial.** SQL, estrutura do banco e
+resultados ficam internos por compliance; o solicitante prepara a referência
+antes do envio e realiza a comparação independente em E3 quando o simulador
+estiver testável. Compartilha só conclusão/divergências sanitizadas quando permitido.
+
+Permanece pendente em E3 preparar e comparar todos os campos enviados, chaves,
+contagens/multiplicidades sobre a cópia estável do cliente 14 / 202609. Volume
+de 21.770 créditos é relato; 3 lotes (2 × 10.000 + 1.770) são previsão a conferir.
+Somente contagens ou o próprio payload não comprovam o esperado independente.
+
+Esta revisão substitui o bloqueio anterior de SQL em E0, preservado no histórico.
+Nenhum piloto ou T01 foi aprovado, nenhum dado interno foi recebido e nenhum
+ticket foi publicado. CONs, regras fiscais, SIM/omitido, sem movimento e limite
+mensal continuam fora do aceite inicial; descoberta tardia de diferenças pode
+exigir retrabalho no contrato ou aplicação.
+

## Out of scope

- Implementar o simulador ou executar/aprovar T01 nesta atualização documental; fechamento de E0 no recorte não equivale ao MVP completo.
- Entregas de E1–E4, NFS-e e ampliação silenciosa dos seis assuntos.
