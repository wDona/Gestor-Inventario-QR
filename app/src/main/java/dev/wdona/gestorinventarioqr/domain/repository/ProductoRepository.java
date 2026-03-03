package dev.wdona.gestorinventarioqr.domain.repository;

import java.util.List;

import dev.wdona.gestorinventarioqr.domain.model.Estanteria;
import dev.wdona.gestorinventarioqr.domain.model.Producto;

public interface ProductoRepository {
    void addUndsProduct(Producto producto, int cantidad) throws Exception;
    void removeUndsProduct(Producto producto, int cantidad) throws Exception;
    void assignProductToEstanteria(Producto producto, Estanteria estanteria) throws Exception;
    Producto getProductoById(Long id);
    void sincronizar(Producto ... productos);
    List<Producto> getAllProductos();
}
