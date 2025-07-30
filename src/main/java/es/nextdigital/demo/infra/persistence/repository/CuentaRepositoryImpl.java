/**
 * Autor: Álvaro López Arcos
 * Fecha de creación: 30 jul 2025
 * Descripción: CuentaRepositoryImpl.java
 *
 */
package es.nextdigital.demo.infra.persistence.repository;

import org.springframework.stereotype.Repository;

import es.nextdigital.demo.domain.model.Cuenta;
import es.nextdigital.demo.domain.repository.CuentaRepository;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class CuentaRepositoryImpl implements CuentaRepository {

    private final CuentaJpaRepository jpaRepository;

    @Override
    public Cuenta save(Cuenta cuenta) {
        return jpaRepository.save(cuenta);
    }
}