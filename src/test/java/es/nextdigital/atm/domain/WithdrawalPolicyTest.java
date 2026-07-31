package es.nextdigital.atm.domain;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class WithdrawalPolicyTest {

    private static final BigDecimal LIMIT = new BigDecimal("500.00");
    private static final BigDecimal NO_FEE = new BigDecimal("0.00");

    private ErrorCode debitError(String amount, String fee, String balance) {
        BusinessException ex = assertThrows(BusinessException.class, () ->
                WithdrawalPolicy.checkDebit(new BigDecimal(amount), new BigDecimal(fee),
                        LIMIT, new BigDecimal(balance)));
        return ex.code();
    }

    @Test
    void debitWithoutBalanceIsRejected() {
        assertEquals(ErrorCode.INSUFFICIENT_FUNDS, debitError("100.00", "0.00", "99.99"));
    }

    // La regla que mas se olvida: el limite se compara contra amount, pero el
    // disponible contra amount + fee. La comision puede empujar por encima.
    @Test
    void feePushingOverTheBalanceIsRejected() {
        assertEquals(ErrorCode.INSUFFICIENT_FUNDS, debitError("100.00", "1.50", "101.00"));
    }

    @Test
    void amountOverCardLimitIsRejectedEvenWithBalance() {
        assertEquals(ErrorCode.LIMIT_EXCEEDED, debitError("500.01", "0.00", "10000.00"));
    }

    @Test
    void limitComparesAmountAloneFeeAside() {
        // amount == limite pasa la guarda del limite aunque amount+fee lo supere.
        assertDoesNotThrow(() ->
                WithdrawalPolicy.checkDebit(new BigDecimal("500.00"), new BigDecimal("1.50"),
                        LIMIT, new BigDecimal("501.50")));
    }

    @Test
    void exactBalanceIncludingFeePasses() {
        assertDoesNotThrow(() ->
                WithdrawalPolicy.checkDebit(new BigDecimal("100.00"), new BigDecimal("1.50"),
                        LIMIT, new BigDecimal("101.50")));
    }

    @Test
    void creditWithinLimitPasses() {
        assertDoesNotThrow(() ->
                WithdrawalPolicy.checkCredit(new BigDecimal("100.00"), NO_FEE, LIMIT,
                        new BigDecimal("399.99"), new BigDecimal("500.00")));
    }

    @Test
    void creditUsedPlusAmountOverTheCreditLimitIsRejected() {
        BusinessException ex = assertThrows(BusinessException.class, () ->
                WithdrawalPolicy.checkCredit(new BigDecimal("100.00"), NO_FEE, LIMIT,
                        new BigDecimal("400.01"), new BigDecimal("500.00")));
        assertEquals(ErrorCode.CREDIT_LIMIT_EXCEEDED, ex.code());
    }

    @Test
    void feePushingOverTheCreditLimitIsRejected() {
        BusinessException ex = assertThrows(BusinessException.class, () ->
                WithdrawalPolicy.checkCredit(new BigDecimal("100.00"), new BigDecimal("1.50"), LIMIT,
                        new BigDecimal("399.00"), new BigDecimal("500.00")));
        assertEquals(ErrorCode.CREDIT_LIMIT_EXCEEDED, ex.code());
    }

    @Test
    void creditRespectsTheCardLimitToo() {
        BusinessException ex = assertThrows(BusinessException.class, () ->
                WithdrawalPolicy.checkCredit(new BigDecimal("500.01"), NO_FEE, LIMIT,
                        BigDecimal.ZERO, new BigDecimal("10000.00")));
        assertEquals(ErrorCode.LIMIT_EXCEEDED, ex.code());
    }
}
