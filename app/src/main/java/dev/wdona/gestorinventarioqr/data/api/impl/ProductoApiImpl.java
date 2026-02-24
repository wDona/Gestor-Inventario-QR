package dev.wdona.gestorinventarioqr.data.api.impl;

import java.util.Collections;
import java.util.List;

import dev.wdona.gestorinventarioqr.data.api.ProductoApi;
import dev.wdona.gestorinventarioqr.domain.model.Estanteria;
import dev.wdona.gestorinventarioqr.domain.model.Producto;
import dev.wdona.gestorinventarioqr.mock.MockDatabaseController;
import dev.wdona.gestorinventarioqr.mock.MockDatabaseOperations;

public class ProductoApiImpl implements ProductoApi {
    MockDatabaseOperations mockDatabaseOperations = new MockDatabaseController();
    @Override
    public void addUndsProduct(Producto producto, int cantidad) {
        if (producto == null || cantidad <= 0) {
            System.out.println("Error, cantidad no válida o producto nulo");
            return;
        }

        try {
            mockDatabaseOperations.addUndsProduct(producto, cantidad);
        } catch (Exception e) {
            System.out.println("Error al agregar unidades del producto: " + e.getMessage());
        }
    }

    @Override
    public void removeUndsProduct(Producto producto, int cantidad) {
        if (producto == null || cantidad <= 0 || producto.getCantidad() - cantidad < 0) {
            System.out.println("Error, cantidad no válida o producto nulo");
            return;
        }

        try {
            mockDatabaseOperations.removeUndsProduct(producto, cantidad);
        } catch (Exception e) {
            System.out.println("Error al agregar unidades del producto: " + e.getMessage());
        }
    }

    @Override
    public void assignProductToEstanteria(Producto producto, Estanteria estanteria) {
        if (producto == null || estanteria == null) {
            System.out.println("Error, producto o estanteria nulo");
            return;
        }

        try {
            mockDatabaseOperations.assignProductToEstanteria(producto, estanteria);
        } catch (Exception e) {
            System.out.println("Error al asignar producto a estanteria: " + e.getMessage());
        }
    }

    @Override
    public Producto getProductoById(Long id) {
        if (id == null || id <= 0) {
            System.out.println("Error, ID no válido");
            return null;
        }

        try {
            return mockDatabaseOperations.getProductoById(id);
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

            mockDatabaseOperations.subirCambios(productos);
        } catch (Exception e) {
            System.out.println("Error al subir cambios del producto: " + e.getMessage());
        }
    }

    @Override
    public List<Producto> getAllProductos() {
        try {
            return mockDatabaseOperations.getAllProductos();
        } catch (Exception e) {
            System.out.println("Error al obtener todos los productos: " + e.getMessage());
            return Collections.emptyList();
        }
    }
}
