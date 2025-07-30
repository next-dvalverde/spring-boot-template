/**
 * Autor: Álvaro López Arcos
 * Fecha de creación: 30 jul 2025
 * Descripción: MovimientoController.java
 *
 */
package es.nextdigital.demo.presentation.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import es.nextdigital.demo.domain.service.ObtenerMovimientosPorCuentaService;
import es.nextdigital.demo.presentation.dto.MovimientoDto;

@RestController
@RequestMapping("/api/cuentas")
public class MovimientoController {
	private final ObtenerMovimientosPorCuentaService service;

    public MovimientoController(ObtenerMovimientosPorCuentaService service) {
		super();
		this.service = service;
	}

	@GetMapping("/{cuentaId}/movimientos")
    public ResponseEntity<List<MovimientoDto>> obtenerMovimientos(@PathVariable Long cuentaId) {
        return ResponseEntity.ok(service.obtenerMovimientos(cuentaId));
    }
}
