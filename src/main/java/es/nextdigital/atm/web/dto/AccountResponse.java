package es.nextdigital.atm.web.dto;

import es.nextdigital.atm.domain.Account;
import java.math.BigDecimal;
import java.util.UUID;

public record AccountResponse(UUID id, String iban, BigDecimal balance, String currency) {

    public static AccountResponse from(Account account) {
        return new AccountResponse(account.getId(), account.getIban(),
                account.getBalance(), account.getCurrency());
    }
}
