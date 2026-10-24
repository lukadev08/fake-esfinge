# PR preparado — issue #1

Situação: rascunho local; push bloqueado por revisão automática de autorização.
Destino: `lukadev08/fake-esfinge`; base `main`; head `issue-1-autenticacao`.
Commit de implementação: `c7b2bd5`. Nenhum PR criado.

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

Autorizar somente push da branch acima para `origin` e criação deste PR em
`lukadev08/fake-esfinge`, base `main`. Merge, implantação e publicação de imagem
não estão incluídos nesta solicitação. A próxima issue é #2, dependente da #1.
