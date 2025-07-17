package es.nextdigital.demo.service;

import es.nextdigital.demo.entity.Banco;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class BancoServiceTest {

    private BancoService bancoService;

    @BeforeEach
    void setUp() {
        bancoService = new BancoService(null);
    }

    @Test
    void testCalcularComisionCajeroAjeno() {
        BigDecimal cantidad = BigDecimal.valueOf(50);
        BigDecimal resultado = bancoService.calcularComisionCajeroAjeno(null, cantidad);
        assertEquals(BigDecimal.valueOf(2), resultado);

        cantidad = BigDecimal.valueOf(500);
        resultado = bancoService.calcularComisionCajeroAjeno(null, cantidad);
        assertTrue(resultado.compareTo(BigDecimal.valueOf(5)) == 0);
    }

    @Test
    void testCalcularComisionTransferencia() {
        BigDecimal cantidad = BigDecimal.valueOf(50);
        BigDecimal resultado = bancoService.calcularComisionTransferencia(false, cantidad);
        assertEquals(BigDecimal.valueOf(1), resultado);

        cantidad = BigDecimal.valueOf(500);
        resultado = bancoService.calcularComisionTransferencia(false, cantidad);
        assertTrue(resultado.compareTo(BigDecimal.valueOf(2.5)) == 0);

        resultado = bancoService.calcularComisionTransferencia(true, cantidad);
        assertEquals(BigDecimal.ZERO, resultado);
    }

    @Test
    void testEsMismoBanco() {
        String iban1 = "ES6600491500051234567890";
        String iban2 = "ES6700491800059876543210";
        String iban3 = "ES6700501800059876543210";

        assertTrue(bancoService.esMismoBanco(iban1, iban2));
        assertFalse(bancoService.esMismoBanco(iban1, iban3));
    }

    @Test
    void testEsCajeroPropio() {
        Banco banco1 = new Banco();
        banco1.setId(1L);
        Banco banco2 = new Banco();
        banco2.setId(1L);
        Banco banco3 = new Banco();
        banco3.setId(2L);

        assertTrue(bancoService.esCajeroPropio(banco1, banco2));
        assertFalse(bancoService.esCajeroPropio(banco1, banco3));
        assertFalse(bancoService.esCajeroPropio(null, banco3));
        assertFalse(bancoService.esCajeroPropio(banco1, null));
        assertFalse(bancoService.esCajeroPropio(null, null));
    }
}
