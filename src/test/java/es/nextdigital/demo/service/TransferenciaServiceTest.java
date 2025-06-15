package es.nextdigital.demo.service;

import es.nextdigital.demo.adapter.in.rest.dto.TransferenciaDTO;
import es.nextdigital.demo.adapter.out.repositories.*;
import es.nextdigital.demo.application.models.*;
import es.nextdigital.demo.application.service.TransferenciaService;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TransferenciaServiceTest {

    @Test
    void testTransferenciaConIBANNoValido() {
        TarjetaRepository tarjetaRepo = mock(TarjetaRepository.class);
        CuentaRepository cuentaRepo = mock(CuentaRepository.class);
        MovimientoRepository movimientoRepo = mock(MovimientoRepository.class);
        BancoRepository bancoRepo = mock(BancoRepository.class);

        Tarjeta tarjeta = new Tarjeta();
        tarjeta.setNumero("1111");
        tarjeta.setActiva(true);
        Cuenta cuenta = new Cuenta();
        cuenta.setBanco(new Banco());
        cuenta.setSaldo(1000.0);
        tarjeta.setCuentaAsociada(cuenta);

        when(tarjetaRepo.findByNumero("1111")).thenReturn(Optional.of(tarjeta));
        when(cuentaRepo.findByIban("MALFORMADO")).thenReturn(Optional.of(new Cuenta()));

        TransferenciaService service = new TransferenciaService(tarjetaRepo, cuentaRepo, movimientoRepo, bancoRepo);

        TransferenciaDTO dto = new TransferenciaDTO();
        dto.setNumeroTarjeta("1111");
        dto.setIbanDestino("MALFORMADO");
        dto.setCantidad(50.0);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            service.transferir(dto);
        });

        assertEquals("El IBAN de destino no es válido", ex.getMessage());
    }

}
