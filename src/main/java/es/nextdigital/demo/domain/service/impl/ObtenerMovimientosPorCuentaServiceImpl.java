
/**
 * Autor: Álvaro López Arcos
 * Fecha de creación: 30 jul 2025
 * Descripción: ObtenerMovimientosPorCuentaServiceImpl.java
 *
 */
package es.nextdigital.demo.domain.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import es.nextdigital.demo.domain.repository.MovimientoRepository;
import es.nextdigital.demo.domain.service.ObtenerMovimientosPorCuentaService;
import es.nextdigital.demo.infra.mapper.MovimientoMapper;
import es.nextdigital.demo.presentation.dto.MovimientoDto;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ObtenerMovimientosPorCuentaServiceImpl implements ObtenerMovimientosPorCuentaService {

    private final MovimientoRepository movimientoRepository;
    private final MovimientoMapper movimientoMapper;
    
    //He tenido que usar el constructor manualmente porque no se que error me esta dando lombok
    public ObtenerMovimientosPorCuentaServiceImpl(
        MovimientoRepository movimientoRepository,
        MovimientoMapper movimientoMapper
    ) {
        this.movimientoRepository = movimientoRepository;
        this.movimientoMapper = movimientoMapper;
    }

    @Override
    public List<MovimientoDto> obtenerMovimientos(Long cuentaId) {
        return movimientoMapper.toDtoList(movimientoRepository.findByCuentaId(cuentaId));
    }
}
