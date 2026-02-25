package dev.wdona.gestorinventarioqr.mock;

import org.json.JSONException;

import java.net.SocketTimeoutException;
import java.util.List;

import dev.wdona.gestorinventarioqr.domain.model.Estanteria;
import dev.wdona.gestorinventarioqr.domain.model.Producto;

/**
 * Simula un escenario sin conexión a internet.
 * Todos los métodos lanzan excepciones simulando timeout de red.
 */
public class MockOffline implements MockDatabaseOperations {

    private static final String TIMEOUT_MESSAGE = "Simulated network timeout: Unable to connect to server";

    /**
     * Simula un delay de red y luego lanza excepción de timeout
     */
    private void simulateNetworkTimeout() throws RuntimeException {
        try {
            // Simular delay antes del timeout (500ms)
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        throw new RuntimeException(TIMEOUT_MESSAGE, new SocketTimeoutException("Connection timed out"));
    }

    @Override
    public void addUndsProduct(Producto producto, int cantidad) throws JSONException {
        simulateNetworkTimeout();
    }

    @Override
    public void removeUndsProduct(Producto producto, int cantidad) throws JSONException {
        simulateNetworkTimeout();
    }

    @Override
    public void assignProductToEstanteria(Producto producto, Estanteria estanteria) throws JSONException {
        simulateNetworkTimeout();
    }

    @Override
    public void addProducto(Producto producto) throws JSONException {
        simulateNetworkTimeout();
    }

    @Override
    public Producto getProductoById(Long id) throws JSONException {
        simulateNetworkTimeout();
        return null; // Nunca llega aquí
    }

    @Override
    public Estanteria getEstanteriaById(Long id) throws JSONException {
        simulateNetworkTimeout();
        return null;
    }

    @Override
    public Estanteria getEstanteriaConProductosById(Long idEstanteria) throws JSONException {
        simulateNetworkTimeout();
        return null;
    }

    @Override
    public void subirCambiosProducto(Producto... producto) throws JSONException {
        simulateNetworkTimeout();
    }

    @Override
    public void subirCambiosEstanteria(Estanteria... estanterias) throws JSONException {
        simulateNetworkTimeout();
    }

    @Override
    public List<Producto> getAllProductos() throws JSONException {
        simulateNetworkTimeout();
        return null;
    }
}
