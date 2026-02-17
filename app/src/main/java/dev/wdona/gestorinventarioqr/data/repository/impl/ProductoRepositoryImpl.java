package dev.wdona.gestorinventarioqr.data.repository.impl;

import dev.wdona.gestorinventarioqr.data.datasource.local.impl.ProductoLocalDataSourceImpl;
import dev.wdona.gestorinventarioqr.data.datasource.remote.impl.ProductoRemoteDataSourceImpl;
import dev.wdona.gestorinventarioqr.data.repository.ProductoRepository;
import dev.wdona.gestorinventarioqr.domain.model.Estanteria;
import dev.wdona.gestorinventarioqr.domain.model.Producto;

public class ProductoRepositoryImpl implements ProductoRepository {
    ProductoRemoteDataSourceImpl remote;
    ProductoLocalDataSourceImpl local;

    ProductoRepositoryImpl(ProductoRemoteDataSourceImpl remote, ProductoLocalDataSourceImpl local) {
        this.remote = remote;
        this.local = local;
    }

    @Override
    public void addUndsProduct(Producto producto) {
        try {
            remote.addUndsProduct(producto);
        } catch (Exception e) {
            local.addPendienteProduct(producto);
            System.out.println("Error: " + e.getMessage());
        }
    }

    @Override
    public void removeUndsProduct(Producto producto) {
        try {
            remote.removeUndsProduct(producto);
        } catch (Exception e) {
            local.addPendienteProduct(producto);
            System.out.println("Error: " + e.getMessage());
        }
    }

    @Override
    public void assignProductToEstanteria(Producto producto, Estanteria estanteria) {
        try {
            remote.assignProductToEstanteria(producto, estanteria);
        } catch (Exception e) {
            local.addPendienteProduct(producto);
            System.out.println("Error: " + e.getMessage());
        }
    }
}
