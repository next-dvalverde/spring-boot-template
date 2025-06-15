package es.nextdigital.demo.service;

import es.nextdigital.demo.adapter.in.rest.dto.CambiarPinDTO;
import es.nextdigital.demo.adapter.out.repositories.*;
import es.nextdigital.demo.application.models.*;
import es.nextdigital.demo.application.service.TarjetaService;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TarjetaServiceTest {

    @Test
    void testCambioPinConPinActualIncorrecto() {
        TarjetaRepository tarjetaRepo = mock(TarjetaRepository.class);

        Tarjeta tarjeta = new Tarjeta();
        tarjeta.setNumero("1111");
        // Simula un PIN cifrado para "1234"
        String pinCifrado = org.springframework.security.crypto.bcrypt.BCrypt.hashpw("1234", org.springframework.security.crypto.bcrypt.BCrypt.gensalt());
        tarjeta.setPin(pinCifrado);

        when(tarjetaRepo.findByNumero("1111")).thenReturn(Optional.of(tarjeta));

        TarjetaService service = new TarjetaService(tarjetaRepo);

        CambiarPinDTO dto = new CambiarPinDTO();
        dto.setNumeroTarjeta("1111");
        dto.setPinActual("0000"); // Incorrecto
        dto.setPinNuevo("5678");

        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            service.cambiarPin(dto);
        });

        assertEquals("El PIN actual no es correcto", ex.getMessage());
    }


}
