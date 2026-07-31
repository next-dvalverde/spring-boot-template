package es.nextdigital.atm.web;

import es.nextdigital.atm.config.SessionFilter;
import es.nextdigital.atm.service.OperationService;
import es.nextdigital.atm.service.SessionContext;
import es.nextdigital.atm.web.dto.DepositRequest;
import es.nextdigital.atm.web.dto.ReceiptResponse;
import es.nextdigital.atm.web.dto.WithdrawalRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/operations")
public class OperationController {

    private final OperationService operationService;

    public OperationController(OperationService operationService) {
        this.operationService = operationService;
    }

    @PostMapping("/withdrawals")
    public ReceiptResponse withdraw(@RequestAttribute(SessionFilter.SESSION_ATTRIBUTE) SessionContext session,
                                    @Valid @RequestBody WithdrawalRequest request) {
        return ReceiptResponse.from(operationService.withdraw(session, request.amount()));
    }

    @PostMapping("/deposits")
    public ReceiptResponse deposit(@RequestAttribute(SessionFilter.SESSION_ATTRIBUTE) SessionContext session,
                                   @Valid @RequestBody DepositRequest request) {
        return ReceiptResponse.from(operationService.deposit(session, request.amount()));
    }
}
