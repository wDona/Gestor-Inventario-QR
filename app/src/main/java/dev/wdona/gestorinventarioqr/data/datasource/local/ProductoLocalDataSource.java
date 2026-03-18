package dev.wdona.gestorinventarioqr.data.datasource.local;

import java.util.List;

import dev.wdona.gestorinventarioqr.data.datasource.common.ProductoDataSource;
import dev.wdona.gestorinventarioqr.domain.model.Producto;

public interface ProductoLocalDataSource extends ProductoDataSource {
    Producto getProductoById(String id);
    List<Producto> getAllProductos();
    void bajarCambios(Producto... productos);
    void insertProducto(Producto producto);
    void deleteProducto(String id);
}
