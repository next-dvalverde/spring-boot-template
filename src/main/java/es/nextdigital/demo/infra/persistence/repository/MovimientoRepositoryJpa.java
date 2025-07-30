/**
 * Autor: Álvaro López Arcos
 * Fecha de creación: 30 jul 2025
 * Descripción: MovimientoRepositoryJpa.java
 *
 */
package es.nextdigital.demo.infra.persistence.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import es.nextdigital.demo.domain.model.Movimiento;

public interface MovimientoRepositoryJpa extends JpaRepository<Movimiento, Long> {
    List<Movimiento> findByCuentaId(Long cuentaId);
}