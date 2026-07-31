package es.nextdigital.atm.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Version;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
public class Card {

    @Id
    private UUID id;

    private String cardNumber;

    private UUID accountId;

    @Enumerated(EnumType.STRING)
    private CardType type;

    @Enumerated(EnumType.STRING)
    private CardStatus status;

    private String pinHash;

    private boolean pinChangeRequired;

    private BigDecimal withdrawalLimit;

    private BigDecimal creditLimit;

    private BigDecimal creditUsed;

    private int failedAttempts;

    // Tambien en Card, no solo en Account (DECISIONS.md B-8): el camino de
    // credito no toca account.balance, solo creditUsed, y failedAttempts muta
    // en cada validacion de PIN.
    @Version
    private long version;

    protected Card() {
    }

    public Card(UUID id, String cardNumber, UUID accountId, CardType type, CardStatus status,
                String pinHash, boolean pinChangeRequired, BigDecimal withdrawalLimit,
                BigDecimal creditLimit, BigDecimal creditUsed) {
        this.id = id;
        this.cardNumber = cardNumber;
        this.accountId = accountId;
        this.type = type;
        this.status = status;
        this.pinHash = pinHash;
        this.pinChangeRequired = pinChangeRequired;
        this.withdrawalLimit = withdrawalLimit;
        this.creditLimit = creditLimit;
        this.creditUsed = creditUsed;
    }

    public UUID getId() {
        return id;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public UUID getAccountId() {
        return accountId;
    }

    public CardType getType() {
        return type;
    }

    public CardStatus getStatus() {
        return status;
    }

    public String getPinHash() {
        return pinHash;
    }

    public boolean isPinChangeRequired() {
        return pinChangeRequired;
    }

    public BigDecimal getWithdrawalLimit() {
        return withdrawalLimit;
    }

    public BigDecimal getCreditLimit() {
        return creditLimit;
    }

    public BigDecimal getCreditUsed() {
        return creditUsed;
    }

    public int getFailedAttempts() {
        return failedAttempts;
    }

    public long getVersion() {
        return version;
    }

    /**
     * Con 4 digitos el espacio son 10^4 combinaciones: quien protege de verdad
     * es este contador, no el hash. Devuelve si la tarjeta ha quedado bloqueada.
     */
    public boolean recordPinFailure() {
        failedAttempts++;
        if (failedAttempts >= MAX_PIN_ATTEMPTS) {
            status = CardStatus.BLOCKED;
        }
        return status == CardStatus.BLOCKED;
    }

    // B-1: un acierto resetea el contador; sin esto la tarjeta se bloquea sola
    // tras 3 fallos repartidos en meses.
    public void resetPinFailures() {
        failedAttempts = 0;
    }

    // La activacion fuerza el cambio del PIN del sobre: el que viaja impreso
    // no puede quedarse como PIN definitivo.
    public void activate() {
        status = CardStatus.ACTIVE;
        pinChangeRequired = true;
    }

    // Una disposicion de credito no toca account.balance: sube creditUsed y
    // sus apuntes van con settlesAccount = false (A-1).
    public void useCredit(BigDecimal amount) {
        creditUsed = creditUsed.add(amount);
    }

    public void changePin(String newPinHash) {
        pinHash = newPinHash;
        pinChangeRequired = false;
    }

    private static final int MAX_PIN_ATTEMPTS = 3;
}
