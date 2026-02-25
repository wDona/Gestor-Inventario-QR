package dev.wdona.gestorinventarioqr.data.repository;

import android.util.Log;

import java.util.List;

import dev.wdona.gestorinventarioqr.data.EstadoOperacion;
import dev.wdona.gestorinventarioqr.data.datasource.local.impl.EstanteriaLocalDataSourceImpl;
import dev.wdona.gestorinventarioqr.data.datasource.local.impl.OperacionLocalDataSourceImpl;
import dev.wdona.gestorinventarioqr.data.datasource.local.impl.ProductoLocalDataSourceImpl;
import dev.wdona.gestorinventarioqr.data.datasource.remote.impl.EstanteriaRemoteDataSourceImpl;
import dev.wdona.gestorinventarioqr.data.datasource.remote.impl.ProductoRemoteDataSourceImpl;
import dev.wdona.gestorinventarioqr.domain.model.Estanteria;
import dev.wdona.gestorinventarioqr.domain.model.Operacion;
import dev.wdona.gestorinventarioqr.domain.model.Producto;

public class SyncRepository {
    EstanteriaRemoteDataSourceImpl estRemote;
    EstanteriaLocalDataSourceImpl estLocal;
    ProductoRemoteDataSourceImpl prodRemote;
    ProductoLocalDataSourceImpl prodLocal;
    OperacionRepositoryImpl operRegistro;

    public SyncRepository(EstanteriaRemoteDataSourceImpl estRemote, EstanteriaLocalDataSourceImpl estLocal, ProductoRemoteDataSourceImpl prodRemote, ProductoLocalDataSourceImpl prodLocal, OperacionRepositoryImpl operRegistro) {
        this.estRemote = estRemote;
        this.estLocal = estLocal;
        this.prodRemote = prodRemote;
        this.prodLocal = prodLocal;
        this.operRegistro = operRegistro;
    }

    public void subirCambios() {
        List<Operacion> pendientes = operRegistro.getOperacionesPorEstado(EstadoOperacion.PENDIENTE);
        for (Operacion operacion : pendientes) {
            boolean exito = operRegistro.reintentarOperacion(operacion);
            if (!exito) {
                Log.e("SyncRepo", "Error al reintentar operación ID " + operacion.getId() + ": sigue pendiente");
            }
        }

        for (Operacion operacion : operRegistro.getOperacionesPorEstado(EstadoOperacion.FALLIDA)) {
            boolean exito = operRegistro.reintentarOperacion(operacion);
            if (!exito) {
                Log.e("SyncRepo", "Error al reintentar operación ID " + operacion.getId() + ": sigue pendiente");
            }
        }

        // Luego subir cambios de productos y estanterías
        for (Producto producto : prodLocal.getAllProductos()) {
            try {
                prodRemote.subirCambios(producto);
            } catch (Exception e) {
                Log.e("SyncRepo", "Error al subir producto " + producto.getNombre() + ": " + e.getMessage());
            }
        }

        for (Estanteria estanteria : estLocal.getAllEstanterias()) {
            try {
                estRemote.subirCambios(estanteria);
            } catch (Exception e) {
                Log.e("SyncRepo", "Error al subir estantería " + estanteria.getNombre() + ": " + e.getMessage());
            }
        }

    }
}
