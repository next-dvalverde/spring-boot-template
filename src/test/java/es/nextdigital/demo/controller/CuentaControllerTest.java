package es.nextdigital.demo.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import es.nextdigital.demo.dto.MovimientoDTO;
import es.nextdigital.demo.dto.OperacionRequestDTO;
import es.nextdigital.demo.dto.TransferenciaRequestDTO;
import es.nextdigital.demo.entity.Banco;
import es.nextdigital.demo.service.CajeroFacadeService;
import es.nextdigital.demo.service.ICuentaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class CuentaControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ICuentaService cuentaService;

    @Mock
    private CajeroFacadeService cajeroFacadeService;

    @InjectMocks
    private CuentaController cuentaController;

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(cuentaController).build();
    }

    @Test
    void getMovimientos_ReturnsList() throws Exception {
        MovimientoDTO mov1 = new MovimientoDTO();
        MovimientoDTO mov2 = new MovimientoDTO();
        List<MovimientoDTO> movimientos = List.of(mov1, mov2);

        when(cuentaService.getMovimientosByCuenta("ES1234")).thenReturn(movimientos);

        mockMvc.perform(get("/v1/api/cuentas/ES1234/movimientos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        verify(cuentaService).getMovimientosByCuenta("ES1234");
    }

    @Test
    void ingresar_ValidRequest_NoContent() throws Exception {
        OperacionRequestDTO request = new OperacionRequestDTO();
        request.setNumeroTarjeta("1234");
        request.setPinHash("pin");
        request.setCantidad(new BigDecimal("100"));
        request.setBancoCajero(new Banco());

        doNothing().when(cajeroFacadeService).ingresarDinero(
                anyString(), anyString(), any(BigDecimal.class), any(Banco.class));

        mockMvc.perform(post("/v1/api/cuentas/ingresar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());

        verify(cajeroFacadeService).ingresarDinero("1234", "pin", new BigDecimal("100"), request.getBancoCajero());
    }

    @Test
    void retirar_ValidRequest_NoContent() throws Exception {
        OperacionRequestDTO request = new OperacionRequestDTO();
        request.setNumeroTarjeta("1234");
        request.setPinHash("pin");
        request.setCantidad(new BigDecimal("50"));
        request.setBancoCajero(new Banco());

        doNothing().when(cajeroFacadeService).retirarDinero(
                anyString(), anyString(), any(BigDecimal.class), any(Banco.class));

        mockMvc.perform(post("/v1/api/cuentas/retirar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());

        verify(cajeroFacadeService).retirarDinero("1234", "pin", new BigDecimal("50"), request.getBancoCajero());
    }

    @Test
    void transferir_ValidRequest_NoContent() throws Exception {
        TransferenciaRequestDTO request = new TransferenciaRequestDTO();
        request.setNumeroTarjeta("1234");
        request.setPinHash("pin");
        request.setIbanDestino("ES9999");
        request.setCantidad(new BigDecimal("75"));

        doNothing().when(cajeroFacadeService).transferir(
                anyString(), anyString(), anyString(), any(BigDecimal.class));

        mockMvc.perform(post("/v1/api/cuentas/transferir")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());

        verify(cajeroFacadeService).transferir("1234", "pin", "ES9999", new BigDecimal("75"));
    }
}
