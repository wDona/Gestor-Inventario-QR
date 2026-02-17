package dev.wdona.gestorinventarioqr.data.datasource;

import dev.wdona.gestorinventarioqr.domain.model.Estanteria;
import dev.wdona.gestorinventarioqr.domain.model.Producto;

public interface ProductoLocalDataSource extends ProductoDataSource{
    void addPendienteProduct(Producto producto);
}
