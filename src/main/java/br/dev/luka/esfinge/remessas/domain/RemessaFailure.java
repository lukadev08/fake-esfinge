package br.dev.luka.esfinge.remessas.domain;

public final class RemessaFailure extends RuntimeException {
    public enum Kind { INVALID_REQUEST, INVALID_STRUCTURE, UNSUPPORTED, NOT_FOUND, CONFLICT, RECOVERY_REQUIRED, BLOCKED }
    private final Kind kind;
    public RemessaFailure(Kind kind) { super(kind.name()); this.kind = kind; }
    public Kind kind() { return kind; }
}
