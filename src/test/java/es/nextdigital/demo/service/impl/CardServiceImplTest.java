package es.nextdigital.demo.service.impl;

import es.nextdigital.demo.model.Card;
import es.nextdigital.demo.model.CardStatus;
import es.nextdigital.demo.repository.CardRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CardServiceImplTest {

    private CardRepository cardRepository;
    private CardServiceImpl cardService;

    @BeforeEach
    void setup() {
        cardRepository = Mockito.mock(CardRepository.class);
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        cardService = new CardServiceImpl(cardRepository, passwordEncoder);
    }

    @Test
    void testActivateCard() {
        Card card = new Card();
        card.setId(1L);
        card.setStatus(CardStatus.INACTIVE);

        when(cardRepository.findById(1L)).thenReturn(java.util.Optional.of(card));
        when(cardRepository.save(any(Card.class))).thenAnswer(i -> i.getArguments()[0]);

        cardService.activateCard(1L);

        assertEquals(CardStatus.ACTIVE, card.getStatus());
        verify(cardRepository, times(1)).save(card);
    }

    @Test
    void testChangePin() {
        Card card = new Card();
        card.setId(1L);
        card.setPinHash("old");

        when(cardRepository.findById(1L)).thenReturn(java.util.Optional.of(card));
        when(cardRepository.save(any(Card.class))).thenAnswer(i -> i.getArguments()[0]);

        String newPin = "1234";
        cardService.changePin(1L, newPin);

        assertTrue(new BCryptPasswordEncoder().matches(newPin, card.getPinHash()));
        verify(cardRepository, times(1)).save(card);
    }

    @Test
    void testUpdateWithdrawLimitWithinRange() {
        Card card = new Card();
        card.setId(1L);
        card.setWithdrawLimit(java.math.BigDecimal.valueOf(1000));

        when(cardRepository.findById(1L)).thenReturn(java.util.Optional.of(card));
        when(cardRepository.save(any(Card.class))).thenAnswer(i -> i.getArguments()[0]);

        cardService.updateWithdrawLimit(1L, 2000);

        assertEquals(java.math.BigDecimal.valueOf(2000), card.getWithdrawLimit());
    }

    @Test
    void testUpdateWithdrawLimitOutOfRange() {
        Card card = new Card();
        card.setId(1L);

        when(cardRepository.findById(1L)).thenReturn(java.util.Optional.of(card));

        Exception exception = assertThrows(RuntimeException.class,
                () -> cardService.updateWithdrawLimit(1L, 7000));
        assertEquals("Limit out of range", exception.getMessage());
    }
}