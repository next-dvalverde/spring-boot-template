package es.nextdigital.demo.model;

import java.math.BigDecimal;
import java.util.Set;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;

@Entity
public class Account {
    @Id
    @GeneratedValue
    private Long id;

    private String iban;

    private BigDecimal balance;

    @OneToMany(mappedBy = "account")
    Set<Card> cards;

    @OneToMany(mappedBy = "account")
    Set<CardOperation> operations;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getIban() {
        return iban;
    }

    public void setIban(String iban) {
        this.iban = iban;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public Set<Card> getCards() {
        return cards;
    }

    public void setCards(Set<Card> cards) {
        this.cards = cards;
    }

    public Set<CardOperation> getOperations() {
        return operations;
    }

    public void setOperations(Set<CardOperation> operations) {
        this.operations = operations;
    }

    @Override
    public String toString() {
        return "Account [id=" + id + ", IBAN=" + iban + ", balance=" + balance + ", cards=" + cards + ", operations="
                + operations + "]";
    }
}
