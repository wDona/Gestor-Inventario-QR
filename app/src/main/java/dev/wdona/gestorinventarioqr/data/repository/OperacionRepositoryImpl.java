package dev.wdona.gestorinventarioqr.data.repository;

import java.util.List;

import dev.wdona.gestorinventarioqr.data.datasource.local.impl.OperacionLocalDataSourceImpl;
import dev.wdona.gestorinventarioqr.domain.repository.OperacionRepository;
import dev.wdona.gestorinventarioqr.domain.model.Operacion;

public class OperacionRepositoryImpl implements OperacionRepository {
    OperacionLocalDataSourceImpl local;

    public OperacionRepositoryImpl(OperacionLocalDataSourceImpl local) {
        this.local = local;
    }

    @Override
    public void agregarOperacionPendiente(Operacion operacion) {
        local.agregarOperacionPendiente(operacion);
    }

    @Override
    public Operacion getOperacionPendienteById(Long id) {
        return local.getOperacionPendienteById(id);
    }

    @Override
    public void actualizarEstadoById(Long id, String nuevoEstado) {
        local.actualizarEstadoById(id, nuevoEstado);
    }

    @Override
    public List<Operacion> getOperacionesEnEstadoPendiente() {
        return local.getOperacionesEnEstadoPendiente();
    }

    @Override
    public Long getUltimoIdOperacionPendiente() {
        return local.getUltimoIdOperacionPendiente() != null ? local.getUltimoIdOperacionPendiente() : -1L;
    }
}
