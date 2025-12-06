package es.nextdigital.demo.controller;

import es.nextdigital.demo.service.CajeroService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cajero")
public class CajeroController {

    private final CajeroService cajeroService;

    public CajeroController(CajeroService cajeroService) {
        this.cajeroService = cajeroService;
    }

    @PostMapping("/retirar")
    public String retirar(
            @RequestParam Long tarjetaId,
            @RequestParam Double cantidad,
            @RequestParam(defaultValue = "true") boolean mismoBanco
    ) {
        cajeroService.retirar(tarjetaId, cantidad, mismoBanco);
        return "Retirada realizada correctamente";
    }

    @PostMapping("/ingresar")
    public String ingresar(
            @RequestParam Long tarjetaId,
            @RequestParam Double cantidad,
            @RequestParam(defaultValue = "true") boolean mismoBanco
    ) {
        cajeroService.ingresar(tarjetaId, cantidad, mismoBanco);
        return "Ingreso realizado correctamente";
    }

    @PostMapping("/transferir")
    public String transferir(
            @RequestParam Long tarjetaId,
            @RequestParam String ibanDestino,
            @RequestParam Double cantidad,
            @RequestParam(defaultValue = "true") boolean mismoBancoDestino
    ) {
        cajeroService.transferir(tarjetaId, ibanDestino, cantidad, mismoBancoDestino);
        return "Transferencia realizada correctamente";
    }

    @PostMapping("/activar-tarjeta/{id}")
    public String activarTarjeta(@PathVariable Long id) {
        cajeroService.activarTarjeta(id);
        return "Tarjeta activada correctamente";
    }

    @PostMapping("/cambiar-pin")
    public String cambiarPin(
            @RequestParam Long tarjetaId,
            @RequestParam(required = false) String pinActual,
            @RequestParam String pinNuevo
    ) {
        cajeroService.cambiarPin(tarjetaId, pinActual, pinNuevo);
        return "PIN cambiado correctamente";
    }

}
