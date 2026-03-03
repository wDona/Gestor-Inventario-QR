package dev.wdona.gestorinventarioqr.data.datasource.mapper;

import java.util.ArrayList;
import java.util.List;

import dev.wdona.gestorinventarioqr.data.db.ProductoDao;
import dev.wdona.gestorinventarioqr.data.entity.EstanteriaEntity;
import dev.wdona.gestorinventarioqr.data.entity.ProductoEntity;
import dev.wdona.gestorinventarioqr.data.entity.ProductoEstanteriaEntity;
import dev.wdona.gestorinventarioqr.domain.model.Estanteria;
import dev.wdona.gestorinventarioqr.domain.model.Producto;

public class EstanteriaMapper {
    public static EstanteriaEntity toEntity(Estanteria estanteria) {
        EstanteriaEntity entity = new EstanteriaEntity();
        entity.setId(estanteria.getId());
        entity.setNombre(estanteria.getNombre());

        return entity;
    }

    public static Estanteria toDomain(EstanteriaEntity entity, List<Producto> productos) {
        return new Estanteria(
                entity.getId(),
                entity.getNombre(),
                productos
        );
    }

    public static Estanteria toDomain(EstanteriaEntity entity) {
        return new Estanteria(
                entity.getId(),
                entity.getNombre()
        );
    }

    /**
     * Convierte una EstanteriaEntity + sus relaciones ProductoEstanteria a dominio,
     * resolviendo cada producto desde ProductoDao.
     */
    public static Estanteria toDomainConProductos(EstanteriaEntity estanteriaEntity,
                                                   List<ProductoEstanteriaEntity> relaciones,
                                                   ProductoDao productoDao) {
        if (estanteriaEntity == null) {
            return null;
        }
        Estanteria estanteria = toDomain(estanteriaEntity);
        List<Producto> productos = new ArrayList<>();

        if (relaciones != null) {
            for (ProductoEstanteriaEntity rel : relaciones) {
                ProductoEntity prodEntity = productoDao.getProductoById(rel.getProductoId());
                if (prodEntity != null) {
                    Producto producto = ProductoMapper.toDomain(prodEntity, estanteria, rel.getCantidad());
                    productos.add(producto);
                }
            }
        }

        estanteria.setProductos(productos);
        return estanteria;
    }

    public static List<Estanteria> toDomain(List<EstanteriaEntity> estanteriaEntities) {
        if (estanteriaEntities == null) {
            return null;
        }
        List<Estanteria> estanterias = new ArrayList<>();
        for (EstanteriaEntity entity : estanteriaEntities) {
            estanterias.add(toDomain(entity));
        }
        return estanterias;
    }
}
