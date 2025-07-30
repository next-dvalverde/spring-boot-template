/**
 * Autor: Álvaro López Arcos
 * Fecha de creación: 30 jul 2025
 * Descripción: TarjetaRepository.java
 *
 */
package es.nextdigital.demo.domain.repository;

import java.util.Optional;

import es.nextdigital.demo.domain.model.Tarjeta;

public interface TarjetaRepository {
	Optional<Tarjeta> findByNumero(String numero);
	Optional<Tarjeta> findById(Long id);

	Tarjeta save(Tarjeta tarjeta);
}
