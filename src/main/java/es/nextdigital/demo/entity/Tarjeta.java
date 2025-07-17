package es.nextdigital.demo.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Entity
@Data
public class Tarjeta {

    @Id
    private String numero;

    @Enumerated(EnumType.STRING)
    private TarjetaTipo tipo; // DEBITO o CREDITO

    private boolean activada;

    private String pinHash;

    @Column(nullable = false)
    private BigDecimal limiteRetirada;

    // Para Tarjetas de crédito
    private BigDecimal maximoCredito;
    private BigDecimal creditoUsado;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "cuenta_id", nullable = false)
    private Cuenta cuentaAsociada;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "banco_id", nullable = false)
    private Banco bancoEmisor;


    public enum TarjetaTipo {
        DEBITO,
        CREDITO
    }

    /**
     * Crédito disponible solo aplica a tarjetas de crédito.
     */
    @Transient
    public BigDecimal getCreditoDisponible() {
        if (tipo == TarjetaTipo.CREDITO && maximoCredito != null && creditoUsado != null) {
            return maximoCredito.subtract(creditoUsado);
        }
        return BigDecimal.ZERO;
    }
}
