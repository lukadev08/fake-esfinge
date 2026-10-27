package br.dev.luka.esfinge.remessas.adapter.in.http;

import jakarta.servlet.ServletException;
import java.io.IOException;
import org.apache.catalina.connector.*;
import org.apache.catalina.valves.ValveBase;
import org.apache.coyote.ActionCode;

public final class ReceptionAbortValve extends ValveBase {
    static final String INTERRUPTED=ReceptionAbortValve.class.getName()+".interrupted";
    public ReceptionAbortValve() {super(true);}
    @Override public void invoke(Request request,Response response) throws IOException,ServletException {
        getNext().invoke(request,response);
        if(Boolean.TRUE.equals(request.getAttribute(INTERRUPTED))) response.getCoyoteResponse().action(ActionCode.CLOSE_NOW,null);
    }
}
