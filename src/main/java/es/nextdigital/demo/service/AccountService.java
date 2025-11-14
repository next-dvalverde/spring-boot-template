package es.nextdigital.demo.service;

import es.nextdigital.demo.dto.AccountDTO;
import es.nextdigital.demo.dto.TransactionDTO;
import java.math.BigDecimal;
import java.util.List;

public interface AccountService {
    List<TransactionDTO> getTransactions(Long accountId);
    AccountDTO deposit(Long accountId, BigDecimal amount, boolean sameBank, Long cardId);
    AccountDTO withdraw(Long accountId, BigDecimal amount, boolean sameBank, Long cardId);
}