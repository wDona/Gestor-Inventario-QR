package dev.wdona.gestorinventarioqr.data.repository;

import java.util.List;

import dev.wdona.gestorinventarioqr.data.EstadoOperacion;
import dev.wdona.gestorinventarioqr.data.datasource.local.impl.EstanteriaLocalDataSourceImpl;
import dev.wdona.gestorinventarioqr.data.datasource.local.impl.OperacionLocalDataSourceImpl;
import dev.wdona.gestorinventarioqr.data.datasource.local.impl.ProductoLocalDataSourceImpl;
import dev.wdona.gestorinventarioqr.data.datasource.remote.impl.EstanteriaRemoteDataSourceImpl;
import dev.wdona.gestorinventarioqr.data.datasource.remote.impl.ProductoRemoteDataSourceImpl;
import dev.wdona.gestorinventarioqr.domain.repository.OperacionRepository;
import dev.wdona.gestorinventarioqr.domain.model.Operacion;

public class OperacionRepositoryImpl implements OperacionRepository {
    OperacionLocalDataSourceImpl operacionLocal;
    ProductoRemoteDataSourceImpl productoRemote;
    EstanteriaLocalDataSourceImpl estanteriaLocal;
    ProductoLocalDataSourceImpl productoLocal;

    public OperacionRepositoryImpl(OperacionLocalDataSourceImpl operacionLocal, ProductoRemoteDataSourceImpl productoRemote, EstanteriaLocalDataSourceImpl estanteriaLocal, ProductoLocalDataSourceImpl productoLocal) {
        this.operacionLocal = operacionLocal;
        this.productoRemote = productoRemote;
        this.estanteriaLocal = estanteriaLocal;
        this.productoLocal = productoLocal;
    }

    @Override
    public void agregarOperacionPendiente(Operacion operacion) {
        operacionLocal.agregarOperacionPendiente(operacion);
    }

    @Override
    public Operacion getOperacionPendienteById(Long id) {
        return operacionLocal.getOperacionPendienteById(id);
    }

    @Override
    public void actualizarEstadoById(Long id, EstadoOperacion nuevoEstado) {
        operacionLocal.actualizarEstadoById(id, nuevoEstado.getValor());
    }

    @Override
    public Long getUltimoIdOperacionPendiente() {
        return operacionLocal.getUltimoIdOperacionPendiente() != null ? operacionLocal.getUltimoIdOperacionPendiente() : -1L;
    }

    @Override
    public List<Operacion> getAllOperaciones() {
        return operacionLocal.getAllOperaciones();
    }

    @Override
    public List<Operacion> getOperacionesPorEstado(EstadoOperacion estado) {
        return operacionLocal.getOperacionesPorEstado(estado.getValor());
    }

    @Override
    public boolean reintentarOperacion(Operacion operacion) {
        try {
            String estado = operacion.getEstado();
            String tipoOperacion = operacion.getTipoOperacion();

            if (!estado.equals(EstadoOperacion.PENDIENTE.getValor()) && !estado.equals(EstadoOperacion.FALLIDA.getValor())) {
                System.out.println("Operación no está en estado PENDIENTE");
                return false;
            }

            if (tipoOperacion.equals("ADD")) {
                productoRemote.addUndsProduct(productoLocal.getProductoById(operacion.getProductoId()), operacion.getCantidad());
            } else if (tipoOperacion.equals("REMOVE")) {
                productoRemote.removeUndsProduct(productoLocal.getProductoById(operacion.getProductoId()), operacion.getCantidad());
            } else if (tipoOperacion.equals("ASSIGN")) {
                productoRemote.assignProductToEstanteria(productoLocal.getProductoById(operacion.getProductoId()), estanteriaLocal.getEstanteriaById(operacion.getEstanteriaId()));
            } else {
                System.out.println("Tipo de operación desconocido: " + tipoOperacion);
                return false;
            }

        } catch (Exception e) {
            System.out.println("Error en reintentarEnvio: " + e.getMessage());
            actualizarEstadoById(operacion.getId(), EstadoOperacion.FALLIDA);
            return false;
        }

        actualizarEstadoById(operacion.getId(), EstadoOperacion.ENVIADA);
        return true;

    }

    @Override
    public boolean reintentarAllOperaciones() {
        boolean allSuccess = true;
        List<Operacion> operacionesPendientes = getOperacionesPorEstado(EstadoOperacion.PENDIENTE);
        List<Operacion> operacionesFallidas = getOperacionesPorEstado(EstadoOperacion.FALLIDA);

        for (Operacion operacion : operacionesPendientes) {
            boolean success = reintentarOperacion(operacion);
            System.out.println("Reintento de operación ID " + operacion.getId() + ": " + (success ? "Éxito" : "Fallo"));
            if (!success) {
                allSuccess = false;
            }
        }

        for (Operacion operacion : operacionesFallidas) {
            boolean success = reintentarOperacion(operacion);
            System.out.println("Reintento de operación fallida ID " + operacion.getId() + ": " + (success ? "Éxito" : "Fallo"));
            if (!success) {
                allSuccess = false;
            }
        }

        return allSuccess;
    }


}
