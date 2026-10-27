# Rascunho local — issue #9

Situação: implementação técnica local revisada e validada; publicação não autorizada.
Destino: `lukadev08/fake-esfinge`; base `main`; head `issue-9-processamento-assuntos`.
Base documental `1fc0128`, funcional `53814c6`; remoto consultado inicialmente
`ddf8487`. Nenhum push/PR/merge da #9 realizado.

## Título

Processa ciclos estruturais em modos manual e automático

## Corpo exato proposto

Implementa o disparo explícito e os estados persistidos RECEBIDO →
EM_PROCESSAMENTO → PROCESSADO_SUCESSO para os seis assuntos. O disparo captura os
lotes elegíveis da chave; lotes posteriores aguardam novo ciclo. Consulta não
executa trabalhos. Inspeção distingue originais, registros processados e efetivos,
preservando multiplicidade, referências, zeros e decimais exatos.

Uma porta atende avanço administrativo e scheduler automático. Configuração,
latência e relógio compartilhado persistem; transições/evidências/efeitos são
atômicos, com guardas de estado/geração e retomada sem reaplicação. A V8 aditiva
preserva dados V7 e migrações anteriores. Limites técnicos temporais são validados
antes dos efeitos e em cada leitura do relógio, inclusive após reinício em system.

Validação: revisão independente aprovada após correções; `clean verify` sob
supervisão com **137 testes**, zero falhas/erros/ignorados; **1.094 verificações na
imagem final V8 Linux amd64 sem rede**, incluindo upgrade real V7, reinício,
10.001 créditos, precisão/origem dos dados e concorrência manual/automático.
As 1.879 verificações de preparação V7 e 3 do snapshot anterior são separadas.

Continuidade técnica autorizada pelo planejamento §13.1 com a #8/E1 ainda pendente
do solicitante. Não altera esse aceite nem sua dependência remota. T01/E3, CI,
finalização, recuperação/reset/expiração e regras fiscais continuam pendentes;
processamento estrutural não significa homologação oficial.

Closes #9
Refs #8

## Limite de publicação

Aguardando autorização específica para push desta branch, criação do PR contra
`main` e merge em `lukadev08/fake-esfinge`, conforme `AGENTS.md`. Implantação e
publicação de imagem fora do pedido. Após integração da #9, preparar a #10 dentro
da sequência autorizada e das dependências restantes; #8 permanece pendente.

Containers do ensaio removidos. Preservados volumes fictícios no contexto `orbstack`:
`fake-esfinge-issue9-supervisao`, `fake-esfinge-issue9-supervisao-final` e
`fake-esfinge-issue9-supervisao-auto`. Exclusão somente após autorização específica;
o terceiro contém ensaio deliberado de horizonte temporal esgotado.
