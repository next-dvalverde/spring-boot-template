/**
 * Autor: Álvaro López Arcos
 * Fecha de creación: 30 jul 2025
 * Descripción: MovimientoMapper.java
 *
 */
package es.nextdigital.demo.infra.mapper;

import java.util.List;

import org.mapstruct.Mapper;

import es.nextdigital.demo.domain.model.Movimiento;
import es.nextdigital.demo.presentation.dto.MovimientoDto;

@Mapper(componentModel = "spring")
public interface MovimientoMapper {
    MovimientoDto toDto(Movimiento movimiento);
    List<MovimientoDto> toDtoList(List<Movimiento> movimientos);
}

