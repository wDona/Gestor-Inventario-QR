package dev.wdona.gestorinventarioqr.data.api;

import dev.wdona.gestorinventarioqr.domain.model.Estanteria;
import dev.wdona.gestorinventarioqr.domain.model.Producto;

public interface ProductoApi {
    void addUndsProduct(Producto producto, int cantidad);
    void removeUndsProduct(Producto producto, int cantidad);
    void assignProductToEstanteria(Producto producto, Estanteria estanteria);
}
