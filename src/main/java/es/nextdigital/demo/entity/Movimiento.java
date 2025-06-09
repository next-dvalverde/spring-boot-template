package es.nextdigital.demo.entity;

import es.nextdigital.demo.enums.TipoMovimiento;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * The type Movimiento.
 */
@Entity
@Getter
@Setter
public class Movimiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime fecha;

    private BigDecimal cantidad;

    private String descripcion;

    @Enumerated(EnumType.STRING)
    private TipoMovimiento tipo;

    @ManyToOne
    private Cuenta cuenta;
}
