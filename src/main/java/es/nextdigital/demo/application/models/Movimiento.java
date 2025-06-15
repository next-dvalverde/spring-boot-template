package es.nextdigital.demo.application.models;

import es.nextdigital.demo.application.models.enums.TipoMovimiento;
import jakarta.persistence.*;
import lombok.Data;


@Data
@Entity
public class Movimiento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double cantidad;

    private String descripcion;

    @Enumerated(EnumType.STRING)
    private TipoMovimiento tipo; // INGRESO, RETIRADA, COMISION, TRANSFERENCIA_ENTRANTE, TRANSFERENCIA_SALIENTE

    private String fecha;

    @ManyToOne
    private Cuenta cuenta;
}
