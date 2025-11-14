package es.nextdigital.demo.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import es.nextdigital.demo.model.TransactionType;

public class TransactionDTO {
    private Long id;
    private TransactionType type;
    private BigDecimal amount;
    private LocalDateTime date;
    private String targetIban;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public TransactionType getType() { return type; }
    public void setType(TransactionType type) { this.type = type; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public LocalDateTime getDate() { return date; }
    public void setDate(LocalDateTime date) { this.date = date; }

    public String getTargetIban() { return targetIban; }
    public void setTargetIban(String targetIban) { this.targetIban = targetIban; }
}