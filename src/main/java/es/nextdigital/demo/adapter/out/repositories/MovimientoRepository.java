package es.nextdigital.demo.adapter.out.repositories;

import es.nextdigital.demo.application.models.Movimiento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MovimientoRepository extends JpaRepository<Movimiento, Long> {
    List<Movimiento> findByCuentaId(Long cuentaId);
}
