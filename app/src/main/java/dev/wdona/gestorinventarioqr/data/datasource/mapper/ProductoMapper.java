package dev.wdona.gestorinventarioqr.data.datasource.mapper;

import java.util.ArrayList;
import java.util.List;

import dev.wdona.gestorinventarioqr.data.db.EstanteriaDao;
import dev.wdona.gestorinventarioqr.data.entity.ProductoEntity;
import dev.wdona.gestorinventarioqr.domain.model.Estanteria;
import dev.wdona.gestorinventarioqr.domain.model.Producto;

public class ProductoMapper {
    public static Producto toDomain(ProductoEntity entity, Estanteria estanteria) {
        if (entity == null) {
            return null;
        }
        return new Producto(
                entity.getId(),
                entity.getNombre(),
                entity.getPrecio(),
                entity.getCantidad(),
                estanteria
        );
    }

    public static List<Producto> toDomainList(List<ProductoEntity> entities, Estanteria estanteria) {
        if (entities == null || estanteria == null) {
            return null;
        }
        List<Producto> productos = new ArrayList<>();
        for (int i = 0; i < entities.size(); i++) {
            productos.add(toDomain(entities.get(i), estanteria));
        }
        return productos;
    }

    /**
     * Convierte una lista de ProductoEntity a Producto obteniendo la estantería de cada uno
     */
    public static List<Producto> toDomainList(List<ProductoEntity> entities, EstanteriaDao estanteriaDao) {
        if (entities == null) {
            return new ArrayList<>();
        }
        List<Producto> productos = new ArrayList<>();
        for (ProductoEntity entity : entities) {
            Estanteria estanteria = null;
            if (entity.getFK_estanteriaId() != null) {
                estanteria = EstanteriaMapper.toDomain(estanteriaDao.getEstanteriaById(entity.getFK_estanteriaId()));
            }
            productos.add(toDomain(entity, estanteria));
        }
        return productos;
    }

    public static ProductoEntity toEntity(Producto producto) {
        if (producto == null) {
            return null;
        }
        ProductoEntity entity = new ProductoEntity();
        entity.setId(producto.getId());
        entity.setNombre(producto.getNombre());
        entity.setPrecio(producto.getPrecio());
        entity.setCantidad(producto.getCantidad());
        if (producto.getEstanteria() != null) {
            entity.setFK_estanteriaId(producto.getEstanteria().getId());
        }
        return entity;
    }
}
