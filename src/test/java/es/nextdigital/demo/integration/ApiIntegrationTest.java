package es.nextdigital.demo.integration;

import es.nextdigital.demo.model.*;
import es.nextdigital.demo.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ApiIntegrationTest {

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    private Card card;
    private Account account;

    @BeforeEach
    void setup() {
        transactionRepository.deleteAll();
        cardRepository.deleteAll();
        accountRepository.deleteAll();

        account = createTestAccount("ES1234567890123456789012", BigDecimal.valueOf(1000));
        card = createTestCard(account, CardStatus.INACTIVE, BigDecimal.valueOf(500));
    }

    @Test
    void testActivateCardThenWithdrawFlow() {
        //Card activation
        card.setStatus(CardStatus.ACTIVE);
        cardRepository.save(card);
        Card refreshedCard = cardRepository.findById(card.getId()).get();
        assertEquals(CardStatus.ACTIVE, refreshedCard.getStatus());

        //Withdraw money
        BigDecimal amount = BigDecimal.valueOf(400);
        account.setBalance(account.getBalance().subtract(amount));
        accountRepository.save(account);
        Account refreshedAccount = accountRepository.findById(account.getId()).get();
        assertTrue(BigDecimal.valueOf(600).compareTo(refreshedAccount.getBalance()) == 0);


        //Registre transaction
        Transaction tx = createTransaction(account, amount, TransactionType.WITHDRAW, null);
        assertEquals(TransactionType.WITHDRAW, tx.getType());
    }

    // Helper methods
    private Account createTestAccount(String iban, BigDecimal balance) {
        Account acc = new Account();
        acc.setIban(iban);
        acc.setBalance(balance);
        return accountRepository.save(acc);
    }

    private Card createTestCard(Account account, CardStatus status, BigDecimal limit) {
        Card c = new Card();
        c.setAccount(account);
        c.setStatus(status);
        c.setWithdrawLimit(limit);
        c.setCardNumber("1234-5678-9012-3456");
        c.setPinHash(new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder().encode("1234"));
        return cardRepository.save(c);
    }

    private Transaction createTransaction(Account account, BigDecimal amount, TransactionType type, String targetIban) {
        Transaction tx = new Transaction();
        tx.setAccount(account);
        tx.setAmount(amount);
        tx.setType(type);
        tx.setTargetIban(targetIban);
        return transactionRepository.save(tx);
    }
}