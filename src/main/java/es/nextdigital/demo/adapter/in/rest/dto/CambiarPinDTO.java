package es.nextdigital.demo.adapter.in.rest.dto;

import lombok.Data;

@Data
public class CambiarPinDTO {
    private String numeroTarjeta;
    private String pinActual;
    private String pinNuevo;
}
