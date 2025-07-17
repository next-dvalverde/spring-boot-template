package es.nextdigital.demo.controller;

import es.nextdigital.demo.dto.ActivarTarjetaRequestDTO;
import es.nextdigital.demo.dto.CambiarPinRequestDTO;
import es.nextdigital.demo.service.ITarjetaService;
import es.nextdigital.demo.service.TarjetaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/api/tarjetas")
@RequiredArgsConstructor
public class TarjetaController {

    private final ITarjetaService tarjetaService;

    @PostMapping("/{numero}/activar")
    public ResponseEntity<Void> activarTarjeta(@PathVariable String numero,
                                               @RequestBody ActivarTarjetaRequestDTO request) {
        tarjetaService.activarTarjeta(numero, request.getPinInicial());
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{numero}/cambiar-pin")
    public ResponseEntity<Void> cambiarPin(@PathVariable String numero,
                                           @RequestBody CambiarPinRequestDTO request) {
        tarjetaService.cambiarPin(numero, request.getPinActual(), request.getNuevoPin());
        return ResponseEntity.ok().build();
    }
}
