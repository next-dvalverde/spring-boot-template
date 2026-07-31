package es.nextdigital.atm.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class FeeCalculatorTest {

    private static final UUID OWN_BANK = UUID.randomUUID();
    private static final UUID OTHER_BANK = UUID.randomUUID();

    @Test
    void sameBankPaysNoFeeEvenWithARule() {
        FeeRule rule = new FeeRule(new BigDecimal("1.50"), new BigDecimal("1.0000"));
        assertEquals(new BigDecimal("0.00"),
                FeeCalculator.forWithdrawal(OWN_BANK, OWN_BANK, new BigDecimal("100.00"), rule));
    }

    @Test
    void foreignBankWithoutRuleChargesZero() {
        assertEquals(new BigDecimal("0.00"),
                FeeCalculator.forWithdrawal(OTHER_BANK, OWN_BANK, new BigDecimal("100.00"), null));
    }

    @Test
    void foreignBankChargesFixedPart() {
        FeeRule rule = new FeeRule(new BigDecimal("1.50"), new BigDecimal("0.0000"));
        assertEquals(new BigDecimal("1.50"),
                FeeCalculator.forWithdrawal(OTHER_BANK, OWN_BANK, new BigDecimal("100.00"), rule));
    }

    @Test
    void foreignBankChargesFixedPlusPercentage() {
        FeeRule rule = new FeeRule(new BigDecimal("1.50"), new BigDecimal("1.0000"));
        // 1.50 + 1% de 200.00 = 3.50
        assertEquals(new BigDecimal("3.50"),
                FeeCalculator.forWithdrawal(OTHER_BANK, OWN_BANK, new BigDecimal("200.00"), rule));
    }

    // El medio centimo sube: HALF_UP, nunca truncar dinero.
    @Test
    void percentageRoundsHalfUp() {
        FeeRule rule = new FeeRule(BigDecimal.ZERO, new BigDecimal("1.5000"));
        // 1.5% de 33.33 = 0.49995 -> 0.50
        assertEquals(new BigDecimal("0.50"),
                FeeCalculator.forWithdrawal(OTHER_BANK, OWN_BANK, new BigDecimal("33.33"), rule));
    }
}
