package dev.wdona.gestorinventarioqr.data.repository.impl;

import dev.wdona.gestorinventarioqr.data.datasource.local.impl.EstanteriaLocalDataSourceImpl;
import dev.wdona.gestorinventarioqr.data.datasource.remote.impl.EstanteriaRemoteDataSourceImpl;
import dev.wdona.gestorinventarioqr.data.repository.EstanteriaRepository;
import dev.wdona.gestorinventarioqr.domain.model.Estanteria;

public class EstanteriaRepositoryImpl implements EstanteriaRepository {

    EstanteriaRemoteDataSourceImpl remote;
    EstanteriaLocalDataSourceImpl local;

    @Override
    public Estanteria getEstanteriaById(int id) {
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
}
