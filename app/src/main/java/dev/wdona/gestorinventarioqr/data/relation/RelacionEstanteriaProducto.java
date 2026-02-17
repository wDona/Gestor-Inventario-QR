package dev.wdona.gestorinventarioqr.data.relation;

import androidx.room.Embedded;
import androidx.room.Relation;

import java.util.List;

import dev.wdona.gestorinventarioqr.data.entity.EstanteriaEntity;
import dev.wdona.gestorinventarioqr.data.entity.ProductoEntity;

public class RelacionEstanteriaProducto {
    @Embedded
    public EstanteriaEntity estanteria;

    @Relation(
            parentColumn = "id",
            entityColumn = "FK_estanteriaId"
    )
    public List<ProductoEntity> productos;


}
