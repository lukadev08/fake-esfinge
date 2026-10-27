"""Smoke local da #14: transporte HTTP real, dados fictícios e nenhum retry."""
import argparse
import base64
import http.client
import json
from decimal import Decimal
from pathlib import Path
import socket
from urllib.parse import urlencode

parser = argparse.ArgumentParser(description=__doc__)
parser.add_argument("phase", choices=["prepare", "resume"])
parser.add_argument("--port", type=int, required=True)
parser.add_argument("--state", type=Path, required=True)
args = parser.parse_args()
checks = 0
ADMIN = {"X-Simulator-Key": "admin-local-ficticio"}
CONTRIBUTOR = '{"cadastrosContribuintes":[{"numeroMatriculaContribuinte":"0007","dataAtualizacaoCadastral":"2026-01-02","tipoContribuinte":"1","numeroDocumentoContribuinte":"00000000000","nomeCompletoContribuinte":"Pessoa Ficticia","indicativoCadastroAtivo":"S","indicativoEspolio":"N"}]}'
CREDIT = '{"lancamentosCreditosTributarios":[{"anoLancamentoCreditoTributario":2020,"codigoLancamentoCreditoTributario":"000009","tipoOperacao":1,"tipoCredito":1,"numeroMatriculaContribuinte":"0007","anoMesCompetencia":202002,"dataLancamento":"2020-02-29","dataVencimento":"2020-03-01","valorLancamentoCreditoTributario":123456789012345678901234567890.12,"numeroCpfResponsavelOperacao":"00000000001","codigoEspecificacaoNaturezaReceita":1}]}'


def check(condition, message):
    global checks
    assert condition, message
    checks += 1


def call(method, path, headers=None, body="", expected=200):
    request_headers = {"Content-Type": "application/json", **(headers or {})}
    connection = http.client.HTTPConnection("127.0.0.1", args.port, timeout=10)
    try:
        connection.request(method, path, body.encode(), request_headers)
        response = connection.getresponse()
        content = response.read()
        check(response.status == expected, f"{method} {path.split('?')[0]}: {response.status}, esperado {expected}")
        return json.loads(content, parse_float=Decimal) if content else None
    finally:
        connection.close()


def interrupt(path, token, payload):
    content = payload.encode()
    headers = (f"POST {path} HTTP/1.1\r\nHost: localhost\r\nConnection: close\r\n"
               f"Content-Type: application/json\r\nauth_token: {token}\r\nContent-Length: {len(content)}\r\n\r\n").encode()
    with socket.create_connection(("127.0.0.1", args.port), timeout=10) as connection:
        connection.sendall(headers + content)
        check(connection.recv(1) == b"", "Falha selecionada recebeu confirmação HTTP")


token = call("POST", "/autenticacao/login?" + urlencode({"codigoUg": "0001", "descricaoEmpresaTI": "EmpresaFicticia", "descritivoSoftware": "SmokeFicticio"}),
             {"codigoAcesso": "acesso-ficticio-a", "senha": "senha-ficticia-a"})["chave"]
auth = {"auth_token": token}


def plan(key, subject):
    return "/__simulator/falhas/recepcao?" + urlencode({"chavePacote": key, "assunto": subject})


def inspect(number):
    return call("GET", f"/__simulator/lotes/{number}", ADMIN)


if args.phase == "prepare":
    key = call("GET", "/v6/tributario/iniciarEnvio?anoMes=202601&cancelamentoAutomatico=NAO", auth)["chavePacote"]
    receive = "/v6/tributario/enviarParcialLote?" + urlencode({"chavePacote": key})
    before = plan(key, "CADASTRO_CONTRIBUINTE")
    call("PUT", before, ADMIN, '{"tipo":"ANTES_ACEITACAO","tentativa":1}')
    interrupt(receive, token, CONTRIBUTOR)
    state = call("GET", before, ADMIN)
    check(state["consumida"] and state["tentativas"] == 1 and not state["recuperacaoPendente"], "Controle anterior incorreto")
    contributor = call("POST", receive, auth, CONTRIBUTOR)["numeroLote"]
    check(contributor == 1, "Falha anterior criou lote ou base não estava vazia")
    after = plan(key, "LANCAMENTO_CREDITO_TRIBUTARIO")
    call("PUT", after, ADMIN, '{"tipo":"APOS_COMMIT","tentativa":2}')
    first = call("POST", receive, auth, CREDIT)["numeroLote"]
    call("POST", receive, auth, "{}", expected=422)
    check(call("GET", after, ADMIN)["tentativas"] == 1, "Pedido inválido consumiu plano")
    interrupt(receive, token, CREDIT)
    lost = first + 1
    snapshot = inspect(lost)
    check(snapshot["situacao"] == "RECEBIDO" and snapshot["processados"] == snapshot["efetivos"] == 0, "Perda iniciou processamento")
    check(base64.b64decode(snapshot["bytesBase64"]) == CREDIT.encode(), "Bytes perdidos após commit")
    check(sum(event["tipo"] == "RESPOSTA_LOTE_PERDIDA" for event in snapshot["eventos"]) == 1, "Incidente não é único")
    check(call("POST", receive, auth, CREDIT, 409)["codigo"] == "SIM_ENVIO_ABERTO_SEM_PROCESSAMENTO", "Retry sem código de recuperação")
    call("DELETE", after, ADMIN, expected=409)
    call("PUT", after, ADMIN, '{"tipo":"ANTES_ACEITACAO","tentativa":1}', 409)
    args.state.write_text(json.dumps({"key": key, "lost": lost, "contributor": contributor}))
else:
    saved = json.loads(args.state.read_text())
    key, lost = saved["key"], saved["lost"]
    receive = "/v6/tributario/enviarParcialLote?" + urlencode({"chavePacote": key})
    after = plan(key, "LANCAMENTO_CREDITO_TRIBUTARIO")
    state = call("GET", after, ADMIN)
    check(state["consumida"] and state["recuperacaoPendente"] and state["tentativas"] == 2, "Reinício perdeu plano ou marca")
    check(inspect(lost)["situacao"] == "RECEBIDO", "Reinício processou lote")
    check(call("POST", receive, auth, CREDIT, 409)["codigo"] == "SIM_ENVIO_ABERTO_SEM_PROCESSAMENTO", "Reinício perdeu bloqueio")
    call("POST", "/v6/tributario/descarteParcialPorAssunto?" + urlencode({"chavePacote": key, "assunto": "LANCAMENTO_CREDITO_TRIBUTARIO"}), auth)
    check(not call("GET", after, ADMIN)["recuperacaoPendente"], "Descarte não limpou marca")
    old = inspect(lost)
    check(not old["ativo"] and old["recebidos"] == 0 and old["recebidosHistoricos"] == 1, "Histórico misturado com ativo")
    check(base64.b64decode(old["bytesBase64"]) == CREDIT.encode(), "Descarte alterou original")
    check(inspect(saved["contributor"])["ativo"], "Descarte atingiu outro assunto")
    first = call("POST", receive, auth, CREDIT)["numeroLote"]
    second = call("POST", receive, auth, CREDIT)["numeroLote"]
    check(lost < first < second, "Reenvio não criou novos IDs")
    call("PUT", "/__simulator/processamento", ADMIN, '{"modo":"MANUAL","latenciaMilissegundos":0}')
    call("POST", "/v6/tributario/iniciar-processamento?" + urlencode({"chavePacote": key}), auth)
    call("POST", "/__simulator/processamento/avancar", ADMIN, '{"milissegundos":0,"passos":20}')
    for number in (first, second):
        current = inspect(number)
        check(current["situacao"] == "PROCESSADO_SUCESSO" and current["efetivos"] == 1, "Reenvio não tornou registro efetivo")
        check(current["registrosEfetivos"] == json.loads(CREDIT, parse_float=Decimal)["lancamentosCreditosTributarios"], "Identificadores ou decimal alterados")
    totals = {name: "0" for name in ("quantidadeCadastroContribuinte", "quantidadeLancamentoCreditosTributarios", "quantidadeDiarioGeralArrecadacao", "quantidadeBaixaCreditosTributarios", "quantidadeSaldoInicialCreditosTributarios", "quantidadeInicioPrazoPrescricional", "quantidadeCadastroImobiliario", "quantidadeCadastroPropriedadeImobiliaria", "quantidadeRevisaoValorLancamentoCreditos", "quantidadeEstornoReceitaDiarioGeralArrecadacao", "quantidadeCertidaoDividaAtiva", "quantidadeCobrancaDividaAtiva", "quantidadeSituacaoCobrancaDividaAtiva", "quantidadeNovoNumeroInscricaoImobiliaria", "quantidadeLancamentoCreditosTributariosOriginal", "quantidadeNumeroCadastroImobiliarioLancamento", "quantidadeRevisaoTipoCreditoTributario")}
    totals.update(chavePacote=key, quantidadeCadastroContribuinte="1", quantidadeLancamentoCreditosTributarios="2")
    call("POST", "/v6/tributario/finalizarEnvio", auth, json.dumps(totals))
    call("POST", receive, auth, CREDIT, 409)
print(json.dumps({"phase": args.phase, "checks": checks, "result": "passed"}))
