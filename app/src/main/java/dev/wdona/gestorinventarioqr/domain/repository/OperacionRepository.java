package dev.wdona.gestorinventarioqr.domain.repository;

import java.util.List;

import dev.wdona.gestorinventarioqr.data.EstadoOperacion;
import dev.wdona.gestorinventarioqr.data.TipoOperacion;
import dev.wdona.gestorinventarioqr.domain.model.Operacion;

public interface OperacionRepository {
    void agregarOperacionPendiente(Operacion operacion);
    Operacion getOperacionPendienteById(Long id);
    void actualizarEstadoById(Long id, EstadoOperacion nuevoEstado);
    Long getUltimoIdOperacionPendiente();
    List<Operacion> getTodasLasOperaciones();
    List<Operacion> getOperacionesPorEstado(EstadoOperacion estado);
}
