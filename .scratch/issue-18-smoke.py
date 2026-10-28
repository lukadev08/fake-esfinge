"""Smoke local da #18: inspeção/relatório, reinício e cursor após reset."""
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
parser.add_argument("--database", type=Path, required=True)
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



def get(path, **query):
    return call("GET", path+("?"+urlencode(query) if query else ""), ADMIN)


def snapshot():
    import sqlite3
    with sqlite3.connect("file:"+str(args.database)+"?mode=ro", uri=True) as database:
        return {name: database.execute('SELECT * FROM "'+name+'" ORDER BY rowid').fetchall()
                for (name,) in database.execute("SELECT name FROM sqlite_master WHERE type='table' ORDER BY name")}


if args.phase == "prepare":
    import hashlib
    call("GET", "/__simulator/lotes?extra=1", expected=401)
    token=login();auth={"auth_token":token};key=open_remit(auth)
    lots=[receive(auth,key,CREDIT) for _ in range(3)]
    page=get("/__simulator/lotes",codigoUg="0001",anoMes="202601",assunto="LANCAMENTO_CREDITO_TRIBUTARIO",limite="2")
    check([l["numeroLote"] for l in page["itens"]]==lots[:2],"Filtro/página incorretos")
    check("bytesBase64" not in page["itens"][0] and "registros" not in page["itens"][0],"Payload em lista")
    report=get("/__simulator/relatorio",origem="FICTICIA",formato="LOCAL",limite="1")
    check(report["artefato"]["versao"]=="0.1.0","Versão real do jar")
    coverage=Path("docs/cobertura.md").read_bytes()
    check(report["manifesto"]["sha256"]==hashlib.sha256(coverage).hexdigest(),"Hash snapshot do manifesto")
    check(report["manifesto"]["conteudo"]==coverage.decode(),"Bytes do manifesto empacotado")
    check(report["comparacao"]=="NAO_COMPARADO" and report["divergencias"] is None,"Comparação inventada")
    check(report["sequencia"]["itens"][0]["tipo"]=="ENVIO_ABERTO","Evento real ausente")
    check(report["origem"]["natureza"]=="DECLARADA_NAO_VERIFICADA","Origem indevidamente verificada")
    shared=get("/__simulator/relatorio",origem="FICTICIA",formato="COMPARTILHAVEL")
    check(set(shared)=={"versaoFormato","artefato","contrato","manifesto","comparacao","divergencias","limites"},"Síntese copiou contexto da execução")
    check(all(secret not in json.dumps(shared) for secret in [key,token,"cenario-ficticio-a","Pessoa Ficticia"]),"Síntese expõe execução")
    before=snapshot()
    get("/__simulator/remessas");get("/__simulator/remessas/1");get("/__simulator/remessas/1/eventos",limite="1")
    records=get(f"/__simulator/lotes/{lots[0]}/registros",tipo="RECEBIDOS",limite="1")
    check(records["itens"][0]["registro"]["numeroMatriculaContribuinte"]=="0007","Zeros perdidos")
    check(records["itens"][0]["registro"]["valorLancamentoCreditoTributario"]==Decimal("123456789012345678901234567890.12"),"Decimal perdeu precisão")
    get("/__simulator/relatorio",origem="FICTICIA",formato="LOCAL")
    check(before==snapshot(),"Leitura alterou SQLite")
    args.state.write_text(json.dumps({"cursor":page["proximoCursor"],"reportCursor":report["sequencia"]["proximoCursor"],"lots":lots,"token":token,"key":key}))
    (args.state.parent/"relatorio-local-001.json").write_text(json.dumps(report,default=str))
else:
    state=json.loads(args.state.read_text())
    page=get("/__simulator/lotes",codigoUg="0001",anoMes="202601",assunto="LANCAMENTO_CREDITO_TRIBUTARIO",limite="2",cursor=state["cursor"])
    check(page["itens"][0]["numeroLote"]==state["lots"][2] and page["proximoCursor"] is None,"Reinício perdeu continuação")
    report=get("/__simulator/relatorio",origem="FICTICIA",formato="LOCAL",limite="1",cursor=state["reportCursor"])
    check(report["sequencia"]["itens"][0]["numeroLote"]==state["lots"][0],"Reinício perdeu vínculo evento/lote")
    reset(0)
    call("GET","/__simulator/lotes?"+urlencode({"codigoUg":"0001","anoMes":"202601","assunto":"LANCAMENTO_CREDITO_TRIBUTARIO","limite":"2","cursor":state["cursor"]}),ADMIN,expected=409)
    call("GET","/__simulator/relatorio?"+urlencode({"origem":"FICTICIA","formato":"LOCAL","limite":"1","cursor":state["reportCursor"]}),ADMIN,expected=409)
    report=get("/__simulator/relatorio",origem="FICTICIA",formato="LOCAL")
    check(report["sequencia"]["geracao"]==1 and not report["sequencia"]["itens"] and not report["resultados"],"Relatório pós-reset contém dados antigos")
    auth={"auth_token":login()};key=open_remit(auth);new=receive(auth,key,CREDIT)
    check(new>max(state["lots"]),"Reset reutilizou IDs")
    before=snapshot();get("/__simulator/lotes");get(f"/__simulator/lotes/{new}/registros",tipo="HISTORICOS")
    check(before==snapshot(),"Leitura após reset alterou SQLite")
    import sqlite3
    with sqlite3.connect("file:"+str(args.database)+"?mode=ro",uri=True) as database:
        check(database.execute("PRAGMA integrity_check").fetchone()==("ok",),"SQLite corrompido")
        check(not database.execute("PRAGMA foreign_key_check").fetchall(),"Violação FK")
print(f"{args.phase}: {checks} verificações aprovadas")
