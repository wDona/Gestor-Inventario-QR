package dev.wdona.gestorinventarioqr.data.datasource.remote.impl;

import java.util.Collections;
import java.util.List;

import dev.wdona.gestorinventarioqr.data.api.ProductoApi;
import dev.wdona.gestorinventarioqr.data.datasource.remote.ProductoRemoteDataSource;
import dev.wdona.gestorinventarioqr.domain.model.Estanteria;
import dev.wdona.gestorinventarioqr.domain.model.Producto;

public class ProductoRemoteDataSourceImpl implements ProductoRemoteDataSource {
    ProductoApi api;
    public ProductoRemoteDataSourceImpl(ProductoApi api) {
        this.api = api;
    }

    @Override
    public void addUndsProduct(Producto producto, int cantidad) throws Exception {
        api.addUndsProduct(producto, cantidad);
    }

    @Override
    public void removeUndsProduct(Producto producto, int cantidad) throws Exception {
        api.removeUndsProduct(producto, cantidad);
    }

    @Override
    public void assignProductToEstanteria(Producto producto, Estanteria estanteria) throws Exception {
        api.assignProductToEstanteria(producto, estanteria);
    }

    @Override
    public Producto getProductoById(Long id) {
        try {
            return api.getProductoById(id);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            return null;
        }
    }

    @Override
    public List<Producto> getAllProductos() {
        try {
            return api.getAllProductos();
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    @Override
    public void subirCambios(Producto... productos) {
        for (Producto producto : productos) {
            try {
                // Intentar obtener el producto remoto para comparar
                Producto remoto = api.getProductoById(producto.getId());
                if (remoto == null) {
                    // Si no existe en remoto, agregarlo
                    api.addUndsProduct(producto, producto.getCantidad());
                } else {
                    // Si existe, comparar cantidades y estantería
                    if (producto.getCantidad() != remoto.getCantidad()) {
                        int diferencia = producto.getCantidad() - remoto.getCantidad();
                        if (diferencia > 0) {
                            api.addUndsProduct(producto, diferencia);
                        } else {
                            api.removeUndsProduct(producto, -diferencia);
                        }
                    }
                    if ((producto.getEstanteria() == null && remoto.getEstanteria() != null) ||
                        (producto.getEstanteria() != null && !producto.getEstanteria().equals(remoto.getEstanteria()))) {
                        api.assignProductToEstanteria(producto, producto.getEstanteria());
                    }
                }
            } catch (Exception e) {
                System.out.println("Error al subir cambios para producto ID " + producto.getId() + ": " + e.getMessage());
            }
        }
    }
}
