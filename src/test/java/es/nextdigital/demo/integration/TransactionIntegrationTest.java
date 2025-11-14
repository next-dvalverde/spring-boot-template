package es.nextdigital.demo.integration;

import es.nextdigital.demo.model.*;
import es.nextdigital.demo.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class TransactionIntegrationTest {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    private Account account;
    private Card card;

    @BeforeEach
    void setup() {
        transactionRepository.deleteAll();
        cardRepository.deleteAll();
        accountRepository.deleteAll();

        account = createTestAccount("ES3333333333333333333333", BigDecimal.valueOf(1000));
        card = createTestCard(account, CardStatus.ACTIVE, BigDecimal.valueOf(5000));

        createTransaction(account, BigDecimal.valueOf(200), TransactionType.DEPOSIT, null);
        createTransaction(account, BigDecimal.valueOf(100), TransactionType.WITHDRAW, null);
    }

    @Test
    void testGetAccountTransactions() {
        List<Transaction> transactions = transactionRepository.findAll().stream()
                .filter(t -> t.getAccount().getId().equals(account.getId()))
                .toList();

        assertEquals(2, transactions.size());
        assertTrue(transactions.stream().anyMatch(t -> t.getType() == TransactionType.DEPOSIT));
        assertTrue(transactions.stream().anyMatch(t -> t.getType() == TransactionType.WITHDRAW));
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
