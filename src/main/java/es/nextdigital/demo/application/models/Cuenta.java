package es.nextdigital.demo.application.models;

import jakarta.persistence.*;

import lombok.Data;

import java.util.List;

@Data
@Entity
public class Cuenta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String iban;
    private Double saldo;

    @OneToMany(mappedBy = "cuenta")
    private List<Movimiento> movimientos;

    @ManyToOne
    private Banco banco;
}
