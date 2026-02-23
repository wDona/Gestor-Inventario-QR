package dev.wdona.gestorinventarioqr.data.repository;

import dev.wdona.gestorinventarioqr.data.datasource.local.impl.ProductoLocalDataSourceImpl;
import dev.wdona.gestorinventarioqr.data.datasource.remote.impl.ProductoRemoteDataSourceImpl;
import dev.wdona.gestorinventarioqr.domain.repository.ProductoRepository;
import dev.wdona.gestorinventarioqr.domain.model.Estanteria;
import dev.wdona.gestorinventarioqr.domain.model.Operacion;
import dev.wdona.gestorinventarioqr.domain.model.Producto;

public class ProductoRepositoryImpl implements ProductoRepository {
    ProductoRemoteDataSourceImpl remote;
    ProductoLocalDataSourceImpl local;
    OperacionRepositoryImpl registro;


    public ProductoRepositoryImpl(ProductoRemoteDataSourceImpl remote, ProductoLocalDataSourceImpl local) {
        this.remote = remote;
        this.local = local;
    }

    @Override
    public void addUndsProduct(Producto producto, int cantidad) {
        try {
            remote.addUndsProduct(producto, cantidad);
        } catch (Exception e) {
            System.out.println("Error en remote.addUndsProduct: " + e.getMessage());
            // Registrar operación pendiente solo si registro está inicializado
            if (registro != null) {
                try {
                    registro.agregarOperacionPendiente(
                            new Operacion(
                                    registro.getUltimoIdOperacionPendiente() + 1,
                                    System.currentTimeMillis(),
                                    "ADD",
                                    producto.getId(),
                                    producto.getEstanteria() != null ? producto.getEstanteria().getId() : null,
                                    cantidad,
                                    "PENDIENTE"
                            )
                    );
                } catch (Exception regError) {
                    System.out.println("Error al registrar operación pendiente: " + regError.getMessage());
                }
            }
        } finally {
            local.addUndsProduct(producto, cantidad);
        }
    }

    @Override
    public void removeUndsProduct(Producto producto, int cantidad) {
        try {
            remote.removeUndsProduct(producto, cantidad);
        } catch (Exception e) {
            System.out.println("Error en remote.removeUndsProduct: " + e.getMessage());
            // Registrar operación pendiente solo si registro está inicializado
            if (registro != null) {
                try {
                    registro.agregarOperacionPendiente(
                            new Operacion(
                                    registro.getUltimoIdOperacionPendiente() + 1,
                                    System.currentTimeMillis(),
                                    "REMOVE",
                                    producto.getId(),
                                    producto.getEstanteria() != null ? producto.getEstanteria().getId() : null,
                                    cantidad,
                                    "PENDIENTE"
                            )
                    );
                } catch (Exception regError) {
                    System.out.println("Error al registrar operación pendiente: " + regError.getMessage());
                }
            }
        } finally {
            local.removeUndsProduct(producto, cantidad);
        }
    }

    @Override
    public void assignProductToEstanteria(Producto producto, Estanteria estanteria) {
        try {
            remote.assignProductToEstanteria(producto, estanteria);
        } catch (Exception e) {
            System.out.println("Error en remote.assignProductToEstanteria: " + e.getMessage());
            // Registrar operación pendiente solo si registro está inicializado
            if (registro != null) {
                try {
                    registro.agregarOperacionPendiente(
                            new Operacion(
                                    registro.getUltimoIdOperacionPendiente() + 1,
                                    System.currentTimeMillis(),
                                    "MOVE",
                                    producto.getId(),
                                    estanteria.getId(),
                                    producto.getCantidad(),
                                    "PENDIENTE"
                            )
                    );
                } catch (Exception regError) {
                    System.out.println("Error al registrar operación pendiente: " + regError.getMessage());
                }
            }
        } finally {
            local.assignProductToEstanteria(producto, estanteria);
        }
    }

    @Override
    public Producto getProductoById(Long id) {
        Producto producto = null;
        try {
            producto = remote.getProductoById(id);
        } catch (Exception e) {
            System.out.println("Error en remote.getProductoById: " + e.getMessage());
        }

        // Si remote devuelve null o falló, intentar en local
        if (producto == null) {
            try {
                producto = local.getProductoById(id);
            } catch (Exception e) {
                System.out.println("Error en local.getProductoById: " + e.getMessage());
            }
        }

        if (producto == null) {
            System.out.println("Producto null - no encontrado ni en remote ni en local, ID: " + id);
        }

        return producto;
    }
}
