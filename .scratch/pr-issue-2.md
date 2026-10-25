# PR preparado — issue #2

Situação: publicado e integrado com autorização do usuário; PR [#29](https://github.com/lukadev08/fake-esfinge/pull/29) merged em `main`, commit `4b61304`.
Implementação revisada e validada no commit `02e036f`.
Destino: `lukadev08/fake-esfinge`; base `main`; head `issue-2-recepcao-contribuintes`.
Base de desenvolvimento: `a36c7bf`; inclui registro documental de conclusão da #1.

## Título

Abre envios e conserva lotes de contribuintes sem processar

## Corpo exato proposto

Permite abrir um envio por UG/competência do perfil 2026/V6/6.06, receber vários
lotes de contribuintes e consultá-los em RECEBIDO. A lista vai diretamente na
raiz do JSON; a validação estrutural preserva tipos, nulos, zeros e bytes originais.
Não há deduplicação por hash nem limite artificial de 10.000 registros.

SQLite conserva bytes, hash, ordem, vínculos, contagens e eventos na mesma transação
antes da confirmação. Uma restrição real impede aberturas concorrentes; a migração
V2 preserva bancos V1 e sessões. A inspeção administrativa exige chave própria e
permite conferir conteúdo e contagens sem transformar recebidos em dados efetivos.

Validação: `clean verify` com **51 testes**, zero falhas/erros/ignorados; **178
verificações independentes** na imagem Linux amd64 sem rede externa, incluindo
concorrência, 10.001 registros, bytes exatos e reinício. Revisão independente do
grupo2 aprovada sem achados materiais. Falhas reais de statement/COMMIT,
contenção, isolamento e upgrade foram exercitados em SQLite temporário.

Outros assuntos, processamento, finalização, reset, CI remoto e aceite humano
E1/T01 permanecem pendentes. Esta mudança não comprova homologação oficial.

Closes #2

## Situação das ações externas

Push da branch acima para `origin` e criação deste PR contra `main` autorizados
e executados. Merge em `lukadev08/fake-esfinge` autorizado e executado em 24/10/2026.
Implantação e publicação de imagem não estão incluídas. A aprovação técnica está registrada; a conta GitHub não
permite submeter aprovação formal em PR da própria conta, conforme verificado na #1.
