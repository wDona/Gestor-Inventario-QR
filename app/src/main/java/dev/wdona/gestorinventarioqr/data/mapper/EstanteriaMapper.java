package dev.wdona.gestorinventarioqr.data.mapper;

import java.util.List;

import dev.wdona.gestorinventarioqr.data.entity.EstanteriaEntity;
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
        return new Estanteria(entity.getId(), entity.getNombre(), productos);
    }
}
