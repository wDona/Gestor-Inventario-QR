package dev.wdona.gestorinventarioqr.data.api;

import org.json.JSONException;

import java.util.List;

import dev.wdona.gestorinventarioqr.domain.model.Estanteria;
import dev.wdona.gestorinventarioqr.domain.model.Producto;

public interface ProductoApi {
    void addUndsProduct(Producto producto, int cantidad) throws JSONException;
    void removeUndsProduct(Producto producto, int cantidad) throws JSONException;
    void assignProductToEstanteria(Producto producto, Estanteria estanteria) throws JSONException;
    Producto getProductoById(Long id);
    void subirCambios(Producto ... producto);
    List<Producto> getAllProductos();
}
