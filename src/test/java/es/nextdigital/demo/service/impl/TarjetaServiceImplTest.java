package es.nextdigital.demo.service.impl;

import es.nextdigital.demo.entity.Tarjeta;
import es.nextdigital.demo.repository.TarjetaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * The type Tarjeta service impl test.
 */
class TarjetaServiceImplTest {

    @InjectMocks
    private TarjetaServiceImpl tarjetaService;

    @Mock
    private TarjetaRepository tarjetaRepo;

    private Tarjeta tarjeta;

    /**
     * Sets up.
     */
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        tarjeta = new Tarjeta();
        tarjeta.setId(1L);
        tarjeta.setActiva(false);
        tarjeta.setPin("0000");
    }

    /**
     * Activar tarjeta when tarjeta exists should activate and set pin.
     */
    @Test
    void activarTarjeta_whenTarjetaExists_shouldActivateAndSetPin() {
        when(tarjetaRepo.findById(1L)).thenReturn(Optional.of(tarjeta));
        when(tarjetaRepo.save(any(Tarjeta.class))).thenAnswer(invocation -> invocation.getArgument(0));

        tarjetaService.activarTarjeta(1L, "1234");

        assertTrue(tarjeta.isActiva());
        assertEquals("1234", tarjeta.getPin());

        verify(tarjetaRepo).findById(1L);
        verify(tarjetaRepo).save(tarjeta);
    }

    /**
     * Activar tarjeta when tarjeta not found should throw.
     */
    @Test
    void activarTarjeta_whenTarjetaNotFound_shouldThrow() {
        when(tarjetaRepo.findById(1L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> tarjetaService.activarTarjeta(1L, "1234"));
        assertEquals("Tarjeta no encontrada", ex.getMessage());

        verify(tarjetaRepo).findById(1L);
        verify(tarjetaRepo, never()).save(any());
    }

    /**
     * Cambiar pin when tarjeta exists should update pin.
     */
    @Test
    void cambiarPin_whenTarjetaExists_shouldUpdatePin() {
        when(tarjetaRepo.findById(1L)).thenReturn(Optional.of(tarjeta));
        when(tarjetaRepo.save(any(Tarjeta.class))).thenAnswer(invocation -> invocation.getArgument(0));

        tarjetaService.cambiarPin(1L, "5678");

        assertEquals("5678", tarjeta.getPin());

        verify(tarjetaRepo).findById(1L);
        verify(tarjetaRepo).save(tarjeta);
    }

    /**
     * Cambiar pin when tarjeta not found should throw.
     */
    @Test
    void cambiarPin_whenTarjetaNotFound_shouldThrow() {
        when(tarjetaRepo.findById(1L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> tarjetaService.cambiarPin(1L, "5678"));
        assertEquals("Tarjeta no encontrada", ex.getMessage());

        verify(tarjetaRepo).findById(1L);
        verify(tarjetaRepo, never()).save(any());
    }
}