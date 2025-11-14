package es.nextdigital.demo.controller;

import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;

import es.nextdigital.demo.dto.AccountDTO;
import es.nextdigital.demo.dto.TransactionDTO;
import es.nextdigital.demo.service.AccountService;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/account")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @GetMapping("/{id}/transactions")
    public List<TransactionDTO> getTransactions(@PathVariable Long id) {
        return accountService.getTransactions(id);
    }

    @PostMapping("/{id}/deposit")
    public AccountDTO deposit(@PathVariable Long id,
                              @RequestParam BigDecimal amount,
                              @RequestParam Long cardId,
                              @RequestParam(defaultValue = "true") boolean sameBank) {
        return accountService.deposit(id, amount, sameBank, cardId);
    }

    @PostMapping("/{id}/withdraw")
    public AccountDTO withdraw(@PathVariable Long id,
                               @RequestParam BigDecimal amount,
                               @RequestParam Long cardId,
                               @RequestParam(defaultValue = "true") boolean sameBank) {
        return accountService.withdraw(id, amount, sameBank, cardId);
    }
}