package es.nextdigital.demo.controller;

import es.nextdigital.demo.model.Movimiento;
import es.nextdigital.demo.service.CuentaService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/cuentas")
public class CuentaController {

    private final CuentaService cuentaService;

    public CuentaController(CuentaService cuentaService) {
        this.cuentaService = cuentaService;
    }

    @GetMapping("/{id}/movimientos")
    public List<Movimiento> obtenerMovimientos(@PathVariable Long id) {
        return cuentaService.obtenerMovimientos(id);
    }
}
