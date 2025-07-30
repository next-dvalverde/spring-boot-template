/**
 * Autor: Álvaro López Arcos
 * Fecha de creación: 30 jul 2025
 * Descripción: TarjetaRepositoryImpl.java
 *
 */
package es.nextdigital.demo.infra.persistence.repository;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import es.nextdigital.demo.domain.model.Tarjeta;
import es.nextdigital.demo.domain.repository.TarjetaRepository;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class TarjetaRepositoryImpl implements TarjetaRepository {

	private final TarjetaJpaRepository tarjetaJpaRepository;

	@Override
	public Optional<Tarjeta> findByNumero(String numero) {
		return tarjetaJpaRepository.findByNumero(numero);
	}

	@Override
	public Tarjeta save(Tarjeta tarjeta) {
		return tarjetaJpaRepository.save(tarjeta);
	}
	
	@Override
	public Optional<Tarjeta> findById(Long id) {
	    return tarjetaJpaRepository.findById(id);
	}

}