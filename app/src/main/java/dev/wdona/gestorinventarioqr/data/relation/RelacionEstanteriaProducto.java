package dev.wdona.gestorinventarioqr.data.relation;

import dev.wdona.gestorinventarioqr.data.entity.EstanteriaEntity;
import dev.wdona.gestorinventarioqr.data.entity.ProductoEntity;
import dev.wdona.gestorinventarioqr.data.entity.ProductoEstanteriaEntity;

/**
 * Clase auxiliar para pasar datos de la relación producto-estantería.
 * Ya no usa @Relation de Room, se construye manualmente.
 */
public class RelacionEstanteriaProducto {
    public EstanteriaEntity estanteria;
    public ProductoEntity producto;
    public ProductoEstanteriaEntity relacion; // contiene la cantidad

    public RelacionEstanteriaProducto(EstanteriaEntity estanteria, ProductoEntity producto, ProductoEstanteriaEntity relacion) {
        this.estanteria = estanteria;
        this.producto = producto;
        this.relacion = relacion;
    }
}
