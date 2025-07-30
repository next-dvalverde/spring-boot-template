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
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/cuentas")
@RequiredArgsConstructor
@AllArgsConstructor
public class MovimientoController {
	private final ObtenerMovimientosPorCuentaService service;

	//He tenido que usar el constructor manualmente porque no se que error me esta dando lombok
	public MovimientoController(ObtenerMovimientosPorCuentaService service) {
		this.service = service;
	}

	@GetMapping("/{cuentaId}/movimientos")
	public ResponseEntity<List<MovimientoDto>> obtenerMovimientos(@PathVariable Long cuentaId) {
		return ResponseEntity.ok(service.obtenerMovimientos(cuentaId));
	}
}
