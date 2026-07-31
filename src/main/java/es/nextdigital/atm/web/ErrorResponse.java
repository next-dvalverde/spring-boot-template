package es.nextdigital.atm.web;

import es.nextdigital.atm.domain.ErrorCode;
import java.time.Instant;

public record ErrorResponse(String code, String message, Instant timestamp) {

    public static ErrorResponse of(ErrorCode code, String message) {
        return new ErrorResponse(code.name(), message, Instant.now());
    }
}
