package dev.wdona.gestorinventarioqr.data.datasource.remote.impl;

import dev.wdona.gestorinventarioqr.data.api.EstanteriaApi;
import dev.wdona.gestorinventarioqr.domain.model.Estanteria;

public class EstanteriaRemoteDataSourceImpl {
    EstanteriaApi api;

    public EstanteriaRemoteDataSourceImpl(EstanteriaApi api) {
        this.api = api;
    }

    public Estanteria getEstanteriaById(Long id) {
        return api.getEstanteriaById(id);
    }
}
