package bflow.receipts.enums;

/**
 * Lifecycle status of a {@link bflow.receipts.entity.ReceiptUpload}.
 */
public enum ReceiptStatus {
    RECEIVED,     // registrado con wallet, esperando OCR
    PROCESSING,   // Textract corriendo
    EXTRACTED,    // OCR listo, mostrado al usuario, esperando confirmación
    CONFIRMED,    // usuario confirmó, Expense/Income ya existe
    FAILED,       // OCR falló o no produjo nada usable
    DISCARDED     // usuario rechazó el resultado, no se creó nada
}
