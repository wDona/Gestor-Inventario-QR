package dev.wdona.gestorinventarioqr.data.datasource.local.impl;

import java.util.List;

import dev.wdona.gestorinventarioqr.data.datasource.local.OperacionLocalDataSource;
import dev.wdona.gestorinventarioqr.data.datasource.mapper.OperacionMapper;
import dev.wdona.gestorinventarioqr.data.db.OperacionDao;
import dev.wdona.gestorinventarioqr.domain.model.Operacion;

public class OperacionLocalDataSourceImpl implements OperacionLocalDataSource{
    OperacionDao dao;

    public OperacionLocalDataSourceImpl(OperacionDao dao) {
        this.dao = dao;
    }

    @Override
    public Operacion getOperacionPendienteById(Long id) {
        return OperacionMapper.toDomain(dao.getOperacionPendienteById(id));
    }

    @Override
    public void actualizarEstadoById(Long id, String nuevoEstado) {
        dao.actualizarEstadoById(id, nuevoEstado);
    }

    @Override
    public void agregarOperacionPendiente(Operacion operacion) {
        dao.agregarOperacion(OperacionMapper.toEntity(operacion));
    }

    @Override
    public Long getUltimoIdOperacionPendiente() {
        return dao.getUltimoIdOperacionPendiente();
    }

    @Override
    public List<Operacion> getAllOperaciones() {
        return OperacionMapper.toDomain(dao.getAllOperaciones());
    }

    @Override
    public List<Operacion> getOperacionesPorEstado(String estado) {
        return OperacionMapper.toDomain(dao.getOperacionesPorEstado(estado));
    }

    @Override
    public void eliminarOperacionesPorEstado(String estado) {
        dao.eliminarOperacionesPorEstado(estado);
    }
}
