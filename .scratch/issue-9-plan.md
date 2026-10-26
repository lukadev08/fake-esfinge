# Preparação e transições — issue #9

Referência: [issue #9](https://github.com/lukadev08/fake-esfinge/issues/9).
Branch `issue-9-processamento-assuntos`, base documental `abad882`, código funcional
`53814c6`, após o merge documental `ddf8487`. Explorer `grupo9_explorer`,
identificado GPT-6 Luna/médio, concluiu exploração somente leitura.

Desenvolvimento técnico autorizado pela seção 13.1 do planejamento, mantendo
#8/E1 pendente. O bloqueio remoto #9→#8 permanece registrado no GitHub; não impede
esta execução técnica autorizada. Publicação/merge da #9 não estão autorizados.

## Contrato e alcance do disparo

- `POST /v6/tributario/iniciar-processamento`, somente query `chavePacote`, corpo
  vazio e `auth_token`. Resposta exata: `situacao=RECEBIDO`, `dataEnvio` e
  `identificadorRemessaOnline`; consulta mantém `identificadorRemessa`.
- Base da seleção: fonte incorporada `envio-assincrono-v6.md`, linhas70–73,
  descreve os lotes enviados anteriormente para a mesma chave. O disparo captura
  **todos os lotes RECEBIDO ainda não agendados** da chave naquele instante.
  Não escolhe assunto por catálogo/ordem fiscal e não acrescenta parâmetro.
  No fluxo relatado, o cliente envia um assunto por ciclo; os seis assuntos
  permanecem em lotes separados e podem repetir ciclos sob a mesma chave.
- Receber depois do disparo conserva RECEBIDO sem agendamento: esses lotes
  aguardam novo disparo. Não criar rejeição de recepção por processamento ativo.
  Disparo ativo da chave, repetido sem novos elegíveis ou vazio retorna409 sem efeitos.
- Estas decisões explicitam o simulador derivado, sem equivalência oficial.
  Finalização #10, erros/recuperação/descarte/cancelamento, reset, expiração72h,
  regras fiscais e painel continuam fora da #9.

## Tabelas de transição anteriores à implementação

| Operação / pré-condição | Trabalho e estado persistidos | Resultado observável |
| --- | --- | --- |
| Receber lote válido | Lote RECEBIDO, sem trabalho; recebidos conservados | 200; processados/efetivos0 |
| Disparar chave autorizada, sem trabalho ativo, com elegíveis | Snapshot dos elegíveis; trabalho por lote com ordem/instante/geração e latência capturados; evento/atividade atômicos | 200 no envelope de disparo; lotes ainda RECEBIDO |
| Disparar ativo ou sem elegíveis | Nenhuma alteração | 409 sanitizado |
| Executar etapa inicial de trabalho elegível | RECEBIDO→EM_PROCESSAMENTO; trabalho passa à etapa de conclusão com instante devido; evento na mesma transação | Consulta200 EM_PROCESSAMENTO; sem efeitos efetivos antecipados |
| Concluir trabalho estruturalmente válido | Revalidar chave aberta, lote/geração/etapa; persistir registros processados e efetivos e concluir trabalho/lote/evento atomicamente | PROCESSADO_SUCESSO; contagens derivadas das linhas persistidas |
| Consultar / inspecionar em qualquer etapa | Somente leitura do progresso; consulta compatível conserva evento/atividade existentes | Nenhuma tarefa ou relógio avançado |
| Receber durante ciclo | Novo RECEBIDO fora do snapshot ativo | Só futuro disparo o agenda |
| Falhar antes de commit de qualquer etapa | Rollback integral; trabalho anterior continua retomável | Sem sucesso parcial; erro500 sanitizado no HTTP |
| Reiniciar antes/depois de commit | Retomar a etapa persistida; trabalho concluído não é reaplicado | Bytes/vínculos/estado/resultados preservados |

## Desenho de supervisão

1. Manter a capacidade no módulo `remessas`, com caso de uso/porta de processamento
   e adaptadores HTTP/admin/JDBC/acionamento automático. Evitar ciclos com
   autenticação/cenários. Núcleo Java puro; scheduler somente aciona a porta.
2. Uma implementação de avanço atende administração manual e tick automático;
   serializar acionamentos na instância. Transações SQLite IMMEDIATE e guardas
   de estado/geração na gravação protegem também retomada/concorrência.
3. Instantes e sequência persistidos definem ordenação. Separar início e conclusão
   para permitir observar EM_PROCESSAMENTO, inclusive com latência zero usando
   um passo manual. Pode reservar instantes consecutivos por lote no snapshot;
   não manter transação aberta durante latência ou espera física.
4. Configuração de processamento persistida por cenário: modo MANUAL por padrão,
   latência1000ms por padrão, decisão própria do simulador. Troca de modo/latência
   não reescreve trabalhos já agendados; afeta novos disparos/acionamentos.
5. Administração protegida por `X-Simulator-Key`, sem parâmetros do integrador:
   GET/PUT `/__simulator/processamento` para modo/latência e
   POST `/__simulator/processamento/avancar` com `milissegundos` não negativos e
   `passos` inteiro positivo. Validar JSON estrito, tipos/extras e overflow antes
   de efeitos. Limitar etapas executadas ao comando; responder com instante e
   quantidade executada, sem payloads/tokens. Manual pode avançar tarefas mesmo
   no modo automático, pela mesma serialização; tick só atua em AUTOMATICO.
6. O avanço manual de tempo necessário à #9 deve usar o **mesmo Clock de negócio**
   já consumido por autenticação/remessas, com avanço persistido por cenário,
   preservando `system`/`fixed`. Um adaptador Clock pode usar instante fixo ou
   deslocamento persistido sobre UTC; leitura não avança nada. Testes manuais
   usam `fixed`, sem sleeps. Não implementar expiração72h ou controles genéricos
   da #16: apenas o mínimo necessário ao avanço de processamento nesta rota.
7. Dados: originais e valores recebidos permanecem intactos. O processamento
   estrutural revalida os bytes pelo validador existente via adaptador/porta,
   sem Jackson no núcleo; evitar segunda implementação de schemas. Persistir
   dados por registro processado e efetivo com índice/vínculos de origem,
   preservando multiplicidade/referências/decimal exato. Não gerar contagens
   apenas copiando `lote.registros` nem deduplicar por chave/hash.
8. Inspeção administrativa mostra estado e conjuntos recebidos/processados/efetivos
   distinguíveis; contagens vêm dos conjuntos persistidos. Consulta compatível
   continua no schema existente, sem campos administrativos ou erro de negócio inventado.
9. V8 aditiva, V1–V7/fontes/checksums intactos. Preservar lotes/IDs/bytes/hash,
   valores nomeados, sessões/eventos/FKs/high-water. Dados V7 começam RECEBIDO
   sem trabalhos ou efeitos. Novas tabelas têm vínculos compostos e unicidade
   por lote/índice, sem conversão monetária em REAL. Incompatibilidade/falha de
   migração reverte, sem recriar dados. Guardas de geração não implementam reset.

## Fatias TDD e validação

Seams já confirmados no pacote E0: HTTP compatível, administração separada,
núcleo/SQLite real e integrador real/esperado independente (este último pendente).
Manter os três primeiros nesta tarefa; simulador fictício não encerra T01/E1.

- Primeiro RED→GREEN HTTP: receber lote, disparar, consultar RECEBIDO, avançar
  início manual, consultar EM_PROCESSAMENTO sem avanço, avançar tempo/conclusão,
  consultar PROCESSADO_SUCESSO e inspecionar registros/contagens exatos.
- Próximas fatias: seis assuntos na mesma chave em ciclos, múltiplos lotes e
  duplicatas; snapshot estável/novos recebidos; autenticação/UG/cenário/corpo
  sem campos de teste; 409 ativo/sem elegíveis/repetido e rejeições sem efeitos.
- Modo/latência/tempo persistidos e leitura sem avanço; ordem por instante/seq,
  latênciazero/positiva, modo automático pela mesma porta, concorrência entre
  manual/auto e entre disparo/recepção sem duplicar trabalho ou efeitos.
- SQLite real: migração V7 preenchida, checksums/FKs/sequências, falha de migração,
  rollback de statement/COMMIT em disparo/início/conclusão e reinício em ambas
  as etapas. Preservar dinheiro e valores de saldo de quatro campos.
- Executor próprio em TDD por fatia; revisão independente/correções delegadas.
  Supervisor executa clean verify e Docker Linux amd64 sem rede, upgrade V7 e
  reinício, incluindo HTTP real e leitura da persistência. Sem publicação prévia.

## Comandos inspecionados

`pom.xml`, wrapper e Dockerfile existentes: Java21/SpringBoot3.5.10/Maven3.9.9;
sem novas dependências necessárias. Maven só usa dependências públicas; testes
SQLite locais e credenciais fictícias, sem TCE/produção. Exemplo focalizado:

```sh
rtk proxy env JAVA_HOME=/Users/luka/.sdkman/candidates/java/current \
  MAVEN_USER_HOME=/private/tmp/fake-esfinge-wrapper-check-fixed \
  ./mvnw -B -ntp -Dmaven.repo.local=/private/tmp/fake-esfinge-m2 -Dtest=... test
```

Depois, uma execução completa `verify` pelo executor; supervisor `clean verify`.
Cache fora do workspace pode exigir escalada. Preservar exit codes/diagnósticos e
handles; nenhum build duplicado enquanto outro estiver pendente.
