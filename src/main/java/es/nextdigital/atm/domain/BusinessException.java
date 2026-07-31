package es.nextdigital.atm.domain;

/**
 * Error de negocio con codigo estable. El mensaje nunca revela estado interno
 * (saldos, existencia de cuentas ajenas, PINs).
 */
public class BusinessException extends RuntimeException {

    private final ErrorCode code;

    public BusinessException(ErrorCode code, String message) {
        super(message);
        this.code = code;
    }

    public ErrorCode code() {
        return code;
    }
}
