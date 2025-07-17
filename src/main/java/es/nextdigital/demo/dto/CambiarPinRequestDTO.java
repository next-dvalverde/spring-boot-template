package es.nextdigital.demo.dto;

import lombok.Data;

@Data
public class CambiarPinRequestDTO {

    private String pinActual;
    private String nuevoPin;
}
