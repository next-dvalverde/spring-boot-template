package es.nextdigital.demo.service.impl;

import es.nextdigital.demo.model.Account;
import es.nextdigital.demo.model.Card;
import es.nextdigital.demo.model.CardStatus;
import es.nextdigital.demo.model.Transaction;
import es.nextdigital.demo.repository.AccountRepository;
import es.nextdigital.demo.repository.CardRepository;
import es.nextdigital.demo.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AccountServiceImplTest {

    private AccountRepository accountRepository;
    private CardRepository cardRepository;
    private TransactionRepository transactionRepository;
    private AccountServiceImpl accountService;

    @BeforeEach
    void setup() {
        accountRepository = mock(AccountRepository.class);
        cardRepository = mock(CardRepository.class);
        transactionRepository = mock(TransactionRepository.class);
        accountService = new AccountServiceImpl(accountRepository, transactionRepository, cardRepository);
    }

    @Test
    void testWithdrawSuccess() {
        Account account = new Account();
        account.setId(1L);
        account.setBalance(BigDecimal.valueOf(1000));

        Card card = new Card();
        card.setId(1L);
        card.setStatus(CardStatus.ACTIVE);
        card.setWithdrawLimit(BigDecimal.valueOf(500));

        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));

        Exception e = assertThrows(RuntimeException.class, () ->
                accountService.withdraw(1L, BigDecimal.valueOf(600), true, 1L));
        assertEquals("Amount exceeds card withdraw limit", e.getMessage());

        accountService.withdraw(1L, BigDecimal.valueOf(400), true, 1L);
        assertEquals(BigDecimal.valueOf(600), account.getBalance());
    }

    @Test
    void testDepositFromOtherBankFails() {
        Account account = new Account();
        account.setId(1L);
        account.setBalance(BigDecimal.valueOf(1000));

        Card card = new Card();
        card.setId(1L);
        card.setStatus(CardStatus.ACTIVE);

        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));

        Exception e = assertThrows(RuntimeException.class, () ->
                accountService.deposit(1L, BigDecimal.valueOf(100), false, 1L));
        assertEquals("Cannot deposit from another bank", e.getMessage());
    }
}