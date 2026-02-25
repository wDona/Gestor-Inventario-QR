package dev.wdona.gestorinventarioqr.data.datasource.mapper;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import dev.wdona.gestorinventarioqr.data.db.EstanteriaDao;
import dev.wdona.gestorinventarioqr.data.entity.ProductoEntity;
import dev.wdona.gestorinventarioqr.domain.model.Estanteria;
import dev.wdona.gestorinventarioqr.domain.model.Producto;

public class ProductoMapper {
    public static Producto toDomain(ProductoEntity entity, Estanteria estanteria) {
        if (entity == null) {
            return null;
        }
        return new Producto(
                entity.getId(),
                entity.getNombre(),
                entity.getPrecio(),
                entity.getCantidad(),
                estanteria
        );
    }

    public static List<Producto> toDomainList(List<ProductoEntity> entities, Estanteria estanteria) {
        if (entities == null || estanteria == null) {
            return null;
        }
        List<Producto> productos = new ArrayList<>();
        for (int i = 0; i < entities.size(); i++) {
            productos.add(toDomain(entities.get(i), estanteria));
        }
        return productos;
    }

    /**
     * Convierte una lista de ProductoEntity a Producto obteniendo la estantería de cada uno
     */
    public static List<Producto> toDomainList(List<ProductoEntity> entities, EstanteriaDao estanteriaDao) {
        if (entities == null) {
            return new ArrayList<>();
        }
        List<Producto> productos = new ArrayList<>();
        for (ProductoEntity entity : entities) {
            Estanteria estanteria = null;
            if (entity.getFK_estanteriaId() != null) {
                estanteria = EstanteriaMapper.toDomain(estanteriaDao.getEstanteriaById(entity.getFK_estanteriaId()));
            }
            productos.add(toDomain(entity, estanteria));
        }
        return productos;
    }

    public static ProductoEntity toEntity(Producto producto) {
        if (producto == null) {
            return null;
        }
        ProductoEntity entity = new ProductoEntity();
        entity.setId(producto.getId());
        entity.setNombre(producto.getNombre());
        entity.setPrecio(producto.getPrecio());
        entity.setCantidad(producto.getCantidad());
        if (producto.getEstanteria() != null) {
            entity.setFK_estanteriaId(producto.getEstanteria().getId());
        }
        return entity;
    }

    public static JSONObject toJSON(Producto producto) {
        if (producto == null) {
            return null;
        }
        JSONObject json = new JSONObject();
        try {
            json.put("id", producto.getId());
            json.put("nombre", producto.getNombre());
            json.put("precio", producto.getPrecio());
            json.put("cantidad", producto.getCantidad());
            if (producto.getEstanteria() != null) {
                json.put("estanteriaId", producto.getEstanteria().getId());
            } else {
                json.put("estanteriaId", JSONObject.NULL);
            }
        } catch (Exception e) {
            System.out.println("Error al convertir Producto a JSON: " + e.getMessage());
            return null;
        }
        return json;
    }

    public static Producto toDomain(JSONObject json) {
        if (json == null) {
            return null;
        }
        try {
            Long id = json.optLong("id", -1);
            String nombre = json.optString("nombre", null);
            double precio = json.optDouble("precio", 0);
            int cantidad = json.optInt("cantidad", 0);
            Long estanteriaId = json.has("estanteriaId") && !json.isNull("estanteriaId") ? json.getLong("estanteriaId") : null;
            Estanteria estanteria = new Estanteria(estanteriaId, null, null);

            return new Producto(id, nombre, precio, cantidad, estanteria);
        } catch (Exception e) {
            System.out.println("Error al convertir JSON a Producto: " + e.getMessage());
            return null;
        }
    }
}
