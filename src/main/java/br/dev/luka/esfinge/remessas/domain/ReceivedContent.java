package br.dev.luka.esfinge.remessas.domain;

public record ReceivedContent(byte[] bytes, int records) {
    public ReceivedContent { bytes=bytes.clone(); }
    @Override public byte[] bytes() { return bytes.clone(); }
}
