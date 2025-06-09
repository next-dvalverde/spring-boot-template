package es.nextdigital.demo.service.impl;

import es.nextdigital.demo.dto.MovimientoDTO;
import es.nextdigital.demo.dto.OperacionIngresoDTO;
import es.nextdigital.demo.dto.OperacionRetiroDTO;
import es.nextdigital.demo.dto.TransferenciaDTO;
import es.nextdigital.demo.entity.Cuenta;
import es.nextdigital.demo.entity.Movimiento;
import es.nextdigital.demo.entity.Tarjeta;
import es.nextdigital.demo.enums.TipoMovimiento;
import es.nextdigital.demo.enums.TipoTarjeta;
import es.nextdigital.demo.repository.CuentaRepository;
import es.nextdigital.demo.repository.MovimientoRepository;
import es.nextdigital.demo.repository.TarjetaRepository;
import es.nextdigital.demo.service.OperacionService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The Class OperacionServiceImpl.
 */
@Service
@RequiredArgsConstructor
public class OperacionServiceImpl implements OperacionService {

	@Autowired
	private TarjetaRepository tarjetaRepo;
	@Autowired
	private CuentaRepository cuentaRepo;
	@Autowired
	private MovimientoRepository movimientoRepo;

	private void validarTarjetaActiva(Tarjeta tarjeta) {
		if (!tarjeta.isActiva()) throw new RuntimeException("Tarjeta no está activa");
	}

	@Override
	public List<MovimientoDTO> consultarMovimientos(Long cuentaId) {
		Cuenta cuenta = cuentaRepo.findById(cuentaId)
				.orElseThrow(() -> new RuntimeException("Cuenta no encontrada"));
		return cuenta.getMovimientos().stream()
				.map(m -> {
					MovimientoDTO dto = new MovimientoDTO();
					dto.setFecha(m.getFecha());
					dto.setTipo(m.getTipo());
					dto.setDescripcion(m.getDescripcion());
					dto.setCantidad(m.getCantidad());
					return dto;
				})
				.collect(Collectors.toList());
	}

	@Override
	public void retirar(OperacionRetiroDTO dto) {
		Tarjeta tarjeta = tarjetaRepo.findById(dto.tarjetaId)
				.orElseThrow(() -> new RuntimeException("Tarjeta no encontrada"));
		validarTarjetaActiva(tarjeta);

		Cuenta cuenta = tarjeta.getCuenta();
		BigDecimal total = dto.getCantidad();

		if (!dto.isCajeroPropio()) {
			total = total.add(BigDecimal.valueOf(2)); // comisión
		}

		if (tarjeta.getTipo() == TipoTarjeta.DEBITO) {
			if (cuenta.getSaldo().compareTo(total) < 0)
				throw new RuntimeException("Saldo insuficiente");
		} else {
			if (tarjeta.getLimite().compareTo(total) < 0)
				throw new RuntimeException("Límite de crédito excedido");
		}

		cuenta.setSaldo(cuenta.getSaldo().subtract(total));
		cuentaRepo.save(cuenta);

		registrarMovimiento(cuenta, dto.getCantidad().negate(), TipoMovimiento.RETIRADA, "Retiro en cajero");

		if (!dto.isCajeroPropio()) {
			registrarMovimiento(cuenta, BigDecimal.valueOf(-2), TipoMovimiento.COMISION, "Comisión cajero externo");
		}
	}

	@Override
	public void ingresar(OperacionIngresoDTO dto) {
		if (!dto.isCajeroPropio()) throw new RuntimeException("No se permiten ingresos en cajeros externos");

		Tarjeta tarjeta = tarjetaRepo.findById(dto.tarjetaId)
				.orElseThrow(() -> new RuntimeException("Tarjeta no encontrada"));
		validarTarjetaActiva(tarjeta);

		Cuenta cuenta = tarjeta.getCuenta();
		cuenta.setSaldo(cuenta.getSaldo().add(dto.getCantidad()));
		cuentaRepo.save(cuenta);

		registrarMovimiento(cuenta, dto.getCantidad(), TipoMovimiento.INGRESO, "Ingreso en cajero");
	}

	@Override
	public void transferir(TransferenciaDTO dto) {
		Tarjeta tarjeta = tarjetaRepo.findById(dto.getTarjetaId())
				.orElseThrow(() -> new RuntimeException("Tarjeta no encontrada"));
		validarTarjetaActiva(tarjeta);

		Cuenta origen = tarjeta.getCuenta();
		Cuenta destino = cuentaRepo.findByIban(dto.getIbanDestino())
				.orElseThrow(() -> new RuntimeException("Cuenta destino no encontrada"));

		BigDecimal comision = origen.getIban().substring(0, 4)
				.equals(destino.getIban().substring(0, 4)) ? BigDecimal.ZERO : dto.getCantidad().multiply(BigDecimal.valueOf(0.01));

		BigDecimal total = dto.getCantidad().add(comision);
		if (origen.getSaldo().compareTo(total) < 0)
			throw new RuntimeException("Saldo insuficiente");

		origen.setSaldo(origen.getSaldo().subtract(total));
		destino.setSaldo(destino.getSaldo().add(dto.getCantidad()));
		cuentaRepo.save(origen);
		cuentaRepo.save(destino);

		registrarMovimiento(origen, dto.getCantidad().negate(), TipoMovimiento.TRANSFERENCIA_SALIENTE, "Transferencia a " + dto.getIbanDestino());
		registrarMovimiento(destino, dto.getCantidad(), TipoMovimiento.TRANSFERENCIA_ENTRANTE, "Transferencia recibida");

		if (comision.compareTo(BigDecimal.ZERO) > 0) {
			registrarMovimiento(origen, comision.negate(), TipoMovimiento.COMISION, "Comisión por transferencia externa");
		}
	}

	private void registrarMovimiento(Cuenta cuenta, BigDecimal cantidad, TipoMovimiento tipo, String descripcion) {
		Movimiento m = new Movimiento();
		m.setCuenta(cuenta);
		m.setCantidad(cantidad);
		m.setFecha(LocalDateTime.now());
		m.setTipo(tipo);
		m.setDescripcion(descripcion);
		movimientoRepo.save(m);
	}

}
