package es.nextdigital.demo.api.v1.controller;

import es.nextdigital.demo.dto.MovimientoDTO;
import es.nextdigital.demo.dto.OperacionIngresoDTO;
import es.nextdigital.demo.dto.OperacionRetiroDTO;
import es.nextdigital.demo.dto.TransferenciaDTO;
import es.nextdigital.demo.service.OperacionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * The type Operacion controller test.
 */
class OperacionControllerTest {

    @Mock
    private OperacionService operacionService;

    @InjectMocks
    private OperacionController operacionController;

    /**
     * Sets up.
     */
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    /**
     * Movimientos delegates to service and returns list.
     */
    @Test
    void movimientos_delegatesToServiceAndReturnsList() {
        Long cuentaId = 1L;
        List<MovimientoDTO> movimientos = List.of(new MovimientoDTO());
        when(operacionService.consultarMovimientos(cuentaId)).thenReturn(movimientos);

        List<MovimientoDTO> result = operacionController.movimientos(cuentaId);

        verify(operacionService).consultarMovimientos(cuentaId);
        assertEquals(movimientos, result);
    }

    /**
     * Retirar calls service and returns ok.
     */
    @Test
    void retirar_callsServiceAndReturnsOk() {
        OperacionRetiroDTO dto = new OperacionRetiroDTO();
        doNothing().when(operacionService).retirar(dto);

        ResponseEntity<Void> response = operacionController.retirar(dto);

        verify(operacionService).retirar(dto);
        assertEquals(200, response.getStatusCodeValue());
        assertNull(response.getBody());
    }

    /**
     * Ingresar calls service and returns ok.
     */
    @Test
    void ingresar_callsServiceAndReturnsOk() {
        OperacionIngresoDTO dto = new OperacionIngresoDTO();
        doNothing().when(operacionService).ingresar(dto);

        ResponseEntity<Void> response = operacionController.ingresar(dto);

        verify(operacionService).ingresar(dto);
        assertEquals(200, response.getStatusCodeValue());
        assertNull(response.getBody());
    }

    /**
     * Transferir calls service and returns ok.
     */
    @Test
    void transferir_callsServiceAndReturnsOk() {
        TransferenciaDTO dto = new TransferenciaDTO();
        doNothing().when(operacionService).transferir(dto);

        ResponseEntity<Void> response = operacionController.transferir(dto);

        verify(operacionService).transferir(dto);
        assertEquals(200, response.getStatusCodeValue());
        assertNull(response.getBody());
    }
}

