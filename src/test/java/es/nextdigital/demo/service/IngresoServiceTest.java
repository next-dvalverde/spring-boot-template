package es.nextdigital.demo.service;

import es.nextdigital.demo.adapter.in.rest.dto.IngresoDTO;
import es.nextdigital.demo.adapter.out.repositories.*;
import es.nextdigital.demo.application.models.*;
import es.nextdigital.demo.application.service.*;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class IngresoServiceTest {

    @Test
    void testIngresoEnCajeroDeOtroBancoNoPermitido() {
        // Arrange
        TarjetaRepository tarjetaRepo = mock(TarjetaRepository.class);
        CajeroRepository cajeroRepo = mock(CajeroRepository.class);
        MovimientoRepository movimientoRepo = mock(MovimientoRepository.class);
        CuentaRepository cuentaRepo = mock(CuentaRepository.class);

        Banco bancoTarjeta = new Banco();
        bancoTarjeta.setId(1L);

        Banco bancoCajero = new Banco();
        bancoCajero.setId(2L);

        Cuenta cuenta = new Cuenta();

        Tarjeta tarjeta = new Tarjeta();
        tarjeta.setNumero("1111");
        tarjeta.setActiva(true);
        tarjeta.setBanco(bancoTarjeta);
        tarjeta.setCuentaAsociada(cuenta);

        Cajero cajero = new Cajero();
        cajero.setId(2L);
        cajero.setBancoPropietario(bancoCajero);

        when(tarjetaRepo.findByNumero("1111")).thenReturn(Optional.of(tarjeta));
        when(cajeroRepo.findById(2L)).thenReturn(Optional.of(cajero));

        IngresoService service = new IngresoService(tarjetaRepo, cajeroRepo, movimientoRepo, cuentaRepo);

        IngresoDTO dto = new IngresoDTO();
        dto.setNumeroTarjeta("1111");
        dto.setCantidad(100.0);
        dto.setIdCajero(2L);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            service.ingresar(dto);
        });

        assertEquals("No se puede ingresar dinero en cajeros de otros bancos", ex.getMessage());
    }

}
