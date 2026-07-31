package es.nextdigital.atm.service;

import es.nextdigital.atm.domain.BusinessException;
import es.nextdigital.atm.domain.Card;
import es.nextdigital.atm.domain.ErrorCode;

/**
 * Guardas 2 y 3 del orden canonico (CLAUDE.md). Dos codigos distintos para
 * INACTIVE y BLOCKED: decir "no activada" a una tarjeta bloqueada invita a
 * intentar activarla.
 */
public final class CardGuards {

    private CardGuards() {
    }

    public static void requireActive(Card card) {
        switch (card.getStatus()) {
            case INACTIVE -> throw new BusinessException(ErrorCode.CARD_NOT_ACTIVATED, "Card not activated");
            case BLOCKED -> throw new BusinessException(ErrorCode.CARD_BLOCKED, "Card blocked");
            case ACTIVE -> {
            }
        }
    }

    // Para toda operacion salvo PUT /cards/pin, que es la unica que pasa con
    // pinChangeRequired = true.
    public static void requireOperational(Card card) {
        requireActive(card);
        if (card.isPinChangeRequired()) {
            throw new BusinessException(ErrorCode.PIN_CHANGE_REQUIRED, "PIN change required");
        }
    }
}
