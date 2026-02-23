package dev.wdona.gestorinventarioqr.data;

public enum TipoOperacion {
    ADD_UNDS_PRODUCT("ADD"),
    REMOVE_UNDS_PRODUCT("REMOVE"),
    ASSIGN_PRODUCT_TO_ESTANTERIA("ASSIGN");

    private final String valor;

    TipoOperacion(String valor) {
        this.valor = valor;
    }

    public String getValor() {
        return valor;
    }
}
