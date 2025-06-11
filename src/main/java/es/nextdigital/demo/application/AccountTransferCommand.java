package es.nextdigital.demo.application;

import java.math.BigDecimal;

public record AccountTransferCommand(String accountIban, BigDecimal amountToTransfer, String destinationIban,
		String destinationBank) {
}
