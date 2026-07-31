package es.nextdigital.atm.web;

import es.nextdigital.atm.config.SessionFilter;
import es.nextdigital.atm.service.MovementService;
import es.nextdigital.atm.service.SessionContext;
import es.nextdigital.atm.web.dto.AccountResponse;
import es.nextdigital.atm.web.dto.MovementPageResponse;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/accounts")
public class AccountController {

    private final MovementService movementService;

    public AccountController(MovementService movementService) {
        this.movementService = movementService;
    }

    @GetMapping
    public List<AccountResponse> accounts(
            @RequestAttribute(SessionFilter.SESSION_ATTRIBUTE) SessionContext session) {
        return movementService.accountsOfHolder(session).stream().map(AccountResponse::from).toList();
    }

    // {id} es String a proposito: un UUID malformado responde el mismo 404 que
    // uno inexistente, sin codigo de error nuevo.
    @GetMapping("/{id}/movements")
    public MovementPageResponse movements(
            @RequestAttribute(SessionFilter.SESSION_ATTRIBUTE) SessionContext session,
            @PathVariable String id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return MovementPageResponse.from(movementService.movementsOf(session, id, page, size));
    }
}
