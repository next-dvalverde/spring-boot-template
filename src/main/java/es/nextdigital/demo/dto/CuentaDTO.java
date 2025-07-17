package es.nextdigital.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class CuentaDTO {
    private String iban;
    private BigDecimal saldo;
}
