package es.nextdigital.demo.controller;

import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;

import es.nextdigital.demo.dto.CardDTO;
import es.nextdigital.demo.service.CardService;

@RestController
@RequestMapping("/card")
@RequiredArgsConstructor
public class CardController {

    private final CardService cardService;

    @PostMapping("/{id}/activate")
    public CardDTO activateCard(@PathVariable Long id) {
        return cardService.activateCard(id);
    }

    @PostMapping("/{id}/change-pin")
    public CardDTO changePin(@PathVariable Long id, @RequestParam String newPin) {
        return cardService.changePin(id, newPin);
    }

    @PutMapping("/{id}/limit")
    public CardDTO updateLimit(@PathVariable Long id, @RequestParam int newLimit) {
        return cardService.updateWithdrawLimit(id, newLimit);
    }
}