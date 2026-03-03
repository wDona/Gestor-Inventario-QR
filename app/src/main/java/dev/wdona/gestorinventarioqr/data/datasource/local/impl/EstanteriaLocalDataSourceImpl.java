package dev.wdona.gestorinventarioqr.data.datasource.local.impl;

import java.util.List;

import dev.wdona.gestorinventarioqr.data.datasource.mapper.EstanteriaMapper;
import dev.wdona.gestorinventarioqr.data.db.EstanteriaDao;
import dev.wdona.gestorinventarioqr.data.db.ProductoDao;
import dev.wdona.gestorinventarioqr.data.entity.EstanteriaEntity;
import dev.wdona.gestorinventarioqr.data.entity.ProductoEstanteriaEntity;
import dev.wdona.gestorinventarioqr.domain.model.Estanteria;

public class EstanteriaLocalDataSourceImpl {
    EstanteriaDao dao;
    ProductoDao productoDao;

    public EstanteriaLocalDataSourceImpl(EstanteriaDao dao, ProductoDao productoDao) {
        this.dao = dao;
        this.productoDao = productoDao;
    }

    public Estanteria getEstanteriaById(Long id) {
        EstanteriaEntity entity = dao.getEstanteriaById(id);
        if (entity == null) {
            System.out.println("Estanteria no encontrada con ID: " + id);
            return null;
        }
        return EstanteriaMapper.toDomain(entity);
    }

    public Estanteria getEstanteriaConProductosById(Long idEstanteria) {
        EstanteriaEntity entity = dao.getEstanteriaById(idEstanteria);
        if (entity == null) {
            System.out.println("Estanteria no encontrada con ID: " + idEstanteria);
            return null;
        }

        List<ProductoEstanteriaEntity> relaciones = dao.getProductoEstanteriaPorEstanteria(idEstanteria);
        return EstanteriaMapper.toDomainConProductos(entity, relaciones, productoDao);
    }

    public void bajarCambios(Estanteria... estanterias) {
        // No-op por ahora
    }

    public List<Estanteria> getAllEstanterias() {
        return EstanteriaMapper.toDomain(dao.getAllEstanterias());
    }
}
