package es.nextdigital.demo.adapter.in.rest.dto;

import lombok.Data;

@Data
public class TransferenciaDTO {
    private String numeroTarjeta;
    private String pin;
    private String ibanDestino;
    private Double cantidad;
    private String concepto;
}
