package es.nextdigital.demo.rest;

import org.springframework.web.bind.annotation.RestController;

import es.nextdigital.demo.model.Card;
import es.nextdigital.demo.repository.CardRepository;
import es.nextdigital.demo.services.CardService;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
public class CardController {

    @Autowired
    private CardService cardService;

    @PostMapping("/enableCard")
    public Card enableCard(@RequestParam String cardNumber, @RequestParam String pin) {
        return cardService.activateCard(cardNumber, pin);
    }

    @PostMapping("/changePIN")
    public void changePIN(@RequestParam String cardNumber, @RequestParam String newPin) {
        cardService.changePin(cardNumber, newPin);
    }

    @PostMapping("/getCardSettings")
    public String getCardSettings(@RequestBody String entity) {
        // TODO: process POST request

        return entity;
    }

    @PostMapping("/changeCardSettings")
    public void changeCardSettings(@RequestParam String cardNumber, @RequestParam BigDecimal limit) {
        cardService.updateWithdrawLimit(cardNumber, limit);
    }
}