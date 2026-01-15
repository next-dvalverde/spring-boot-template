package entities;

import java.math.BigDecimal;
import java.util.UUID;

import enumerado.CardType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "cards")
public class Card {
    
	@Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "card_number", nullable = false, unique = true, length = 16)
    private String cardNumber;
    
    @Column(name = "pin_hash", nullable = false)
    private String pinHash;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "card_type", nullable = false, length = 10)
    private CardType type; 
    
    @Column(name = "daily_withdrawal_limit", precision = 15, scale = 2)
    private BigDecimal dailyWithdrawalLimit;
    
    @Column(name = "credit_limit", precision = 15, scale = 2)
    private BigDecimal creditLimit;
    
    @Column(name = "is_active", nullable = false)
    private boolean isActive = false;
    
    @Column(name = "pin_changed", nullable = false)
    private boolean pinChanged = false;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;
    
    // Constructores
    public Card() {}
    
    public Card(String cardNumber, String pinHash, CardType type, 
            BigDecimal dailyWithdrawalLimit, BigDecimal creditLimit, 
            boolean isActive, boolean pinChanged, Account account) {
	    this.cardNumber = cardNumber;
	    this.pinHash = pinHash;
	    this.type = type;
	    this.dailyWithdrawalLimit = dailyWithdrawalLimit;
	    this.creditLimit = creditLimit;
	    this.isActive = isActive;
	    this.pinChanged = pinChanged;
	    this.account = account;
    }
    
    // Getters y Setters
    public UUID getId() { 
    	return id; 
    }
    
    public void setId(UUID id) { 
    	this.id = id; 
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
    
    public CardType getType() { 
    	return type; 
    }
    
    public void setType(CardType type) { 
    	this.type = type; 
    }
    
    public BigDecimal getDailyWithdrawalLimit() { 
    	return dailyWithdrawalLimit; 
    }
    
    public void setDailyWithdrawalLimit(BigDecimal dailyWithdrawalLimit) { 
        this.dailyWithdrawalLimit = dailyWithdrawalLimit; 
    }
    
    public BigDecimal getCreditLimit() { 
    	return creditLimit; 
    }
    
    public void setCreditLimit(BigDecimal creditLimit) { 
        this.creditLimit = creditLimit; 
    }
    
    public boolean isActive() { 
    	return isActive; 
    }
    
    public void setActive(boolean active) { 
    	isActive = active; 
    }
    
    public boolean isPinChanged() { 
    	return pinChanged; 
    }
    
    public void setPinChanged(boolean pinChanged) { 
    	this.pinChanged = pinChanged; 
    }
    
    public Account getAccount() { 
    	return account; 
    }
    
    public void setAccount(Account account) { 
    	this.account = account; 
    }
}