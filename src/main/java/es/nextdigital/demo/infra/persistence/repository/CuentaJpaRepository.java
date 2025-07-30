/**
 * Autor: Álvaro López Arcos
 * Fecha de creación: 30 jul 2025
 * Descripción: CuentaJpaRepository.java
 *
 */
package es.nextdigital.demo.infra.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import es.nextdigital.demo.domain.model.Cuenta;

public interface CuentaJpaRepository extends JpaRepository<Cuenta, Long> {
}
