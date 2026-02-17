package dev.wdona.gestorinventarioqr.data.datasource.local.impl;

import java.util.List;

import dev.wdona.gestorinventarioqr.data.datasource.local.OperacionLocalDataSource;
import dev.wdona.gestorinventarioqr.domain.model.Operacion;

public class OperacionLocalDataSourceImpl implements OperacionLocalDataSource{
    OperacionLocalDataSource dao;

    public OperacionLocalDataSourceImpl(OperacionLocalDataSource dao) {
        this.dao = dao;
    }

    @Override
    public Operacion getOperacionPendienteById(Long id) {
        return dao.getOperacionPendienteById(id);
    }

    @Override
    public void actualizarEstadoById(Long id, String nuevoEstado) {
        dao.actualizarEstadoById(id, nuevoEstado);
    }

    @Override
    public List<Operacion> getOperacionesEnEstadoPendiente() {
        return dao.getOperacionesEnEstadoPendiente();
    }

    @Override
    public void agregarOperacionPendiente(Operacion operacion) {
        dao.agregarOperacionPendiente(operacion);
    }

    @Override
    public Long getUltimoIdOperacionPendiente() {
        return dao.getUltimoIdOperacionPendiente();
    }
}
