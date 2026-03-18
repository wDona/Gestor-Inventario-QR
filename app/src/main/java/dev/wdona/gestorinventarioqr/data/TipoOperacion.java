package dev.wdona.gestorinventarioqr.data;

public enum TipoOperacion {
    ADD("ADD"),
    REMOVE("REMOVE"),
    ASSIGN("ASSIGN"),
    CREATE_PRODUCT("CREATE_PRODUCT"),
    DELETE_PRODUCT("DELETE_PRODUCT"),
    CREATE_ESTANTERIA("CREATE_ESTANTERIA"),
    DELETE_ESTANTERIA("DELETE_ESTANTERIA");

    private final String valor;

    TipoOperacion(String valor) {
        this.valor = valor;
    }

    public String getValor() {
        return valor;
    }
}
