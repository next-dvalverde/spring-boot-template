package enumerado;

public enum TransactionType {
    WITHDRAWAL("Retiro de efectivo"),
    DEPOSIT("Ingreso"),
    TRANSFER_OUT("Transferencia saliente"),
    TRANSFER_IN("Transferencia entrante"),
    FEE("Comisión"),
    COMMISSION("Comisión cajero externo");
    
    private final String description;
    
    TransactionType(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}