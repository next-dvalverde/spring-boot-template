package es.nextdigital.demo.mapper;

import es.nextdigital.demo.dto.CardDTO;
import es.nextdigital.demo.model.Card;

public class CardMapper {

    public static CardDTO toDTO(Card card) {
        CardDTO dto = new CardDTO();
        dto.setId(card.getId());
        dto.setCardNumber(card.getCardNumber());
        dto.setStatus(card.getStatus());
        dto.setWithdrawLimit(card.getWithdrawLimit());
        return dto;
    }
}