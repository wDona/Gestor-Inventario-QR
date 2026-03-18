package dev.wdona.gestorinventarioqr.data.datasource.local;

import java.util.List;

import dev.wdona.gestorinventarioqr.data.datasource.common.EstanteriaDataSource;
import dev.wdona.gestorinventarioqr.domain.model.Estanteria;

public interface EstanteriaLocalDataSource extends EstanteriaDataSource {
    void addPendiente(Estanteria estanteria);
    Estanteria getEstanteriaById(String id);
    Estanteria getEstanteriaConProductosById(String idEstanteria);
    List<Estanteria> getAllEstanterias();
    void bajarCambios(Estanteria... estanterias);
    void deleteEstanteria(String id);
}
