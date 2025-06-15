package es.nextdigital.demo.adapter.in.rest;

import es.nextdigital.demo.adapter.in.rest.dto.MovimientoDTO;
import es.nextdigital.demo.adapter.in.rest.dto.RetiradaDTO;
import es.nextdigital.demo.application.models.Movimiento;
import es.nextdigital.demo.application.service.MovimientoService;
import es.nextdigital.demo.application.service.RetiradaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/retiradas")
class RetiradaController {

    private RetiradaService retiradaService;

    //Aunque lo mas optimo es usar un modelmapper, lo hacemos manualmente para ahorrar configuraciones

    //####MAPPERS####
    private MovimientoDTO mapToMovimientoDTO(Movimiento mov) {
        MovimientoDTO dto = new MovimientoDTO();
        dto.setId(mov.getId());
        dto.setCantidad(mov.getCantidad());
        dto.setDescripcion(mov.getDescripcion());
        dto.setTipo(mov.getTipo());
        dto.setFecha(mov.getFecha());
        return dto;
    }

    //####CODIGO####

    public RetiradaController(RetiradaService retiradaService) {
        this.retiradaService = retiradaService;
    }

    @PostMapping("/retirar")
    public ResponseEntity<?> retirar(@RequestBody RetiradaDTO dto) {
        try {
            Movimiento movimiento = retiradaService.retirar(dto);
            MovimientoDTO movDto = mapToMovimientoDTO(movimiento);
            return ResponseEntity.ok().body(movDto);
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

}
