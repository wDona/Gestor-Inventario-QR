package dev.wdona.gestorinventarioqr.data.datasource.local.impl;

import java.util.Collections;
import java.util.List;

import dev.wdona.gestorinventarioqr.data.datasource.local.ProductoLocalDataSource;
import dev.wdona.gestorinventarioqr.data.datasource.mapper.EstanteriaMapper;
import dev.wdona.gestorinventarioqr.data.datasource.mapper.ProductoMapper;
import dev.wdona.gestorinventarioqr.data.db.EstanteriaDao;
import dev.wdona.gestorinventarioqr.data.db.ProductoDao;
import dev.wdona.gestorinventarioqr.data.entity.ProductoEntity;
import dev.wdona.gestorinventarioqr.domain.model.Estanteria;
import dev.wdona.gestorinventarioqr.domain.model.Producto;

public class ProductoLocalDataSourceImpl implements ProductoLocalDataSource {
    ProductoDao dao;
    EstanteriaDao estanteriaDao;


    public ProductoLocalDataSourceImpl(ProductoDao dao, EstanteriaDao estanteriaDao) {
        this.dao = dao;
        this.estanteriaDao = estanteriaDao;
    }

    @Override
    public void addUndsProduct(Producto producto, int cantidad) {
        try {
            dao.addUndsProduct(producto.getId(), cantidad);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    @Override
    public void removeUndsProduct(Producto producto, int cantidad) {
        try {
            dao.removeUndsProduct(producto.getId(), cantidad);
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

    @Override
    public Producto getProductoById(Long id) {
        try {
            ProductoEntity productoEntity = dao.getProductoById(id);
            if (productoEntity == null) {
                System.out.println("Producto no encontrado con ID: " + id);
                return null;
            }

            Estanteria estanteria = null;
            if (productoEntity.getFK_estanteriaId() != null) {
                estanteria = EstanteriaMapper.toDomain(estanteriaDao.getEstanteriaById(productoEntity.getFK_estanteriaId()));
            }

            Producto producto = ProductoMapper.toDomain(productoEntity, estanteria);

            return producto;
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            return null;
        }
    }

    @Override
    public List<Producto> getAllProductos() {
        try {
            List<ProductoEntity> entities = dao.getAllProductos();
            if (entities == null || entities.isEmpty()) {
                System.out.println("No se encontraron productos en la base de datos local.");
                return Collections.emptyList();
            }

            return ProductoMapper.toDomainList(entities, estanteriaDao);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    @Override
    public void bajarCambios(Producto ... productos) {
        for (Producto producto : productos) {
            try {
                ProductoEntity entity = ProductoMapper.toEntity(producto);
                dao.updateProducto(entity);
            } catch (Exception e) {
                System.out.println("Error al bajar cambios para producto ID " + producto.getId() + ": " + e.getMessage());
            }
        }
    }
}
