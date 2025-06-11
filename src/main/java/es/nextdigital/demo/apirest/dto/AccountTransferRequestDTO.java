package es.nextdigital.demo.apirest.dto;

import java.math.BigDecimal;

public record AccountTransferRequestDTO(BigDecimal quantityToTransfer, String destinationIban, String destinationBank) {
}
