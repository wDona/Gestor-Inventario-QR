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
    Producto getProductoById(String id) throws JSONException;
    Estanteria getEstanteriaById(String id) throws JSONException;
    Estanteria getEstanteriaConProductosById(String idEstanteria) throws JSONException;
    void subirCambiosProducto(Producto ... producto) throws JSONException;

    void subirCambiosEstanteria(Estanteria... estanterias) throws JSONException;

    List<Producto> getAllProductos() throws JSONException;
    void deleteProducto(String id) throws JSONException;
    void deleteEstanteria(String id) throws JSONException;
}
