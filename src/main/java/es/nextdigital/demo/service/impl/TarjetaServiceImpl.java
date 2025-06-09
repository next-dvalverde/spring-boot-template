package es.nextdigital.demo.service.impl;

import es.nextdigital.demo.entity.Tarjeta;
import es.nextdigital.demo.repository.TarjetaRepository;
import es.nextdigital.demo.service.TarjetaService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * The Class TarjetaServiceImpl.
 */
@Service
@RequiredArgsConstructor
public class TarjetaServiceImpl implements TarjetaService {

	@Autowired
	private TarjetaRepository tarjetaRepo;

	@Override
	public void activarTarjeta(Long tarjetaId, String nuevoPin) {
		Tarjeta tarjeta = tarjetaRepo.findById(tarjetaId)
				.orElseThrow(() -> new RuntimeException("Tarjeta no encontrada"));
		tarjeta.setActiva(true);
		tarjeta.setPin(nuevoPin);
		tarjetaRepo.save(tarjeta);
	}

	@Override
	public void cambiarPin(Long tarjetaId, String nuevoPin) {
		Tarjeta tarjeta = tarjetaRepo.findById(tarjetaId)
				.orElseThrow(() -> new RuntimeException("Tarjeta no encontrada"));
		tarjeta.setPin(nuevoPin);
		tarjetaRepo.save(tarjeta);
	}

}
