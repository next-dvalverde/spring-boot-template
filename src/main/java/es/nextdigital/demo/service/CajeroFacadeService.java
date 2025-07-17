package es.nextdigital.demo.service;

import es.nextdigital.demo.entity.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class CajeroFacadeService implements ICajeroFacadeService {

    private final ITarjetaService tarjetaService;
    private final ICuentaService cuentaService;
    private final IBancoService bancoService;
    private final IMovimientoService movimientoService;

    @Override
    @Transactional
    public void retirarDinero(String numeroTarjeta, String pinHash, BigDecimal cantidad, Banco bancoCajero) {
        Tarjeta tarjeta = tarjetaService.obtenerTarjeta(numeroTarjeta);
        tarjetaService.validarTarjetaActiva(tarjeta);
        tarjetaService.validarPin(tarjeta, pinHash);
        tarjetaService.validarLimiteRetirada(tarjeta, cantidad);

        Cuenta cuenta = tarjeta.getCuentaAsociada();

        boolean esPropio = bancoService.esCajeroPropio(tarjeta.getBancoEmisor(), bancoCajero);
        BigDecimal comision = esPropio ? BigDecimal.ZERO : bancoService.calcularComisionCajeroAjeno(bancoCajero, cantidad);

        BigDecimal totalADescontar = cantidad.add(comision);

        if (tarjeta.getTipo() == Tarjeta.TarjetaTipo.DEBITO) {
            if (!cuentaService.tieneSaldoSuficiente(cuenta, totalADescontar)) {
                throw new IllegalStateException("Saldo insuficiente");
            }
            cuentaService.descontarSaldo(cuenta, totalADescontar);

        } else { // CREDITO
            BigDecimal disponible = cuenta.getSaldo().add(tarjeta.getCreditoDisponible());
            if (disponible.compareTo(totalADescontar) < 0) {
                throw new IllegalStateException("Crédito insuficiente");
            }

            BigDecimal falta = totalADescontar.subtract(cuenta.getSaldo());
            if (falta.compareTo(BigDecimal.ZERO) > 0) {
                tarjetaService.consumirCredito(tarjeta, falta);
                cuenta.setSaldo(BigDecimal.ZERO);
            } else {
                cuentaService.descontarSaldo(cuenta, totalADescontar);
            }
        }

        movimientoService.registrarMovimiento(cuenta, cantidad.negate(), Movimiento.TipoMovimiento.RETIRADA, "Retirada en cajero");
        if (comision.compareTo(BigDecimal.ZERO) > 0) {
            movimientoService.registrarMovimiento(cuenta, comision.negate(), Movimiento.TipoMovimiento.COMISION, "Comisión cajero ajeno");
        }
    }

    @Override
    @Transactional
    public void ingresarDinero(String numeroTarjeta, String pinHash, BigDecimal cantidad, Banco bancoCajero) {
        Tarjeta tarjeta = tarjetaService.obtenerTarjeta(numeroTarjeta);
        tarjetaService.validarTarjetaActiva(tarjeta);
        tarjetaService.validarPin(tarjeta, pinHash);

        if (!bancoService.esCajeroPropio(tarjeta.getBancoEmisor(), bancoCajero)) {
            throw new IllegalArgumentException("No se pueden hacer ingresos en cajeros de otro banco");
        }

        Cuenta cuenta = tarjeta.getCuentaAsociada();
        cuentaService.abonarSaldo(cuenta, cantidad);

        movimientoService.registrarMovimiento(cuenta, cantidad, Movimiento.TipoMovimiento.INGRESO, "Ingreso en cajero propio");
    }

    @Override
    @Transactional
    public void transferir(String numeroTarjeta, String pinHash, String ibanDestino, BigDecimal cantidad) {
        Tarjeta tarjeta = tarjetaService.obtenerTarjeta(numeroTarjeta);
        tarjetaService.validarTarjetaActiva(tarjeta);
        tarjetaService.validarPin(tarjeta, pinHash);

        Cuenta origen = tarjeta.getCuentaAsociada();
        Cuenta destino = cuentaService.obtenerCuenta(ibanDestino);

        boolean mismoBanco = origen.getBanco().getId().equals(destino.getBanco().getId());
        BigDecimal comision = bancoService.calcularComisionTransferencia(mismoBanco, cantidad);
        BigDecimal totalADescontar = cantidad.add(comision);

        if (!cuentaService.tieneSaldoSuficiente(origen, totalADescontar)) {
            throw new IllegalStateException("Saldo insuficiente para transferencia");
        }

        cuentaService.descontarSaldo(origen, totalADescontar);
        cuentaService.abonarSaldo(destino, cantidad);

        movimientoService.registrarMovimiento(origen, cantidad.negate(), Movimiento.TipoMovimiento.TRANSFERENCIA_SALIENTE,
                "Transferencia a " + ibanDestino);
        if (comision.compareTo(BigDecimal.ZERO) > 0) {
            movimientoService.registrarMovimiento(origen, comision.negate(), Movimiento.TipoMovimiento.COMISION,
                    "Comisión transferencia a otro banco");
        }
        movimientoService.registrarMovimiento(destino, cantidad, Movimiento.TipoMovimiento.TRANSFERENCIA_ENTRANTE,
                "Transferencia recibida de " + origen.getIban());
    }
}
