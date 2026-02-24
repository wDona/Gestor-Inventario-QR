package dev.wdona.gestorinventarioqr.data.api;

import java.util.List;

import dev.wdona.gestorinventarioqr.domain.model.Estanteria;
import dev.wdona.gestorinventarioqr.domain.model.Producto;

public interface ProductoApi {
    void addUndsProduct(Producto producto, int cantidad);
    void removeUndsProduct(Producto producto, int cantidad);
    void assignProductToEstanteria(Producto producto, Estanteria estanteria);
    Producto getProductoById(Long id);
    void subirCambios(Producto ... producto);
    List<Producto> getAllProductos();
}
