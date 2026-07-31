package es.nextdigital.atm.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * Fila de tarifa. bankCode es el banco QUE COBRA (DECISIONS.md A-3); decidir
 * cual es segun la operacion es cosa del servicio. El dominio puro no ve esta
 * entidad: recibe un record FeeRule (A-2).
 */
@Entity
public class FeePolicy {

    @Id
    private UUID id;

    // CHAR(4) en el schema, como bank.code.
    @JdbcTypeCode(SqlTypes.CHAR)
    private String bankCode;

    @Enumerated(EnumType.STRING)
    private FeeOperationType operationType;

    private BigDecimal fixedAmount;

    private BigDecimal percentage;

    // La v1 no las lee (DECISIONS.md C-8): fechan la vigencia para que el
    // extracto historico sea reproducible el dia que las tarifas cambien.
    private Instant validFrom;

    private Instant validTo;

    protected FeePolicy() {
    }

    public FeePolicy(UUID id, String bankCode, FeeOperationType operationType,
                     BigDecimal fixedAmount, BigDecimal percentage, Instant validFrom, Instant validTo) {
        this.id = id;
        this.bankCode = bankCode;
        this.operationType = operationType;
        this.fixedAmount = fixedAmount;
        this.percentage = percentage;
        this.validFrom = validFrom;
        this.validTo = validTo;
    }

    public UUID getId() {
        return id;
    }

    public String getBankCode() {
        return bankCode;
    }

    public FeeOperationType getOperationType() {
        return operationType;
    }

    public BigDecimal getFixedAmount() {
        return fixedAmount;
    }

    public BigDecimal getPercentage() {
        return percentage;
    }

    public Instant getValidFrom() {
        return validFrom;
    }

    public Instant getValidTo() {
        return validTo;
    }
}
