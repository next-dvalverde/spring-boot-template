package es.nextdigital.demo.rest;

import org.springframework.web.bind.annotation.RestController;

import es.nextdigital.demo.model.Card;
import es.nextdigital.demo.model.CardOperation;
import es.nextdigital.demo.repository.AccountRepository;
import es.nextdigital.demo.services.AccountService;
import es.nextdigital.demo.services.CardService;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
public class AccountController {

    @Autowired
    private AccountService accountService;

    @Autowired
    private CardService cardService;

    @GetMapping("/getOperations")
    public List<CardOperation> getOperations(@RequestParam String iban) {
        return accountService.getOperations(iban);
    }

    @PostMapping("/insertMoneyCard")
    public void insertMoneyCard(@RequestParam String cardNumber, @RequestParam String pin,
            @RequestParam BigDecimal amount) {
        Card card = cardService.validateCard(cardNumber, pin);
        accountService.deposit(card.getAccount(), amount);
    }

    @PostMapping("/extractMoneyCard")
    public void extractMoneyCard(@RequestParam String cardNumber, @RequestParam String pin,
            @RequestParam BigDecimal amount) {
        Card card = cardService.validateCard(cardNumber, pin);

        if (amount.compareTo(card.getWithdrawLimit()) > 0)
            throw new RuntimeException("Límite de tarjeta excedido");

        accountService.withdraw(card.getAccount(), amount);
    }
}
