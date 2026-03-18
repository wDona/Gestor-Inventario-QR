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

    public Estanteria getEstanteriaById(String id) {
        return api.getEstanteriaById(id);
    }
    public Estanteria getEstanteriaConProductosById(String idEstanteria) {
        return api.getEstanteriaConProductosById(idEstanteria);
    }
    public void subirCambios(Estanteria... estanterias) {
        api.subirCambios(estanterias);
    }
    public void deleteEstanteria(String id) {
        api.deleteEstanteria(id);
    }
}
