package es.nextdigital.demo.dto;


import lombok.Data;
import java.math.BigDecimal;

@Data
public class TransferenciaRequestDTO {
    private String numeroTarjeta;
    private String pinHash;
    private String ibanDestino;
    private BigDecimal cantidad;
    private String descripcion;
}
