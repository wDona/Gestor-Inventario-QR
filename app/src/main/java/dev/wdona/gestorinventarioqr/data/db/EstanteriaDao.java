package dev.wdona.gestorinventarioqr.data.db;

import androidx.room.Query;
import androidx.room.Transaction;

import java.util.List;

import dev.wdona.gestorinventarioqr.data.relation.RelacionEstanteriaProducto;
import dev.wdona.gestorinventarioqr.domain.model.Estanteria;

public interface EstanteriaDao {
    @Query("SELECT * FROM Estanteria WHERE id = :id")
    Estanteria getEstanteriaById(Long id);

    @Transaction
    @Query("SELECT * FROM Estanteria WHERE id = :idEstanteria")
    List<RelacionEstanteriaProducto> getEstanteriaConProductosById(Long idEstanteria);

}
