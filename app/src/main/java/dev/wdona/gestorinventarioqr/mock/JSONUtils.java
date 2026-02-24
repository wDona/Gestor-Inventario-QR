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
            return new JSONObject(); // Retornar objeto vacío si hay error
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

    public static void modificarJSONObjectEnArchivo(JSONObject json, String nombreArchivo) {
        try {
            JSONObject jsonArchivo = cargarJSONDesdeArchivo(nombreArchivo);
            String idProducto = String.valueOf(json.getInt("id"));
            if (jsonArchivo.has(idProducto)) {
                jsonArchivo.put(idProducto, json);
                escribirJSONDeNuevo(jsonArchivo, nombreArchivo);
            } else {
                System.out.println("Error: No se encontró el producto con ID " + idProducto + " en el archivo.");
            }
        } catch (JSONException e) {
            System.out.println("Error al modificar JSON: " + e.getMessage());
        }
    }

    public static boolean crearArchivoSiNoExiste(String nombreArchivo) {
        File file = getFile(nombreArchivo);
        // Crear si no existe O si está vacío
//        if (!file.exists() || file.length() == 0) {
            try (FileOutputStream fos = new FileOutputStream(file)) {
                fos.write("{}".getBytes(StandardCharsets.UTF_8));
                android.util.Log.d("JSONUtils", "Archivo JSON creado/recreado: " + file.getAbsolutePath());
            } catch (Exception e) {
                android.util.Log.e("JSONUtils", "Error al crear archivo: " + e.getMessage());
                return false;
            }
            return true;
//        }
//        return false;
    }

    public static void mockearProductosBase() throws JSONException {
        String nombreArchivo = "productos.json";
        crearArchivoSiNoExiste(nombreArchivo);

        // Producto A - en Estanteria 1
        JSONObject producto1 = new JSONObject();
        producto1.put("id", 1);
        producto1.put("nombre", "Producto A");
        producto1.put("cantidad", 10);
        producto1.put("precio", 10.0);
        producto1.put("estanteriaId", 1);

        // Producto B - en Estanteria 2
        JSONObject producto2 = new JSONObject();
        producto2.put("id", 2);
        producto2.put("nombre", "Producto B");
        producto2.put("cantidad", 5);
        producto2.put("precio", 20.0);
        producto2.put("estanteriaId", 2);

        anadirJSONObjectAlArchivo(producto1, nombreArchivo);
        anadirJSONObjectAlArchivo(producto2, nombreArchivo);

        android.util.Log.d("JSONUtils", "Productos base creados en JSON");
    }

    public static void mockearEstanteriasBase() throws JSONException {
        String nombreArchivo = "estanterias.json";
        crearArchivoSiNoExiste(nombreArchivo);

        // Estanteria 1
        JSONObject estanteria1 = new JSONObject();
        estanteria1.put("id", 1);
        estanteria1.put("nombre", "Estanteria 1");

        // Estanteria 2
        JSONObject estanteria2 = new JSONObject();
        estanteria2.put("id", 2);
        estanteria2.put("nombre", "Estanteria 2");

        anadirJSONObjectAlArchivo(estanteria1, nombreArchivo);
        anadirJSONObjectAlArchivo(estanteria2, nombreArchivo);

        android.util.Log.d("JSONUtils", "Estanterias base creadas en JSON");
    }
}
