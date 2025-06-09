package es.nextdigital.demo.api.v1.controller;

import es.nextdigital.demo.dto.ActivacionTarjetaDTO;
import es.nextdigital.demo.dto.CambioPinDTO;
import es.nextdigital.demo.service.TarjetaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * The type Tarjeta controller.
 */
@RestController
@RequestMapping("/tarjetas")
public class TarjetaController {

    @Autowired
    private TarjetaService tarjetaService;

    /**
     * Activar response entity.
     *
     * @param id  the id
     * @param dto the dto
     * @return the response entity
     */
    @PostMapping("/{id}/activar")
    public ResponseEntity<Void> activar(@PathVariable Long id, @RequestBody ActivacionTarjetaDTO dto) {
        tarjetaService.activarTarjeta(id, dto.nuevoPin);
        return ResponseEntity.ok().build();
    }

    /**
     * Cambiar pin response entity.
     *
     * @param id  the id
     * @param dto the dto
     * @return the response entity
     */
    @PostMapping("/{id}/cambiar-pin")
    public ResponseEntity<Void> cambiarPin(@PathVariable Long id, @RequestBody CambioPinDTO dto) {
        tarjetaService.cambiarPin(id, dto.nuevoPin);
        return ResponseEntity.ok().build();
    }
}

