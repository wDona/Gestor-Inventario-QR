package dev.wdona.gestorinventarioqr.domain.repository;

import java.util.List;

import dev.wdona.gestorinventarioqr.domain.model.Estanteria;
import dev.wdona.gestorinventarioqr.domain.model.Producto;

public interface ProductoRepository {
    void addUndsProduct(Producto producto, int cantidad) throws Exception;
    void removeUndsProduct(Producto producto, int cantidad) throws Exception;
    void assignProductToEstanteria(Producto producto, Estanteria estanteria) throws Exception;
    void moverCantidad(String productoId, String estanteriaOrigenId, String estanteriaDestinoId, int cantidad) throws Exception;
    Producto getProductoById(String id);
    Producto getProductoEnEstanteria(String productoId, String estanteriaId);
    List<Producto> getUbicacionesProducto(String productoId);
    void sincronizar(Producto ... productos);
    void createProducto(Producto producto) throws Exception;
    List<Producto> getAllProductos();
    void deleteProducto(String id) throws Exception;
}
