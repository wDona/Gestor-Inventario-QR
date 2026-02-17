package dev.wdona.gestorinventarioqr.data.mapper;

import dev.wdona.gestorinventarioqr.data.entity.ProductoEntity;
import dev.wdona.gestorinventarioqr.domain.model.Estanteria;
import dev.wdona.gestorinventarioqr.domain.model.Producto;

public class ProductoMapper {
    public static Producto toDomain(ProductoEntity entity, Estanteria estanteria) {
        if (entity == null) {
            return null;
        }
        return new Producto(
                entity.getNombre(),
                entity.getPrecio(),
                entity.getCantidad(),
                estanteria
        );
    }

    public static ProductoEntity toEntity(Producto producto) {
        if (producto == null) {
            return null;
        }
        ProductoEntity entity = new ProductoEntity();
        entity.setNombre(producto.getNombre());
        entity.setPrecio(producto.getPrecio());
        entity.setCantidad(producto.getCantidad());
        entity.setFK_estanteriaId(producto.getEstanteria().getId());
        return entity;
    }
}
