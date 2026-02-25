package dev.wdona.gestorinventarioqr.mock;

/**
 * Configuración centralizada para alternar entre modo online y offline.
 */
public class MockConfig {

    /**
     * Variable para simular modo offline (sin conexión)
     * Se puede cambiar en tiempo de ejecución
     */
    private static boolean simulateOffline = false;

    /**
     * Obtiene si está en modo offline
     */
    public static boolean isOffline() {
        return simulateOffline;
    }

    /**
     * Activa o desactiva el modo offline
     */
    public static void setOffline(boolean offline) {
        simulateOffline = offline;
    }

    /**
     * Alterna entre modo online y offline
     * @return true si ahora está offline, false si está online
     */
    public static boolean toggleOffline() {
        simulateOffline = !simulateOffline;
        return simulateOffline;
    }

    /**
     * Obtiene la implementación de MockDatabaseOperations según la configuración
     */
    public static MockDatabaseOperations getMockDatabase() {
        if (simulateOffline) {
            return new MockOffline();
        } else {
            return new MockDatabaseController();
        }
    }
}

