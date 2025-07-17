package es.nextdigital.demo.controller;

import es.nextdigital.demo.dto.MovimientoDTO;
import es.nextdigital.demo.dto.OperacionRequestDTO;
import es.nextdigital.demo.dto.TransferenciaRequestDTO;
import es.nextdigital.demo.service.CajeroFacadeService;
import es.nextdigital.demo.service.ICuentaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/api/cuentas")
@RequiredArgsConstructor
public class CuentaController {

    private final ICuentaService cuentaService;
    private final CajeroFacadeService cajeroFacadeService;

    @GetMapping("/{iban}/movimientos")
    public ResponseEntity<List<MovimientoDTO>> getMovimientos(@PathVariable String iban) {
        return ResponseEntity.ok(cuentaService.getMovimientosByCuenta(iban));
    }

    @PostMapping("/ingresar")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void ingresar(@RequestBody OperacionRequestDTO request) {
        cajeroFacadeService.ingresarDinero(
                request.getNumeroTarjeta(),
                request.getPinHash(),
                request.getCantidad(),
                request.getBancoCajero()
        );
    }

    @PostMapping("/retirar")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void retirar(@RequestBody OperacionRequestDTO request) {
        cajeroFacadeService.retirarDinero(
                request.getNumeroTarjeta(),
                request.getPinHash(),
                request.getCantidad(),
                request.getBancoCajero()
        );
    }

    @PostMapping("/transferir")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void transferir(@RequestBody TransferenciaRequestDTO request) {
        cajeroFacadeService.transferir(
                request.getNumeroTarjeta(),
                request.getPinHash(),
                request.getIbanDestino(),
                request.getCantidad()
        );
    }
}
