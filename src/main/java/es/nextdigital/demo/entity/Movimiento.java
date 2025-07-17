package es.nextdigital.demo.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Movimiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime fecha;

    private BigDecimal cantidad;

    @Enumerated(EnumType.STRING)
    private TipoMovimiento tipo;

    private String descripcion;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "cuenta_id")
    private Cuenta cuenta;

    public enum TipoMovimiento {
        INGRESO,
        RETIRADA,
        TRANSFERENCIA_ENTRANTE,
        TRANSFERENCIA_SALIENTE,
        COMISION
    }
}
