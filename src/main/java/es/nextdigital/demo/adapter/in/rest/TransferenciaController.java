package es.nextdigital.demo.adapter.in.rest;

import es.nextdigital.demo.adapter.in.rest.dto.*;
import es.nextdigital.demo.application.models.*;
import es.nextdigital.demo.application.service.TransferenciaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/transferencias")
class TransferenciaController {
    private final TransferenciaService transferenciaService;

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

    public TransferenciaController(TransferenciaService transferenciaService) {
        this.transferenciaService = transferenciaService;
    }

    @PostMapping("/transferir")
    public ResponseEntity<?> transferir(@RequestBody TransferenciaDTO dto) {
        try {
            Movimiento movimiento = transferenciaService.transferir(dto);
            MovimientoDTO movDto = mapToMovimientoDTO(movimiento);
            return ResponseEntity.ok().body(movDto);
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }


}
