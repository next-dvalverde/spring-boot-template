package es.nextdigital.atm.web.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record PinChangeRequest(
        @NotNull(message = "INVALID_PIN_FORMAT")
        @Pattern(regexp = "\\d{4}", message = "INVALID_PIN_FORMAT")
        String currentPin,
        @NotNull(message = "INVALID_PIN_FORMAT")
        @Pattern(regexp = "\\d{4}", message = "INVALID_PIN_FORMAT")
        String newPin) {
}
