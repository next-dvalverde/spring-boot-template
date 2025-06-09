package es.nextdigital.demo.dto;

import es.nextdigital.demo.enums.TipoMovimiento;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * The type Movimiento dto.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MovimientoDTO {

    /**
     * The fecha.
     */
    private LocalDateTime fecha;

    /**
     * The cantidad.
     */
    private BigDecimal cantidad;

    /**
     * The tipo.
     */
    private TipoMovimiento tipo;

    /**
     * The descripcion.
     */
    private String descripcion;

}

