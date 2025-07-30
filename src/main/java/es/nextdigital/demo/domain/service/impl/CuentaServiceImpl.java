/**
 * Autor: Álvaro López Arcos
 * Fecha de creación: 30 jul 2025
 * Descripción: CuentaServiceImpl.java
 *
 */
package es.nextdigital.demo.domain.service.impl;

import org.springframework.stereotype.Service;

import es.nextdigital.demo.domain.repository.CuentaRepository;
import es.nextdigital.demo.domain.service.CuentaService;
import es.nextdigital.demo.infra.mapper.CuentaMapper;
import es.nextdigital.demo.presentation.dto.CuentaDto;
import lombok.RequiredArgsConstructor;
@Service
@RequiredArgsConstructor
public class CuentaServiceImpl implements CuentaService {

    private final CuentaRepository cuentaRepository;
    private final CuentaMapper cuentaMapper;

    @Override
    public CuentaDto crearCuenta(CuentaDto cuentaDto) {
        var cuenta = cuentaMapper.toEntity(cuentaDto);
        var guardada = cuentaRepository.save(cuenta);
        return cuentaMapper.toDto(guardada);
    }
}