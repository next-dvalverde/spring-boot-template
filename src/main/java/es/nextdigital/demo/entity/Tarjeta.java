package es.nextdigital.demo.entity;

import es.nextdigital.demo.enums.TipoTarjeta;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * The type Tarjeta.
 */
@Entity
@Getter
@Setter
public class Tarjeta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String numero;

    private boolean activa;

    private String pin;

    private BigDecimal limite;

    @Enumerated(EnumType.STRING)
    private TipoTarjeta tipo;

    @ManyToOne
    private Cliente cliente;

    @OneToOne
    private Cuenta cuenta;
}

