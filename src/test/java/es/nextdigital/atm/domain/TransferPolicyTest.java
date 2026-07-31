package es.nextdigital.atm.domain;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class TransferPolicyTest {

    @Test
    void transferToTheSameAccountIsRejected() {
        BusinessException ex = assertThrows(BusinessException.class, () ->
                TransferPolicy.requireDifferentTarget("ES0515000001000000000001",
                        "ES0515000001000000000001"));
        assertEquals(ErrorCode.INVALID_IBAN, ex.code());
    }

    @Test
    void insufficientBalanceIsRejected() {
        BusinessException ex = assertThrows(BusinessException.class, () ->
                TransferPolicy.checkFunds(new BigDecimal("99.99"), new BigDecimal("100.00"),
                        new BigDecimal("0.00")));
        assertEquals(ErrorCode.INSUFFICIENT_FUNDS, ex.code());
    }

    // La comision cuenta contra el disponible tambien aqui.
    @Test
    void feePushingOverTheBalanceIsRejected() {
        BusinessException ex = assertThrows(BusinessException.class, () ->
                TransferPolicy.checkFunds(new BigDecimal("101.00"), new BigDecimal("100.00"),
                        new BigDecimal("2.50")));
        assertEquals(ErrorCode.INSUFFICIENT_FUNDS, ex.code());
    }

    @Test
    void exactBalanceIncludingFeePasses() {
        assertDoesNotThrow(() ->
                TransferPolicy.checkFunds(new BigDecimal("102.50"), new BigDecimal("100.00"),
                        new BigDecimal("2.50")));
    }
}
