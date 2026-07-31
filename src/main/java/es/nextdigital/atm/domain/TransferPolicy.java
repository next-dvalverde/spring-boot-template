package es.nextdigital.atm.domain;

import java.math.BigDecimal;

/**
 * Reglas puras de la transferencia. Sin limite de tarjeta: withdrawalLimit es
 * de la retirada de efectivo, no del envio entre cuentas.
 */
public final class TransferPolicy {

    private TransferPolicy() {
    }

    public static void requireDifferentTarget(String sourceIban, String targetIban) {
        if (sourceIban.equals(targetIban)) {
            throw new BusinessException(ErrorCode.INVALID_IBAN, "Target IBAN must differ from the source account");
        }
    }

    public static void checkFunds(BigDecimal balance, BigDecimal amount, BigDecimal fee) {
        if (balance.compareTo(amount.add(fee)) < 0) {
            throw new BusinessException(ErrorCode.INSUFFICIENT_FUNDS, "Insufficient funds");
        }
    }
}
