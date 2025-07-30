/**
 * Autor: Álvaro López Arcos
 * Fecha de creación: 30 jul 2025
 * Descripción: TarjetaServiceImplTest.java
 *
 */
package es.nextdigital.demo.domain.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import es.nextdigital.demo.domain.model.Tarjeta;
import es.nextdigital.demo.domain.repository.TarjetaRepository;

public class TarjetaServiceImplTest {

    @Mock
    private TarjetaRepository tarjetaRepository;

    private BCryptPasswordEncoder passwordEncoder;

    private TarjetaServiceImpl tarjetaService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        passwordEncoder = new BCryptPasswordEncoder();
        tarjetaService = new TarjetaServiceImpl(tarjetaRepository, passwordEncoder);
    }

    @Test
    void activarTarjeta_tarjetaInactiva_laActivaYEncriptaPin() {
        Long id = 1L;
        String pin = "1234";
        Tarjeta tarjeta = Tarjeta.builder().id(id).activa(false).build();

        when(tarjetaRepository.findById(id)).thenReturn(Optional.of(tarjeta));

        tarjetaService.activarTarjeta(id, pin);

        assertTrue(tarjeta.isActiva());
        assertNotEquals(pin, tarjeta.getPin());
        verify(tarjetaRepository).save(any(Tarjeta.class));
    }

    @Test
    void activarTarjeta_tarjetaYaActiva_lanzaExcepcion() {
        Long id = 1L;
        Tarjeta tarjeta = Tarjeta.builder().id(id).activa(true).build();

        when(tarjetaRepository.findById(id)).thenReturn(Optional.of(tarjeta));

        assertThrows(IllegalStateException.class, () -> {
            tarjetaService.activarTarjeta(id, "1234");
        });

        verify(tarjetaRepository, never()).save(any());
    }
}
