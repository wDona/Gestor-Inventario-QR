package dev.wdona.gestorinventarioqr.data.datasource.remote.impl;

import android.util.Log;

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
                api.subirCambios(producto);
            } catch (Exception e) {
                Log.e("ProductoRemoteDS", "Error al subir cambios para producto ID " + producto.getId() + ": " + e.getMessage());
            }
        }
    }
}
