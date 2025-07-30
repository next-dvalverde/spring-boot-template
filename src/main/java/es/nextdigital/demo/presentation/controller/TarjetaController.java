/**
 * Autor: Álvaro López Arcos
 * Fecha de creación: 30 jul 2025
 * Descripción: TarjetaController.java
 *
 */
package es.nextdigital.demo.presentation.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import es.nextdigital.demo.application.command.ActivarTarjetaCommand;
import es.nextdigital.demo.domain.service.TarjetaService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/tarjetas")
@RequiredArgsConstructor
public class TarjetaController {

    private final TarjetaService tarjetaService;

    @PostMapping("/{id}/activar")
    public ResponseEntity<Void> activarTarjeta(@PathVariable Long id, @RequestBody ActivarTarjetaCommand command) {
        tarjetaService.activarTarjeta(id, command.getPin());
        return ResponseEntity.ok().build();
    }
}