package dev.wdona.gestorinventarioqr.data.datasource.mapper;

import java.util.List;

import dev.wdona.gestorinventarioqr.data.entity.EstanteriaEntity;
import dev.wdona.gestorinventarioqr.data.relation.RelacionEstanteriaProducto;
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

    public static Estanteria toDomain(RelacionEstanteriaProducto relacion) {
        if (relacion == null || relacion.estanteria == null) {
            return null;
        }

        EstanteriaEntity estanteriaEntity = relacion.estanteria;
        List<Producto> productos = ProductoMapper.toDomainList(relacion.productos, toDomain(estanteriaEntity));

        return toDomain(estanteriaEntity, productos);
    }

    public static List<Estanteria> toDomain(List<EstanteriaEntity> estanteriaEntities) {
        if (estanteriaEntities == null) {
            return null;
        }
        List<Estanteria> estanterias = new java.util.ArrayList<>();
        for (EstanteriaEntity entity : estanteriaEntities) {
            estanterias.add(toDomain(entity));
        }
        return estanterias;
    }
}
