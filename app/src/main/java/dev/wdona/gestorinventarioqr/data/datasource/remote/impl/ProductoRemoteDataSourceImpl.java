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
    public void addUndsProduct(Producto producto, int cantidad) {
        try {
            api.addUndsProduct(producto, cantidad);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    @Override
    public void removeUndsProduct(Producto producto, int cantidad) {
        try {
            api.removeUndsProduct(producto, cantidad);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    @Override
    public void assignProductToEstanteria(Producto producto, Estanteria estanteria) {
        try {
            api.assignProductToEstanteria(producto, estanteria);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
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
    public void subirCambios(List<Producto> productosPendientes) {
        // TODO
    }
}
