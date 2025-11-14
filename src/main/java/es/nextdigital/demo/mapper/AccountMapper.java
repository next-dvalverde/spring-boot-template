package es.nextdigital.demo.mapper;

import es.nextdigital.demo.dto.AccountDTO;
import es.nextdigital.demo.model.Account;

public class AccountMapper {

    public static AccountDTO toDTO(Account account) {
        AccountDTO dto = new AccountDTO();
        dto.setId(account.getId());
        dto.setIban(account.getIban());
        dto.setBalance(account.getBalance());
        return dto;
    }
}