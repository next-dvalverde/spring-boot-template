package entities;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "accounts")
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "account_number", nullable = false, unique = true, length = 20)
    private String accountNumber;
    
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal balance = BigDecimal.ZERO; 
    
    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Transaction> transactions = new ArrayList<>();
    
    @Column(name = "iban", nullable = false, unique = true, length = 34)
    private String iban; 
    
    @Column(name = "account_type", length = 20)
    private String accountType; 
    
    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;
    
    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Card> cards = new ArrayList<>(); 
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer; 
    
    // Constructores
    public Account() {}
    
    public Account(String accountNumber, String iban, Customer customer) {
        this.accountNumber = accountNumber;
        this.iban = iban;
        this.customer = customer;
        this.balance = BigDecimal.ZERO;
        this.isActive = true;
    }
    
    // Métodos de negocio
    public boolean canWithdraw(BigDecimal amount) {
        return balance.compareTo(amount) >= 0;
    }
    
    public void deposit(BigDecimal amount) {
        this.balance = this.balance.add(amount);
    }
    
    public void withdraw(BigDecimal amount) {
        if (canWithdraw(amount)) {
            this.balance = this.balance.subtract(amount);
        } else {
            throw new IllegalArgumentException("Saldo insuficiente");
        }
    }
    
    // Getters y Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public String getIban() {
        return iban;
    }

    public void setIban(String iban) {
        this.iban = iban;
    }

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public List<Card> getCards() {
        return cards;
    }

    public void setCards(List<Card> cards) {
        this.cards = cards;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }
    
    // Métodos helper para la relación bidireccional
    public void addCard(Card card) {
        cards.add(card);
        card.setAccount(this);
    }
    
    public void removeCard(Card card) {
        cards.remove(card);
        card.setAccount(null);
    }
    
    @Override
    public String toString() {
        return "Account{" +
                "id=" + id +
                ", Número de cuenta ='" + accountNumber + '\'' +
                ", saldo =" + balance +
                ", iban ='" + iban + '\'' +
                '}';
    }
}
