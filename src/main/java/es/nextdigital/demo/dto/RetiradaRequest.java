package es.nextdigital.demo.dto;

import es.nextdigital.demo.entity.Banco;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class RetiradaRequest {
    private String numeroTarjeta;
    private String pinHash;
    private BigDecimal cantidad;
    private Banco bancoCajero;
}
