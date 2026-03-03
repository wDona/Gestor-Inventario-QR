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
    private Long productoId;
    @NonNull
    private Long estanteriaId;
    private int cantidad;

    public Long getProductoId() {
        return productoId;
    }

    public void setProductoId(Long productoId) {
        this.productoId = productoId;
    }

    public Long getEstanteriaId() {
        return estanteriaId;
    }

    public void setEstanteriaId(Long estanteriaId) {
        this.estanteriaId = estanteriaId;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }
}

