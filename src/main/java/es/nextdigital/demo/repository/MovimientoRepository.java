package es.nextdigital.demo.repository;

import es.nextdigital.demo.entity.Movimiento;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * The interface Movimiento repository.
 */
public interface MovimientoRepository extends JpaRepository<Movimiento, Long> {
}
