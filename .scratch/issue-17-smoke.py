"""Smoke local da #17: reset explícito, duas UGs fictícias e reinício."""
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


def reset(generation):
    return call("POST", "/__simulator/reset", ADMIN, json.dumps({"cenario": "cenario-ficticio-a", "geracaoEsperada": generation}))


def configuration():
    return call("GET", "/__simulator/configuracao", ADMIN)


if args.phase == "prepare":
    initial = configuration()
    token = login()
    auth = {"auth_token": token}
    key = open_remit(auth)
    successful = receive(auth, key, CREDIT)
    call("PUT", "/__simulator/processamento", ADMIN, '{"modo":"MANUAL","latenciaMilissegundos":0}')
    start(auth, key)
    advance(0, 2)
    check(inspect(successful)["efetivos"] == 1, "Preparação não tornou crédito efetivo")
    pending = receive(auth, key, CONTRIBUTOR)
    call("PUT", f"/__simulator/falhas/processamento/{pending}", ADMIN, '{"registro":0}')
    call("PUT", "/__simulator/falhas/recepcao?" + urlencode({"chavePacote": key, "assunto": "LANCAMENTO_CREDITO_TRIBUTARIO"}), ADMIN, '{"tipo":"APOS_COMMIT","tentativa":2}')
    call("PUT", "/__simulator/processamento", ADMIN, '{"modo":"MANUAL","latenciaMilissegundos":12345}')
    start(auth, key)
    advance(1000)
    check(inspect(pending)["situacao"] == "EM_PROCESSAMENTO", "Preparação não deixou trabalho pendente")
    other_auth = {"auth_token": login("0002")}
    other_key = open_remit(other_auth)
    other = receive(other_auth, other_key, CREDIT)
    call("PUT", "/__simulator/competencias/0001/202601", ADMIN, '{"bloqueada":true}')
    call("PUT", "/__simulator/processamento", ADMIN, '{"modo":"AUTOMATICO","latenciaMilissegundos":12345}')
    result = reset(0)
    check(result == {"cenario": "cenario-ficticio-a", "geracaoAnterior": 0, "geracao": 1, "remessasRemovidas": 2, "lotesRemovidos": 3, "sessoesInvalidadas": 2}, "Contagens de reset incorretas")
    after = configuration()
    check(after == {**initial, "geracao": 1}, "Reset alterou configuração/seed/identidades")
    current = call("GET", "/__simulator/processamento", ADMIN)
    check(current == {"modo": "MANUAL", "latenciaMilissegundos": 1000, "instante": initial["instanteInicial"]}, "Reset não restaurou relógio/processamento")
    call("GET", f"/v6/tributario/consultarPorNumeroLote/{pending}", auth, expected=401)
    for number in (successful, pending, other):
        call("GET", f"/__simulator/lotes/{number}", ADMIN, expected=404)
    check(not call("GET", "/__simulator/competencias/0001/202601", ADMIN)["bloqueada"], "Reset conservou bloqueio")
    args.state.write_text(json.dumps({"token": token, "key": key, "last": other, "configuration": after}))
else:
    saved = json.loads(args.state.read_text())
    check(configuration() == saved["configuration"], "Reinício perdeu geração/configuração do reset")
    check(call("GET", "/__simulator/processamento", ADMIN)["instante"] == saved["configuration"]["instanteInicial"], "Reinício perdeu offset reiniciado")
    call("GET", f"/v6/tributario/consultarPorNumeroLote/{saved['last']}", {"auth_token": saved["token"]}, expected=401)
    auth = {"auth_token": login()}
    call("POST", "/v6/tributario/enviarParcialLote?" + urlencode({"chavePacote": saved["key"]}), auth, CREDIT, 404)
    key = open_remit(auth)
    check(key != saved["key"], "Nova execução reutilizou chave")
    first = receive(auth, key, CREDIT)
    second = receive(auth, key, CREDIT)
    contributor = receive(auth, key, CONTRIBUTOR)
    check(saved["last"] < first < second < contributor, "Reset reutilizou IDs históricos")
    start(auth, key)
    advance(10000, 10)
    for number in (first, second):
        current = inspect(number)
        check(current["situacao"] == "PROCESSADO_SUCESSO" and current["efetivos"] == 1, "Replay não processou crédito")
        check(current["registrosEfetivos"] == json.loads(CREDIT, parse_float=Decimal)["lancamentosCreditosTributarios"], "Replay alterou zeros ou decimal")
        check(base64.b64decode(current["bytesBase64"]) == CREDIT.encode(), "Replay alterou original")
    check(inspect(contributor)["situacao"] == "PROCESSADO_SUCESSO", "Replay herdou plano de falha antigo")
    call("POST", "/__simulator/reset", ADMIN, '{"cenario":"cenario-ficticio-a","geracaoEsperada":0}', 409)
    check(inspect(first)["efetivos"] == 1, "Comando obsoleto apagou execução nova")
    totals = dict.fromkeys(TOTALS, "0")
    totals.update(chavePacote=key, quantidadeCadastroContribuinte="1", quantidadeLancamentoCreditosTributarios="2")
    call("POST", "/v6/tributario/finalizarEnvio", auth, json.dumps(totals))
print(json.dumps({"phase": args.phase, "checks": checks, "result": "passed"}))
