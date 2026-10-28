package br.dev.luka.esfinge.remessas.adapter.in.http;

import br.dev.luka.esfinge.remessas.application.port.in.InspectExecution;
import br.dev.luka.esfinge.remessas.domain.*;
import com.fasterxml.jackson.databind.*;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.time.Instant;
import java.util.*;
import org.springframework.web.bind.annotation.*;

@RestController
public final class ExecutionInspectionController {
    private final InspectExecution inspection;
    private final ObjectMapper json;
    private final String coverage;
    private final String coverageHash;
    public ExecutionInspectionController(InspectExecution inspection,ObjectMapper json) throws IOException,java.security.NoSuchAlgorithmException {
        this.inspection=inspection;this.json=json.copy().enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS);
        try(var stream=getClass().getResourceAsStream("/evidencias/cobertura.md")) {
            if(stream==null)throw new IllegalStateException("Manifesto de cobertura nao empacotado");
            byte[] bytes=stream.readAllBytes();coverage=new String(bytes,java.nio.charset.StandardCharsets.UTF_8);
            coverageHash=HexFormat.of().formatHex(java.security.MessageDigest.getInstance("SHA-256").digest(bytes));
        }
    }
    private String authorize(HttpServletRequest request,String... allowed) throws IOException {
        String key=request.getHeader("X-Simulator-Key");inspection.authorize(key);
        var names=Set.of(allowed);
        for(var e:request.getParameterMap().entrySet()) if(!names.contains(e.getKey()) || e.getValue().length!=1 || e.getValue()[0].isEmpty())invalid();
        if(request.getInputStream().read()!=-1)invalid();return key;
    }
    private static void invalid(){throw new RemessaFailure(RemessaFailure.Kind.INVALID_REQUEST);}
    private long number(String value) {
        try {if(value==null || !value.matches("[1-9][0-9]*"))throw new IllegalArgumentException();return Long.parseLong(value);}
        catch(IllegalArgumentException failure){invalid();return 0;}
    }
    private int limit(HttpServletRequest r){String value=r.getParameter("limite");long n=value==null?50:number(value);if(n>200)invalid();return (int)n;}
    private Inspection.Filter filter(HttpServletRequest r) {
        String month=r.getParameter("anoMes"),subject=r.getParameter("assunto");Integer yearMonth=null;Assunto assunto=null;
        if(month!=null){long n=number(month);if(n<202601 || n>202612)invalid();yearMonth=(int)n;}
        if(subject!=null)try{assunto=Assunto.valueOf(subject);}catch(IllegalArgumentException failure){invalid();}
        return new Inspection.Filter(r.getParameter("cenario"),r.getParameter("codigoUg"),yearMonth,assunto);
    }
    public record RemessaResponse(long identificadorRemessa,String chavePacote,String cenario,String codigoUg,int anoMes,String situacao,Instant abertura,Instant atividade,Instant finalizadaEm,Instant canceladaEm,Instant expiraEm,Instant expiradaEm) {}
    private RemessaResponse remessa(Remessa r){return new RemessaResponse(r.id(),r.key(),r.scenario(),r.ug(),r.yearMonth(),r.expiredAt()!=null?"EXPIRADO":r.cancelledAt()!=null?"CANCELADO":r.finalizedAt()!=null?"FINALIZADO":"ABERTO",r.openedAt(),r.lastActivity(),r.finalizedAt(),r.cancelledAt(),r.expiresAt(),r.expiredAt());}
    public record LotResponse(long numeroLote,long identificadorRemessa,String chavePacote,String cenario,String codigoUg,int anoMes,int ordem,Assunto assunto,boolean ativo,long recebidos,long recebidosHistoricos,long processados,long efetivos,String hash,Instant recebimento,Processing.State situacao,Processing.Diagnostic diagnostico) {}
    private LotResponse lot(Inspection.LotView view){var l=view.lote();var p=view.processamento();return new LotResponse(l.numeroLote(),l.identificadorRemessa(),l.chavePacote(),l.cenario(),l.codigoUg(),l.anoMes(),l.ordem(),l.assunto(),l.ativo(),l.ativo()?l.recebidosHistoricos():0,l.recebidosHistoricos(),p.processados(),p.efetivos(),l.hash(),l.recebimento(),p.situacao(),p.diagnostico());}
    private <T,R> Inspection.Page<R> map(Inspection.Page<T> p,java.util.function.Function<T,R> mapper){return new Inspection.Page<>(p.itens().stream().map(mapper).toList(),p.proximoCursor(),p.geracao(),p.teto());}
    @GetMapping("/__simulator/remessas")
    public Inspection.Page<RemessaResponse> remessas(HttpServletRequest r) throws IOException {
        String key=authorize(r,"cenario","codigoUg","anoMes","assunto","limite","cursor");return map(inspection.remessas(key,filter(r),limit(r),r.getParameter("cursor")),this::remessa);
    }
    @GetMapping("/__simulator/lotes")
    public Inspection.Page<LotResponse> lotes(HttpServletRequest r) throws IOException {
        String key=authorize(r,"cenario","codigoUg","anoMes","assunto","limite","cursor");return map(inspection.lotes(key,filter(r),limit(r),r.getParameter("cursor")),this::lot);
    }
    public record RemessaDetail(RemessaResponse remessa,List<Inspection.Counts> recebimento,List<Inspection.ResultCounts> processamento,String lotes,String eventos) {}
    @GetMapping("/__simulator/remessas/{id}")
    public RemessaDetail detail(@PathVariable String id,HttpServletRequest r) throws IOException {
        String key=authorize(r);var d=inspection.remessa(key,number(id));return new RemessaDetail(remessa(d.remessa()),d.recebimento(),d.processamento(),"/__simulator/lotes","/__simulator/remessas/"+id+"/eventos");
    }
    @GetMapping("/__simulator/remessas/{id}/eventos")
    public Inspection.Page<Inspection.Event> events(@PathVariable String id,HttpServletRequest r) throws IOException {
        String key=authorize(r,"limite","cursor");return inspection.eventos(key,number(id),limit(r),r.getParameter("cursor"),"remessa");
    }
    public record RecordResponse(long indice,JsonNode registro) {}
    @GetMapping("/__simulator/lotes/{id}/registros")
    public Inspection.Page<RecordResponse> records(@PathVariable String id,HttpServletRequest r) throws IOException {
        String key=authorize(r,"tipo","limite","cursor");Inspection.Type type;
        try{type=Inspection.Type.valueOf(r.getParameter("tipo"));}catch(IllegalArgumentException|NullPointerException failure){invalid();return null;}
        return map(inspection.records(key,number(id),type,limit(r),r.getParameter("cursor")),record->{try{return new RecordResponse(record.indice(),json.readTree(record.payload()));}catch(IOException failure){throw new IllegalStateException(failure);}});
    }
    private Map<String,Object> general() {
        var result=new LinkedHashMap<String,Object>();result.put("versaoFormato",1);
        String version=getClass().getPackage().getImplementationVersion();
        result.put("artefato",Map.of("nome","fake-esfinge","versao",version==null?"INDISPONIVEL_FORA_DO_JAR":version,"git","INDISPONIVEL","tag","INDISPONIVEL","digestImagem","INDISPONIVEL"));
        result.put("contrato",Map.of("perfil","2026/V6/6.06","natureza","DERIVADO_SEM_HOMOLOGACAO"));
        result.put("manifesto",Map.of("recurso","evidencias/cobertura.md","sha256",coverageHash,"conteudo",coverage));
        result.put("comparacao","NAO_COMPARADO");result.put("divergencias",null);
        result.put("limites",List.of("Referencia independente e SQL internos nao disponiveis ao simulador","Aceites #8/E1 e T01/E3 pendentes","Regras tributarias e vinculos de negocio nao implementados","Paginacao local: padrao 50, maximo 200; sem limite TCE implicito","Estados e contagens atuais por pagina; teto de IDs nao congela resultados"));return result;
    }
    @GetMapping("/__simulator/relatorio")
    public Map<String,Object> report(HttpServletRequest r) throws IOException {
        String key=authorize(r,"origem","formato","limite","cursor");String origin=r.getParameter("origem"),format=r.getParameter("formato");
        if(!Set.of("FICTICIA","SANITIZADA","ORIGINAL","NAO_INFORMADA").contains(origin==null?"":origin))invalid();
        if(!Set.of("LOCAL","COMPARTILHAVEL").contains(format==null?"":format))invalid();
        if(format.equals("COMPARTILHAVEL")){if(r.getParameter("limite")!=null || r.getParameter("cursor")!=null)invalid();return general();}
        var report=inspection.report(key,limit(r),r.getParameter("cursor"),origin);var result=general();var s=report.configuracao();
        result.put("origem",Map.of("classificacao",origin,"natureza","DECLARADA_NAO_VERIFICADA"));
        result.put("configuracao",Map.of("cenario",s.scenario(),"perfil",s.profile(),"seed",s.seed(),"instanteInicial",s.initialTime(),"modoRelogio",s.clockMode(),"geracao",s.generation()));
        result.put("processamento",report.processamento());result.put("recebimento",report.recebimento());result.put("resultados",report.resultados());
        result.put("sequencia",report.sequencia());result.put("correlacao","Referencia LOCAL persistida: remessa_eventos.id e vinculos remessa/lote; nao UUID HTTP");
        result.put("continuacao","GET /__simulator/relatorio com mesma origem, formato=LOCAL, limite e proximoCursor como cursor");return result;
    }
}
