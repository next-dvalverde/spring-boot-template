package es.nextdigital.demo.apirest.dto;

import java.math.BigDecimal;

public record MoneyWithdrawalRequestDTO(BigDecimal quantityToWithdraw, String bank) {
}
