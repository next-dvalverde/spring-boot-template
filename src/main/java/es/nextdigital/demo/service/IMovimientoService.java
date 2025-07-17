package es.nextdigital.demo.service;

import es.nextdigital.demo.entity.Cuenta;
import es.nextdigital.demo.entity.Movimiento;

import java.math.BigDecimal;

public interface IMovimientoService {

    void registrarMovimiento(Cuenta cuenta, BigDecimal cantidad, Movimiento.TipoMovimiento tipo, String desc);

}
