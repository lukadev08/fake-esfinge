# Tributário V6 \- Envio assíncrono



### Resumo:

A partir da versão para 2026 do [Layout de Envio](https://jira-tcesc.atlassian.net/wiki/spaces/SD/pages/1512570881/2026+-+Layouts), visando otimizar o processamento dos dados recebidos, implementamos uma alteração no fluxo de envio, incluindo uma nova etapa que marca o início do processamento assíncrono dos dados.

O fluxo atualizado dos envios passa a ser:

1. [**Iniciar Envio**](https://jira-tcesc.atlassian.net/wiki/spaces/SD/pages/1582465028/Tribut+rio+V6+-+Envio+ass+ncrono#Iniciar-Envio%3A)—Inicia a transmissão para receber a *"chave do pacote"* que deve ser utilizada nos envios parciais.
2. [**Enviar Parcial**](https://jira-tcesc.atlassian.net/wiki/spaces/SD/pages/1582465028/Tribut+rio+V6+-+Envio+ass+ncrono#Enviar-Parcial%3A)— Serviço não teve alteração em seu modo de envio, onde é realizado os envios dos dados em lotes, será possível enviar quantos lotes forem necessários para uma chave de pacote iniciada e posterior consulta ao [status de processamento](https://jira-tcesc.atlassian.net/wiki/spaces/SD/pages/1582465028/Tribut+rio+V6+-+Envio+ass+ncrono#Consultar-Por-N%C3%BAmero-de-Lote%3A).
3. [**Iniciar Processamento**](https://jira-tcesc.atlassian.net/wiki/spaces/SD/pages/1582465028/Tribut+rio+V6+-+Envio+ass+ncrono#Iniciar-Processamento%3A)—Nova etapa onde inicia o processamento dos lotes enviados anteriormente.
4. [**Descartar SubAssunto (opcional)**](<https://jira-tcesc.atlassian.net/wiki/spaces/SD/pages/1582465028/Tribut+rio+V6+-+Envio+ass+ncrono#Descartar-SubAssunto-(parcial)%3A>) — Descarta dados enviados de um determinado sub assunto.
5. [**Descartar Lote (opcional)**](<https://jira-tcesc.atlassian.net/wiki/spaces/SD/pages/1582465028/Tribut+rio+V6+-+Envio+ass+ncrono#Descartar-Lote-(opcional)>) — Permite descartar um lote específico que ainda não foi processado (situação **RECEBIDO**). 
6. [**Finalizar Envio**](https://jira-tcesc.atlassian.net/wiki/spaces/SD/pages/1582465028/Tribut+rio+V6+-+Envio+ass+ncrono#Finalizar-Envio%3A) —Finaliza o envio da remessa, somente será possível finalizar quando todos os lotes forem processados com sucesso.

---

### Serviços da API [SWAGGER UI](https://api.virtual.testing.tce.sc.gov.br/esfingeonline/swagger-ui.html?urls.primaryName=Tribut%C3%A1rio)

### **Iniciar Envio:**

**/v6/tributario/iniciarEnvio**

O sistema terá dois comportamentos, dependendo do cancelamentoAutomatico.

Se ***SIM ***ou não informar nenhum valor neste campo, o sistema terá o comportamento de em caso de erro o sistema irá descartar automaticamente os lotes restantes e todo o pacote será cancelado.

Se ***NAO***, em caso de erro no lote os demais serão cancelados, mas os dados de lotes anteriores não serão removidos.

O não envio do cancelamentoAutomatico ou envio com ***SIM,*** faz com que o comportamento do sistema seja o mesmo de antes da criação deste campo (antes do dia 19/12/2023)

**Se for usada a opção de cancelamentoAutomatico= *****NAO, *****recomendamos que para facilitar a organização dos envios e o descarte parcial que cada lote possua somente um assunto.**

Organize cada lote com um único assunto para facilitar descartes parciais.

Retorno:  

```
{
    "chavePacote": "eyJpZGVudGbWVzc2FPbkYWdG9yYSI6IjI5NCIsImFub01lcyI6IjIwMjUwOSJ9"
}
```

---

### **Enviar Parcial:**

**/v6/tributario/enviarParcialLote**

Para o serviço de envio parcial, após o e-Sfinge Online receber os registros será gerado um número de lote, para que possa ser consultado o status de seu processamento após a etapa **3.** **Iniciar Processamento**. 

Retorno:

```
{
    "situacao": "RECEBIDO",
    "numeroLote": 12345,
    "dataEnvio": "2025-10-22T11:28:56.080+00:00",
    "identificadorRemessaOnline": 1234
```

---

### **Iniciar Processamento:**

**/v6/tributario/iniciar-processamento**

Operação que Inicia o processamento dos envios parciais da remessa recebidos pelo e-Sfinge Online.

- Deve ser chamado somente após concluir o envio dos lotes desejados para a mesma **chave de pacote**.
- Não será possível chamar **“Iniciar Processamento”** se já existir um processamento anterior em execução para a mesma **chave de pacote**.
- Enquanto o processamento estiver em execução, utilize [**Consultar Por Número de Lote**](https://jira-tcesc.atlassian.net/wiki/spaces/SD/pages/1582465028/Tribut+rio+V6+-+Envio+ass+ncrono#Consultar-Por-N%C3%BAmero-de-Lote%3A)** ou** [**Consultar Status dos lotes por chave do pacote**](https://jira-tcesc.atlassian.net/wiki/spaces/SD/pages/1582465028/Tribut+rio+V6+-+Envio+ass+ncrono#Servi%C3%A7os-Gerais)para acompanhar o [status do processamento dos lotes](https://jira-tcesc.atlassian.net/wiki/spaces/SD/pages/1582465028/Tribut+rio+V6+-+Envio+ass+ncrono#Quais-situa%C3%A7%C3%B5es-o-lote-pode-assumir%3F).

Retorno:

```json
{
    "situacao": "RECEBIDO",
    "dataEnvio": "2025-10-22T11:29:03.462+00:00",
    "identificadorRemessaOnline": 96517648
}
```

---

### **Consultar Por Número de Lote:**

**/v6/tributario/consultarPorNumeroLote/{numeroLote}**

Serviço responsável por apresentar a situação do lote e os possíveis erros de todo o processamento concatenado.

**Retorno:**

```json
{
    "situacao": "PROCESSADO_SUCESSO",
    "identificadorRemessa": 12345,
    "numeroLote": 12345,
    "dataEnvio": "2025-10-22T11:26:53.277+00:00"
}
```

```json
{
    "situacao": "PROCESSADO_ERRO_NEGOCIO",
    "identificadorRemessa": 12345,
    "numeroLote": 3,
    "dataEnvio": "2025-10-22T11:02:45.777+00:00",
    "retornoErros": {
        "erros": [
            {
                "identificador": 2020066,
                "mensagem": "Data da Baixa do crédito tributário não pode ser anterior ao Lançamento do Crédito Tributário",
                "idRemessaOnline": 12345,
                "idCodigoErro": 2020066,
                "objetoErro": {
                    "identificador": 269772936,
                    "selecionado": false,
                    "identificadorUnidadeGestora": 1234,
                    "identificadorINT_RemessaOnLine": 123456,
                    "identificadorLote": 1,
                    "anoMes": 202509,
                    "anoLancamentoCreditoTributario": 2025,
                    "codigoLancamentoCreditoTributario": "Teste.1",
                    "codigoSequencialOperacaoBaixa": 1,
                    "dataBaixa": "2024-10-01T03:00:00.000+00:00",
                    "tipoBaixa": 1,
                    "valorOperacaoBaixa": 109.79,
                    "indicativoEstornoBaixa": true,
                    "motivoBaixa": "123456",
                    "anoLancamentoCreditoTributarioDividaAtiva": 2025,
                    "codigoLancamentoCreditoTributarioDividaAtiva": "Teste.1",
                    "numeroCpfResponsavelOperacao": 12345678
                  }
            }
        ]
    }
}
```

---

### **Finalizar Envio:**

**/v6/tributario/finalizarEnvio**

Serviço igual aos outros módulos, porem somente é possível finalizar uma transmissão depois que todos os lotes enviados estiverem na situação  ***"PROCESSADO\_SUCESSO"***

- Caso o cancelamentoAutomatico seja ***SIM, **ou não informado,* e ocorrer um erro de validação (regra de consistência) na finaliza, todos os dados do pacote serão removidos.
- Caso o cancelamentoAutomatico seja ***NAO*** e ocorrer um erro, os dados não serão removidos e poderão ser adicionado novos ou um subassunto pode ser descartado.

---

### **Descartar SubAssunto (parcial):**

**/v6/tributario/descarteParcialPorAssunto**

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

---

### **Descartar Lote (opcional)**

**/v6/tributario/descartar-lote**

Permite descartar um lote específico de uma remessa que ainda não iniciou o processamento (situação **RECEBIDO**), informando como parâmetros a chavePacote e o numeroLote que deseja descartar.

Após o descarte, o lote assume a situação **LOTE\_DESCARTADO**.

Regra:

- Somente lotes em situação **RECEBIDO** podem ser descartados por este serviço.

**Atenção:**  
**Caso no **[**Iniciar Envio**](https://jira-tcesc.atlassian.net/wiki/spaces/SD/pages/1582465028/Tribut+rio+V6+-+Envio+ass+ncrono#Iniciar-Envio%3A)** tenha sido utilizada a opção SIM, o lote é descartado permanentemente.**  
**Caso no **[**Iniciar Envio**](https://jira-tcesc.atlassian.net/wiki/spaces/SD/pages/1582465028/Tribut+rio+V6+-+Envio+ass+ncrono#Iniciar-Envio%3A)** tenha sido utilizada a opção NÃO, o lote terá o mesmo comportamento de Descartar SubAssunto (parcial)**

---

### **Serviços Gerais**

- [**Consultar Status dos lotes por chave do pacote**](https://api.virtual.testing.tce.sc.gov.br/esfingeonline/swagger-ui.html?urls.primaryName=Servi%C3%A7os%20Gerais)

**/servicosGerais/consultarStatusLotePorChavePacote/{chavePacote}**

Serviço para consultar todos os lotes de uma chave de pacote.  
  
Retorno:  

```
[
  {
    "situacao": "RECEBIDO",
    "identificadorRemessa": 12345,
    "numeroLote": 1,
    "dataEnvio": "2025-10-22T11:28:28.147+00:00"
  },
    {
    "situacao": "PROCESSADO_SUCESSO",
    "identificadorRemessa": 12345,
    "numeroLote": 2,
    "dataEnvio": "2025-10-22T11:26:53.277+00:00"
  },
  {
    "situacao": "PROCESSADO_ERRO_NEGOCIO",
    "identificadorRemessa": 12345,
    
    "numeroLote": 3,
    "dataEnvio": "2025-10-22T11:02:45.777+00:00"
  }
]
```

---

### **Quais situações o lote pode assumir?**

- ***RECEBIDO** = Lote foi recebido pelo e-Sfinge Online e está aguardando processamento.*
- **LOTE\_DESCARTADO** = Lote descartado manualmente antes de ser processado, através do serviço [/v6/tributario/descartar-lote](<https://jira-tcesc.atlassian.net/wiki/spaces/SD/pages/1582465028/Tribut+rio+V6+-+Envio+ass+ncrono#Descartar-Lote-(opcional)>), somente lotes em situação **RECEBIDO** podem ser descartados. 

Quando ocorrer um erro de negócio em um lote e o cancelamentoAutomatico = NÃO e lote tiver apenas um assunto, nenhum dado será persistido na base, e permitirá o reenvio da informação corrigida sem necessidade de descarte parcial.

- ***PROCESSADO\_SUCESSO** = Todos os registros do lote foram processados com sucesso.*
- ***PROCESSADO\_ERRO\_NEGOCIO** = Foram processados os registros e concatenados os erros de negócio encontrados pelo e-Sfinge Online, código da remessa foi cancelado. *
- ***PROCESSADO\_ERRO\_INTERNO** = Algo inesperado aconteceu no processamento do lote, código da remessa foi cancelado. Favor abrir chamado para nosso helpdesk com o detalhe do erro e identificador da remessa.*
- **ABORTADO\_ERRO\_LOTE\_ANTERIOR **= Quando um dos lotes anteriores da mesma *"chave de pacote"* tenha ficado com a situação ***PROCESSADO\_ERRO\_NEGOCIO*** ou ***PROCESSADO\_ERRO\_INTERNO ou ABORTADO\_AGUARDANDO\_DESCARTE\_PARCIAL***, todos os próximos lotes serão abortados, pois o código da remessa foi cancelado.
- ***EM\_PROCESSAMENTO **= Lote sendo processado pelo e-Sfinge Online.*
- **ABORTADO\_AGUARDANDO\_DESCARTE\_PARCIAL** = Quando ocorrer um erro em um lote e o cancelamentoAutomatico = NÃO, o lote atual ficará com essa situação SE tiver mais de um assunto no lote. (caso tenha apenas um assunto, assumirá a situação: **LOTE\_DESCARTADO** e não precisará do descarte parcial)

Se existir ao menos um pacote com essa situação, novos lotes serão abortados e ficarão com a situação: ***ABORTADO\_ERRO\_LOTE\_ANTERIOR.***

**ABORTADO = ** Quando ocorrer a chamada da Descartar **SubAssunto**o lote que estava na situação ***ABORTADO\_AGUARDANDO\_DESCARTE\_PARCIAL** *mudará seu status para ***ABORTADO***. A situação ***ABORTADO ***permite que novos lotes sejam recebidos e processados.

---

### **Comportamento do timeout.**

O timeout do pacote do tributário é de 72h sem recebimento de pacotes ou chamada da Descartar SubAssunto. A cada chamada destes endpoints o timeout será resetado.
