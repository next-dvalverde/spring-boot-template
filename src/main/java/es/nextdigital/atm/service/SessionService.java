package es.nextdigital.atm.service;

import es.nextdigital.atm.domain.BusinessException;
import es.nextdigital.atm.domain.Atm;
import es.nextdigital.atm.domain.Card;
import es.nextdigital.atm.domain.ErrorCode;
import es.nextdigital.atm.repository.AtmRepository;
import es.nextdigital.atm.repository.CardRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SessionService {

    public record OpenedSession(String token, boolean pinChangeRequired) {
    }

    private final CardRepository cardRepository;
    private final AtmRepository atmRepository;
    private final PinVerifier pinVerifier;
    private final SessionStore sessionStore;

    public SessionService(CardRepository cardRepository, AtmRepository atmRepository,
                          PinVerifier pinVerifier, SessionStore sessionStore) {
        this.cardRepository = cardRepository;
        this.atmRepository = atmRepository;
        this.pinVerifier = pinVerifier;
        this.sessionStore = sessionStore;
    }

    // No aplica la guarda 3: con pinChangeRequired = true hay que poder abrir
    // sesion para llegar a PUT /cards/pin. El flag viaja en la respuesta para
    // que el cajero redirija al cambio de PIN.
    @Transactional
    public OpenedSession open(String cardNumber, String pin, String atmId) {
        Atm atm = atmRepository.findById(Uuids.parse(atmId, ErrorCode.ATM_NOT_FOUND, "ATM not found"))
                .orElseThrow(() -> new BusinessException(ErrorCode.ATM_NOT_FOUND, "ATM not found"));
        Card card = cardRepository.findByCardNumber(cardNumber)
                .orElseThrow(() -> new BusinessException(ErrorCode.CARD_NOT_FOUND, "Card not found"));
        CardGuards.requireActive(card);
        pinVerifier.verify(card, pin);
        String token = sessionStore.issue(new SessionContext(card.getId(), card.getAccountId(), atm.getId()));
        return new OpenedSession(token, card.isPinChangeRequired());
    }
}
