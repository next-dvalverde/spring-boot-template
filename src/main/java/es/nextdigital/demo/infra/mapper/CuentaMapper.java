/**
 * Autor: Álvaro López Arcos
 * Fecha de creación: 30 jul 2025
 * Descripción: CuentaMapper.java
 *
 */
package es.nextdigital.demo.infra.mapper;


import org.mapstruct.Mapper;

import es.nextdigital.demo.domain.model.Cuenta;
import es.nextdigital.demo.presentation.dto.CuentaDto;

@Mapper(componentModel = "spring")
public interface CuentaMapper {
    Cuenta toEntity(CuentaDto dto);
    CuentaDto toDto(Cuenta entity);
}