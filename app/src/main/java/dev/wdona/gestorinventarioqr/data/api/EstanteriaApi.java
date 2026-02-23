package dev.wdona.gestorinventarioqr.data.api;

import dev.wdona.gestorinventarioqr.data.relation.RelacionEstanteriaProducto;
import dev.wdona.gestorinventarioqr.domain.model.Estanteria;

public interface EstanteriaApi {
    Estanteria getEstanteriaById(Long id);
    Estanteria getEstanteriaConProductosById(Long idEstanteria);
}
