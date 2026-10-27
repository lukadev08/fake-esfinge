# Rascunho local — issue #11

Situação: implementação técnica local revisada e validada; publicação não autorizada.
Destino: `lukadev08/fake-esfinge`; base `main`; head `issue-11-erro-assincrono`.
Base funcional `4de2ba5` (PR #37/#10), plano anterior ao código `a7beb04`.
Inclui registro documental da integração/limpeza autorizada da #10 (`1a371ed`).

## Título

Injeta erro assíncrono com descarte e aborto rastreáveis

## Corpo exato proposto

Adiciona plano administrativo determinístico por lote/registro, persistido por
geração e protegido por chave própria. Na conclusão, o alvo vira LOTE_DESCARTADO e
os seguintes não concluídos do mesmo assunto são abortados atomicamente, incluindo
os ainda não agendados. Anteriores válidos e outros assuntos permanecem.

Consulta mantém HTTP 200 com diagnóstico próprio e registro zero-based ou null.
Dados ativos são removidos; originais, bytes/hash e controles permanecem no arquivo
histórico. Inspeção distingue os conjuntos, e finalização conta somente ativos.
V10 conserva os dados V9, amplia terminais e protege contra efeitos tardios ou
reaplicação após reinício. DTOs HTTP têm mapeamentos separados do núcleo.

Validação: revisão independente aprovada após correção; `clean verify` da supervisão
com **186 testes**, zero falhas/erros/ignorados; **1.555 verificações na imagem final
V10 Linux amd64 sem rede**, incluindo upgrade V9 real, quinze tabelas preservadas,
reinícios, primeiro/intermediário/último, conteúdo exato e disputa manual/automático.
As 105 verificações de preparação V9 e 3 do snapshot anterior são separadas.

Injeção é aproximação de teste, sem validação fiscal T04 ou CON. #8/E1 e T01/E3
continuam pendentes. Recuperação por descarte/reenvio (#12), cancelamento/reset/
expiração, CI remoto e homologação não são concluídos por esta entrega.

Closes #11
Refs #8

## Limite de publicação

Aguardando autorização específica para push de `issue-11-erro-assincrono`, criação
do PR contra `main` e merge em `lukadev08/fake-esfinge`. Publicação de imagem e
implantação fora do pedido. Depois da integração, continuar #12 dentro de suas
dependências e supervisão; #8/E1 permanece pendente.

Containers temporários removidos. Volumes fictícios `fake-esfinge-issue11-supervisao`
e `fake-esfinge-issue11-supervisao-auto` preservados no contexto `orbstack`;
exclusão exige autorização específica.
