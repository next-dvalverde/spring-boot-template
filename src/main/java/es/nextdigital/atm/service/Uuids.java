package es.nextdigital.atm.service;

import es.nextdigital.atm.domain.BusinessException;
import es.nextdigital.atm.domain.ErrorCode;
import java.util.UUID;

/**
 * Un id malformado responde igual que uno inexistente: distinguirlos no aporta
 * nada al cliente y evita inventar un codigo de error fuera del catalogo.
 */
final class Uuids {

    private Uuids() {
    }

    static UUID parse(String raw, ErrorCode notFound, String message) {
        try {
            return UUID.fromString(raw);
        } catch (IllegalArgumentException | NullPointerException ex) {
            throw new BusinessException(notFound, message);
        }
    }
}
