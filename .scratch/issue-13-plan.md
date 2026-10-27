# Preparação e decisões — issue #13

Referência: [issue #13](https://github.com/lukadev08/fake-esfinge/issues/13), aberta
`ready-for-agent`, sem comentários; dependências #10/#12 concluídas. Branch
`issue-13-cancelamento-envio`, base funcional `f2d7e58` (PR #39), registro `15da126`.
E2 técnico autorizado pelo planejamento §13.1; #8/E1 permanece pendente.
Publicação/merge da #13 não autorizados.

Explorer próprio `grupo13_explorer`, configurado GPT-6 Luna/médio, concluiu
somente leitura, sem inferir variante efetiva indisponível. Supervisor leu
planejamento, arquitetura, contrato E0, schema/fonte incorporados e chamadores.

## Contrato e decisões anteriores ao código

- POST `/servicosGerais/cancelarEnvioParcial`, `auth_token`, query exata
  `chavePacote`, corpo vazio; 200 com corpo exatamente vazio, resposta derivada
  de E0. Parâmetro ausente/vazio/duplicado/extra ou corpo não vazio: 400.
  Token inválido/expirado: 401 antes de efeitos; chave desconhecida/de outra
  UG/cenário: 404. Cancelamento repetido ou chave finalizada/cancelada: 409 sem efeitos.
- Cancelamento é integral do envio aberto, inclusive RECEBIDO sem trabalho,
  agendado e EM_PROCESSAMENTO, em todos os seis assuntos. Não reutilizar a guarda
  de descarte de assunto que impede ciclos ativos: #13 exige invalidar trabalhos
  já iniciados e rejeitar resultados tardios. Não cancelar remessa concluída.
- Mesma transação IMMEDIATE fecha a chave, remove efetivos→processados→valores
  recebidos de todos os lotes ativos do envio e os inativa. Nenhum dado ativo
  residual; bytes/hash/quantidade original, controles, planos e eventos ficam
  históricos. Outras remessas/UGs/cenários e remessas anteriores não mudam.
- PROCESSADO_SUCESSO permanece sucesso histórico sem diagnóstico inventado,
  com evento de dados removidos. RECEBIDO sem trabalho ou agendado e EM tornam-se
  ABORTADO, diagnóstico próprio `SIM_ENVIO_CANCELADO`, mensagem fixa sanitizada
  `Envio cancelado no simulador`, registro null e evento terminal. Não usar
  ABORTADO_ERRO_LOTE_ANTERIOR ou LOTE_DESCARTADO para cancelamento. Históricos
  previamente inativos conservam todos os estados/diagnósticos/eventos.
- ABORTADO está no vocabulário/schema derivado, mas sua aplicação ao cancelamento
  e o diagnóstico são decisões do simulador, sem equivalência oficial comprovada.
  Lote vazio não é criado; envio aberto sem lotes também pode ser cancelado.
- Registrar `ENVIO_CANCELADO` uma única vez, atividade no instante do cancelamento
  e marcador `cancelada_em` distinto de `finalizada_em`. Não marcar cancelamento
  como conclusão de dados; manter timestamps/semântica de finalização intactos.
- Nova abertura para a mesma UG/competência retorna chave distinta e permite
  reenvio integral com IDs/ordem próprios, sem deduplicação por hash nem plano
  herdado. Chave antiga nunca reabre; consulta histórica autorizada continua200,
  sem renovar atividade/eventos ou avançar processamento.
- Não incrementar geração global do cenário: isso invalidaria trabalhos de
  outros envios. Revalidar chave aberta/cancelada, ativo, estado e geração na
  transação de begin/complete/fail; trabalho capturado antes do cancelamento
  retorna false e não escreve após o commit. Reinício conserva o cancelamento.

## Tabela de transições

| Pré-condição / ação | Resultado e efeitos |
| --- | --- |
| Aberta sem lotes | 200 vazio, fecha por cancelamento, evento próprio, nenhum lote artificial |
| Aberta com recebidos/sucessos/agendados/EM | 200 vazio, remove todo dado ativo e inativa lotes; pendentes ABORTADO, sucessos preservados |
| Históricos já descartados/abortados/inativos | Permanecem sem reescrita, controles/planos/bytes preservados |
| Trabalho termina antes do cancelamento | Pode concluir; cancelamento posterior remove seus efeitos e preserva sucesso histórico |
| Cancelamento vence trabalho já capturado | begin/complete/fail tardios não escrevem; nenhuma linha ou evento reativado |
| Cancelar repetido / receber / disparar / descartar / finalizar chave cancelada | 409, sem dados, atividade ou evento novos |
| Consultar lote histórico de chave cancelada | 200, estado/diagnóstico histórico; sem renovação nem avanço |
| Cancelar chave finalizada | 409, remessa concluída e todos os seus dados intactos |
| Nova abertura mesma UG/competência | Chave distinta; reenvio integral independente dos IDs/planos antigos |
| Auth inválida / chave isolada / transporte inválido | 401 /404 /400 antes de efeitos |
| Falha statement/COMMIT após fechamento/remoção/terminal/evento | Rollback integral e500 sanitizado; repetir depois funciona |

## Menor implementação e migração

1. `Processamento`/`ProcessingService` coordenam cancelamento, pois já possuem
   portas de remessa, trabalhos, relógio e transação. Acrescentar `cancel(token,key)`
   sem ciclo em RemessaService. Rota pode ficar no ProcessingController existente,
   ao lado do descarte; remover apenas cancelamento do controlador indisponível.
   Manter resposta void e mapeamento na fronteira; nenhuma nova configuração/DTO.
2. `Remessa` ganha instante de cancelamento e `requireOpen()` verifica os dois
   encerramentos. JdbcRemessaRepository mapeia o marcador e inclui condição em
   receber/finalizar/touch; consultas históricas permanecem. Auditar cada chamador
   e guardas de agendamento/next/current/planos, incluindo duas instâncias SQLite.
3. ProcessingRepository/JDBC cancelam dentro da transação, com UPDATE condicional
   de remessa/UG/cenário ainda abertos. Reusar `removeData`/terminal da #11/#12;
   não introduzir framework de ciclo de vida ou abstração genérica. Planos antigos
   permanecem consultáveis; mutações administrativas de lote cancelado dão409.
4. V11 adiciona `cancelada_em TEXT` em remessas e atualiza índice de unicidade de
   envio aberto para ambos os encerramentos nulos. Coluna legada `estado` não é
   ampliada: seu CHECK só admite ABERTO; abertura real depende dos marcadores.
   Evitar reconstruir remessas e todas as suas FKs apenas para mudar um enum interno.
5. V11 reconstrói somente processamento_trabalhos para admitir ABORTADO, copiando
   **todas** as colunas antigas, inclusive diagnóstico/nulls, e conservando IDs,
   índices, sequências/high-water mesmo acima de MAX(id), FKs e planos. Registrar
   script em SqliteDatabase; V1–V10/checksums/fontes/schemas originais intactos.
   Sem tabela/índice duplicado, detecção dinâmica de versão em produção ou dependência.
6. Adaptar fixtures antigas ao novo record/versão somente onde necessário; testes
   de upgrade continuam preparando versões antigas reais. Não transformar V10
   artificialmente em V11 para evitar prova da migração.

## Fatias TDD e supervisão

- RED→GREEN HTTP vertical: receber/disparar→cancelar enquanto pendente→consultar
  ABORTADO/histórico→abrir nova chave mesma competência→reenviar/processar/finalizar.
- Seis assuntos/múltiplos lotes, dinheiro decimal exato e identificadores com zeros;
  cancelar tudo elimina ativos, preserva originais/diagnósticos antigos/sucessos,
  mantém outras chaves/UGs/cenários/concluídos e permite cancelar envio sem lotes.
- Query/corpo/header estritos,200 vazio, auth expirada/renovação,404 isolado,
  409 repetição/chave concluída/todas as mutações na cancelada; consulta histórica
  não renova atividade nem eventos e não conduz trabalho.
- Rollback real statement e COMMIT após fechamento/exclusões/terminal/evento;
  500 sanitizado, sem senha/token/chave/SQL/stack trace no corpo ou logs.
- Corridas entre cancelamento e recepção/disparo/última conclusão/finalização/
  outra abertura/cancelamento por duas instâncias; resultado linearizável. Pausar
  cálculo fora da transação e cancelar antes do commit; begin/complete/fail antigos
  não gravam, inclusive após reinício. Sem incremento de geração de outros envios.
- Upgrade de V10 preenchida preserva16 tabelas e todas as colunas antigas,
  bytes/diagnósticos/planos/configuração/sessões/offset/seq/FKs; marcador novo nulo.
  Reiniciar V11 conserva cancelamento e controles, sem duplicar efeitos.
- Executor próprio → reviewer independente → correções por outro agente se
  necessárias; supervisor clean verify e imagem final Linux amd64 sem rede,
  upgrade V10 real, cancelamento agendado/EM, reinício e agendador automático.

Comandos existentes inspecionados: Java21/Maven Wrapper3.9.9, dependências públicas/
cache local, SQLite temporário e Docker orbstack em rede none com dados fictícios.
Focais `-Dtest=... test`, verify do executor e clean verify da supervisão, sem Maven
concorrente. RTK/proxy, exit codes e diagnósticos preservados. Cancelamento
automático/SIM, resposta perdida, reset, expiração, regras fiscais e UI fora da #13.
#8/E1, T01/E3 e homologação não comprovados; nenhum dado original será usado.
