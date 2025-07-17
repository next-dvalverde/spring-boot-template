package es.nextdigital.demo.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Banco {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String nombre;

    @Column(nullable = false)
    private double comisionCajeroAjeno = 0.02;

}
