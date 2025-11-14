package es.nextdigital.demo.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import lombok.RequiredArgsConstructor;

import es.nextdigital.demo.repository.CardRepository;
import es.nextdigital.demo.model.Card;
import es.nextdigital.demo.model.CardStatus;
import es.nextdigital.demo.dto.CardDTO;
import es.nextdigital.demo.mapper.CardMapper;
import es.nextdigital.demo.service.CardService;

@Service
@RequiredArgsConstructor
public class CardServiceImpl implements CardService {

    private final CardRepository cardRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Override
    public CardDTO activateCard(Long cardId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new RuntimeException("Card not found"));
        card.setStatus(CardStatus.ACTIVE);
        cardRepository.save(card);
        return CardMapper.toDTO(card);
    }

    @Override
    public CardDTO changePin(Long cardId, String newPin) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new RuntimeException("Card not found"));
        card.setPinHash(passwordEncoder.encode(newPin));
        cardRepository.save(card);
        return CardMapper.toDTO(card);
    }

    @Override
    public CardDTO updateWithdrawLimit(Long cardId, int newLimit) {
        if (newLimit < 500 || newLimit > 6000) throw new RuntimeException("Limit out of range");
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new RuntimeException("Card not found"));
        card.setWithdrawLimit(java.math.BigDecimal.valueOf(newLimit));
        cardRepository.save(card);
        return CardMapper.toDTO(card);
    }
}