package es.nextdigital.atm.domain;

import java.math.BigDecimal;

/**
 * Value object que el servicio construye desde la entidad FeePolicy (A-2).
 * Nombre distinto a proposito: FeePolicy es la fila, FeeRule es el valor que
 * cruza la frontera del dominio. percentage va en puntos porcentuales
 * (1.0000 = 1 %).
 */
public record FeeRule(BigDecimal fixedAmount, BigDecimal percentage) {
}
