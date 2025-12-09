package es.nextdigital.demo.model;

import java.math.BigDecimal;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

@Entity
public class Card {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.ORDINAL)
    private CardType type;

    private Long limit;

    private String cardNumber;

    private String pinHash;

    private boolean active;

    private BigDecimal withdrawLimit;

    private BigDecimal creditLimit;

    @ManyToOne
    private Account account;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public CardType getType() {
        return type;
    }

    public void setType(CardType type) {
        this.type = type;
    }

    public Long getLimit() {
        return limit;
    }

    public void setLimit(Long limit) {
        this.limit = limit;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public String getPinHash() {
        return pinHash;
    }

    public void setPinHash(String pinHash) {
        this.pinHash = pinHash;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public BigDecimal getWithdrawLimit() {
        return withdrawLimit;
    }

    public void setWithdrawLimit(BigDecimal withdrawLimit) {
        this.withdrawLimit = withdrawLimit;
    }

    public BigDecimal getCreditLimit() {
        return creditLimit;
    }

    public void setCreditLimit(BigDecimal creditLimit) {
        this.creditLimit = creditLimit;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    @Override
    public String toString() {
        return "Card [id=" + id + ", type=" + type + ", limit=" + limit + ", cardNumber=" + cardNumber + ", pinHash="
                + pinHash + ", active=" + active + ", withdrawLimit=" + withdrawLimit + ", creditLimit=" + creditLimit
                + ", account=" + account + "]";
    }
}
