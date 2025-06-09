package es.nextdigital.demo.api.v1.controller;

import es.nextdigital.demo.dto.ActivacionTarjetaDTO;
import es.nextdigital.demo.dto.CambioPinDTO;
import es.nextdigital.demo.service.TarjetaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;

/**
 * The type Tarjeta controller test.
 */
class TarjetaControllerTest {

    @Mock
    private TarjetaService tarjetaService;

    @InjectMocks
    private TarjetaController tarjetaController;

    /**
     * Sets up.
     */
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    /**
     * Activar calls service and returns ok.
     */
    @Test
    void activar_callsServiceAndReturnsOk() {
        Long tarjetaId = 1L;
        ActivacionTarjetaDTO dto = new ActivacionTarjetaDTO();
        dto.nuevoPin = "1234";

        doNothing().when(tarjetaService).activarTarjeta(tarjetaId, dto.nuevoPin);

        ResponseEntity<Void> response = tarjetaController.activar(tarjetaId, dto);

        verify(tarjetaService).activarTarjeta(tarjetaId, dto.nuevoPin);
        assertEquals(200, response.getStatusCodeValue());
        assertNull(response.getBody());
    }

    /**
     * Cambiar pin calls service and returns ok.
     */
    @Test
    void cambiarPin_callsServiceAndReturnsOk() {
        Long tarjetaId = 1L;
        CambioPinDTO dto = new CambioPinDTO();
        dto.nuevoPin = "5678";

        doNothing().when(tarjetaService).cambiarPin(tarjetaId, dto.nuevoPin);

        ResponseEntity<Void> response = tarjetaController.cambiarPin(tarjetaId, dto);

        verify(tarjetaService).cambiarPin(tarjetaId, dto.nuevoPin);
        assertEquals(200, response.getStatusCodeValue());
        assertNull(response.getBody());
    }
}
