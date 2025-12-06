package es.nextdigital.demo.controller;

import es.nextdigital.demo.model.*;
import es.nextdigital.demo.repository.CuentaRepository;
import es.nextdigital.demo.repository.TarjetaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class CajeroControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CuentaRepository cuentaRepository;

    @Autowired
    private TarjetaRepository tarjetaRepository;

    @BeforeEach
    void setup() {
        Cuenta c = new Cuenta(1L, "ES123", 1000.0);
        cuentaRepository.save(c);

        Tarjeta t = new Tarjeta(1L, "1111", TipoTarjeta.DEBITO, 500.0, 0.0, 1L);
        t.setActivada(true);
        t.setPinPendienteCambio(false);
        tarjetaRepository.save(t);
    }

    @Test
    void retirarEndpoint_DebeDevolver200() throws Exception {
        mockMvc.perform(post("/cajero/retirar")
                        .param("tarjetaId", "1")
                        .param("cantidad", "100"))
                .andExpect(status().isOk());
    }
}
