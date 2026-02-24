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

    @Override
    public Estanteria getEstanteriaConProductosById(Long idEstanteria) throws JSONException {
        return getEstanteriaById(idEstanteria);
    }

    @Override
    public void subirCambios(Producto... productos) throws JSONException {
        if (productos == null || productos.length == 0) {
            return;
        }

        for (Producto producto : productos) {
            if (producto == null || producto.getId() == null) {
                continue;
            }

            // Crear JSONObject con todos los campos del producto
            JSONObject jsonProducto = new JSONObject();
            jsonProducto.put("id", producto.getId());
            jsonProducto.put("nombre", producto.getNombre());
            jsonProducto.put("precio", producto.getPrecio());
            jsonProducto.put("cantidad", producto.getCantidad());
            if (producto.getEstanteria() != null) {
                jsonProducto.put("estanteriaId", producto.getEstanteria().getId());
            } else {
                jsonProducto.put("estanteriaId", JSONObject.NULL);
            }

            // Actualizar en el archivo JSON (solo si existe)
            JSONUtils.modificarJSONObjectEnArchivo(jsonProducto, "productos.json");

            android.util.Log.d("MockDB", "Producto actualizado en JSON: " + producto.getNombre());
        }
    }


    public static void initialize() {
        try {
            android.util.Log.d("MockDB", "Inicializando MockDatabaseController...");

            // Siempre crear archivos si no existen
            JSONUtils.crearArchivoSiNoExiste("estanterias.json");
            JSONUtils.crearArchivoSiNoExiste("productos.json");

            // Verificar y poblar estanterias
            boolean necesitaPoblarEstanterias = false;
            try {
                org.json.JSONObject estanteriasJson = JSONUtils.cargarJSONDesdeArchivo("estanterias.json");
                necesitaPoblarEstanterias = (estanteriasJson.length() == 0 || !estanteriasJson.has("1"));
                android.util.Log.d("MockDB", "estanterias.json tiene " + estanteriasJson.length() + " elementos");
            } catch (Exception e) {
                necesitaPoblarEstanterias = true;
                android.util.Log.d("MockDB", "Error leyendo estanterias.json: " + e.getMessage());
            }

            if (necesitaPoblarEstanterias) {
                android.util.Log.d("MockDB", "Poblando estanterias.json...");
                JSONUtils.mockearEstanteriasBase();
            }

            // Verificar y poblar productos
            boolean necesitaPoblarProductos = false;
            try {
                org.json.JSONObject productosJson = JSONUtils.cargarJSONDesdeArchivo("productos.json");
                necesitaPoblarProductos = (productosJson.length() == 0 || !productosJson.has("1"));
                android.util.Log.d("MockDB", "productos.json tiene " + productosJson.length() + " elementos");
            } catch (Exception e) {
                necesitaPoblarProductos = true;
                android.util.Log.d("MockDB", "Error leyendo productos.json: " + e.getMessage());
            }

            if (necesitaPoblarProductos) {
                android.util.Log.d("MockDB", "Poblando productos.json...");
                JSONUtils.mockearProductosBase();
            }

            android.util.Log.d("MockDB", "MockDatabaseController inicializado correctamente");
        } catch (Exception e) {
            android.util.Log.e("MockDB", "Error al inicializar archivos JSON: " + e.getMessage(), e);
        }

    }

}
