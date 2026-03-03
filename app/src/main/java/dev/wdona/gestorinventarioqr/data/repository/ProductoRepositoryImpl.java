package dev.wdona.gestorinventarioqr.data.repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dev.wdona.gestorinventarioqr.data.EstadoOperacion;
import dev.wdona.gestorinventarioqr.data.TipoOperacion;
import dev.wdona.gestorinventarioqr.data.datasource.local.ProductoLocalDataSource;
import dev.wdona.gestorinventarioqr.data.datasource.local.impl.ProductoLocalDataSourceImpl;
import dev.wdona.gestorinventarioqr.data.datasource.remote.ProductoRemoteDataSource;
import dev.wdona.gestorinventarioqr.data.datasource.remote.impl.ProductoRemoteDataSourceImpl;
import dev.wdona.gestorinventarioqr.domain.repository.OperacionRepository;
import dev.wdona.gestorinventarioqr.domain.repository.ProductoRepository;
import dev.wdona.gestorinventarioqr.domain.model.Estanteria;
import dev.wdona.gestorinventarioqr.domain.model.Operacion;
import dev.wdona.gestorinventarioqr.domain.model.Producto;
import dev.wdona.gestorinventarioqr.mock.MockConfig;

public class ProductoRepositoryImpl implements ProductoRepository {
    ProductoRemoteDataSource remote;
    ProductoLocalDataSource local;
    OperacionRepository registro;

    public ProductoRepositoryImpl(ProductoRemoteDataSourceImpl remote, ProductoLocalDataSourceImpl local, OperacionRepositoryImpl registro) {
        this.remote = remote;
        this.local = local;
        this.registro = registro;
    }

    @Override
    public void addUndsProduct(Producto producto, int cantidad) throws Exception {
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
                Long ultimoId = registro.getUltimoIdOperacionPendiente();

                if (ultimoId == null) {
                    ultimoId = -1L;
                }

                registro.agregarOperacionPendiente(
                        new Operacion(
                                ultimoId + 1,
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
    public void removeUndsProduct(Producto producto, int cantidad) throws Exception {
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
    public void assignProductToEstanteria(Producto producto, Estanteria estanteria) throws Exception {
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
        List<Producto> productosLocales = new ArrayList<>();
        List<Producto> productosRemotos = new ArrayList<>();

        // Obtener productos locales
        try {
            productosLocales = local.getAllProductos();
            android.util.Log.d("ProductoRepo", "Productos locales: " + productosLocales.size());
        } catch (Exception e) {
            android.util.Log.e("ProductoRepo", "Error obteniendo locales: " + e.getMessage());
        }

        // Obtener productos remotos (solo si está online)
        if (!MockConfig.isOffline()) {
            try {
                productosRemotos = remote.getAllProductos();
                android.util.Log.d("ProductoRepo", "Productos remotos: " + productosRemotos.size());
            } catch (Exception e) {
                android.util.Log.e("ProductoRepo", "Error obteniendo remotos: " + e.getMessage());
            }
        }

        // Fusionar: local prevalece, pero añadir remotos que no existan en local
        Map<Long, Producto> productosMap = new HashMap<>();

        // Primero añadir remotos
        for (Producto remoto : productosRemotos) {
            if (remoto.getId() != null) {
                productosMap.put(remoto.getId(), remoto);
            }
        }

        // Luego sobrescribir con locales (prevalecen)
        for (Producto localProd : productosLocales) {
            if (localProd.getId() != null) {
                productosMap.put(localProd.getId(), localProd);
            }
        }

        List<Producto> resultado = new ArrayList<>(productosMap.values());

        // Guardar fusión en local para tener productos remotos nuevos
        if (!productosRemotos.isEmpty()) {
            for (Producto remoto : productosRemotos) {
                Producto localProducto = local.getProductoById(remoto.getId());
                if (localProducto == null) {
                    try {
                        local.insertProducto(remoto);
                        android.util.Log.d("ProductoRepo", "Producto remoto guardado en local: " + remoto.getNombre());
                    } catch (Exception e) {
                        android.util.Log.e("ProductoRepo", "Error guardando remoto en local: " + e.getMessage());
                    }
                }
            }
        }

        return resultado;
    }


}
