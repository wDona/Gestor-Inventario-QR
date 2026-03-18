package dev.wdona.gestorinventarioqr.data.api;

import dev.wdona.gestorinventarioqr.data.relation.RelacionEstanteriaProducto;
import dev.wdona.gestorinventarioqr.domain.model.Estanteria;

public interface EstanteriaApi {
    Estanteria getEstanteriaById(String id);
    Estanteria getEstanteriaConProductosById(String idEstanteria);
    void subirCambios(Estanteria... estanterias);
    void deleteEstanteria(String id);
}
