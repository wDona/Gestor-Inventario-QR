package dev.wdona.gestorinventarioqr.mock;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import dev.wdona.gestorinventarioqr.domain.model.Estanteria;
import dev.wdona.gestorinventarioqr.domain.model.Producto;

public class MockDatabaseController implements MockDatabaseOperations {

    private static final String PRODUCTOS_FILE = "productos.json";
    private static final String ESTANTERIAS_FILE = "estanterias.json";
    private static final String RELACIONES_FILE = "producto_estanteria.json";

    /**
     * Genera una clave única para la relación producto-estantería en el JSON
     */
    private String claveRelacion(String productoId, String estanteriaId) {
        return (productoId != null ? productoId.trim() : "null") + "_" + (estanteriaId != null ? estanteriaId.trim() : "null");
    }

    @Override
    public void addUndsProduct(Producto producto, int cantidad) throws JSONException {
        if (producto.getEstanteria() == null) {
            Log.e("MockDB", "No se puede añadir sin estantería");
            return;
        }

        String productoId = producto.getId().trim();
        String estanteriaId = producto.getEstanteria().getId().trim();
        String clave = claveRelacion(productoId, estanteriaId);

        JSONObject relaciones = JSONUtils.cargarJSONDesdeArchivo(RELACIONES_FILE);

        if (relaciones.has(clave)) {
            JSONObject rel = relaciones.getJSONObject(clave);
            int cantidadActual = rel.getInt("cantidad");
            rel.put("cantidad", cantidadActual + cantidad);
            JSONUtils.modificarJSONObjectEnArchivoConClave(rel, RELACIONES_FILE, clave);
        } else {
            // Crear nueva relación
            JSONObject nuevaRel = new JSONObject();
            nuevaRel.put("productoId", productoId);
            nuevaRel.put("estanteriaId", estanteriaId);
            nuevaRel.put("cantidad", cantidad);
            JSONUtils.anadirJSONObjectAlArchivoConClave(nuevaRel, RELACIONES_FILE, clave);
        }

        Log.d("MockDB", "addUnds: producto " + productoId + " en estantería " + estanteriaId + " +" + cantidad);
    }

    @Override
    public void removeUndsProduct(Producto producto, int cantidad) throws JSONException {
        if (producto.getEstanteria() == null) {
            Log.e("MockDB", "No se puede quitar sin estantería");
            return;
        }

        String productoId = producto.getId().trim();
        String estanteriaId = producto.getEstanteria().getId().trim();
        String clave = claveRelacion(productoId, estanteriaId);

        JSONObject relaciones = JSONUtils.cargarJSONDesdeArchivo(RELACIONES_FILE);

        if (relaciones.has(clave)) {
            JSONObject rel = relaciones.getJSONObject(clave);
            int cantidadActual = rel.getInt("cantidad");
            int nuevaCantidad = cantidadActual - cantidad;

            if (nuevaCantidad <= 0) {
                // Eliminar la relación
                relaciones.remove(clave);
                JSONUtils.escribirJSONDeNuevo(relaciones, RELACIONES_FILE);
            } else {
                rel.put("cantidad", nuevaCantidad);
                JSONUtils.modificarJSONObjectEnArchivoConClave(rel, RELACIONES_FILE, clave);
            }
        }

        Log.d("MockDB", "removeUnds: producto " + productoId + " en estantería " + estanteriaId + " -" + cantidad);
    }

    @Override
    public void assignProductToEstanteria(Producto producto, Estanteria estanteria) throws JSONException {
        String productoId = producto.getId().trim();
        String estanteriaId = estanteria.getId().trim();
        String clave = claveRelacion(productoId, estanteriaId);

        JSONObject relaciones = JSONUtils.cargarJSONDesdeArchivo(RELACIONES_FILE);

        if (relaciones.has(clave)) {
            // Ya existe, sumar cantidad
            JSONObject rel = relaciones.getJSONObject(clave);
            int cantidadActual = rel.getInt("cantidad");
            rel.put("cantidad", cantidadActual + producto.getCantidad());
            JSONUtils.modificarJSONObjectEnArchivoConClave(rel, RELACIONES_FILE, clave);
        } else {
            // Crear nueva relación
            JSONObject nuevaRel = new JSONObject();
            nuevaRel.put("productoId", productoId);
            nuevaRel.put("estanteriaId", estanteriaId);
            nuevaRel.put("cantidad", producto.getCantidad());
            JSONUtils.anadirJSONObjectAlArchivoConClave(nuevaRel, RELACIONES_FILE, clave);
        }

        Log.d("MockDB", "assignProduct: producto " + productoId + " a estantería " + estanteriaId + " con " + producto.getCantidad());
    }

    @Override
    public void addProducto(Producto producto) throws JSONException {
        JSONObject jsonProducto = new JSONObject();
        String prodId = producto.getId().trim();
        jsonProducto.put("id", prodId);
        jsonProducto.put("nombre", producto.getNombre());
        jsonProducto.put("precio", producto.getPrecio());

        JSONUtils.anadirJSONObjectAlArchivo(jsonProducto, PRODUCTOS_FILE);

        // Si tiene estantería, crear relación
        if (producto.getEstanteria() != null && producto.getCantidad() > 0) {
            String estId = producto.getEstanteria().getId().trim();
            String clave = claveRelacion(prodId, estId);
            JSONObject rel = new JSONObject();
            rel.put("productoId", prodId);
            rel.put("estanteriaId", estId);
            rel.put("cantidad", producto.getCantidad());
            JSONUtils.anadirJSONObjectAlArchivoConClave(rel, RELACIONES_FILE, clave);
        }
    }

    @Override
    public Producto getProductoById(String id) throws JSONException {
        JSONObject jsonProductos = JSONUtils.cargarJSONDesdeArchivo(PRODUCTOS_FILE);
        String idStr = String.valueOf(id);

        if (!jsonProductos.has(idStr)) {
            Log.e("MockDB", "Producto no encontrado ID: " + id);
            return null;
        }

        JSONObject jsonProducto = jsonProductos.getJSONObject(idStr);
        String nombre = jsonProducto.getString("nombre");
        double precio = jsonProducto.optDouble("precio", 0.0);

        // Buscar en qué estanterías está y sumar cantidad total
        JSONObject relaciones = JSONUtils.cargarJSONDesdeArchivo(RELACIONES_FILE);
        Iterator<String> keys = relaciones.keys();
        int cantidadTotal = 0;
        Estanteria primeraEstanteria = null;

        while (keys.hasNext()) {
            String key = keys.next();
            JSONObject rel = relaciones.getJSONObject(key);
            if (rel.getString("productoId").equals(id)) {
                int cant = rel.getInt("cantidad");
                cantidadTotal += cant;

                if (primeraEstanteria == null) {
                    String estId = rel.getString("estanteriaId");
                    JSONObject jsonEstanterias = JSONUtils.cargarJSONDesdeArchivo(ESTANTERIAS_FILE);
                    if (jsonEstanterias.has(estId)) {
                        JSONObject jsonEst = jsonEstanterias.getJSONObject(estId);
                        primeraEstanteria = new Estanteria(estId, jsonEst.optString("nombre", "Estanteria " + estId));
                    }
                }
            }
        }

        return new Producto(id, nombre, precio, cantidadTotal, primeraEstanteria);
    }

    @Override
    public Estanteria getEstanteriaById(String estanteriaId) throws JSONException {
        return getEstanteriaConProductosById(estanteriaId);
    }

    @Override
    public Estanteria getEstanteriaConProductosById(String estanteriaId) throws JSONException {
        JSONObject jsonEstanterias = JSONUtils.cargarJSONDesdeArchivo(ESTANTERIAS_FILE);

        if (!jsonEstanterias.has(estanteriaId)) {
            Log.e("MockDB", "Estantería no encontrada ID: " + estanteriaId);
            return null;
        }

        JSONObject jsonEstanteria = jsonEstanterias.getJSONObject(estanteriaId);
        String nombreEstanteria = jsonEstanteria.optString("nombre", "Estanteria " + estanteriaId);
        Estanteria estanteria = new Estanteria(estanteriaId, nombreEstanteria);

        // Buscar productos en esta estantería desde relaciones
        JSONObject relaciones = JSONUtils.cargarJSONDesdeArchivo(RELACIONES_FILE);
        JSONObject jsonProductos = JSONUtils.cargarJSONDesdeArchivo(PRODUCTOS_FILE);
        Iterator<String> keys = relaciones.keys();

        while (keys.hasNext()) {
            String key = keys.next();
            JSONObject rel = relaciones.getJSONObject(key);
            String relEstanteriaId = rel.getString("estanteriaId");

            if (relEstanteriaId.equals(estanteriaId)) {
                String productoId = rel.getString("productoId");
                int cantidad = rel.getInt("cantidad");

                if (jsonProductos.has(productoId)) {
                    JSONObject jsonProd = jsonProductos.getJSONObject(productoId);
                    String nombreProd = jsonProd.getString("nombre");
                    double precio = jsonProd.optDouble("precio", 0.0);

                    Producto producto = new Producto(productoId, nombreProd, precio, cantidad, estanteria);
                    estanteria.addProducto(producto);
                    Log.d("MockDB", "Producto " + nombreProd + " (" + cantidad + " uds) en estantería " + nombreEstanteria);
                }
            }
        }

        Log.d("MockDB", "Estantería " + nombreEstanteria + " tiene " + estanteria.getProductos().size() + " productos");
        return estanteria;
    }

    @Override
    public void subirCambiosProducto(Producto... productos) throws JSONException {
        if (productos == null || productos.length == 0) return;

        for (Producto producto : productos) {
            if (producto == null || producto.getId() == null) continue;

            // Actualizar datos base
            JSONObject jsonProducto = new JSONObject();
            jsonProducto.put("id", producto.getId());
            jsonProducto.put("nombre", producto.getNombre());
            jsonProducto.put("precio", producto.getPrecio());

            JSONObject jsonProductos = JSONUtils.cargarJSONDesdeArchivo(PRODUCTOS_FILE);
            if (jsonProductos.has(String.valueOf(producto.getId()))) {
                JSONUtils.modificarJSONObjectEnArchivo(jsonProducto, PRODUCTOS_FILE);
            }

            // Actualizar relación si tiene estantería
            if (producto.getEstanteria() != null) {
                String clave = claveRelacion(producto.getId(), producto.getEstanteria().getId());
                JSONObject rel = new JSONObject();
                rel.put("productoId", producto.getId());
                rel.put("estanteriaId", producto.getEstanteria().getId());
                rel.put("cantidad", producto.getCantidad());

                JSONObject relaciones = JSONUtils.cargarJSONDesdeArchivo(RELACIONES_FILE);
                relaciones.put(clave, rel);
                JSONUtils.escribirJSONDeNuevo(relaciones, RELACIONES_FILE);
            }

            Log.d("MockDB", "Producto actualizado: " + producto.getNombre());
        }
    }

    @Override
    public void subirCambiosEstanteria(Estanteria... estanterias) throws JSONException {
        if (estanterias == null || estanterias.length == 0) return;

        for (Estanteria estanteria : estanterias) {
            if (estanteria == null || estanteria.getId() == null) continue;

            JSONObject json = new JSONObject();
            json.put("id", estanteria.getId());
            json.put("nombre", estanteria.getNombre());

            JSONObject jsonArchivo = JSONUtils.cargarJSONDesdeArchivo(ESTANTERIAS_FILE);
            String idStr = String.valueOf(estanteria.getId());
            
            if (jsonArchivo.has(idStr)) {
                JSONUtils.modificarJSONObjectEnArchivo(json, ESTANTERIAS_FILE);
                Log.d("MockDB", "Estanteria actualizada: " + estanteria.getNombre());
            } else {
                JSONUtils.anadirJSONObjectAlArchivo(json, ESTANTERIAS_FILE);
                Log.d("MockDB", "Estanteria creada: " + estanteria.getNombre());
            }
        }
    }

    @Override
    public List<Producto> getAllProductos() throws JSONException {
        JSONObject jsonProductos = JSONUtils.cargarJSONDesdeArchivo(PRODUCTOS_FILE);
        if (jsonProductos.length() == 0) return Collections.emptyList();

        JSONObject relaciones = JSONUtils.cargarJSONDesdeArchivo(RELACIONES_FILE);
        List<Producto> productos = new ArrayList<>();
        Iterator<String> keys = jsonProductos.keys();

        while (keys.hasNext()) {
            String key = keys.next();
            JSONObject jsonProd = jsonProductos.getJSONObject(key);
            String productoId = jsonProd.getString("id");
            String nombre = jsonProd.getString("nombre");
            double precio = jsonProd.optDouble("precio", 0.0);

            // Calcular cantidad total y encontrar primera estantería
            int cantidadTotal = 0;
            Estanteria primeraEstanteria = null;
            Iterator<String> relKeys = relaciones.keys();

            while (relKeys.hasNext()) {
                String relKey = relKeys.next();
                JSONObject rel = relaciones.getJSONObject(relKey);
                if (rel.getString("productoId").equals(productoId)) {
                    cantidadTotal += rel.getInt("cantidad");
                    if (primeraEstanteria == null) {
                        String estId = rel.getString("estanteriaId");
                        primeraEstanteria = getEstanteriaSimple(estId);
                    }
                }
            }

            productos.add(new Producto(productoId, nombre, precio, cantidadTotal, primeraEstanteria));
        }

        return productos;
    }

    @Override
    public void deleteProducto(String id) throws JSONException {
        String prodId = id.trim();
        JSONObject productos = JSONUtils.cargarJSONDesdeArchivo(PRODUCTOS_FILE);
        if (productos.has(prodId)) {
            productos.remove(prodId);
            JSONUtils.escribirJSONDeNuevo(productos, PRODUCTOS_FILE);
        }

        // Eliminar relaciones
        JSONObject relaciones = JSONUtils.cargarJSONDesdeArchivo(RELACIONES_FILE);
        Iterator<String> keys = relaciones.keys();
        boolean changed = false;
        while (keys.hasNext()) {
            String key = keys.next();
            JSONObject rel = relaciones.getJSONObject(key);
            if (rel.getString("productoId").equals(prodId)) {
                keys.remove();
                changed = true;
            }
        }
        if (changed) {
            JSONUtils.escribirJSONDeNuevo(relaciones, RELACIONES_FILE);
        }
        Log.d("MockDB", "Producto eliminado: " + prodId);
    }

    @Override
    public void deleteEstanteria(String id) throws JSONException {
        String estId = id.trim();
        JSONObject estanterias = JSONUtils.cargarJSONDesdeArchivo(ESTANTERIAS_FILE);
        if (estanterias.has(estId)) {
            estanterias.remove(estId);
            JSONUtils.escribirJSONDeNuevo(estanterias, ESTANTERIAS_FILE);
        }

        // Eliminar relaciones
        JSONObject relaciones = JSONUtils.cargarJSONDesdeArchivo(RELACIONES_FILE);
        Iterator<String> keys = relaciones.keys();
        boolean changed = false;
        while (keys.hasNext()) {
            String key = keys.next();
            JSONObject rel = relaciones.getJSONObject(key);
            if (rel.getString("estanteriaId").equals(estId)) {
                keys.remove();
                changed = true;
            }
        }
        if (changed) {
            JSONUtils.escribirJSONDeNuevo(relaciones, RELACIONES_FILE);
        }
        Log.d("MockDB", "Estantería eliminada: " + estId);
    }

    /**
     * Obtiene estantería sin productos (para evitar bucles)
     */
    private Estanteria getEstanteriaSimple(String estanteriaId) {
        try {
            JSONObject jsonEstanterias = JSONUtils.cargarJSONDesdeArchivo(ESTANTERIAS_FILE);
            if (jsonEstanterias.has(estanteriaId)) {
                JSONObject json = jsonEstanterias.getJSONObject(estanteriaId);
                return new Estanteria(estanteriaId, json.optString("nombre", "Estanteria " + estanteriaId));
            }
        } catch (Exception e) {
            Log.e("MockDB", "Error obteniendo estantería simple: " + e.getMessage());
        }
        return null;
    }

    public static void initialize() {
        try {
            Log.d("MockDB", "Inicializando MockDatabaseController...");

            JSONUtils.crearArchivoSiNoExiste(ESTANTERIAS_FILE);
            JSONUtils.crearArchivoSiNoExiste(PRODUCTOS_FILE);
            JSONUtils.crearArchivoSiNoExiste(RELACIONES_FILE);

            JSONObject estanteriasJson = JSONUtils.cargarJSONDesdeArchivo(ESTANTERIAS_FILE);
            if (estanteriasJson.length() == 0) {
                Log.d("MockDB", "Poblando estanterias...");
                JSONUtils.mockearEstanteriasBase();
            }

            JSONObject productosJson = JSONUtils.cargarJSONDesdeArchivo(PRODUCTOS_FILE);
            if (productosJson.length() == 0) {
                Log.d("MockDB", "Poblando productos...");
                JSONUtils.mockearProductosBase();
            }

            JSONObject relacionesJson = JSONUtils.cargarJSONDesdeArchivo(RELACIONES_FILE);
            if (relacionesJson.length() == 0) {
                Log.d("MockDB", "Poblando relaciones producto-estanteria...");
                JSONUtils.mockearRelacionesBase();
            }

            Log.d("MockDB", "MockDatabaseController inicializado correctamente");
        } catch (Exception e) {
            Log.e("MockDB", "Error al inicializar: " + e.getMessage(), e);
        }
    }

}
