package dev.wdona.gestorinventarioqr.data.datasource.local.impl;

import dev.wdona.gestorinventarioqr.data.datasource.local.ProductoLocalDataSource;
import dev.wdona.gestorinventarioqr.data.db.OperacionDao;
import dev.wdona.gestorinventarioqr.data.db.ProductoDao;
import dev.wdona.gestorinventarioqr.domain.model.Estanteria;
import dev.wdona.gestorinventarioqr.domain.model.Producto;

public class ProductoLocalDataSourceImpl implements ProductoLocalDataSource {
    ProductoDao dao;

    ProductoLocalDataSourceImpl(ProductoDao dao, OperacionDao operacionDao) {
        this.dao = dao;
    }

    @Override
    public void addUndsProduct(Producto producto) {
        try {
            dao.addUndsProduct(producto.getId(), producto.getCantidad());
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    @Override
    public void removeUndsProduct(Producto producto) {
        try {
            dao.removeUndsProduct(producto.getId(), producto.getCantidad());
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    @Override
    public void assignProductToEstanteria(Producto producto, Estanteria estanteria) {
        try {
            dao.assignProductToEstanteria(producto.getId(), estanteria.getId());
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}
