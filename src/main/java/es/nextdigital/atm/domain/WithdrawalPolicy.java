package es.nextdigital.atm.domain;

import java.math.BigDecimal;

/**
 * Reglas de la retirada (PLAN.md &sect;3). El limite de tarjeta se compara
 * contra amount a secas — la comision va aparte y solo cuenta contra el
 * disponible. Es limite por operacion, no diario (C-3, documentado).
 */
public final class WithdrawalPolicy {

    private WithdrawalPolicy() {
    }

    public static void checkDebit(BigDecimal amount, BigDecimal fee,
                                  BigDecimal withdrawalLimit, BigDecimal balance) {
        checkLimit(amount, withdrawalLimit);
        if (balance.compareTo(amount.add(fee)) < 0) {
            throw new BusinessException(ErrorCode.INSUFFICIENT_FUNDS, "Insufficient funds");
        }
    }

    public static void checkCredit(BigDecimal amount, BigDecimal fee, BigDecimal withdrawalLimit,
                                   BigDecimal creditUsed, BigDecimal creditLimit) {
        checkLimit(amount, withdrawalLimit);
        if (creditUsed.add(amount).add(fee).compareTo(creditLimit) > 0) {
            throw new BusinessException(ErrorCode.CREDIT_LIMIT_EXCEEDED, "Credit limit exceeded");
        }
    }

    private static void checkLimit(BigDecimal amount, BigDecimal withdrawalLimit) {
        if (amount.compareTo(withdrawalLimit) > 0) {
            throw new BusinessException(ErrorCode.LIMIT_EXCEEDED, "Amount exceeds the card withdrawal limit");
        }
    }
}
