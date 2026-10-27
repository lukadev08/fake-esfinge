# Orientações para agentes

## Fonte de planejamento

Antes de implementar, revisar ou decompor trabalho, leia
[`docs/planejamento-simulador-tributario.md`](docs/planejamento-simulador-tributario.md).
Use esse Markdown como referência de desenvolvimento; os arquivos HTML, PDF e
DOCX são versões de apresentação do planejamento. Preserve a distinção entre
requisitos confirmados, propostas e decisões pendentes.

Antes de implementar ou revisar código, leia também
[`ARCHITECTURE.md`](ARCHITECTURE.md): stack adotada, arquitetura hexagonal,
fronteiras de dependência, persistência e estratégia de testes.

Consulte especialmente:

- Seções 3–5: fronteira, escopo e contrato HTTP.
- Seções 6–9: arquitetura adotada, invariantes, processamento e cobertura.
- Seções 10–14: cenários, validação, etapas e critérios de conclusão.
- Seção 15: situação das decisões D01–D12 e lacunas para implementação.

Ao definir operações, estados ou o piloto, consulte também
[`docs/contrato-e0.md`](docs/contrato-e0.md). Esse documento consolida o contrato
derivado e o fechamento documental de E0 no recorte inicial. Por revisão
autorizada em 23/10/2026, SQL e resultados ficam internos; o solicitante prepara
a referência e faz a conferência independente em E3. E0 define roteiro e critérios;
aceite ponta a ponta e fracionamento efetivo dependem dessa execução. Para schemas e fontes,
consulte o pacote ligado nesse contrato; use os ajustes do cliente sem alterar
as cópias originais dos insumos.

## Estado e sequência de desenvolvimento

Na inicialização deste guia, o repositório contém documentação, sem aplicação,
configuração de build, testes ou Docker. Verifique o estado atual antes de agir;
não apresente entregas planejadas como implementadas.

Siga as condições de saída de E0 → E1 → E2 → E3 → E4 da seção 13, dentro da
tarefa autorizada. E0 deve fechar o inventário do cliente e o contrato das
operações prioritárias antes da implementação compatível. Registre lacunas e
peça os insumos necessários quando afetarem o comportamento; prossiga apenas
com trabalho independente delas.
Para iniciar E2 com a #8 pendente, consulte a exceção autorizada na
[seção 13.1 do planejamento](docs/planejamento-simulador-tributario.md#131-pendência-e1-e-continuidade-de-e2).

Java 21 e Spring Boot 3.5.10 descrevem o integrador confirmado por relato do
usuário em 23/10/2026, sem inspeção independente do cliente. A stack adotada para
o simulador está em `ARCHITECTURE.md`; sua adoção documental não significa implementação.
Docker é requisito confirmado. Evite adicionar infraestrutura sem necessidade
demonstrada no escopo.

## Fronteira e contrato

- O simulador começa na entrada HTTP do TCE. Não acessa a origem nem reproduz
  a obtenção, transformação ou montagem de lotes do integrador.
- O recorte cobre os seis assuntos da seção 4.2, em lotes separados por assunto.
  O cliente relata vários assuntos sob a mesma `chavePacote`, com processamento
  por assunto e finalização ao término. O contrato derivado fixa uma UG/competência
  por envio e chave retornada pelo destino; comprovação independente é futura.
  A premissa de um assunto por remessa foi retirada.
  NFS-e, lotes com assuntos misturados e cancelamento de remessa concluída ficam
  fora do recorte.
- O fluxo relatado é abrir → enviar todos os lotes de um assunto → processar →
  consultar → repetir por assunto com a mesma chave → finalizar. Diferencie
  cancelamento integral do envio aberto e descarte de lote/assunto.
- Exercício 2026, endpoints V6 e leiaute 6.06 foram confirmados pelo usuário,
  sem validação independente do cliente. CONs 6.9 continuam referência proposta.
  Preserve o perfil anual; não misture contratos de exercícios.
- A lista de rotas do planejamento não é um OpenAPI validado. Fixe métodos,
  schemas, autenticação e respostas com evidências, origem, data e hash conforme
  a seção 5.2. Identifique contratos derivados e suas lacunas.
- Não invente códigos de CON, mensagens oficiais, enums externos, idempotência
  ou limites obrigatórios a partir de recomendações de dimensionamento.
  Registre aproximações no manifesto de cobertura.

## Invariantes de implementação

Preserve todos os invariantes da seção 7, em particular:

- Consultas não disparam nem avançam processamento.
- Lotes, trabalhos e vínculos com remessa/UG/cenário persistem consistentemente;
  reinício conserva o estado e reset invalida trabalhos da geração anterior.
- Dados recebidos, processados e efetivos permanecem distinguíveis e rastreáveis.
  Contagens e resultados derivam dos payloads e de seus efeitos.
- Valores monetários usam decimal exato; identificadores preservam tipos e zeros.
- Processamento e expiração admitem avanço determinístico de tarefas/tempo.
- Controle administrativo fica separado da API compatível; parâmetros de teste
  não entram no payload do integrador.
- Operações ou assuntos sem suporte não podem produzir aprovação silenciosa
  no modo estrito.

Use credenciais fictícias e tokens exclusivos do simulador. O piloto admite
dados originais de produção somente na base local de testes e no ambiente
controlado informado pelo usuário. Mantenha esses dados fora do versionamento,
de serviços externos e de evidências compartilhadas. Use exemplos fictícios
ou sanitizados; logs comuns não expõem senhas, tokens ou dados pessoais.
O serviço funciona sem encaminhar chamadas ao TCE e sem depender dele nos ensaios.

## Validação e conclusão

Antes de executar build ou testes, encontre e inspecione os comandos realmente
disponíveis e seus efeitos. Não há comando de validação estabelecido nesta fase.
Use RTK quando a filtragem preservar a evidência necessária; use `rtk proxy`
para saída integral ou comandos não suportados, preservando erros e exit codes.

Associe testes à matriz T01–T19 e às regras implementadas. O recorte inicial
prioriza estrutura, sequência, conteúdo e contagens, mantendo estados, falhas e
recuperação definidos. Validação de vínculos entre assuntos e regras tributárias
fica para evolução, com cobertura explicitamente não implementada. Valide os
vínculos lote/remessa/UG/cenário e os efeitos do reset. T01 completo exige
o integrador real com entrada conhecida e esperado independente; fixtures ou
cliente simples validam o simulador, mas não comprovam esse aceite.

Mantenha o manifesto com operação, assunto, regra, versão, evidência, teste e
situação: implementada, aproximada, não implementada ou pendente. Relate o que
foi executado, os resultados e as lacunas. Compatibilidade com um subconjunto
não equivale a homologação oficial.

## Rastreamento das tarefas

Antes de iniciar uma tarefa, leia os registros pertinentes em
[`PROGRESS.md`](PROGRESS.md). Ao concluir, interromper ou ficar bloqueado,
acrescente uma entrada conforme o formato desse arquivo, com o que foi feito,
o que deu errado e o próximo passo. Registre também tarefas parciais e ligue a
entrada à issue, ao ticket local ou à solicitação que a originou.

Preserve o histórico e diferencie execução comprovada de trabalho planejado.
O registro local não substitui o rastreador nem autoriza publicação externa.
Se a tarefa proibir alterações ou permitir somente leitura, apresente a entrada
no relato final para inclusão posterior, sem editar `PROGRESS.md`.

## Limites de trabalho

Escreva documentação e relatos em português, preservando identificadores do
contrato. Limite alterações à tarefa solicitada. Pergunte quando a ambiguidade
afetar escopo, correção, aceite ou autorização; resolva escolhas rotineiras pelas
convenções existentes.

Auditorias autorizam somente leitura. Publicação, implantação, mensagens
externas, alterações em dados compartilhados/produção e operações destrutivas
exigem autorização específica para a ação e o alvo. Prepare e valide a mudança
antes desse limite; acesso a ferramentas não substitui autorização.

## Princípios Desenvolvimento

- **KISS e YAGNI:** a menor solução que cumpre o ticket e seus casos de falha; abstrações e padrões precisam resolver um problema presente.
- **SOLID proporcional:** responsabilidades coesas, contratos pequenos e dependências explícitas.
- **TDD por comportamento:** teste que falha pelo motivo esperado, implementação mínima e revisão/refatoração com testes verdes; uma fatia vertical por vez, nos pontos de verificação aprovados na especificação.

## Agent skills

### Issue tracker

Issues e especificações são acompanhadas no GitHub Issues deste repositório. Consulte `docs/agents/issue-tracker.md`.

### Triage labels

A triagem usa os cinco rótulos canônicos. Consulte `docs/agents/triage-labels.md`.

### Domain docs

Layout single-context: `CONTEXT.md` na raiz e decisões em `docs/adr/`. Consulte `docs/agents/domain.md`.
