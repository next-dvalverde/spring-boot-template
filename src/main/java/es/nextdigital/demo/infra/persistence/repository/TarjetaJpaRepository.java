/**
 * Autor: Álvaro López Arcos
 * Fecha de creación: 30 jul 2025
 * Descripción: TarjetaJpaRepository.java
 *
 */
package es.nextdigital.demo.infra.persistence.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import es.nextdigital.demo.domain.model.Tarjeta;

public interface TarjetaJpaRepository extends JpaRepository<Tarjeta, Long> {
    Optional<Tarjeta> findByNumero(String numero);
}