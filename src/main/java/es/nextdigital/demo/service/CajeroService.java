package es.nextdigital.demo.service;

import es.nextdigital.demo.model.*;
import es.nextdigital.demo.repository.CuentaRepository;
import es.nextdigital.demo.repository.TarjetaRepository;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDateTime;

@Service
public class CajeroService {

    private final CuentaRepository cuentaRepository;
    private final TarjetaRepository tarjetaRepository;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public CajeroService(CuentaRepository cuentaRepository, TarjetaRepository tarjetaRepository) {
        this.cuentaRepository = cuentaRepository;
        this.tarjetaRepository = tarjetaRepository;
    }

    private Tarjeta obtenerTarjetaValida(Long tarjetaId) {
        Tarjeta tarjeta = tarjetaRepository.findById(tarjetaId);

        if (tarjeta == null) {
            throw new RuntimeException("Tarjeta no encontrada");
        }

        if (!tarjeta.isActivada()) {
            throw new RuntimeException("La tarjeta no está activada");
        }

        if (tarjeta.getPinPendienteCambio()) {
            throw new RuntimeException("Debe cambiar el PIN antes de realizar operaciones");
        }

        return tarjeta;
    }

    private Cuenta obtenerCuentaAsociada(Tarjeta tarjeta) {
        Cuenta cuenta = cuentaRepository.findById(tarjeta.getCuentaId());

        if (cuenta == null) {
            throw new RuntimeException("La cuenta asociada no existe");
        }

        return cuenta;
    }


    public void retirar(Long tarjetaId, Double cantidad, boolean esMismoBanco) {

        Tarjeta tarjeta = obtenerTarjetaValida(tarjetaId);
        Cuenta cuenta = obtenerCuentaAsociada(tarjeta);

        if (tarjeta.getRetiradoHoy() + cantidad > tarjeta.getLimiteDiario()) {
            throw new RuntimeException("Límite diario de la tarjeta superado");
        }

        double comision = esMismoBanco ? 0.0 : cantidad * 0.02;
        double total = cantidad + comision;

        if (tarjeta.getTipo() == TipoTarjeta.DEBITO) {
            if (cuenta.getSaldo() < total) {
                throw new RuntimeException("Saldo insuficiente");
            }
            cuenta.setSaldo(cuenta.getSaldo() - total);
        }

        if (tarjeta.getTipo() == TipoTarjeta.CREDITO) {
            if (tarjeta.getCreditoDisponible() < total) {
                throw new RuntimeException("Crédito insuficiente");
            }
            tarjeta.setCreditoDisponible(tarjeta.getCreditoDisponible() - total);
        }

        tarjeta.setRetiradoHoy(tarjeta.getRetiradoHoy() + cantidad);

        Movimiento mov = new Movimiento(
                System.currentTimeMillis(),
                LocalDateTime.now(),
                TipoMovimiento.RETIRADA,
                cantidad,
                esMismoBanco ?
                        "Retirada de efectivo" :
                        "Retirada en cajero externo (incluye comisión)",
                cuenta.getId()
        );

        cuenta.getMovimientos().add(mov);

        tarjetaRepository.save(tarjeta);
        cuentaRepository.save(cuenta);
    }

    public void ingresar(Long tarjetaId, Double cantidad, boolean esMismoBanco) {

        Tarjeta tarjeta = obtenerTarjetaValida(tarjetaId);

        if (!esMismoBanco) {
            throw new RuntimeException("No se pueden ingresar fondos desde cajeros de otras entidades");
        }

        Cuenta cuenta = obtenerCuentaAsociada(tarjeta);

        cuenta.setSaldo(cuenta.getSaldo() + cantidad);

        Movimiento mov = new Movimiento(
                System.currentTimeMillis(),
                LocalDateTime.now(),
                TipoMovimiento.INGRESO,
                cantidad,
                "Ingreso de efectivo en cajero del banco",
                cuenta.getId()
        );

        cuenta.getMovimientos().add(mov);

        cuentaRepository.save(cuenta);
    }

    public void transferir(Long tarjetaId, String ibanDestino, Double cantidad, boolean esMismoBancoDestino) {

        Tarjeta tarjeta = obtenerTarjetaValida(tarjetaId);
        Cuenta cuentaOrigen = obtenerCuentaAsociada(tarjeta);

        if (!IbanValidator.validarIBAN(ibanDestino)) {
            throw new RuntimeException("IBAN destino no válido");
        }

        Cuenta cuentaDestino = null;
        for (Cuenta c : cuentaRepository.findAll()) {
            if (c.getIban().equals(ibanDestino)) {
                cuentaDestino = c;
                break;
            }
        }

        if (cuentaDestino == null) {
            throw new RuntimeException("Cuenta destino no encontrada");
        }

        double comision = esMismoBancoDestino ? 0.0 : cantidad * 0.02;
        double total = cantidad + comision;

        if (tarjeta.getTipo() == TipoTarjeta.DEBITO) {
            if (cuentaOrigen.getSaldo() < total) {
                throw new RuntimeException("Saldo insuficiente para transferencia");
            }
            cuentaOrigen.setSaldo(cuentaOrigen.getSaldo() - total);
        }

        if (tarjeta.getTipo() == TipoTarjeta.CREDITO) {
            if (tarjeta.getCreditoDisponible() < total) {
                throw new RuntimeException("Crédito insuficiente");
            }
            tarjeta.setCreditoDisponible(tarjeta.getCreditoDisponible() - total);
        }

        cuentaDestino.setSaldo(cuentaDestino.getSaldo() + cantidad);

        Movimiento movSalida = new Movimiento(
                System.currentTimeMillis(),
                LocalDateTime.now(),
                TipoMovimiento.TRANSFERENCIA_SALIENTE,
                cantidad,
                esMismoBancoDestino ?
                        "Transferencia a cuenta del mismo banco" :
                        "Transferencia a otro banco (incluye comisión)",
                cuentaOrigen.getId()
        );
        cuentaOrigen.getMovimientos().add(movSalida);

        Movimiento movEntrada = new Movimiento(
                System.currentTimeMillis() + 1,
                LocalDateTime.now(),
                TipoMovimiento.TRANSFERENCIA_ENTRANTE,
                cantidad,
                "Transferencia recibida",
                cuentaDestino.getId()
        );
        cuentaDestino.getMovimientos().add(movEntrada);

        cuentaRepository.save(cuentaOrigen);
        cuentaRepository.save(cuentaDestino);
        tarjetaRepository.save(tarjeta);
    }

    public void activarTarjeta(Long tarjetaId) {
        Tarjeta tarjeta = tarjetaRepository.findById(tarjetaId);

        if (tarjeta == null) {
            throw new RuntimeException("Tarjeta no encontrada");
        }

        if (tarjeta.isActivada()) {
            throw new RuntimeException("La tarjeta ya estaba activada");
        }

        tarjeta.setActivada(true);
        tarjeta.setPinPendienteCambio(true);
        tarjetaRepository.save(tarjeta);
    }

    public void cambiarPin(Long tarjetaId, String pinActual, String pinNuevo) {

        Tarjeta tarjeta = tarjetaRepository.findById(tarjetaId);

        if (tarjeta == null) {
            throw new RuntimeException("Tarjeta no encontrada");
        }

        if (!tarjeta.getPinPendienteCambio()) {
            if (!encoder.matches(pinActual, tarjeta.getPinHash())) {
                throw new RuntimeException("El PIN actual no es correcto");
            }
        }

        // Guardamos el nuevo PIN hasheado
        tarjeta.setPinHash(encoder.encode(pinNuevo));
        tarjeta.setPinPendienteCambio(false);

        tarjetaRepository.save(tarjeta);
    }

}