package es.nextdigital.demo.dto;

import java.math.BigDecimal;
import es.nextdigital.demo.model.CardStatus;

public class CardDTO {
    private Long id;
    private String cardNumber;
    private CardStatus status;
    private BigDecimal withdrawLimit;


    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCardNumber() { return cardNumber; }
    public void setCardNumber(String cardNumber) { this.cardNumber = cardNumber; }

    public CardStatus getStatus() { return status; }
    public void setStatus(CardStatus status) { this.status = status; }

    public BigDecimal getWithdrawLimit() { return withdrawLimit; }
    public void setWithdrawLimit(BigDecimal withdrawLimit) { this.withdrawLimit = withdrawLimit; }
}