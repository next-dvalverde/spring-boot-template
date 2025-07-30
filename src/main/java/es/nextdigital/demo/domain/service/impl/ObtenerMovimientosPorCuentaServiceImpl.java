
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

@Service
public class ObtenerMovimientosPorCuentaServiceImpl implements ObtenerMovimientosPorCuentaService {

	//he tenido que usar constructor manual pq no va lombok y estaba poerdiendo mucho tiempo
    private final MovimientoRepository movimientoRepository;
    public ObtenerMovimientosPorCuentaServiceImpl(MovimientoRepository movimientoRepository,
			MovimientoMapper movimientoMapper) {
		super();
		this.movimientoRepository = movimientoRepository;
		this.movimientoMapper = movimientoMapper;
	}

	private final MovimientoMapper movimientoMapper;

    @Override
    public List<MovimientoDto> obtenerMovimientos(Long cuentaId) {
        return movimientoMapper.toDtoList(movimientoRepository.findByCuentaId(cuentaId));
    }
}
