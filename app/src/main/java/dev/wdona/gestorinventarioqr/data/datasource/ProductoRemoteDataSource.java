package dev.wdona.gestorinventarioqr.data.datasource;

import java.util.List;

import dev.wdona.gestorinventarioqr.domain.model.Estanteria;
import dev.wdona.gestorinventarioqr.domain.model.Producto;

public interface ProductoRemoteDataSource extends ProductoDataSource{
    void sincronizarPendientes(List<Producto> productosPendientes);
}
