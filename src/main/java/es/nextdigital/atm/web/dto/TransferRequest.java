package es.nextdigital.atm.web.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

// La forma del IBAN la valida el dominio (IbanValidator): aqui solo presencia.
// El concepto no valida longitud: el servicio lo trunca al ancho de la columna.
public record TransferRequest(
        @NotNull(message = "INVALID_IBAN")
        String targetIban,
        @NotNull(message = "INVALID_AMOUNT")
        @Positive(message = "INVALID_AMOUNT")
        @Digits(integer = 17, fraction = 2, message = "INVALID_AMOUNT")
        BigDecimal amount,
        String concept) {
}
