package dev.wdona.gestorinventarioqr.data.datasource.local;

import java.util.List;

import dev.wdona.gestorinventarioqr.domain.model.Operacion;

public interface OperacionLocalDataSource {
    void agregarOperacionPendiente(Operacion operacion);
    Operacion getOperacionPendienteById(Long id);
    void actualizarEstadoById(Long id, String nuevoEstado);
    Long getUltimoIdOperacionPendiente();
    List<Operacion> getAllOperaciones();
    List<Operacion> getOperacionesPorEstado(String estado);


}
