package es.nextdigital.demo.service;

import es.nextdigital.demo.model.*;
import es.nextdigital.demo.repository.CuentaRepository;
import es.nextdigital.demo.repository.TarjetaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class IbanValidatorTest {

    @Test
    void validarIban_InvalidoDebeFallar() {
        assertFalse(IbanValidator.validarIBAN("1234"));
    }

    @Test
    void validarIban_ValidoDebePasar() {
        assertTrue(IbanValidator.validarIBAN("ES9121000418450200051332"));
    }

}