package br.dev.luka.esfinge.autenticacao.adapter.in.http;

import br.dev.luka.esfinge.autenticacao.application.port.in.Authentication;
import br.dev.luka.esfinge.autenticacao.domain.AuthenticationFailure;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.*;
import org.springframework.web.bind.annotation.*;

@RestController
public final class LoginController {
    private final Authentication authentication;
    public LoginController(Authentication authentication) { this.authentication = authentication; }
    public record LoginResponse(String chave) {}

    @PostMapping("/autenticacao/login")
    public LoginResponse login(HttpServletRequest request) throws IOException {
        if (request.getInputStream().read() != -1 || !request.getParameterMap().keySet().equals(Set.of("codigoUg", "descricaoEmpresaTI", "descritivoSoftware")))
            throw invalid();
        String access = header(request, "codigoAcesso"), password = header(request, "senha");
        String ug = parameter(request, "codigoUg");
        parameter(request, "descricaoEmpresaTI"); parameter(request, "descritivoSoftware");
        return new LoginResponse(authentication.login(access, password, ug));
    }
    private static String header(HttpServletRequest request, String name) {
        var values = Collections.list(request.getHeaders(name));
        if (values.size() != 1 || values.getFirst().isEmpty()) throw invalid();
        return values.getFirst();
    }
    private static String parameter(HttpServletRequest request, String name) {
        String[] values = request.getParameterValues(name);
        if (values == null || values.length != 1 || values[0].isEmpty()) throw invalid();
        return values[0];
    }
    private static AuthenticationFailure invalid() { return new AuthenticationFailure(AuthenticationFailure.Kind.INVALID_REQUEST); }
}
