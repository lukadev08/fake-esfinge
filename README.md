# fake-esfinge

Simulador HTTP local do e-Sfinge Tributário do TCE/SC,
independente da origem dos dados e sem encaminhar chamadas ao TCE.

E0 está concluído documentalmente no recorte inicial. A primeira fatia E1
([#1](https://github.com/lukadev08/fake-esfinge/issues/1)) implementa autenticação
fictícia, contexto configurável e persistência SQLite. Remessas, lotes,
processamento, reset e painel ainda não estão implementados. A saída E1 depende
do aceite humano da #8; login isolado não comprova compatibilidade ou T01.

## Documentação

- [Planejamento de desenvolvimento](docs/planejamento-simulador-tributario.md):
  fonte principal de escopo, etapas E0–E4, invariantes e aceite T01–T19.
- [Arquitetura de código](ARCHITECTURE.md): stack adotada, portas e adaptadores,
  domínio, fluxo de dados e práticas de implementação e testes para agentes.
- [Contrato E0 consolidado](docs/contrato-e0.md): inventário relatado,
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
para as decisões atuais. Os comandos abaixo validam somente a fatia implementada.

Dados originais do piloto permanecem no ambiente local controlado, fora do Git
e de serviços externos. Exemplos e evidências compartilhadas exigem sanitização;
credenciais e tokens do simulador serão fictícios.

## Executar e validar a fatia #1

Requer JDK 21. O wrapper oficial fixa Maven 3.9.9; o primeiro uso baixa Maven e
as dependências do Maven Central. O build fixa Spring Boot 3.5.10, SQLite JDBC
3.49.1.0 e ArchUnit 1.4.0. Não acessa o TCE ou bases do integrador.

```sh
./mvnw verify
java -jar target/fake-esfinge-0.1.0.jar
```

A suíte cobre núcleo, HTTP, SQLite temporário e dependências de classes. A base
padrão fica em `data/simulator.sqlite`, ignorada pelo Git. Reiniciar conserva
configuração, vínculos e sessões; schema, migração ou configuração persistida
incompatíveis interrompem a inicialização sem recriar dados. Não há reset nesta
fatia. Para outra execução, use outro arquivo/volume; preserve o anterior.

Exemplo inteiramente fictício, sem corpo na requisição:

```sh
curl -i -X POST 'http://localhost:8080/autenticacao/login?codigoUg=0001&descricaoEmpresaTI=EmpresaFicticia&descritivoSoftware=SoftwareFicticio' \
  -H 'codigoAcesso: acesso-ficticio-a' -H 'senha: senha-ficticia-a'
curl -i 'http://localhost:8080/__simulator/configuracao' \
  -H 'X-Simulator-Key: admin-local-ficticio'
```

O login devolve `{"chave":"sim-..."}`; use esse valor em `auth_token`. Credenciais
inválidas retornam 401; UG não autorizada, 403; parâmetros/corpo inválidos, 400.
As rotas contratuais conhecidas ainda indisponíveis validam o token e retornam
422 `SIM_COBERTURA_AUSENTE`; não abrem remessas nem aprovam dados. Recurso
inexistente retorna 404. Erros têm `codigo`, `mensagem` sanitizada e `correlacao`.

A inspeção administrativa usa exclusivamente `X-Simulator-Key`; não aceita o
token compatível e não retorna senhas, códigos de acesso ou tokens. Mostra perfil,
cenário, seed, tempo inicial, modo do relógio, validade, geração e IDs/UGs fictícios.
Os logs padrão ficam desabilitados para evitar detalhes de frameworks/SQL; o
processo imprime somente o resultado genérico da inicialização. Erros HTTP
permanecem consultáveis com correlação. Não habilite logs de requisição ou SQL
sobre dados originais.

## Configuração local

O exemplo inicial está em [application.properties](src/main/resources/application.properties).
Sobrescreva propriedades por arquivo externo com
`--spring.config.additional-location=file:/caminho/simulador.properties` ou
argumentos Spring. Não versione arquivos locais com dados do piloto.

| Propriedade | Exemplo / efeito |
|---|---|
| `simulator.database` / `SIMULATOR_DATABASE` | Arquivo SQLite local exclusivo da execução |
| `simulator.scenario` | ID do cenário, string sem transformação |
| `simulator.profile` | Somente `2026/V6/6.06` implementado; outros são recusados |
| `simulator.seed`, `simulator.initial-time` | Contexto rastreável; instante ISO-8601 |
| `simulator.clock-mode` | `system` acompanha UTC; `fixed` conserva o instante configurado |
| `simulator.token-seconds` | Inteiro positivo; padrão 3600, decisão aproximada local |
| `simulator.admin-key` / `SIMULATOR_ADMIN_KEY` | Chave fictícia administrativa, diferente das credenciais |
| `simulator.users[0].id`, `.access-code`, `.password`, `.ugs` | Identidade fictícia e UGs separadas por vírgula, preservando zeros |

Outros usuários usam índices consecutivos `[1]`, `[2]` etc. Senhas são guardadas
com PBKDF2 e salt; tokens persistem somente por hash. Alterar configuração de um
cenário já persistido exige arquivo/volume novo; esta fatia não migra identidades
nem altera parâmetros de execução ativa. O avanço administrativo de tempo fica
na #16; testes usam relógio substituível sem espera real.

## Docker mínimo Linux amd64

Empacote com `./mvnw verify` antes do build. A imagem usa Java 21 fixado por digest
amd64 e usuário sem privilégios; não inclui ferramentas de desenvolvimento.

```sh
docker build --platform linux/amd64 -t fake-esfinge:0.1.0 .
docker volume create fake-esfinge-execucao
docker run --name fake-esfinge-execucao --platform linux/amd64 \
  -p 127.0.0.1:8080:8080 --mount source=fake-esfinge-execucao,target=/data \
  fake-esfinge:0.1.0
docker restart fake-esfinge-execucao
```

O volume conserva o estado entre reinícios. Compose, CI e guia operacional
completo pertencem à #21. O runtime não contém cliente/proxy para o TCE.
Consulte [cobertura atual](docs/cobertura.md) e
[evidência da implementação](docs/evidencias/issue-1.md) para resultados e limites.
