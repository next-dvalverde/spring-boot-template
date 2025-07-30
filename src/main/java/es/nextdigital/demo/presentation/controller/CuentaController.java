/**
 * Autor: Álvaro López Arcos
 * Fecha de creación: 30 jul 2025
 * Descripción: CuentaController.java
 *
 */
package es.nextdigital.demo.presentation.controller;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import es.nextdigital.demo.domain.service.CuentaService;
import es.nextdigital.demo.presentation.dto.CuentaDto;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/cuentas")
@RequiredArgsConstructor
public class CuentaController {

    private final CuentaService cuentaService;

    @PostMapping
    public ResponseEntity<CuentaDto> crearCuenta(@RequestBody CuentaDto cuentaDto) {
        return ResponseEntity.ok(cuentaService.crearCuenta(cuentaDto));
    }
}