package es.nextdigital.atm.domain;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

// B-2: esta politica la invocan las TRES operaciones de dinero. Un ingreso
// negativo es una retirada sin limite.
class AmountPolicyTest {

    private void assertRejected(BigDecimal amount) {
        BusinessException ex = assertThrows(BusinessException.class,
                () -> AmountPolicy.requirePositive(amount));
        assertEquals(ErrorCode.INVALID_AMOUNT, ex.code());
    }

    @Test
    void rejectsNull() {
        assertRejected(null);
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "0.00", "-0.01", "-100.00"})
    void rejectsZeroAndNegative(String amount) {
        assertRejected(new BigDecimal(amount));
    }

    @Test
    void rejectsMoreThanTwoDecimals() {
        assertRejected(new BigDecimal("10.005"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"0.01", "50", "50.5", "120.00"})
    void acceptsPositiveAmounts(String amount) {
        assertDoesNotThrow(() -> AmountPolicy.requirePositive(new BigDecimal(amount)));
    }
}
