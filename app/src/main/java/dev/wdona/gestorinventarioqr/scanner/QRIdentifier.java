package dev.wdona.gestorinventarioqr.scanner;

public class QRIdentifier {

    public enum QRType {
        ESTANTERIA,
        PRODUCTO,
        UNKNOWN
    }

    public static class QRResult {
        public final QRType type;
        public final String id;
        public final String rawData;

        public QRResult(QRType type, String id, String rawData) {
            this.type = type;
            this.id = id;
            this.rawData = rawData;
        }
    }

    /**
     * Identifica el tipo de QR escaneado.
     * TODO: Modificar según el formato real de tus QRs
     *
     * Formato esperado por defecto:
     * - Estantería: "EST-{id}" o "ESTANTERIA-{id}"
     * - Producto: "PROD-{id}" o "PRODUCTO-{id}"
     */
    @Deprecated
    public static QRResult identify(String qrData) {
        if (qrData == null || qrData.isEmpty()) {
            return new QRResult(QRType.UNKNOWN, null, qrData);
        }

        String upperData = qrData.toUpperCase().trim();

        // Detectar estantería
        if (upperData.startsWith("EST-") || upperData.startsWith("ESTANTERIA-")) {
            String id = extractId(qrData, "-");
            return new QRResult(QRType.ESTANTERIA, id, qrData);
        }

        // Detectar producto
        if (upperData.startsWith("PROD-") || upperData.startsWith("PRODUCTO-")) {
            String id = extractId(qrData, "-");
            return new QRResult(QRType.PRODUCTO, id, qrData);
        }

        return new QRResult(QRType.UNKNOWN, null, qrData);
    }

    private static String extractId(String data, String separator) {

        int index = data.trim().lastIndexOf(separator);
        if (index != -1 && index < data.length() - 1) {
            return data.trim().substring(index + 1);
        }
        return data.trim();
    }
}

