package es.nextdigital.demo.services;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import es.nextdigital.demo.model.Account;
import es.nextdigital.demo.model.CardOperation;
import es.nextdigital.demo.model.OperationType;
import es.nextdigital.demo.repository.AccountRepository;
import es.nextdigital.demo.repository.CardOperationRepository;

@Service
public class AccountService {
    @Autowired
    private AccountRepository accountRepo;

    @Autowired
    private CardOperationRepository operationRepo;

    public List<CardOperation> getOperations(String iban) {
        Account acc = accountRepo.findByIban(iban)
                .orElseThrow(() -> new RuntimeException("Account missing. Use valid one"));
        return operationRepo.findByAccount(acc);
    }

    public void deposit(Account account, BigDecimal amount) {
        account.setBalance(account.getBalance().add(amount));

        CardOperation op = new CardOperation();
        op.setAccount(account);
        op.setAmount(amount);
        op.setType(OperationType.DEPOSIT);
        operationRepo.save(op);

        accountRepo.save(account);
    }

    public void withdraw(Account account, BigDecimal amount) {
        if (account.getBalance().compareTo(amount) < 0)
            throw new RuntimeException("Not enough amount in the account.");

        account.setBalance(account.getBalance().subtract(amount));

        CardOperation op = new CardOperation();
        op.setAccount(account);
        op.setAmount(amount);
        op.setType(OperationType.WITHDRAWAL);
        operationRepo.save(op);

        accountRepo.save(account);
    }
}
