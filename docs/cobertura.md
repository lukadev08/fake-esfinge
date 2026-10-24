# Cobertura implementada — perfil 2026/V6/6.06

Artefato 0.1.0, issue [#1](https://github.com/lukadev08/fake-esfinge/issues/1).
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
| Persistência | SQLite real, migração/checksum/version, FK por conexão, reabertura, incompatibilidade sem recriar | `JdbcAuthenticationRepositoryTest` | Implementada para contexto/identidades/sessões; não conclui T13 |
| Erros e logs | Envelope próprio sanitizado, sem senha/token/SQL/stack trace | `InternalFailureHttpTest`, testes HTTP/configuração | Implementada; logs padrão desabilitados |
| Fronteiras | Núcleo Java puro, domínio sem aplicação, capacidades sem ciclos | `ArchitectureTest`, análise de dependências de classes | Implementada na fatia atual |
| Operações de remessa/lote ainda indisponíveis | Token válido recebe 422 de cobertura, inválido 401; inexistente 404 | `AuthenticationHttpTest`; parcela de T18 | Aproximada como cobertura ausente; operação não implementada |
| Abertura / recepção / consulta / disparo / finalização | Fluxo completo e conteúdo | Issues #2–#10 | Não implementada |
| Seis assuntos | Contribuinte, crédito, arrecadação, baixa, saldo e prazo | Schemas documentais; issues #2–#7 | Não implementada |
| Descarte / cancelamento / recuperação / reset / expiração de envio | Efeitos, geração e relógio administrativo | Issues #11–#17 | Não implementada |
| Regras fiscais / vínculos entre assuntos / CONs | Evolução sem catálogo aprovado | Issue #27 | Não implementada |
| Aceites humanos / T01 / E1–E5 | Integrador real, referência independente e reprodução | Issues #8, #19, #20, #22, #26 | Pendente |
| Docker Linux amd64 | Imagem mínima, volume local e runtime Java21 | Dockerfile; supervisor: 93 verificações HTTP/reinício sem rede, imagem final identificada na evidência | Implementada e exercitada localmente no jar final |

A suíte automatizada desta fatia não conclui integralmente T10/T11 nem substitui
renovação pelo integrador real. Recepção, processamento e regras ausentes não
recebem aprovação fiscal ou homologação implícita.
