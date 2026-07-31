package es.nextdigital.atm.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

/**
 * Puro: recibe datos y devuelve decisiones. No lee fee_policy — el servicio
 * carga la politica y la pasa como FeeRule (A-2). Sin regla, comision 0.
 */
public final class FeeCalculator {

    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    private FeeCalculator() {
    }

    // La identidad de banco se resuelve por bankId, nunca por prefijos de IBAN.
    public static BigDecimal forWithdrawal(UUID atmBankId, UUID accountBankId, BigDecimal amount, FeeRule rule) {
        if (atmBankId.equals(accountBankId)) {
            return zero();
        }
        return apply(amount, rule);
    }

    private static BigDecimal apply(BigDecimal amount, FeeRule rule) {
        if (rule == null) {
            return zero();
        }
        BigDecimal percentagePart = amount.multiply(rule.percentage())
                .divide(HUNDRED, 2, RoundingMode.HALF_UP);
        return rule.fixedAmount().add(percentagePart).setScale(2, RoundingMode.HALF_UP);
    }

    private static BigDecimal zero() {
        return BigDecimal.ZERO.setScale(2);
    }
}
