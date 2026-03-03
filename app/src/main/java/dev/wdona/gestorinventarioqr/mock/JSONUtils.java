package dev.wdona.gestorinventarioqr.mock;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class JSONUtils {

    private static Context appContext;

    public static void init(Context context) {
        appContext = context.getApplicationContext();
    }

    private static File getFile(String nombreArchivo) {
        if (appContext == null) {
            throw new IllegalStateException("JSONUtils no ha sido inicializado. Llama a JSONUtils.init(context) primero.");
        }
        return new File(appContext.getFilesDir(), nombreArchivo);
    }

    public static boolean eliminarArchivo(String nombreArchivo) {
        File file = getFile(nombreArchivo);
        if (file.exists()) {
            boolean eliminado = file.delete();
            android.util.Log.d("JSONUtils", "Archivo " + nombreArchivo + " eliminado: " + eliminado);
            return eliminado;
        }
        return false;
    }

    public static JSONObject cargarJSONDesdeArchivo(String nombreArchivo) throws JSONException {
        StringBuilder sb = new StringBuilder();
        File file = getFile(nombreArchivo);
        android.util.Log.d("JSONUtils", "Cargando archivo: " + file.getAbsolutePath() + " existe: " + file.exists() + " tamaño: " + file.length());

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                sb.append(linea);
            }
        } catch (Exception e) {
            android.util.Log.e("JSONUtils", "Error al cargar JSON " + nombreArchivo + ": " + e.getMessage());
            return new JSONObject();
        }

        String content = sb.toString().trim();
        android.util.Log.d("JSONUtils", "Contenido de " + nombreArchivo + ": " + (content.length() > 100 ? content.substring(0, 100) + "..." : content));

        if (content.isEmpty()) {
            return new JSONObject();
        }
        return new JSONObject(content);
    }

    public static void escribirJSONDeNuevo(JSONObject json, String nombreArchivo) {
        File file = getFile(nombreArchivo);
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(json.toString(4).getBytes(StandardCharsets.UTF_8));
            android.util.Log.d("JSONUtils", "Archivo JSON guardado: " + file.getAbsolutePath() + " con " + json.length() + " elementos");
        } catch (Exception e) {
            android.util.Log.e("JSONUtils", "Error al escribir JSON: " + e.getMessage());
        }
    }

    public static void anadirJSONObjectAlArchivo(JSONObject json, String nombreArchivo) {
        try {
            JSONObject jsonArchivo = cargarJSONDesdeArchivo(nombreArchivo);
            String idProducto = String.valueOf(json.getInt("id"));
            jsonArchivo.put(idProducto, json);
            escribirJSONDeNuevo(jsonArchivo, nombreArchivo);
            android.util.Log.d("JSONUtils", "Añadido objeto con id " + idProducto + " a " + nombreArchivo);
        } catch (JSONException e) {
            android.util.Log.e("JSONUtils", "Error al añadir JSON: " + e.getMessage());
        }
    }

    public static void anadirJSONObjectAlArchivoConClave(JSONObject json, String nombreArchivo, String clave) {
        try {
            JSONObject jsonArchivo = cargarJSONDesdeArchivo(nombreArchivo);
            jsonArchivo.put(clave, json);
            escribirJSONDeNuevo(jsonArchivo, nombreArchivo);
            android.util.Log.d("JSONUtils", "Añadido objeto con clave " + clave + " a " + nombreArchivo);
        } catch (JSONException e) {
            android.util.Log.e("JSONUtils", "Error al añadir JSON con clave: " + e.getMessage());
        }
    }

    public static void modificarJSONObjectEnArchivo(JSONObject json, String nombreArchivo) {
        try {
            JSONObject jsonArchivo = cargarJSONDesdeArchivo(nombreArchivo);
            String idProducto = String.valueOf(json.getInt("id"));
            if (jsonArchivo.has(idProducto)) {
                jsonArchivo.put(idProducto, json);
                escribirJSONDeNuevo(jsonArchivo, nombreArchivo);
            } else {
                android.util.Log.e("JSONUtils", "Error: No se encontró el objeto con ID " + idProducto + " en " + nombreArchivo);
            }
        } catch (JSONException e) {
            android.util.Log.e("JSONUtils", "Error al modificar JSON: " + e.getMessage());
        }
    }

    public static void modificarJSONObjectEnArchivoConClave(JSONObject json, String nombreArchivo, String clave) {
        try {
            JSONObject jsonArchivo = cargarJSONDesdeArchivo(nombreArchivo);
            if (jsonArchivo.has(clave)) {
                jsonArchivo.put(clave, json);
                escribirJSONDeNuevo(jsonArchivo, nombreArchivo);
            } else {
                android.util.Log.e("JSONUtils", "Error: No se encontró la clave " + clave + " en " + nombreArchivo);
            }
        } catch (JSONException e) {
            android.util.Log.e("JSONUtils", "Error al modificar JSON con clave: " + e.getMessage());
        }
    }

    public static boolean crearArchivoSiNoExiste(String nombreArchivo) {
        File file = getFile(nombreArchivo);
        if (!file.exists() || file.length() == 0) {
            try (FileOutputStream fos = new FileOutputStream(file)) {
                fos.write("{}".getBytes(StandardCharsets.UTF_8));
                android.util.Log.d("JSONUtils", "Archivo JSON creado/recreado: " + file.getAbsolutePath());
            } catch (Exception e) {
                android.util.Log.e("JSONUtils", "Error al crear archivo: " + e.getMessage());
                return false;
            }
            return true;
        }
        return false;
    }

    /**
     * Productos base: solo datos base (sin cantidad ni estanteriaId)
     */
    public static void mockearProductosBase() throws JSONException {
        String nombreArchivo = "productos.json";
        crearArchivoSiNoExiste(nombreArchivo);

        crearProductoBase(1, "Cable USB-C", 5.99);
        crearProductoBase(2, "Cargador 20W", 15.99);
        crearProductoBase(3, "Auriculares Bluetooth", 29.99);
        crearProductoBase(4, "Destornillador Phillips", 3.50);
        crearProductoBase(5, "Llave inglesa", 12.00);
        crearProductoBase(6, "Cinta metrica 5m", 4.99);
        crearProductoBase(7, "Detergente 2L", 6.50);
        crearProductoBase(8, "Escoba industrial", 8.99);
        crearProductoBase(9, "Boligrafos pack 10", 2.99);
        crearProductoBase(10, "Cuaderno A4", 1.50);

        android.util.Log.d("JSONUtils", "10 Productos base creados en JSON");
    }

    private static void crearProductoBase(int id, String nombre, double precio) throws JSONException {
        JSONObject prod = new JSONObject();
        prod.put("id", id);
        prod.put("nombre", nombre);
        prod.put("precio", precio);
        anadirJSONObjectAlArchivo(prod, "productos.json");
    }

    /**
     * Relaciones producto-estanteria con cantidades
     */
    public static void mockearRelacionesBase() throws JSONException {
        String nombreArchivo = "producto_estanteria.json";
        crearArchivoSiNoExiste(nombreArchivo);

        // Estanteria 1 - Electronica
        crearRelacion(1, 1, 50, nombreArchivo);   // Cable USB-C → Estanteria A: 50
        crearRelacion(2, 1, 30, nombreArchivo);   // Cargador 20W → Estanteria A: 30
        crearRelacion(3, 1, 20, nombreArchivo);   // Auriculares → Estanteria A: 20

        // Estanteria 2 - Herramientas
        crearRelacion(4, 2, 100, nombreArchivo);  // Destornillador → Estanteria B: 100
        crearRelacion(5, 2, 25, nombreArchivo);   // Llave inglesa → Estanteria B: 25
        crearRelacion(6, 2, 40, nombreArchivo);   // Cinta metrica → Estanteria B: 40

        // Estanteria 3 - Limpieza
        crearRelacion(7, 3, 60, nombreArchivo);   // Detergente → Estanteria C: 60
        crearRelacion(8, 3, 15, nombreArchivo);   // Escoba → Estanteria C: 15

        // Estanteria 4 - Oficina
        crearRelacion(9, 4, 200, nombreArchivo);  // Boligrafos → Estanteria D: 200
        crearRelacion(10, 4, 150, nombreArchivo); // Cuaderno → Estanteria D: 150

        android.util.Log.d("JSONUtils", "10 Relaciones producto-estantería creadas en JSON");
    }

    private static void crearRelacion(long productoId, long estanteriaId, int cantidad, String nombreArchivo) throws JSONException {
        String clave = productoId + "_" + estanteriaId;
        JSONObject rel = new JSONObject();
        rel.put("productoId", productoId);
        rel.put("estanteriaId", estanteriaId);
        rel.put("cantidad", cantidad);
        anadirJSONObjectAlArchivoConClave(rel, nombreArchivo, clave);
    }

    public static void mockearEstanteriasBase() throws JSONException {
        String nombreArchivo = "estanterias.json";
        crearArchivoSiNoExiste(nombreArchivo);

        JSONObject estanteria1 = new JSONObject();
        estanteria1.put("id", 1);
        estanteria1.put("nombre", "Estanteria A - Electronica");

        JSONObject estanteria2 = new JSONObject();
        estanteria2.put("id", 2);
        estanteria2.put("nombre", "Estanteria B - Herramientas");

        JSONObject estanteria3 = new JSONObject();
        estanteria3.put("id", 3);
        estanteria3.put("nombre", "Estanteria C - Limpieza");

        JSONObject estanteria4 = new JSONObject();
        estanteria4.put("id", 4);
        estanteria4.put("nombre", "Estanteria D - Oficina");

        anadirJSONObjectAlArchivo(estanteria1, nombreArchivo);
        anadirJSONObjectAlArchivo(estanteria2, nombreArchivo);
        anadirJSONObjectAlArchivo(estanteria3, nombreArchivo);
        anadirJSONObjectAlArchivo(estanteria4, nombreArchivo);

        android.util.Log.d("JSONUtils", "4 Estanterias base creadas en JSON");
    }
}
