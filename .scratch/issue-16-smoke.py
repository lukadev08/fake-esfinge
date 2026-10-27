"""Smoke local da #16: relógio fixed, duas UGs fictícias, avanço virtual e reinício."""
import argparse
import base64
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
CREDIT = '{"lancamentosCreditosTributarios":[{"anoLancamentoCreditoTributario":2020,"codigoLancamentoCreditoTributario":"000009","tipoOperacao":1,"tipoCredito":1,"numeroMatriculaContribuinte":"0007","anoMesCompetencia":202002,"dataLancamento":"2020-02-29","dataVencimento":"2020-03-01","valorLancamentoCreditoTributario":123456789012345678901234567890.12,"numeroCpfResponsavelOperacao":"00000000001","codigoEspecificacaoNaturezaReceita":1}]}'
CONTRIBUTOR = '{"cadastrosContribuintes":[{"numeroMatriculaContribuinte":"0007","dataAtualizacaoCadastral":"2026-01-02","tipoContribuinte":"1","numeroDocumentoContribuinte":"00000000000","nomeCompletoContribuinte":"Pessoa Ficticia","indicativoCadastroAtivo":"S","indicativoEspolio":"N"}]}'
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


def login(ug="0001"):
    return call("POST", "/autenticacao/login?" + urlencode({"codigoUg": ug, "descricaoEmpresaTI": "EmpresaFicticia", "descritivoSoftware": "SmokeFicticio"}),
                {"codigoAcesso": "acesso-ficticio-a", "senha": "senha-ficticia-a"})["chave"]


def open_remit(auth):
    return call("GET", "/v6/tributario/iniciarEnvio?anoMes=202601&cancelamentoAutomatico=NAO", auth)["chavePacote"]


def receive(auth, key, content):
    return call("POST", "/v6/tributario/enviarParcialLote?" + urlencode({"chavePacote": key}), auth, content)["numeroLote"]


def inspect(number):
    return call("GET", f"/__simulator/lotes/{number}", ADMIN)


def advance(millis, steps=1):
    return call("POST", "/__simulator/processamento/avancar", ADMIN, json.dumps({"milissegundos": millis, "passos": steps}))


def start(auth, key):
    return call("POST", "/v6/tributario/iniciar-processamento?" + urlencode({"chavePacote": key}), auth)


auth = {"auth_token": login()}
if args.phase == "prepare":
    key = open_remit(auth)
    successful = receive(auth, key, CREDIT)
    call("PUT", "/__simulator/processamento", ADMIN, '{"modo":"MANUAL","latenciaMilissegundos":0}')
    start(auth, key)
    advance(0, 2)
    check(inspect(successful)["situacao"] == "PROCESSADO_SUCESSO", "Preparação não processou crédito")
    pending = receive(auth, key, CONTRIBUTOR)
    call("PUT", "/__simulator/processamento", ADMIN, '{"modo":"MANUAL","latenciaMilissegundos":999999999}')
    start(auth, key)
    advance(3600000)
    call("GET", f"/v6/tributario/consultarPorNumeroLote/{pending}", auth, expected=401)
    auth = {"auth_token": login()}
    check(call("GET", f"/v6/tributario/consultarPorNumeroLote/{pending}", auth)["situacao"] == "EM_PROCESSAMENTO", "Novo login perdeu lote ou conduziu processamento")
    before = advance(259199999)["instante"]
    call("GET", f"/v6/tributario/consultarPorNumeroLote/{pending}", auth, expected=401)
    current = inspect(pending)
    check(current["ativo"] and current["situacao"] == "EM_PROCESSAMENTO" and current["efetivos"] == current["processados"] == 0, "Expirou antes do limite ou concluiu pendente")
    good = inspect(successful)
    check(good["ativo"] and good["efetivos"] == 1, "Perdeu sucesso antes da expiração")
    other_auth = {"auth_token": login("0002")}
    other_key = open_remit(other_auth)
    other_lot = receive(other_auth, other_key, CREDIT)
    args.state.write_text(json.dumps({"key": key, "successful": successful, "pending": pending, "other": other_lot, "before": before}))
else:
    saved = json.loads(args.state.read_text())
    key, successful, pending = saved["key"], saved["successful"], saved["pending"]
    check(call("GET", "/__simulator/processamento", ADMIN)["instante"] == saved["before"], "Reinício perdeu relógio")
    check(inspect(pending)["ativo"], "Reinício perdeu agenda ou expirou cedo")
    advance(1)
    current = inspect(pending)
    check(current["situacao"] == "ABORTADO" and current["diagnostico"]["codigo"] == "SIM_ENVIO_EXPIRADO", "Prazo inclusivo não abortou pendente")
    check(not current["ativo"] and current["recebidos"] == current["processados"] == current["efetivos"] == 0 and current["recebidosHistoricos"] == 1, "Expiração deixou dados ativos")
    check(base64.b64decode(current["bytesBase64"]) == CONTRIBUTOR.encode(), "Expiração alterou original")
    old_success = inspect(successful)
    check(old_success["situacao"] == "PROCESSADO_SUCESSO" and not old_success["ativo"] and old_success["efetivos"] == old_success["processados"] == 0, "Histórico de sucesso confundido com efetivo")
    check(base64.b64decode(old_success["bytesBase64"]) == CREDIT.encode(), "Original monetário alterado")
    check(inspect(saved["other"])["ativo"], "Expirou outra UG com deadline futuro")
    check(sum(event["tipo"] == "ENVIO_EXPIRADO" for event in current["eventos"]) == 1, "Expiração duplicada")
    check(call("GET", f"/v6/tributario/consultarPorNumeroLote/{pending}", auth)["situacao"] == "ABORTADO", "Consulta histórica reabriu envio")
    check(inspect(pending)["eventos"] == current["eventos"], "Consulta histórica renovou atividade/eventos")
    call("POST", "/v6/tributario/enviarParcialLote?" + urlencode({"chavePacote": key}), auth, CREDIT, 409)
    fresh_key = open_remit(auth)
    check(fresh_key != key, "Reabertura reutilizou chave expirada")
    fresh_lot = receive(auth, fresh_key, CREDIT)
    check(fresh_lot > saved["other"], "Reenvio reutilizou ID histórico")
    call("PUT", "/__simulator/processamento", ADMIN, '{"modo":"MANUAL","latenciaMilissegundos":0}')
    start(auth, fresh_key)
    advance(0, 2)
    check(inspect(fresh_lot)["registrosEfetivos"] == json.loads(CREDIT, parse_float=Decimal)["lancamentosCreditosTributarios"], "Conteúdo/zeros/decimal alterados")
    totals = dict.fromkeys(TOTALS, "0")
    totals.update(chavePacote=fresh_key, quantidadeLancamentoCreditosTributarios="1")
    call("POST", "/v6/tributario/finalizarEnvio", auth, json.dumps(totals))
print(json.dumps({"phase": args.phase, "checks": checks, "result": "passed"}))
