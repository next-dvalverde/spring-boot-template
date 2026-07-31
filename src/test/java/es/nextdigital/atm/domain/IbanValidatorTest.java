package es.nextdigital.atm.domain;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

class IbanValidatorTest {

    private void assertRejected(String iban) {
        BusinessException ex = assertThrows(BusinessException.class, () -> IbanValidator.validate(iban));
        assertEquals(ErrorCode.INVALID_IBAN, ex.code());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "ES0515000001000000000001",   // los del seed
            "ES7515000001000000000002",
            "GB82WEST12345698765432",     // ejemplo canonico ISO
            "DE89370400440532013000"})
    void acceptsValidIbans(String iban) {
        assertDoesNotThrow(() -> IbanValidator.validate(iban));
    }

    // Un digito cambiado rompe el checksum: es exactamente el error de tecleo
    // que el mod 97 existe para atrapar.
    @ParameterizedTest
    @ValueSource(strings = {
            "ES0515000001000000000002",
            "ES0615000001000000000001",
            "GB82WEST12345698765431"})
    void rejectsBrokenChecksums(String iban) {
        assertRejected(iban);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {
            "",
            "ES05",
            "es0515000001000000000001",       // minusculas
            "ES05 1500 0001 0000 0000 0001",  // espacios: se exige forma canonica
            "E50515000001000000000001",       // pais malformado
            "ESAB15000001000000000001",       // digitos de control no numericos
            "ES05150000010000000000019999999999999"})
    void rejectsMalformedStructures(String iban) {
        assertRejected(iban);
    }

    @Test
    void entityCodeIsPositions5To8NotThePrefix() {
        assertEquals("1500", IbanValidator.entityCode("ES0515000001000000000001"));
    }
}
