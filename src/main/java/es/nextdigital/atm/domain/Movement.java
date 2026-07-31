package es.nextdigital.atm.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
public class Movement {

    @Id
    private UUID id;

    private UUID accountId;

    private UUID cardId;

    // La v1 no lo lee: responde "que cajero dispenso esto" el dia que haga
    // falta auditoria (DECISIONS.md C-10).
    private UUID atmId;

    private UUID operationId;

    @Enumerated(EnumType.STRING)
    private MovementType type;

    private BigDecimal amount;

    private boolean settlesAccount;

    private BigDecimal balanceAfter;

    private String description;

    private String counterpartyIban;

    private Instant createdAt;

    protected Movement() {
    }

    public Movement(UUID id, UUID accountId, UUID cardId, UUID atmId, UUID operationId,
                    MovementType type, BigDecimal amount, boolean settlesAccount,
                    BigDecimal balanceAfter, String description, String counterpartyIban) {
        this.id = id;
        this.accountId = accountId;
        this.cardId = cardId;
        this.atmId = atmId;
        this.operationId = operationId;
        this.type = type;
        this.amount = amount;
        this.settlesAccount = settlesAccount;
        this.balanceAfter = balanceAfter;
        this.description = description;
        this.counterpartyIban = counterpartyIban;
        this.createdAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public UUID getAccountId() {
        return accountId;
    }

    public UUID getCardId() {
        return cardId;
    }

    public UUID getAtmId() {
        return atmId;
    }

    public UUID getOperationId() {
        return operationId;
    }

    public MovementType getType() {
        return type;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public boolean isSettlesAccount() {
        return settlesAccount;
    }

    public BigDecimal getBalanceAfter() {
        return balanceAfter;
    }

    public String getDescription() {
        return description;
    }

    public String getCounterpartyIban() {
        return counterpartyIban;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
