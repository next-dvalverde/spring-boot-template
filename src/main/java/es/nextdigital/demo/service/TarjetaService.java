package es.nextdigital.demo.service;

import es.nextdigital.demo.entity.Tarjeta;
import es.nextdigital.demo.repository.TarjetaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class TarjetaService implements ITarjetaService {

    private final TarjetaRepository tarjetaRepository;
    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();


    @Override
    public Tarjeta obtenerTarjeta(String numeroTarjeta) {
        return tarjetaRepository.findById(numeroTarjeta)
                .orElseThrow(() -> new IllegalArgumentException("Tarjeta no encontrada"));
    }

    @Override
    public void validarTarjetaActiva(Tarjeta tarjeta) {
        if (!tarjeta.isActivada()) {
            throw new IllegalStateException("La tarjeta no está activada");
        }
    }

    @Override
    public void validarPin(Tarjeta tarjeta, String pinPlano) {
        if (!passwordEncoder.matches(pinPlano, tarjeta.getPinHash())) {
            throw new SecurityException("PIN incorrecto");
        }
    }

    @Override
    @Transactional
    public void activarTarjeta(String numeroTarjeta, String pinPlano) {
        Tarjeta tarjeta = obtenerTarjeta(numeroTarjeta);
        if (tarjeta.isActivada()) {
            throw new IllegalStateException("La tarjeta ya está activada");
        }
        tarjeta.setPinHash(passwordEncoder.encode(pinPlano));
        tarjeta.setActivada(true);
        tarjetaRepository.save(tarjeta);
    }

    @Override
    @Transactional
    public void cambiarPin(String numeroTarjeta, String pinAntiguo, String pinNuevo) {
        Tarjeta tarjeta = obtenerTarjeta(numeroTarjeta);
        validarPin(tarjeta, pinAntiguo);
        tarjeta.setPinHash(passwordEncoder.encode(pinNuevo));
        tarjetaRepository.save(tarjeta);
    }

    @Override
    public void validarLimiteRetirada(Tarjeta tarjeta, BigDecimal cantidad) {
        if (cantidad.compareTo(tarjeta.getLimiteRetirada()) > 0) {
            throw new IllegalArgumentException("Cantidad supera el límite configurado para esta tarjeta");
        }
    }

    @Override
    @Transactional
    public void cambiarLimiteRetirada(String numeroTarjeta, BigDecimal nuevoLimite) {
        if (nuevoLimite.compareTo(BigDecimal.valueOf(500)) < 0 ||
                nuevoLimite.compareTo(BigDecimal.valueOf(6000)) > 0) {
            throw new IllegalArgumentException("El límite debe estar entre 500 y 6000 euros");
        }
        Tarjeta tarjeta = obtenerTarjeta(numeroTarjeta);
        tarjeta.setLimiteRetirada(nuevoLimite);
        tarjetaRepository.save(tarjeta);
    }

    @Override
    @Transactional
    public void consumirCredito(Tarjeta tarjeta, BigDecimal cantidadCredito) {
        tarjeta.setCreditoUsado(tarjeta.getCreditoUsado().add(cantidadCredito));
        tarjetaRepository.save(tarjeta);
    }
}
