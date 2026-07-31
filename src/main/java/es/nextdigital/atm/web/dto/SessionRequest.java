package es.nextdigital.atm.web.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

// El message de cada constraint es un codigo del catalogo: lo lee el
// GlobalExceptionHandler. cardNumber y atmId no llevan forma: un valor raro
// acaba en 404, que no revela nada.
public record SessionRequest(
        String cardNumber,
        @NotNull(message = "INVALID_PIN_FORMAT")
        @Pattern(regexp = "\\d{4}", message = "INVALID_PIN_FORMAT")
        String pin,
        String atmId) {
}
