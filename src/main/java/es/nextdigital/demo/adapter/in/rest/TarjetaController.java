package es.nextdigital.demo.adapter.in.rest;

import es.nextdigital.demo.adapter.in.rest.dto.ActivarTarjetaDTO;
import es.nextdigital.demo.adapter.in.rest.dto.CambiarPinDTO;
import es.nextdigital.demo.application.service.TarjetaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tarjetas")
class TarjetaController {

    private final TarjetaService tarjetaService;

    //Aunque lo mas optimo es usar un modelmapper, lo hacemos manualmente para ahorrar configuraciones

    //####MAPPERS####

    //####CODIGO####
    public TarjetaController(TarjetaService tarjetaService) {
        this.tarjetaService = tarjetaService;
    }

    @PostMapping("/activarTarjeta")
    public ResponseEntity<?> activarTarjeta(@RequestBody ActivarTarjetaDTO dto) {
        try {
            tarjetaService.activarTarjeta(dto);
            return ResponseEntity.ok().body("Tarjeta activada correctamente");
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @PostMapping("/cambiarPin")
    public ResponseEntity<?> cambiarPin(@RequestBody CambiarPinDTO dto) {
        try {
            tarjetaService.cambiarPin(dto);
            return ResponseEntity.ok().body("PIN cambiado correctamente");
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }
}
