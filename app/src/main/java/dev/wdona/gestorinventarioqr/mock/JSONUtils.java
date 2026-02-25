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
                android.util.Log.e("JSONUtils", "Error: No se encontró el producto con ID " + idProducto + " en el archivo.");
            }
        } catch (JSONException e) {
            android.util.Log.e("JSONUtils", "Error al modificar JSON: " + e.getMessage());
        }
    }

    public static boolean crearArchivoSiNoExiste(String nombreArchivo) {
        File file = getFile(nombreArchivo);
        // Crear si no existe O si está vacío
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

    public static void mockearProductosBase() throws JSONException {
        String nombreArchivo = "productos.json";
        crearArchivoSiNoExiste(nombreArchivo);

        // === Estanteria 1 - Electronica ===
        JSONObject producto1 = new JSONObject();
        producto1.put("id", 1);
        producto1.put("nombre", "Cable USB-C");
        producto1.put("cantidad", 50);
        producto1.put("precio", 5.99);
        producto1.put("estanteriaId", 1);

        JSONObject producto2 = new JSONObject();
        producto2.put("id", 2);
        producto2.put("nombre", "Cargador 20W");
        producto2.put("cantidad", 30);
        producto2.put("precio", 15.99);
        producto2.put("estanteriaId", 1);

        JSONObject producto3 = new JSONObject();
        producto3.put("id", 3);
        producto3.put("nombre", "Auriculares Bluetooth");
        producto3.put("cantidad", 20);
        producto3.put("precio", 29.99);
        producto3.put("estanteriaId", 1);

        // === Estanteria 2 - Herramientas ===
        JSONObject producto4 = new JSONObject();
        producto4.put("id", 4);
        producto4.put("nombre", "Destornillador Phillips");
        producto4.put("cantidad", 100);
        producto4.put("precio", 3.50);
        producto4.put("estanteriaId", 2);

        JSONObject producto5 = new JSONObject();
        producto5.put("id", 5);
        producto5.put("nombre", "Llave inglesa");
        producto5.put("cantidad", 25);
        producto5.put("precio", 12.00);
        producto5.put("estanteriaId", 2);

        JSONObject producto6 = new JSONObject();
        producto6.put("id", 6);
        producto6.put("nombre", "Cinta metrica 5m");
        producto6.put("cantidad", 40);
        producto6.put("precio", 4.99);
        producto6.put("estanteriaId", 2);

        // === Estanteria 3 - Limpieza ===
        JSONObject producto7 = new JSONObject();
        producto7.put("id", 7);
        producto7.put("nombre", "Detergente 2L");
        producto7.put("cantidad", 60);
        producto7.put("precio", 6.50);
        producto7.put("estanteriaId", 3);

        JSONObject producto8 = new JSONObject();
        producto8.put("id", 8);
        producto8.put("nombre", "Escoba industrial");
        producto8.put("cantidad", 15);
        producto8.put("precio", 8.99);
        producto8.put("estanteriaId", 3);

        // === Estanteria 4 - Oficina ===
        JSONObject producto9 = new JSONObject();
        producto9.put("id", 9);
        producto9.put("nombre", "Boligrafos pack 10");
        producto9.put("cantidad", 200);
        producto9.put("precio", 2.99);
        producto9.put("estanteriaId", 4);

        JSONObject producto10 = new JSONObject();
        producto10.put("id", 10);
        producto10.put("nombre", "Cuaderno A4");
        producto10.put("cantidad", 150);
        producto10.put("precio", 1.50);
        producto10.put("estanteriaId", 4);

        anadirJSONObjectAlArchivo(producto1, nombreArchivo);
        anadirJSONObjectAlArchivo(producto2, nombreArchivo);
        anadirJSONObjectAlArchivo(producto3, nombreArchivo);
        anadirJSONObjectAlArchivo(producto4, nombreArchivo);
        anadirJSONObjectAlArchivo(producto5, nombreArchivo);
        anadirJSONObjectAlArchivo(producto6, nombreArchivo);
        anadirJSONObjectAlArchivo(producto7, nombreArchivo);
        anadirJSONObjectAlArchivo(producto8, nombreArchivo);
        anadirJSONObjectAlArchivo(producto9, nombreArchivo);
        anadirJSONObjectAlArchivo(producto10, nombreArchivo);

        android.util.Log.d("JSONUtils", "10 Productos base creados en JSON");
    }

    public static void mockearEstanteriasBase() throws JSONException {
        String nombreArchivo = "estanterias.json";
        crearArchivoSiNoExiste(nombreArchivo);

        // Estanteria 1 - Electronica
        JSONObject estanteria1 = new JSONObject();
        estanteria1.put("id", 1);
        estanteria1.put("nombre", "Estanteria A - Electronica");

        // Estanteria 2 - Herramientas
        JSONObject estanteria2 = new JSONObject();
        estanteria2.put("id", 2);
        estanteria2.put("nombre", "Estanteria B - Herramientas");

        // Estanteria 3 - Limpieza
        JSONObject estanteria3 = new JSONObject();
        estanteria3.put("id", 3);
        estanteria3.put("nombre", "Estanteria C - Limpieza");

        // Estanteria 4 - Oficina
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
