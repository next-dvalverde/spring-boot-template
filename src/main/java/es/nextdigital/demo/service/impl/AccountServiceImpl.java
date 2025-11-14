package es.nextdigital.demo.service.impl;

import es.nextdigital.demo.dto.AccountDTO;
import es.nextdigital.demo.dto.TransactionDTO;
import es.nextdigital.demo.mapper.AccountMapper;
import es.nextdigital.demo.mapper.TransactionMapper;
import es.nextdigital.demo.model.*;
import es.nextdigital.demo.repository.AccountRepository;
import es.nextdigital.demo.repository.CardRepository;
import es.nextdigital.demo.repository.TransactionRepository;
import es.nextdigital.demo.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final CardRepository cardRepository;

    @Override
    public List<TransactionDTO> getTransactions(Long accountId) {
        return transactionRepository.findByAccountId(accountId)
                .stream()
                .map(TransactionMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public AccountDTO withdraw(Long accountId, BigDecimal amount, boolean sameBank, Long cardId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        // Verify active card and withdraw limit
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new RuntimeException("Card not found"));
        if (card.getStatus() != CardStatus.ACTIVE)
            throw new RuntimeException("Card is not active");
        if (amount.compareTo(card.getWithdrawLimit()) > 0)
            throw new RuntimeException("Amount exceeds card withdraw limit");

        if (account.getBalance().compareTo(amount) < 0)
            throw new RuntimeException("Insufficient balance");

        account.setBalance(account.getBalance().subtract(amount));

        Transaction t = new Transaction();
        t.setAccount(account);
        t.setAmount(amount);
        t.setType(TransactionType.WITHDRAW);
        transactionRepository.save(t);

        accountRepository.save(account);
        return AccountMapper.toDTO(account);
    }

    @Override
    public AccountDTO deposit(Long accountId, BigDecimal amount, boolean sameBank, Long cardId) {
        if (!sameBank) throw new RuntimeException("Cannot deposit from another bank");

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        // Verify active card
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new RuntimeException("Card not found"));
        if (card.getStatus() != CardStatus.ACTIVE)
            throw new RuntimeException("Card is not active");

        account.setBalance(account.getBalance().add(amount));

        Transaction t = new Transaction();
        t.setAccount(account);
        t.setAmount(amount);
        t.setType(TransactionType.DEPOSIT);
        transactionRepository.save(t);

        accountRepository.save(account);
        return AccountMapper.toDTO(account);
    }

}
