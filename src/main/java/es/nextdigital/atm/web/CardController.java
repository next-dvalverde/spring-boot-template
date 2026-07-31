package es.nextdigital.atm.web;

import es.nextdigital.atm.config.SessionFilter;
import es.nextdigital.atm.service.CardService;
import es.nextdigital.atm.service.SessionContext;
import es.nextdigital.atm.web.dto.ActivationRequest;
import es.nextdigital.atm.web.dto.PinChangeRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/cards")
public class CardController {

    private final CardService cardService;

    public CardController(CardService cardService) {
        this.cardService = cardService;
    }

    @PostMapping("/activation")
    public void activate(@Valid @RequestBody ActivationRequest request) {
        cardService.activate(request.cardNumber(), request.pin(), request.atmId());
    }

    @PutMapping("/pin")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changePin(@RequestAttribute(SessionFilter.SESSION_ATTRIBUTE) SessionContext session,
                          @Valid @RequestBody PinChangeRequest request) {
        cardService.changePin(session, request.currentPin(), request.newPin());
    }
}
