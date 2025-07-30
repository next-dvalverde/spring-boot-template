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

    private final TarjetaRepository tarjetaRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

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