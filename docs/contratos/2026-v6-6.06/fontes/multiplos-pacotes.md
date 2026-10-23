# Tributário \- Envio de Múltiplos Pacotes de Dados

Com o objetivo de otimizar o processamento dos dados recebidos pelo TCE/SC, foi disponibilizada, a partir de setembro de 2025, para os envios referentes ao exercício de 2026, a possibilidade de segmentação do envio do Módulo Tributário em múltiplas remessas, conforme definido no layout v6.00 – Tributário, publicado em 12 de setembro de 2025.  
  
Essa funcionalidade permite maior flexibilidade no envio, melhor desempenho no processamento e maior eficiência nos processos de validação, correção, reenvio, cancelamento e consulta dos dados. 

## Conceitos Fundamentais

Para correto entendimento da estrutura de envio, adotam-se as seguintes definições:

- Remessa: Agrupamento lógico de dados enviado ao TCE/SC. É a remessa que, após processamento sem erros impeditivos, recebe o código de registro.
- Lote: Conjunto de dados enviado de forma assíncrona ao TCE/SC. Uma remessa pode ser composta por um ou mais lotes. 

## **Limites e Regras Gerais** 

- No módulo tributário é permitido o envio de até 64 remessas por mês, por Unidade Gestora.
- Esse limite não se aplica aos lotes, não havendo limitação de quantidade de lotes por remessa.
- Não é permitido iniciar mais de uma remessa simultaneamente para o mesmo ano/mês e Unidade Gestora.
- A funcionalidade foi disponibilizada nos ambientes de Produção e Homologação/Testes no início de 2026. 

## **Boas Práticas de Dimensionamento** 

Lotes:

- Recomenda-se que cada lote contenha entre 10.000 e 50.000 registros.
- Recomenda-se que cada lote contenha apenas um assunto.

  
Remessas:

- Recomenda-se, preferencialmente, até 500.000 registros por remessa.
- Para volumes superiores a 100.000 registros mensais em um único assunto, recomenda-se o envio de remessa exclusiva.

  
A segmentação pode considerar critérios adicionais, como divisão por período ou categoria. Essa abordagem facilita processos como validação, identificação de inconsistências, correção, reenvio e cancelamento de dados específicos. 

## **Exemplos de estratégias recomendadas**

### **Cenário 1 – Unidade Pequena**

Quantidade de dados:

| Assunto | Quantidade |
| --- | --- |
| Cadastro Imobiliário | 500 |
| Cadastro de Propriedade Imobiliária | 200 |
| Cadastro de Contribuinte | 300 |
| Início do Prazo Prescricional | 200 |
| Cobrança de Dívida Ativa | 200 |
| Estorno de Receita do Diário Geral | 100 |
| Lançamento de Créditos Tributários | 800 |
| Situação da Cobrança de Dívida Ativa | 100 |
| Certidão de Dívida Ativa | 100 |
| Baixa de Créditos Tributários | 200 |
| Revisão de Valor de Lançamento | 100 |
| Diário Geral de Arrecadação | 1.000 |

Recomendação: 

- Envio em uma única remessa, contendo um lote por assunto.

### **Cenário 2 – Unidade Média**

Quantidade de dados:

| Assunto | Quantidade |
| --- | --- |
| Cadastro Imobiliário | 1.000 |
| Cadastro de Propriedade Imobiliária | 1.000 |
| Cadastro de Contribuinte | 1.000 |
| Início do Prazo Prescricional | 1.000 |
| Cobrança de Dívida Ativa | 1.000 |
| Estorno de Receita do Diário Geral | 1.000 |
| Lançamento de Créditos Tributários | 6.000 |
| Situação da Cobrança de Dívida Ativa | 1.000 |
| Certidão de Dívida Ativa | 1.000 |
| Baixa de Créditos Tributários | 2.000 |
| Revisão de Valor de Lançamento | 60.000 |
| Diário Geral de Arrecadação | 10.000 |

Recomendação: 

- Envio em uma única remessa, com um lote por assunto.
- Para assuntos com maior volume (ex.: Revisão de Valor de Lançamento), dividir em múltiplos lotes dentro da mesma remessa (ex.: 50.000 + 10.000 registros ou múltiplos lotes de 10.000).

### **Cenário 3 – Unidade Média/Grande**

Quantidade de dados:

| Assunto | Quantidade |
| --- | --- |
| Cadastro Imobiliário | 100.000 |
| Cadastro de Propriedade Imobiliária | 20.000 |
| Cadastro de Contribuinte | 30.000 |
| Início do Prazo Prescricional | 20.000 |
| Cobrança de Dívida Ativa | 20.000 |
| Estorno de Receita do Diário Geral | 10.000 |
| Lançamento de Créditos Tributários | 150.000 |
| Situação da Cobrança de Dívida Ativa | 50.000 |
| Certidão de Dívida Ativa | 10.000 |
| Baixa de Créditos Tributários | 60.000 |
| Revisão de Valor de Lançamento | 200.000 |
| Diário Geral de Arrecadação | 200.000 |

Recomendação: 

- Envio em múltiplas remessas, preferencialmente uma remessa por assunto,
- Lotes com 10.000 a 50.000 registros.

### **Cenário 4 – Unidade Grande**

Quantidade de dados:

| Assunto | Quantidade |
| --- | --- |
| Cadastro Imobiliário | 1.000.000 |
| Cadastro de Propriedade Imobiliária | 200.000 |
| Cadastro de Contribuinte | 300.000 |
| Início do Prazo Prescricional | 200.000 |
| Cobrança de Dívida Ativa | 200.000 |
| Estorno de Receita do Diário Geral | 100.000 |
| Lançamento de Créditos Tributários | 1.500.000 |
| Situação da Cobrança de Dívida Ativa | 500.000 |
| Certidão de Dívida Ativa | 100.000 |
| Baixa de Créditos Tributários | 500.000 |
| Revisão de Valor de Lançamento | 500.000 |
| Diário Geral de Arrecadação | 2.000.000 |

Recomendação:

- Envio fortemente segmentado, com:
    - uma remessa por assunto;
    - múltiplas remessas para assuntos de altíssima volumetria;
    - lotes entre 10.000 e 50.000 registros.
- Exemplo de divisão:
    - Cadastro Imobiliário: 2 remessas;
    - Lançamento de Créditos Tributários: 3 remessas;
    - Diário Geral de Arrecadação: 4 remessas.

Essa estratégia simplifica significativamente os processos de validação, correção, reenvio e eventual cancelamento.

## **Validação e Ambiente de Testes** 

Conforme a Instrução Normativa TC-28/2021, art. 29, inciso VI, é **obrigatória **a implementação nos sistemas contratados as regras de consistência (CONs) impeditivas e de alerta divulgadas pelo TCE/SC. 

Adicionalmente a essa implementação, recomenda-se:

- Utilizar o ambiente de homologação/testes para novos processos ou grandes volumes de dados.
- Disponibilizar, nos sistemas das Unidades Gestoras, mecanismos claros para envio ao ambiente de testes. (Exemplo: Botão de "Envio para Teste" ou parâmetro de ambiente) para direcionar o envio dos dados para homologação. Isso garante que a própria Unidade Gestora tenha autonomia para homologar seus pacotes de forma segura antes da submissão definitiva.
