package es.nextdigital.demo.apirest.dto;

import java.math.BigDecimal;

public record MovementDTO(String movementType, BigDecimal quantity) {
}
