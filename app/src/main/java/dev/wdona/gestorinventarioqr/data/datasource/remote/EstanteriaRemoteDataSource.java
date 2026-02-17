package dev.wdona.gestorinventarioqr.data.datasource.remote;

import java.util.List;

import dev.wdona.gestorinventarioqr.data.datasource.common.EstanteriaDataSource;
import dev.wdona.gestorinventarioqr.domain.model.Estanteria;

public interface EstanteriaRemoteDataSource extends EstanteriaDataSource {
    void sincronizarPendientes(List<Estanteria> estanteriasPendientes);
}
