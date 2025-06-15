package es.nextdigital.demo.application.service;

import es.nextdigital.demo.adapter.in.rest.dto.*;
import es.nextdigital.demo.adapter.out.repositories.*;
import es.nextdigital.demo.application.models.*;
import es.nextdigital.demo.application.models.enums.TipoMovimiento;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class TransferenciaService {

    private final TarjetaRepository tarjetaRepo;
    private final CuentaRepository cuentaRepo;
    private final MovimientoRepository movimientoRepo;
    private final BancoRepository bancoRepo;

    //Usamos 2.5 como ejemplo de la comision a otro banco
    private static final double COMISION_OTRO_BANCO = 2.50;

    public TransferenciaService(TarjetaRepository tarjetaRepo, CuentaRepository cuentaRepo,
                                MovimientoRepository movimientoRepo, BancoRepository bancoRepo) {
        this.tarjetaRepo = tarjetaRepo;
        this.cuentaRepo = cuentaRepo;
        this.movimientoRepo = movimientoRepo;
        this.bancoRepo = bancoRepo;
    }

    @Transactional
    public Movimiento transferir(TransferenciaDTO dto) {

        Tarjeta tarjeta = tarjetaRepo.findByNumero(dto.getNumeroTarjeta())
                .orElseThrow(() -> new RuntimeException("Tarjeta no encontrada"));

        if (!tarjeta.isActiva()) {
            throw new RuntimeException("Tarjeta no activada");
        }

        Cuenta cuentaOrigen = tarjeta.getCuentaAsociada();

        Cuenta cuentaDestino = cuentaRepo.findByIban(dto.getIbanDestino())
                .orElseThrow(() -> new RuntimeException("Cuenta de destino no encontrada"));

        if (!validarIBAN(dto.getIbanDestino())) {
            throw new RuntimeException("El IBAN de destino no es válido");
        }

        boolean esMismoBanco = cuentaOrigen.getBanco().getId().equals(cuentaDestino.getBanco().getId());
        double comision = esMismoBanco ? 0.0 : COMISION_OTRO_BANCO;
        double totalDescontar = dto.getCantidad() + comision;

        if (cuentaOrigen.getSaldo() < totalDescontar) {
            throw new RuntimeException("Saldo insuficiente para la transferencia");
        }

        cuentaOrigen.setSaldo(cuentaOrigen.getSaldo() - totalDescontar);
        cuentaDestino.setSaldo(cuentaDestino.getSaldo() + dto.getCantidad());
        cuentaRepo.save(cuentaOrigen);
        cuentaRepo.save(cuentaDestino);

        Movimiento movSalida = new Movimiento();
        movSalida.setCuenta(cuentaOrigen);
        movSalida.setCantidad(dto.getCantidad());
        movSalida.setTipo(TipoMovimiento.valueOf("TRANSFERENCIA_SALIENTE"));
        movSalida.setDescripcion("Transferencia a " + dto.getIbanDestino());
        movSalida.setFecha(LocalDateTime.now().toString());
        movimientoRepo.save(movSalida);

        Movimiento movEntrada = new Movimiento();
        movEntrada.setCuenta(cuentaDestino);
        movEntrada.setCantidad(dto.getCantidad());
        movEntrada.setTipo(TipoMovimiento.valueOf("TRANSFERENCIA_ENTRANTE"));
        movEntrada.setDescripcion("Transferencia recibida de " + cuentaOrigen.getIban());
        movEntrada.setFecha(LocalDateTime.now().toString());
        movimientoRepo.save(movEntrada);

        if (comision > 0) {
            Movimiento movCom = new Movimiento();
            movCom.setCuenta(cuentaOrigen);
            movCom.setCantidad(comision);
            movCom.setTipo(TipoMovimiento.valueOf("COMISION"));
            movCom.setDescripcion("Comisión por transferencia a otro banco");
            movCom.setFecha(LocalDateTime.now().toString());
            movimientoRepo.save(movCom);
        }

        return movSalida;
    }

    // Validación simple de IBAN (comienza por 2 letras + 22 digitos)
    private boolean validarIBAN(String iban) {
        return iban != null && iban.matches("^[A-Z]{2}\\d{22}$");
    }

}
