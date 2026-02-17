package dev.wdona.gestorinventarioqr.data.datasource.common;

import dev.wdona.gestorinventarioqr.domain.model.Estanteria;
import dev.wdona.gestorinventarioqr.domain.model.Producto;

public interface EstanteriaDataSource {
    Estanteria getEstanteriaById(int id);
    void addProducto(Estanteria estanteria, Producto producto);
}
