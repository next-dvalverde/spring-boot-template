package es.nextdigital.atm.service;

import es.nextdigital.atm.domain.BusinessException;
import es.nextdigital.atm.domain.Card;
import es.nextdigital.atm.domain.CardStatus;
import es.nextdigital.atm.domain.ErrorCode;
import es.nextdigital.atm.domain.PinPolicy;
import es.nextdigital.atm.repository.AtmRepository;
import es.nextdigital.atm.repository.CardRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CardService {

    private final CardRepository cardRepository;
    private final AtmRepository atmRepository;
    private final PinVerifier pinVerifier;
    private final PasswordEncoder passwordEncoder;

    public CardService(CardRepository cardRepository, AtmRepository atmRepository,
                       PinVerifier pinVerifier, PasswordEncoder passwordEncoder) {
        this.cardRepository = cardRepository;
        this.atmRepository = atmRepository;
        this.pinVerifier = pinVerifier;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Unica excepcion al orden de guardas: invierte la 2 (exige INACTIVE) pero
     * ejecuta la 4 con el mismo contador (B-4) — es el unico endpoint sin token
     * y no puede ser un oraculo de PIN gratuito.
     */
    @Transactional
    public void activate(String cardNumber, String pin, String atmId) {
        atmRepository.findById(Uuids.parse(atmId, ErrorCode.ATM_NOT_FOUND, "ATM not found"))
                .orElseThrow(() -> new BusinessException(ErrorCode.ATM_NOT_FOUND, "ATM not found"));
        Card card = cardRepository.findByCardNumber(cardNumber)
                .orElseThrow(() -> new BusinessException(ErrorCode.CARD_NOT_FOUND, "Card not found"));
        if (card.getStatus() == CardStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.CARD_ALREADY_ACTIVE, "Card is already active");
        }
        if (card.getStatus() == CardStatus.BLOCKED) {
            throw new BusinessException(ErrorCode.CARD_BLOCKED, "Card blocked");
        }
        pinVerifier.verify(card, pin);
        card.activate();
    }

    // Unico endpoint que pasa con pinChangeRequired = true (guarda 3).
    @Transactional
    public void changePin(SessionContext session, String currentPin, String newPin) {
        Card card = cardRepository.findById(session.cardId())
                .orElseThrow(() -> new BusinessException(ErrorCode.CARD_NOT_FOUND, "Card not found"));
        CardGuards.requireActive(card);
        pinVerifier.verify(card, currentPin);
        PinPolicy.validateNewPin(newPin, currentPin);
        card.changePin(passwordEncoder.encode(newPin));
    }
}
