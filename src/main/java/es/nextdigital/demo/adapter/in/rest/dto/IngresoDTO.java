package es.nextdigital.demo.adapter.in.rest.dto;

import lombok.Data;

//Aunque sea igual que el de retirada, usamos DTOs distintos por si en el futuro es necesario añadir campos diferentes
@Data
public class IngresoDTO {
    private String numeroTarjeta;
    private String pin;
    private Double cantidad;
    private Long idCajero;
}
