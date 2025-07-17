package es.nextdigital.demo.service;

import es.nextdigital.demo.entity.Cuenta;
import es.nextdigital.demo.entity.Movimiento;
import es.nextdigital.demo.repository.MovimientoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class MovimientoService implements IMovimientoService {

    private final MovimientoRepository movimientoRepository;

    public void registrarMovimiento(Cuenta cuenta, BigDecimal cantidad, Movimiento.TipoMovimiento tipo, String desc) {
        Movimiento mov = new Movimiento();
        mov.setCuenta(cuenta);
        mov.setCantidad(cantidad);
        mov.setTipo(tipo);
        mov.setDescripcion(desc);
        mov.setFecha(LocalDateTime.now());
        movimientoRepository.save(mov);
    }
}
