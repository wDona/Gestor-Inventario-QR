package dev.wdona.gestorinventarioqr.data.datasource.remote.impl;

import java.util.List;

import dev.wdona.gestorinventarioqr.data.api.ProductoApi;
import dev.wdona.gestorinventarioqr.data.datasource.remote.ProductoRemoteDataSource;
import dev.wdona.gestorinventarioqr.domain.model.Estanteria;
import dev.wdona.gestorinventarioqr.domain.model.Producto;

public class ProductoRemoteDataSourceImpl implements ProductoRemoteDataSource {
    ProductoApi api;
    ProductoRemoteDataSourceImpl(ProductoApi api) {
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
    public void sincronizarPendientes(List<Producto> productosPendientes) {
        // TODO
    }
}
