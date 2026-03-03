package dev.wdona.gestorinventarioqr.data.repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dev.wdona.gestorinventarioqr.data.EstadoOperacion;
import dev.wdona.gestorinventarioqr.data.TipoOperacion;
import dev.wdona.gestorinventarioqr.data.datasource.local.impl.ProductoLocalDataSourceImpl;
import dev.wdona.gestorinventarioqr.data.datasource.remote.impl.ProductoRemoteDataSourceImpl;
import dev.wdona.gestorinventarioqr.domain.repository.ProductoRepository;
import dev.wdona.gestorinventarioqr.domain.model.Estanteria;
import dev.wdona.gestorinventarioqr.domain.model.Operacion;
import dev.wdona.gestorinventarioqr.domain.model.Producto;
import dev.wdona.gestorinventarioqr.mock.MockConfig;

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
    public void addUndsProduct(Producto producto, int cantidad) throws Exception {
        local.addUndsProduct(producto, cantidad);
        boolean exito = false;
        try {
            remote.addUndsProduct(producto, cantidad);
            exito = true;
        } catch (Exception e) {
            android.util.Log.e("ProductoRepo", "Error en remote.addUndsProduct: " + e.getMessage());
        }

        registrarOperacion(TipoOperacion.ADD.getValor(), producto, cantidad, exito);
    }

    @Override
    public void removeUndsProduct(Producto producto, int cantidad) throws Exception {
        local.removeUndsProduct(producto, cantidad);
        boolean exito = false;
        try {
            remote.removeUndsProduct(producto, cantidad);
            exito = true;
        } catch (Exception e) {
            android.util.Log.e("ProductoRepo", "Error en remote.removeUndsProduct: " + e.getMessage());
        }

        registrarOperacion(TipoOperacion.REMOVE.getValor(), producto, cantidad, exito);
    }

    @Override
    public void assignProductToEstanteria(Producto producto, Estanteria estanteria) throws Exception {
        local.assignProductToEstanteria(producto, estanteria);
        boolean exito = false;
        try {
            remote.assignProductToEstanteria(producto, estanteria);
            exito = true;
        } catch (Exception e) {
            android.util.Log.e("ProductoRepo", "Error en remote.assignProductToEstanteria: " + e.getMessage());
        }

        registrarOperacion(TipoOperacion.ASSIGN.getValor(), producto, producto.getCantidad(), exito, estanteria.getId());
    }

    @Override
    public void moverCantidad(Long productoId, Long estanteriaOrigenId, Long estanteriaDestinoId, int cantidad) throws Exception {
        // Mover en local
        local.moverCantidad(productoId, estanteriaOrigenId, estanteriaDestinoId, cantidad);

        // Mover en remote
        boolean exito = false;
        try {
            Producto productoOrigen = new Producto(productoId, null, 0, cantidad, new Estanteria(estanteriaOrigenId, null));
            remote.removeUndsProduct(productoOrigen, cantidad);

            Producto productoDestino = new Producto(productoId, null, 0, cantidad, new Estanteria(estanteriaDestinoId, null));
            remote.addUndsProduct(productoDestino, cantidad);
            exito = true;
        } catch (Exception e) {
            android.util.Log.e("ProductoRepo", "Error en remote.moverCantidad: " + e.getMessage());
        }

        // Registrar operación
        if (registro != null) {
            try {
                Long ultimoId = registro.getUltimoIdOperacionPendiente();
                if (ultimoId == null) ultimoId = 0L;

                registro.agregarOperacionPendiente(
                        new Operacion(
                                ultimoId + 1,
                                System.currentTimeMillis(),
                                TipoOperacion.ASSIGN.getValor(),
                                productoId,
                                estanteriaDestinoId,
                                cantidad,
                                exito ? EstadoOperacion.ENVIADA.getValor() : EstadoOperacion.PENDIENTE.getValor()
                        )
                );
            } catch (Exception regError) {
                android.util.Log.e("ProductoRepo", "Error registrando operación: " + regError.getMessage());
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
                return producto;
            }
        } catch (Exception e) {
            android.util.Log.e("ProductoRepo", "Error en local.getProductoById: " + e.getMessage());
        }

        try {
            producto = remote.getProductoById(id);
            if (producto != null) {
                android.util.Log.d("ProductoRepo", "getProductoById desde remote: " + producto.getNombre());
                return producto;
            }
        } catch (Exception e) {
            android.util.Log.e("ProductoRepo", "Error en remote.getProductoById: " + e.getMessage());
        }

        android.util.Log.w("ProductoRepo", "Producto no encontrado, ID: " + id);
        return null;
    }

    @Override
    public Producto getProductoEnEstanteria(Long productoId, Long estanteriaId) {
        try {
            Producto producto = local.getProductoEnEstanteria(productoId, estanteriaId);
            if (producto != null) {
                return producto;
            }
        } catch (Exception e) {
            android.util.Log.e("ProductoRepo", "Error en local.getProductoEnEstanteria: " + e.getMessage());
        }
        return null;
    }

    @Override
    public List<Producto> getUbicacionesProducto(Long productoId) {
        try {
            return local.getUbicacionesProducto(productoId);
        } catch (Exception e) {
            android.util.Log.e("ProductoRepo", "Error en local.getUbicacionesProducto: " + e.getMessage());
        }
        return new ArrayList<>();
    }

    @Override
    public void sincronizar(Producto... productos) {
        try {
            remote.subirCambios(productos);
        } catch (Exception e) {
            android.util.Log.e("ProductoRepo", "Error sincronizando remote: " + e.getMessage());
        }
        try {
            local.bajarCambios(productos);
        } catch (Exception e) {
            android.util.Log.e("ProductoRepo", "Error sincronizando local: " + e.getMessage());
        }
    }

    @Override
    public List<Producto> getAllProductos() {
        List<Producto> productosLocales = new ArrayList<>();
        List<Producto> productosRemotos = new ArrayList<>();

        try {
            productosLocales = local.getAllProductos();
            android.util.Log.d("ProductoRepo", "Productos locales: " + productosLocales.size());
        } catch (Exception e) {
            android.util.Log.e("ProductoRepo", "Error obteniendo locales: " + e.getMessage());
        }

        if (!MockConfig.isOffline()) {
            try {
                productosRemotos = remote.getAllProductos();
                android.util.Log.d("ProductoRepo", "Productos remotos: " + productosRemotos.size());
            } catch (Exception e) {
                android.util.Log.e("ProductoRepo", "Error obteniendo remotos: " + e.getMessage());
            }
        }

        Map<Long, Producto> productosMap = new HashMap<>();
        for (Producto remoto : productosRemotos) {
            if (remoto.getId() != null) {
                productosMap.put(remoto.getId(), remoto);
            }
        }
        for (Producto localProd : productosLocales) {
            if (localProd.getId() != null) {
                productosMap.put(localProd.getId(), localProd);
            }
        }

        List<Producto> resultado = new ArrayList<>(productosMap.values());

        if (!productosRemotos.isEmpty()) {
            for (Producto remoto : productosRemotos) {
                Producto localProducto = local.getProductoById(remoto.getId());
                if (localProducto == null) {
                    try {
                        local.insertProducto(remoto);
                    } catch (Exception e) {
                        android.util.Log.e("ProductoRepo", "Error guardando remoto en local: " + e.getMessage());
                    }
                }
            }
        }

        return resultado;
    }

    private void registrarOperacion(String tipo, Producto producto, int cantidad, boolean exito) {
        registrarOperacion(tipo, producto, cantidad, exito, null);
    }

    private void registrarOperacion(String tipo, Producto producto, int cantidad, boolean exito, Long estanteriaDestinoId) {
        if (registro == null) return;
        try {
            Long ultimoId = registro.getUltimoIdOperacionPendiente();
            if (ultimoId == null) ultimoId = 0L;

            registro.agregarOperacionPendiente(
                    new Operacion(
                            ultimoId + 1,
                            System.currentTimeMillis(),
                            tipo,
                            producto.getId(),
                            estanteriaDestinoId != null ? estanteriaDestinoId : (producto.getEstanteria() != null ? producto.getEstanteria().getId() : null),
                            cantidad,
                            exito ? EstadoOperacion.ENVIADA.getValor() : EstadoOperacion.PENDIENTE.getValor()
                    )
            );
        } catch (Exception regError) {
            android.util.Log.e("ProductoRepo", "Error registrando operación: " + regError.getMessage());
        }
    }
}
