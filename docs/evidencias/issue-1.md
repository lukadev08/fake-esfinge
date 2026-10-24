# Evidência local — issue #1

Data: 24/10/2026. Branch `issue-1-autenticacao`, artefato 0.1.0.
Executor: `grupo1_executor`, GPT-6.1 Sol, raciocínio low. Exploração anterior por
`grupo1_explorer` (GPT-6 Luna, raciocínio médio). Revisão independente por
`grupo1_reviewer` (GPT-6.1 Sol, raciocínio médio), aprovada sem achados materiais.
Todos os exemplos são fictícios. Não houve acesso ao TCE, integrador ou dados do piloto.

## Ciclos observados

| Fatia | RED observado | GREEN posterior |
|---|---|---|
| Emissão de chave opaca | `autenticaComChaveOpaca`: `UnsupportedOperationException` | Um teste aprovado |
| Negação de credencial | `Expected RuntimeException to be thrown, but nothing was thrown` | Dois testes do núcleo aprovados |
| Token com contexto | `tokenMantemContextoAteExpirar`: `INVALID_CREDENTIALS` em token emitido | Sete testes do núcleo aprovados, incluindo limite de expiração |
| SQLite / reabertura | `reaberturaConservaSessaoEContexto`: `UnsupportedOperationException` | Sessão/contexto recuperados por novo adaptador/base reaberta |
| Login HTTP | `Status expected:<200> but was:<404>` | HTTP 200 com `chave` |
| Inspeção administrativa | `Status expected:<200> but was:<404>` | HTTP 200 protegido por chave própria |

Os comandos focalizados usaram Maven 3.9.9, JDK Corretto 21.0.2 e SQLite JDBC
3.49.1.0, com `-Dtest=<classe> test`. Maven foi obtido do Maven Central em diretório
temporário e teve SHA-512 conferido; o wrapper oficial 3.3.2 fixa Maven3.9.9 e SHA-256.

## Verificação final do executor

Comando real: `./mvnw -B -ntp -Dmaven.repo.local=/private/tmp/fake-esfinge-m2 verify`,
com JDK21 e cache do wrapper em diretório temporário. Resultado: **30 testes,
zero falhas, zero erros, zero ignorados**, build e empacotamento aprovados.

- Núcleo: 8 testes; TTL padrão/configurado, expiração exata, vínculos, negações.
- HTTP compatível/administrativo: 8 testes, incluindo múltiplas requisições negativas.
- SQLite real: 6 testes; reabertura, credenciais, FK em cada conexão, versão e
  configuração incompatíveis, preservação de banco desconhecido.
- Arquitetura: 3 testes sobre classes compiladas.
- Configuração: 4 testes; contexto, perfil, TTL inválido, segredos e chave própria.
- Falha interna HTTP: 1 teste; falha real SQLite não confirma login nem expõe
  SQL/credenciais/stack trace em resposta ou logs capturados.

O ambiente emite avisos de autoanexação Mockito/ByteBuddy nos testes Spring,
sem falha da suíte. A produção não usa mocks. Os testes de transporte desta suíte
usam MockMvc com SQLite real; a execução em socket/container foi conferida
separadamente pelo supervisor. Dockerfile fixado em Temurin21.0.10_7 por digest amd64.

## Supervisão local de Docker

O supervisor executou `clean verify` no código final: 30 testes, zero falhas,
erros ou ignorados; build aprovado em 5,387 segundos. Usou o wrapper, JDK21 do
SDKMAN e cache temporário, sem instalação global.

Build Linux amd64 aprovado e repetido após a correção da fronteira de inspeção
de identidades. Imagem final:
`sha256:ed25542942bc78946f9c640641e0d038c503075cd9f7749cd81038ebe538b44d`.
Executada com usuário `10001:10001`, volume local e `--network none`. Cliente Java
independente temporário executou 85 verificações HTTP (login, 400/401/403/404/422,
administração separada e sanitização), mais oito após reinício: token/contexto/UG
persistiram. As 93 verificações passaram também na imagem final. Logs observados
continham somente `Simulador iniciado`. Inicialização com TTL malformado retornou
exit code 1 e somente `SIM_INICIALIZACAO_INVALIDA`, sem valor, SQL ou stack trace.
São evidências locais de supervisão; não comprovam integrador real/T01.

O primeiro download limpo do wrapper com checksum encontrou incompatibilidade no
`sha256sum -c` do macOS (exige indicar stdin). Ajustado para `sha256sum -c -`,
compatível com macOS/GNU; checksum conferido e novo download seguido de 30 testes
aprovados. O wrapper mantém a distribuição oficial e checksum fixado. Self-check adicional
com checksum adulterado retornou exit1 antes de executar Maven; `sh -n mvnw` passou.

## Limites

Revisão independente e supervisão da #1 concluídas; aceite humano E1 continua pendente.
Não há remessas/lotes/processamento/reset/painel ou prova T01. As fontes originais
e schemas do contrato foram preservados. Nenhum commit, push ou PR foi feito pelo executor.
