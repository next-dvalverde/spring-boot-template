package es.nextdigital.demo.application.service;

import es.nextdigital.demo.adapter.out.repositories.MovimientoRepository;
import es.nextdigital.demo.adapter.out.repositories.TarjetaRepository;
import es.nextdigital.demo.application.models.Movimiento;
import es.nextdigital.demo.application.models.Tarjeta;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MovimientoService {

    private final MovimientoRepository movimientoRepository;
    private final TarjetaRepository tarjetaRepository;

    public MovimientoService(MovimientoRepository movimientoRepository, TarjetaRepository tarjetaRepository) {
        this.movimientoRepository = movimientoRepository;
        this.tarjetaRepository = tarjetaRepository;
    }

    public List<Movimiento> obtenerMovimientosPorCuenta(Long cuentaId, String pin) {

        Tarjeta tarjeta = tarjetaRepository.findByCuentaAsociadaId(cuentaId);

        if (!BCrypt.checkpw(pin, tarjeta.getPin())) {
            throw new RuntimeException("El PIN introducido no es correcto");
        }

        return movimientoRepository.findByCuentaId(cuentaId);
    }

}
