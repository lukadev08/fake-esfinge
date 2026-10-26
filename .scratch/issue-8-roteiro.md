# Roteiro local de aceite E1 — issue #8

**Situação: PENDENTE DE EXECUÇÃO E ACEITE HUMANO.** O solicitante confirmou que
executará no ambiente interno controlado. Este arquivo contém somente o roteiro;
não recebe payloads, capturas, resultados ou registros internos do ensaio.

Referência: [issue #8](https://github.com/lukadev08/fake-esfinge/issues/8),
[planejamento, etapa E1](../docs/planejamento-simulador-tributario.md),
[contrato E0](../docs/contrato-e0.md) e [comandos existentes](../README.md).
O objetivo é comprovar recepção de payload efetivamente produzido e enviado pelo integrador
e conservação após reabrir a instância. O fluxo completo e o esperado independente
de E3/T01 permanecem separados; E2 aguarda a saída E1 aprovada expressamente.

## 1. Fixar o artefato e preparar a execução interna

- Código funcional: `53814c6`, contrato `2026/V6/6.06`, schema SQLite V7.
- Referência verificada pela supervisão neste Mac: imagem `fake-esfinge:issue-7-local`,
  Linux amd64, ID `sha256:bcbc05ff4d10596e4b651e16adef205e6e4c7696ed0108d61f98a90f48a13c99`;
  jar SHA-256 `0d54ec70afea3c1f7f2a87db0e172683795000913e4c4668576643b17c5d5957`.
  Isso não comprova disponibilidade ou execução no ambiente da empresa.
- No ambiente interno, obtenha o artefato pelo procedimento permitido. Se construir
  novamente, use a revisão acima e o procedimento do README, confira os 105 testes
  e registre os hashes efetivos dessa construção internamente; não presuma hashes iguais.
- Use instância e volume exclusivos, com nomes inéditos. Não reutilize os volumes
  dos ensaios fictícios #6/#7, já excluídos. Defina antes da primeira inicialização
  cenário e credenciais fictícias, incluindo a UG selecionada para o ensaio.
  O alias do cliente não determina o código de UG. Configuração e exemplos estão
  nas seções “Configuração local” e “Docker mínimo Linux amd64” do README.
- Para o integrador executado diretamente no mesmo host que publica a porta do
  Docker, use `127.0.0.1:8080:8080`, como no README. Em outras topologias, incluindo
  integrador em outro container ou host, resolva o acesso interno antes do ensaio;
  não suponha que o loopback seja alcançável pelo cliente. Não use `--network none`
  para esse ensaio HTTP.
- Guarde payloads originais, configuração local, tokens, inspeções e evidências
  detalhadas somente no ambiente interno permitido, fora deste repositório e de
  serviços externos. Não habilite logs de requisição/SQL sobre dados originais.

## 2. Receber o payload real sem exigir processamento

1. O solicitante configura o integrador real para a URL interna do simulador e
   credenciais fictícias. Versão e configuração dependem do integrador, não
   inspecionado pelo agente. Resolva esses pontos internamente; este aceite não
   exige nova instrumentação para captura do corpo nem uma opção de pausa.
2. Execute a recepção pelo integrador. O contrato existente usa login, abertura
   `GET /v6/tributario/iniciarEnvio` com `anoMes` selecionado e
   `cancelamentoAutomatico=NAO`, seguida de
   `POST /v6/tributario/enviarParcialLote?chavePacote=...` com uma lista por assunto.
   Exemplos `curl` fictícios do README servem à orientação da API; não substituem
   o payload produzido e enviado pelo integrador para este aceite.
3. Preserve internamente os retornos e referências da execução pelo integrador
   real que comprovem a origem do envio e permitam localizar cada lote.
   Recepção bem-sucedida retorna HTTP200,
   `situacao=RECEBIDO`, `numeroLote`, `identificadorRemessaOnline` e `dataEnvio`.
4. Consulte `GET /v6/tributario/consultarPorNumeroLote/{numeroLote}` com `auth_token`.
   Espere HTTP200 e `situacao=RECEBIDO`; a consulta usa `identificadorRemessa`.
   Pelo controle separado, consulte `GET /__simulator/lotes/{numeroLote}` com
   `X-Simulator-Key`, não com o token compatível.
5. Guarde a inspeção administrativa interna como referência anterior à reabertura.
   Decodifique `bytesBase64` e preserve esses bytes sem reformatar JSON, para
   compará-los byte a byte com a inspeção posterior. Confira também o `hash`, assunto,
   chave, lote, remessa, UG, competência, referências, multiplicidade e quantidade
   recebida. `processados` e `efetivos` devem ser zero. Guarde a referência dessa
   conferência internamente, sem copiar os dados para este arquivo. Se o corpo
   efetivamente enviado já estiver disponível internamente, compare-o também;
   essa comparação adicional não exige nova instrumentação nem o esperado de E3.

Disparo e finalização ainda retornam HTTP422 `SIM_COBERTURA_AUSENTE` com token
válido. Essa limitação conhecida não exige implementar E2 para executar a #8 e
não comprova sucesso do fluxo completo do integrador. A tentativa rejeitada não
inicia processamento nem altera os lotes recebidos: consulte e inspecione esses
lotes mesmo após o HTTP422, com autenticação válida. Não é necessário pausar o
integrador antes dessa resposta; não substitua o envio real por uma simulação.

## 3. Reabrir a mesma instância e conferir novamente

1. Reinicie somente o container exclusivo do ensaio pelo procedimento Docker do
   README (`docker restart`), mantendo **a mesma imagem, configuração e volume**.
   Não recrie a base, não faça reset e não exclua o volume durante a comprovação.
2. Consulte os mesmos lotes pela API compatível e pela inspeção administrativa.
   Se o token tiver expirado — padrão de 3600 segundos — faça novo login para a
   mesma identidade/UG; a expiração não deve ser confundida com perda do lote.
3. Compare a evidência interna anterior e posterior: bytes, hash, assunto,
   chave/lote/remessa/UG/competência, referências, registros e contagens permanecem
   iguais. Consulte novamente e confirme `RECEBIDO`, sem processamento ou efetivação.
   Eventos de consulta podem aumentar; não exija igualdade de toda a lista de eventos.
4. Reconcilie qualquer divergência antes de concluir. Preserve o volume e os
   detalhes internos conforme a política da empresa; este roteiro não autoriza
   sua exclusão nem publicação de evidências.

## 4. Critérios e decisão do solicitante

| Critério a conferir pelo solicitante | Situação atual |
| --- | --- |
| Payload efetivamente produzido pelo integrador recebido no ambiente controlado | PENDENTE |
| Bytes, referências, vínculos e contagens conferidos antes/depois da reabertura | PENDENTE |
| Consultas mantêm RECEBIDO, processados/efetivos zero | PENDENTE |
| Divergências reconciliadas e assuntos ausentes/limitações registrados internamente | PENDENTE |
| Versão do artefato/contrato identificada e saída E1 aprovada expressamente | PENDENTE |

Depois do ensaio, compartilhe somente a conclusão sanitizada, quando permitida
pela empresa, com este conteúdo mínimo:

- Artefato/código e perfil de contrato usados: **a preencher após o ensaio**.
- Etapa avaliada: **recepção e reabertura E1**.
- Critérios acima: **a preencher com conforme, divergente ou não verificado**.
- Assuntos ausentes e limitações: **descrição genérica, sem dados do cliente**.
- Divergências: **conclusão sanitizada ou indicação de pendência**.
- Decisão expressa do solicitante sobre a saída E1: **pendente após a conferência**.

Não inclua payloads, SQL, identificadores/chaves/UGs, contagens reais, senhas,
tokens ou hashes de conteúdo real na conclusão compartilhada. Detalhes permanecem
internos. Assuntos ausentes não recebem cobertura automática; o cenário
complementar continua pendente quando necessário. A #8 permanece aberta até a
execução/conferência e decisão expressa; seu aceite não encerra T01/E3 integral,
validação fiscal ou homologação oficial.

## Verificação deste roteiro

Documento preparado por leitura do README, configuração e controllers atuais.
Rotas, headers, nomes de resposta e limites foram conferidos estaticamente.
Nenhum comando operacional, build, teste, integrador ou ensaio com payload real
foi executado para preparar este arquivo. As situações acima permanecem pendentes.
