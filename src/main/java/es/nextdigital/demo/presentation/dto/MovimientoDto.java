/**
 * Autor: Álvaro López Arcos
 * Fecha de creación: 30 jul 2025
 * Descripción: MovimientoDto.java
 *
 */
package es.nextdigital.demo.presentation.dto;

import java.time.LocalDateTime;

import es.nextdigital.demo.domain.model.enums.MovimientoTipo;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MovimientoDto {
    private Long id;
    private Double cantidad;
    private LocalDateTime fecha;
    private MovimientoTipo tipo;
}
