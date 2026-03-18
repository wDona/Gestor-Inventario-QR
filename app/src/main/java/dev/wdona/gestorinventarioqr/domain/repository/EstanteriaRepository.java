package dev.wdona.gestorinventarioqr.domain.repository;

import java.util.List;

import dev.wdona.gestorinventarioqr.data.relation.RelacionEstanteriaProducto;
import dev.wdona.gestorinventarioqr.domain.model.Estanteria;

public interface EstanteriaRepository {
        Estanteria getEstanteriaById(String id);
        Estanteria getEstanteriaConProductosById(String idEstanteria);
        void createEstanteria(Estanteria estanteria) throws Exception;
        void sincronizar(Estanteria ... estanterias);
        void deleteEstanteria(String id) throws Exception;
}
