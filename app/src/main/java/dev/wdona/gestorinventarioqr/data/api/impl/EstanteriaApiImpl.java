package dev.wdona.gestorinventarioqr.data.api.impl;

import dev.wdona.gestorinventarioqr.data.api.EstanteriaApi;
import dev.wdona.gestorinventarioqr.domain.model.Estanteria;
import dev.wdona.gestorinventarioqr.mock.MockConfig;
import dev.wdona.gestorinventarioqr.mock.MockDatabaseOperations;

public class EstanteriaApiImpl implements EstanteriaApi {

    // Obtiene el mock dinámicamente cada vez (respeta el estado offline/online actual)
    private MockDatabaseOperations getMock() {
        return MockConfig.getMockDatabase();
    }

    @Override
    public Estanteria getEstanteriaById(String id) {
        try {
            return getMock().getEstanteriaById(id);
        } catch (Exception e) {
            System.out.println("Error al obtener estanteria por ID: " + e.getMessage());
            return null;
        }
    }

    @Override
    public Estanteria getEstanteriaConProductosById(String idEstanteria) {
        try {
            return getMock().getEstanteriaConProductosById(idEstanteria);
        } catch (Exception e) {
            System.out.println("Error al obtener estanteria con productos por ID: " + e.getMessage());
            return null;
        }
    }

    @Override
    public void subirCambios(Estanteria... estanterias) {
        try {
            getMock().subirCambiosEstanteria(estanterias);
        } catch (Exception e) {
            System.out.println("Error al subir cambios de estanterias: " + e.getMessage());
        }
    }

    @Override
    public void deleteEstanteria(String id) {
        try {
            getMock().deleteEstanteria(id);
        } catch (Exception e) {
            System.out.println("Error al eliminar estanteria por ID: " + e.getMessage());
        }
    }
}
