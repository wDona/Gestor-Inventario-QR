package dev.wdona.gestorinventarioqr.mock;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import dev.wdona.gestorinventarioqr.data.datasource.mapper.ProductoMapper;
import dev.wdona.gestorinventarioqr.domain.model.Estanteria;
import dev.wdona.gestorinventarioqr.domain.model.Producto;

public class MockDatabaseController implements MockDatabaseOperations{
    @Override
    public void addUndsProduct(Producto producto, int cantidad) throws JSONException {
        android.util.Log.e("DatabaseController", "Producto: " + producto + ", Cantidad añadida: " + cantidad);

        JSONObject jsonProductos = JSONUtils.cargarJSONDesdeArchivo("productos.json");
        String productoIdStr = String.valueOf(producto.getId());
        if (jsonProductos.has(productoIdStr)) {
            JSONObject jsonProductoExistente = jsonProductos.getJSONObject(productoIdStr);

            Producto productoObjeto = ProductoMapper.toDomain(jsonProductoExistente);

            productoObjeto.setCantidad(productoObjeto.getCantidad() + cantidad);
            JSONObject jsonProductoActualizado = ProductoMapper.toJSON(productoObjeto);
            JSONUtils.modificarJSONObjectEnArchivo(jsonProductoActualizado, "productos.json");
        }
    }

    @Override
    public void removeUndsProduct(Producto producto, int cantidad) throws JSONException {
//        throw new RuntimeException("Fake error al quitar unidades del producto para probar manejo de errores");
        android.util.Log.e("DatabaseController", "Producto: " + producto + ", Cantidad restada: " + cantidad);

        JSONObject jsonProductos = JSONUtils.cargarJSONDesdeArchivo("productos.json");
        String productoIdStr = String.valueOf(producto.getId());
        if (jsonProductos.has(productoIdStr)) {
            JSONObject jsonProductoExistente = jsonProductos.getJSONObject(productoIdStr);

            Producto productoObjeto = ProductoMapper.toDomain(jsonProductoExistente);

            productoObjeto.setCantidad(productoObjeto.getCantidad() - cantidad);
            JSONObject jsonProductoActualizado = ProductoMapper.toJSON(productoObjeto);
            JSONUtils.modificarJSONObjectEnArchivo(jsonProductoActualizado, "productos.json");
        }
    }

    @Override
    public void assignProductToEstanteria(Producto producto, Estanteria estanteria) throws JSONException {
//        throw new RuntimeException("Fake error al asignar producto a estanteria para probar manejo de errores");
        android.util.Log.e("DatabaseController", "Producto: " + producto + ", Estanteria asignada: " + estanteria);

        JSONObject jsonProductos = JSONUtils.cargarJSONDesdeArchivo("productos.json");
        String productoIdStr = String.valueOf(producto.getId());
        if (jsonProductos.has(productoIdStr)) {
            JSONObject jsonProductoExistente = jsonProductos.getJSONObject(productoIdStr);

            Producto productoObjeto = ProductoMapper.toDomain(jsonProductoExistente);

            productoObjeto.setEstanteria(estanteria);
            JSONObject jsonProductoActualizado = ProductoMapper.toJSON(productoObjeto);
            JSONUtils.modificarJSONObjectEnArchivo(jsonProductoActualizado, "productos.json");
        }
    }

    @Override
    public void addProducto(Producto producto) throws JSONException {
        android.util.Log.e("DatabaseController", "Producto añadido: " + producto);
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
            android.util.Log.e("DatabaseController", "Producto añadido. ID: " + id + " encontrado en el archivo.");
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
                    android.util.Log.e("DatabaseController", "Estanteria encontrada. ID: " + estanteriaId + " encontrado en el archivo. Al hacer getProductoById.");
                    JSONObject jsonEstanteria = jsonEstanterias.getJSONObject(String.valueOf(estanteriaId));
                    String nombreEstanteria = jsonEstanteria.optString("nombre", "Estanteria " + estanteriaId);
                    estanteria = new Estanteria(estanteriaId, nombreEstanteria);
                } else {
                    android.util.Log.e("DatabaseController", "Advertencia: No se encontró la estantería con ID " + estanteriaId + ", producto sin estantería.");
                }
            }

            return new Producto(productoId, nombreProducto, precio, cantidad, estanteria);
        } else {
            android.util.Log.e("DatabaseController", "Error: No se encontró el producto con ID " + id + " en el archivo.");
            return null;
        }
    }

    @Override
    public Estanteria getEstanteriaById(Long estanteriaId) throws JSONException {
        JSONObject jsonEstanterias = JSONUtils.cargarJSONDesdeArchivo("estanterias.json");
        if (jsonEstanterias.has(String.valueOf(estanteriaId))) {
            android.util.Log.e("DatabaseController", "(mockdbcontroller) Estanteria encontrada. ID: " + estanteriaId + " encontrado en el archivo.");

            JSONObject jsonEstanteria = jsonEstanterias.getJSONObject(String.valueOf(estanteriaId));
            String nombreEstanteria = jsonEstanteria.optString("nombre", "Estanteria " + estanteriaId);
            Estanteria estanteria = new Estanteria(estanteriaId, nombreEstanteria);

            JSONObject jsonProductos = JSONUtils.cargarJSONDesdeArchivo("productos.json");
            Iterator<String> keys = jsonProductos.keys();
            android.util.Log.e("DatabaseController", "Keys en productos.json: " + jsonProductos.length());

            while (keys.hasNext()) {
                String key = keys.next();

                JSONObject jsonProducto = jsonProductos.getJSONObject(key);
                Long FKestanteriaId = jsonProducto.optLong("estanteriaId", -1);

                if (FKestanteriaId.equals(estanteriaId)) {
                    Log.e("DatabaseController", "Producto con estanteriaId " + estanteriaId + " encontrado: " + jsonProducto.optString("nombre", "Producto sin nombre"));
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
            android.util.Log.e("DatabaseController", "Error: No se encontró la estantería con ID " + estanteriaId + " en el archivo.");
            return null;
        }
    }

    @Override
    public Estanteria getEstanteriaConProductosById(Long idEstanteria) throws JSONException {
        return getEstanteriaById(idEstanteria);
    }

    @Override
    public void subirCambiosProducto(Producto... productos) throws JSONException {
        if (productos == null || productos.length == 0) {
            throw new IllegalArgumentException("El array de productos no puede ser nulo o estar vacío.");
        }
//        throw new RuntimeException("Fake error al subir cambios del producto para probar manejo de errores");

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

    @Override
    public void subirCambiosEstanteria(Estanteria... estanterias) throws JSONException {
        if (estanterias == null || estanterias.length == 0) {
            throw new IllegalArgumentException("El array de productos no puede ser nulo o estar vacío.");
        }
//        throw new RuntimeException("Fake error al subir cambios del producto para probar manejo de errores");

        for (Estanteria estanteria : estanterias) {
            if (estanteria == null || estanteria.getId() == null) {
                continue;
            }

            // Crear JSONObject con todos los campos del producto
            JSONObject jsonProducto = new JSONObject();
            jsonProducto.put("id", estanteria.getId());
            jsonProducto.put("nombre", estanteria.getNombre());

            // Actualizar en el archivo JSON (solo si existe)
            JSONUtils.modificarJSONObjectEnArchivo(jsonProducto, "estanterias.json");

            Log.d("MockDB", "Estanteria actualizada en JSON: " + estanteria.getNombre());
        }
    }

    @Override
    public List<Producto> getAllProductos() throws JSONException {
//        throw new RuntimeException("Fake error al obtener todos los productos para probar manejo de errores");
        JSONObject jsonProductos = JSONUtils.cargarJSONDesdeArchivo("productos.json");
        if (jsonProductos.length() == 0) {
            return Collections.emptyList();
        }

        List<Producto> productos = new java.util.ArrayList<>();
        Iterator<String> keys = jsonProductos.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            JSONObject jsonProducto = jsonProductos.getJSONObject(key);
            Long productoId = jsonProducto.getLong("id");
            String nombreProducto = jsonProducto.getString("nombre");
            double precio = jsonProducto.optDouble("precio", 0.0);
            int cantidad = jsonProducto.getInt("cantidad");
            Long estanteriaId = jsonProducto.optLong("estanteriaId", -1);

            Estanteria estanteria = null;
            if (estanteriaId != -1) {
                estanteria = getEstanteriaById(estanteriaId);
            }

            Producto producto = new Producto(productoId, nombreProducto, precio, cantidad, estanteria);
            productos.add(producto);
        }

        return productos;
    }


    public static void initialize() {
        try {
            android.util.Log.d("MockDB", "Inicializando MockDatabaseController...");

//            JSONUtils.eliminarArchivo("estanterias.json");
//            JSONUtils.eliminarArchivo("productos.json");
//            JSONUtils.escribirJSONDeNuevo(new org.json.JSONObject(), "estanterias.json");
//            JSONUtils.escribirJSONDeNuevo(new org.json.JSONObject(), "productos.json");
            JSONUtils.crearArchivoSiNoExiste("estanterias.json");
            JSONUtils.crearArchivoSiNoExiste("productos.json");
            android.util.Log.d("MockDB", "Archivos JSON vaciados");

            // Luego poblar con datos nuevos
            android.util.Log.d("MockDB", "Poblando estanterias.json...");
            org.json.JSONObject estanteriasJson = JSONUtils.cargarJSONDesdeArchivo("estanterias.json");
            if (estanteriasJson.length() == 0) {
                android.util.Log.d("MockDB", "Archivo estanterias.json está vacío, poblando con datos base...");
                JSONUtils.mockearEstanteriasBase();
            }

            org.json.JSONObject productosJson = JSONUtils.cargarJSONDesdeArchivo("productos.json");
            if (productosJson.length() == 0) {
                android.util.Log.d("MockDB", "Archivo productos.json está vacío, poblando con datos base...");
                JSONUtils.mockearProductosBase();
            }

            android.util.Log.d("MockDB", "MockDatabaseController inicializado correctamente");
        } catch (Exception e) {
            android.util.Log.e("MockDB", "Error al inicializar archivos JSON: " + e.getMessage(), e);
        }

    }

}
