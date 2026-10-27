# Rascunho local — issue #10

Situação: implementação técnica local revisada e validada; publicação não autorizada.
Destino: `lukadev08/fake-esfinge`; base `main`; head `issue-10-finalizacao`.
Base funcional `eb376d0` (PR #36/#9), plano anterior ao código `db0ee14`.
Inclui registro documental local da integração/limpeza autorizada da #9 (`f4e48b4`).

## Título

Finaliza envios com totalizadores dos registros efetivos

## Corpo exato proposto

Implementa `POST /v6/tributario/finalizarEnvio` com chave e 17 totalizadores no JSON
estrito. Compara os seis assuntos com linhas efetivas por remessa/UG/cenário,
conservando multiplicidade e precisão; os onze assuntos sem suporte exigem `"0"`.
Envio vazio, pendências ou divergências recebem 409 sem efeitos. Sucesso 200 vazio
fecha a chave e registra atividade/evento atomicamente.

A migração V9 acrescenta marcador de finalização e ajusta o índice parcial, sem
reconstruir tabelas ou alterar V1–V8. Mutações implementadas rejeitam chave fechada;
consulta histórica preserva conteúdo sem renovar atividade. Nova abertura na
mesma competência cria chave distinta, sem cota mensal e sem perder histórico.
Guardas do executor impedem efeitos tardios; falhas de statement/COMMIT revertem.

Validação: revisão independente aprovada; `clean verify` da supervisão com
**155 testes**, zero falhas/erros/ignorados; **1.279 verificações na imagem final
V9 Linux amd64 sem rede**, incluindo upgrade real V8, quinze tabelas preservadas,
reinício, 17 campos, 10.001 créditos, conteúdo exato e disputas de fechamento/abertura.
As 538 verificações de preparação V8 e 3 do snapshot anterior são separadas.

#8/E1 e T01/E3 permanecem pendentes. Recuperação futura integrará descartados e
abortados aos critérios de lote ativo; esses estados, cancelamento/reset/expiração,
sem movimento, T17, regras fiscais e CI remoto não foram implementados ou aceitos
por esta entrega. Continuidade técnica E2 segue o planejamento §13.1, sem homologação.

Closes #10
Refs #8

## Limite de publicação

Aguardando autorização específica para push de `issue-10-finalizacao`, criação do
PR contra `main` e merge em `lukadev08/fake-esfinge`. Publicação de imagem e implantação
fora do pedido. Depois da integração, próxima fatia técnica é #11, respeitando
suas dependências e o ciclo de supervisão; #8/E1 permanece pendente.

Container temporário removido. Volume fictício `fake-esfinge-issue10-supervisao`
preservado no contexto `orbstack`; exclusão exige autorização específica.
