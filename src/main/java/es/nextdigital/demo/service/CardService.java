package es.nextdigital.demo.service;

import es.nextdigital.demo.dto.CardDTO;

public interface CardService {
    CardDTO activateCard(Long cardId);
    CardDTO changePin(Long cardId, String newPin);
    CardDTO updateWithdrawLimit(Long cardId, int newLimit);
}