package es.nextdigital.demo.service;

import es.nextdigital.demo.adapter.in.rest.dto.*;
import es.nextdigital.demo.adapter.out.repositories.*;
import es.nextdigital.demo.application.models.*;
import es.nextdigital.demo.application.service.*;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RetiradaServiceTest {
    @Test
    void testRetirarConTarjetaNoActiva() {
        // Arrange: Mocks y datos de ejemplo
        TarjetaRepository tarjetaRepo = mock(TarjetaRepository.class);
        CajeroRepository cajeroRepo = mock(CajeroRepository.class);
        MovimientoRepository movimientoRepo = mock(MovimientoRepository.class);
        CuentaRepository cuentaRepo = mock(CuentaRepository.class);

        Tarjeta tarjeta = new Tarjeta();
        tarjeta.setNumero("1111");
        tarjeta.setActiva(false);

        when(tarjetaRepo.findByNumero("1111")).thenReturn(Optional.of(tarjeta));

        RetiradaService service = new RetiradaService(tarjetaRepo, cajeroRepo, movimientoRepo, cuentaRepo);

        RetiradaDTO dto = new RetiradaDTO();
        dto.setNumeroTarjeta("1111");
        dto.setCantidad(50.0);
        dto.setIdCajero(1L);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            service.retirar(dto);
        });

        assertEquals("Tarjeta no activada", ex.getMessage());
    }

    @Test
    void testRetirarConSaldoInsuficiente() {
        // Arrange
        TarjetaRepository tarjetaRepo = mock(TarjetaRepository.class);
        CajeroRepository cajeroRepo = mock(CajeroRepository.class);
        MovimientoRepository movimientoRepo = mock(MovimientoRepository.class);
        CuentaRepository cuentaRepo = mock(CuentaRepository.class);

        Cuenta cuenta = new Cuenta();
        cuenta.setSaldo(10.0);

        Tarjeta tarjeta = new Tarjeta();
        tarjeta.setNumero("1111");
        tarjeta.setActiva(true);
        tarjeta.setTipo("DEBITO");
        tarjeta.setCuentaAsociada(cuenta);

        Cajero cajero = new Cajero();
        cajero.setId(1L);
        cajero.setBancoPropietario(new Banco());
        cajero.setComisionOtraEntidad(2.0);

        when(tarjetaRepo.findByNumero("1111")).thenReturn(Optional.of(tarjeta));
        when(cajeroRepo.findById(1L)).thenReturn(Optional.of(cajero));

        RetiradaService service = new RetiradaService(tarjetaRepo, cajeroRepo, movimientoRepo, cuentaRepo);

        RetiradaDTO dto = new RetiradaDTO();
        dto.setNumeroTarjeta("1111");
        dto.setCantidad(50.0);
        dto.setIdCajero(1L);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            service.retirar(dto);
        });

        assertEquals("Saldo insuficiente", ex.getMessage());
    }

}
