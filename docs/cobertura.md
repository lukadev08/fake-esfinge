# Cobertura implementada — perfil 2026/V6/6.06

Artefato 0.1.0, issues [#1](https://github.com/lukadev08/fake-esfinge/issues/1) e
[#2](https://github.com/lukadev08/fake-esfinge/issues/2).
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
| Persistência | SQLite real, migrações V1→V2 com checksum/version, FK por conexão, reabertura, incompatibilidade sem recriar | `JdbcAuthenticationRepositoryTest`, `JdbcRemessaTest` | Implementada para contexto/identidades/sessões/remessas/lotes/eventos; parcela de T13 |
| Erros e logs | Envelope próprio sanitizado, sem senha/token/SQL/stack trace | `InternalFailureHttpTest`, testes HTTP/configuração | Implementada; logs padrão desabilitados |
| Fronteiras | Núcleo Java puro, domínio sem aplicação, capacidades sem ciclos | `ArchitectureTest`, análise de dependências de classes | Implementada na fatia atual |
| Operações/assuntos ainda indisponíveis | Token válido recebe 422 de cobertura, inválido 401; inexistente 404 | `AuthenticationHttpTest`, `RemessaHttpTest`; parcela de T18 | Cobertura ausente explícita; sem processamento/finalização ou outros cinco assuntos |
| Abrir envio | UG/cenário do token, mês de 2026, NAO; restrição única para abertura concorrente | `RemessaHttpTest`, `JdbcRemessaTest`; parcelas T10/T14 | Implementada no contrato derivado; SIM/omissão recebem 422 |
| Receber contribuinte | Lista direta na raiz; schema, bytes/hash/ordem/contagem/evento atômicos; sem dedupe/limite de 10.000 | `RemessaHttpTest`, `JdbcRemessaTest`, `RemessaFailureHttpTest`; parcelas T01/T18 | Implementada estruturalmente; sem validação fiscal |
| Consultar lote | RECEBIDO persistido, nomes contratuais, isolamento e atividade/evento sem processamento | `RemessaHttpTest`, `JdbcRemessaTest`, `RemessaExpiryHttpTest`; parcelas T02/T10/T13 | Implementada; recebidos não são efetivos |
| Inspeção de lote | Chave administrativa, cenário atual, bytesBase64/registros/contagens/eventos | `RemessaHttpTest`, `RemessaFailureHttpTest` | Implementada sob `/__simulator/lotes/{numeroLote}` |
| Falha de persistência | Rollback de statement/commit SQLite real, 500 sanitizado e nenhuma confirmação parcial | `JdbcRemessaTest`, `RemessaFailureHttpTest` | Implementada para abertura/recepção/consulta da fatia |
| Disparo / finalização | Processar e concluir fluxo completo | Issues #9–#10 | Não implementada |
| Demais cinco assuntos | Crédito, arrecadação, baixa, saldo e prazo | Schemas documentais; issues #3–#7 | Não implementada |
| Descarte / cancelamento / recuperação / reset / expiração de envio | Efeitos, geração e relógio administrativo | Issues #11–#17 | Não implementada |
| Regras fiscais / vínculos entre assuntos / CONs | Evolução sem catálogo aprovado | Issue #27 | Não implementada |
| Aceites humanos / T01 / E1–E5 | Integrador real, referência independente e reprodução | Issues #8, #19, #20, #22, #26 | Pendente |
| Docker Linux amd64 | Imagem mínima, volume local e runtime Java21 | Dockerfile; supervisão da #2 com 178 verificações HTTP/reinício sem rede, imagem identificada na evidência | Implementada e exercitada localmente no jar da #2 |

Evidência da #2: [execução local](evidencias/issue-2.md), 51 testes aprovados no total,
178 verificações Docker e revisão independente sem achados materiais.

A suíte automatizada destas fatias não conclui integralmente T10/T11 nem substitui
renovação pelo integrador real. Recepção, processamento e regras ausentes não
recebem aprovação fiscal ou homologação implícita.
