package br.dev.luka.esfinge.autenticacao.domain;

public final class AuthenticationFailure extends RuntimeException {
    private final Kind kind;
    public enum Kind { INVALID_REQUEST, INVALID_CREDENTIALS, FORBIDDEN }
    public AuthenticationFailure(Kind kind) { super(kind.name()); this.kind = kind; }
    public Kind kind() { return kind; }
}
