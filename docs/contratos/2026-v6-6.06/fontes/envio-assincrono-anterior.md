# Tributário \- Envio assíncrono

## Resumo

Para o módulo tributário os serviços de envio parcial serão processados de forma assíncrona.

O Fluxo continua sendo Iniciar envio, Enviar Parcial e Finalizar.

Para o serviço de envio parcial por lote após o e-Sfinge Online receber os registros será gerado um número de lote e adicionado esses registros em uma fila de processamento, com esse número de lote aplicação das empresas pode consultar o status de processamento. Será possível enviar quantos lotes forem necessários para uma chave de pacote iniciada.

Finalizar envio, somente será possível finalizar quando todos os lotes forem processados com sucesso.


## Serviços da API [SWAGGER UI](https://api.virtual.testing.tce.sc.gov.br/esfingeonline/swagger-ui.html)


- **Iniciar Envio**

Esse serviço continua igual, sendo necessário iniciar a transmissão para receber a *"chave do pacote"* para ser utilizado no envio parcial.

O sistema terá dois comportamentos, dependendo do cancelamentoAutomatico.

Se ***SIM ***ou não informar nenhum valor neste campo, o sistema terá o comportamento de em caso de erro o sistema irá descartar automaticamente os lotes restantes e todo o pacote será cancelado.

Se ***NAO***, em caso de erro no lote os demais serão cancelados, mas os dados de lotes anteriores não serão removidos.

O não envio do cancelamentoAutomatico ou envio com ***SIM,*** faz com que o comportamento do sistema seja o mesmo de antes da criação deste campo (antes do dia 19/12/2023)

Se for usada a opção de cancelamentoAutomatico= ***NAO, ***recomendamos que para facilitar a organização dos envios e o descarte parcial que cada lote possua somente um assunto.


- **Enviar Parcial em Lote**

Esse serviço recebe os dados tributários e retornará um número de lote, para que possa ser consultado o status de seu processamento.

Retorno:


- **Consultar Por Número de Lote**

Serviço responsável por apresentar a situação do lote e os possíveis erros de todo o processamento concatenado.

**Retorno:**


- **Finalizar Envio**

Serviço igual aos outros módulos, porem somente é possível finalizar uma transmissão depois que todos os lotes enviados estiverem na situação  *"PROCESSADO\_SUCESSO"*

Caso o cancelamentoAutomatico seja ***SIM, **ou não informado,* e ocorrer um erro de validação (regra de consistência) na finaliza, todos os dados do pacote serão removidos.

Caso o cancelamentoAutomatico seja ***NAO*** e ocorrer um erro, os dados não serão removidos e poderão ser adicionado novos ou um subassunto pode ser descartado.


- **Descartar SubAssunto (parcial)**

Caso o cancelamentoAutomatico seja ***NAO***, o sistema aceitará que possa ser descartado todos os dados enviados de um determinado subAssunto do tributário:

**CADASTRO\_CONTRIBUINTE**

**CADASTRO\_IMOBILIARIO**

**CADASTRO\_PROPRIEDADE\_IMOBILIARIA**

**LANCAMENTO\_CREDITO\_TRIBUTARIO**

**REVISAO\_LANCAMENTO\_CREDITO\_TRIBUTARIO**

**BAIXA\_CREDITO\_TRIBUTARIO**

**COBRANCA\_DIVIDA\_ATIVA	**

**DIARIO\_ARRECADACAO**

**ESTORNO\_DIARIO\_ARRECADACAO**

**CERTIDAO\_DIVIDA\_ATIVA**

**SITUACAO\_TIPO\_COBRANCA\_DIVIDA\_ATIVA**

**INICIO\_PRAZO\_PRESCRICIONAL**

Sendo necessário informar a chave do pacote e o subAssunto que será descartado.

Todos os lotes deverão estar processados para acionar o descarte parcial.

Os dados solicitados serão descartados se isso não gerar algum problema de dependência, mas os lotes sempre serão cancelados.

Mesmo que seja solicitado um descarte parcial de dados não existentes no lote, os lotes não presados serão descartados e o sistema retornará sucesso no descarte.

Importante destacar que a chamada da Descartar SubAssunto não muda a situação dos lotes ***PROCESSADO\_SUCESSO**,* mesmo que todos os dados do lote sejam removidos pela descartar, a situação será a original, ***PROCESSADO\_SUCESSO***.

As únicas situações que a descartar muda é  ***ABORTADO\_AGUARDANDO\_DESCARTE\_PARCIAL** *para ***ABORTADO***.

Se tiver pacotes com a situação ***RECEBIDO** *a chamada da descartar retornará erro.


- **Serviços Gerais**
    - **Consultar Status dos lotes por chave do pacote**

Serviço para consultar todos os lotes de uma chave de pacote.

## **Quais situações o lote pode assumir?**

- ***RECEBIDO** = Lote foi recebido pelo e-Sfinge Online e está aguardando processamento.*
- ***PROCESSADO\_SUCESSO** = Todos os registros do lote foram processados com sucesso.*
- ***PROCESSADO\_ERRO\_NEGOCIO** = Foram processados os registros e concatenados os erros de negócio encontrados pelo e-Sfinge Online, código da remessa foi cancelado. *
- ***PROCESSADO\_ERRO\_INTERNO** = Algo inesperado aconteceu no processamento do lote, código da remessa foi cancelado. Favor abrir chamado para nosso helpdesk com o detalhe do erro e identificador da remessa.*
- **ABORTADO\_ERRO\_LOTE\_ANTERIOR **= Quando um dos lotes anteriores da mesma *"chave de pacote"* tenha ficado com a situação ***PROCESSADO\_ERRO\_NEGOCIO*** ou ***PROCESSADO\_ERRO\_INTERNO ou ABORTADO\_AGUARDANDO\_DESCARTE\_PARCIAL***, todos os próximos lotes serão abortados, pois o código da remessa foi cancelado.
- ***EM\_PROCESSAMENTO**= Lote sendo processado pelo e-Sfinge Online.*
- **ABORTADO\_AGUARDANDO\_DESCARTE\_PARCIAL =** Quanto ocorrer um erro em um lote e o cancelamentoAutomatico = ***NAO*** o lote atual ficará com essa situação.

Se existir ao menos um pacote com essa situação, novos lotes serão abortados e ficarão com a situação: ***ABORTADO\_ERRO\_LOTE\_ANTERIOR.***

**ABORTADO = ** Quando ocorrer a chamada da Descartar **SubAssunto**o lote que estava na situação ***ABORTADO\_AGUARDANDO\_DESCARTE\_PARCIAL** *mudará seu status para ***ABORTADO***. A situação ***ABORTADO ***permite que novos lotes sejam recebidos e processados.


- **Comportamento do timeout.**

O timeout do pacote do tributário é de 72h sem recebimento de pacotes ou chamada da Descartar SubAssunto. A cada chamada destes endppoint o timeout será resetado.
