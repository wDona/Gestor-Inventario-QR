package dev.wdona.gestorinventarioqr.data.repository;

import dev.wdona.gestorinventarioqr.data.datasource.EstanteriaLocalDataSourceImpl;
import dev.wdona.gestorinventarioqr.data.datasource.EstanteriaRemoteDataSourceImpl;
import dev.wdona.gestorinventarioqr.domain.model.Estanteria;

public class EstanteriaRepositoryImpl implements EstanteriaRepository{

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
