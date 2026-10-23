# Tickets publicados — simulador e-Sfinge Tributário

Publicado e conferido em 23/10/2026, após aprovação explícita da divisão pelo solicitante.

27 issues abertas: 21 AFK e seis HITL. Rótulos criados e aplicados: `ready-for-agent`, `ready-for-human` e `needs-info`.

Dependências documentadas em `Blocked by` no corpo das issues, conforme a proposta aprovada. Não foram criadas dependências nativas, issue pai ou alterações nos rascunhos históricos E0. Não houve implementação, commit, push de código, implantação ou execução do piloto.

## Índice

| Local | Issue | Tipo | Bloqueada por | Histórias |
|---|---|---|---|---|
| I01 | [#1 — [E1][AFK] Autenticar usuário e UG fictícios no perfil 2026](https://github.com/lukadev08/fake-esfinge/issues/1) | AFK | Nenhuma | H01, H03, H04, H05, H06, H07, H08, H45, H49, H50, H51 |
| I02 | [#2 — [E1][AFK] Abrir envio e receber lotes de contribuintes sem processar](https://github.com/lukadev08/fake-esfinge/issues/2) | AFK | [#1](https://github.com/lukadev08/fake-esfinge/issues/1) | H02, H04, H05, H08, H09, H11, H12, H18, H19, H20, H21, H23, H26, H36, H40, H45, H46, H49, H50 |
| I03 | [#3 — [E1][AFK] Receber e inspecionar lotes de lançamento de crédito tributário](https://github.com/lukadev08/fake-esfinge/issues/3) | AFK | [#2](https://github.com/lukadev08/fake-esfinge/issues/2) | H02, H11, H13, H18, H19, H20, H21, H26, H45 |
| I04 | [#4 — [E1][AFK] Receber e inspecionar lotes de diário de arrecadação](https://github.com/lukadev08/fake-esfinge/issues/4) | AFK | [#2](https://github.com/lukadev08/fake-esfinge/issues/2) | H02, H14, H18, H19, H20, H21, H26, H45 |
| I05 | [#5 — [E1][AFK] Receber e inspecionar lotes de baixa de crédito tributário](https://github.com/lukadev08/fake-esfinge/issues/5) | AFK | [#2](https://github.com/lukadev08/fake-esfinge/issues/2) | H02, H15, H18, H19, H20, H21, H26, H45 |
| I06 | [#6 — [E1][AFK] Receber e inspecionar saldo inicial em cenário de janeiro](https://github.com/lukadev08/fake-esfinge/issues/6) | AFK | [#2](https://github.com/lukadev08/fake-esfinge/issues/2) | H02, H16, H18, H19, H20, H21, H26, H44, H45, H52 |
| I07 | [#7 — [E1][AFK] Receber e inspecionar início do prazo prescricional](https://github.com/lukadev08/fake-esfinge/issues/7) | AFK | [#2](https://github.com/lukadev08/fake-esfinge/issues/2) | H02, H17, H18, H19, H20, H21, H26, H44, H45 |
| I08 | [#8 — [E1][HITL] Comprovar recepção de payload real e persistência de E1](https://github.com/lukadev08/fake-esfinge/issues/8) | HITL | [#3](https://github.com/lukadev08/fake-esfinge/issues/3), [#4](https://github.com/lukadev08/fake-esfinge/issues/4), [#5](https://github.com/lukadev08/fake-esfinge/issues/5), [#6](https://github.com/lukadev08/fake-esfinge/issues/6), [#7](https://github.com/lukadev08/fake-esfinge/issues/7) | H01, H02, H20, H21, H36, H44, H49, H50 |
| I09 | [#9 — [E2][AFK] Processar ciclos por assunto em modos manual e automático](https://github.com/lukadev08/fake-esfinge/issues/9) | AFK | [#8](https://github.com/lukadev08/fake-esfinge/issues/8) | H10, H11, H22, H23, H24, H26, H36, H38, H40, H45 |
| I10 | [#10 — [E2][AFK] Finalizar envio com totalizadores dos registros efetivos](https://github.com/lukadev08/fake-esfinge/issues/10) | AFK | [#9](https://github.com/lukadev08/fake-esfinge/issues/9) | H10, H25, H26, H40, H45, H46 |
| I11 | [#11 — [E2][AFK] Injetar erro assíncrono intermediário com efeitos rastreáveis](https://github.com/lukadev08/fake-esfinge/issues/11) | AFK | [#9](https://github.com/lukadev08/fake-esfinge/issues/9) | H24, H26, H27, H28, H30, H38, H45 |
| I12 | [#12 — [E2][AFK] Descartar assunto e reenviar mantendo chave e demais assuntos](https://github.com/lukadev08/fake-esfinge/issues/12) | AFK | [#10](https://github.com/lukadev08/fake-esfinge/issues/10), [#11](https://github.com/lukadev08/fake-esfinge/issues/11) | H25, H26, H28, H29, H30, H45 |
| I13 | [#13 — [E2][AFK] Cancelar envio aberto e recomeçar com nova chave](https://github.com/lukadev08/fake-esfinge/issues/13) | AFK | [#10](https://github.com/lukadev08/fake-esfinge/issues/10), [#12](https://github.com/lukadev08/fake-esfinge/issues/12) | H26, H30, H31, H36, H40, H45 |
| I14 | [#14 — [E2][AFK] Reproduzir resposta perdida após commit e recuperação do assunto](https://github.com/lukadev08/fake-esfinge/issues/14) | AFK | [#12](https://github.com/lukadev08/fake-esfinge/issues/12) | H21, H26, H29, H30, H32, H33, H34, H38, H45 |
| I15 | [#15 — [E2][AFK] Bloquear competência pelo controle administrativo](https://github.com/lukadev08/fake-esfinge/issues/15) | AFK | [#9](https://github.com/lukadev08/fake-esfinge/issues/9) | H08, H35, H45 |
| I16 | [#16 — [E2][AFK] Controlar expiração de tokens e envios com relógio virtual](https://github.com/lukadev08/fake-esfinge/issues/16) | AFK | [#10](https://github.com/lukadev08/fake-esfinge/issues/10) | H07, H23, H24, H38, H39, H40, H45 |
| I17 | [#17 — [E2][AFK] Resetar cenário sem aceitar efeitos de trabalhos antigos](https://github.com/lukadev08/fake-esfinge/issues/17) | AFK | [#13](https://github.com/lukadev08/fake-esfinge/issues/13), [#14](https://github.com/lukadev08/fake-esfinge/issues/14), [#15](https://github.com/lukadev08/fake-esfinge/issues/15), [#16](https://github.com/lukadev08/fake-esfinge/issues/16) | H24, H35, H36, H37, H38, H39, H47, H49 |
| I18 | [#18 — [E2][AFK] Inspecionar execução e emitir evidências locais de conteúdo e cobertura](https://github.com/lukadev08/fake-esfinge/issues/18) | AFK | [#17](https://github.com/lukadev08/fake-esfinge/issues/17) | H03, H04, H05, H20, H26, H30, H42, H44, H45, H46, H48, H49, H50, H55 |
| I19 | [#19 — [E3][HITL] Executar piloto real e reconciliar referência independente local](https://github.com/lukadev08/fake-esfinge/issues/19) | HITL | [#18](https://github.com/lukadev08/fake-esfinge/issues/18) | H01, H02, H10, H11, H20, H25, H26, H27, H29, H31, H32, H33, H34, H41, H42, H43, H44, H49, H50 |
| I20 | [#20 — [E3][HITL] Aceitar matriz do recorte com seis assuntos e cenário complementar](https://github.com/lukadev08/fake-esfinge/issues/20) | HITL | [#19](https://github.com/lukadev08/fake-esfinge/issues/19) | H02, H10, H11, H16, H17, H38, H41, H42, H43, H44, H45, H52 |
| I21 | [#21 — [E4][AFK] Reproduzir backend em Docker Compose e CI Linux amd64](https://github.com/lukadev08/fake-esfinge/issues/21) | AFK | [#20](https://github.com/lukadev08/fake-esfinge/issues/20) | H01, H03, H38, H45, H47, H48, H49, H50, H51 |
| I22 | [#22 — [E4][HITL] Comprovar instalação teste e reset por outra pessoa](https://github.com/lukadev08/fake-esfinge/issues/22) | HITL | [#21](https://github.com/lukadev08/fake-esfinge/issues/21) | H44, H45, H47, H48, H49, H50, H51 |
| I23 | [#23 — [E5][AFK] Acompanhar remessas lotes e registros no painel local](https://github.com/lukadev08/fake-esfinge/issues/23) | AFK | [#22](https://github.com/lukadev08/fake-esfinge/issues/22) | H20, H26, H44, H45, H49, H50, H55 |
| I24 | [#24 — [E5][AFK] Selecionar cenário latência e falhas suportados no painel](https://github.com/lukadev08/fake-esfinge/issues/24) | AFK | [#23](https://github.com/lukadev08/fake-esfinge/issues/23) | H24, H38, H53 |
| I25 | [#25 — [E5][AFK] Avançar tarefas relógio e confirmar reset pelo painel](https://github.com/lukadev08/fake-esfinge/issues/25) | AFK | [#24](https://github.com/lukadev08/fake-esfinge/issues/24) | H24, H37, H38, H39, H54 |
| I26 | [#26 — [E5][HITL] Aceitar painel observando execução do integrador real](https://github.com/lukadev08/fake-esfinge/issues/26) | HITL | [#25](https://github.com/lukadev08/fake-esfinge/issues/25) | H43, H44, H48, H53, H54, H55 |
| I27 | [#27 — [Evolução][HITL] Fechar contrato dos casos adiados para o MVP completo](https://github.com/lukadev08/fake-esfinge/issues/27) | HITL | [#20](https://github.com/lukadev08/fake-esfinge/issues/20) | H04, H05, H18, H19, H27, H44, H45, H52 |

## Conferências

Releitura remota de todas as 27 issues confirmou título, corpo, rótulos, estado aberto e ausência de referências Ixx pendentes. Os corpos finais estão em [publication.json](publication.json); a [divisão aprovada](README.md) e [payload original](issues.json) preservam os textos anteriores à publicação. Hashes das seis fontes foram conferidos antes da publicação e não mudaram.

## Sanitização do piloto

A revisão automática de aprovação rejeitou a tentativa inicial de publicação de I19 por conter metadados internos de cliente, competência e volumes em repositório público. A tentativa rejeitada não criou issue. A versão publicada em #19 omite esses detalhes e preserva comparação independente interna, multiplicidades, conteúdo e demonstração de múltiplos lotes. Não houve envio do SQL, da estrutura do banco, de payloads originais ou de resultados internos.

## Continuidade

Primeira fatia sem bloqueios: [#1](https://github.com/lukadev08/fake-esfinge/issues/1). Sua execução requer solicitação própria. Os pontos HITL permanecem abertos; E5 só inicia após as saídas E1–E4 do recorte aprovado. #27 define a evolução ainda sem contrato suficiente e não bloqueia E5.
