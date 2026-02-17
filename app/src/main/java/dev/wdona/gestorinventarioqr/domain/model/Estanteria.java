package dev.wdona.gestorinventarioqr.domain.model;

import java.util.List;

public class Estanteria {
    private Long id;
    private String nombre;
    private List<Producto> productos;

    public Estanteria(Long id, String nombre) {
        this.id = id;
        this.nombre = nombre;
    }

    public Estanteria(Long id, String nombre, List<Producto> productos) {
        this.id = id;
        this.nombre = nombre;
        this.productos = productos;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public List<Producto> getProductos() {
        return productos;
    }

    public void setProductos(List<Producto> productos) {
        this.productos = productos;
    }
}
