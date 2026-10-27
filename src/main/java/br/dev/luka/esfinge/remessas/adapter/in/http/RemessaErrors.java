package br.dev.luka.esfinge.remessas.adapter.in.http;

import br.dev.luka.esfinge.remessas.domain.RemessaFailure;
import java.util.UUID;
import org.springframework.core.annotation.Order;
import org.springframework.core.Ordered;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public final class RemessaErrors {
    public record ErrorBody(String codigo, String mensagem, String correlacao) {}
    @ExceptionHandler(RemessaFailure.class)
    public ResponseEntity<ErrorBody> handle(RemessaFailure failure) {
        int status; String code, message;
        switch (failure.kind()) {
            case INVALID_REQUEST -> { status=400; code="SIM_REQUISICAO_INVALIDA"; message="Requisicao invalida"; }
            case INVALID_STRUCTURE -> { status=422; code="SIM_ESTRUTURA_INVALIDA"; message="Estrutura invalida"; }
            case UNSUPPORTED -> { status=422; code="SIM_COBERTURA_AUSENTE"; message="Capacidade nao implementada"; }
            case NOT_FOUND -> { status=404; code="SIM_RECURSO_NAO_ENCONTRADO"; message="Recurso nao encontrado"; }
            case RECOVERY_REQUIRED -> { status=409; code="SIM_ENVIO_ABERTO_SEM_PROCESSAMENTO"; message="Assunto aguarda descarte apos perda de resposta"; }
            default -> { status=409; code="SIM_ESTADO_INVALIDO"; message="Operacao incompativel com o estado atual"; }
        }
        return ResponseEntity.status(status).body(new ErrorBody(code, message, "sim-" + UUID.randomUUID()));
    }
}
