package es.nextdigital.demo.adapter.in.rest.dto;

import lombok.Data;

@Data
public class ActivarTarjetaDTO {
    private String numeroTarjeta;
    private String pin;
    // Puedes pedir otros datos de seguridad si lo consideras (DNI, fecha caducidad...)
}
