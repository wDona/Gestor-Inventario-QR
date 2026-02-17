package dev.wdona.gestorinventarioqr.data.datasource.local.impl;

import java.util.List;

import dev.wdona.gestorinventarioqr.data.db.EstanteriaDao;
import dev.wdona.gestorinventarioqr.data.relation.RelacionEstanteriaProducto;
import dev.wdona.gestorinventarioqr.domain.model.Estanteria;

public class EstanteriaLocalDataSourceImpl {
    EstanteriaDao dao;

    public EstanteriaLocalDataSourceImpl(EstanteriaDao dao) {
        this.dao = dao;
    }

    public Estanteria getEstanteriaById(Long id) {
        return dao.getEstanteriaById(id);
    }

    public RelacionEstanteriaProducto getEstanteriaConProductosById(Long idEstanteria) {
        return dao.getEstanteriaConProductosById(idEstanteria);
    }

}
