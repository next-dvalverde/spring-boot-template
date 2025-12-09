package es.nextdigital.demo.services;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import es.nextdigital.demo.model.Card;
import es.nextdigital.demo.repository.CardRepository;

@Service
public class CardService {
    @Autowired
    private CardRepository cardRepo;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public Card activateCard(String cardNumber, String pin) {
        Card card = cardRepo.findByCardNumber(cardNumber)
                .orElseThrow(() -> new RuntimeException("Card missing. Please use valid one"));

        if (card.isActive())
            throw new RuntimeException("Card enable already. You can use it");

        card.setPinHash(encoder.encode(pin));
        card.setActive(true);
        return cardRepo.save(card);
    }

    public void changePin(String cardNumber, String newPin) {
        Card card = cardRepo.findByCardNumber(cardNumber)
                .orElseThrow(() -> new RuntimeException("Card missing. Please use valid one"));

        if (!card.isActive())
            throw new RuntimeException("Card disabled. You need enable before use it");

        card.setPinHash(encoder.encode(newPin));
        cardRepo.save(card);
    }

    public Card validateCard(String cardNumber, String pin) {
        Card card = cardRepo.findByCardNumber(cardNumber)
                .orElseThrow(() -> new RuntimeException("Card missing. Please use valid one"));

        if (!card.isActive())
            throw new RuntimeException("Card disabled. You need enable before use it");

        if (!encoder.matches(pin, card.getPinHash()))
            throw new RuntimeException("PIN incorrecto");

        return card;
    }

    public void updateWithdrawLimit(String cardNumber, BigDecimal newLimit) {
        if (newLimit.compareTo(BigDecimal.valueOf(500)) < 0 ||
                newLimit.compareTo(BigDecimal.valueOf(6000)) > 0)
            throw new RuntimeException("Límite fuera de rango");

        Card card = cardRepo.findByCardNumber(cardNumber)
                .orElseThrow(() -> new RuntimeException("Card missing. Please use valid one"));

        card.setWithdrawLimit(newLimit);
        cardRepo.save(card);
    }
}
