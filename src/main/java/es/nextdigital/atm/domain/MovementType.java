package es.nextdigital.atm.domain;

public enum MovementType {
    DEPOSIT, WITHDRAWAL, FEE, TRANSFER_IN, TRANSFER_OUT;

    /**
     * Convencion de signo del extracto: amount es siempre positivo (CHECK en el
     * schema) y la direccion la da el tipo. Sin esta funcion, las dos invariantes
     * de PLAN.md &sect;6 serian incompatibles entre si.
     */
    public static int signOf(MovementType type) {
        return switch (type) {
            case DEPOSIT, TRANSFER_IN -> +1;
            case WITHDRAWAL, FEE, TRANSFER_OUT -> -1;
        };
    }
}
