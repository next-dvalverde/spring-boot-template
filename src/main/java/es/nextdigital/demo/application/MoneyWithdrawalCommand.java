package es.nextdigital.demo.application;

import java.math.BigDecimal;
import java.util.UUID;

public record MoneyWithdrawalCommand(UUID cardId, BigDecimal amountToWithdraw, String originBank) {
}
