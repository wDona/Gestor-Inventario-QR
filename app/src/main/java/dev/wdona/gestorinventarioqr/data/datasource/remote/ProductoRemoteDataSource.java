package dev.wdona.gestorinventarioqr.data.datasource.remote;

import java.util.List;

import dev.wdona.gestorinventarioqr.data.datasource.common.ProductoDataSource;
import dev.wdona.gestorinventarioqr.domain.model.Producto;

public interface ProductoRemoteDataSource extends ProductoDataSource {
    Producto getProductoById(String id);
    List<Producto> getAllProductos();
    void subirCambios(Producto... productos);
    void deleteProducto(String id);
}
