package dev.wdona.gestorinventarioqr.data.repository;

import java.util.Collections;
import java.util.List;

import dev.wdona.gestorinventarioqr.data.EstadoOperacion;
import dev.wdona.gestorinventarioqr.data.TipoOperacion;
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

    public ProductoRepositoryImpl(ProductoRemoteDataSourceImpl remote, ProductoLocalDataSourceImpl local, OperacionRepositoryImpl registro) {
        this.remote = remote;
        this.local = local;
        this.registro = registro;
    }

    @Override
    public void addUndsProduct(Producto producto, int cantidad) {
        // Siempre guardar localmente
        local.addUndsProduct(producto, cantidad);
        boolean exito = false;
        try {
            remote.addUndsProduct(producto, cantidad);
            exito = true;
        } catch (Exception e) {
            android.util.Log.e("ProductoRepo", "Error en remote.addUndsProduct: " + e.getMessage());
            exito = false;
        }

        // Registrar operación
        if (registro != null) {
            try {
                registro.agregarOperacionPendiente(
                        new Operacion(
                                registro.getUltimoIdOperacionPendiente() + 1,
                                System.currentTimeMillis(),
                                TipoOperacion.ADD.getValor(),
                                producto.getId(),
                                producto.getEstanteria() != null ? producto.getEstanteria().getId() : null,
                                cantidad,
                                exito ? EstadoOperacion.ENVIADA.getValor() : EstadoOperacion.PENDIENTE.getValor()
                        )
                );
            } catch (Exception regError) {
                android.util.Log.e("ProductoRepo",  "Error al registrar operación pendiente: " + regError.getMessage());
            }
        }

    }

    @Override
    public void removeUndsProduct(Producto producto, int cantidad) {
        boolean exito = false;
        // Siempre guardar localmente
        local.removeUndsProduct(producto, cantidad);

        try {
            remote.removeUndsProduct(producto, cantidad);
            exito = true;
        } catch (Exception e) {
            android.util.Log.e("ProductoRepo", "Error en remote.removeUndsProduct: " + e.getMessage());
            exito = false;
        }

        // Registrar operación
        if (registro != null) {
            try {
                registro.agregarOperacionPendiente(
                        new Operacion(
                                registro.getUltimoIdOperacionPendiente() + 1,
                                System.currentTimeMillis(),
                                TipoOperacion.REMOVE.getValor(),
                                producto.getId(),
                                producto.getEstanteria() != null ? producto.getEstanteria().getId() : null,
                                cantidad,
                                exito ? EstadoOperacion.ENVIADA.getValor() : EstadoOperacion.PENDIENTE.getValor()
                        )
                );
            } catch (Exception regError) {
                android.util.Log.e("ProductoRepo",  "Error al registrar operación pendiente: " + regError.getMessage());
            }
        }

    }

    @Override
    public void assignProductToEstanteria(Producto producto, Estanteria estanteria) {
        // Siempre guardar localmente
        local.assignProductToEstanteria(producto, estanteria);

        boolean exito = false;
        try {
            remote.assignProductToEstanteria(producto, estanteria);
            exito = true;
        } catch (Exception e) {
            android.util.Log.e("ProductoRepo", "Error en remote.assignProductToEstanteria: " + e.getMessage());
            exito = false;
        }

        // Registrar operación
        if (registro != null) {
            try {
                registro.agregarOperacionPendiente(
                        new Operacion(
                                registro.getUltimoIdOperacionPendiente() + 1,
                                System.currentTimeMillis(),
                                TipoOperacion.ASSIGN.getValor(),
                                producto.getId(),
                                estanteria.getId(),
                                producto.getCantidad(),
                                exito ? EstadoOperacion.ENVIADA.getValor() : EstadoOperacion.PENDIENTE.getValor()
                        )
                );
            } catch (Exception regError) {
                android.util.Log.e("ProductoRepo", "Error al registrar operación pendiente: " + regError.getMessage());
            }
        }

    }

    @Override
    public Producto getProductoById(Long id) {
        Producto producto = null;
        try {
            producto = local.getProductoById(id);
            if (producto != null) {
                android.util.Log.d("ProductoRepo", "getProductoById desde local: " + producto.getNombre());
                sincronizar(producto);
                return producto;
            }
        } catch (Exception e) {
            android.util.Log.e("ProductoRepo", "Error en local.getProductoById: " + e.getMessage());
        }

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


        android.util.Log.w("ProductoRepo", "Producto no encontrado ni en remote ni en local, ID: " + id);
        return null;
    }

    @Override
    public void sincronizar(Producto... productos) {
        remote.subirCambios(productos);
        local.bajarCambios(productos);
    }

    @Override
    public List<Producto> getAllProductos() {
        List<Producto> productos = Collections.emptyList();

        try {
            productos = local.getAllProductos();
            if (productos != null) {
                android.util.Log.d("ProductoRepo", "getAllProductos desde local: " + productos.size() + " productos");
                sincronizar(productos.toArray(new Producto[0]));
                return productos;
            }
        } catch (Exception e) {
            android.util.Log.e("ProductoRepo", "Error en local.getAllProductos: " + e.getMessage());
        }

        try {
            productos = remote.getAllProductos();
            if (productos != null && !productos.isEmpty()) {
                android.util.Log.d("ProductoRepo", "getAllProductos desde remote: " + productos.size() + " productos");

                return productos;
            }
        } catch (Exception e) {
            android.util.Log.e("ProductoRepo", "Error en remote.getAllProductos: " + e.getMessage());
        }

        android.util.Log.w("ProductoRepo", "No se pudieron obtener productos ni de remote ni de local");
        return Collections.emptyList();
    }
}
