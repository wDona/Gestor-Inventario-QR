package dev.wdona.gestorinventarioqr.mock;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class JSONUtils {
    public static JSONObject cargarJSONDesdeArchivo(String nombreArchivo) throws JSONException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(nombreArchivo), StandardCharsets.UTF_8))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                sb.append(linea);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return new JSONObject(String.valueOf(sb));
    }

    public static void escribirJSONDeNuevo(JSONObject json, String nombreArchivo) {
        try (java.io.FileWriter file = new java.io.FileWriter(nombreArchivo)) {
            file.write(json.toString(4)); // Indentación de 4 espacios para mejor legibilidad
            System.out.println("Archivo JSON guardado exitosamente: " + nombreArchivo);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void anadirJSONObjectAlArchivo(JSONObject json, String nombreArchivo) {
        try {
            JSONObject jsonArchivo = cargarJSONDesdeArchivo(nombreArchivo);
            String idProducto = String.valueOf(json.getInt("id"));
            jsonArchivo.put(idProducto, json);
            escribirJSONDeNuevo(jsonArchivo, nombreArchivo);
        } catch (JSONException e) {
            throw new RuntimeException(e);
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
            throw new RuntimeException(e);
        }
    }

    public static boolean crearArchivoSiNoExiste(String nombreArchivo) {
        java.io.File file = new java.io.File(nombreArchivo);
        if (!file.exists()) {
            try (java.io.FileWriter writer = new java.io.FileWriter(file)) {
                writer.write("{}"); // Escribir un objeto JSON vacío
                System.out.println("Archivo JSON creado exitosamente: " + nombreArchivo);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            return true;
        }
        return false;
    }

    public static void mockearProductosBase() throws JSONException {
        String nombreArchivo = "productos.json";
        crearArchivoSiNoExiste(nombreArchivo);

        JSONObject producto1 = new JSONObject();
        producto1.put("id", 1);
        producto1.put("nombre", "Producto A");
        producto1.put("cantidad", 10);
        producto1.put("precio", 9.99);
        producto1.put("estanteriaId", 1);

        JSONObject producto2 = new JSONObject();
        producto2.put("id", 2);
        producto2.put("nombre", "Producto B");
        producto2.put("cantidad", 5);
        producto2.put("precio", 14.99);
        producto2.put("estanteriaId", 2);

        anadirJSONObjectAlArchivo(producto1, nombreArchivo);
        anadirJSONObjectAlArchivo(producto2, nombreArchivo);
    }

    public static void mockearEstanteriasBase() throws JSONException {
        String nombreArchivo = "estanterias.json";
        crearArchivoSiNoExiste(nombreArchivo);

        JSONObject estanteria1 = new JSONObject();
        estanteria1.put("id", 1);
        estanteria1.put("nombre", "Estanteria 1");

        JSONObject estanteria2 = new JSONObject();
        estanteria2.put("id", 2);
        estanteria2.put("nombre", "Estanteria 2");

        anadirJSONObjectAlArchivo(estanteria1, nombreArchivo);
        anadirJSONObjectAlArchivo(estanteria2, nombreArchivo);
    }
}
