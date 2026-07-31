package es.nextdigital.atm.domain;

/**
 * Espejo 1:1 del catalogo canonico de CLAUDE.md ("Convenciones de codigo").
 * Anadir un codigo aqui sin anadirlo alli es dejar la tabla mintiendo.
 */
public enum ErrorCode {
    INVALID_IBAN(400),
    INVALID_PIN_FORMAT(400),
    INVALID_AMOUNT(400),
    INVALID_PIN(401),
    INVALID_SESSION(401),
    FOREIGN_ATM_DEPOSIT_NOT_ALLOWED(403),
    CARD_NOT_FOUND(404),
    ATM_NOT_FOUND(404),
    ACCOUNT_NOT_FOUND(404),
    CARD_NOT_ACTIVATED(409),
    CARD_BLOCKED(409),
    CARD_ALREADY_ACTIVE(409),
    PIN_CHANGE_REQUIRED(409),
    INSUFFICIENT_FUNDS(409),
    LIMIT_EXCEEDED(409),
    CREDIT_LIMIT_EXCEEDED(409),
    OPERATION_NOT_SUPPORTED_FOR_CARD_TYPE(409),
    CONCURRENT_MODIFICATION(409);

    // int y no HttpStatus: el dominio no importa Spring.
    private final int httpStatus;

    ErrorCode(int httpStatus) {
        this.httpStatus = httpStatus;
    }

    public int httpStatus() {
        return httpStatus;
    }
}
