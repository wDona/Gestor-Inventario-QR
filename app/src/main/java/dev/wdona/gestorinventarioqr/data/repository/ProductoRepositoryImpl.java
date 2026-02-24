package dev.wdona.gestorinventarioqr.data.repository;

import java.util.Collections;
import java.util.List;

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

        // Primero intentar remote
        try {
            producto = remote.getProductoById(id);
            if (producto != null) {
                android.util.Log.d("ProductoRepo", "getProductoById desde remote: " + producto.getNombre());
                sincronizar(producto);
                return producto;
            }
        } catch (Exception e) {
            android.util.Log.e("ProductoRepo", "Error en remote.getProductoById: " + e.getMessage());
        }

        // Si remote devuelve null o falló, intentar en local
        try {
            producto = local.getProductoById(id);
            if (producto != null) {
                android.util.Log.d("ProductoRepo", "getProductoById desde local: " + producto.getNombre());
                return producto;
            }
        } catch (Exception e) {
            android.util.Log.e("ProductoRepo", "Error en local.getProductoById: " + e.getMessage());
        }

        android.util.Log.w("ProductoRepo", "Producto no encontrado ni en remote ni en local, ID: " + id);
        return null;
    }

    @Override
    public void sincronizar(Producto... productos) {
        local.bajarCambios(productos);
    }

    @Override
    public List<Producto> getAllProductos() {
        List<Producto> productos = Collections.emptyList();

        // Primero intentar remote
        try {
            productos = remote.getAllProductos();
            if (productos != null && !productos.isEmpty()) {
                android.util.Log.d("ProductoRepo", "getAllProductos desde remote: " + productos.size() + " productos");
                sincronizar(productos);
                return productos;
            }
        } catch (Exception e) {
            android.util.Log.e("ProductoRepo", "Error en remote.getAllProductos: " + e.getMessage());
        }

        // Si remote falla o devuelve vacío, intentar local
        try {
            productos = local.getAllProductos();
            if (productos != null) {
                android.util.Log.d("ProductoRepo", "getAllProductos desde local: " + productos.size() + " productos");
                return productos;
            }
        } catch (Exception e) {
            android.util.Log.e("ProductoRepo", "Error en local.getAllProductos: " + e.getMessage());
        }

        android.util.Log.w("ProductoRepo", "No se pudieron obtener productos ni de remote ni de local");
        return Collections.emptyList();
    }


}
