package es.nextdigital.atm.web.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record WithdrawalRequest(
        @NotNull(message = "INVALID_AMOUNT")
        @Positive(message = "INVALID_AMOUNT")
        @Digits(integer = 17, fraction = 2, message = "INVALID_AMOUNT")
        BigDecimal amount) {
}
