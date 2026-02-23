package dev.wdona.gestorinventarioqr.data;

public enum EstadoOperacion {
    PENDIENTE("PENDIENTE"),
    FALLIDA("FALLIDA"),
    ENVIADA("ENVIADA");

    private final String valor;

    EstadoOperacion(String valor) {
        this.valor = valor;
    }

    public String getValor() {
        return valor;
    }
}
