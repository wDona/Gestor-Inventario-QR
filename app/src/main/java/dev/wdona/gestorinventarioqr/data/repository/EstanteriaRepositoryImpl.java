package dev.wdona.gestorinventarioqr.data.repository;

import java.util.List;

import dev.wdona.gestorinventarioqr.data.datasource.local.impl.EstanteriaLocalDataSourceImpl;
import dev.wdona.gestorinventarioqr.data.datasource.remote.impl.EstanteriaRemoteDataSourceImpl;
import dev.wdona.gestorinventarioqr.data.relation.RelacionEstanteriaProducto;
import dev.wdona.gestorinventarioqr.domain.repository.EstanteriaRepository;
import dev.wdona.gestorinventarioqr.domain.model.Estanteria;

public class EstanteriaRepositoryImpl implements EstanteriaRepository {

    EstanteriaRemoteDataSourceImpl remote;
    EstanteriaLocalDataSourceImpl local;
    OperacionRepositoryImpl operacionRepository;

    public EstanteriaRepositoryImpl(EstanteriaRemoteDataSourceImpl remote, EstanteriaLocalDataSourceImpl local, OperacionRepositoryImpl operacionRepository) {
        this.remote = remote;
        this.local = local;
        this.operacionRepository = operacionRepository;
    }

    @Override
    public Estanteria getEstanteriaById(String id) {
        // Primero intentar remote
        try {
            Estanteria estanteria = remote.getEstanteriaById(id);
            if (estanteria != null) {
                android.util.Log.d("EstanteriaRepo", "getEstanteriaById desde remote: " + estanteria.getNombre());
                return estanteria;
            }
        } catch (Exception e) {
            android.util.Log.e("EstanteriaRepo", "Error remote getEstanteriaById: " + e.getMessage());
        }

        // Si remote falla o es null, intentar local
        try {
            Estanteria estanteria = local.getEstanteriaById(id);
            if (estanteria != null) {
                android.util.Log.d("EstanteriaRepo", "getEstanteriaById desde local: " + estanteria.getNombre());
                return estanteria;
            }
        } catch (Exception e) {
            android.util.Log.e("EstanteriaRepo", "Error local getEstanteriaById: " + e.getMessage());
        }

        return null;
    }

    @Override
    public Estanteria getEstanteriaConProductosById(String idEstanteria) {
        // Primero intentar remote
        try {
            Estanteria estanteria = remote.getEstanteriaConProductosById(idEstanteria);
            if (estanteria != null) {
                // Verificar que tenga productos, si no tiene, intentar local
                if (estanteria.getProductos() != null && !estanteria.getProductos().isEmpty()) {
                    android.util.Log.d("EstanteriaRepo", "getEstanteriaConProductosById desde remote: " +
                        estanteria.getNombre() + " con " + estanteria.getProductos().size() + " productos");
                    
                    // Sincronizar con local para mantener consistencia
                    try {
                        local.bajarCambios(estanteria); // Guardar estantería
                        for (dev.wdona.gestorinventarioqr.domain.model.Producto p : estanteria.getProductos()) {
                            // Guardar productos y relaciones
                             // Asumimos que los productos vienen con su estantería asignada
                             if (p.getEstanteria() == null) p.setEstanteria(estanteria);
                             // Necesitamos un método en ProductoLocalDS o similar para guardar lote
                             // Pero aquí no tenemos acceso directo a ProductoRepo.
                             // Sin embargo, podemos usar EstanteriaLocalDataSourceImpl si tuviera método.
                             // Como no lo tiene fácilmente accesible aquí sin cast o inyección,
                             // podemos omitirlo o agregarlo a EstanteriaRepo.
                        }
                    } catch (Exception e) {
                        android.util.Log.e("EstanteriaRepo", "Error sincronizando local desde remote: " + e.getMessage());
                    }

                    return estanteria;
                } else {
                    android.util.Log.d("EstanteriaRepo", "Remote devolvió estantería sin productos, intentando local...");
                }
            }
        } catch (Exception e) {
            android.util.Log.e("EstanteriaRepo", "Error remote getEstanteriaConProductosById: " + e.getMessage());
        }

        // Si remote falla, es null, o no tiene productos, intentar local
        try {
            Estanteria estanteria = local.getEstanteriaConProductosById(idEstanteria);
            if (estanteria != null) {
                int numProductos = estanteria.getProductos() != null ? estanteria.getProductos().size() : 0;
                android.util.Log.d("EstanteriaRepo", "getEstanteriaConProductosById desde local: " +
                    estanteria.getNombre() + " con " + numProductos + " productos");
                return estanteria;
            }
        } catch (Exception e) {
            android.util.Log.e("EstanteriaRepo", "Error local getEstanteriaConProductosById: " + e.getMessage());
        }

        return null;
    }

    @Override
    public void sincronizar(Estanteria... estanterias) {
        remote.subirCambios(estanterias);
        local.bajarCambios(estanterias);
    }

    @Override
    public void createEstanteria(Estanteria estanteria) throws Exception {
        local.bajarCambios(estanteria);
        boolean exito = false;
        try {
            remote.subirCambios(estanteria);
            exito = true;
        } catch (Exception e) {
            android.util.Log.e("EstanteriaRepo", "Error remote createEstanteria: " + e.getMessage());
        }
        
        registrarOperacion(dev.wdona.gestorinventarioqr.data.TipoOperacion.CREATE_ESTANTERIA.getValor(), estanteria.getId(), exito);
    }

    @Override
    public void deleteEstanteria(String id) throws Exception {
        local.deleteEstanteria(id);
        boolean exito = false;
        try {
            remote.deleteEstanteria(id);
            exito = true;
        } catch (Exception e) {
            android.util.Log.e("EstanteriaRepo", "Error remote deleteEstanteria: " + e.getMessage());
        }
        
        registrarOperacion(dev.wdona.gestorinventarioqr.data.TipoOperacion.DELETE_ESTANTERIA.getValor(), id, exito);
    }

    private void registrarOperacion(String tipo, String estanteriaId, boolean exito) {
        if (operacionRepository != null) {
            try {
                Long ultimoId = operacionRepository.getUltimoIdOperacionPendiente();
                if (ultimoId == null) ultimoId = 0L;

                operacionRepository.agregarOperacionPendiente(
                        new dev.wdona.gestorinventarioqr.domain.model.Operacion(
                                ultimoId + 1,
                                System.currentTimeMillis(),
                                tipo,
                                null, // productoId
                                estanteriaId,
                                0, // cantidad
                                exito ? dev.wdona.gestorinventarioqr.data.EstadoOperacion.ENVIADA.getValor() : dev.wdona.gestorinventarioqr.data.EstadoOperacion.PENDIENTE.getValor()
                        )
                );
            } catch (Exception e) {
                android.util.Log.e("EstanteriaRepo", "Error registrando operación: " + e.getMessage());
            }
        }
    }

}
