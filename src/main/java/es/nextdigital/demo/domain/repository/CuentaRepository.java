/**
 * Autor: Álvaro López Arcos
 * Fecha de creación: 30 jul 2025
 * Descripción: CuentaRepository.java
 *
 */
package es.nextdigital.demo.domain.repository;

import es.nextdigital.demo.domain.model.Cuenta;

public interface CuentaRepository {
	Cuenta save(Cuenta cuenta);
}
