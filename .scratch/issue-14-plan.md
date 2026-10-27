# Plano local — issue #14

Referência: [#14](https://github.com/lukadev08/fake-esfinge/issues/14), T12,
contrato derivado 2026/V6/6.06. Retomada autorizada em 27/10/2026 após a parada
da #13; este plano não encerra #8/E1 nem T01/E3.

Desenho aprovado pela supervisão antes da produção e implementado. Revisão de
código e conferência documental final aprovadas pelo revisor exclusivo, sem
achados materiais. Resultados e limites estão na [evidência](../docs/evidencias/issue-14.md).

Publicação concluída em27/10/2026 no [PR #41](https://github.com/lukadev08/fake-esfinge/pull/41),
merge `f4ba83693ce9d12900ff140248b6dc3661cb193e`; #14 encerrada como completed.
#15 explorada e ainda não implementada; aceites humanos continuam pendentes.

## Contrato administrativo proposto

GET/PUT/DELETE `/__simulator/falhas/recepcao` com query exata
`chavePacote`/`assunto`, chave própria `X-Simulator-Key` e nenhuma instrução
de teste no payload compatível. `assunto` usa os seis nomes já existentes.

PUT exige JSON UTF-8 estrito
`{"tipo":"ANTES_ACEITACAO","tentativa":2}` ou tipo `APOS_COMMIT`.
`tentativa` é inteiro positivo de até `Long.MAX_VALUE`, relativo às próximas
recepções elegíveis desse envio/assunto após a configuração. GET/PUT devolvem
`chavePacote`, `assunto`, `tipo`, `tentativa`, `tentativas`, `consumida` e
`recuperacaoPendente`. GET sem plano retorna 404; DELETE sem plano também.
GET/DELETE não têm corpo. Parâmetros/corpo inválidos retornam 400, assunto sem
cobertura 422, administração não autorizada 401 e envio de outro cenário 404.
PUT/DELETE exigem envio aberto. Plano com recuperação pendente não pode ser
substituído ou removido (409): o controle administrativo não contorna o descarte.
Remover plano não recuperável ou reconfigurá-lo é permitido; o histórico de
eventos e lotes anteriores permanece. Administração não renova atividade.

## Transições e atomicidade

| Entrada / condição | Efeito persistido | Resultado |
|---|---|---|
| Inválida, não autorizada, outra UG/cenário ou chave fechada | Nenhum; plano não avança | Erro HTTP habitual |
| Sem plano / plano já consumido sem recuperação pendente | Recepção habitual | Confirmação habitual |
| Elegível antes do alvo | Incrementa contador junto com lote/valores/atividade/evento | Confirmação habitual |
| Alvo ANTES_ACEITACAO | Somente contador/consumo do controle de falha; nenhum lote, valor, trabalho, atividade ou evento de aceitação | Interrupção real de transporte |
| Alvo APOS_COMMIT | Lote/bytes/valores/vínculos/evento de aceitação, consumo e marca de recuperação no mesmo commit | Interrupção real de transporte; lote RECEBIDO |
| Nova recepção do assunto com marca pendente | Nenhum, inclusive contador/atividade | 409 SIM_ENVIO_ABERTO_SEM_PROCESSAMENTO |
| Descarte sem ciclo ativo | Limpeza habitual e remoção da marca pendente na mesma transação; plano consumido e incidente permanecem históricos | Pode reenviar na mesma chave |
| Cancelamento integral | Limpeza habitual e fechamento da chave; plano fica histórico/inoperante | Nova chave independente |
| Falha de statement/COMMIT | Reverte lote/valores/atividade/eventos/contador/marca da unidade | 500 sanitizado; nenhuma interrupção simulada |

Recepção e seleção são serializadas pela transação SQLite IMMEDIATE, inclusive
entre duas instâncias dos serviços. O controle é indexado por remessa/assunto/
geração, contém cenário/UG e FK para o envio. Reinício conserva contador, consumo
e marca; geração divergente não aplica plano. Mera presença de RECEBIDO não
bloqueia múltiplos lotes normais; hash não deduplica.

## Alteração mínima e fronteiras

- V12 aditiva cria somente controles de recepção; V1–V11 intactas.
- `Remessas`, `RemessaService` e `RemessaRepository` concentram a seleção e a
  configuração; JDBC implementa persistência. Modelo Java puro representa plano
  e interrupção selecionada; `receive` conserva a assinatura para os chamadores.
  A interrupção selecionada é emitida somente depois de a transação retornar.
- `RemessaController` traduz a interrupção para atributo interno da requisição
  e não escreve resposta. Valve do Tomcat instalado encerra a conexão antes do
  commit HTTP; Tomcat permanece restrito ao adaptador/composição. Sem biblioteca,
  reflexão ou confirmação artificial. Prova será por servidor/socket reais.
- Descarte JDBC limpa a marca do mesmo contexto na transação já existente.
  Cancelamento não precisa apagar o controle, pois a chave fechada já é inelegível.
- Administração recebe controller próprio e validação estrita no padrão local.
- Testes: novo transporte real sem retry e JDBC real; ajustar apenas a expectativa
  de versão do schema em testes antigos quando a V12 tornar isso necessário.
- README, cobertura, evidência e rascunho de PR registram contrato, comandos e
  resultados efetivos. Cliente de smoke reutilizável usa somente dados fictícios.

## Verificação planejada

Inspecionados `pom.xml`, wrapper Maven 3.9.9 e padrões JUnit/Spring existentes:
build/testes usam SQLite temporário, sem TCE ou dados do integrador.
Executar RED/GREEN focal da #14, depois `./mvnw verify`; supervisão executará
`clean verify` e smoke Docker sobre o jar final, sem Maven concorrente.
Testar dois limites de transporte, alvo relativo, inválidos sem consumo,
reinício, rollback statement/COMMIT, duas recepções concorrentes, isolamento,
recuperação/finalização e preservação de bytes/multiplicidades/decimais.

Fora: demora/HTTP500 adicionais, reset #17, bloqueio #15, expiração #16,
regras fiscais, dados originais e aceite do integrador real.
