/**
 * Autor: Álvaro López Arcos
 * Fecha de creación: 30 jul 2025
 * Descripción: MovimientoRepository.java
 *
 */
package es.nextdigital.demo.domain.repository;

import java.util.List;

import es.nextdigital.demo.domain.model.Movimiento;

public interface MovimientoRepository {
    List<Movimiento> findByCuentaId(Long cuentaId);
}