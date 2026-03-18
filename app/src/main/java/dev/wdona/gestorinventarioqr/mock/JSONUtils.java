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
            String idProducto = String.valueOf(json.get("id"));
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
            String idProducto = String.valueOf(json.get("id"));
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
        // Método vaciado para no insertar datos de ejemplo
    }

    /**
     * Relaciones producto-estanteria con cantidades
     */
    public static void mockearRelacionesBase() throws JSONException {
        // Método vaciado para no insertar datos de ejemplo
    }

    public static void mockearEstanteriasBase() throws JSONException {
        // Método vaciado para no insertar datos de ejemplo
    }
}
