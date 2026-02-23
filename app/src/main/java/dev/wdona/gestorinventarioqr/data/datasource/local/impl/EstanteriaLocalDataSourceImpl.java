package dev.wdona.gestorinventarioqr.data.datasource.local.impl;

import java.util.List;

import dev.wdona.gestorinventarioqr.data.datasource.mapper.EstanteriaMapper;
import dev.wdona.gestorinventarioqr.data.datasource.mapper.ProductoMapper;
import dev.wdona.gestorinventarioqr.data.db.EstanteriaDao;
import dev.wdona.gestorinventarioqr.data.entity.EstanteriaEntity;
import dev.wdona.gestorinventarioqr.data.relation.RelacionEstanteriaProducto;
import dev.wdona.gestorinventarioqr.domain.model.Estanteria;

public class EstanteriaLocalDataSourceImpl {
    EstanteriaDao dao;

    public EstanteriaLocalDataSourceImpl(EstanteriaDao dao) {
        this.dao = dao;
    }

    public Estanteria getEstanteriaById(Long id) {
        EstanteriaEntity entity = dao.getEstanteriaById(id);
        if (entity == null) {
            System.out.println("Estanteria no encontrada con ID: " + id);
            return null;
        }
        Estanteria estanteria = EstanteriaMapper.toDomain(entity);
        estanteria.setProductos(ProductoMapper.toDomainList(dao.getProductosByEstanteriaId(id), estanteria));
        return estanteria;
    }

    public Estanteria getEstanteriaConProductosById(Long idEstanteria) {
        RelacionEstanteriaProducto relacion = dao.getEstanteriaConProductosById(idEstanteria);
        if (relacion == null || relacion.estanteria == null) {
            System.out.println("Relacion o estanteria es null para id: " + idEstanteria);
            return null;
        }
        Estanteria estanteria = EstanteriaMapper.toDomain(relacion.estanteria);
        estanteria.setProductos(ProductoMapper.toDomainList(relacion.productos, estanteria));
        return estanteria;
    }
}
