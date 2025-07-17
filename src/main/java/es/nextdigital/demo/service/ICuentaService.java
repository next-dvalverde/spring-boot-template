package es.nextdigital.demo.service;
import es.nextdigital.demo.dto.MovimientoDTO;
import es.nextdigital.demo.entity.Cuenta;

import java.math.BigDecimal;
import java.util.List;

public interface ICuentaService {

    List<MovimientoDTO> getMovimientosByCuenta(String iban);

    Cuenta obtenerCuenta(String iban);

    boolean tieneSaldoSuficiente(Cuenta cuenta, BigDecimal cantidad);

    void descontarSaldo(Cuenta cuenta, BigDecimal cantidad);

    void abonarSaldo(Cuenta cuenta, BigDecimal cantidad);
}
