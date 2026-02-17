package dev.wdona.gestorinventarioqr.data.datasource.mapper;

import dev.wdona.gestorinventarioqr.data.entity.OperacionEntity;
import dev.wdona.gestorinventarioqr.domain.model.Operacion;

public class OperacionMapper {
    public static OperacionEntity toEntity(Operacion operacion) {
        OperacionEntity entity = new OperacionEntity();
        entity.setId(operacion.getId());
        entity.setProductoId(operacion.getProductoId());
        entity.setEstanteriaId(operacion.getEstanteriaId());
        entity.setCantidad(operacion.getCantidad());
        entity.setTipoOperacion(operacion.getTipoOperacion());
        entity.setTimestamp(System.currentTimeMillis());
        entity.setEstado(operacion.getEstado());

        return entity;
    }

    public static Operacion toDomain(OperacionEntity entity) {
        return new Operacion(
                entity.getId(),
                entity.getTimestamp(),
                entity.getTipoOperacion(),
                entity.getProductoId(),
                entity.getEstanteriaId(),
                entity.getCantidad(),
                entity.getEstado()
        );
    }
}
