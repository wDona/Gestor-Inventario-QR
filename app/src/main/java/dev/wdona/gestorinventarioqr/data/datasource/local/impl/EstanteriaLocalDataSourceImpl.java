package dev.wdona.gestorinventarioqr.data.datasource.local.impl;

import dev.wdona.gestorinventarioqr.data.db.EstanteriaDao;
import dev.wdona.gestorinventarioqr.domain.model.Estanteria;

public class EstanteriaLocalDataSourceImpl {
    EstanteriaDao dao;

    public EstanteriaLocalDataSourceImpl(EstanteriaDao dao) {
        this.dao = dao;
    }

    public Estanteria getEstanteriaById(int id) {
        return dao.getEstanteriaById(id);
    }
}
