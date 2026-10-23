# v6\.06 \- Tributário

Registra os lançamentos de crédito tributários, as inscrições em dívida ativa e os cadastros relacionados da Unidade Gestora, todos ocorridos em um determinado ano/mês.

Aplicável somente para as unidades gestoras da esfera municipal, exceto Empresas e Associações.

A partir do exercício de 2026 será permitido o envio de até 64 pacotes desse tipo por Ano/Mês e por Unidade Gestora.

Mesmo se não existir lançamentos tributários ou qualquer outro dado para ser enviado no ano/mês, este pacote deve ser enviado com o conteúdo vazio.

O cálculo do saldo de um código de lançamento do crédito/inscrição em dívida ativa é efetuado do seguinte modo:

**A)** Para códigos de lançamento que **não possuem saldo inicial em 01/01/2026**:  
( + ) Lançamento do Crédito Tributário/DA  
(+/-) Revisões do Crédito Tributário/DA  
( -/+ ) Baixas/Estornos de Baixas do Crédito Tributário/DA  
( - ) Arrecadações do Diário Geral de Arrecadação (exceto Pagamento a Maior)  
( + ) Estornos de Arrecadação da Receita (exceto Pagamento  Maior)  
( = ) Saldo Final do Crédito Tributário/DA

**B)** Para códigos de lançamento que **possuem saldo inicial em 01/01/2026**:  
( + ) Saldo inicial de Créditos Tributários e Não Tributários/Inscrição em Dívida Ativa enviado no pacote de janeiro   
(+/-) Revisões do Crédito Tributário/DA  
( -/+ ) Baixas/Estornos de Baixas do Crédito Tributário/DA  
( - ) Arrecadações do Diário Geral de Arrecadação (exceto Pagamento a Maior)  
( + ) Estornos de Arrecadação da Receita (exceto Pagamento a Maior)  
( = ) Saldo Final do Crédito Tributário/DA

**Prazo de envio: Até o vigésimo dia do mês subsequente à ocorrência dos fatos.**

## Parâmetros de Entrada

|  |  |  |  |  |
| --- | --- | --- | --- | --- |
| **Descrição do campo** | **Tipo** | **Tamanho** | **Obrigatório** | **Observações** |
| #Ano/Mês | Numérico | 6 | Sim | Ano/Mês do pacote. Não confundir com o Ano/Mês do lançamento do Crédito Tributário ou da data do envio. |
| Etiqueta da remessa | Carácter | 16 | Não | Campo de livre uso pelos usuários/empresas para identificar o pacote do tributário. Este dado será anexado ao campo identificação da remessa e será visível pelo usuário nas telas de consulta. |
| [Cadastro Contribuinte](https://jira-tcesc.atlassian.net/wiki/spaces/SD/pages/2154923885/v6.06+-+Tribut+rio#CadastroContribuinte) | Tabela | N | Não |  |
| [Cadastro Imobiliário](https://jira-tcesc.atlassian.net/wiki/spaces/SD/pages/2154923885/v6.06+-+Tribut+rio#CadastroImobiliario) | Tabela | N | Não |  |
| [Cadastro de Propriedade Imobiliária](https://jira-tcesc.atlassian.net/wiki/spaces/SD/pages/2154923885/v6.06+-+Tribut+rio#CadastroPropriedadeImobiliaria) | Tabela | N | Não |  |
| [Lançamento de Créditos Tributários/Inscrição em Dívida Ativa](https://jira-tcesc.atlassian.net/wiki/spaces/SD/pages/2154923885/v6.06+-+Tribut+rio#LancamentoCreditosTributarios) | Tabela | N | Não |  |
| [Saldo inicial de Créditos Tributários e Não Tributários/Inscrição em Dívida Ativa](https://jira-tcesc.atlassian.net/wiki/spaces/SD/pages/2154923885/v6.06+-+Tribut+rio#saldoInicial) | Tabela | N | Não | Para unidades do tipo Prefeitura, é obrigatório o envio de ao menos um registro, exclusivamente no mês de janeiro (mês 1) para o tipo 1 - Saldo Inicial Abertura Exercício. ~~Para os demais meses somente será aceito o tipo 2 - Saldo Inicial - Reintegração de Saldo.~~ |
| [Cobrança de Dívida Ativa](https://jira-tcesc.atlassian.net/wiki/spaces/SD/pages/2154923885/v6.06+-+Tribut+rio#ProtestoExecParc) | Tabela | N | Não |  |
| [Início do prazo prescricional](https://jira-tcesc.atlassian.net/wiki/spaces/SD/pages/2154923885/v6.06+-+Tribut+rio#DataInicioPrazoPrescricional) | Tabela | N | Não |  |
| [Certidão de Dívida Ativa](https://jira-tcesc.atlassian.net/wiki/spaces/SD/pages/2154923885/v6.06+-+Tribut+rio#CDA) | Tabela | N | Não |  |
| [Situação do Tipo de Cobrança de Dívida Ativa](https://jira-tcesc.atlassian.net/wiki/spaces/SD/pages/2154923885/v6.06+-+Tribut+rio#SituaCobraDA) | Tabela | N | Não |  |
| [Revisão de Valor de Lançamento de Créditos Tributários e de Dívidas Ativas](https://jira-tcesc.atlassian.net/wiki/spaces/SD/pages/2154923885/v6.06+-+Tribut+rio#RevisaoValorLancamentoCreditosTributarios) | Tabela | N | Não |  |
| [Revisão de Tipo de Crédito Tributários e de Dívidas Ativas](https://jira-tcesc.atlassian.net/wiki/spaces/SD/pages/2154923885/v6.06+-+Tribut+rio#RevisaoTipoLancamentoCreditosTributarios) | Tabela | N | Não |  |
| [Baixa dos Créditos Tributários](https://jira-tcesc.atlassian.net/wiki/spaces/SD/pages/2154923885/v6.06+-+Tribut+rio#BaixaCreditosTributarios) | Tabela | N | Não |  |
| [Diário Geral de Arrecadação](https://jira-tcesc.atlassian.net/wiki/spaces/SD/pages/2154923885/v6.06+-+Tribut+rio#DiarioGeralArrecadacao) | Tabela | N | Não |  |
| [Estorno ou Restituição de Receita do Diário Geral de Arrecadação](https://jira-tcesc.atlassian.net/wiki/spaces/SD/pages/2154923885/v6.06+-+Tribut+rio#EstornoReceitaDiarioGeralArrecadacao) | Tabela | N | Não |  |

## Totalizadores

Esse campo será gerado automaticamente pelo sistema, não necessita ser preenchido pelo usuário.

|  |  |  |  |  |
| --- | --- | --- | --- | --- |
| **Descrição do campo** | **Tipo** | **Tamanho** | **Obrigatório** | **Observações** |
| Quantidade de Registros de Cadastro Contribuinte | Numérico   | 8   | Sim   | Indica a quantidade total de registros enviados de Cadastro Contribuinte |
| Quantidade de Registros de Cadastro Imobiliário | Numérico   | 8   | Sim   | Indica a quantidade total de registros enviados de Cadastro Imobiliário |
| Quantidade de Registros de Cadastro de Propriedade Imobiliária | Numérico   | 8   | Sim   | Indica a quantidade total de registros enviados de Cadastro de Propriedade Imobiliária |
| Quantidade de Registros de Lançamento de Créditos Tributários | Numérico   | 8   | Sim   | Indica a quantidade total de registros enviados de Lançamento de Créditos Tributários |
| Quantidade de Registros de Revisão de Valor de Lançamento de Créditos Tributários | Numérico   | 8   | Sim   | Indica a quantidade total de registros enviados de Revisão de Valor de Lançamento de Créditos Tributários |
| Quantidade de Registros de Baixa dos Créditos Tributários | Numérico   | 8   | Sim   | Indica a quantidade total de registros enviados de Baixa dos Créditos Tributários |
| Quantidade de Registros de Diário Geral de Arrecadação | Numérico   | 8   | Sim   | Indica a quantidade total de registros enviados de Diário Geral de Arrecadação |
| Quantidade de Registros de Estorno de Receita do Diário Geral de Arrecadação | Numérico   | 8   | Sim   | Indica a quantidade total de registros enviados de Estorno de Receita do Diário Geral de Arrecadação |
| Quantidade de registros de Protesto/Execução/Parcelamento | Numérico   | 8   | Sim   | Indica a quantidade total de registros enviados de Protesto/Execução/Parcelamento |
| Quantidade de registros de Certidão de Dívida Ativa | Numérico   | 8   | Sim   | Indica a quantidade total de registros enviados de Certidão de Dívida Ativa |
| Quantidade de registros de Situação Cobrança de Dívida Ativa | Numérico   | 8   | Sim   | Indica a quantidade total de registros enviados de Situação Cobrança de Dívida Ativa |
| Quantidade de registros de Início do prazo prescricional | Numérico   | 8 | Sim   | Indica a quantidade total de registros enviados de Início do prazo prescricional |
| Quantidade de registros de Saldo inicial de Créditos Tributários e Não Tributários/Inscrição em Dívida Ativa | Numérico   | 8 | Sim   | Indica a quantidade total de registros enviados de Saldo inicial de Créditos Tributários e Não Tributários/Inscrição em Dívida Ativa |
| Quantidade de registros de Novo Número de Inscrição Imobiliária | Numérico   | 8 | Sim   | Indica a quantidade total de registros enviados de Novo Número de Inscrição Imobiliária |
| Quantidade de registros de Lançamento do Crédito Tributário ou da Dívida Ativa Original - Transferência | Numérico   | 8 | Sim   | Indica a quantidade total de registros enviados de Lançamento do Crédito Tributário ou da Dívida Ativa Original - Transferência |
| Quantidade de registros de Número de Inscrição Imobiliária vinculados ao Lançamento do Crédito Tributário ou da Dívida Ativa | Numérico   | 8 | Sim   | Indica a quantidade total de registros enviados de Número de Inscrição Imobiliária vinculados ao Lançamento do Crédito Tributário ou da Dívida Ativa |
| Quantidade de registros de Revisão de Tipo de Crédito Tributários e de Dívidas Ativas | Numérico   | 8 | Sim   | Indica a quantidade total de registros enviados de Tipo de Crédito Tributários e de Dívidas Ativas |


## **Cadastro Contribuinte**

*Capta os dados referentes aos contribuintes do município para os quais haja lançamento tributário ou propriedade imobiliária.*

|  |  |  |  |  |
| --- | --- | --- | --- | --- |
| **Descrição do campo** | **Tipo** | **Tamanho** | **Obrigatório** | **Observações** |
| #Número/Matricula do Contribuinte | Carácter | 16 | Sim | Representa o código de inscrição do contribuinte no cadastro do município. |
| #Data de Atualização Cadastral | Data |  | Sim | Corresponde à data da atualização cadastral do contribuinte. |
| Número do Documento Contribuinte | Numérico | 14 | Sim | Representa o número do documento identificador do contribuinte (CPF para pessoa física e CNPJ para jurídica).Conforme comunicado DIE n. 02/2023, se o Tipo de Contribuinte for "01 - Pessoa Física" e em casos excepcionais será admitido não informar CPF para contribuinte quando estiver em processo de atualização cadastral. Com isso a regra a ser aplicada é:1) Obrigatório informar para os Tipo de Contribuinte iguais a 2, 3 e 4.2) Se Tipo de Contribuinte for "02 - Pessoa Jurídica" será testado o digito verificador de CNPJ.3) Se Tipo de Contribuinte for "01 - Pessoa Física pessoa física" será aceito 0 ou vazio que será convertido para valor 0, para os casos excepcionais de contribuintes em processo de atualização cadastral pela origem. Se diferente de vazio ou 0 será testado o digito verificador de CPF.4) Se o Tipo de Contribuinte for "03 - Identificação Internacional – Empresas" ou "04 - Pessoa Física – Estrangeiro" e o número do documento utilizado possua letras e números, utilizar somente os números, removendo as letras.Este campo é obrigatório desde 2024. |
| Nome Completo do Contribuinte | Carácter | 512 | Sim | Representa o nome do contribuinte cadastrado no município. |
| Tipo de Contribuinte | Numérico | 2 | Sim | Indica o tipo de pessoa conforme [Tabela 40 – Tipo de Pessoa](https://jira-tcesc.atlassian.net/wiki/spaces/SD/pages/2154924964/v6.06+-+Tabela+40+Tipo+de+Pessoa). |
| Indicativo de Cadastro Ativo | Carácter | 1 | Sim | Indica se o cadastro do contribuinte está ativo ou inativo. ("S" ou "N")Para o primeiro envio (cadastro inicial) não se pode enviar inativo ("N"). Inativo somente é aceito depois de existir ao menos um ativo ("S") na base de dados. |
| Indicativo de Espólio | Carácter | 1 | Sim | Indica se o contribuinte se enquadra como espólio ("S" ou "N") nos termos do art. 131 III do CTN. |
| Nome do Representante | Carácter | 512 | Depende dos dados | Corresponde ao nome do Representante em caso de Espólio.  Obrigatório se o Tipo de Representante for 01 - Administrador Provisório (art. 1.797 Lei 10.406/2002) 02 - Inventariante (art. 75 Lei 13.105/2015)Obs: A indicação de um representante do espólio se mostra necessária, tendo em vista que o espólio figura como responsável dos tributos devidos pelo de cujus, até a abertura da sucessão (art. 131, III da Lei 5.172/66) e como contribuinte para os fatos gerados ocorridos após a data de falecimento, enquanto não realizada a transferência dos bens. |
| CPF Representante | Numérico | 11 | Depende dos dados | Corresponde CPF do Representante em caso de Espólio. Obrigatório se o Tipo de Representante for 01 - Administrador Provisório (art. 1.797 Lei 10.406/2002) 02 - Inventariante (art. 75 Lei 13.105/2015) |
| Tipo de Representante | Numérico | 2 | Depende dos dados | Indica o tipo de representante conforme [Tabela 133 - Tipo de Representante](https://jira-tcesc.atlassian.net/wiki/spaces/SD/pages/2154926362/v6.06+-+Tabela+133+-+Tipo+de+Representante) Obrigatório se Indicativo de Espólio = “S”Obs.: O uso do tipo “03 - Falecido mas Sem Administrador Provisório ou Inventariante Identificado” deve esporádico e temporário. |
| Novo Número/Matricula do Contribuinte | Carácter | 16 | Não | Representa o novo código de inscrição contribuinte no cadastro do município. Informar somente no caso de mudança do Número/Matricula do Contribuinte para um outro número. Só pode ser informado se o campo Indicativo de Cadastro Ativo for “N”. |



## **Cadastro Imobiliário**

*Capta os dados referentes aos imóveis localizados na zona urbana do município sobre os quais incide o Imposto Predial e Territorial Urbano (IPTU) e Imposto de Transmissão de Bens Imóveis Inter Vivos (ITBI).*

*Na primeira remessa deste cadastro deverão ser encaminhados todos os registros atualizados dos imóveis. Em remessas posteriores deverão ser encaminhados os registros dos imóveis que sofreram modificações em suas características.*

*Registro deve ser atualizado a cada mudança de um dos seus valores, por exemplo, atualização do valor venal.*

|  |  |  |  |  |
| --- | --- | --- | --- | --- |
| **Descrição do campo** | **Tipo** | **Tamanho** | **Obrigatório** | **Observações** |
| #Número de Inscrição Imobiliária | Carácter | 40 | Sim | Representa o número de inscrição imobiliária constante do cadastro imobiliário do município. |
| #Data de Atualização Cadastral | Data |  | Sim | Corresponde à data da atualização cadastral do imóvel. |
| Número de Cadastro do Imóvel | Carácter | 32 | Não | Número do imóvel constante no cadastro interno da Unidade |
| Código CIB | Carácter | 8 | Não | Código CIB (Cadastro Imobiliário Brasileiro), nos termos do art 4º, Parágrafo único do Decreto nº 11.208/2022 (federal) c/c art.265 da Lei Complementar nº 214/2025 (federal), conforme segue:*Art.4º(…)**Parágrafo único: A inscrição no CIB a que se refere o caput consiste em um código de identificação unívoco atribuído pelo Sinter a cada imóvel, válido no território nacional, **formado por sete caracteres alfanuméricos e um dígito verificador, com a estrutura “AAAAAAA-D”**.*Não enviar o traço, envie no formato “***AAAAAAAD”*** |
| Tipo de Imóvel | Numérico | 2 | Sim | Indica o tipo de imóvel Conforme [Tabela 91 – Tipo de Imóvel – Cadastro Imobiliário](https://jira-tcesc.atlassian.net/wiki/spaces/SD/pages/2154925717/v6.06+-+Tabela+91+Tipo+de+Im+vel+Cadastro+Imobili+rio). |
| Tipo de Utilização do Imóvel | Numérico | 2 | Sim | Indica o tipo de utilização do imóvel conforme [Tabela 90 – Tipo de Utilização do Imóvel – Cadastro Imobiliário](https://jira-tcesc.atlassian.net/wiki/spaces/SD/pages/2154925702/v6.06+-+Tabela+90+Tipo+de+Utiliza+o+do+Im+vel+Cadastro+Imobili+rio). |
| Fração Ideal | Numérico | 16 | Não | Indica a fração ideal do imóvel em m2Enviar sempre com duas casas decimais, com separador '.', mesmo que o valor seja 0. |
| Área Territorial Total do Imóvel | Numérico | 16 | Sim | Indica a área territorial total do imóvel em m2. No caso de condomínios informar a área total do condomínio e o correspondente a cada condômino.Enviar sempre com duas casas decimais, com separador '.', mesmo que o valor seja 0. |
| Área Edificada do Imóvel | Numérico | 16 | Sim | Indica a área edificada do imóvel em m2.Enviar sempre com duas casas decimais, com separador '.', mesmo que o valor seja 0.Obs.: Quando não houver edificação informar o valor 0 (zero) como área edificada. |
| Valor Venal do Imóvel | Numérico | 16 | Sim | Corresponde ao o valor venal do imóvel constante do cadastro e utilizado para o lançamento do imposto no ano. Se o tipo de utilização for imóvel rural, informar o valor base para cálculo do ITBI.Enviar sempre com duas casas decimais, com separador '.', mesmo que o valor seja 0. |
| Descrição do Logradouro do Imóvel | Carácter | 512 | Sim | Representa o logradouro detalhado o imóvel.Deve identificar, no mínimo, o tipo (rua, avenida, viela, quadra, travessa, alameda etc.) e o nome da via. |
| Número do Imóvel | Carácter | 16 | Sim | Representa o número do móvel. Caso não possua, informar “s/n”. |
| Bairro do Imóvel | Carácter | 100 | Não | Representa o nome do bairro onde se encontra o imóvel. Caso não exista, não informar este campo. |
| Complemento Endereço do Imóvel | Carácter | 512 | Não | Dados complementares ao endereço/ logradouro como número do apartamento, da casa, sala, nome do condomínio, etc. |
| CEP do Imóvel | Numérico | 8 | Sim | Representa o Código de Endereçamento Postal (CEP) conforme Guia Postal Brasileiro. |
| Frequência da Coleta de Lixo Domiciliar | Numérico | 2 | Não | Indica a frequência com que a coleta de lixo é realizada.Conforme [Tabela 134 - Tipo de Frequência de Coleta](https://jira-tcesc.atlassian.net/wiki/spaces/SD/pages/2154926377/v6.06+-+Tabela+134+-+Tipo+de+Frequ+ncia+de+Coleta) |
| Frequência da Coleta de Lixo Seletiva | Numérico | 2 | Não | Indica a frequência com que a coleta de lixo é realizada.Conforme [Tabela 134 - Tipo de Frequência de Coleta](https://jira-tcesc.atlassian.net/wiki/spaces/SD/pages/2154926377/v6.06+-+Tabela+134+-+Tipo+de+Frequ+ncia+de+Coleta) |
| Observação Cadastro Imobiliário | Carácter | 512 | N | Pode ser usado para informar dados adicionais não existentes no sistema. |
| [Novo Número de Inscrição Imobiliária](https://jira-tcesc.atlassian.net/wiki/spaces/SD/pages/2154923885/v6.06+-+Tribut+rio#NovoCadastroImob) | Tabela | N | Depende dos dados | Representa a lista dos novos números de inscrição imobiliária no caso de mudança desse número.É obrigatório se tipo de utilização do imóvel for: - 97 - "Extinção da Inscrição Imobiliária por mudança de número de Inscrição Imobiliária" - 98 - Extinção da Inscrição Imobiliária por fusão/desmembramento" conforme [Tabela 90 – Tipo de Utilização do Imóvel – Cadastro Imobiliário](https://jira-tcesc.atlassian.net/wiki/spaces/SD/pages/2154925702/v6.06+-+Tabela+90+Tipo+de+Utiliza+o+do+Im+vel+Cadastro+Imobili+rio) Para os demais casos não enviar esta informação. |



## **Novo Número de Inscrição Imobiliária**

*Capta os dados referentes a migração/mudança de Número de Inscrição Imobiliária.*

|  |  |  |  |  |
| --- | --- | --- | --- | --- |
| **Descrição do campo** | **Tipo** | **Tamanho** | **Obrigatório** | **Observações** |
| #Novo Número de Inscrição Imobiliária | Carácter | 40 | Depende dos dados | Representa o novo número de inscrição imobiliária no caso de mudança desse número. |


## **Cadastro de Propriedade Imobiliária**

*Capta os dados referentes à propriedade dos imóveis localizados na zona urbana do município sobre os quais incide IPTU e ITBI, vinculando-os aos seus proprietários/responsáveis.*

*Na primeira remessa deverão ser informados todos os registros atualizados de vínculo entre imóveis e seus titulares. Em remessas posteriores deverão ser encaminhados registros quando houver mudança de titularidade e/ou condição do imóvel ou do proprietário.*

|  |  |  |  |  |
| --- | --- | --- | --- | --- |
| **Descrição do campo** | **Tipo** | **Tamanho** | **Obrigatório** | **Observações** |
| #Número de Inscrição Imobiliária | Carácter | 40 | Sim | Representa o número de inscrição imobiliária constante do cadastro imobiliário do município. |
| #Número/Matricula do Contribuinte | Carácter | 16 | Sim | Representa o código de inscrição de pessoa física ou jurídica no cadastro de contribuintes do município. |
| #Data de Alteração da Titularidade | Data |  | Sim | Representa a data em que o imóvel foi vinculado a proprietário/responsável. |
| Condição do Imóvel ou do Proprietário | Numérico | 2 | Sim | Indica se o imóvel ou proprietário é tributado ou não conforme [Tabela 92 – Tipo de Condição do Contribuinte – Cadastro Imobiliário](https://jira-tcesc.atlassian.net/wiki/spaces/SD/pages/2154925732/v6.06+-+Tabela+92+Situa+o+Fiscal+Tipo+de+Condi+o+do+Contribuinte+Cadastro+Imobili+rio). |


## **Lançamento de Créditos Tributários e Não Tributários/Inscrição em Dívida Ativa**

*Capta o lançamento dos créditos tributários e não tributários, bem como as inscrições em dívida ativa, previamente definidos na tabela de natureza e tipos de créditos. Os dados devem ser encaminhados por contribuinte/imóvel.*

Observações:

1. Para os Lançamentos efetuados com o Tipo de Operação **04 - Implantação de Saldos em virtude de migração de Sistema**

*Espera-se que sejam mantidos os dados do lançamento Original, sendo alterados apenas:*

- \#Código do lançamento do Crédito Tributário ou Número de Inscrição em Dívida Ativa - Representa controle sequencial de cada operação **existente no sistema anterior**

1. Para os Lançamentos efetuados com o Tipo de Operação **05 - Transferência de Crédito Tributário entre Contribuintes**

*Espera-se que sejam **mantidos os dados do Lançamento Original**, sendo alterados apenas os seguintes campos:*

- \#Código do lançamento do Crédito Tributário ou Número de Inscrição em Dívida Ativa - Representa controle sequencial de cada operação 
- Numero/Matrícula do Contribuinte: Deve representar o novo responsável pelo valor do crédito tributário (vide questões de responsabilidade tributária).
- Data do Lançamento Tributário ou Data da inscrição em Dívida Ativa: Deve representar a data em que houve a a transferência de responsabilidade
- Valor do Lançamento: Deve descrever o total de saldo transferido ao novo contribuinte

OBS: O Tipo de Operação - 05 **Transferência de Crédito Tributário entre Contribuintes**  **não deve ser utilizado para corrigir erros de Lançamentos,** os quais devem ser realizados por meio de Baixa e realização de novos Lançamentos

|  |  |  |  |  |
| --- | --- | --- | --- | --- |
| Descrição do campo | Tipo | Tamanho | Obrigatório | Observações |
| #Ano do Lançamento do Crédito Tributário ou da inscrição Dívida Ativa | Numérico | 4 | Sim | Representa o ano do código sequencial de controle de cada operação. |
| #Código do lançamento do Crédito Tributário ou Número de Inscrição em Dívida Ativa | Carácter | 16 | Sim | Representa o código sequencial de controle de cada operação.No caso do Tipo de Operação 01 - Lançamento Inicial do Crédito Tributário (Não Inscrito em Dívida Ativa) e 02 - Implantação de Saldos de Exercícios Anteriores (Não Inscritos em Dívida Ativa) deve corresponder ao número do lançamento tributário.Quando for o Tipo de Operação 03 - Dívida Ativa (Inscrição ou Saldos de Exercícios Anteriores) deve corresponder ao número de inscrição em Dívida Ativa. |
| Tipo de Operação | Numérico | 2 | Sim | Indica o tipo de operação do crédito conforme [Tabela 84 - Tipos de Operações dos Créditos Tributários e Não Tributários](https://jira-tcesc.atlassian.net/wiki/spaces/SD/pages/2154925612/v6.06+-+Tabela+84+-+Tipos+de+Opera+es+dos+Cr+ditos+Tribut+rios+e+N+o+Tribut+rios). |
| Observação do Lançamento do Crédito Tributário ou da inscrição Dívida Ativa | Carácter | 512 | Depende dos dados | Obrigatório, no mínimo 5 caracteres não brancos e diferentes se o Tipo de Operação 05 -  Transferência de Créditos Tributários entre Contribuintes. Neste caso deve ser descrito o Motivo da Transferência do Saldo do Crédito. Nos demais casos pode ser usado para informar dados adicionais não existentes no sistema. |
| Tipo de Crédito | Numérico | 2 | Sim | Indica o tipo de crédito conforme [Tabela 87 – Tipos de Créditos](https://jira-tcesc.atlassian.net/wiki/spaces/SD/pages/2154925657/v6.06+-+Tabela+87+Tipos+de+Cr+ditos). |
| Detalhamento do Tipo de Crédito | Carácter | 50 | Depende dos dados | Detalha o nome do tributo de com a legislação municipal.Obrigatório apenas quando o tipo de credito for 05 - Taxas ([Tabela 87 – Tipos de Créditos](https://jira-tcesc.atlassian.net/wiki/spaces/SD/pages/2154925657/v6.06+-+Tabela+87+Tipos+de+Cr+ditos)) |
| Número/Matrícula do Contribuinte | Carácter | 16 | Sim | Representa o código de inscrição do contribuinte no cadastro do município.Obrigatório para todos os tipos de crédito constantes [Tabela 87 – Tipos de Créditos](https://jira-tcesc.atlassian.net/wiki/spaces/SD/pages/2154925657/v6.06+-+Tabela+87+Tipos+de+Cr+ditos). Nos casos em que houver mais de um proprietário, por exemplo, deve-se enviar apenas o Número/Matrícula de quem foi efetuado o lançamento tributário. |
| Ano e Mês de competência do Crédito Tributário ou da inscrição em Dívida Ativa | Numérico | 6 | Sim | Corresponde ao ano e mês da competência do crédito tributário (formato AAAAMM).No caso do Tipo de Operação 01 - Lançamento Inicial do Crédito Tributário (Não Inscrito em Dívida Ativa) e 02 - Implantação de Saldos de Exercícios Anteriores (Não Inscritos em Dívida Ativa) deve corresponder ao mês/ano do fato gerador do tributo.Quando for o Tipo de Operação 03 - Dívida Ativa (Inscrição ou Saldos de Exercícios Anteriores) deve corresponder ao mês/ano da inscrição em Dívida Ativa.Obs.: mês deve estar compreendido no intervalo de 1 até 12 |
| Data do Lançamento Tributário ou Data da inscrição em Dívida Ativa | Data |  | Sim | Corresponde a data em que o crédito tributário foi lançado ou inscrito em dívida ativa.Deve ser menor do Ano/Mês do pacote sendo tirando os casos de implantação de saldos.Excepcionalmente durante a implantação do módulo está aceitando ser menor que o Ano/Mês do pacote. No caso do Tipo de Operação 01 - Lançamento Inicial do Crédito Tributário (Não Inscrito em Dívida Ativa) e 02 - Implantação de Saldos de Exercícios Anteriores (Não Inscritos em Dívida Ativa) deve corresponder a data do lançamento tributárioQuando for o Tipo de Operação 03 - Dívida Ativa (Inscrição ou Saldos de Exercícios Anteriores) deve corresponder à data da inscrição em Dívida Ativa.Quanto for Tipo de Operação 05 - Transferência de Crédito entre Contribuintes deve corresponder a data que originou a transferência do crédito. |
| Data de Vencimento | Data |  | Sim | Corresponde à data de vencimento original do crédito tributário. |
| Data de Importação Simples Nacional | Data |  | Depende dos dados | Data de Importação do Arquivo do Simples NacionalObrigatório se for Tipo de Crédito 15 - ISS SIMPLES NACIONAL.Nos demais casos não deve ser preenchido. |
| Valor do Lançamento do Crédito Tributário ou da Dívida Ativa | Numérico | 16 | Sim | Corresponde ao valor do crédito tributário. Quando a operação for do Tipo 01 - Lançamento Inicial do Crédito Tributário (Não Inscrito em Dívida Ativa) e 02 - Implantação de Saldos de Exercícios Anteriores (Não Inscritos em Dívida Ativa) deve corresponder ao valor do lançamento tributário.No caso de operação 03 - Dívida Ativa (Inscrição ou Saldos de Exercícios Anteriores), deve ser enviado o valor do saldo original com juros, multas e correões monetárias e deve ser igual ao valor da baixa do lançamento tributário.Enviar sempre com duas casas decimais, com separador '.', mesmo que o valor seja 0. |
| CPF do Responsável pela Operação | Numérico | 11 | Sim | Indica o CPF do usuário/agente público responsável por realizar o lançamento tributário ou a inscrição em dívida ativa.No caso de lançamentos ou cancelamentos em massa, efetuados pelo sistema, deve ser enviado o CPF de quem efetuou o comando. Caso não haja essa informação, deve ser enviado o CPF do gestor público responsável pelo setor que efetuou a operação. |
| Código da Especificação da Receita/Natureza da Receita | Numérico | 8 | Sim | Representa a especificação orçamentária da natureza da receita.Informar apenas as Naturezas de Receita valorizáveis e escrituráveis, ou seja, as analíticas, conforme planilha "NR" do [Anexo II da Portaria STN nº 642, de 20 de setembro de 2019 (Leiaute da MSC)](https://sisweb.tesouro.gov.br/apex/f?p=2501:9::::9:P9_ID_PUBLICACAO_ANEXO:22327). |
| [Número de Inscrição Imobiliária](https://jira-tcesc.atlassian.net/wiki/spaces/SD/pages/2154923885/v6.06+-+Tribut+rio#NumInscrImob) | Tabela | N | Depende dos dados | Representa o número de inscrição imobiliária constante do cadastro imobiliário do município.Obrigatório quando tratar-se de 01 - IPTU, 02 - ITBI (URBANO) e 04 - CONTRIBUIÇÃO DE MELHORIA.No caso de 05 - TAXAS, deve-se informar, obrigatoriamente, o Número de Inscrição Imobiliária quando o tributo tiver vinculação com determinado imóvel (Taxa de Lixo, Taxa de Emissão de Habite-se, Taxa de Licença de Execução de Obras.So é possível receber mais de um Número de Inscrição Imobiliária se o Tipos de Créditos for ITBI (códigos 02, 11 e 12) |
| [Lançamento do Crédito Tributário ou da Dívida Ativa Original - Transferência](https://jira-tcesc.atlassian.net/wiki/spaces/SD/pages/2154923885/v6.06+-+Tribut+rio#LancamentoCreditosTributariosTransf) | Tabela | N | Depende dos dados | Identifica os Lançamentos do Crédito Tributário para os casos de mudança de sistema ou transferência de crédito entre contribuintes (questões relacionadas à responsabilidade tributária).Somente será aceito se existir uma Baixa dos Créditos Tributários com o Tipos de Baixa dos Créditos Tributários e Não Tributários 12 - Zerar saldo em virtude de migração de Sistema de TI ou 13 - Transferência de crédito tributário de um contribuinte para outro.Se criado em decorrência de uma baixa do tipo 12, deve-se preencher o Lançamento do Crédito Tributário do sistema que está sendo migrado (código do sistema antigo).Se criado em decorrência de uma baixa do tipo 13, deve-se preencher o Lançamento do Crédito Tributário do novo sujeito passivo.**Campo obrigatório se o Tipo de Operação for 04 - Implantação de Saldos em virtude de migração de Sistema de TI ou 05 - Transferência de crédito tributário de um contribuinte para outro.**Para os demais Tipo de Operação não deve ser informado, e se informado será ignorado. |

## 

## **Número de Inscrição Imobiliária**

*Capta o Número de Inscrição Imobiliária dos créditos tributários*.

|  |  |  |  |  |
| --- | --- | --- | --- | --- |
| Descrição do campo | Tipo | Tamanho | Obrigatório | Observações |
| Número de Inscrição Imobiliária | Carácter | 40 | Depende dos dados | Representa o número de inscrição imobiliária constante do cadastro imobiliário do município.Obrigatório quando tratar-se de 01 - IPTU, 02 - ITBI (URBANO), 04 - CONTRIBUIÇÃO DE MELHORIA, **11 - ITBI (RURAL) e 12 - ITBI SOBREPARTILHA OU ITBI DIREITOS REAIS e 13 - IPTU DECORRENTES DE LEILÃO**.No caso de 05 - TAXAS, deve-se informar, obrigatoriamente, o Número de Inscrição Imobiliária quando o tributo tiver vinculação com determinado imóvel (Taxa de Lixo, Taxa de Emissão de Habite-se, Taxa de Licença de Execução de Obras. |


## **Lançamento do Crédito Tributário ou da Dívida Ativa Original - Transferência**

*Capta o lançamento dos créditos tributários em caso de transferência de *Lançamento do Crédito Tributário ou da Dívida Ativa.


|  |  |  |  |  |
| --- | --- | --- | --- | --- |
| Descrição do campo | Tipo | Tamanho | Obrigatório | Observações |
| #Ano do lançamento do Crédito Tributário ou da Dívida Ativa Original  | Numérico | 4 | Sim | Identifica o ano do Lançamento do Crédito Tributário, em caso de mudança de sistema (ver mais detalhes no campo: #Código do lançamento do Crédito Tributário ou Número de Inscrição em Dívida Ativa Original). |
| #Código do lançamento do Crédito Tributário ou Número de Inscrição em Dívida Ativa **Original** | Carácter | 16 | Sim | Identifica o Código do Lançamento do Crédito Tributário para os casos de mudança de sistema ou transferência de crédito entre contribuintes. |


## **Saldo Inicial (Implantação de Saldos) de Créditos Tributários e Não Tributários/Inscrição em Dívida Ativa**

*Esta seção destina-se ao registro dos saldos iniciais (vigentes em 31 de dezembro do exercício anterior) dos lançamento de créditos tributários e não tributários, bem como as inscrições em dívida ativa, previamente informados em exercícios anteriores por meio do serviço “Lançamento de Créditos Tributários e Não Tributários/Inscrição em Dívida Ativa”, **desde que ainda apresentem saldos em aberto**. *

A tabela também é destinada ao registro de **reintegração do crédito original,** nas hipóteses de reversão da inscrição em dívida ativa incluídas na remessa de Saldos Iniciais de abertura de exercício (*vide o item 2. Reintegração do Crédito Original da presente seção e o item 3.1 “Estorno de  Dívida Ativa oriunda de anos anteriores, como proceder?” constante na seção Baixas do Crédito Tributário*)

---

### 1. Regras Gerais para o Registro de Saldos Iniciais

- Devem ser informados como Saldo Inicial **todos os créditos tributários, não tributários e inscrições em dívida ativa** (Tipo de Saldo Inicial = 1 - Saldo Inicial ao ano) que:
    - tenham sido lançados em exercícios anteriores; e
    - apresentem **saldo em aberto** em 31 de dezembro do exercício anterior.
    - não pode ser enviado um Saldo Inicial se o Lançamento do Crédito Tributário ou da Dívida Ativa for feito no ano corrente.
- O valor informado como Saldo Inicial deve corresponder ao **saldo efetivamente existente**, incluindo, quando aplicável:
    - valor principal;
    - juros;
    - multas;
    - correções monetárias;
- O envio do Saldo Inicial **habilita o crédito** para movimentações futuras no exercício, tais como revisões, baixas, arrecadações e os respectivos estornos.
- O envio de Situação do Tipo de Cobrança de Dívida Ativa pode ser feito independente do envio de saldo inicial no exercício para o lançamento correspondente.

Os lançamentos realizados em exercícios anteriores que **não forem informados na Tabela Saldo Inicial** no mês de janeiro serão considerados **extintos no exercício anterior**, sendo vedado o registro de quaisquer movimentações vinculadas a esses créditos ao longo do exercício corrente.

- ~~Não será aceito o envio de saldo inicial de lançamentos quando este estiver associado a uma dívida ativa.~~
- Não será aceito o envio de saldo inicial de lançamentos associados a baixas cujo “Indicativo de Estorno da Baixa“ = “N“, quando não existir o respectivo registro de baixa com “Indicativo de Estorno da Baixa“ = “S“, que sejam dos tipos:
    - 05 - Prescrição
    - 10 - Inscrição em Dívida Ativa (Apenas para créditos tributários - Vide Tabela 87 Tipo de Crédito)
    - 12 - Zerar saldo em virtude de migração de Sistema de TI
    - 13 - Transferência de Crédito Tributário entre Contribuintes

Caso o último envio de baixa seja um estorno o saldo inicial será aceito normalmente.

### 2. Reintegração do Crédito Original 

A **Reintegração de Saldo do Crédito Original** de tipo “Saldo Inicial = 02 - Saldo Inicial (Reintegração de Crédito)” destina-se exclusivamente aos casos em que, após a **reversão da inscrição em dívida ativa**, o crédito retorna à sua situação anterior (crédito original); Além disso, cumpre ressaltar que a referida sistemática  aplica-se somente às situações em que o **crédito original não tenha sido devidamente informado na Tabela de Saldo Inicial do pacote de janeiro**, em razão de ter permanecido inscrito em dívida ativa à época da implantação dos saldos.

A utilização dessa modalidade está condicionada, obrigatoriamente:

- à prévia **Baixa da dívida ativa pelo tipo de Baixa 15– Reintegração do Crédito Original**, para extinção da dívida ativa vigente (Vide seção Baixa dos Créditos Tributários); e
- O Valor da reintegração do Crédito Original deve ser igual ao valor baixado (Tabela Baixa dos Créditos Tributários - Valor da Operação de Baixa);
- à **existência e validade do crédito original**.

Diferentemente da implantação regular de saldos iniciais, a **Reintegração do Crédito Original pode ser encaminhada em qualquer competência do exercício corrente ** do Tipo de Saldo Inicial = 02 - Saldo Inicial (Reintegração de Crédito), devendo ser informada no pacote de referência correspondente ao mês em que ocorreu o estorno da dívida ativa.

A Reintegração do Crédito Original tem por finalidade **recompor o saldo do crédito original**, permitindo sua continuidade no controle do sistema e a realização de movimentações no exercício corrente.

É **vedada** a reintegração quando o crédito original:

- for inexistente ou inválido;
- tiver sido definitivamente cancelado;
- estiver prescrito, desconstituído por decisão definitiva ou alcançado por outra forma de extinção do crédito.

### 3. Reintegração do Crédito Original por Decisão Judicial

A modalidade de Saldo Inicial do tipo “03 - Saldo Inicial (Reintegração de Crédito por Decisão Judicial)” destinam-se a situações em que o **crédito já existe na base do e-Sfinge**, mas necessita ter seu saldo reintegrado ao controle do exercício corrente por motivo de decisão judicial expedida.

Nesses casos, o envio do Saldo Inicial poderá ocorrer na competência correspondente ao fato que motivou a reintegração.

Após o envio ao e-Sfinge, conforme Exemplo 4 (ver EXEMPLOS PRÁTICOS), o crédito estará apto a receber as movimentações do exercício corrente.

### Exemplo 1 – Implantação Regular de Saldo Inicial (Abertura do Exercício)

**Situação:**

- Crédito tributário lançado em 2024;
- Não inscrito em dívida ativa;
- Saldo em aberto em 31/12/2025;
- Crédito permanece válido e exigível.

**Procedimento no e-Sfinge:**

1. No **pacote de janeiro de 2026**, registrar o crédito na tabela de Saldo Inicial do Tipo de Saldo Inicial = 01 - Saldo Inicial (Abertura de Exercício);
2. Informar o valor correspondente ao saldo existente em 31/12/2025;

**Resultado Esperado:**

- Crédito passa a ser controlado no exercício de 2026;
- Ficam habilitadas movimentações como arrecadação, revisão ou baixa.

---

### Exemplo 2 – Crédito de Exercício Anterior Não Informado como Saldo Inicial

**Situação:**

- Crédito lançado em 2024;
- Saldo em aberto em 31/12/2025;
- Não informado na tabela de Saldo Inicial no pacote de janeiro de 2026.

**Resultado no e-Sfinge:**

- Crédito considerado **extinto no exercício anterior**;
- ❌ Não são admitidas movimentações em 2026 (baixa, arrecadação, revisão ou estorno);

---

### Exemplo 3 – Reintegração do Crédito Original 

**Situação:**

- Crédito tributário lançado em 2023;
- Inscrito em dívida ativa em 2024;
- Permaneceu inscrito em dívida ativa em 31/12/2025;
- Em razão disso, crédito original **não foi informado como Saldo Inicial no pacote de janeiro de 2026**;
- Em abril de 2026, identificada irregularidade na inscrição em dívida ativa, sem invalidação do crédito original.

**Procedimento no e-Sfinge:**

1. No pacote de abril de 2026, registrar **Baixa do tipo 15– Reintegração do Crédito Original**, extinguindo a dívida ativa;
2. No mesmo pacote (ou no correspondente ao mês do estorno), registrar o valor do crédito original na tabela de Saldo Inicial do Tipo de Saldo Inicial = 02 - Saldo Inicial (Reintegração de Crédito);

**Resultado Esperado:**

- Dívida ativa extinta;
- Crédito original reintegrado ao controle do sistema;
- Crédito passa a admitir novas movimentações no exercício corrente.

### Exemplo 4 – Reintegração de Crédito por Decisão Judicial

**Situação:**

- Crédito extinto e não enviado saldo inicial para 2026;
- Decisão judicial em abril de 2026 determinando a reabertura do crédito;

**Procedimento no e-Sfinge:**

1. Na remessa a ser enviada ao e-Sfinge:
    1. Saldo inicial com “Tipo 03 - Saldo Inicial (Reintegração de Crédito por Decisão Judicial)”, referenciando o lançamento correspondente, com o saldo correspondente à situação atual do crédito.

**Resultado no e-Sfinge:**

- Crédito devidamente apto a receber as movimentações do exercício corrente.


| **Saldo Inicial** | **Quando encaminhar?** |
| --- | --- |
| Abertura do Exercício | Exclusivamente na remessa/pacote de **janeiro **(Tipo de Saldo Inicial = 1 - Saldo Inicial ao ano) |
| Reintegração de Crédito Original | Na remessa/pacote de competência que ocorrer o estorno da dívida ativa Tipo de Saldo Inicial = 02 - Saldo Inicial (Reintegração de Crédito) |

---

|  |  |  |  |  |
| --- | --- | --- | --- | --- |
| Descrição do campo | Tipo | Tamanho | Obrigatório | Observações |
| #Ano do lançamento do Crédito Tributário ou da inscrição Dívida Ativa | Numérico | 4 | Sim | Representa o ano do código sequencial de controle de cada operação, informado em exercícios anteriores através do serviço *Lançamento de Créditos Tributários e Não Tributários/Inscrição em Dívida Ativa.* |
| #Código do lançamento do Crédito Tributário ou Número de Inscrição em Dívida Ativa | Carácter | 16 | Sim | Representa o código sequencial de controle de cada operação, informado em exercícios anteriores através do serviço *Lançamento de Créditos Tributários e Não Tributários/Inscrição em Dívida Ativa.* |
|  Tipo de Saldo Inicial | Numérico | 2 | Depende dos dados | Conforme [Tabela 149 - Tipo de Saldo Inicial](https://jira-tcesc.atlassian.net/wiki/spaces/SD/pages/2154926557/v6.06+-+Tabela+149+-+Tipo+de+Saldo+Inicial). Obrigatório a partir de fevereiro de 2026. Se não informado será considerado o valor 01 - Saldo Inicial (Abertura de Exercício). |
| ~~Saldo~~ | ~~Numérico~~ | ~~16~~ | ~~Sim~~ | ~~Representa o saldo do lançamento no dia 1º de janeiro de cada exercício (ano da remessa).~~~~Enviar sempre com duas casas decimais, com separador '.'.~~~~Não aceita valor 0 (zero).~~ |
| Saldo Principal | Numérico | 16 | Sim | Representa o saldo do valor principal do lançamento no dia 1º de janeiro de cada exercício (ano da remessa).Enviar sempre com duas casas decimais, com separador '.'.Não aceita valor 0 (zero). |
| Saldo Multa | Numérico | 16 | Sim | Representa o saldo de multas do lançamento no dia 1º de janeiro de cada exercício (ano da remessa).Enviar sempre com duas casas decimais, com separador '.', mesmo que o valor seja 0 (zero). |
| Saldo Juros | Numérico | 16 | Sim | Representa o saldo de juros do lançamento no dia 1º de janeiro de cada exercício (ano da remessa).Enviar sempre com duas casas decimais, com separador '.', mesmo que o valor seja 0 (zero). |
| Saldo Atualização Monetária | Numérico | 16 | Sim | Representa o saldo de atualizações monetárias do lançamento no dia 1º de janeiro de cada exercício (ano da remessa).Enviar sempre com duas casas decimais, com separador '.', mesmo que o valor seja 0 (zero). |


## **Revisão de Valor de Lançamento de Créditos Tributários e de Dívidas Ativas**

*Capta as informações a respeito de alterações no valor original dos lançamentos dos créditos tributários e não tributários podendo corresponder a incrementos ou reduções por estornos originados em erros de lançamentos.*

*Os estornos diferem dos cancelamentos, que devem ser enviados na tabela de baixa dos créditos tributários.*

*Deve ser utilizado também nos casos de atualização monetária do principal. No caso de juros, multa ou atualização monetária, deve ser enviado mensalmente ou conforme lei municipal.*

|  |  |  |  |  |
| --- | --- | --- | --- | --- |
| Descrição do campo | Tipo | Tamanho | Obrigatório | Observações |
| #Ano do lançamento do Crédito Tributário ou da Dívida Ativa | Numérico | 4 | Sim | Representa o ano do código sequencial de controle de cada operação.*Detalhes verifique neste mesmo campo em: Lançamento de Créditos Tributários/Inscrição em Dívida Ativa* |
| #Código do lançamento do Crédito Tributário ou Número de Inscrição em Dívida Ativa | Carácter | 16 | Sim | Representa o código sequencial de controle de cada operação.*Detalhes verifique neste mesmo campo em: Lançamento de Créditos Tributários/Inscrição em Dívida Ativa* |
| #Código Sequencial da Revisão | Numérico | 16 | Sim | Representa o código sequencial de controle de cada operação de revisão. |
| Tipo Revisão de Lançamento de Créditos Tributários e de Dívidas Ativas | Numérico | 2 | Sim | Indica o tipo de revisão conforme [Tabela 110 - Tipo Revisão de Lançamento de Créditos Tributários e de Dívidas Ativas](https://jira-tcesc.atlassian.net/wiki/spaces/SD/pages/2154926017/v6.06+-+Tabela+110+-+Tipo+Revis+o+de+Lan+amento+de+Cr+ditos+Tribut+rios+e+de+D+vidas+Ativas) |
| Data da Revisão | Data |  | Sim | Corresponde à data em que ocorreu a revisão do crédito originalmente lançado ou da dívida ativa. |
| Valor da Operação de Revisão | Numérico | 16 | Sim | Corresponde ao valor de cada operação de revisão, devendo ser informada apenas a diferença.Exemplos: Se o valor original era 100 e a revisão foi efetuada para 80, o valor da operação de revisão é a diferença, ou seja, 20 com Indicativo de Redução do Lançamento do Crédito Tributário = S.Enviar sempre com duas casas decimais, com separador '.', mesmo que o valor seja 0. |
| Indicativo de Redução do Lançamento do Crédito Tributário ou da Dívida Ativa | Carácter | 1 | Sim | Indica se houve redução no lançamento tributário ou na dívida ativa.Se “S” o valor reduzirá o saldo do lançamento e “N”, incrementará o saldo do lançamento.Somente aceita "S" para o tipo revisão (Tabela 110):01 - Revisão por Erro ou Estorno de Lançamento 04 - Revisão por atualização monetária 05 - Estorno de Revisão por Multa 06 - Estorno de Revisão por Juros 07 - Estorno de Revisão por Atualização monetária |
| Motivo da Revisão | Carácter | 512 | Depende dos Dados | Indica o motivo de cada operação de revisão.Obrigatório apenas quando o Tipo de Revisão for 01 - Revisão por Erro de Lançamento.Deve incluir fundamento legal ou ato jurídico correspondente. |
| CPF do Responsável pela Operação | Numérico | 11 | Sim | Indica o CPF do usuário/agente público responsável por realizar a operação de revisão.No caso de lançamentos ou cancelamentos em massa, efetuados pelo sistema, deve ser enviado o CPF de quem efetuou o comando. Caso não haja essa informação, deve ser enviado o CPF do gestor público responsável pelo setor que efetuou a operação. |



## **Revisão de Tipo de Crédito Tributário e de Dívida Ativa**

*Capta as informações a respeito das revisões no Tipos de Créditos original dos lançamentos dos créditos tributários e não tributários.*


| Descrição do campo | Tipo | Tamanho | Obrigatório | Observações |
| --- | --- | --- | --- | --- |
| #Ano do lançamento do Crédito Tributário ou da Dívida Ativa | Numérico | 4 | Sim | Representa o ano do código sequencial de controle de cada operação.*Detalhes verifique neste mesmo campo em: Lançamento de Créditos Tributários/Inscrição em Dívida Ativa* |
| #Código do lançamento do Crédito Tributário ou Número de Inscrição em Dívida Ativa | Carácter | 16 | Sim | Representa o código sequencial de controle de cada operação.*Detalhes verifique neste mesmo campo em: Lançamento de Créditos Tributários/Inscrição em Dívida Ativa* |
| Data da Alteração | Data |  | Sim | Corresponde a Data em que foi realizada a Alteração do Tipo de Crédito. |
| Tipo de Crédito | Numérico | 2 | Sim | Indica o tipo de crédito conforme [Tabela 87 – Tipos de Créditos](https://jira-tcesc.atlassian.net/wiki/spaces/SD/pages/2154925657/v6.06+-+Tabela+87+Tipos+de+Cr+ditos). |
| Motivo da Revisão | Carácter | 512 | Obrigatório | Indica o motivo de cada revisão. Deve incluir fundamento legal ou ato jurídico correspondente. |


## **Baixa dos Créditos Tributários**


*~~Capta as informações a respeito das baixas dos créditos tributários e não tributários, exceto às relacionadas à arrecadação. ~~*

*~~Nos casos de inscrição de dívida ativa, devem ser baixado o valor de juros, multas e correões monetárias, devidamente registradas na Revisão de Valor de Lançamento de Créditos Tributários. ~~*

*~~O valor lançado deve zerar o saldo do lançamento original.~~*

Esta seção destina-se ao registro das **baixas de créditos tributários e não tributários**, compreendendo os eventos que **extinguem total ou parcialmente o crédito**, **excetuadas as hipóteses de arrecadação**, as quais devem ser informadas em serviço específico (vide seção Diário de Arrecadação).

A baixa representa a ocorrência de uma **causa legal de extinção do crédito**, tais como cancelamento administrativo, remissão, anistia, prescrição, decadência, decisão administrativa definitiva ou decisão judicial transitada em julgado, nos termos da legislação tributária vigente.

---

### **1. Regras Gerais para o Registro de Baixa**

- A baixa deve estar **sempre vinculada a um lançamento previamente informado** ao e-Sfinge.
- O valor informado na baixa deve ser **compatível com o saldo existente**, sendo **vedado** o registro de baixa em valor **superior ao saldo do crédito ou em créditos que não possuam saldo **.
- **TODAS** as baixas **ocorridas em determinada competência** devem ser **integralmente informadas no respectivo pacote de referência**, observando o caráter **mensal** do envio das informações.

**ALERTA – Saldo Negativo**

O registro de baixa que resulte em **saldo negativo do crédito** aciona a **Regra de Consistência CON737 – Impede o recebimento de Créditos Tributários que tenham o Saldo Negativo**, de **severidade impeditiva**, impossibilitando o recebimento da remessa pelo sistema e-Sfinge. (Para maiores informações acesse a seção “Regas de Consistência” deste Manual).

### **2. Inscrição em Dívida Ativa**

Nos casos de **inscrição em dívida ativa (Tipo de Baixa - 10, **conforme ****Tabela 88 - Tipos de Baixa dos Créditos Tributários e Não Tributários**)**, a baixa deve abranger, de forma **integral e consolidada**, todos os componentes do crédito, compreendendo:

- valor principal;
- juros;
- multas;
- correções monetárias.

Os acréscimos legais somente poderão ser baixados quando **previamente constituídos e registrados por meio de Revisão de Valor do Lançamento**.

A baixa de crédito para inscrição em dívida ativa deve, obrigatoriamente, **zerar o saldo total do crédito original**, não sendo admitida baixa parcial que resulte em saldo remanescente, quando o Tipo de Crédito (Tabela 87) for igual a: 01 - IPTU; 02 - ITBI (URBANO); 03 - ISS; 05 - TAXAS; 06 - COSIP; 11 - ITBI (RURAL); 12 - ITBI SOBREPARTILHA OU ITBI DIREITOS REAIS; 13 - IPTU DECORRENTES DE LEILÃO; 14 - ISS (RETIDO); ou 15 - ISS SIMPLES NACIONAL (Lei Complementar nº 123/2006).

### 3. Estorno de Baixa

O estorno de baixa destina-se a reverter, total ou parcialmente, uma baixa anteriormente registrada de forma indevida ou em valor incorreto.

O estorno deve estar vinculado a uma baixa originalmente registrada, sendo vedado o estorno em valor superior ao saldo existente de baixa para o respectivo código de lançamento (vide CON 736).

Nos casos em que se faça necessário estornar inscrição em dívida ativa, **realizada em exercício anterior** e que, por essa razão, tenha sido **informada na Tabela de Saldos Iniciais**, deverá ser observada a sistemática específica de controle de saldos adotada pelo e-Sfinge.

Nessa hipótese, o procedimento correto consiste em:

a) registrar **Baixa do tipo 15– Reintegração do Crédito Original**, com a finalidade de **baixar integralmente a dívida ativa atualmente existente, realizando a vinculação com o Lançamento de Crédito original**;  
b) reintegrar o saldo do crédito original (vide orientações constantes na seção** Saldos Iniciais - Reintegração de Créditos**)~~.~~

É **vedado** o estorno direto da baixa da dívida ativa nesses casos, tendo em vista a necessidade de preservação da **integridade histórica dos saldos**.

A adoção da Baixa do tipo 15 e da correspondente reintegração do Crédito Original (Tabela - Saldo Inicial) é **obrigatória** para garantir a coerência entre os registros de créditos tributários, dívida ativa e saldos controlados no sistema.

ALERTAS

- A baixa do tipo 15 - Reintegração do Crédito Original só pode ser utilizada para Créditos Lançados com o tipo de Operação “03 - Inscrição em Dívida Ativa”
- o envio da Baixa do tipo 15 – Reintegração do Crédito Original e dos Saldos Iniciais – Reintegração de Créditos **deve ser realizado obrigatoriamente na mesma remessa.**
- O controle desta operação será realizado por regra de consistência. (CON XXX)

#### 3.1.1. Quando é vedada a utilização da Reintegração de Saldo?

Quando a inscrição em dívida ativa decorrer de crédito que seja inválido, nulo, tenha sido definitivamente desconstituído ou que não seja mais passível de ser exigível, **não é admitida **a reintegração do crédito original. 

Nessas hipóteses, o procedimento correto consiste na **baixa da dívida ativa por cancelamento**, devendo o crédito ser considerado **definitivamente extinto**, sem retorno à condição de crédito originário.



### Exemplo 1 – Crédito Prescrito Reconhecido Definitivamente

**Situação:**

- Crédito tributário inscrito em dívida ativa;
- Prescrição reconhecida por decisão administrativa definitiva;
- Crédito sem possibilidade de exigibilidade futura.

**Procedimento Esperado:**

- Registrar baixa da dívida ativa por Prescrição (vide Tabela 88 - Tipos de Baixa dos Créditos Tributários e Não Tributários);
- Não reintegrar crédito por Saldo Inicial Complementar.

**Resultado Esperado:**

- Crédito extinto de forma definitiva;
- Encerramento do controle do crédito no sistema.

---

### Exemplo 2 – Estorno de Dívida Ativa com Reintegração do Crédito Original (Baixa tipo 15)

**Situação:**

- Crédito tributário lançado em 2023;
- Inscrito em dívida ativa em 2024;
- Saldo da Dívida ativa em aberto em 31/12/2025;
- Saldo Inicial do Crédito tributário lançado em 2023 não enviado;
- Informado na **Tabela de Saldos Iniciais** na remessa de janeiro de 2026;
- Em 2026, constatado erro formal na inscrição em dívida ativa, **sem invalidação do crédito original**.

**Procedimento Correto:**

1. Registrar **Baixa do tipo 15– Reintegração do Crédito Original**, com o valor correspondente ao saldo integral da dívida ativa e vincular essa baixa ao Crédito tributário lançado em 2023;
2. Inserir o valor do crédito original na **Tabela de Saldos Iniciais Complementares ** com o Tipo de Saldo Inicial = 02 - Saldo Inicial (Reintegração de Crédito)**.**~~vinculado ao~~****~~Crédito tributário lançado em 2023~~.

**Resultado Esperado:**

- Dívida ativa baixada;
- Crédito original restabelecido;
- Saldo coerente e passível de novas movimentações.

---


|  |  |  |  |  |
| --- | --- | --- | --- | --- |
| **Descrição do campo** | **Tipo** | **Tamanho** | **Obrigatório** | **Observações** |
| #Ano do lançamento do Crédito Tributário ou da Dívida Ativa | Numérico | 4 | Sim | Representa o ano do código sequencial de controle de cada operação.Detalhes verifique neste mesmo campo em: Lançamento de Créditos Tributários/Inscrição em Dívida Ativa |
| #Código do lançamento do Crédito Tributário ou Número de Inscrição em Dívida Ativa | Carácter | 16 | Sim | Representa o código sequencial de controle de cada operação.Detalhes verifique neste mesmo campo em: Lançamento de Créditos Tributários/Inscrição em Dívida Ativa |
| #Código Sequencial de Operação de Baixa. | Numérico | 16 | Sim | Representa o código sequencial de controle de cada operação de baixa. |
| Tipo da Baixa | Numérico | 2 | Sim | Indica o tipo de baixa conforme [Tabela 88 – Tipos de Baixa dos Créditos Tributários e Não Tributários](https://jira-tcesc.atlassian.net/wiki/spaces/SD/pages/2154925672/v6.06+-+Tabela+88+Tipos+de+Baixa+dos+Cr+ditos+Tribut+rios+e+N+o+Tribut+rios). |
| Data da Baixa | Data |  | Sim | Corresponde à data da baixa do crédito. |
| Valor da Operação de Baixa | Numérico | 16 | Sim | Corresponde ao valor de cada operação.Para os casos de Tipo da Baixa = 10 - Inscrição em Dívida Ativa o Valor da Operação de Baixa deve ser o valor do saldo original com juros, multas e correões monetárias e deve ser igual ao Valor do Lançamento do Crédito Tributário ou da Dívida Ativa do lançamento tributário da dívida ativa (Tipo de Operação 03 - Dívida Ativa (Inscrição ou Saldos de Exercícios Anteriores)).Enviar sempre com duas casas decimais, com separador '.', mesmo que o valor seja 0. |
| Indicativo de Estorno da Baixa | Carácter | 1 | Sim | Indica se houve estorno da operação de baixa.Se “N” registra uma baixa normal. Se “S” reduz ou anula valor de baixa anteriormente informada com valor errôneo. |
| Motivo da Baixa | Carácter | 512 | Sim | Indica o motivo de cada operação de baixa.Deve indicar fundamento legal ou ato jurídico correspondente. |
| Novo Ano do lançamento do Crédito Tributário ou da Dívida Ativa (Ano da Dívida Ativa) | Numérico | 4 | Depende dos dados | Representa o ano do código sequencial de controle de cada operação.Serve para vincular o Lançamento do Crédito Tributário original com Lançamento da Dívida Ativa.*Detalhes verifique neste mesmo campo em: Novo Código do lançamento do Crédito Tributário ou Número de Inscrição em Dívida Ativa (Número da Dívida Ativa).* |
| Novo Código do lançamento do Crédito Tributário ou Número de Inscrição em Dívida Ativa  (Número da Dívida Ativa) | Carácter | 16 | Depende dos dados | Representa o código sequencial de controle de cada operação. Serve para vincular o Lançamento do Crédito Tributário original com Lançamento da Dívida Ativa ou outro lançamento derivado.Obrigatório se: - Tipo da Baixa = 10 - Inscrição em Dívida Ativa e o Lançamento do Crédito Tributário ou Número de Inscrição em Dívida Ativa deve ser do Tipo de Operação 03 - Dívida Ativa (Inscrição ou Saldos de Exercícios Anteriores). Neste caso o valor do campo “Valor da Operação de Baixa” deve ser igual ao “Valor do Lançamento do Crédito Tributário ou da Dívida Ativa” do lançamento referenciado neste campo. - Tipo da Baixa = 15 – Reintegração do Crédito Original e dos Saldos Iniciais – Reintegração de Créditos e o o Lançamento do Crédito Tributário ou Número de Inscrição em Dívida Ativa deve ser diferente do Tipo de Operação 03 - Dívida Ativa (Inscrição ou Saldos de Exercícios Anteriores). Caso o registro da Inscrição em Dívida Ativa  esteja vinculado a uma baixa do tipo 10 - Inscrição em Dívida Ativa, *o Novo **Código do lançamento do Crédito Tributário ou Número de Inscrição em Dívida Ativa (Número da Dívida Ativa)** e  o Novo **Ano do lançamento do Crédito Tributário ou da Dívida Ativa (Ano da Dívida Ativa) **deverá ser idêntico ao ano e **código do lançamento do crédito tributário** informado, à época, na respectiva **baixa do tipo 10**.* Para os demais casos é opcional. |
| CPF do Responsável pela Operação | Numérico | 11 | Sim | Indica o CPF do usuário/agente público responsável por realizar a operação de baixa.No caso de lançamentos ou cancelamentos em massa, efetuados pelo sistema, deve ser enviado o CPF de quem efetuou o comando. Caso não haja essa informação, deve ser enviado o CPF do gestor público responsável pelo setor que efetuou a operação. |


## **Cobrança de Dívida Ativa**

*Capta as informações a respeito do Protesto/Execução/Parcelamento da dívida ativa. *

*Deve estar vinculado a um Lançamento do Crédito Tributário com o Tipos de Operações dos Créditos Tributários e Não Tributários = 03 - Dívida Ativa (Inscrição ou Saldos de Exercícios Anteriores) e para lançamentos posteriores a 2025 uma Baixa dos Créditos Tributários com Tipo da Baixa = 10 - Inscrição em Dívida Ativa.*

|  |  |  |  |  |
| --- | --- | --- | --- | --- |
| **Descrição do campo** | **Tipo** | **Tamanho** | **Obrigatório** | **Observações** |
| #Ano do lançamento do Crédito Tributário ou da Dívida Ativa | Numérico | 4 | Sim | Representa o ano do código sequencial de controle de cada operação.*Detalhes verifique neste mesmo campo em: Lançamento de Créditos Tributários/Inscrição em Dívida Ativa* |
| #Código do lançamento do Crédito Tributário ou Número de Inscrição em Dívida Ativa | Carácter | 16 | Sim | Representa o código sequencial de controle de cada operação.*Detalhes verifique neste mesmo campo em: Lançamento de Créditos Tributários/Inscrição em Dívida Ativa* |
| Tipo Cobrança | Numérico | 2 | Sim | Indica o tipo de cobrança realizado (protestada, executada e parcelada) conforme [Tabela 111 - Tipo Cobrança](https://jira-tcesc.atlassian.net/wiki/spaces/SD/pages/2154926032/v6.06+-+Tabela+111+-+Tipo+Cobran+a) |
| #Número/Ano do Tipo de Cobrança  | Carácter | 50 | Sim | Representa o número e o ano de controle da operação de cobrança da dívida ativa (protesto extrajudicial, execução fiscal ou parcelamento) Deve ser enviado o número atribuído internamente pelo sistema municipal. Nos casos em que o número interno seja igual ao externo, deve ser enviado o último. Ex: Número de controle do protesto extrajudicial é o mesmo que o utilizado no CENPROT.Pode repetir para tipos de cobrança diferentes. |
| Data da Cobrança | Data |  | Sim | Corresponde à data em que o tipo de cobrança foi realizado. Deve corresponder, no protesto extrajudicial, à data de envio; na execução fiscal, à data do ajuizamento; e no parcelamento, à data do acordo. |
| Tipo de Parcelamento | Numérico | 2 | Depende dos dados | Indica se o parcelamento foi ordinário ou extraordinário conforme [Tabela 112 - Tipo de Parcelamento](https://jira-tcesc.atlassian.net/wiki/spaces/SD/pages/2154926047/v6.06+-+Tabela+112+-+Tipo+de+Parcelamento)Campo obrigatório se o Tipo de Cobrança for 03 - Parcelamento Para os demais Tipos de Cobrança, não deve ser informado e, se informado, será ignorado. |
| Número de Parcelas | Numérico | 4 | Depende dos dados | Indica o número total de parcelas de um determinado parcelamento.Campo obrigatório se o Tipo de Cobrança for 03 - Parcelamento.Somente pode ser enviado para as situações de cobrança: 01 - Ativo e 08 - Atualização de Dados |
| Legislação do Parcelamento | Carácter | 100 | Depende dos dados | Corresponde à legislação instituidora do parcelamento.Campo obrigatório se o Tipo de Cobrança for 03 - Parcelamento |


## **Início do prazo prescricional**

*Capta as informações a respeito do marco inicial do prazo prescricional.*

*Deve ser informado no momento da criação da dívida ativa e em caso de atualização da data do início do prazo prescricional.*

*Deve considerar todas as causas interruptivas do prazo prescricional (art. 174 Lei 5.172/66 - Código Tributário Nacional).*

*Com relação ao IPTU, deve ser informado conforme Tema 980 do STJ.*

*Deve estar vinculado a um Lançamento do Crédito Tributário com o Tipos de Operações dos Créditos Tributários e Não Tributários = 03 - Dívida Ativa (Inscrição ou Saldos de Exercícios Anteriores) e para lançamentos posteriores a 2025 uma Baixa dos Créditos Tributários com Tipo da Baixa = 10 - Inscrição em Dívida Ativa.*

*O envio de uma segundo registro do “Início do prazo prescricional” para o mesmo lançamento será considerada o cancelamento da primeira.*

|  |  |  |  |  |
| --- | --- | --- | --- | --- |
| **Descrição do campo** | **Tipo** | **Tamanho** | **Obrigatório** | **Observações** |
| #Ano do lançamento do Crédito Tributário ou da Dívida Ativa | Numérico | 4 | Sim | Representa o ano do código sequencial de controle de cada operação.*Detalhes verifique neste mesmo campo em: Lançamento de Créditos Tributários/Inscrição em Dívida Ativa* |
| #Código do lançamento do Crédito Tributário ou Número de Inscrição em Dívida Ativa | Carácter | 16 | Sim | Representa o código sequencial de controle de cada operação.*Detalhes verifique neste mesmo campo em: Lançamento de Créditos Tributários/Inscrição em Dívida Ativa* |
| #Data de Atualização do prazo | Data |  | Sim | A Data em que o prazo prescricional foi atualizado. |
| Data do início do prazo prescricional | Data |  | Sim | Corresponde ao marco inicial do prazo prescricional. Deve considerar todas as causas interruptivas do prazo prescricional (art. 174 Lei 5.172/66 - Código Tributário Nacional).Com relação ao IPTU, deve ser informado conforme Tema 980 do STJ. |


## **Certidão de Dívida Ativa**

*Capta as informações a respeito da Certidão de Dívida Ativa (CDA). Deve estar vinculado a um **Tipo de Cobrança da Dívida Ativa** (Protesto/Execução/Parcelamento).*

*O envio de uma segunda CDA para o mesmo lançamento será considerada o cancelamento da primeira.*

*Envio obrigatório nos casos do Lançamento do Crédito Tributário estar vinculado aos tipos de cobrança: 01 - Protesto Extrajudicial e 02 - Execução Fiscal.*

|  |  |  |  |  |
| --- | --- | --- | --- | --- |
| **Descrição do campo** | **Tipo** | **Tamanho** | **Obrigatório** | **Observações** |
| #Ano do lançamento do Crédito Tributário ou da Dívida Ativa | Numérico | 4 | Sim | Representa o ano do código sequencial de controle de cada operação.*Detalhes verifique neste mesmo campo em: Lançamento de Créditos Tributários/Inscrição em Dívida Ativa* |
| #Código do lançamento do Crédito Tributário ou Número de Inscrição em Dívida Ativa | Carácter | 16 | Sim | Representa o código sequencial de controle de cada operação.*Detalhes verifique neste mesmo campo em: Lançamento de Créditos Tributários/Inscrição em Dívida Ativa* |
| #Número/Ano da Certidão de Dívida Ativa | Carácter | 50 | Sim | Representa o número e o ano da CDA. |
| Data da emissão da Certidão de Dívida Ativa | Data |  | Sim | Corresponde à data em que a CDA vigente foi emitida. |

## 

## **Situação do Tipo de Cobrança de Dívida Ativa**

*Capta as informações a respeito da cobrança da dívida ativa.*

*Deve estar vinculado a um Tipo de Cobrança da Dívida Ativa (Protesto/Execução/Parcelamento).*

*Caso ocorra mais de uma situação no mesmo dia, enviem na ordem cronológica, sendo o último registro o que será considerado como a última situação.*

|  |  |  |  |  |
| --- | --- | --- | --- | --- |
| **Descrição do campo** | **Tipo** | **Tamanho** | **Obrigatório** | **Observações** |
| #Ano do lançamento do Crédito Tributário ou da Dívida Ativa | Numérico | 4 | Sim | Representa o ano do código sequencial de controle de cada operação.*Detalhes verifique neste mesmo campo em: Lançamento de Créditos Tributários/Inscrição em Dívida Ativa* |
| #Código do lançamento do Crédito Tributário ou Número de Inscrição em Dívida Ativa | Carácter | 16 | Sim | Representa o código sequencial de controle de cada operação.*Detalhes verifique neste mesmo campo em: Lançamento de Créditos Tributários/Inscrição em Dívida Ativa* |
| #Número/Ano do Tipo de Cobrança | Carácter | 50 | Sim | Representa o número e o ano de controle da operação de cobrança (número atribuído internamente pelo sistema municipal). Deve ser enviada para um cada tipo de cobrança (protesto extrajudicial da dívida, execução fiscal ou parcelamento)Pode duplicar para tipos de cobrança diferentes. |
| #Data de Atualização da Situação | Data |  | Sim | Corresponde à data de atualização da situação do tipo de cobrança.Exemplo: Quando for efetuado um parcelamento, deve ser enviada a data do acordo de parcelamento. Posteriormente, quando esse parcelamento for cancelado, deve ser enviada a data de cancelamento do acordo. |
| #Tipo Situação de Cobrança de Dívida Ativa | Carácter | 2  | Sim | Indica a situação da cobrança da dívida ativa conforme a [Tabela 113 - Tipo Situação de Cobrança de Dívida Ativa](https://jira-tcesc.atlassian.net/wiki/spaces/SD/pages/2154926062/v6.06+-+Tabela+113+-+Tipo+Situa+o+de+Cobran+a+de+D+vida+Ativa) |
| Número do Protesto Extrajudicial ou da Execução Fiscal | Carácter | 30 | Depende dos dados | Representa o número do processo de cobrança.No caso do protesto extrajudicial, deve ser enviado o número de identificação na CENPROT; no de execução fiscal, o número da ação no Tribunal de Justiça.Somente pode ser enviado para as situações 01 - Ativo e 08 - Atualização de Dados.Não enviar para os demais casos. |
| Descrição da Situação | Carácter | 2000 | Não | Representa o detalhamento detalhamento do tipo da situação da cobrança. Exemplo: Se a execução fiscal for extinta por falta de interesse de agir, esse motivo deve ser detalhado nesse campo.Não enviar para as Situações: 01 - Ativo e 04 - Quitado |


## **Diário Geral de Arrecadação**

*Capta dados do diário de arrecadação (tributos inscritos e não inscritos em dívida ativa) contendo data da arrecadação, contribuinte, tipo de tributo, classificação contábil, valores, descrição e outros dados necessários para identificar a receita arrecadada.*

**Arrecadação de Créditos Não Informados no Saldo Inicial**

**Excepcionalmente**, será admitido o registro de arrecadação referente a **pagamentos realizados a maior ou em duplicidade**, ainda que vinculados a créditos que **não constem na Tabela de Saldo Inicial**.

Essa exceção aplica-se exclusivamente aos casos em que não represente impacto no** saldo devido do crédito informado**, não integrando, portanto, o cálculo do saldo do crédito tributário ou não tributário, conforme fórmula do cálculo do saldo apresenta na seção Inicial deste Manual.

Ressalta-se que, fora dessa hipótese, **não é permitido o envio de movimentações no exercício corrente** para créditos de exercícios anteriores que **não tenham sido previamente informados na Tabela de Saldo Inicial**.

|  |  |  |  |  |
| --- | --- | --- | --- | --- |
| **Descrição do campo** | **Tipo** | **Tamanho** | **Obrigatório** | **Observações** |
| #Ano do Código Sequencial da Operação de Arrecadação | Numérico | 4 | Sim | Representa o ano do código sequencial de cada operação de arrecadação. |
| #Código Sequencial da Operação de Arrecadação | Carácter | 16 | Sim | Representa o código sequencial de cada operação arrecadada. |
| Tipo de Arrecadação | Numérico | 2 | Sim | Identifica o tipo de arrecadação conforme a [Tabela 89 – Tipos de Arrecadação](https://jira-tcesc.atlassian.net/wiki/spaces/SD/pages/2154925687/v6.06+-+Tabela+89+Tipos+de+Arrecada+o). |
| Data da Operação | Data |  | Sim | Corresponde à data da operação de cada receita arrecadada. |
| Código da Especificação da Receita/Natureza da Receita | Numérico | 8 | Sim | Representa a especificação orçamentária da natureza da receita.Informar apenas as Naturezas de Receita valorizáveis e escrituráveis, ou seja, as analíticas, conforme planilha "NR" do [Anexo II da Portaria STN nº 642, de 20 de setembro de 2019 (Leiaute da MSC)](https://sisweb.tesouro.gov.br/apex/f?p=2501:9::::9:P9_ID_PUBLICACAO_ANEXO:22327). |
| Ano do lançamento do Crédito Tributário ou da Dívida Ativa | Numérico | 4 | Sim | *Detalhes verifique neste mesmo campo em: Lançamento de Créditos Tributários/Inscrição em Dívida Ativa* |
| Código do lançamento do Crédito Tributário ou da Dívida Ativa | Carácter | 16 | Sim | *Detalhes verifique neste mesmo campo em: Lançamento de Créditos Tributários/Inscrição em Dívida Ativa* |
| Valor do Crédito Tributário Arrecadado | Numérico | 16 | Sim | Representa o valor de cada operação de crédito tributário.Representa o valor arrecadado do crédito tributário de acordo com a natureza da receita.Exemplo: Arrecadação do principal, juros e multa de IPTU inscrito em dívida ativa. Nesse caso, deve ser enviada uma arrecadação para o principal (1.1.1.2.50.0.1) e outra para multas e juros de mora (1.1.1.2.50.0.2), desde que a legislação pertinente não diferencie a destinação das Multas da destinação dos Juros de Mora.Enviar sempre com duas casas decimais, com separador '.', mesmo que o valor seja 0. |
| #Número/Ano do Tipo de Cobrança | Carácter | 50 | Depende dos dados | Representa o número e o ano de controle da operação de cobrança (número atribuído internamente pelo sistema municipal).Obrigatório nos casos de arrecadação de dívida ativa estiver vinculado a um Tipo de Cobrança da Dívida Ativa (Protesto/Execução/Parcelamento) |
| Número da Parcela | Numérico | 4 | Depende dos dados | Representa a parcela referente ao valor arrecadado.Exemplo: Um contribuinte efetuou um acordo para pagar suas dívidas de IPTU de 2020 de em 5 parcelas mensais. Quando pagar a 1ª parcela, além dos valores arrecadados, deve ser enviado o número da parcela do parcelamento a que esse pagamento se refere.Obrigatório nos casos de arrecadação de dívida ativa estiver vinculado a um Tipo de Cobrança da Dívida Ativa (Protesto/Execução/Parcelamento) e tiver um parcelamento ativo. |


## **Estorno ou Restituição de Receita do Diário Geral de Arrecadação**

*Capta dados dos estornos e das restituições da receita orçamentária arrecadada. *

*Considera-se estorno apenas aquelas operações cujo registro original foi objeto de falha no momento da inserção do dado. *

*Já a restituição consiste na devolução total ou parcial de receitas orçamentárias que foram recolhidas a maior ou indevidamente, as quais, em observância aos princípios constitucionais da capacidade contributiva e da vedação ao confisco, devem ser devolvidas.*

|  |  |  |  |  |
| --- | --- | --- | --- | --- |
| **Descrição do campo** | **Tipo** | **Tamanho** | **Obrigatório** | **Observações** |
| #Ano do Código Sequencial da Operação de Arrecadação | Numérico | 4 | Sim | Representa o ano do código sequencial de cada operação de arrecadação. |
| #Código Sequencial da Operação de Arrecadação | Carácter | 16 | Sim | Representa o código sequencial de cada operação arrecadada. |
| #Código Sequencial do Estorno ou Restituição | Numérico | 16 | Sim | Representa o código sequencial de controle de cada operação estornada. |
| Indicativo de Estorno | Carácter | 1 | Sim | Indica se o movimento se refere a estorno ou à restituição da receita. Se “S” indica um estorno de arrecadação da receita. Se “N” indica uma restituição de receita. |
| Data do Estorno ou Restituição | Data |  | Sim | Corresponde à data do estorno ou da restituição.Deve ser superior ou igual a Data da Operação. |
| Valor do Estorno ou Restituição | Numérico | 16 | Sim | Corresponde ao valor de cada operação de estorno ou de restituição.Enviar sempre com duas casas decimais, com separador '.', mesmo que o valor seja 0. |
| Motivo do Estorno ou Restituição | Carácter | 512 | Sim | Representa o motivo de cada operação de estorno ou de restituição. |
