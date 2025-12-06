package es.nextdigital.demo.service;

import es.nextdigital.demo.model.Cuenta;
import es.nextdigital.demo.model.Movimiento;
import es.nextdigital.demo.repository.CuentaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CuentaService {

    private final CuentaRepository cuentaRepository;

    public CuentaService(CuentaRepository cuentaRepository) {
        this.cuentaRepository = cuentaRepository;
    }

    public List<Movimiento> obtenerMovimientos(Long cuentaId) {
        Cuenta cuenta = cuentaRepository.findById(cuentaId);

        if (cuenta == null) {
            throw new RuntimeException("La cuenta no existe");
        }

        return cuenta.getMovimientos();
    }
}
