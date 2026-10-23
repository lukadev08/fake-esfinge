# fake-esfinge

Planejamento de um simulador HTTP local do e-Sfinge Tributário do TCE/SC,
independente da origem dos dados e sem encaminhar chamadas ao TCE.

O repositório contém documentação. Aplicação, build, testes e empacotamento
Docker ainda não estão implementados. E0 permanece aberto.

## Documentação

- [Planejamento de desenvolvimento](docs/planejamento-simulador-tributario.md):
  fonte principal de escopo, etapas E0–E4, invariantes e aceite T01–T19.
- [Arquitetura de código](ARCHITECTURE.md): stack adotada, portas e adaptadores,
  domínio, fluxo de dados e práticas de implementação e testes para agentes.
- [Contrato E0 em elaboração](docs/contrato-e0.md): inventário relatado,
  comportamentos definidos, cobertura e pendências do piloto.
- [Mapa local E0](.scratch/wayfinder-e0/map.md): decisões e oito tickets locais;
  rascunhos sem publicação no GitHub.
- [Orientações para agentes](AGENTS.md) e configuração de
  [rastreador](docs/agents/issue-tracker.md),
  [rótulos](docs/agents/triage-labels.md) e
  [documentação de domínio](docs/agents/domain.md).

Perfil selecionado pelo usuário: 2026 / V6 / leiaute 6.06. O recorte inicial
prioriza recepção, validação estrutural, sequência, conteúdo e contagens.
Validação de vínculos entre assuntos e regras tributárias fica para evolução;
o piloto inicial não equivale ao aceite integral do MVP ou T01.

As versões HTML, PDF e DOCX do planejamento são apresentações da versão
anterior e não foram regeneradas nesta revisão de 23/10/2026. Consulte o Markdown
para as decisões atuais. Ainda não há comando de instalação ou validação da aplicação.

Dados originais do piloto permanecem no ambiente local controlado, fora do Git
e de serviços externos. Exemplos e evidências compartilhadas exigem sanitização;
credenciais e tokens do simulador serão fictícios.
