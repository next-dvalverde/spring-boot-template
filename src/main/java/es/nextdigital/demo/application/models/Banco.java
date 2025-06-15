package es.nextdigital.demo.application.models;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
public class Banco {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
    private String codigo;
}