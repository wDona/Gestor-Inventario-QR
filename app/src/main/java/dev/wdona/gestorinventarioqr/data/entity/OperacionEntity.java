package dev.wdona.gestorinventarioqr.data.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "Operacion_pendiente")
public class OperacionEntity {
    @PrimaryKey
    private Long id;
    private Long timestamp; // Marca de tiempo de la operación
    private String tipoOperacion; // "ADD", "REMOVE", "MOVE"
    private long productoId; // ID del producto involucrado
    private Long estanteriaId; // ID de la estantería involucrada
    private int cantidad; // Cantidad a agregar o remover
    private String estado; // "PENDIENTE", "ENVIADA", "FALLIDA"

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public String getTipoOperacion() {
        return tipoOperacion;
    }

    public void setTipoOperacion(String tipoOperacion) {
        this.tipoOperacion = tipoOperacion;
    }

    public long getProductoId() {
        return productoId;
    }

    public void setProductoId(long productoId) {
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

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}
