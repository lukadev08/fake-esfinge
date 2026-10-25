# Cobertura implementada — perfil 2026/V6/6.06

Artefato 0.1.0, issues [#1](https://github.com/lukadev08/fake-esfinge/issues/1) e
[#2](https://github.com/lukadev08/fake-esfinge/issues/2) e
[#3](https://github.com/lukadev08/fake-esfinge/issues/3) e
[#4](https://github.com/lukadev08/fake-esfinge/issues/4) e
[#5](https://github.com/lukadev08/fake-esfinge/issues/5) e
[#6](https://github.com/lukadev08/fake-esfinge/issues/6).
Fontes: [contrato E0](contrato-e0.md), [pacote derivado](contratos/2026-v6-6.06/README.md)
e [hashes das fontes](contratos/2026-v6-6.06/fontes.json), consolidados em 23/10/2026.
Não há OpenAPI oficial nem ensaio independente do integrador/TCE. O manifesto
inicial dentro de E0 conserva o estado documental daquela entrega; este arquivo
registra a evolução implementada.

| Operação / assunto | Regra | Evidência / teste | Situação |
|---|---|---|---|
| Login | POST sem corpo, headers/query, `chave`, 400/401/403 | Contrato E0; `AuthenticationHttpTest`, `AuthenticationServiceTest` | Implementada no contrato derivado |
| Token | Vínculo usuário/UG/cenário, opaco, inválido/expirado 401 antes de efeito | `AuthenticationServiceTest`, `AuthenticationHttpTest`; parcela T10/T11 | Implementada |
| Validade de token | 3600 segundos configuráveis, relógio substituível | Contrato E0; testes de limite e duração customizada | Aproximada; duração local aprovada, sem equivalência oficial |
| Administração | GET `/__simulator/configuracao`, chave própria, inspeção sem segredos | `AuthenticationHttpTest`, `SimulatorSettingsTest` | Implementada; API exclusiva do simulador |
| Persistência | SQLite real, migrações V1→V2→V3→V4→V5→V6 com checksum/version, FK por conexão, reabertura, incompatibilidade sem recriar | `JdbcAuthenticationRepositoryTest`, `JdbcRemessaTest` | Implementada para contexto/identidades/sessões/remessas/lotes/eventos; parcela de T13 |
| Erros e logs | Envelope próprio sanitizado, sem senha/token/SQL/stack trace | `InternalFailureHttpTest`, testes HTTP/configuração | Implementada; logs padrão desabilitados |
| Fronteiras | Núcleo Java puro, domínio sem aplicação, capacidades sem ciclos | `ArchitectureTest`, análise de dependências de classes | Implementada na fatia atual |
| Operações/assuntos ainda indisponíveis | Token válido recebe 422 de cobertura, inválido 401; inexistente 404 | `AuthenticationHttpTest`, `RemessaHttpTest`; parcela de T18 | Cobertura ausente explícita; sem processamento/finalização ou prazo prescricional |
| Abrir envio | UG/cenário do token, mês de 2026, NAO; restrição única para abertura concorrente | `RemessaHttpTest`, `JdbcRemessaTest`; parcelas T10/T14 | Implementada no contrato derivado; SIM/omissão recebem 422 |
| Receber contribuinte | Lista direta na raiz; schema, bytes/hash/ordem/contagem/evento atômicos; sem dedupe/limite de 10.000 | `RemessaHttpTest`, `JdbcRemessaTest`, `RemessaFailureHttpTest`; parcelas T01/T18 | Implementada estruturalmente; sem validação fiscal |
| Receber crédito | Lista direta, schema `credito`, mês histórico válido, dinheiro BigDecimal/múltiplo exato de 0,01 sem coerção/arredondamento; TEXT por registro e bytes originais | `CreditoHttpTest`, `JdbcRemessaTest`, `RemessaFailureHttpTest`; parcelas T01/T02/T18 | Implementada estruturalmente; referências, cronologia, catálogos e cálculos tributários não implementados |
| Receber arrecadação | Lista direta `diarioGeralArrecadacao`, schema `arrecadacao`, decimal exato em TEXT e bytes/contagens/vínculos atômicos | `ArrecadacaoHttpTest`, `JdbcRemessaTest`, `RemessaFailureHttpTest`; parcelas T01/T02/T18 | Implementada estruturalmente; sem cronologia, catálogos, referência fiscal ao crédito ou cálculos |
| Receber baixa | Lista direta `baixasCreditosTributarios`, schema `baixa`, obrigatórios/opcionais e decimal exato em TEXT; bytes/contagens/vínculos atômicos | `BaixaHttpTest`, `JdbcRemessaTest`, `RemessaFailureHttpTest`; parcelas T01/T02/T18 | Implementada estruturalmente; baixa anterior ao crédito, existência de crédito/dívida ativa, catálogos e cálculos não implementados |
| Receber saldo inicial | Lista direta, schema `saldo`, quatro valores nomeados BigDecimal/TEXT, ano histórico e opcional sem default | `SaldoHttpTest`, `ReceivedContentTest`, `JdbcRemessaTest`, `RemessaFailureHttpTest`; parcelas T01/T02/T16/T18 | Implementada estruturalmente em cenário fictício janeiro; sem condições/defaults/catálogo fiscal ou validação de abertura de exercício |
| Valores recebidos | Mapas imutáveis por registro e campo exato por assunto; V6 atribui nomes ao dinheiro antigo | `ReceivedContentTest`, `JdbcRemessaTest`; campos/precisão/cardinalidade/imutabilidade/migração | Implementada; quatro linhas por registro de saldo, uma nos três assuntos monetários anteriores |
| `numeroParcelas` | Inteiro opcional/nulo no plural; singular `numeroParcela` rejeitado | Pacote derivado/relato do cliente; `ArrecadacaoHttpTest` | Aproximada; divergência da extração preservada explicitamente |
| Consultar lote | RECEBIDO persistido, nomes contratuais, isolamento e atividade/evento sem processamento | `RemessaHttpTest`, `JdbcRemessaTest`, `RemessaExpiryHttpTest`; parcelas T02/T10/T13 | Implementada; recebidos não são efetivos |
| Inspeção de lote | Chave administrativa, cenário atual, bytesBase64/registros/contagens/eventos | `RemessaHttpTest`, `CreditoHttpTest`, `ArrecadacaoHttpTest`, `BaixaHttpTest`, `SaldoHttpTest`, `RemessaFailureHttpTest` | Implementada para contribuintes/créditos/arrecadação/baixas/saldo com números exatos sob `/__simulator/lotes/{numeroLote}` |
| Falha de persistência | Rollback de statement/commit SQLite real, 500 sanitizado e nenhuma confirmação parcial | `JdbcRemessaTest`, `RemessaFailureHttpTest` | Implementada para abertura/recepção/consulta da fatia |
| Disparo / finalização | Processar e concluir fluxo completo | Issues #9–#10 | Não implementada |
| Prazo prescricional | Schema documental; issue #7 | Recepção estrutural futura | Não implementada |
| Descarte / cancelamento / recuperação / reset / expiração de envio | Efeitos, geração e relógio administrativo | Issues #11–#17 | Não implementada |
| Regras fiscais / vínculos entre assuntos / CONs | Evolução sem catálogo aprovado | Issue #27 | Não implementada |
| Aceites humanos / T01 / E1–E5 | Integrador real, referência independente e reprodução | Issues #8, #19, #20, #22, #26 | Pendente |
| Docker Linux amd64 | Imagem mínima, volume local e runtime Java21 | Dockerfile; supervisão da #2 com 178 verificações HTTP/reinício sem rede, imagem identificada na evidência | Implementada e exercitada localmente no jar da #2 |

Evidência da #2: [execução local](evidencias/issue-2.md), 51 testes aprovados no total,
178 verificações Docker e revisão independente sem achados materiais.

Evidência da #3: [execução local](evidencias/issue-3.md), 61 testes aprovados no total,
revisão independente sem achados materiais e 767 verificações na imagem V3,
incluindo upgrade de base V2 preenchida, HTTP, reinício e leitura decimal em TEXT.
As 163 verificações de preparação na imagem anterior são contabilizadas separadamente.

Evidência da #4: [execução local](evidencias/issue-4.md), 71 testes aprovados no total,
revisão independente sem achados materiais e 853 verificações na imagem V4,
incluindo upgrade de V3 preenchida, HTTP, reinício e leitura exata da tabela comum
TEXT. As 704 verificações de preparação na imagem V3 são contabilizadas separadamente.

Evidência da #5: [execução local](evidencias/issue-5.md), 82 testes aprovados no total,
revisão independente sem achados materiais e 794 verificações na imagem V5,
incluindo upgrade de V4 preenchida, HTTP, reinício e valores TEXT exatos dos três
assuntos monetários. As 771 verificações de preparação em V4 são contabilizadas separadamente.

Evidência da #6: [execução local](evidencias/issue-6.md), 95 testes aprovados no total,
revisão independente sem achados materiais e 729 verificações no jar macOS arm64,
incluindo upgrade, reinício e quatro valores nomeados exatos por saldo. Docker V6
permanece pendente por indisponibilidade do runtime; não há imagem V6 validada.
Janeiro é demonstração estrutural; T01/T16 integral, cenário complementar pelo
integrador real e saída E1 permanecem pendentes.

A suíte automatizada destas fatias não conclui integralmente T10/T11 nem substitui
renovação pelo integrador real. Recepção, processamento e regras ausentes não
recebem aprovação fiscal ou homologação implícita.
