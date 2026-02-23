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
        try {
            Estanteria estanteria = remote.getEstanteriaById(id);
            if (estanteria != null) {
                return estanteria;
            } else {
                System.out.println("Estanteria null");
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
        return local.getEstanteriaById(id);
    }

    @Override
    public Estanteria getEstanteriaConProductosById(Long idEstanteria) {
        try {
            Estanteria estanteria = remote.getEstanteriaConProductosById(idEstanteria);
            if (estanteria != null) {
                return estanteria;
            } else {
                System.out.println("Estanteria con productos null");
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
        return local.getEstanteriaConProductosById(idEstanteria);
    }
}
