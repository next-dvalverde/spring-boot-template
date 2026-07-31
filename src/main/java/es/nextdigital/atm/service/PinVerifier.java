package es.nextdigital.atm.service;

import es.nextdigital.atm.domain.BusinessException;
import es.nextdigital.atm.domain.Card;
import es.nextdigital.atm.domain.ErrorCode;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Guarda 4 del orden canonico. La comparten login, activacion (B-4: mismo
 * contador, o la activacion seria un oraculo de PIN sin throttling) y cambio
 * de PIN.
 */
@Service
public class PinVerifier {

    private final PasswordEncoder passwordEncoder;
    private final PinFailureRecorder failureRecorder;

    public PinVerifier(PasswordEncoder passwordEncoder, PinFailureRecorder failureRecorder) {
        this.passwordEncoder = passwordEncoder;
        this.failureRecorder = failureRecorder;
    }

    public void verify(Card card, String rawPin) {
        if (passwordEncoder.matches(rawPin, card.getPinHash())) {
            card.resetPinFailures();
            return;
        }
        boolean blockedNow = failureRecorder.recordFailure(card.getId());
        if (blockedNow) {
            // Al tercer fallo la respuesta ya es la de tarjeta bloqueada.
            throw new BusinessException(ErrorCode.CARD_BLOCKED, "Card blocked");
        }
        throw new BusinessException(ErrorCode.INVALID_PIN, "Invalid PIN");
    }
}
