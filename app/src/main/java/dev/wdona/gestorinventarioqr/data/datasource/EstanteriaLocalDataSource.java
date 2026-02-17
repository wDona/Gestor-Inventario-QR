package dev.wdona.gestorinventarioqr.data.datasource;

import dev.wdona.gestorinventarioqr.domain.model.Estanteria;

public interface EstanteriaLocalDataSource extends EstanteriaDataSource {
    void addPendiente(Estanteria estanteria);
}
