package dev.wdona.gestorinventarioqr.data.datasource.remote.impl;

import dev.wdona.gestorinventarioqr.data.api.EstanteriaApi;
import dev.wdona.gestorinventarioqr.data.api.impl.EstanteriaApiImpl;
import dev.wdona.gestorinventarioqr.data.relation.RelacionEstanteriaProducto;
import dev.wdona.gestorinventarioqr.domain.model.Estanteria;

public class EstanteriaRemoteDataSourceImpl {
    EstanteriaApi api;
    public EstanteriaRemoteDataSourceImpl(EstanteriaApi api) {
        this.api = api;
    }

    public Estanteria getEstanteriaById(Long id) {
        return api.getEstanteriaById(id);
    }
    public Estanteria getEstanteriaConProductosById(Long idEstanteria) {
        return api.getEstanteriaConProductosById(idEstanteria);
    }
}
