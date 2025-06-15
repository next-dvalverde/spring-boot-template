package es.nextdigital.demo.application.service;

import es.nextdigital.demo.adapter.in.rest.dto.ActivarTarjetaDTO;
import es.nextdigital.demo.adapter.in.rest.dto.CambiarPinDTO;
import es.nextdigital.demo.adapter.out.repositories.TarjetaRepository;
import es.nextdigital.demo.application.models.Tarjeta;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

@Service
public class TarjetaService {

    private final TarjetaRepository tarjetaRepo;

    public TarjetaService(TarjetaRepository tarjetaRepo) {
        this.tarjetaRepo = tarjetaRepo;
    }


    @Transactional
    public void cambiarPin(CambiarPinDTO dto) {
        Tarjeta tarjeta = tarjetaRepo.findByNumero(dto.getNumeroTarjeta())
                .orElseThrow(() -> new RuntimeException("Tarjeta no encontrada"));

        //Validacion Pin Actual
        if (!BCrypt.checkpw(dto.getPinActual(), tarjeta.getPin())) {
            throw new RuntimeException("El PIN introducido no es correcto");
        }

        String nuevoPinCifrado = BCrypt.hashpw(dto.getPinNuevo(), BCrypt.gensalt());

        tarjeta.setPin(nuevoPinCifrado);
        if (!tarjeta.isActiva()){
            tarjeta.setActiva(true);
        }

        tarjetaRepo.save(tarjeta);
    }

    @Transactional
    public void activarTarjeta(ActivarTarjetaDTO dto) {
        Tarjeta tarjeta = tarjetaRepo.findByNumero(dto.getNumeroTarjeta())
                .orElseThrow(() -> new RuntimeException("Tarjeta no encontrada"));

        if (!BCrypt.checkpw(dto.getPin(), tarjeta.getPin())) {
            throw new RuntimeException("El PIN introducido no es correcto");
        }

        if (tarjeta.isActiva()) {
            throw new RuntimeException("La tarjeta ya está activada");
        }

        tarjeta.setActiva(true);
        tarjetaRepo.save(tarjeta);
    }
}
