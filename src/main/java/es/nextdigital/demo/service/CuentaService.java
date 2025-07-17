package es.nextdigital.demo.service;

import es.nextdigital.demo.dto.MovimientoDTO;
import es.nextdigital.demo.entity.Cuenta;
import es.nextdigital.demo.mapper.Mapper;
import es.nextdigital.demo.repository.CuentaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CuentaService implements ICuentaService {

    private final CuentaRepository cuentaRepository;

    public List<MovimientoDTO> getMovimientosByCuenta(String iban) {
        Cuenta cuenta = cuentaRepository.findById(iban)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cuenta no encontrada"));

        return cuenta.getMovimientos().stream()
                .map(Mapper::toMovimientoDTO)
                .toList();
    }

    @Override
    public Cuenta obtenerCuenta(String iban) {
        return cuentaRepository.findById(iban)
                .orElseThrow(() -> new IllegalArgumentException("Cuenta no encontrada"));
    }

    @Override
    public boolean tieneSaldoSuficiente(Cuenta cuenta, BigDecimal cantidad) {
        return cuenta.getSaldo().compareTo(cantidad) >= 0;
    }

    @Override
    public void descontarSaldo(Cuenta cuenta, BigDecimal cantidad) {
        cuenta.setSaldo(cuenta.getSaldo().subtract(cantidad));
        cuentaRepository.save(cuenta);
    }

    @Override
    public void abonarSaldo(Cuenta cuenta, BigDecimal cantidad) {
        cuenta.setSaldo(cuenta.getSaldo().add(cantidad));
        cuentaRepository.save(cuenta);
    }
}
