package dev.wdona.gestorinventarioqr.domain.repository;

import dev.wdona.gestorinventarioqr.domain.model.Estanteria;
import dev.wdona.gestorinventarioqr.domain.model.Producto;

public interface ProductoRepository {
    void addUndsProduct(Producto producto, int cantidad);
    void removeUndsProduct(Producto producto, int cantidad);
    void assignProductToEstanteria(Producto producto, Estanteria estanteria);
}
