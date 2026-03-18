package dev.wdona.gestorinventarioqr.data.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import dev.wdona.gestorinventarioqr.data.entity.ProductoEstanteriaEntity;

@Dao
public interface ProductoEstanteriaDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertar(ProductoEstanteriaEntity relacion);

    @Query("SELECT * FROM ProductoEstanteria WHERE productoId = :productoId AND estanteriaId = :estanteriaId")
    ProductoEstanteriaEntity getRelacion(String productoId, String estanteriaId);

    @Query("SELECT * FROM ProductoEstanteria WHERE estanteriaId = :estanteriaId")
    List<ProductoEstanteriaEntity> getProductosPorEstanteria(String estanteriaId);

    @Query("SELECT * FROM ProductoEstanteria WHERE productoId = :productoId")
    List<ProductoEstanteriaEntity> getEstanteriasPorProducto(String productoId);

    @Query("UPDATE ProductoEstanteria SET cantidad = cantidad + :cantidad WHERE productoId = :productoId AND estanteriaId = :estanteriaId")
    void addUnds(String productoId, String estanteriaId, int cantidad);

    @Query("UPDATE ProductoEstanteria SET cantidad = cantidad - :cantidad WHERE productoId = :productoId AND estanteriaId = :estanteriaId")
    void removeUnds(String productoId, String estanteriaId, int cantidad);

    @Query("DELETE FROM ProductoEstanteria WHERE productoId = :productoId AND estanteriaId = :estanteriaId")
    void eliminarRelacion(String productoId, String estanteriaId);

    @Query("SELECT COALESCE(SUM(cantidad), 0) FROM ProductoEstanteria WHERE productoId = :productoId")
    int getCantidadTotalProducto(String productoId);

    @Query("SELECT cantidad FROM ProductoEstanteria WHERE productoId = :productoId AND estanteriaId = :estanteriaId")
    int getCantidadEnEstanteria(String productoId, String estanteriaId);
}
