package dev.wdona.gestorinventarioqr.mock;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

import dev.wdona.gestorinventarioqr.domain.model.Estanteria;
import dev.wdona.gestorinventarioqr.domain.model.Producto;

public class MockDatabaseController implements MockDatabaseOperations{
    @Override
    public void addUndsProduct(Producto producto, int cantidad) throws JSONException {
        System.out.println("Producto: " + producto + ", Cantidad añadida: " + cantidad);

        JSONObject jsonProducto = new JSONObject();

        jsonProducto.put("id", producto.getId());
        jsonProducto.put("cantidad", producto.getCantidad() + cantidad);

        JSONUtils.modificarJSONObjectEnArchivo(jsonProducto, "productos.json");
    }

    @Override
    public void removeUndsProduct(Producto producto, int cantidad) throws JSONException {
        System.out.println("Producto: " + producto + ", Cantidad restada: " + cantidad);

        JSONObject jsonProducto = new JSONObject();

        jsonProducto.put("id", producto.getId());
        jsonProducto.put("cantidad", producto.getCantidad() - cantidad);

        JSONUtils.modificarJSONObjectEnArchivo(jsonProducto, "productos.json");
    }

    @Override
    public void assignProductToEstanteria(Producto producto, Estanteria estanteria) throws JSONException {
        System.out.println("Producto: " + producto + ", Estanteria asignada: " + estanteria);
        JSONObject jsonProducto = new JSONObject();

        jsonProducto.put("id", producto.getId());
        jsonProducto.put("estanteriaId", estanteria.getId());

        JSONUtils.modificarJSONObjectEnArchivo(jsonProducto, "productos.json");
    }

    @Override
    public void addProducto(Producto producto) throws JSONException {
        System.out.println("Producto añadido: " + producto);
        JSONObject jsonProducto = new JSONObject();

        jsonProducto.put("id", producto.getId());
        jsonProducto.put("nombre", producto.getNombre());
        jsonProducto.put("cantidad", producto.getCantidad());
        jsonProducto.put("precio", producto.getPrecio());
        jsonProducto.put("estanteriaId", producto.getEstanteria() != null ? producto.getEstanteria().getId() : JSONObject.NULL);

        JSONUtils.anadirJSONObjectAlArchivo(jsonProducto, "productos.json");
    }

    @Override
    public Producto getProductoById(Long id) throws JSONException {
        JSONObject jsonProductos = JSONUtils.cargarJSONDesdeArchivo("productos.json");
        if (jsonProductos.has(String.valueOf(id))) {
            JSONObject jsonProducto = jsonProductos.getJSONObject(String.valueOf(id));
            Long productoId = jsonProducto.getLong("id");
            String nombreProducto = jsonProducto.getString("nombre");
            double precio = jsonProducto.optDouble("precio", 0.0);
            int cantidad = jsonProducto.getInt("cantidad");
            Long estanteriaId = jsonProducto.optLong("estanteriaId", -1);

            Estanteria estanteria = null;

            if (estanteriaId != -1) {
                JSONObject jsonEstanterias = JSONUtils.cargarJSONDesdeArchivo("estanterias.json");
                if (jsonEstanterias.has(String.valueOf(estanteriaId))) {
                    JSONObject jsonEstanteria = jsonEstanterias.getJSONObject(String.valueOf(estanteriaId));
                    String nombreEstanteria = jsonEstanteria.optString("nombre", "Estanteria " + estanteriaId);
                    estanteria = new Estanteria(estanteriaId, nombreEstanteria);
                } else {
                    System.out.println("Advertencia: No se encontró la estantería con ID " + estanteriaId + ", producto sin estantería.");
                }
            }

            return new Producto(productoId, nombreProducto, precio, cantidad, estanteria);
        } else {
            System.out.println("Error: No se encontró el producto con ID " + id + " en el archivo.");
            return null;
        }
    }

    @Override
    public Estanteria getEstanteriaById(Long id) throws JSONException {
        JSONObject jsonEstanterias = JSONUtils.cargarJSONDesdeArchivo("estanterias.json");
        if (jsonEstanterias.has(String.valueOf(id))) {
            JSONObject jsonEstanteria = jsonEstanterias.getJSONObject(String.valueOf(id));
            String nombreEstanteria = jsonEstanteria.optString("nombre", "Estanteria " + id);
            Estanteria estanteria = new Estanteria(id, nombreEstanteria);

            JSONObject jsonProductos = JSONUtils.cargarJSONDesdeArchivo("productos.json");
            Iterator<String> keys = jsonProductos.keys();
            while (keys.hasNext()) {
                String key = keys.next();

                JSONObject jsonProducto = jsonProductos.getJSONObject(key);
                Long estanteriaId = jsonProducto.optLong("estanteriaId", -1);

                if (estanteriaId.equals(id)) {
                    Long productoId = jsonProducto.getLong("id");
                    String nombreProducto = jsonProducto.getString("nombre");
                    double precio = jsonProducto.getDouble("precio");
                    int cantidad = jsonProducto.getInt("cantidad");
                    Producto producto = new Producto(productoId, nombreProducto, precio, cantidad, estanteria);
                    estanteria.addProducto(producto);
                }
            }

            return estanteria;
        } else {
            System.out.println("Error: No se encontró la estantería con ID " + id + " en el archivo.");
            return null;
        }
    }

    public static void initialize() {
        try {
            if (JSONUtils.crearArchivoSiNoExiste("estanterias.json")) {
                JSONUtils.mockearEstanteriasBase();
            }
            if (JSONUtils.crearArchivoSiNoExiste("productos.json")) {
                JSONUtils.mockearProductosBase();
            }
        } catch (Exception e) {
            System.out.println("Error al inicializar archivos JSON: " + e.getMessage());
        }
    }

}
