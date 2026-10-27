package br.dev.luka.esfinge.remessas.domain;

public final class Reception {
    private Reception() {}
    public enum Type { ANTES_ACEITACAO, APOS_COMMIT }
    public record Plan(String chavePacote,Assunto assunto,Type tipo,long tentativa,long tentativas,boolean consumida,boolean recuperacaoPendente) {}
    public static final class Interrupted extends RuntimeException {
        private final Type type;
        public Interrupted(Type type) {super(type.name(),null,false,false);this.type=type;}
        public Type type() {return type;}
    }
}
