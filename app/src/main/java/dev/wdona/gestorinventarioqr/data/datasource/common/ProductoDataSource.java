package dev.wdona.gestorinventarioqr.data.datasource.common;

import java.util.List;

import dev.wdona.gestorinventarioqr.domain.model.Estanteria;
import dev.wdona.gestorinventarioqr.domain.model.Producto;

public interface ProductoDataSource {
    void addUndsProduct(Producto producto, int cantidad);
    void removeUndsProduct(Producto producto, int cantidad);
    void assignProductToEstanteria(Producto producto, Estanteria estanteria);
    Producto getProductoById(Long id);
    List<Producto> getAllProductos();
}
