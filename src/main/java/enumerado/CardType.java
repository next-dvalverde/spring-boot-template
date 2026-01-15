package enumerado;

public enum CardType {
	DEBIT("Debito"), 
 	CREDIT("Credito");
 	
 	private final String description;
    
    CardType(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}
