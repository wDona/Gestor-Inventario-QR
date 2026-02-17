package dev.wdona.gestorinventarioqr.data.datasource;

import dev.wdona.gestorinventarioqr.domain.model.Estanteria;
import dev.wdona.gestorinventarioqr.domain.model.Producto;

public interface ProductoDataSource {
    void addUndsProduct(Producto producto);
    void removeUndsProduct(Producto producto);
    void assignProductToEstanteria(Producto producto, Estanteria estanteria);
}
