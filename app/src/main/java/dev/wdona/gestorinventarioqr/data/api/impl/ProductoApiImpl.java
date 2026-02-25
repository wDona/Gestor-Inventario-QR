package dev.wdona.gestorinventarioqr.data.api.impl;

import org.json.JSONException;

import java.util.Collections;
import java.util.List;

import dev.wdona.gestorinventarioqr.data.api.ProductoApi;
import dev.wdona.gestorinventarioqr.domain.model.Estanteria;
import dev.wdona.gestorinventarioqr.domain.model.Producto;
import dev.wdona.gestorinventarioqr.mock.MockConfig;
import dev.wdona.gestorinventarioqr.mock.MockDatabaseOperations;

public class ProductoApiImpl implements ProductoApi {

    // Obtiene el mock dinámicamente cada vez (respeta el estado offline/online actual)
    private MockDatabaseOperations getMock() {
        return MockConfig.getMockDatabase();
    }

    @Override
    public void addUndsProduct(Producto producto, int cantidad) throws JSONException {
        if (producto == null || cantidad <= 0) {
            System.out.println("Error, cantidad no válida o producto nulo");
            throw new IllegalArgumentException("Error, cantidad no válida o producto nulo");
        }

        getMock().addUndsProduct(producto, cantidad);
    }

    @Override
    public void removeUndsProduct(Producto producto, int cantidad) throws JSONException {
        if (producto == null || cantidad <= 0 || producto.getCantidad() - cantidad < 0) {
            System.out.println("Error, cantidad no válida o producto nulo");
            throw new IllegalArgumentException("Error, cantidad no válida o producto nulo");
        }

        getMock().removeUndsProduct(producto, cantidad);
    }

    @Override
    public void assignProductToEstanteria(Producto producto, Estanteria estanteria) throws JSONException {
        if (producto == null || estanteria == null) {
            System.out.println("Error, producto o estanteria nulo");
            throw new IllegalArgumentException("Error, producto o estanteria nulo");
        }

        getMock().assignProductToEstanteria(producto, estanteria);
    }

    @Override
    public Producto getProductoById(Long id) {
        if (id == null || id <= 0) {
            System.out.println("Error, ID no válido");
            throw new IllegalArgumentException("Error, ID no válido");
        }

        try {
            return getMock().getProductoById(id);
        } catch (Exception e) {
            System.out.println("Error al obtener producto por ID: " + e.getMessage());
            return null;
        }
    }

    @Override
    public void subirCambios(Producto ... productos) {
        try {
            for (Producto p : productos) {
                if (p == null) {
                    System.out.println("Error, producto nulo en subirCambios");
                    return;
                }
            }

            getMock().subirCambiosProducto(productos);
        } catch (Exception e) {
            System.out.println("Error al subir cambios del producto: " + e.getMessage());
        }
    }

    @Override
    public List<Producto> getAllProductos() {
        try {
            return getMock().getAllProductos();
        } catch (Exception e) {
            System.out.println("Error al obtener todos los productos: " + e.getMessage());
            return Collections.emptyList();
        }
    }
}
