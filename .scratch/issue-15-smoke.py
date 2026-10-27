"""Smoke local da #15, em base exclusiva com duas UGs fictícias (0001,0002)."""
import argparse
import http.client
import json
from decimal import Decimal
from pathlib import Path
from urllib.parse import urlencode

parser = argparse.ArgumentParser(description=__doc__)
parser.add_argument("phase", choices=["prepare", "resume"])
parser.add_argument("--port", type=int, required=True)
parser.add_argument("--state", type=Path, required=True)
args = parser.parse_args()
checks = 0
ADMIN = {"X-Simulator-Key": "admin-local-ficticio"}
ROUTE = "/__simulator/competencias/0001/202601"
CREDIT = '{"lancamentosCreditosTributarios":[{"anoLancamentoCreditoTributario":2020,"codigoLancamentoCreditoTributario":"000009","tipoOperacao":1,"tipoCredito":1,"numeroMatriculaContribuinte":"0007","anoMesCompetencia":202002,"dataLancamento":"2020-02-29","dataVencimento":"2020-03-01","valorLancamentoCreditoTributario":123456789012345678901234567890.12,"numeroCpfResponsavelOperacao":"00000000001","codigoEspecificacaoNaturezaReceita":1}]}'
TOTALS = ("quantidadeCadastroContribuinte", "quantidadeLancamentoCreditosTributarios", "quantidadeDiarioGeralArrecadacao", "quantidadeBaixaCreditosTributarios", "quantidadeSaldoInicialCreditosTributarios", "quantidadeInicioPrazoPrescricional", "quantidadeCadastroImobiliario", "quantidadeCadastroPropriedadeImobiliaria", "quantidadeRevisaoValorLancamentoCreditos", "quantidadeEstornoReceitaDiarioGeralArrecadacao", "quantidadeCertidaoDividaAtiva", "quantidadeCobrancaDividaAtiva", "quantidadeSituacaoCobrancaDividaAtiva", "quantidadeNovoNumeroInscricaoImobiliaria", "quantidadeLancamentoCreditosTributariosOriginal", "quantidadeNumeroCadastroImobiliarioLancamento", "quantidadeRevisaoTipoCreditoTributario")


def check(condition, message):
    global checks
    assert condition, message
    checks += 1


def call(method, path, headers=None, body="", expected=200):
    connection = http.client.HTTPConnection("127.0.0.1", args.port, timeout=10)
    try:
        connection.request(method, path, body.encode(), {"Content-Type": "application/json", **(headers or {})})
        response = connection.getresponse()
        content = response.read()
        check(response.status == expected, f"{method} {path.split('?')[0]}: {response.status}, esperado {expected}")
        return json.loads(content, parse_float=Decimal) if content else None
    finally:
        connection.close()


def login(ug):
    return call("POST", "/autenticacao/login?" + urlencode({"codigoUg": ug, "descricaoEmpresaTI": "EmpresaFicticia", "descritivoSoftware": "SmokeFicticio"}),
                {"codigoAcesso": "acesso-ficticio-a", "senha": "senha-ficticia-a"})["chave"]


def totals(key, count):
    values = dict.fromkeys(TOTALS, "0")
    values.update(chavePacote=key, quantidadeLancamentoCreditosTributarios=str(count))
    return json.dumps(values)


token = login("0001")
auth = {"auth_token": token}
if args.phase == "prepare":
    other = {"auth_token": login("0002")}
    call("GET", ROUTE, expected=401)
    call("GET", ROUTE, auth, expected=401)
    call("GET", ROUTE, {"X-Simulator-Key": token}, expected=401)
    check(call("GET", ROUTE, ADMIN) == {"ug": "0001", "anoMes": 202601, "bloqueada": False}, "Estado inicial incorreto")
    key = call("GET", "/v6/tributario/iniciarEnvio?anoMes=202601&cancelamentoAutomatico=NAO", auth)["chavePacote"]
    query = urlencode({"chavePacote": key})
    receive = "/v6/tributario/enviarParcialLote?" + query
    lot = call("POST", receive, auth, CREDIT)["numeroLote"]
    plan = "/__simulator/falhas/recepcao?" + urlencode({"chavePacote": key, "assunto": "LANCAMENTO_CREDITO_TRIBUTARIO"})
    call("PUT", plan, ADMIN, '{"tipo":"APOS_COMMIT","tentativa":2}')
    call("POST", "/v6/tributario/iniciar-processamento?" + query, auth)
    check(call("PUT", ROUTE, ADMIN, '{"bloqueada":true}')["bloqueada"], "Bloqueio não aplicado")
    for method, path, body in (
        ("GET", "/v6/tributario/iniciarEnvio?anoMes=202601&cancelamentoAutomatico=NAO", ""),
        ("POST", receive, CREDIT),
        ("POST", "/v6/tributario/iniciar-processamento?" + query, ""),
        ("POST", "/v6/tributario/finalizarEnvio", totals(key, 1)),
        ("POST", "/v6/tributario/descarteParcialPorAssunto?" + query + "&assunto=LANCAMENTO_CREDITO_TRIBUTARIO", ""),
        ("POST", "/servicosGerais/cancelarEnvioParcial?" + query, ""),
    ):
        check(call(method, path, auth, body, 403)["codigo"] == "SIM_ACESSO_NEGADO", "Erro de bloqueio incorreto")
    check(call("GET", plan, ADMIN)["tentativas"] == 0, "Recepção bloqueada consumiu plano")
    check(call("GET", f"/v6/tributario/consultarPorNumeroLote/{lot}", auth)["situacao"] == "RECEBIDO", "Consulta avançou processamento")
    current = call("GET", f"/__simulator/lotes/{lot}", ADMIN)
    check(current["efetivos"] == current["processados"] == 0, "Bloqueio/consulta processou dados")
    call("GET", "/v6/tributario/iniciarEnvio?anoMes=202601&cancelamentoAutomatico=NAO", other)
    call("GET", "/v6/tributario/iniciarEnvio?anoMes=202602&cancelamentoAutomatico=NAO", auth)
    args.state.write_text(json.dumps({"key": key, "lot": lot}))
else:
    saved = json.loads(args.state.read_text())
    key, lot = saved["key"], saved["lot"]
    query = urlencode({"chavePacote": key})
    check(call("GET", ROUTE, ADMIN)["bloqueada"], "Reinício perdeu bloqueio")
    check(call("GET", f"/v6/tributario/consultarPorNumeroLote/{lot}", auth)["situacao"] == "RECEBIDO", "Reinício processou lote")
    call("POST", "/__simulator/processamento/avancar", ADMIN, '{"milissegundos":1000,"passos":10}')
    first = call("GET", f"/__simulator/lotes/{lot}", ADMIN)
    check(first["situacao"] == "PROCESSADO_SUCESSO" and first["efetivos"] == 1, "Trabalho aceito foi suspenso")
    call("POST", "/v6/tributario/finalizarEnvio", auth, totals(key, 1), 403)
    check(not call("PUT", ROUTE, ADMIN, '{"bloqueada":false}')["bloqueada"], "Desbloqueio não aplicado")
    fresh = call("POST", "/v6/tributario/enviarParcialLote?" + query, auth, CREDIT)["numeroLote"]
    check(fresh > lot, "Novo lote não tem ID distinto")
    call("POST", "/v6/tributario/iniciar-processamento?" + query, auth)
    call("POST", "/__simulator/processamento/avancar", ADMIN, '{"milissegundos":1000,"passos":10}')
    current = call("GET", f"/__simulator/lotes/{fresh}", ADMIN)
    check(current["registrosEfetivos"] == json.loads(CREDIT, parse_float=Decimal)["lancamentosCreditosTributarios"], "Conteúdo/identificadores/decimal alterados")
    call("POST", "/v6/tributario/finalizarEnvio", auth, totals(key, 2))
print(json.dumps({"phase": args.phase, "checks": checks, "result": "passed"}))
