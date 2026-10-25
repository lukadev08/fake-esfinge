# PR da issue #1

Situação: publicado em 24/10/2026 após autorização expressa do solicitante.
PR: https://github.com/lukadev08/fake-esfinge/pull/28 — integrado em `main`.
Merge confirmado: `a36c7bf87e039170a81f642b0da4cee51237005c`; issue #1 fechada.
Destino: `lukadev08/fake-esfinge`; base `main`; head `issue-1-autenticacao`.
Commit de implementação: `c7b2bd5`. Head inicial publicado: `aff307b`.

## Título

Implementa autenticação fictícia com persistência SQLite

## Corpo exato proposto

Entrega o primeiro caminho executável do simulador 2026/V6/6.06: login com
usuário/UG fictícios, token vinculado ao cenário com expiração controlável e
inspeção administrativa protegida por chave própria. Credenciais, contexto e
sessões persistem em SQLite com migração versionada, FK por conexão e reabertura
sem recriar dados incompatíveis.

Inclui build Maven/Java 21/Spring Boot 3.5.10, testes de dependências de classes,
imagem mínima Linux amd64 fixada por digest, guia e manifesto de cobertura.
Operações futuras conhecidas respondem cobertura ausente; esta fatia não recebe
lotes nem processa remessas e não encerra E1/T01.

Validação local: `clean verify` com 30 testes, zero falhas/erros/ignorados;
93 verificações HTTP e de reinício na imagem final, sem rede externa e com usuário
sem privilégios. SQLite real, separação administrativa, expiração e erros
sanitizados exercitados. Revisão independente do grupo 1 aprovada sem achados
materiais. CI remoto pertence à etapa posterior; integrador real não executado.

Closes #1

## Limite de autorização

O solicitante autorizou push da branch acima para `origin` e criação deste PR em
`lukadev08/fake-esfinge`, base `main`, ao responder “autorizo a publicação,
continue a meta”. Merge, implantação e publicação de imagem não estão incluídos
na autorização solicitada. A próxima issue é #2, dependente da #1.

## Aprovação formal preparada

Revisão técnica independente do grupo 1 aprovada para a issue #1, sem achados
materiais. O conteúdo publicado foi conferido contra o código revisado: alterações
posteriores somente documentais. Supervisão confirmou clean verify com 30 testes
sem falhas/erros/ignorados e 93 verificações HTTP/reinício no Docker Linux amd64
sem rede externa. Integrador real, aceite E1/T01 e CI remoto não estão comprovados
nesta fatia.

A primeira submissão `APPROVE` foi rejeitada pela revisão automática de autorização.
O solicitante então autorizou expressamente aprovação e merge. A segunda tentativa
chegou ao GitHub, que retornou 422: `Review Can not approve your own pull request`.
Nenhum review foi publicado; a aprovação técnica independente está registrada.
O merge autorizado foi aceito pelo GitHub, conferido no PR e na issue #1.
