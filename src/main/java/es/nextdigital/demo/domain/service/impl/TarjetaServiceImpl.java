/**
 * Autor: Álvaro López Arcos
 * Fecha de creación: 30 jul 2025
 * Descripción: TarjetaServiceImpl.java
 *
 */
package es.nextdigital.demo.domain.service.impl;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import es.nextdigital.demo.domain.model.Tarjeta;
import es.nextdigital.demo.domain.repository.TarjetaRepository;
import es.nextdigital.demo.domain.service.TarjetaService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TarjetaServiceImpl implements TarjetaService {

	//he tenido que usar constructor manual pq no va lombok y estaba poerdiendo mucho tiempo

    private final TarjetaRepository tarjetaRepository;

    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
  
    public TarjetaServiceImpl(TarjetaRepository tarjetaRepository, BCryptPasswordEncoder passwordEncoder) {
        this.tarjetaRepository = tarjetaRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void activarTarjeta(Long id, String pin) {
        Tarjeta tarjeta = tarjetaRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Tarjeta no encontrada"));

        if (tarjeta.isActiva()) {
            throw new IllegalStateException("La tarjeta ya está activa");
        }

        tarjeta.setPin(passwordEncoder.encode(pin));
        tarjeta.setActiva(true);

        tarjetaRepository.save(tarjeta);
    }
}