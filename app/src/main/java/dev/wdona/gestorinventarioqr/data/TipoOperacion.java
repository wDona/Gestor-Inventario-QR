package dev.wdona.gestorinventarioqr.data;

public enum TipoOperacion {
    ADD("ADD"),
    REMOVE("REMOVE"),
    ASSIGN("ASSIGN");

    private final String valor;

    TipoOperacion(String valor) {
        this.valor = valor;
    }

    public String getValor() {
        return valor;
    }
}
