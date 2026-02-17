package dev.wdona.gestorinventarioqr.data.datasource;

import java.util.List;

import dev.wdona.gestorinventarioqr.domain.model.Estanteria;

public interface EstanteriaRemoteDataSource extends EstanteriaDataSource {
    void sincronizarPendientes(List<Estanteria> estanteriasPendientes);
}
