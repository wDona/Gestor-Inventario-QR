package dev.wdona.gestorinventarioqr.data.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import dev.wdona.gestorinventarioqr.data.entity.EstanteriaEntity;
import dev.wdona.gestorinventarioqr.data.entity.ProductoEstanteriaEntity;

@Dao
public interface EstanteriaDao {
    @Query("SELECT * FROM Estanteria WHERE id = :id")
    EstanteriaEntity getEstanteriaById(Long id);

    @Query("SELECT * FROM ProductoEstanteria WHERE estanteriaId = :idEstanteria")
    List<ProductoEstanteriaEntity> getProductoEstanteriaPorEstanteria(Long idEstanteria);

    @Query("SELECT COUNT(*) FROM Estanteria")
    int getCount();

    @Insert
    void insertEstanteria(EstanteriaEntity estanteria);

    @Query("SELECT * FROM Estanteria")
    List<EstanteriaEntity> getAllEstanterias();
}
