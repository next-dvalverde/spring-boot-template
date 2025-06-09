package es.nextdigital.demo.repository;

import es.nextdigital.demo.entity.Tarjeta;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * The interface Tarjeta repository.
 */
public interface TarjetaRepository extends JpaRepository<Tarjeta, Long> {
}

