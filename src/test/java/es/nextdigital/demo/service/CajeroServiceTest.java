package es.nextdigital.demo.service;

import es.nextdigital.demo.model.*;
import es.nextdigital.demo.repository.CuentaRepository;
import es.nextdigital.demo.repository.TarjetaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CajeroServiceTest {

    private CuentaRepository cuentaRepository;
    private TarjetaRepository tarjetaRepository;
    private CajeroService cajeroService;

    @BeforeEach
    void setup() {
        cuentaRepository = new CuentaRepository();
        tarjetaRepository = new TarjetaRepository();
        cajeroService = new CajeroService(cuentaRepository, tarjetaRepository);

        // Cuenta inicial
        Cuenta c = new Cuenta(1L, "ES123", 1000.0);
        cuentaRepository.save(c);

        // Tarjeta activada
        Tarjeta t = new Tarjeta(1L, "1111", TipoTarjeta.DEBITO, 500.0, 0.0, 1L);
        t.setActivada(true);
        t.setPinPendienteCambio(false);
        tarjetaRepository.save(t);
    }

    @Test
    void retirar_DebeRestarSaldoCorrectamente() {
        cajeroService.retirar(1L, 100.0, true);

        Cuenta cuenta = cuentaRepository.findById(1L);
        assertEquals(900.0, cuenta.getSaldo());
        assertEquals(1, cuenta.getMovimientos().size());
        assertEquals(TipoMovimiento.RETIRADA, cuenta.getMovimientos().get(0).getTipo());
    }

    @Test
    void retirar_SaldoInsuficiente_DebeLanzarError() {
        assertThrows(RuntimeException.class, () -> {
            cajeroService.retirar(1L, 2000.0, true);
        });
    }
}