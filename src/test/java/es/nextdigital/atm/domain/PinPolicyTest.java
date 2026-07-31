package es.nextdigital.atm.domain;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

class PinPolicyTest {

    private static final String CURRENT = "1975";

    private void assertRejected(String newPin) {
        BusinessException ex = assertThrows(BusinessException.class,
                () -> PinPolicy.validateNewPin(newPin, CURRENT));
        assertEquals(ErrorCode.INVALID_PIN_FORMAT, ex.code());
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"123", "12345", "12a4", "1 23", "abcd", ""})
    void rejectsAnythingButFourDigits(String newPin) {
        assertRejected(newPin);
    }

    @Test
    void rejectsReusingTheCurrentPin() {
        assertRejected(CURRENT);
    }

    @ParameterizedTest
    @ValueSource(strings = {"0000", "1111", "9999"})
    void rejectsRepeatedDigits(String newPin) {
        assertRejected(newPin);
    }

    @ParameterizedTest
    @ValueSource(strings = {"1234", "0123", "6789"})
    void rejectsAscendingSequences(String newPin) {
        assertRejected(newPin);
    }

    @ParameterizedTest
    @ValueSource(strings = {"4321", "9876", "3210"})
    void rejectsDescendingSequences(String newPin) {
        assertRejected(newPin);
    }

    @ParameterizedTest
    @ValueSource(strings = {"2846", "1357", "1212", "8091"})
    void acceptsNonTrivialPins(String newPin) {
        assertDoesNotThrow(() -> PinPolicy.validateNewPin(newPin, CURRENT));
    }
}
