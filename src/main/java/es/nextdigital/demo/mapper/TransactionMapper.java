package es.nextdigital.demo.mapper;

import es.nextdigital.demo.dto.TransactionDTO;
import es.nextdigital.demo.model.Transaction;

public class TransactionMapper {

    public static TransactionDTO toDTO(Transaction transaction) {
        TransactionDTO dto = new TransactionDTO();
        dto.setId(transaction.getId());
        dto.setType(transaction.getType());
        dto.setAmount(transaction.getAmount());
        dto.setDate(transaction.getDate());
        dto.setTargetIban(transaction.getTargetIban());
        return dto;
    }
}