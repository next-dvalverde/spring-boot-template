package es.nextdigital.demo.application.models;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
public class Tarjeta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String numero;

    private String tipo; // "DEBITO" o "CREDITO"
    private boolean activa;
    private Double limite;
    private Double disponible;
    private String pin;

    @ManyToOne
    private Cuenta cuentaAsociada;

    @ManyToOne
    private Banco banco; // Relación con Banco
}
