package es.nextdigital.demo.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "cards")
public class Card {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String cardNumber;

    @Column(nullable = false)
    private String pinHash; // guardamos solo el hash del pin

    @Enumerated(EnumType.STRING)
    private CardStatus status = CardStatus.NEW;

    @Column(nullable = false)
    private BigDecimal withdrawLimit = BigDecimal.valueOf(1000); // valor default

    @OneToOne
    @JoinColumn(name = "account_id")
    private Account account;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCardNumber() { return cardNumber; }
    public void setCardNumber(String cardNumber) { this.cardNumber = cardNumber; }

    public String getPinHash() { return pinHash; }
    public void setPinHash(String pinHash) { this.pinHash = pinHash; }

    public CardStatus getStatus() { return status; }
    public void setStatus(CardStatus status) { this.status = status; }

    public BigDecimal getWithdrawLimit() { return withdrawLimit; }
    public void setWithdrawLimit(BigDecimal withdrawLimit) { this.withdrawLimit = withdrawLimit; }

    public Account getAccount() { return account; }
    public void setAccount(Account account) { this.account = account; }
}