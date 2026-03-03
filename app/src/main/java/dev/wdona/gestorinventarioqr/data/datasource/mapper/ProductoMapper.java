package dev.wdona.gestorinventarioqr.data.datasource.mapper;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import dev.wdona.gestorinventarioqr.data.entity.ProductoEntity;
import dev.wdona.gestorinventarioqr.data.entity.ProductoEstanteriaEntity;
import dev.wdona.gestorinventarioqr.domain.model.Estanteria;
import dev.wdona.gestorinventarioqr.domain.model.Producto;

public class ProductoMapper {

    /**
     * Convierte ProductoEntity + contexto de estantería a dominio
     */
    public static Producto toDomain(ProductoEntity entity, Estanteria estanteria, int cantidad) {
        if (entity == null) {
            return null;
        }
        return new Producto(
                entity.getId(),
                entity.getNombre(),
                entity.getPrecio(),
                cantidad,
                estanteria
        );
    }

    /**
     * Convierte ProductoEntity a dominio sin contexto de estantería
     */
    public static Producto toDomain(ProductoEntity entity) {
        if (entity == null) {
            return null;
        }
        return new Producto(
                entity.getId(),
                entity.getNombre(),
                entity.getPrecio()
        );
    }

    public static ProductoEntity toEntity(Producto producto) {
        if (producto == null) {
            return null;
        }
        ProductoEntity entity = new ProductoEntity();
        entity.setId(producto.getId());
        entity.setNombre(producto.getNombre());
        entity.setPrecio(producto.getPrecio());
        return entity;
    }

    public static ProductoEstanteriaEntity toRelacionEntity(Producto producto) {
        if (producto == null || producto.getEstanteria() == null) {
            return null;
        }
        ProductoEstanteriaEntity pe = new ProductoEstanteriaEntity();
        pe.setProductoId(producto.getId());
        pe.setEstanteriaId(producto.getEstanteria().getId());
        pe.setCantidad(producto.getCantidad());
        return pe;
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
            Estanteria estanteria = estanteriaId != null ? new Estanteria(estanteriaId, null) : null;

            return new Producto(id, nombre, precio, cantidad, estanteria);
        } catch (Exception e) {
            System.out.println("Error al convertir JSON a Producto: " + e.getMessage());
            return null;
        }
    }

    public static List<ProductoEntity> toEntityList(List<Producto> productos) {
        if (productos == null) {
            return null;
        }
        List<ProductoEntity> entities = new ArrayList<>();
        for (Producto producto : productos) {
            entities.add(toEntity(producto));
        }
        return entities;
    }

    public static List<ProductoEntity> toEntityList(Producto... productos) {
        if (productos == null) {
            return null;
        }
        List<ProductoEntity> entities = new ArrayList<>();
        for (Producto producto : productos) {
            entities.add(toEntity(producto));
        }
        return entities;
    }
}
