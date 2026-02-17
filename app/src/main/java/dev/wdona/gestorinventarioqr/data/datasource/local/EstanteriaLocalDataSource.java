package dev.wdona.gestorinventarioqr.data.datasource.local;

import dev.wdona.gestorinventarioqr.data.datasource.common.EstanteriaDataSource;
import dev.wdona.gestorinventarioqr.domain.model.Estanteria;

public interface EstanteriaLocalDataSource extends EstanteriaDataSource {
    void addPendiente(Estanteria estanteria);
}
