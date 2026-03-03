package dev.wdona.gestorinventarioqr.data.datasource.local.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dev.wdona.gestorinventarioqr.data.datasource.local.ProductoLocalDataSource;
import dev.wdona.gestorinventarioqr.data.datasource.mapper.EstanteriaMapper;
import dev.wdona.gestorinventarioqr.data.datasource.mapper.ProductoMapper;
import dev.wdona.gestorinventarioqr.data.db.EstanteriaDao;
import dev.wdona.gestorinventarioqr.data.db.ProductoDao;
import dev.wdona.gestorinventarioqr.data.db.ProductoEstanteriaDao;
import dev.wdona.gestorinventarioqr.data.entity.EstanteriaEntity;
import dev.wdona.gestorinventarioqr.data.entity.ProductoEntity;
import dev.wdona.gestorinventarioqr.data.entity.ProductoEstanteriaEntity;
import dev.wdona.gestorinventarioqr.domain.model.Estanteria;
import dev.wdona.gestorinventarioqr.domain.model.Producto;

public class ProductoLocalDataSourceImpl implements ProductoLocalDataSource {
    ProductoDao dao;
    EstanteriaDao estanteriaDao;
    ProductoEstanteriaDao peDao;

    public ProductoLocalDataSourceImpl(ProductoDao dao, EstanteriaDao estanteriaDao, ProductoEstanteriaDao peDao) {
        this.dao = dao;
        this.estanteriaDao = estanteriaDao;
        this.peDao = peDao;
    }

    @Override
    public void addUndsProduct(Producto producto, int cantidad) {
        try {
            if (producto.getEstanteria() == null) {
                android.util.Log.e("ProductoLocalDS", "No se puede añadir sin estantería");
                return;
            }
            Long productoId = producto.getId();
            Long estanteriaId = producto.getEstanteria().getId();

            // Verificar si existe la relación
            ProductoEstanteriaEntity relacion = peDao.getRelacion(productoId, estanteriaId);
            if (relacion != null) {
                peDao.addUnds(productoId, estanteriaId, cantidad);
            } else {
                // Crear nueva relación
                ProductoEstanteriaEntity nueva = new ProductoEstanteriaEntity();
                nueva.setProductoId(productoId);
                nueva.setEstanteriaId(estanteriaId);
                nueva.setCantidad(cantidad);
                peDao.insertar(nueva);
            }
        } catch (Exception e) {
            android.util.Log.e("ProductoLocalDS", "Error addUnds: " + e.getMessage());
        }
    }

    @Override
    public void removeUndsProduct(Producto producto, int cantidad) {
        try {
            if (producto.getEstanteria() == null) {
                android.util.Log.e("ProductoLocalDS", "No se puede quitar sin estantería");
                return;
            }
            Long productoId = producto.getId();
            Long estanteriaId = producto.getEstanteria().getId();

            peDao.removeUnds(productoId, estanteriaId, cantidad);

            // Si la cantidad llega a 0 o menos, eliminar la relación
            ProductoEstanteriaEntity relacion = peDao.getRelacion(productoId, estanteriaId);
            if (relacion != null && relacion.getCantidad() <= 0) {
                peDao.eliminarRelacion(productoId, estanteriaId);
            }
        } catch (Exception e) {
            android.util.Log.e("ProductoLocalDS", "Error removeUnds: " + e.getMessage());
        }
    }

    @Override
    public void assignProductToEstanteria(Producto producto, Estanteria estanteria) {
        try {
            // Esto ahora crea/actualiza una relación en la tabla intermedia
            ProductoEstanteriaEntity relacion = peDao.getRelacion(producto.getId(), estanteria.getId());
            if (relacion != null) {
                // Ya existe, sumar la cantidad
                peDao.addUnds(producto.getId(), estanteria.getId(), producto.getCantidad());
            } else {
                // Crear nueva relación
                ProductoEstanteriaEntity nueva = new ProductoEstanteriaEntity();
                nueva.setProductoId(producto.getId());
                nueva.setEstanteriaId(estanteria.getId());
                nueva.setCantidad(producto.getCantidad());
                peDao.insertar(nueva);
            }
        } catch (Exception e) {
            android.util.Log.e("ProductoLocalDS", "Error assignProduct: " + e.getMessage());
        }
    }

    /**
     * Mover cantidad de un producto de una estantería a otra
     */
    public void moverCantidad(Long productoId, Long estanteriaOrigenId, Long estanteriaDestinoId, int cantidad) {
        try {
            // Restar del origen
            peDao.removeUnds(productoId, estanteriaOrigenId, cantidad);

            // Verificar si queda algo en origen
            ProductoEstanteriaEntity relOrigen = peDao.getRelacion(productoId, estanteriaOrigenId);
            if (relOrigen != null && relOrigen.getCantidad() <= 0) {
                peDao.eliminarRelacion(productoId, estanteriaOrigenId);
            }

            // Sumar al destino
            ProductoEstanteriaEntity relDestino = peDao.getRelacion(productoId, estanteriaDestinoId);
            if (relDestino != null) {
                peDao.addUnds(productoId, estanteriaDestinoId, cantidad);
            } else {
                ProductoEstanteriaEntity nueva = new ProductoEstanteriaEntity();
                nueva.setProductoId(productoId);
                nueva.setEstanteriaId(estanteriaDestinoId);
                nueva.setCantidad(cantidad);
                peDao.insertar(nueva);
            }
        } catch (Exception e) {
            android.util.Log.e("ProductoLocalDS", "Error moverCantidad: " + e.getMessage());
        }
    }

    /**
     * Obtiene un producto con la cantidad específica que tiene en una estantería concreta.
     */
    public Producto getProductoEnEstanteria(Long productoId, Long estanteriaId) {
        try {
            ProductoEntity entity = dao.getProductoById(productoId);
            if (entity == null) return null;

            ProductoEstanteriaEntity relacion = peDao.getRelacion(productoId, estanteriaId);
            if (relacion == null) return null;

            EstanteriaEntity estEntity = estanteriaDao.getEstanteriaById(estanteriaId);
            Estanteria estanteria = estEntity != null ? EstanteriaMapper.toDomain(estEntity) : null;

            return ProductoMapper.toDomain(entity, estanteria, relacion.getCantidad());
        } catch (Exception e) {
            android.util.Log.e("ProductoLocalDS", "Error getProductoEnEstanteria: " + e.getMessage());
            return null;
        }
    }

    /**
     * Devuelve una lista de Producto, uno por cada estantería donde está,
     * cada uno con la cantidad específica de esa estantería.
     */
    public List<Producto> getUbicacionesProducto(Long productoId) {
        List<Producto> ubicaciones = new ArrayList<>();
        try {
            ProductoEntity entity = dao.getProductoById(productoId);
            if (entity == null) return ubicaciones;

            List<ProductoEstanteriaEntity> relaciones = peDao.getEstanteriasPorProducto(productoId);
            if (relaciones == null) return ubicaciones;

            for (ProductoEstanteriaEntity rel : relaciones) {
                EstanteriaEntity estEntity = estanteriaDao.getEstanteriaById(rel.getEstanteriaId());
                Estanteria estanteria = estEntity != null ? EstanteriaMapper.toDomain(estEntity) : null;
                ubicaciones.add(ProductoMapper.toDomain(entity, estanteria, rel.getCantidad()));
            }
        } catch (Exception e) {
            android.util.Log.e("ProductoLocalDS", "Error getUbicacionesProducto: " + e.getMessage());
        }
        return ubicaciones;
    }

    @Override
    public Producto getProductoById(Long id) {
        try {
            ProductoEntity entity = dao.getProductoById(id);
            if (entity == null) {
                return null;
            }

            // Obtener la primera relación con estantería (para contexto)
            List<ProductoEstanteriaEntity> relaciones = peDao.getEstanteriasPorProducto(id);
            if (relaciones != null && !relaciones.isEmpty()) {
                ProductoEstanteriaEntity primeraRelacion = relaciones.get(0);
                EstanteriaEntity estEntity = estanteriaDao.getEstanteriaById(primeraRelacion.getEstanteriaId());
                Estanteria estanteria = estEntity != null ? EstanteriaMapper.toDomain(estEntity) : null;
                int cantidadTotal = peDao.getCantidadTotalProducto(id);
                return ProductoMapper.toDomain(entity, estanteria, cantidadTotal);
            }

            return ProductoMapper.toDomain(entity);
        } catch (Exception e) {
            android.util.Log.e("ProductoLocalDS", "Error getProductoById: " + e.getMessage());
            return null;
        }
    }

    @Override
    public List<Producto> getAllProductos() {
        try {
            List<ProductoEntity> entities = dao.getAllProductos();
            if (entities == null || entities.isEmpty()) {
                return Collections.emptyList();
            }

            List<Producto> productos = new ArrayList<>();
            for (ProductoEntity entity : entities) {
                List<ProductoEstanteriaEntity> relaciones = peDao.getEstanteriasPorProducto(entity.getId());
                int cantidadTotal = peDao.getCantidadTotalProducto(entity.getId());

                Estanteria estanteria = null;
                if (relaciones != null && !relaciones.isEmpty()) {
                    EstanteriaEntity estEntity = estanteriaDao.getEstanteriaById(relaciones.get(0).getEstanteriaId());
                    estanteria = estEntity != null ? EstanteriaMapper.toDomain(estEntity) : null;
                }

                productos.add(ProductoMapper.toDomain(entity, estanteria, cantidadTotal));
            }
            return productos;
        } catch (Exception e) {
            android.util.Log.e("ProductoLocalDS", "Error getAllProductos: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    @Override
    public void bajarCambios(Producto... productos) {
        try {
            for (Producto producto : productos) {
                // Actualizar datos base del producto
                dao.insertProducto(ProductoMapper.toEntity(producto));

                // Actualizar relación si tiene estantería
                if (producto.getEstanteria() != null) {
                    ProductoEstanteriaEntity pe = ProductoMapper.toRelacionEntity(producto);
                    if (pe != null) {
                        peDao.insertar(pe); // REPLACE si ya existe
                    }
                }
            }
        } catch (Exception e) {
            android.util.Log.e("ProductoLocalDS", "Error bajarCambios: " + e.getMessage());
        }
    }

    @Override
    public void insertProducto(Producto producto) {
        try {
            dao.insertProducto(ProductoMapper.toEntity(producto));

            // Si tiene contexto de estantería, crear la relación
            if (producto.getEstanteria() != null && producto.getCantidad() > 0) {
                ProductoEstanteriaEntity pe = ProductoMapper.toRelacionEntity(producto);
                if (pe != null) {
                    peDao.insertar(pe);
                }
            }
        } catch (Exception e) {
            android.util.Log.e("ProductoLocalDS", "Error insertProducto: " + e.getMessage());
        }
    }
}
