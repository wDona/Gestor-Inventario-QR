package dev.wdona.gestorinventarioqr.domain.repository;

import java.util.List;

import dev.wdona.gestorinventarioqr.domain.model.Operacion;

public interface OperacionRepository {
    void agregarOperacionPendiente(Operacion operacion);
    Operacion getOperacionPendienteById(Long id);
    void actualizarEstadoById(Long id, String nuevoEstado);
    List<Operacion> getOperacionesEnEstadoPendiente();

    Long getUltimoIdOperacionPendiente();
}
