package es.nextdigital.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * The type Operacion ingreso dto.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OperacionIngresoDTO {

    /**
     * The Tarjeta id.
     */
    public Long tarjetaId;
    /**
     * The Cantidad.
     */
    public BigDecimal cantidad;
    /**
     * The Cajero propio.
     */
    public boolean cajeroPropio;
}
