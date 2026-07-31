package es.nextdigital.atm.web;

import es.nextdigital.atm.domain.BusinessException;
import es.nextdigital.atm.domain.ErrorCode;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> business(BusinessException ex) {
        return ResponseEntity.status(ex.code().httpStatus())
                .body(ErrorResponse.of(ex.code(), ex.getMessage()));
    }

    // Convencion: el message de cada constraint de los DTOs es un codigo del
    // catalogo (p. ej. @Pattern(..., message = "INVALID_PIN_FORMAT")). Asi la
    // validacion de forma responde con los mismos codigos estables que el
    // dominio, sin una segunda tabla de mapeo.
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> beanValidation(MethodArgumentNotValidException ex) {
        ErrorCode code = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> toErrorCode(error.getDefaultMessage()))
                .filter(c -> c != null)
                .findFirst()
                .orElse(ErrorCode.INVALID_AMOUNT);
        return ResponseEntity.status(code.httpStatus())
                .body(ErrorResponse.of(code, "Invalid request"));
    }

    // El conflicto optimista ya viene traducido por Spring Data: capturar la
    // OptimisticLockException de JPA aqui no se dispararia nunca (B-3).
    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<ErrorResponse> concurrentModification(ObjectOptimisticLockingFailureException ex) {
        ErrorCode code = ErrorCode.CONCURRENT_MODIFICATION;
        return ResponseEntity.status(code.httpStatus())
                .body(ErrorResponse.of(code, "The resource was modified concurrently, retry the operation"));
    }

    private static ErrorCode toErrorCode(String constraintMessage) {
        try {
            return ErrorCode.valueOf(constraintMessage);
        } catch (IllegalArgumentException | NullPointerException ex) {
            return null;
        }
    }
}
