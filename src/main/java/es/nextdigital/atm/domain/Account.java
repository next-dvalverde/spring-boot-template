package es.nextdigital.atm.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Version;
import java.math.BigDecimal;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
public class Account {

    @Id
    private UUID id;

    private String iban;

    private UUID customerId;

    private UUID bankId;

    private BigDecimal balance;

    // La v1 no la lee: fija la divisa desde el dia 1 para que multi-divisa no
    // sea arqueologia (DECISIONS.md C-10). CHAR(3) en el schema, de ahi el
    // JdbcTypeCode: validate rechaza bpchar contra un varchar esperado.
    @JdbcTypeCode(SqlTypes.CHAR)
    private String currency;

    @Version
    private long version;

    protected Account() {
    }

    public Account(UUID id, String iban, UUID customerId, UUID bankId, BigDecimal balance, String currency) {
        this.id = id;
        this.iban = iban;
        this.customerId = customerId;
        this.bankId = bankId;
        this.balance = balance;
        this.currency = currency;
    }

    public UUID getId() {
        return id;
    }

    public String getIban() {
        return iban;
    }

    public UUID getCustomerId() {
        return customerId;
    }

    public UUID getBankId() {
        return bankId;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public String getCurrency() {
        return currency;
    }

    public long getVersion() {
        return version;
    }

    // La aritmetica del saldo vive aqui, no en el servicio. Las guardas de
    // saldo son de WithdrawalPolicy; el CHECK (balance >= 0) del schema es la
    // ultima linea de defensa.
    public void debit(BigDecimal amount) {
        balance = balance.subtract(amount);
    }

    public void credit(BigDecimal amount) {
        balance = balance.add(amount);
    }
}
