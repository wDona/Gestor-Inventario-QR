package dev.wdona.gestorinventarioqr.data.db;

import dev.wdona.gestorinventarioqr.domain.model.Estanteria;
import dev.wdona.gestorinventarioqr.domain.model.Producto;

public interface ProductoDao {
    void addUndsProduct(Producto producto);
    void removeUndsProduct(Producto producto);
    void assignProductToEstanteria(Producto producto, Estanteria estanteria);
    void addPendienteProduct(Producto producto);
}
