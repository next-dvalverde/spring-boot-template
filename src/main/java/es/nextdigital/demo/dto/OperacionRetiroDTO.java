package es.nextdigital.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * The type Operacion retiro dto.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OperacionRetiroDTO {

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

