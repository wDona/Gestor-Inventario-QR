package dev.wdona.gestorinventarioqr.data.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;

@Entity(
        tableName = "ProductoEstanteria",
        primaryKeys = {"productoId", "estanteriaId"},
        foreignKeys = {
                @ForeignKey(
                        entity = ProductoEntity.class,
                        parentColumns = "id",
                        childColumns = "productoId",
                        onDelete = ForeignKey.CASCADE
                ),
                @ForeignKey(
                        entity = EstanteriaEntity.class,
                        parentColumns = "id",
                        childColumns = "estanteriaId",
                        onDelete = ForeignKey.CASCADE
                )
        },
        indices = {
                @Index("productoId"),
                @Index("estanteriaId")
        }
)
public class ProductoEstanteriaEntity {
    @NonNull
    private String productoId;
    @NonNull
    private String estanteriaId;
    private int cantidad;

    public void setProductoId(String productoId) {
        this.productoId = productoId;
    }

    public String getProductoId() {
        return productoId;
    }

    public void setEstanteriaId(String estanteriaId) {
        this.estanteriaId = estanteriaId;
    }

    public String getEstanteriaId() {
        return estanteriaId;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }
}
