package es.nextdigital.demo.service;

/**
 * The Interface TarjetaService.
 */
public interface TarjetaService {

    void activarTarjeta(Long tarjetaId, String nuevoPin);

    void cambiarPin(Long tarjetaId, String nuevoPin);
}
