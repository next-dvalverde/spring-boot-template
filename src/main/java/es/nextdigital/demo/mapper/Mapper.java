package es.nextdigital.demo.mapper;

import es.nextdigital.demo.dto.CuentaDTO;
import es.nextdigital.demo.dto.MovimientoDTO;
import es.nextdigital.demo.entity.Cuenta;
import es.nextdigital.demo.entity.Movimiento;

public class Mapper {

    public static MovimientoDTO toMovimientoDTO(Movimiento m) {
        return new MovimientoDTO(
                m.getId(),
                m.getFecha(),
                m.getCantidad(),
                m.getTipo().name(),
                m.getDescripcion(),
                m.getCuenta() != null ? m.getCuenta().getIban() : null
        );
    }

    public static CuentaDTO toCuentaDTO(Cuenta c) {
        return new CuentaDTO(
                c.getIban(),
                c.getSaldo()
        );
    }
}
