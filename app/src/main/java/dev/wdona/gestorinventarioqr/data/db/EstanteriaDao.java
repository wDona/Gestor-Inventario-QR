package dev.wdona.gestorinventarioqr.data.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;

import java.util.List;

import dev.wdona.gestorinventarioqr.data.entity.EstanteriaEntity;
import dev.wdona.gestorinventarioqr.data.entity.ProductoEntity;
import dev.wdona.gestorinventarioqr.data.relation.RelacionEstanteriaProducto;
import dev.wdona.gestorinventarioqr.domain.model.Estanteria;

@Dao
public interface EstanteriaDao {
    @Query("SELECT * FROM Estanteria WHERE id = :id")
    EstanteriaEntity getEstanteriaById(Long id);

    @Transaction
    @Query("SELECT * FROM Estanteria WHERE id = :idEstanteria")
    RelacionEstanteriaProducto getEstanteriaConProductosById(Long idEstanteria);

    @Query("SELECT * FROM Producto WHERE FK_estanteriaId = :idEstanteria")
    List<ProductoEntity> getProductosByEstanteriaId(Long idEstanteria);
    @Query("SELECT COUNT(*) FROM Estanteria")
    int getCount();
    @Insert
    void insertEstanteria(EstanteriaEntity estanteria);
    @Query("SELECT * FROM Estanteria")
    List<EstanteriaEntity> getAllEstanterias();
}
