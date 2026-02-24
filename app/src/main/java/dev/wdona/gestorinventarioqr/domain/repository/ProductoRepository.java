package dev.wdona.gestorinventarioqr.domain.repository;

import java.util.List;

import dev.wdona.gestorinventarioqr.domain.model.Estanteria;
import dev.wdona.gestorinventarioqr.domain.model.Producto;

public interface ProductoRepository {
    void addUndsProduct(Producto producto, int cantidad);
    void removeUndsProduct(Producto producto, int cantidad);
    void assignProductToEstanteria(Producto producto, Estanteria estanteria);
    Producto getProductoById(Long id);
    void sincronizar(Producto ... productos);
    List<Producto> getAllProductos();
}
