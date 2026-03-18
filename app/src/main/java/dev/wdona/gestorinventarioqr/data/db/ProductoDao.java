package dev.wdona.gestorinventarioqr.data.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import dev.wdona.gestorinventarioqr.data.entity.ProductoEntity;

@Dao
public interface ProductoDao {
    @Query("SELECT * FROM Producto WHERE id = :id")
    ProductoEntity getProductoById(String id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertProducto(ProductoEntity productoEntity);

    @Update
    void updateProducto(ProductoEntity productoEntity);

    @Query("SELECT * FROM Producto")
    List<ProductoEntity> getAllProductos();

    @Query("DELETE FROM Producto WHERE id = :id")
    void deleteProducto(String id);
}
