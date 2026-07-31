package es.nextdigital.atm.service;

import es.nextdigital.atm.domain.BusinessException;
import es.nextdigital.atm.domain.Card;
import es.nextdigital.atm.domain.ErrorCode;
import es.nextdigital.atm.repository.CardRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * REQUIRES_NEW a proposito: la operacion que fallo el PIN termina en excepcion
 * y su transaccion se revierte — el incremento del contador tiene que
 * sobrevivir a ese rollback o el bloqueo a los 3 intentos seria decorativo.
 */
@Service
public class PinFailureRecorder {

    private final CardRepository cardRepository;

    public PinFailureRecorder(CardRepository cardRepository) {
        this.cardRepository = cardRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean recordFailure(UUID cardId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CARD_NOT_FOUND, "Card not found"));
        return card.recordPinFailure();
    }
}
