package br.dev.luka.esfinge.autenticacao.adapter.in.http;

import br.dev.luka.esfinge.autenticacao.application.port.in.Authentication;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public final class UnsupportedOperationsController {
    private final Authentication authentication;
    public UnsupportedOperationsController(Authentication authentication) { this.authentication = authentication; }
    @GetMapping({"/v6/tributario/iniciarEnvio", "/v6/tributario/consultarPorNumeroLote/{numeroLote}"})
    public ResponseEntity<ErrorResponses.ErrorBody> read(HttpServletRequest request) { return unsupported(request); }
    @PostMapping({"/v6/tributario/enviarParcialLote", "/v6/tributario/iniciar-processamento", "/v6/tributario/finalizarEnvio",
            "/v6/tributario/descarteParcialPorAssunto", "/v6/tributario/descartar-lote", "/servicosGerais/cancelarEnvioParcial"})
    public ResponseEntity<ErrorResponses.ErrorBody> write(HttpServletRequest request) { return unsupported(request); }
    private ResponseEntity<ErrorResponses.ErrorBody> unsupported(HttpServletRequest request) {
        authentication.authenticate(request.getHeader("auth_token"));
        return ErrorResponses.response(422, "SIM_COBERTURA_AUSENTE", "Operacao ainda nao implementada");
    }
}
