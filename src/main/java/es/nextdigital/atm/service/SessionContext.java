package es.nextdigital.atm.service;

import java.util.UUID;

/**
 * Lo que el token opaco representa: tarjeta + cuenta + cajero de la sesion.
 * El cliente nunca envia accountId; la cuenta sale siempre de aqui — operar
 * sobre una cuenta ajena es imposible por construccion, no por comprobacion.
 */
public record SessionContext(UUID cardId, UUID accountId, UUID atmId) {
}
