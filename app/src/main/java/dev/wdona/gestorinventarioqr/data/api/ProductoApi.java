package dev.wdona.gestorinventarioqr.data.api;

import dev.wdona.gestorinventarioqr.domain.model.Estanteria;
import dev.wdona.gestorinventarioqr.domain.model.Producto;

public interface ProductoApi {
    void addUndsProduct(Producto producto);
    void removeUndsProduct(Producto producto);
    void assignProductToEstanteria(Producto producto, Estanteria estanteria);
}
