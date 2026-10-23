# Disponibilizar fontes e amostras sanitizadas do contrato

Status: draft
Type: task
Mode: HITL
Label: `wayfinder:task`
Blocked by: none


> Histórico: perguntas, dependências e pendências abaixo registram as rodadas anteriores.
> A situação vigente está na consolidação final ao término deste ticket. `Status: draft`
> continua indicando ausência de publicação, não ausência de decisão.

## Question

Quais documentos, exports e amostras podem ser fornecidos? Disponibilizar arquivos ou links, indicando origem, data e se cada fonte é oficial ou derivada.

## Answers

Resposta fornecida em 21/10/2026.

O usuário reuniu manuais do TCE sobre leiaute, envio do Tributário e API, além de uma extração dos campos/tipos dos DTOs enviados pelo integrador. Não pode fornecer código bruto do integrador. Estes são os insumos disponíveis por ora.

### Materiais disponibilizados

| Arquivo local | Origem e classificação | Data de obtenção/extração |
|---|---|---|
| `/Users/luka/Downloads/Tributário - Envio de Múltiplos Pacotes de Dados/Tributário - Envio assíncrono.md` | Manual do TCE, conforme informado pelo usuário; cópia local em Markdown, sem verificação de integridade com a publicação oficial. | Não informada; disponibilizado nesta conversa em 21/10/2026. |
| `/Users/luka/Downloads/Tributário - Envio de Múltiplos Pacotes de Dados/Tributário - Envio de Múltiplos Pacotes de Dados.md` | Manual do TCE, conforme informado pelo usuário; cópia local em Markdown, sem verificação de integridade com a publicação oficial. | Não informada; disponibilizado nesta conversa em 21/10/2026. |
| `/Users/luka/Downloads/Tributário - Envio de Múltiplos Pacotes de Dados/Tributário V6 - Envio assíncrono.md` | Manual do TCE, conforme informado pelo usuário; cópia local em Markdown, sem verificação de integridade com a publicação oficial. | Não informada; disponibilizado nesta conversa em 21/10/2026. |
| `/Users/luka/Downloads/Tributário - Envio de Múltiplos Pacotes de Dados/v6.06 - Tributário.md` | Manual do TCE, conforme informado pelo usuário; cópia local em Markdown, sem verificação de integridade com a publicação oficial. | Não informada; disponibilizado nesta conversa em 21/10/2026. |
| `/Users/luka/Downloads/outputdto.txt` | Extração do integrador fornecida pelo usuário; fonte derivada, não OpenAPI oficial. | Não informada; disponibilizado nesta conversa em 21/10/2026. |

### Rastreabilidade dos arquivos recebidos

SHA-256 calculado sobre os arquivos locais em 21/10/2026; identifica a cópia recebida, sem comprovar autenticidade oficial.

- `Tributário - Envio assíncrono.md`: `866a68bf9ebd063e3fc7326245a24585928866944466560eb776c2fc4b00ed2d`.
- `Tributário - Envio de Múltiplos Pacotes de Dados.md`: `0f3df606a8993f755bfd016cadd62051ed1568030ca2b161a84780ca37348f53`.
- `Tributário V6 - Envio assíncrono.md`: `f1116929aede3cbf7ccbd719ca4ea8ff4c3984ff0c74e8e9e8b61e4830ca48fd`.
- `v6.06 - Tributário.md`: `35be9ca7c120cf8348999767101cfc59bd4a054f39d0a9b09fb99608bcf3024d`.
- `outputdto.txt`: `ace6afafd9e921ee60a41c8fe2cc4a30b45bca26889ee21b96eb0410df057593`.

### Lacunas e limites

- Não foi fornecida exportação OpenAPI, amostras completas de requisições/respostas nem exemplos de erros ou cancelamento parcial.
- A data de obtenção dos manuais e a data/versão da extração dos DTOs não foram informadas. A versão 6.06 no nome do leiaute não confirma, por si só, a versão usada pelo integrador.
- `outputdto.txt` contém campos, anotações JSON e tipos; não equivale ao contrato HTTP completo nem comprova quais rotas o cliente chama.
- O manual anterior à V6 descreve fluxo diferente. Deve permanecer como referência histórica, sem misturar seu comportamento ao perfil V6 a confirmar.
- A indisponibilidade de código bruto é uma restrição informada, não uma pendência de fornecimento. O inventário das chamadas poderá ser obtido por descrição e exemplos sanitizados.

### Consolidação em 23/10/2026

Inventário e limites incorporados ao [contrato E0 em elaboração](../../../docs/contrato-e0.md#perfil-e-evidências).
Os caminhos e hashes acima preservam o registro de 21/10; os arquivos externos
não foram relidos ou copiados nesta consolidação. Falta uma localização
reproduzível para fontes contratuais sanitizadas, com data/versão conhecida.
Isso não inclui versionar dados originais do piloto.

## Consolidação final de 23/10/2026 — plano aprovado

Situação documental do recorte inicial: **concluída**.

- Incorporados quatro manuais e `outputdto.txt` no pacote contratual, sem alterar bytes; SHA-256 iguais aos cinco registrados em 21/10.
- Fornecimento em 21/10; incorporação/verificação em 23/10; data original de obtenção e versão da extração desconhecidas declaradas, sem autenticidade oficial presumida.
- Schemas e exemplos fictícios derivados documentados; dados originais do piloto não foram copiados. Código bruto continua indisponível por restrição, não pendência.

Referência atual: [contrato E0](../../../docs/contrato-e0.md) e
[pacote contratual](../../../docs/contratos/2026-v6-6.06/README.md).
Origem: respostas do solicitante e plano explicitamente aprovado nesta conversa;
sem captura HTTP, execução do integrador ou publicação no GitHub.
