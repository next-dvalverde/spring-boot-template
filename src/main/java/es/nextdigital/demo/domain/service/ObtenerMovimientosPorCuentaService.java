/**
 * Autor: Álvaro López Arcos
 * Fecha de creación: 30 jul 2025
 * Descripción: ObtenerMovimientosPorCuentaService.java
 *
 */
package es.nextdigital.demo.domain.service;

import java.util.List;

import es.nextdigital.demo.presentation.dto.MovimientoDto;

public interface ObtenerMovimientosPorCuentaService {
	List<MovimientoDto> obtenerMovimientos(Long cuentaId);
}
