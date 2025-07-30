/**
 * Autor: Álvaro López Arcos
 * Fecha de creación: 30 jul 2025
 * Descripción: ObtenerMovimientosPorCuentaServiceImplTest.java
 *
 */
package es.nextdigital.demo.domain.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import es.nextdigital.demo.domain.model.Movimiento;
import es.nextdigital.demo.domain.model.enums.MovimientoTipo;
import es.nextdigital.demo.domain.repository.MovimientoRepository;
import es.nextdigital.demo.infra.mapper.MovimientoMapper;
import es.nextdigital.demo.presentation.dto.MovimientoDto;

public class ObtenerMovimientosPorCuentaServiceImplTest {

    @Mock
    private MovimientoRepository movimientoRepository;

    @Mock
    private MovimientoMapper movimientoMapper;

    @InjectMocks
    private ObtenerMovimientosPorCuentaServiceImpl service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void obtenerMovimientos_devuelveMovimientosParaCuentaExistente() {
        // Arrange
        Long cuentaId = 1L;

        Movimiento movimiento = Movimiento.builder()
                .id(1L)
                .cantidad(100.0)
                .fecha(LocalDateTime.now())
                .tipo(MovimientoTipo.INGRESO)
                .build();

        MovimientoDto movimientoDto = MovimientoDto.builder()
                .id(1L)
                .cantidad(100.0)
                .fecha(movimiento.getFecha())
                .tipo(MovimientoTipo.INGRESO)
                .build();

        when(movimientoRepository.findByCuentaId(cuentaId)).thenReturn(List.of(movimiento));
        when(movimientoMapper.toDtoList(List.of(movimiento))).thenReturn(List.of(movimientoDto));

        // Act
        List<MovimientoDto> resultado = service.obtenerMovimientos(cuentaId);

        // Assert
        assertEquals(1, resultado.size());
        assertEquals(100.0, resultado.get(0).getCantidad());
        verify(movimientoRepository).findByCuentaId(cuentaId);
        verify(movimientoMapper).toDtoList(List.of(movimiento));
    }
}