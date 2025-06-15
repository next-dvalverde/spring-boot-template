package es.nextdigital.demo.application.models;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
public class Cajero {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Banco bancoPropietario; // Relación con Banco

    private Double comisionOtraEntidad;
}
