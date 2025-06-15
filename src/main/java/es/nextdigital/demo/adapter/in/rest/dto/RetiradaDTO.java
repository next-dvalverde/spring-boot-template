package es.nextdigital.demo.adapter.in.rest.dto;

import lombok.Data;

@Data
public class RetiradaDTO {
    private String numeroTarjeta;
    private String pin;
    private Double cantidad;
    private Long idCajero;
}
