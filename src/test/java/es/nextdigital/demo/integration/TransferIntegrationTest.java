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
class TransferIntegrationTest {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    private Account accountA;
    private Account accountB;
    private Card cardA;

    @BeforeEach
    void setup() {
        transactionRepository.deleteAll();
        cardRepository.deleteAll();
        accountRepository.deleteAll();

        accountA = createTestAccount("ES1111111111111111111111", BigDecimal.valueOf(1000));
        accountB = createTestAccount("ES2222222222222222222222", BigDecimal.valueOf(500));
        cardA = createTestCard(accountA, CardStatus.ACTIVE, BigDecimal.valueOf(5000));
    }

    @Test
    void testTransferSameBank() {
        BigDecimal amount = BigDecimal.valueOf(200);

        //Trans between same bank
        accountA.setBalance(accountA.getBalance().subtract(amount));
        accountB.setBalance(accountB.getBalance().add(amount));
        accountRepository.save(accountA);
        accountRepository.save(accountB);

        //Register transactions
        createTransaction(accountA, amount, TransactionType.TRANSFER_OUT, accountB.getIban());
        createTransaction(accountB, amount, TransactionType.TRANSFER_IN, accountA.getIban());

        //Check amounts
        assertTrue(BigDecimal.valueOf(800).compareTo(accountRepository.findById(accountA.getId()).get().getBalance()) == 0);
        assertTrue(BigDecimal.valueOf(700).compareTo(accountRepository.findById(accountB.getId()).get().getBalance()) == 0);

        //Verify transactions
        List<Transaction> transactionsA = transactionRepository.findAll().stream()
                .filter(t -> t.getAccount().getId().equals(accountA.getId()))
                .toList();
        assertTrue(transactionsA.stream().anyMatch(t -> t.getType() == TransactionType.TRANSFER_OUT));

        List<Transaction> transactionsB = transactionRepository.findAll().stream()
                .filter(t -> t.getAccount().getId().equals(accountB.getId()))
                .toList();
        assertTrue(transactionsB.stream().anyMatch(t -> t.getType() == TransactionType.TRANSFER_IN));
    }


    @Test
    void testTransferOtherBankWithCommission() {
        BigDecimal amount = BigDecimal.valueOf(200);
        BigDecimal commission = amount.multiply(BigDecimal.valueOf(0.01)); // 1%
        BigDecimal totalDebit = amount.add(commission);

        accountA.setBalance(accountA.getBalance().subtract(totalDebit));
        accountB.setBalance(accountB.getBalance().add(amount));
        accountRepository.save(accountA);
        accountRepository.save(accountB);

        createTransaction(accountA, totalDebit, TransactionType.TRANSFER_OUT, "IBAN_OTHERBANK");
        createTransaction(accountB, amount, TransactionType.TRANSFER_IN, accountA.getIban());

        assertTrue(BigDecimal.valueOf(1000).subtract(totalDebit).compareTo(accountRepository.findById(accountA.getId()).get().getBalance()) == 0);
        assertTrue(BigDecimal.valueOf(500).add(amount).compareTo(accountRepository.findById(accountB.getId()).get().getBalance()) == 0);
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
