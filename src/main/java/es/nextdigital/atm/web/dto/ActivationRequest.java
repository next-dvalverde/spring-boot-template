package es.nextdigital.atm.web.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record ActivationRequest(
        String cardNumber,
        @NotNull(message = "INVALID_PIN_FORMAT")
        @Pattern(regexp = "\\d{4}", message = "INVALID_PIN_FORMAT")
        String pin,
        String atmId) {
}
