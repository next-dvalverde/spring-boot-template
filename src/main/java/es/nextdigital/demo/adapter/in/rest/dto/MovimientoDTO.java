package es.nextdigital.demo.adapter.in.rest.dto;

import es.nextdigital.demo.application.models.enums.TipoMovimiento;
import lombok.Data;


@Data
public class MovimientoDTO {
    private Long id;
    private Double cantidad;
    private String descripcion;
    private TipoMovimiento tipo;
    private String fecha;
}
