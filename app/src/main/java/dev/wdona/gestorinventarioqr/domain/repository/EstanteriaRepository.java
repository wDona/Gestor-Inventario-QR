package dev.wdona.gestorinventarioqr.domain.repository;

import java.util.List;

import dev.wdona.gestorinventarioqr.data.relation.RelacionEstanteriaProducto;
import dev.wdona.gestorinventarioqr.domain.model.Estanteria;

public interface EstanteriaRepository {
        Estanteria getEstanteriaById(Long id);

        Estanteria getEstanteriaConProductosById(Long idEstanteria);
}
