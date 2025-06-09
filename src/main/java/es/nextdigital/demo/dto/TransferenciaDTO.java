package es.nextdigital.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * The type Transferencia dto.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TransferenciaDTO {

    /**
     * The Tarjeta id.
     */
    public Long tarjetaId;
    /**
     * The Iban destino.
     */
    public String ibanDestino;
    /**
     * The Cantidad.
     */
    public BigDecimal cantidad;
}

