package dev.wdona.gestorinventarioqr.mock;

import org.json.JSONException;

import java.util.List;

import dev.wdona.gestorinventarioqr.domain.model.Estanteria;
import dev.wdona.gestorinventarioqr.domain.model.Producto;

public interface MockDatabaseOperations {
    void addUndsProduct(Producto producto, int cantidad) throws JSONException;
    void removeUndsProduct(Producto producto, int cantidad) throws JSONException;
    void assignProductToEstanteria(Producto producto, Estanteria estanteria) throws JSONException;
    void addProducto(Producto producto) throws JSONException;
    Producto getProductoById(Long id) throws JSONException;
    Estanteria getEstanteriaById(Long id) throws JSONException;
    Estanteria getEstanteriaConProductosById(Long idEstanteria) throws JSONException;
    void subirCambios(Producto ... producto) throws JSONException;
    List<Producto> getAllProductos() throws JSONException;
}
