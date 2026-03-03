package dev.wdona.gestorinventarioqr.domain.repository;

import java.util.List;

import dev.wdona.gestorinventarioqr.domain.model.Estanteria;
import dev.wdona.gestorinventarioqr.domain.model.Producto;

public interface ProductoRepository {
    void addUndsProduct(Producto producto, int cantidad) throws Exception;
    void removeUndsProduct(Producto producto, int cantidad) throws Exception;
    void assignProductToEstanteria(Producto producto, Estanteria estanteria) throws Exception;
    void moverCantidad(Long productoId, Long estanteriaOrigenId, Long estanteriaDestinoId, int cantidad) throws Exception;
    Producto getProductoById(Long id);
    Producto getProductoEnEstanteria(Long productoId, Long estanteriaId);
    List<Producto> getUbicacionesProducto(Long productoId);
    void sincronizar(Producto ... productos);
    List<Producto> getAllProductos();
}
