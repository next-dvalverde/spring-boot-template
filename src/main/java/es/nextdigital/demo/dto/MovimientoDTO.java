package es.nextdigital.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MovimientoDTO {
    private Long id;
    private LocalDateTime fecha;
    private BigDecimal cantidad;
    private String tipo;
    private String descripcion;
    private String cuentaIban;
}

