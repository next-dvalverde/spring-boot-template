/**
 * Autor: Álvaro López Arcos
 * Fecha de creación: 30 jul 2025
 * Descripción: QueryMovimientosHandler.java
 *
 */
package es.nextdigital.demo.application.handler;

import java.util.List;

import es.nextdigital.demo.application.command.QueryMovimientosCommand;
import es.nextdigital.demo.domain.service.ObtenerMovimientosPorCuentaService;
import es.nextdigital.demo.presentation.dto.MovimientoDto;

public class QueryMovimientosHandler {
	//he tenido que usar constructor manual pq no va lombok y estaba poerdiendo mucho tiempo
    private final ObtenerMovimientosPorCuentaService obtenerMovimientosService;

    public QueryMovimientosHandler(ObtenerMovimientosPorCuentaService obtenerMovimientosService) {
		super();
		this.obtenerMovimientosService = obtenerMovimientosService;
	}

	public List<MovimientoDto> handle(QueryMovimientosCommand command) {
        return obtenerMovimientosService.obtenerMovimientos(command.getCuentaId());
    }
}
