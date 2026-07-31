package es.nextdigital.atm.service;

import java.util.Optional;

/**
 * Frontera de C-1: el resto del sistema solo conoce esta interfaz. Migrar a
 * Redis es una implementacion nueva y un bean, cero cambios en servicios.
 */
public interface SessionStore {

    String issue(SessionContext context);

    Optional<SessionContext> resolve(String token);
}
