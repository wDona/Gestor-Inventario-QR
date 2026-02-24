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

    public EstanteriaRepositoryImpl(EstanteriaRemoteDataSourceImpl remote, EstanteriaLocalDataSourceImpl local) {
        this.remote = remote;
        this.local = local;
    }

    @Override
    public Estanteria getEstanteriaById(Long id) {
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
    public Estanteria getEstanteriaConProductosById(Long idEstanteria) {
        // Primero intentar remote
        try {
            Estanteria estanteria = remote.getEstanteriaConProductosById(idEstanteria);
            if (estanteria != null) {
                // Verificar que tenga productos, si no tiene, intentar local
                if (estanteria.getProductos() != null && !estanteria.getProductos().isEmpty()) {
                    android.util.Log.d("EstanteriaRepo", "getEstanteriaConProductosById desde remote: " +
                        estanteria.getNombre() + " con " + estanteria.getProductos().size() + " productos");
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
}
