# Pacote contratual derivado — 2026 / V6 / 6.06

Revisão 1, consolidada em 23/10/2026 por solicitação do usuário.
**Artefato documental; nenhuma operação implementada ou compatibilidade comprovada.**
E0 está concluído documentalmente no recorte inicial, com roteiro de conferência
local definido; a referência interna e o aceite do piloto permanecem em E3, conforme o
[contrato E0](../../contrato-e0.md#pendências-e-condição-de-saída-de-e0).

## Conteúdo e rastreabilidade

- [Fontes e hashes](fontes.json): quatro cópias de manuais e uma extração de DTOs,
  fornecidas em 21/10 e incorporadas/verificadas em 23/10. Os bytes coincidem com
  os hashes registrados no ticket local 01. Data original de obtenção e versão
  da extração continuam desconhecidas, declaradas como `null`, sem bloquear a
  identificação deste contrato como derivado. Hash não autentica publicação oficial.
- [Schemas JSON](schemas.json): JSON Schema 2020-12. A raiz valida um lote; `$defs`
  contém registros, projeções de headers/query, corpos e respostas das operações.
- [Exemplos fictícios](exemplos.json): cada item indica o nome em `$defs` e o
  valor que o exemplifica. Os seis exemplos de lote são independentes; não são
  dados reais nem uma execução do integrador. A finalização exemplifica um registro
  de cada assunto; deve ser ajustada às contagens efetivas da execução concreta.
- Requisições, códigos HTTP e efeitos estão no [contrato E0](../../contrato-e0.md).
  O pacote não é OpenAPI oficial exportado. O manual anterior à V6 foi preservado
  como referência histórica, sem prevalecer sobre o perfil V6.

O manual de leiaute mantém os exemplos públicos da cópia fornecida, sem alteração.
Dados originais do piloto, resultados SQL e credenciais reais não integram o pacote.
O caminho original local e a primeira incorporação estão registrados no
[ticket 01](../../../.scratch/wayfinder-e0/issues/01-disponibilizar-fontes-e-amostras.md).

## Decisões de schema

| Elemento | Origem e aplicação |
|---|---|
| Cinco registros extraídos | `outputdto.txt`: nomes JSON e tipos Java, com os ajustes do cliente abaixo |
| Obrigatórios incondicionais | Tabelas dos seis assuntos no leiaute 6.06; rejeitar ausência e `null` |
| Campos “Depende dos dados” | Opcionais/nulos no recorte inicial. Condições tributárias, defaults condicionais e referências de negócio não são validados nem inferidos |
| Comprimentos de strings | Limites explícitos das tabelas; identificadores originalmente strings conservam zeros |
| Datas dos registros | Strings `YYYY-MM-DD`, com validação de data de calendário; não aceitar timestamp em seu lugar |
| Valores monetários | Número JSON decimal exato, múltiplo de 0,01; sem arredondar. A equivalência numérica permite `1`, `1.0` e `1.00`, preservando os bytes originais. Não se exige grafia fixa de duas casas |
| Tipos numéricos | Sem coerção de string ou booleano para número. Ano/mês de abertura restrito a meses de 2026. Ano/mês de competência do crédito deve conter mês válido; não se impõe ano 2026 a créditos históricos |
| Enums/tamanhos numéricos | Catálogos referidos por links não foram incorporados; não validar tabelas externas, dígitos fiscais, limites numéricos ambíguos ou valores mínimos tributários por suposição. Cobertura fica não implementada |
| Listas do envelope | Exatamente uma propriedade reconhecida e uma lista não vazia; demais listas omitidas, sem `null`; sem limite de 10.000 imposto pelo simulador |
| Extras | Rejeitados no modo estrito, em registros e corpos. Nos schemas de headers/query, a projeção inclui apenas campos de contrato; headers HTTP comuns, como `Host`, não são extras de payload |
| Totalizadores | Todas as 17 propriedades fornecidas pelo usuário presentes como strings de dígitos; as 11 fora dos seis assuntos exigem `"0"`. Comparar numericamente com o conjunto efetivo, sem coerção dos registros |

Precisão/escala são tratadas com decimal exato; o validador não pode converter
`multipleOf` para comparação aproximada em ponto flutuante. `format` deve ser
validado, além da expressão de data. As regras de ano/mês do crédito e efeitos
de fluxo complementam o JSON Schema; schema isolado não valida transições.

### Divergências preservadas

1. `numeroParcela` consta na extração; o cliente confirmou **`numeroParcelas`**,
   inteiro opcional. O schema usa o plural e rejeita o singular no modo estrito.
2. `inicioPrazoPrescricional` não consta na extração: os quatro campos do exemplo
   fornecido foram aprovados com ano inteiro, código string e datas `YYYY-MM-DD`.
3. A extração contém campos simples de inscrição imobiliária/transferência; o
   exemplo Swagger contém listas aninhadas. O cliente não os envia. Essas
   propriedades simples e aninhadas ficam fora do schema inicial, sem expansão
   silenciosa de cobertura.
4. Strings numéricas no exemplo Swagger não substituem os tipos efetivamente
   enviados pelo cliente. A exceção confirmada são os totalizadores da finalização,
   que permanecem strings.
5. A cópia V6 impede descarte por assunto com lote `RECEBIDO`. A recuperação
   escolhida permite limpar os pendentes do assunto após resposta perdida,
   preservando a chave; é aproximação explícita do simulador.
6. Obrigatoriedade condicional, defaults do tipo de saldo, enums externos,
   cronologia, CPF/CNPJ e regras fiscais permanecem fora da validação inicial.
   As exigências incondicionais das tabelas são o subconjunto estrutural adotado;
   recepção não declara conformidade fiscal integral.

## Cobertura documental e teste

Cada schema de registro rastreia seu assunto no manifesto do contrato. Os campos
são enumerados em `properties`, e `required` distingue as exigências estruturais
implementáveis. Condições não expressas ali devem permanecer no manifesto como
não implementadas, sem atribuir aprovação fiscal ao sucesso estrutural.

Os quatro pontos de teste foram confirmados: HTTP compatível, administração
separada, integrador real/SQL independente e núcleo/SQLite. Validar exemplos e
rejeições de schema nesta entrega verifica somente os artefatos documentais,
sem provar persistência, transporte real, recuperação ou aceite T01.
