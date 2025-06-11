package es.nextdigital.demo.application;

import java.math.BigDecimal;
import java.util.UUID;

public record MoneyDepositCommand(UUID cardId, BigDecimal amountToDeposit, String originBank) {
}
