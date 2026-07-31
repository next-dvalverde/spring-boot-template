package es.nextdigital.atm.domain;

import java.math.BigDecimal;

/**
 * B-2, critico: se invoca en las TRES operaciones de dinero. Un ingreso
 * negativo seria una retirada sin limite ni comision. El @Positive del DTO es
 * la primera capa y el CHECK del schema la ultima; esta es la del dominio.
 */
public final class AmountPolicy {

    private AmountPolicy() {
    }

    public static void requirePositive(BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) {
            throw new BusinessException(ErrorCode.INVALID_AMOUNT, "Amount must be positive");
        }
        // Mas de 2 decimales no es un importe en EUR: rechazar, no redondear en
        // silencio dinero que el cliente no ha pedido mover.
        if (amount.stripTrailingZeros().scale() > 2) {
            throw new BusinessException(ErrorCode.INVALID_AMOUNT, "Amount cannot have more than 2 decimals");
        }
    }
}
