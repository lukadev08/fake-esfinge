# Rascunho local — issue #7

Situação: implementação/revisão/suíte e Docker V7 aprovados, sem push ou PR.
Destino: `lukadev08/fake-esfinge`; base `main`; head `issue-7-recepcao-prazo`.
Base funcional `ad27bd8`, registro inicial `27e7980`.

## Título

Recebe e inspeciona início do prazo prescricional

## Corpo exato proposto

Habilita `inicioPrazoPrescricional` nos fluxos de recepção, consulta RECEBIDO e
inspeção administrativa. Valida os quatro campos aprovados: ano inteiro, código
string e duas datas reais `YYYY-MM-DD`, preservando bytes/referências/contagens
sem coerção ou processamento. O schema continua derivado de exemplo, sem DTO
extraído; prescrição fiscal, cronologia e vínculo com crédito permanecem adiados.

Prazo usa conteúdo não monetário. A migração V7 admite o sexto assunto e conserva
os dados V6, valores nomeados, IDs, eventos, sessões, FKs e sequências, com rollback
em falha. V1–V6, fontes contratuais e dependências permanecem intactas.

Validação: revisão independente sem achados materiais; `clean verify` sob
supervisão com **105 testes**, zero falhas/erros/ignorados; **678 verificações na
imagem V7 Linux amd64 sem rede**, incluindo upgrade real de V6, hashes/contagens
de dez tabelas preservados, reinício, 10.001 prazos e dinheiro antigo exato.
As 1.350 verificações de preparação V6 são contabilizadas separadamente.

CI remoto, integrador real e aceite humano #8/E1/T01 continuam pendentes. Se prazo
faltar no piloto, cenário complementar continua necessário; sem homologação oficial.

Closes #7

## Limite de publicação

Rascunho local validado. Solicitar autorização específica para push de
`issue-7-recepcao-prazo`, criação do PR contra `main` e merge em
`lukadev08/fake-esfinge`. Implantação e publicação de imagem não estão incluídas.
Após integração, próximo ponto é #8/HITL; E2 permanece bloqueada pelo aceite humano.
Container temporário removido; exclusão do volume fictício
`fake-esfinge-issue7-supervisao` no contexto `orbstack` exige decisão específica.
