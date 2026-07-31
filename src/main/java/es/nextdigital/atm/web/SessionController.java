package es.nextdigital.atm.web;

import es.nextdigital.atm.service.SessionService;
import es.nextdigital.atm.web.dto.SessionRequest;
import es.nextdigital.atm.web.dto.SessionResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/sessions")
public class SessionController {

    private final SessionService sessionService;

    public SessionController(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @PostMapping
    public SessionResponse open(@Valid @RequestBody SessionRequest request) {
        return SessionResponse.from(
                sessionService.open(request.cardNumber(), request.pin(), request.atmId()));
    }
}
