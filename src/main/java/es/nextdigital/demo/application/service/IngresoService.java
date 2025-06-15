package es.nextdigital.demo.application.service;

import es.nextdigital.demo.adapter.in.rest.dto.*;
import es.nextdigital.demo.adapter.out.repositories.*;
import es.nextdigital.demo.application.models.*;
import es.nextdigital.demo.application.models.enums.TipoMovimiento;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class IngresoService {

    private final TarjetaRepository tarjetaRepo;
    private final CajeroRepository cajeroRepo;
    private final MovimientoRepository movimientoRepo;
    private final CuentaRepository cuentaRepo;

    public IngresoService(TarjetaRepository tarjetaRepo, CajeroRepository cajeroRepo,
                          MovimientoRepository movimientoRepo, CuentaRepository cuentaRepo) {
        this.tarjetaRepo = tarjetaRepo;
        this.cajeroRepo = cajeroRepo;
        this.movimientoRepo = movimientoRepo;
        this.cuentaRepo = cuentaRepo;
    }

    @Transactional
    public Movimiento ingresar(IngresoDTO dto) {
        Tarjeta tarjeta = tarjetaRepo.findByNumero(dto.getNumeroTarjeta())
                .orElseThrow(() -> new RuntimeException("Tarjeta no encontrada"));
        if (!tarjeta.isActiva()) {
            throw new RuntimeException("Tarjeta no activada");
        }

        if (!BCrypt.checkpw(dto.getPin(), tarjeta.getPin())) {
            throw new RuntimeException("El PIN introducido no es correcto");
        }

        Cajero cajero = cajeroRepo.findById(dto.getIdCajero())
                .orElseThrow(() -> new RuntimeException("Cajero no encontrado"));

        // Validar que ambos bancos son el mismo
        if (!tarjeta.getBanco().getId().equals(cajero.getBancoPropietario().getId())) {
            throw new RuntimeException("No se puede ingresar dinero en cajeros de otros bancos");
        }

        Cuenta cuenta = tarjeta.getCuentaAsociada();
        cuenta.setSaldo(cuenta.getSaldo() + dto.getCantidad());
        cuentaRepo.save(cuenta);

        Movimiento mov = new Movimiento();
        mov.setCuenta(cuenta);
        mov.setCantidad(dto.getCantidad());
        mov.setTipo(TipoMovimiento.valueOf("INGRESO"));
        mov.setDescripcion("Ingreso en cajero " + cajero.getBancoPropietario().getNombre());
        mov.setFecha(LocalDateTime.now().toString());
        movimientoRepo.save(mov);

        return mov;
    }

}
