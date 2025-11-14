package es.nextdigital.demo.dto;

import java.math.BigDecimal;

public class AccountDTO {
    private Long id;
    private String iban;
    private BigDecimal balance;


    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getIban() { return iban; }
    public void setIban(String iban) { this.iban = iban; }

    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }
}