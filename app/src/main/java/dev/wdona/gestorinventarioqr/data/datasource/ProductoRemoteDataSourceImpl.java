package dev.wdona.gestorinventarioqr.data.datasource;

import java.util.List;

import dev.wdona.gestorinventarioqr.data.api.ProductoApi;
import dev.wdona.gestorinventarioqr.domain.model.Estanteria;
import dev.wdona.gestorinventarioqr.domain.model.Producto;

public class ProductoRemoteDataSourceImpl implements ProductoRemoteDataSource {
    ProductoApi api;
    ProductoRemoteDataSourceImpl(ProductoApi api) {
        this.api = api;
    }

    @Override
    public void addUndsProduct(Producto producto) {
        try {
            // Actualizar el producto y el producto de la lista de la estanteria
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    @Override
    public void removeUndsProduct(Producto producto) {
        try {
            // Actualizar el producto y el producto de la lista de la estanteria
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    @Override
    public void assignProductToEstanteria(Producto producto, Estanteria estanteria) {
        try {
            // Actualizar el producto y el producto de la lista de la estanteria
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    @Override
    public void sincronizarPendientes(List<Producto> productosPendientes) {

    }
}
