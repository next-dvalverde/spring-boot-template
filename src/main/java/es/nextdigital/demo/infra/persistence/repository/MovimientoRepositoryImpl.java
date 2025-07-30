/**
 * Autor: Álvaro López Arcos
 * Fecha de creación: 30 jul 2025
 * Descripción: MovimientoRepositoryImpl.java
 *
 */
package es.nextdigital.demo.infra.persistence.repository;

import java.util.List;

import org.springframework.stereotype.Repository;

import es.nextdigital.demo.domain.model.Movimiento;
import es.nextdigital.demo.domain.repository.MovimientoRepository;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class MovimientoRepositoryImpl implements MovimientoRepository {

	private final MovimientoRepositoryJpa movimientoRepositoryJpa;

	@Override
    public List<Movimiento> findByCuentaId(Long cuentaId) {
        return movimientoRepositoryJpa.findByCuentaId(cuentaId);
    }
}