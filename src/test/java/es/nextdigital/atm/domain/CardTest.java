package es.nextdigital.atm.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class CardTest {

    private Card activeDebitCard() {
        return new Card(UUID.randomUUID(), "4001000000000001", UUID.randomUUID(),
                CardType.DEBIT, CardStatus.ACTIVE, "$2a$10$hash", false,
                new BigDecimal("500.00"), null, BigDecimal.ZERO);
    }

    @Test
    void twoFailuresDoNotBlock() {
        Card card = activeDebitCard();
        assertFalse(card.recordPinFailure());
        assertFalse(card.recordPinFailure());
        assertEquals(CardStatus.ACTIVE, card.getStatus());
        assertEquals(2, card.getFailedAttempts());
    }

    @Test
    void thirdFailureBlocksTheCard() {
        Card card = activeDebitCard();
        card.recordPinFailure();
        card.recordPinFailure();
        assertTrue(card.recordPinFailure());
        assertEquals(CardStatus.BLOCKED, card.getStatus());
    }

    @Test
    void activationForcesPinChange() {
        Card card = new Card(UUID.randomUUID(), "4001000000000003", UUID.randomUUID(),
                CardType.DEBIT, CardStatus.INACTIVE, "$2a$10$hash", false,
                new BigDecimal("500.00"), null, BigDecimal.ZERO);
        card.activate();
        assertEquals(CardStatus.ACTIVE, card.getStatus());
        assertTrue(card.isPinChangeRequired());
    }

    @Test
    void changingThePinClearsTheRequirement() {
        Card card = activeDebitCard();
        card.activate();
        card.changePin("$2a$10$newhash");
        assertEquals("$2a$10$newhash", card.getPinHash());
        assertFalse(card.isPinChangeRequired());
    }

    // B-1: sin el reset, 3 fallos repartidos en meses bloquean la tarjeta.
    @Test
    void successResetsTheCounter() {
        Card card = activeDebitCard();
        card.recordPinFailure();
        card.recordPinFailure();
        card.resetPinFailures();
        assertEquals(0, card.getFailedAttempts());
        assertFalse(card.recordPinFailure());
        assertEquals(CardStatus.ACTIVE, card.getStatus());
    }
}
