package es.nextdigital.demo.adapter.in.rest;

import es.nextdigital.demo.adapter.in.rest.dto.MovimientoDTO;
import es.nextdigital.demo.application.models.Movimiento;
import es.nextdigital.demo.application.service.MovimientoService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/movimientos")
class MovimientoController {

    private MovimientoService movimientoService;

    //Aunque lo mas optimo es usar un modelmapper, lo hacemos manualmente para ahorrar configuraciones

    //####MAPPERS####
    private MovimientoDTO convertirAMovimientoDTO(Movimiento movimiento) {
        MovimientoDTO dto = new MovimientoDTO();
        dto.setId(movimiento.getId());
        dto.setCantidad(movimiento.getCantidad());
        dto.setDescripcion(movimiento.getDescripcion());
        dto.setTipo(movimiento.getTipo());
        dto.setFecha(movimiento.getFecha());
        return dto;
    }

    //####CODIGO####

    public MovimientoController(MovimientoService movimientoService) {
        this.movimientoService = movimientoService;
    }

    @GetMapping("/obtenerMovimientos")
    public List<MovimientoDTO> obtenerMovimientos(@RequestParam Long cuentaId, @RequestParam String pin) {
        //Mapeamos la entidad a DTO para facilitar la comunicación entre front y back
        List<Movimiento> movimientos = movimientoService.obtenerMovimientosPorCuenta(cuentaId, pin);
        return movimientos.stream().map(this::convertirAMovimientoDTO).toList();
    }
}
