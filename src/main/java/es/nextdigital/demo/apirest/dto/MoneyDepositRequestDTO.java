package es.nextdigital.demo.apirest.dto;

import java.math.BigDecimal;

public record MoneyDepositRequestDTO(BigDecimal quantityToDeposit, String bank) {
}
