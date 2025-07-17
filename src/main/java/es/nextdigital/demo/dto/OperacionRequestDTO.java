package es.nextdigital.demo.dto;

import es.nextdigital.demo.entity.Banco;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class OperacionRequestDTO {

    private BigDecimal cantidad;

    private String descripcion;

    private String numeroTarjeta;

    private String pinHash;

    private Banco bancoCajero;
}
