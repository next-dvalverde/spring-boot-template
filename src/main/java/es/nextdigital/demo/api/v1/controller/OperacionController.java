package es.nextdigital.demo.api.v1.controller;

import es.nextdigital.demo.dto.MovimientoDTO;
import es.nextdigital.demo.dto.OperacionIngresoDTO;
import es.nextdigital.demo.dto.OperacionRetiroDTO;
import es.nextdigital.demo.dto.TransferenciaDTO;
import es.nextdigital.demo.service.OperacionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * The type Operacion controller.
 */
@RestController
@RequestMapping("/operaciones")
public class OperacionController {

    @Autowired
    private OperacionService operacionService;

    /**
     * Movimientos list.
     *
     * @param cuentaId the cuenta id
     * @return the list
     */
    @GetMapping("/movimientos/{cuentaId}")
    public List<MovimientoDTO> movimientos(@PathVariable Long cuentaId) {
        return operacionService.consultarMovimientos(cuentaId);
    }

    /**
     * Retirar response entity.
     *
     * @param dto the dto
     * @return the response entity
     */
    @PostMapping("/retiro")
    public ResponseEntity<Void> retirar(@RequestBody OperacionRetiroDTO dto) {
        operacionService.retirar(dto);
        return ResponseEntity.ok().build();
    }

    /**
     * Ingresar response entity.
     *
     * @param dto the dto
     * @return the response entity
     */
    @PostMapping("/ingreso")
    public ResponseEntity<Void> ingresar(@RequestBody OperacionIngresoDTO dto) {
        operacionService.ingresar(dto);
        return ResponseEntity.ok().build();
    }

    /**
     * Transferir response entity.
     *
     * @param dto the dto
     * @return the response entity
     */
    @PostMapping("/transferencia")
    public ResponseEntity<Void> transferir(@RequestBody TransferenciaDTO dto) {
        operacionService.transferir(dto);
        return ResponseEntity.ok().build();
    }
}
