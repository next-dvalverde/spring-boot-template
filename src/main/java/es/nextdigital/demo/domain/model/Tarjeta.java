/**
 * Autor: Álvaro López Arcos
 * Fecha de creación: 30 jul 2025
 * Descripción: Tarjeta.java
 *
 */
package es.nextdigital.demo.domain.model;


import es.nextdigital.demo.domain.model.enums.TipoTarjeta;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class Tarjeta extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String numero; 

    private boolean activa;

    private String pin;

    private Double limite;

    private Double creditoDisponible;

    @Enumerated(EnumType.STRING)
    private TipoTarjeta tipo;
    @ManyToOne
    @JoinColumn(name = "cuenta_id")
    private Cuenta cuenta;
}