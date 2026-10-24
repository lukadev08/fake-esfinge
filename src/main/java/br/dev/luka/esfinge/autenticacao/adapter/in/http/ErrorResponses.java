package br.dev.luka.esfinge.autenticacao.adapter.in.http;

import br.dev.luka.esfinge.autenticacao.domain.AuthenticationFailure;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import java.util.UUID;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
@RestController
public final class ErrorResponses implements ErrorController {
    public record ErrorBody(String codigo, String mensagem, String correlacao) {}
    public static ResponseEntity<ErrorBody> response(int status, String code, String message) {
        return ResponseEntity.status(status).body(new ErrorBody(code, message, "sim-" + UUID.randomUUID()));
    }
    @ExceptionHandler(AuthenticationFailure.class)
    public ResponseEntity<ErrorBody> authentication(AuthenticationFailure failure) {
        return switch (failure.kind()) {
            case INVALID_REQUEST -> response(400, "SIM_REQUISICAO_INVALIDA", "Requisicao invalida");
            case INVALID_CREDENTIALS -> response(401, "SIM_AUTENTICACAO_INVALIDA", "Autenticacao invalida");
            case FORBIDDEN -> response(403, "SIM_ACESSO_NEGADO", "Acesso negado");
        };
    }
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorBody> missing() { return response(404, "SIM_RECURSO_NAO_ENCONTRADO", "Recurso nao encontrado"); }
    @ExceptionHandler(org.springframework.web.HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorBody> method() { return response(405, "SIM_REQUISICAO_INVALIDA", "Metodo nao suportado"); }
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorBody> internal() { return response(500, "SIM_ERRO_INTERNO", "Falha interna do simulador"); }
    @RequestMapping("/error")
    public ResponseEntity<ErrorBody> error(HttpServletRequest request) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        if (Integer.valueOf(404).equals(status)) return missing();
        if (Integer.valueOf(400).equals(status)) return response(400, "SIM_REQUISICAO_INVALIDA", "Requisicao invalida");
        if (Integer.valueOf(405).equals(status)) return method();
        return internal();
    }
}
