package es.nextdigital.demo.application.service;

import es.nextdigital.demo.adapter.in.rest.dto.RetiradaDTO;
import es.nextdigital.demo.adapter.out.repositories.*;
import es.nextdigital.demo.application.models.*;
import es.nextdigital.demo.application.models.enums.TipoMovimiento;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class RetiradaService {
    private final TarjetaRepository tarjetaRepo;
    private final CajeroRepository cajeroRepo;
    private final MovimientoRepository movimientoRepo;
    private final CuentaRepository cuentaRepo;

    public RetiradaService(TarjetaRepository tarjetaRepo, CajeroRepository cajeroRepo,
                           MovimientoRepository movimientoRepo, CuentaRepository cuentaRepo) {
        this.tarjetaRepo = tarjetaRepo;
        this.cajeroRepo = cajeroRepo;
        this.movimientoRepo = movimientoRepo;
        this.cuentaRepo = cuentaRepo;
    }

    @Transactional
    public Movimiento retirar(RetiradaDTO dto) {
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

        Double comision = 0.0;
        if (!tarjeta.getBanco().getId().equals(cajero.getBancoPropietario().getId())) {
            comision = cajero.getComisionOtraEntidad();
        }

        Double cantidadTotal = dto.getCantidad() + comision;
        Cuenta cuenta = tarjeta.getCuentaAsociada();

        if (dto.getCantidad() > tarjeta.getLimite()) {
            throw new RuntimeException("Límite de tarjeta superado");
        }

        if (tarjeta.getTipo().equalsIgnoreCase("DEBITO")) {
            if (cuenta.getSaldo() < cantidadTotal) {
                throw new RuntimeException("Saldo insuficiente");
            }
            cuenta.setSaldo(cuenta.getSaldo() - cantidadTotal);
        } else if (tarjeta.getTipo().equalsIgnoreCase("CREDITO")) {
            if (tarjeta.getDisponible() < cantidadTotal) {
                throw new RuntimeException("Crédito insuficiente");
            }
            tarjeta.setDisponible(tarjeta.getDisponible() - cantidadTotal);
            cuenta.setSaldo(cuenta.getSaldo() - cantidadTotal);
        }

        cuentaRepo.save(cuenta);
        tarjetaRepo.save(tarjeta);

        // Registra movimiento principal
        Movimiento mov = new Movimiento();
        mov.setCuenta(cuenta);
        mov.setCantidad(dto.getCantidad());
        mov.setTipo(TipoMovimiento.valueOf("RETIRADA"));
        mov.setDescripcion("Retirada en cajero " + cajero.getBancoPropietario().getNombre());
        mov.setFecha(LocalDateTime.now().toString());
        movimientoRepo.save(mov);

        // Si hay comisión, crea y guarda movimiento de comisión
        if (comision > 0) {
            Movimiento movCom = new Movimiento();
            movCom.setCuenta(cuenta);
            movCom.setCantidad(comision);
            movCom.setTipo(TipoMovimiento.valueOf("COMISION"));
            movCom.setDescripcion("Comisión por retirada en otro banco");
            movCom.setFecha(LocalDateTime.now().toString());
            movimientoRepo.save(movCom);
        }

        return mov;
    }
}

