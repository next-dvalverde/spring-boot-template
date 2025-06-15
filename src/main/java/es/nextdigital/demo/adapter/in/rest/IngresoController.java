package es.nextdigital.demo.adapter.in.rest;

import es.nextdigital.demo.adapter.in.rest.dto.IngresoDTO;
import es.nextdigital.demo.adapter.in.rest.dto.MovimientoDTO;
import es.nextdigital.demo.application.models.Movimiento;
import es.nextdigital.demo.application.service.IngresoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ingresos")
class IngresoController {
    private IngresoService ingresoService;

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

    public IngresoController(IngresoService ingresoService) {
        this.ingresoService = ingresoService;
    }

    //####CODIGO####

    @PostMapping
    public ResponseEntity<?> ingresar(@RequestBody IngresoDTO dto) {
        try {
            Movimiento movimiento = ingresoService.ingresar(dto);
            MovimientoDTO movDto = mapToMovimientoDTO(movimiento);
            return ResponseEntity.ok().body(movDto);
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }


}
